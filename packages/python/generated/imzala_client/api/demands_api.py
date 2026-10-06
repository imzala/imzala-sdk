"""
    imzala External API

    imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.10.2 · **Son güncelleme:** 2026-10-06  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API anahtarı kendi organizasyonuna bağlıdır: `X-Workspace-Id` başlığı gönderilmezse anahtarın organizasyonu otomatik uygulanır; gönderilirse anahtarın organizasyonuyla aynı olmalıdır (aksi halde 403 `WORKSPACE_MISMATCH`). Kişisel anahtarlar için bu başlık gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Onaylayan (onay adımı) Şablonda bir taraf **Onaylayan** olarak işaretlenmişse, o şablondan oluşturulan sözleşmede bu taraf belgeyi imzalamaz, onaylar. Onaylayan içeren sözleşmelerde davetler iki aşamada gönderilir: - Sözleşme oluşturulurken (`POST /api/v1/demands`,   `POST /api/v1/demands/bulk`) ve `POST /api/v1/demands/{demandId}/dispatch`   çağrıldığında önce yalnızca onay bekleyen onaylayanlar davet edilir.   Sıralı sözleşmede (`ordered: true`) onaylayanlar da kendi sıralarıyla   davet edilir. - İmzacılar, tüm onaylayanlar onayladıktan sonra otomatik olarak davet   edilir. - Bir onaylayan sözleşmeyi reddederse imzacılar davet edilmez;   `dispatch` çağrısı da kimseye davet göndermez. - Onay vermiş bir onaylayan `dispatch` ile yeniden davet edilmez.  Onaylayan içermeyen sözleşmelerde davet davranışı değişmez.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |----|-------|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  `403 AI_TOOL_ACCESS_DISABLED`: organizasyon yöneticisi, organizasyonun yapay zekâ araçlarıyla erişimini kapatmıştır. Bu kontrol resmî İmzala MCP istemcisini (`User-Agent` başlığı `imzala-mcp/` ile başlayan istekler) tanır ve yalnız organizasyon çalışma alanını hedefleyen isteklerde uygulanır. Yanıt gövdesi:  ```json {   \"error\": \"AI_TOOL_ACCESS_DISABLED\",   \"code\": \"AI_TOOL_ACCESS_DISABLED\",   \"message\": \"Kurumunuz yapay zekâ araçlarının erişimini kapattı.\" } ```  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez. - Hatırlatma yalnızca davet sırası gelmiş taraflara gönderilir. Onaylayan   onayını beklerken imzacılar, sıralı sözleşmede sırası henüz gelmemiş   taraflar ve bir onaylayan reddettiyse tüm taraflar `details[]` içinde   `skipped` olarak `reason: \"party_not_eligible\"` ile döner.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (11) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı | | `contract.expiring` | Takip edilen sözleşme bitiş tarihine yaklaşıyor | | `contract.ended` | Takip edilen sözleşmenin platformdaki bitiş tarihi geçti | | `contract.advanced` | Otomatik yenilenen sözleşmenin takip edilen bitişi ileri alındı |  > **Not (contract.\\* olayları):** Bunlar platformun **takip amaçlı** > kayıtlarıdır; sözleşmenin hukuken yenilendiğini veya sona erdiğini > BELİRTMEZ. Detay için `ContractTerm` şemasına ve `state` alanına bakın.  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type`: yukarıdaki 11 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 

    The version of the OpenAPI document: 1.10.2
    Contact: destek@imzala.org
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


import warnings
from pydantic import validate_call, Field, StrictFloat, StrictStr, StrictInt
from typing import Any, Dict, List, Optional, Tuple, Union
from typing_extensions import Annotated

from datetime import date
from pydantic import Field, StrictBytes, StrictInt, StrictStr, field_validator
from typing import List, Optional, Tuple, Union
from typing_extensions import Annotated
from uuid import UUID
from imzala_client.models.api_v1_demands_bulk_post200_response import ApiV1DemandsBulkPost200Response
from imzala_client.models.api_v1_demands_bulk_post_request import ApiV1DemandsBulkPostRequest
from imzala_client.models.api_v1_demands_demand_id_dispatch_post200_response import ApiV1DemandsDemandIdDispatchPost200Response
from imzala_client.models.api_v1_demands_demand_id_dispatch_post_request import ApiV1DemandsDemandIdDispatchPostRequest
from imzala_client.models.api_v1_demands_demand_id_documents_doc_id_assignments_put_request import ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest
from imzala_client.models.api_v1_demands_demand_id_documents_doc_id_patch_request import ApiV1DemandsDemandIdDocumentsDocIdPatchRequest
from imzala_client.models.api_v1_demands_demand_id_documents_get200_response import ApiV1DemandsDemandIdDocumentsGet200Response
from imzala_client.models.api_v1_demands_demand_id_documents_order_put_request import ApiV1DemandsDemandIdDocumentsOrderPutRequest
from imzala_client.models.api_v1_demands_demand_id_documents_post201_response import ApiV1DemandsDemandIdDocumentsPost201Response
from imzala_client.models.api_v1_demands_demand_id_documents_post_request import ApiV1DemandsDemandIdDocumentsPostRequest
from imzala_client.models.api_v1_demands_get200_response import ApiV1DemandsGet200Response
from imzala_client.models.api_v1_demands_id_archive_post200_response import ApiV1DemandsIdArchivePost200Response
from imzala_client.models.api_v1_demands_id_cancel_post200_response import ApiV1DemandsIdCancelPost200Response
from imzala_client.models.api_v1_demands_id_cancel_post_request import ApiV1DemandsIdCancelPostRequest
from imzala_client.models.api_v1_demands_id_embed_session_post200_response import ApiV1DemandsIdEmbedSessionPost200Response
from imzala_client.models.api_v1_demands_id_embed_session_post_request import ApiV1DemandsIdEmbedSessionPostRequest
from imzala_client.models.api_v1_demands_id_get200_response import ApiV1DemandsIdGet200Response
from imzala_client.models.api_v1_demands_id_parties_party_id_resend_post200_response import ApiV1DemandsIdPartiesPartyIdResendPost200Response
from imzala_client.models.api_v1_demands_id_term_patch200_response import ApiV1DemandsIdTermPatch200Response
from imzala_client.models.api_v1_demands_id_timeline_get200_response import ApiV1DemandsIdTimelineGet200Response
from imzala_client.models.api_v1_demands_id_unarchive_post200_response import ApiV1DemandsIdUnarchivePost200Response
from imzala_client.models.api_v1_demands_post201_response import ApiV1DemandsPost201Response
from imzala_client.models.api_v1_demands_upload_post201_response import ApiV1DemandsUploadPost201Response
from imzala_client.models.api_v1_field_templates_id_preview_layout_post200_response import ApiV1FieldTemplatesIdPreviewLayoutPost200Response
from imzala_client.models.api_v1_templates_id_delete200_response import ApiV1TemplatesIdDelete200Response
from imzala_client.models.contract_term_input import ContractTermInput
from imzala_client.models.create_demand_request import CreateDemandRequest
from imzala_client.models.patch_stamp_item_request import PatchStampItemRequest
from imzala_client.models.patch_stamp_item_response import PatchStampItemResponse
from imzala_client.models.upsert_items_request import UpsertItemsRequest
from imzala_client.models.upsert_items_response import UpsertItemsResponse

from imzala_client.api_client import ApiClient, RequestSerialized
from imzala_client.api_response import ApiResponse
from imzala_client.rest import RESTResponseType


class DemandsApi:
    """NOTE: This class is auto generated by OpenAPI Generator
    Ref: https://openapi-generator.tech

    Do not edit the class manually.
    """

    def __init__(self, api_client=None) -> None:
        if api_client is None:
            api_client = ApiClient.get_default()
        self.api_client = api_client


    @validate_call
    def api_v1_demands_bulk_post(
        self,
        api_v1_demands_bulk_post_request: ApiV1DemandsBulkPostRequest,
        x_workspace_id: Annotated[Optional[UUID], Field(description="Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   `WORKSPACE_MISMATCH` döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (`Not a member of this organization`). ")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsBulkPost200Response:
        """Toplu sözleşme oluştur (tek şablondan N alıcı)

        Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \"bulk send\" modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa her satırda önce yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez.  **Davranış:** - `rows` en fazla 10 (aşarsa 400 `BULK_MAX_10`). Daha büyük listeler   istemci tarafında 10'arlı parçalara bölünür. - Kredi: sabit \"1 satır = 1 kredi\" değildir, her satırın maliyeti   `POST /demands` ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; `eidas_timestamp` seçilirse   satır başına +1 kredi eklenir). Şablon çok-belgeli bir zarf   tanımlıyorsa imzacı başına maliyet o imzacıya ATANMIŞ belge   sayısına göre hesaplanır ve belge sayısına göre azalan birim   fiyatlı bir indirim uygulanır (1 belge %0, 2 belge %30, 3 ve üzeri   %50, imzacı başına ayrı yukarı yuvarlanır; bkz. `POST /demands`   \"Kredi (çok belgeli zarf)\" bölümü). PAdES seviye eki yalnız QES'te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   `ocr_id` ve `liveness` +1 kredi, diğerleri 0'dır. `options`   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır `failed` (`error: \"INSUFFICIENT_CREDITS\"`)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine `failed` döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (`demand_id` response'ta bulunur, davet   gönderilmemiştir); mutabakat kredi-dışı bir hatayla düşerse satır   `failed` (`error: \"RECONCILE_FAILED\"`) döner, sözleşme yine taslağa   düşer. - Atamasız imzacı: çok belgeli şablonda eşlenen bir taraf hiçbir   belgeye atanmamışsa (ve `options.dispatch_notifications` `false`   DEĞİLSE) o satır `failed` (`error: \"PARTY_WITHOUT_DOCUMENTS\"`)   döner; kontrol satır oluşturulmadan ÖNCE (`demand_id` YOK) veya   oluşturulduktan hemen SONRA (sözleşme taslağa düşer, `demand_id` +   `party_ids` döner) çalışabilir. Diğer satırlar etkilenmez. - Belge seçimi satır başınadır: `rows[i].documents` (`POST /demands`   ile aynı şema). `options.documents` gönderilirse 400   `INVALID_DOCUMENT_SELECTION`. Satırın seçim hatası o satırı `failed`   yapar (`error`: `INVALID_DOCUMENT_SELECTION` + `details`, ya da   `PARTY_WITHOUT_DOCUMENTS` + `template_party_ids`); diğer satırlar   etkilenmez. - Belge başına değişkenler (`document_variables`) bu uçta   desteklenmez; yalnız `POST /demands` ile gönderilir. Satırda (kökte   ya da `party_mapping[i]` içinde) gönderilirse o satır `failed`   (`error: \"INVALID_DOCUMENT_VARIABLES\"`, `details.reason:   \"unsupported_endpoint\"`, `details.path`) döner ve oluşturulmaz;   `options.document_variables` gönderilirse 400   `INVALID_DOCUMENT_VARIABLES` döner, hiçbir satır oluşturulmaz. - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace'in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar `failed` olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - `X-Workspace-Id` header'ı ile organizasyon workspace'i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 

        :param api_v1_demands_bulk_post_request: (required)
        :type api_v1_demands_bulk_post_request: ApiV1DemandsBulkPostRequest
        :param x_workspace_id: Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   `WORKSPACE_MISMATCH` döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (`Not a member of this organization`). 
        :type x_workspace_id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_bulk_post_serialize(
            api_v1_demands_bulk_post_request=api_v1_demands_bulk_post_request,
            x_workspace_id=x_workspace_id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsBulkPost200Response",
            '400': "ApiError",
            '401': "ApiV1TemplatesGet401Response",
            '402': "ApiError",
            '403': "ApiError",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "ApiError",
            '429': "ApiError",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_bulk_post_with_http_info(
        self,
        api_v1_demands_bulk_post_request: ApiV1DemandsBulkPostRequest,
        x_workspace_id: Annotated[Optional[UUID], Field(description="Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   `WORKSPACE_MISMATCH` döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (`Not a member of this organization`). ")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsBulkPost200Response]:
        """Toplu sözleşme oluştur (tek şablondan N alıcı)

        Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \"bulk send\" modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa her satırda önce yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez.  **Davranış:** - `rows` en fazla 10 (aşarsa 400 `BULK_MAX_10`). Daha büyük listeler   istemci tarafında 10'arlı parçalara bölünür. - Kredi: sabit \"1 satır = 1 kredi\" değildir, her satırın maliyeti   `POST /demands` ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; `eidas_timestamp` seçilirse   satır başına +1 kredi eklenir). Şablon çok-belgeli bir zarf   tanımlıyorsa imzacı başına maliyet o imzacıya ATANMIŞ belge   sayısına göre hesaplanır ve belge sayısına göre azalan birim   fiyatlı bir indirim uygulanır (1 belge %0, 2 belge %30, 3 ve üzeri   %50, imzacı başına ayrı yukarı yuvarlanır; bkz. `POST /demands`   \"Kredi (çok belgeli zarf)\" bölümü). PAdES seviye eki yalnız QES'te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   `ocr_id` ve `liveness` +1 kredi, diğerleri 0'dır. `options`   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır `failed` (`error: \"INSUFFICIENT_CREDITS\"`)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine `failed` döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (`demand_id` response'ta bulunur, davet   gönderilmemiştir); mutabakat kredi-dışı bir hatayla düşerse satır   `failed` (`error: \"RECONCILE_FAILED\"`) döner, sözleşme yine taslağa   düşer. - Atamasız imzacı: çok belgeli şablonda eşlenen bir taraf hiçbir   belgeye atanmamışsa (ve `options.dispatch_notifications` `false`   DEĞİLSE) o satır `failed` (`error: \"PARTY_WITHOUT_DOCUMENTS\"`)   döner; kontrol satır oluşturulmadan ÖNCE (`demand_id` YOK) veya   oluşturulduktan hemen SONRA (sözleşme taslağa düşer, `demand_id` +   `party_ids` döner) çalışabilir. Diğer satırlar etkilenmez. - Belge seçimi satır başınadır: `rows[i].documents` (`POST /demands`   ile aynı şema). `options.documents` gönderilirse 400   `INVALID_DOCUMENT_SELECTION`. Satırın seçim hatası o satırı `failed`   yapar (`error`: `INVALID_DOCUMENT_SELECTION` + `details`, ya da   `PARTY_WITHOUT_DOCUMENTS` + `template_party_ids`); diğer satırlar   etkilenmez. - Belge başına değişkenler (`document_variables`) bu uçta   desteklenmez; yalnız `POST /demands` ile gönderilir. Satırda (kökte   ya da `party_mapping[i]` içinde) gönderilirse o satır `failed`   (`error: \"INVALID_DOCUMENT_VARIABLES\"`, `details.reason:   \"unsupported_endpoint\"`, `details.path`) döner ve oluşturulmaz;   `options.document_variables` gönderilirse 400   `INVALID_DOCUMENT_VARIABLES` döner, hiçbir satır oluşturulmaz. - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace'in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar `failed` olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - `X-Workspace-Id` header'ı ile organizasyon workspace'i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 

        :param api_v1_demands_bulk_post_request: (required)
        :type api_v1_demands_bulk_post_request: ApiV1DemandsBulkPostRequest
        :param x_workspace_id: Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   `WORKSPACE_MISMATCH` döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (`Not a member of this organization`). 
        :type x_workspace_id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_bulk_post_serialize(
            api_v1_demands_bulk_post_request=api_v1_demands_bulk_post_request,
            x_workspace_id=x_workspace_id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsBulkPost200Response",
            '400': "ApiError",
            '401': "ApiV1TemplatesGet401Response",
            '402': "ApiError",
            '403': "ApiError",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "ApiError",
            '429': "ApiError",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_bulk_post_without_preload_content(
        self,
        api_v1_demands_bulk_post_request: ApiV1DemandsBulkPostRequest,
        x_workspace_id: Annotated[Optional[UUID], Field(description="Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   `WORKSPACE_MISMATCH` döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (`Not a member of this organization`). ")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Toplu sözleşme oluştur (tek şablondan N alıcı)

        Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \"bulk send\" modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa her satırda önce yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez.  **Davranış:** - `rows` en fazla 10 (aşarsa 400 `BULK_MAX_10`). Daha büyük listeler   istemci tarafında 10'arlı parçalara bölünür. - Kredi: sabit \"1 satır = 1 kredi\" değildir, her satırın maliyeti   `POST /demands` ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; `eidas_timestamp` seçilirse   satır başına +1 kredi eklenir). Şablon çok-belgeli bir zarf   tanımlıyorsa imzacı başına maliyet o imzacıya ATANMIŞ belge   sayısına göre hesaplanır ve belge sayısına göre azalan birim   fiyatlı bir indirim uygulanır (1 belge %0, 2 belge %30, 3 ve üzeri   %50, imzacı başına ayrı yukarı yuvarlanır; bkz. `POST /demands`   \"Kredi (çok belgeli zarf)\" bölümü). PAdES seviye eki yalnız QES'te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   `ocr_id` ve `liveness` +1 kredi, diğerleri 0'dır. `options`   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır `failed` (`error: \"INSUFFICIENT_CREDITS\"`)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine `failed` döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (`demand_id` response'ta bulunur, davet   gönderilmemiştir); mutabakat kredi-dışı bir hatayla düşerse satır   `failed` (`error: \"RECONCILE_FAILED\"`) döner, sözleşme yine taslağa   düşer. - Atamasız imzacı: çok belgeli şablonda eşlenen bir taraf hiçbir   belgeye atanmamışsa (ve `options.dispatch_notifications` `false`   DEĞİLSE) o satır `failed` (`error: \"PARTY_WITHOUT_DOCUMENTS\"`)   döner; kontrol satır oluşturulmadan ÖNCE (`demand_id` YOK) veya   oluşturulduktan hemen SONRA (sözleşme taslağa düşer, `demand_id` +   `party_ids` döner) çalışabilir. Diğer satırlar etkilenmez. - Belge seçimi satır başınadır: `rows[i].documents` (`POST /demands`   ile aynı şema). `options.documents` gönderilirse 400   `INVALID_DOCUMENT_SELECTION`. Satırın seçim hatası o satırı `failed`   yapar (`error`: `INVALID_DOCUMENT_SELECTION` + `details`, ya da   `PARTY_WITHOUT_DOCUMENTS` + `template_party_ids`); diğer satırlar   etkilenmez. - Belge başına değişkenler (`document_variables`) bu uçta   desteklenmez; yalnız `POST /demands` ile gönderilir. Satırda (kökte   ya da `party_mapping[i]` içinde) gönderilirse o satır `failed`   (`error: \"INVALID_DOCUMENT_VARIABLES\"`, `details.reason:   \"unsupported_endpoint\"`, `details.path`) döner ve oluşturulmaz;   `options.document_variables` gönderilirse 400   `INVALID_DOCUMENT_VARIABLES` döner, hiçbir satır oluşturulmaz. - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace'in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar `failed` olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - `X-Workspace-Id` header'ı ile organizasyon workspace'i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 

        :param api_v1_demands_bulk_post_request: (required)
        :type api_v1_demands_bulk_post_request: ApiV1DemandsBulkPostRequest
        :param x_workspace_id: Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   `WORKSPACE_MISMATCH` döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (`Not a member of this organization`). 
        :type x_workspace_id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_bulk_post_serialize(
            api_v1_demands_bulk_post_request=api_v1_demands_bulk_post_request,
            x_workspace_id=x_workspace_id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsBulkPost200Response",
            '400': "ApiError",
            '401': "ApiV1TemplatesGet401Response",
            '402': "ApiError",
            '403': "ApiError",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "ApiError",
            '429': "ApiError",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_bulk_post_serialize(
        self,
        api_v1_demands_bulk_post_request,
        x_workspace_id,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        # process the query parameters
        # process the header parameters
        if x_workspace_id is not None:
            _header_params['X-Workspace-Id'] = x_workspace_id
        # process the form parameters
        # process the body parameter
        if api_v1_demands_bulk_post_request is not None:
            _body_params = api_v1_demands_bulk_post_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/demands/bulk',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_demand_id_dispatch_post(
        self,
        demand_id: UUID,
        api_v1_demands_demand_id_dispatch_post_request: Optional[ApiV1DemandsDemandIdDispatchPostRequest] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsDemandIdDispatchPost200Response:
        """Zarfı imzaya gönder (yayınla + davet)

        Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, `DRAFT` ise sözleşmeyi `PENDING`'e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  **Onaylayan:** Sözleşmede Onaylayan varsa bu uç önce yalnızca onay bekleyen onaylayanları davet eder; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir. Bir onaylayan reddettiyse bu uç kimseye davet göndermez; onay vermiş onaylayan yeniden davet edilmez.  🔴 **Bu uçta yukarıdaki `/documents*` ailesinin bayrak kapısı (`ENVELOPE_DECISION_ENFORCE`) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — `dispatch_notifications: false` ile oluşturulur, ardından `POST .../documents` / `POST .../documents/upload` ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; `send_invitations` gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir. **Bu uç çağrılana kadar** birden çok belgeli bir zarfın oluşturma yanıtındaki `signing_url`'leri ÇALIŞMAZ (imzacı için 410; `POST .../parties/{partyId}/resend` ve `POST .../reminders` için 409 `ENVELOPE_NOT_DISPATCHED` döner); belge uçları (`/documents*`) ise gönderim beklemeden kullanılabilir.  **Kredi:** tek tahsilat noktası burasıdır (`reconcileDemandSigningCost`). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (`dispatched: false` döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. `v1ResendRateLimiter`, `/parties/{partyId}/resend` ile AYNI dakika-bazlı freni paylaşır). 

        :param demand_id: (required)
        :type demand_id: UUID
        :param api_v1_demands_demand_id_dispatch_post_request:
        :type api_v1_demands_demand_id_dispatch_post_request: ApiV1DemandsDemandIdDispatchPostRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_dispatch_post_serialize(
            demand_id=demand_id,
            api_v1_demands_demand_id_dispatch_post_request=api_v1_demands_demand_id_dispatch_post_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDispatchPost200Response",
            '400': None,
            '402': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
            '429': None,
            '500': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_demand_id_dispatch_post_with_http_info(
        self,
        demand_id: UUID,
        api_v1_demands_demand_id_dispatch_post_request: Optional[ApiV1DemandsDemandIdDispatchPostRequest] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsDemandIdDispatchPost200Response]:
        """Zarfı imzaya gönder (yayınla + davet)

        Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, `DRAFT` ise sözleşmeyi `PENDING`'e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  **Onaylayan:** Sözleşmede Onaylayan varsa bu uç önce yalnızca onay bekleyen onaylayanları davet eder; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir. Bir onaylayan reddettiyse bu uç kimseye davet göndermez; onay vermiş onaylayan yeniden davet edilmez.  🔴 **Bu uçta yukarıdaki `/documents*` ailesinin bayrak kapısı (`ENVELOPE_DECISION_ENFORCE`) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — `dispatch_notifications: false` ile oluşturulur, ardından `POST .../documents` / `POST .../documents/upload` ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; `send_invitations` gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir. **Bu uç çağrılana kadar** birden çok belgeli bir zarfın oluşturma yanıtındaki `signing_url`'leri ÇALIŞMAZ (imzacı için 410; `POST .../parties/{partyId}/resend` ve `POST .../reminders` için 409 `ENVELOPE_NOT_DISPATCHED` döner); belge uçları (`/documents*`) ise gönderim beklemeden kullanılabilir.  **Kredi:** tek tahsilat noktası burasıdır (`reconcileDemandSigningCost`). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (`dispatched: false` döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. `v1ResendRateLimiter`, `/parties/{partyId}/resend` ile AYNI dakika-bazlı freni paylaşır). 

        :param demand_id: (required)
        :type demand_id: UUID
        :param api_v1_demands_demand_id_dispatch_post_request:
        :type api_v1_demands_demand_id_dispatch_post_request: ApiV1DemandsDemandIdDispatchPostRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_dispatch_post_serialize(
            demand_id=demand_id,
            api_v1_demands_demand_id_dispatch_post_request=api_v1_demands_demand_id_dispatch_post_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDispatchPost200Response",
            '400': None,
            '402': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
            '429': None,
            '500': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_demand_id_dispatch_post_without_preload_content(
        self,
        demand_id: UUID,
        api_v1_demands_demand_id_dispatch_post_request: Optional[ApiV1DemandsDemandIdDispatchPostRequest] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Zarfı imzaya gönder (yayınla + davet)

        Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, `DRAFT` ise sözleşmeyi `PENDING`'e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  **Onaylayan:** Sözleşmede Onaylayan varsa bu uç önce yalnızca onay bekleyen onaylayanları davet eder; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir. Bir onaylayan reddettiyse bu uç kimseye davet göndermez; onay vermiş onaylayan yeniden davet edilmez.  🔴 **Bu uçta yukarıdaki `/documents*` ailesinin bayrak kapısı (`ENVELOPE_DECISION_ENFORCE`) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — `dispatch_notifications: false` ile oluşturulur, ardından `POST .../documents` / `POST .../documents/upload` ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; `send_invitations` gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir. **Bu uç çağrılana kadar** birden çok belgeli bir zarfın oluşturma yanıtındaki `signing_url`'leri ÇALIŞMAZ (imzacı için 410; `POST .../parties/{partyId}/resend` ve `POST .../reminders` için 409 `ENVELOPE_NOT_DISPATCHED` döner); belge uçları (`/documents*`) ise gönderim beklemeden kullanılabilir.  **Kredi:** tek tahsilat noktası burasıdır (`reconcileDemandSigningCost`). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (`dispatched: false` döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. `v1ResendRateLimiter`, `/parties/{partyId}/resend` ile AYNI dakika-bazlı freni paylaşır). 

        :param demand_id: (required)
        :type demand_id: UUID
        :param api_v1_demands_demand_id_dispatch_post_request:
        :type api_v1_demands_demand_id_dispatch_post_request: ApiV1DemandsDemandIdDispatchPostRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_dispatch_post_serialize(
            demand_id=demand_id,
            api_v1_demands_demand_id_dispatch_post_request=api_v1_demands_demand_id_dispatch_post_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDispatchPost200Response",
            '400': None,
            '402': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
            '429': None,
            '500': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_demand_id_dispatch_post_serialize(
        self,
        demand_id,
        api_v1_demands_demand_id_dispatch_post_request,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if demand_id is not None:
            _path_params['demandId'] = demand_id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if api_v1_demands_demand_id_dispatch_post_request is not None:
            _body_params = api_v1_demands_demand_id_dispatch_post_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/demands/{demandId}/dispatch',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_demand_id_documents_doc_id_assignments_put(
        self,
        demand_id: UUID,
        doc_id: UUID,
        api_v1_demands_demand_id_documents_doc_id_assignments_put_request: ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsDemandIdDocumentsPost201Response:
        """Belgeye imzacı ata (tam-küme replace)

        Belgeye atanmış imzacı kümesini **tam olarak** `party_ids` ile değiştirir (eski atamalar silinir, yenileri yazılır). `party_ids` boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 `INVALID_PARTY_ID` — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 `ASSIGNMENT_HAS_DECISION`). 

        :param demand_id: (required)
        :type demand_id: UUID
        :param doc_id: (required)
        :type doc_id: UUID
        :param api_v1_demands_demand_id_documents_doc_id_assignments_put_request: (required)
        :type api_v1_demands_demand_id_documents_doc_id_assignments_put_request: ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_doc_id_assignments_put_serialize(
            demand_id=demand_id,
            doc_id=doc_id,
            api_v1_demands_demand_id_documents_doc_id_assignments_put_request=api_v1_demands_demand_id_documents_doc_id_assignments_put_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDocumentsPost201Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_demand_id_documents_doc_id_assignments_put_with_http_info(
        self,
        demand_id: UUID,
        doc_id: UUID,
        api_v1_demands_demand_id_documents_doc_id_assignments_put_request: ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsDemandIdDocumentsPost201Response]:
        """Belgeye imzacı ata (tam-küme replace)

        Belgeye atanmış imzacı kümesini **tam olarak** `party_ids` ile değiştirir (eski atamalar silinir, yenileri yazılır). `party_ids` boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 `INVALID_PARTY_ID` — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 `ASSIGNMENT_HAS_DECISION`). 

        :param demand_id: (required)
        :type demand_id: UUID
        :param doc_id: (required)
        :type doc_id: UUID
        :param api_v1_demands_demand_id_documents_doc_id_assignments_put_request: (required)
        :type api_v1_demands_demand_id_documents_doc_id_assignments_put_request: ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_doc_id_assignments_put_serialize(
            demand_id=demand_id,
            doc_id=doc_id,
            api_v1_demands_demand_id_documents_doc_id_assignments_put_request=api_v1_demands_demand_id_documents_doc_id_assignments_put_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDocumentsPost201Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_demand_id_documents_doc_id_assignments_put_without_preload_content(
        self,
        demand_id: UUID,
        doc_id: UUID,
        api_v1_demands_demand_id_documents_doc_id_assignments_put_request: ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Belgeye imzacı ata (tam-küme replace)

        Belgeye atanmış imzacı kümesini **tam olarak** `party_ids` ile değiştirir (eski atamalar silinir, yenileri yazılır). `party_ids` boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 `INVALID_PARTY_ID` — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 `ASSIGNMENT_HAS_DECISION`). 

        :param demand_id: (required)
        :type demand_id: UUID
        :param doc_id: (required)
        :type doc_id: UUID
        :param api_v1_demands_demand_id_documents_doc_id_assignments_put_request: (required)
        :type api_v1_demands_demand_id_documents_doc_id_assignments_put_request: ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_doc_id_assignments_put_serialize(
            demand_id=demand_id,
            doc_id=doc_id,
            api_v1_demands_demand_id_documents_doc_id_assignments_put_request=api_v1_demands_demand_id_documents_doc_id_assignments_put_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDocumentsPost201Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_demand_id_documents_doc_id_assignments_put_serialize(
        self,
        demand_id,
        doc_id,
        api_v1_demands_demand_id_documents_doc_id_assignments_put_request,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if demand_id is not None:
            _path_params['demandId'] = demand_id
        if doc_id is not None:
            _path_params['docId'] = doc_id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if api_v1_demands_demand_id_documents_doc_id_assignments_put_request is not None:
            _body_params = api_v1_demands_demand_id_documents_doc_id_assignments_put_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='PUT',
            resource_path='/api/v1/demands/{demandId}/documents/{docId}/assignments',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_demand_id_documents_doc_id_delete(
        self,
        demand_id: UUID,
        doc_id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1TemplatesIdDelete200Response:
        """Belgeyi zarftan sil

        Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N'e yeniden sıralanır. **Zarftaki son belge silinemez** (400 `CANNOT_DELETE_LAST_DOCUMENT`) — zarf hiç boş kalamaz. 

        :param demand_id: (required)
        :type demand_id: UUID
        :param doc_id: (required)
        :type doc_id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_doc_id_delete_serialize(
            demand_id=demand_id,
            doc_id=doc_id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1TemplatesIdDelete200Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_demand_id_documents_doc_id_delete_with_http_info(
        self,
        demand_id: UUID,
        doc_id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1TemplatesIdDelete200Response]:
        """Belgeyi zarftan sil

        Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N'e yeniden sıralanır. **Zarftaki son belge silinemez** (400 `CANNOT_DELETE_LAST_DOCUMENT`) — zarf hiç boş kalamaz. 

        :param demand_id: (required)
        :type demand_id: UUID
        :param doc_id: (required)
        :type doc_id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_doc_id_delete_serialize(
            demand_id=demand_id,
            doc_id=doc_id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1TemplatesIdDelete200Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_demand_id_documents_doc_id_delete_without_preload_content(
        self,
        demand_id: UUID,
        doc_id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Belgeyi zarftan sil

        Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N'e yeniden sıralanır. **Zarftaki son belge silinemez** (400 `CANNOT_DELETE_LAST_DOCUMENT`) — zarf hiç boş kalamaz. 

        :param demand_id: (required)
        :type demand_id: UUID
        :param doc_id: (required)
        :type doc_id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_doc_id_delete_serialize(
            demand_id=demand_id,
            doc_id=doc_id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1TemplatesIdDelete200Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_demand_id_documents_doc_id_delete_serialize(
        self,
        demand_id,
        doc_id,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if demand_id is not None:
            _path_params['demandId'] = demand_id
        if doc_id is not None:
            _path_params['docId'] = doc_id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='DELETE',
            resource_path='/api/v1/demands/{demandId}/documents/{docId}',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_demand_id_documents_doc_id_patch(
        self,
        demand_id: UUID,
        doc_id: UUID,
        api_v1_demands_demand_id_documents_doc_id_patch_request: ApiV1DemandsDemandIdDocumentsDocIdPatchRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsDemandIdDocumentsPost201Response:
        """Belge metadata güncelle

        Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (`PUT .../assignments`). 

        :param demand_id: (required)
        :type demand_id: UUID
        :param doc_id: (required)
        :type doc_id: UUID
        :param api_v1_demands_demand_id_documents_doc_id_patch_request: (required)
        :type api_v1_demands_demand_id_documents_doc_id_patch_request: ApiV1DemandsDemandIdDocumentsDocIdPatchRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_doc_id_patch_serialize(
            demand_id=demand_id,
            doc_id=doc_id,
            api_v1_demands_demand_id_documents_doc_id_patch_request=api_v1_demands_demand_id_documents_doc_id_patch_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDocumentsPost201Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_demand_id_documents_doc_id_patch_with_http_info(
        self,
        demand_id: UUID,
        doc_id: UUID,
        api_v1_demands_demand_id_documents_doc_id_patch_request: ApiV1DemandsDemandIdDocumentsDocIdPatchRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsDemandIdDocumentsPost201Response]:
        """Belge metadata güncelle

        Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (`PUT .../assignments`). 

        :param demand_id: (required)
        :type demand_id: UUID
        :param doc_id: (required)
        :type doc_id: UUID
        :param api_v1_demands_demand_id_documents_doc_id_patch_request: (required)
        :type api_v1_demands_demand_id_documents_doc_id_patch_request: ApiV1DemandsDemandIdDocumentsDocIdPatchRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_doc_id_patch_serialize(
            demand_id=demand_id,
            doc_id=doc_id,
            api_v1_demands_demand_id_documents_doc_id_patch_request=api_v1_demands_demand_id_documents_doc_id_patch_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDocumentsPost201Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_demand_id_documents_doc_id_patch_without_preload_content(
        self,
        demand_id: UUID,
        doc_id: UUID,
        api_v1_demands_demand_id_documents_doc_id_patch_request: ApiV1DemandsDemandIdDocumentsDocIdPatchRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Belge metadata güncelle

        Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (`PUT .../assignments`). 

        :param demand_id: (required)
        :type demand_id: UUID
        :param doc_id: (required)
        :type doc_id: UUID
        :param api_v1_demands_demand_id_documents_doc_id_patch_request: (required)
        :type api_v1_demands_demand_id_documents_doc_id_patch_request: ApiV1DemandsDemandIdDocumentsDocIdPatchRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_doc_id_patch_serialize(
            demand_id=demand_id,
            doc_id=doc_id,
            api_v1_demands_demand_id_documents_doc_id_patch_request=api_v1_demands_demand_id_documents_doc_id_patch_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDocumentsPost201Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_demand_id_documents_doc_id_patch_serialize(
        self,
        demand_id,
        doc_id,
        api_v1_demands_demand_id_documents_doc_id_patch_request,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if demand_id is not None:
            _path_params['demandId'] = demand_id
        if doc_id is not None:
            _path_params['docId'] = doc_id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if api_v1_demands_demand_id_documents_doc_id_patch_request is not None:
            _body_params = api_v1_demands_demand_id_documents_doc_id_patch_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='PATCH',
            resource_path='/api/v1/demands/{demandId}/documents/{docId}',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_demand_id_documents_get(
        self,
        demand_id: UUID,
        view: Annotated[Optional[StrictStr], Field(description="`wizard` → tam DTO (`assigned_party_ids` + `decision_count` dahil).")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsDemandIdDocumentsGet200Response:
        """Zarf belge listesi

        Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm `/documents*` + `/documents/{docId}*` + `/documents/{docId}/assignments` uçları `ENVELOPE_DECISION_ENFORCE` bayrağı **arkasındadır**; bayrak kapalıyken **409 `ENVELOPE_MULTI_DOC_DISABLED`** döner. Bu, dashboard'un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca `POST /demands/{demandId}/dispatch` çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. `?view=wizard` verilirse tam şekil döner (`EnvelopeDocument` şemasındaki alan notlarına bakın). 

        :param demand_id: (required)
        :type demand_id: UUID
        :param view: `wizard` → tam DTO (`assigned_party_ids` + `decision_count` dahil).
        :type view: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_get_serialize(
            demand_id=demand_id,
            view=view,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDocumentsGet200Response",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_demand_id_documents_get_with_http_info(
        self,
        demand_id: UUID,
        view: Annotated[Optional[StrictStr], Field(description="`wizard` → tam DTO (`assigned_party_ids` + `decision_count` dahil).")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsDemandIdDocumentsGet200Response]:
        """Zarf belge listesi

        Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm `/documents*` + `/documents/{docId}*` + `/documents/{docId}/assignments` uçları `ENVELOPE_DECISION_ENFORCE` bayrağı **arkasındadır**; bayrak kapalıyken **409 `ENVELOPE_MULTI_DOC_DISABLED`** döner. Bu, dashboard'un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca `POST /demands/{demandId}/dispatch` çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. `?view=wizard` verilirse tam şekil döner (`EnvelopeDocument` şemasındaki alan notlarına bakın). 

        :param demand_id: (required)
        :type demand_id: UUID
        :param view: `wizard` → tam DTO (`assigned_party_ids` + `decision_count` dahil).
        :type view: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_get_serialize(
            demand_id=demand_id,
            view=view,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDocumentsGet200Response",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_demand_id_documents_get_without_preload_content(
        self,
        demand_id: UUID,
        view: Annotated[Optional[StrictStr], Field(description="`wizard` → tam DTO (`assigned_party_ids` + `decision_count` dahil).")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Zarf belge listesi

        Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm `/documents*` + `/documents/{docId}*` + `/documents/{docId}/assignments` uçları `ENVELOPE_DECISION_ENFORCE` bayrağı **arkasındadır**; bayrak kapalıyken **409 `ENVELOPE_MULTI_DOC_DISABLED`** döner. Bu, dashboard'un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca `POST /demands/{demandId}/dispatch` çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. `?view=wizard` verilirse tam şekil döner (`EnvelopeDocument` şemasındaki alan notlarına bakın). 

        :param demand_id: (required)
        :type demand_id: UUID
        :param view: `wizard` → tam DTO (`assigned_party_ids` + `decision_count` dahil).
        :type view: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_get_serialize(
            demand_id=demand_id,
            view=view,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDocumentsGet200Response",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_demand_id_documents_get_serialize(
        self,
        demand_id,
        view,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if demand_id is not None:
            _path_params['demandId'] = demand_id
        # process the query parameters
        if view is not None:
            
            _query_params.append(('view', view))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/api/v1/demands/{demandId}/documents',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_demand_id_documents_order_put(
        self,
        demand_id: UUID,
        api_v1_demands_demand_id_documents_order_put_request: ApiV1DemandsDemandIdDocumentsOrderPutRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsDemandIdDocumentsGet200Response:
        """Zarftaki belgelerin sırasını değiştir

        Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. `document_ids` zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 `ORDER_SET_MISMATCH`** döner. 

        :param demand_id: (required)
        :type demand_id: UUID
        :param api_v1_demands_demand_id_documents_order_put_request: (required)
        :type api_v1_demands_demand_id_documents_order_put_request: ApiV1DemandsDemandIdDocumentsOrderPutRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_order_put_serialize(
            demand_id=demand_id,
            api_v1_demands_demand_id_documents_order_put_request=api_v1_demands_demand_id_documents_order_put_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDocumentsGet200Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_demand_id_documents_order_put_with_http_info(
        self,
        demand_id: UUID,
        api_v1_demands_demand_id_documents_order_put_request: ApiV1DemandsDemandIdDocumentsOrderPutRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsDemandIdDocumentsGet200Response]:
        """Zarftaki belgelerin sırasını değiştir

        Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. `document_ids` zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 `ORDER_SET_MISMATCH`** döner. 

        :param demand_id: (required)
        :type demand_id: UUID
        :param api_v1_demands_demand_id_documents_order_put_request: (required)
        :type api_v1_demands_demand_id_documents_order_put_request: ApiV1DemandsDemandIdDocumentsOrderPutRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_order_put_serialize(
            demand_id=demand_id,
            api_v1_demands_demand_id_documents_order_put_request=api_v1_demands_demand_id_documents_order_put_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDocumentsGet200Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_demand_id_documents_order_put_without_preload_content(
        self,
        demand_id: UUID,
        api_v1_demands_demand_id_documents_order_put_request: ApiV1DemandsDemandIdDocumentsOrderPutRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Zarftaki belgelerin sırasını değiştir

        Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. `document_ids` zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 `ORDER_SET_MISMATCH`** döner. 

        :param demand_id: (required)
        :type demand_id: UUID
        :param api_v1_demands_demand_id_documents_order_put_request: (required)
        :type api_v1_demands_demand_id_documents_order_put_request: ApiV1DemandsDemandIdDocumentsOrderPutRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_order_put_serialize(
            demand_id=demand_id,
            api_v1_demands_demand_id_documents_order_put_request=api_v1_demands_demand_id_documents_order_put_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDocumentsGet200Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_demand_id_documents_order_put_serialize(
        self,
        demand_id,
        api_v1_demands_demand_id_documents_order_put_request,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if demand_id is not None:
            _path_params['demandId'] = demand_id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if api_v1_demands_demand_id_documents_order_put_request is not None:
            _body_params = api_v1_demands_demand_id_documents_order_put_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='PUT',
            resource_path='/api/v1/demands/{demandId}/documents/order',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_demand_id_documents_post(
        self,
        demand_id: UUID,
        api_v1_demands_demand_id_documents_post_request: ApiV1DemandsDemandIdDocumentsPostRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsDemandIdDocumentsPost201Response:
        """Zarfa metadata-only belge ekle

        Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: `POST /demands/{demandId}/documents/upload`.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`'te yapılır. 

        :param demand_id: (required)
        :type demand_id: UUID
        :param api_v1_demands_demand_id_documents_post_request: (required)
        :type api_v1_demands_demand_id_documents_post_request: ApiV1DemandsDemandIdDocumentsPostRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_post_serialize(
            demand_id=demand_id,
            api_v1_demands_demand_id_documents_post_request=api_v1_demands_demand_id_documents_post_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "ApiV1DemandsDemandIdDocumentsPost201Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_demand_id_documents_post_with_http_info(
        self,
        demand_id: UUID,
        api_v1_demands_demand_id_documents_post_request: ApiV1DemandsDemandIdDocumentsPostRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsDemandIdDocumentsPost201Response]:
        """Zarfa metadata-only belge ekle

        Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: `POST /demands/{demandId}/documents/upload`.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`'te yapılır. 

        :param demand_id: (required)
        :type demand_id: UUID
        :param api_v1_demands_demand_id_documents_post_request: (required)
        :type api_v1_demands_demand_id_documents_post_request: ApiV1DemandsDemandIdDocumentsPostRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_post_serialize(
            demand_id=demand_id,
            api_v1_demands_demand_id_documents_post_request=api_v1_demands_demand_id_documents_post_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "ApiV1DemandsDemandIdDocumentsPost201Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_demand_id_documents_post_without_preload_content(
        self,
        demand_id: UUID,
        api_v1_demands_demand_id_documents_post_request: ApiV1DemandsDemandIdDocumentsPostRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Zarfa metadata-only belge ekle

        Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: `POST /demands/{demandId}/documents/upload`.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`'te yapılır. 

        :param demand_id: (required)
        :type demand_id: UUID
        :param api_v1_demands_demand_id_documents_post_request: (required)
        :type api_v1_demands_demand_id_documents_post_request: ApiV1DemandsDemandIdDocumentsPostRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_post_serialize(
            demand_id=demand_id,
            api_v1_demands_demand_id_documents_post_request=api_v1_demands_demand_id_documents_post_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "ApiV1DemandsDemandIdDocumentsPost201Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_demand_id_documents_post_serialize(
        self,
        demand_id,
        api_v1_demands_demand_id_documents_post_request,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if demand_id is not None:
            _path_params['demandId'] = demand_id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if api_v1_demands_demand_id_documents_post_request is not None:
            _body_params = api_v1_demands_demand_id_documents_post_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/demands/{demandId}/documents',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_demand_id_documents_upload_post(
        self,
        demand_id: UUID,
        file: Annotated[Union[StrictBytes, StrictStr, Tuple[StrictStr, StrictBytes]], Field(description="PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.")],
        idempotency_key: Annotated[StrictStr, Field(description="Zorunlu tekrar-koruma anahtarı.")],
        title: StrictStr,
        doc_kind: Optional[StrictStr] = None,
        is_required: Annotated[Optional[StrictStr], Field(description="Multipart alanı — string olarak gönderilir.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsDemandIdDocumentsPost201Response:
        """Zarfa dosya yükle (belge başına tek dosya)

        Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  `idempotency_key` **zorunludur** (gövde alanı; `Idempotency-Key` HEADER'ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı `(demandId, idempotency_key)` çifti ile tekrar çağrı **409 `IDEMPOTENT_REPLAY`** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO'su döner.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`'te yapılır. 

        :param demand_id: (required)
        :type demand_id: UUID
        :param file: PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya. (required)
        :type file: bytes
        :param idempotency_key: Zorunlu tekrar-koruma anahtarı. (required)
        :type idempotency_key: str
        :param title: (required)
        :type title: str
        :param doc_kind:
        :type doc_kind: str
        :param is_required: Multipart alanı — string olarak gönderilir.
        :type is_required: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_upload_post_serialize(
            demand_id=demand_id,
            file=file,
            idempotency_key=idempotency_key,
            title=title,
            doc_kind=doc_kind,
            is_required=is_required,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDocumentsPost201Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
            '413': None,
            '415': None,
            '422': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_demand_id_documents_upload_post_with_http_info(
        self,
        demand_id: UUID,
        file: Annotated[Union[StrictBytes, StrictStr, Tuple[StrictStr, StrictBytes]], Field(description="PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.")],
        idempotency_key: Annotated[StrictStr, Field(description="Zorunlu tekrar-koruma anahtarı.")],
        title: StrictStr,
        doc_kind: Optional[StrictStr] = None,
        is_required: Annotated[Optional[StrictStr], Field(description="Multipart alanı — string olarak gönderilir.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsDemandIdDocumentsPost201Response]:
        """Zarfa dosya yükle (belge başına tek dosya)

        Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  `idempotency_key` **zorunludur** (gövde alanı; `Idempotency-Key` HEADER'ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı `(demandId, idempotency_key)` çifti ile tekrar çağrı **409 `IDEMPOTENT_REPLAY`** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO'su döner.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`'te yapılır. 

        :param demand_id: (required)
        :type demand_id: UUID
        :param file: PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya. (required)
        :type file: bytes
        :param idempotency_key: Zorunlu tekrar-koruma anahtarı. (required)
        :type idempotency_key: str
        :param title: (required)
        :type title: str
        :param doc_kind:
        :type doc_kind: str
        :param is_required: Multipart alanı — string olarak gönderilir.
        :type is_required: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_upload_post_serialize(
            demand_id=demand_id,
            file=file,
            idempotency_key=idempotency_key,
            title=title,
            doc_kind=doc_kind,
            is_required=is_required,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDocumentsPost201Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
            '413': None,
            '415': None,
            '422': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_demand_id_documents_upload_post_without_preload_content(
        self,
        demand_id: UUID,
        file: Annotated[Union[StrictBytes, StrictStr, Tuple[StrictStr, StrictBytes]], Field(description="PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.")],
        idempotency_key: Annotated[StrictStr, Field(description="Zorunlu tekrar-koruma anahtarı.")],
        title: StrictStr,
        doc_kind: Optional[StrictStr] = None,
        is_required: Annotated[Optional[StrictStr], Field(description="Multipart alanı — string olarak gönderilir.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Zarfa dosya yükle (belge başına tek dosya)

        Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  `idempotency_key` **zorunludur** (gövde alanı; `Idempotency-Key` HEADER'ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı `(demandId, idempotency_key)` çifti ile tekrar çağrı **409 `IDEMPOTENT_REPLAY`** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO'su döner.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`'te yapılır. 

        :param demand_id: (required)
        :type demand_id: UUID
        :param file: PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya. (required)
        :type file: bytes
        :param idempotency_key: Zorunlu tekrar-koruma anahtarı. (required)
        :type idempotency_key: str
        :param title: (required)
        :type title: str
        :param doc_kind:
        :type doc_kind: str
        :param is_required: Multipart alanı — string olarak gönderilir.
        :type is_required: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_demand_id_documents_upload_post_serialize(
            demand_id=demand_id,
            file=file,
            idempotency_key=idempotency_key,
            title=title,
            doc_kind=doc_kind,
            is_required=is_required,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsDemandIdDocumentsPost201Response",
            '400': None,
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
            '413': None,
            '415': None,
            '422': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_demand_id_documents_upload_post_serialize(
        self,
        demand_id,
        file,
        idempotency_key,
        title,
        doc_kind,
        is_required,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if demand_id is not None:
            _path_params['demandId'] = demand_id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        if file is not None:
            _files['file'] = file
        if idempotency_key is not None:
            _form_params.append(('idempotency_key', idempotency_key))
        if title is not None:
            _form_params.append(('title', title))
        if doc_kind is not None:
            _form_params.append(('doc_kind', doc_kind))
        if is_required is not None:
            _form_params.append(('is_required', is_required))
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'multipart/form-data'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/demands/{demandId}/documents/upload',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_get(
        self,
        status: Optional[StrictStr] = None,
        q: Annotated[Optional[StrictStr], Field(description="Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez.")] = None,
        var_from: Optional[date] = None,
        to: Optional[date] = None,
        template_id: Optional[UUID] = None,
        page: Optional[StrictInt] = None,
        limit: Annotated[Optional[StrictInt], Field(description="Sayfa boyutu (page_size ile aynı)")] = None,
        sort: Annotated[Optional[StrictStr], Field(description="alan:yön (ör. createdAt:desc)")] = None,
        archived: Annotated[Optional[StrictStr], Field(description="Arşiv durumu filtresi. `exclude`: yalnız arşivsiz sözleşmeler. `only`: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan `include`: tüm sözleşmeler (arşivli ve arşivsiz). ")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsGet200Response:
        """Sözleşme listesi (counts-only, PII'siz)

        Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (`parties_total`, `parties_signed`). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için `GET /demands/{id}`. 

        :param status:
        :type status: str
        :param q: Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez.
        :type q: str
        :param var_from:
        :type var_from: date
        :param to:
        :type to: date
        :param template_id:
        :type template_id: UUID
        :param page:
        :type page: int
        :param limit: Sayfa boyutu (page_size ile aynı)
        :type limit: int
        :param sort: alan:yön (ör. createdAt:desc)
        :type sort: str
        :param archived: Arşiv durumu filtresi. `exclude`: yalnız arşivsiz sözleşmeler. `only`: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan `include`: tüm sözleşmeler (arşivli ve arşivsiz). 
        :type archived: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_get_serialize(
            status=status,
            q=q,
            var_from=var_from,
            to=to,
            template_id=template_id,
            page=page,
            limit=limit,
            sort=sort,
            archived=archived,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsGet200Response",
            '401': "ApiV1TemplatesGet401Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_get_with_http_info(
        self,
        status: Optional[StrictStr] = None,
        q: Annotated[Optional[StrictStr], Field(description="Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez.")] = None,
        var_from: Optional[date] = None,
        to: Optional[date] = None,
        template_id: Optional[UUID] = None,
        page: Optional[StrictInt] = None,
        limit: Annotated[Optional[StrictInt], Field(description="Sayfa boyutu (page_size ile aynı)")] = None,
        sort: Annotated[Optional[StrictStr], Field(description="alan:yön (ör. createdAt:desc)")] = None,
        archived: Annotated[Optional[StrictStr], Field(description="Arşiv durumu filtresi. `exclude`: yalnız arşivsiz sözleşmeler. `only`: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan `include`: tüm sözleşmeler (arşivli ve arşivsiz). ")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsGet200Response]:
        """Sözleşme listesi (counts-only, PII'siz)

        Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (`parties_total`, `parties_signed`). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için `GET /demands/{id}`. 

        :param status:
        :type status: str
        :param q: Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez.
        :type q: str
        :param var_from:
        :type var_from: date
        :param to:
        :type to: date
        :param template_id:
        :type template_id: UUID
        :param page:
        :type page: int
        :param limit: Sayfa boyutu (page_size ile aynı)
        :type limit: int
        :param sort: alan:yön (ör. createdAt:desc)
        :type sort: str
        :param archived: Arşiv durumu filtresi. `exclude`: yalnız arşivsiz sözleşmeler. `only`: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan `include`: tüm sözleşmeler (arşivli ve arşivsiz). 
        :type archived: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_get_serialize(
            status=status,
            q=q,
            var_from=var_from,
            to=to,
            template_id=template_id,
            page=page,
            limit=limit,
            sort=sort,
            archived=archived,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsGet200Response",
            '401': "ApiV1TemplatesGet401Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_get_without_preload_content(
        self,
        status: Optional[StrictStr] = None,
        q: Annotated[Optional[StrictStr], Field(description="Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez.")] = None,
        var_from: Optional[date] = None,
        to: Optional[date] = None,
        template_id: Optional[UUID] = None,
        page: Optional[StrictInt] = None,
        limit: Annotated[Optional[StrictInt], Field(description="Sayfa boyutu (page_size ile aynı)")] = None,
        sort: Annotated[Optional[StrictStr], Field(description="alan:yön (ör. createdAt:desc)")] = None,
        archived: Annotated[Optional[StrictStr], Field(description="Arşiv durumu filtresi. `exclude`: yalnız arşivsiz sözleşmeler. `only`: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan `include`: tüm sözleşmeler (arşivli ve arşivsiz). ")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Sözleşme listesi (counts-only, PII'siz)

        Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (`parties_total`, `parties_signed`). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için `GET /demands/{id}`. 

        :param status:
        :type status: str
        :param q: Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez.
        :type q: str
        :param var_from:
        :type var_from: date
        :param to:
        :type to: date
        :param template_id:
        :type template_id: UUID
        :param page:
        :type page: int
        :param limit: Sayfa boyutu (page_size ile aynı)
        :type limit: int
        :param sort: alan:yön (ör. createdAt:desc)
        :type sort: str
        :param archived: Arşiv durumu filtresi. `exclude`: yalnız arşivsiz sözleşmeler. `only`: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan `include`: tüm sözleşmeler (arşivli ve arşivsiz). 
        :type archived: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_get_serialize(
            status=status,
            q=q,
            var_from=var_from,
            to=to,
            template_id=template_id,
            page=page,
            limit=limit,
            sort=sort,
            archived=archived,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsGet200Response",
            '401': "ApiV1TemplatesGet401Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_get_serialize(
        self,
        status,
        q,
        var_from,
        to,
        template_id,
        page,
        limit,
        sort,
        archived,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        # process the query parameters
        if status is not None:
            
            _query_params.append(('status', status))
            
        if q is not None:
            
            _query_params.append(('q', q))
            
        if var_from is not None:
            if isinstance(var_from, date):
                _query_params.append(
                    (
                        'from',
                        var_from.strftime(
                            self.api_client.configuration.date_format
                        )
                    )
                )
            else:
                _query_params.append(('from', var_from))
            
        if to is not None:
            if isinstance(to, date):
                _query_params.append(
                    (
                        'to',
                        to.strftime(
                            self.api_client.configuration.date_format
                        )
                    )
                )
            else:
                _query_params.append(('to', to))
            
        if template_id is not None:
            
            _query_params.append(('template_id', template_id))
            
        if page is not None:
            
            _query_params.append(('page', page))
            
        if limit is not None:
            
            _query_params.append(('limit', limit))
            
        if sort is not None:
            
            _query_params.append(('sort', sort))
            
        if archived is not None:
            
            _query_params.append(('archived', archived))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/api/v1/demands',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_id_archive_post(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsIdArchivePost200Response:
        """Sözleşmeyi arşivle

        Arşiv sözleşmenin `status`'unu DEĞİŞTİRMEZ; yalnız `archived_at` damgası ekler ve arşivlenen sözleşmeyi salt-okunur yapar (`PATCH .../term` gibi mutasyonlar 409 `DEMAND_ARCHIVED` ile reddedilir; `GET` uçları etkilenmez). Yalnız tamamlanmış, iptal edilmiş veya süresi dolmuş sözleşmeler arşivlenebilir. Zaten arşivliyse idempotent (200, mevcut `archived_at` ile). 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_archive_post_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdArchivePost200Response",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "CodedError",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_id_archive_post_with_http_info(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsIdArchivePost200Response]:
        """Sözleşmeyi arşivle

        Arşiv sözleşmenin `status`'unu DEĞİŞTİRMEZ; yalnız `archived_at` damgası ekler ve arşivlenen sözleşmeyi salt-okunur yapar (`PATCH .../term` gibi mutasyonlar 409 `DEMAND_ARCHIVED` ile reddedilir; `GET` uçları etkilenmez). Yalnız tamamlanmış, iptal edilmiş veya süresi dolmuş sözleşmeler arşivlenebilir. Zaten arşivliyse idempotent (200, mevcut `archived_at` ile). 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_archive_post_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdArchivePost200Response",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "CodedError",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_id_archive_post_without_preload_content(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Sözleşmeyi arşivle

        Arşiv sözleşmenin `status`'unu DEĞİŞTİRMEZ; yalnız `archived_at` damgası ekler ve arşivlenen sözleşmeyi salt-okunur yapar (`PATCH .../term` gibi mutasyonlar 409 `DEMAND_ARCHIVED` ile reddedilir; `GET` uçları etkilenmez). Yalnız tamamlanmış, iptal edilmiş veya süresi dolmuş sözleşmeler arşivlenebilir. Zaten arşivliyse idempotent (200, mevcut `archived_at` ile). 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_archive_post_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdArchivePost200Response",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "CodedError",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_id_archive_post_serialize(
        self,
        id,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/demands/{id}/archive',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_id_belge_document_id_pdf_get(
        self,
        id: UUID,
        document_id: Annotated[UUID, Field(description="Zarftaki belgenin kimliği.")],
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> bytes:
        """Belge-özgü imzalı PDF (çok-belgeli zarf)

        Çok-belgeli zarfta TEK bir belgenin imzalı PDF'ini indirir. Zarf-geneli `/demands/{id}/pdf` ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND'lenir (başka zarfın belgesi istenirse 404). 

        :param id: (required)
        :type id: UUID
        :param document_id: Zarftaki belgenin kimliği. (required)
        :type document_id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_belge_document_id_pdf_get_serialize(
            id=id,
            document_id=document_id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "bytes",
            '404': "ApiV1TemplatesIdGet404Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_id_belge_document_id_pdf_get_with_http_info(
        self,
        id: UUID,
        document_id: Annotated[UUID, Field(description="Zarftaki belgenin kimliği.")],
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[bytes]:
        """Belge-özgü imzalı PDF (çok-belgeli zarf)

        Çok-belgeli zarfta TEK bir belgenin imzalı PDF'ini indirir. Zarf-geneli `/demands/{id}/pdf` ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND'lenir (başka zarfın belgesi istenirse 404). 

        :param id: (required)
        :type id: UUID
        :param document_id: Zarftaki belgenin kimliği. (required)
        :type document_id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_belge_document_id_pdf_get_serialize(
            id=id,
            document_id=document_id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "bytes",
            '404': "ApiV1TemplatesIdGet404Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_id_belge_document_id_pdf_get_without_preload_content(
        self,
        id: UUID,
        document_id: Annotated[UUID, Field(description="Zarftaki belgenin kimliği.")],
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Belge-özgü imzalı PDF (çok-belgeli zarf)

        Çok-belgeli zarfta TEK bir belgenin imzalı PDF'ini indirir. Zarf-geneli `/demands/{id}/pdf` ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND'lenir (başka zarfın belgesi istenirse 404). 

        :param id: (required)
        :type id: UUID
        :param document_id: Zarftaki belgenin kimliği. (required)
        :type document_id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_belge_document_id_pdf_get_serialize(
            id=id,
            document_id=document_id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "bytes",
            '404': "ApiV1TemplatesIdGet404Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_id_belge_document_id_pdf_get_serialize(
        self,
        id,
        document_id,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        if document_id is not None:
            _path_params['document_id'] = document_id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/pdf', 
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/api/v1/demands/{id}/belge/{document_id}/pdf',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_id_cancel_post(
        self,
        id: UUID,
        api_v1_demands_id_cancel_post_request: Optional[ApiV1DemandsIdCancelPostRequest] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsIdCancelPost200Response:
        """Sözleşme iptal (void)

        Bekleyen bir sözleşmeyi iptal eder (status=CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 

        :param id: (required)
        :type id: UUID
        :param api_v1_demands_id_cancel_post_request:
        :type api_v1_demands_id_cancel_post_request: ApiV1DemandsIdCancelPostRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_cancel_post_serialize(
            id=id,
            api_v1_demands_id_cancel_post_request=api_v1_demands_id_cancel_post_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdCancelPost200Response",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_id_cancel_post_with_http_info(
        self,
        id: UUID,
        api_v1_demands_id_cancel_post_request: Optional[ApiV1DemandsIdCancelPostRequest] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsIdCancelPost200Response]:
        """Sözleşme iptal (void)

        Bekleyen bir sözleşmeyi iptal eder (status=CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 

        :param id: (required)
        :type id: UUID
        :param api_v1_demands_id_cancel_post_request:
        :type api_v1_demands_id_cancel_post_request: ApiV1DemandsIdCancelPostRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_cancel_post_serialize(
            id=id,
            api_v1_demands_id_cancel_post_request=api_v1_demands_id_cancel_post_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdCancelPost200Response",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_id_cancel_post_without_preload_content(
        self,
        id: UUID,
        api_v1_demands_id_cancel_post_request: Optional[ApiV1DemandsIdCancelPostRequest] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Sözleşme iptal (void)

        Bekleyen bir sözleşmeyi iptal eder (status=CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 

        :param id: (required)
        :type id: UUID
        :param api_v1_demands_id_cancel_post_request:
        :type api_v1_demands_id_cancel_post_request: ApiV1DemandsIdCancelPostRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_cancel_post_serialize(
            id=id,
            api_v1_demands_id_cancel_post_request=api_v1_demands_id_cancel_post_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdCancelPost200Response",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_id_cancel_post_serialize(
        self,
        id,
        api_v1_demands_id_cancel_post_request,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if api_v1_demands_id_cancel_post_request is not None:
            _body_params = api_v1_demands_id_cancel_post_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/demands/{id}/cancel',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_id_certificate_get(
        self,
        id: UUID,
        lang: Annotated[Optional[StrictStr], Field(description="tr | en")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> bytes:
        """Tamamlanma sertifikası (PAdES B-T)

        Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 

        :param id: (required)
        :type id: UUID
        :param lang: tr | en
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_certificate_get_serialize(
            id=id,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "bytes",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_id_certificate_get_with_http_info(
        self,
        id: UUID,
        lang: Annotated[Optional[StrictStr], Field(description="tr | en")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[bytes]:
        """Tamamlanma sertifikası (PAdES B-T)

        Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 

        :param id: (required)
        :type id: UUID
        :param lang: tr | en
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_certificate_get_serialize(
            id=id,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "bytes",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_id_certificate_get_without_preload_content(
        self,
        id: UUID,
        lang: Annotated[Optional[StrictStr], Field(description="tr | en")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Tamamlanma sertifikası (PAdES B-T)

        Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 

        :param id: (required)
        :type id: UUID
        :param lang: tr | en
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_certificate_get_serialize(
            id=id,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "bytes",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_id_certificate_get_serialize(
        self,
        id,
        lang,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        if lang is not None:
            
            _query_params.append(('lang', lang))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/pdf', 
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/api/v1/demands/{id}/certificate',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_id_delete(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1TemplatesIdDelete200Response:
        """Sözleşme sil (yalnızca tamamlanmamış)

        Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API'den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 `DEMAND_COMPLETED`. Arşivlenmiş sözleşme de silinemez → 409 `DEMAND_ARCHIVED`; önce `POST /api/v1/demands/{id}/unarchive` ile arşivden çıkarın. 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_delete_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1TemplatesIdDelete200Response",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "ApiV1DemandsIdDelete409Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_id_delete_with_http_info(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1TemplatesIdDelete200Response]:
        """Sözleşme sil (yalnızca tamamlanmamış)

        Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API'den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 `DEMAND_COMPLETED`. Arşivlenmiş sözleşme de silinemez → 409 `DEMAND_ARCHIVED`; önce `POST /api/v1/demands/{id}/unarchive` ile arşivden çıkarın. 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_delete_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1TemplatesIdDelete200Response",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "ApiV1DemandsIdDelete409Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_id_delete_without_preload_content(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Sözleşme sil (yalnızca tamamlanmamış)

        Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API'den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 `DEMAND_COMPLETED`. Arşivlenmiş sözleşme de silinemez → 409 `DEMAND_ARCHIVED`; önce `POST /api/v1/demands/{id}/unarchive` ile arşivden çıkarın. 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_delete_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1TemplatesIdDelete200Response",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "ApiV1DemandsIdDelete409Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_id_delete_serialize(
        self,
        id,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='DELETE',
            resource_path='/api/v1/demands/{id}',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_id_embed_session_post(
        self,
        id: Annotated[UUID, Field(description="Sözleşme (demand) ID")],
        api_v1_demands_id_embed_session_post_request: ApiV1DemandsIdEmbedSessionPostRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsIdEmbedSessionPost200Response:
        """Gömülü imza oturumu başlat (embed token mint)

        Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token'ı üretir. Dönen `embed_url` bir `<iframe>` içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla atılan dijital imza, 5070 sayılı Elektronik İmza Kanunu m.3 anlamında elektronik imzadır; güvenli elektronik imza değildir ve AB hukuku bakımından gelişmiş veya nitelikli imza olarak sunulmaz. Telefon doğrulaması, T.C. kimlik numarası kontrolü veya biyometrik adımlar gibi ek doğrulamalar imzanın delil değerini güçlendirir; imzayı gelişmiş ya da güvenli elektronik imzaya dönüştürmez. Güvenli elektronik imza gerekiyorsa bu akış yerine nitelikli elektronik sertifika ile imzalama akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: `expires_at` alanında belirtilen sürede geçersiz olur. - `embed_allowed_origins` kısıtı: API anahtarına tanımlanmış   izin verilen origin'ler dışından `<iframe>` açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** `X-Workspace-Id` header'ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace'in sözleşmesi için 404 döner (IDOR koruması). Kurum çalışma alanında sözleşme düzenleme ile aynı rol kuralı geçerlidir: OWNER ve ADMIN kurumun tüm sözleşmeleri için, MEMBER yalnız kendi oluşturduğu sözleşmeler için oturum alır. Erişim yoksa 404 döner. 

        :param id: Sözleşme (demand) ID (required)
        :type id: UUID
        :param api_v1_demands_id_embed_session_post_request: (required)
        :type api_v1_demands_id_embed_session_post_request: ApiV1DemandsIdEmbedSessionPostRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_embed_session_post_serialize(
            id=id,
            api_v1_demands_id_embed_session_post_request=api_v1_demands_id_embed_session_post_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdEmbedSessionPost200Response",
            '400': "ApiV1TemplatesIdGet404Response",
            '401': "ApiV1TemplatesGet401Response",
            '403': "ApiError",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "ApiV1TemplatesIdGet404Response",
            '429': "ApiError",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_id_embed_session_post_with_http_info(
        self,
        id: Annotated[UUID, Field(description="Sözleşme (demand) ID")],
        api_v1_demands_id_embed_session_post_request: ApiV1DemandsIdEmbedSessionPostRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsIdEmbedSessionPost200Response]:
        """Gömülü imza oturumu başlat (embed token mint)

        Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token'ı üretir. Dönen `embed_url` bir `<iframe>` içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla atılan dijital imza, 5070 sayılı Elektronik İmza Kanunu m.3 anlamında elektronik imzadır; güvenli elektronik imza değildir ve AB hukuku bakımından gelişmiş veya nitelikli imza olarak sunulmaz. Telefon doğrulaması, T.C. kimlik numarası kontrolü veya biyometrik adımlar gibi ek doğrulamalar imzanın delil değerini güçlendirir; imzayı gelişmiş ya da güvenli elektronik imzaya dönüştürmez. Güvenli elektronik imza gerekiyorsa bu akış yerine nitelikli elektronik sertifika ile imzalama akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: `expires_at` alanında belirtilen sürede geçersiz olur. - `embed_allowed_origins` kısıtı: API anahtarına tanımlanmış   izin verilen origin'ler dışından `<iframe>` açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** `X-Workspace-Id` header'ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace'in sözleşmesi için 404 döner (IDOR koruması). Kurum çalışma alanında sözleşme düzenleme ile aynı rol kuralı geçerlidir: OWNER ve ADMIN kurumun tüm sözleşmeleri için, MEMBER yalnız kendi oluşturduğu sözleşmeler için oturum alır. Erişim yoksa 404 döner. 

        :param id: Sözleşme (demand) ID (required)
        :type id: UUID
        :param api_v1_demands_id_embed_session_post_request: (required)
        :type api_v1_demands_id_embed_session_post_request: ApiV1DemandsIdEmbedSessionPostRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_embed_session_post_serialize(
            id=id,
            api_v1_demands_id_embed_session_post_request=api_v1_demands_id_embed_session_post_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdEmbedSessionPost200Response",
            '400': "ApiV1TemplatesIdGet404Response",
            '401': "ApiV1TemplatesGet401Response",
            '403': "ApiError",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "ApiV1TemplatesIdGet404Response",
            '429': "ApiError",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_id_embed_session_post_without_preload_content(
        self,
        id: Annotated[UUID, Field(description="Sözleşme (demand) ID")],
        api_v1_demands_id_embed_session_post_request: ApiV1DemandsIdEmbedSessionPostRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Gömülü imza oturumu başlat (embed token mint)

        Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token'ı üretir. Dönen `embed_url` bir `<iframe>` içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla atılan dijital imza, 5070 sayılı Elektronik İmza Kanunu m.3 anlamında elektronik imzadır; güvenli elektronik imza değildir ve AB hukuku bakımından gelişmiş veya nitelikli imza olarak sunulmaz. Telefon doğrulaması, T.C. kimlik numarası kontrolü veya biyometrik adımlar gibi ek doğrulamalar imzanın delil değerini güçlendirir; imzayı gelişmiş ya da güvenli elektronik imzaya dönüştürmez. Güvenli elektronik imza gerekiyorsa bu akış yerine nitelikli elektronik sertifika ile imzalama akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: `expires_at` alanında belirtilen sürede geçersiz olur. - `embed_allowed_origins` kısıtı: API anahtarına tanımlanmış   izin verilen origin'ler dışından `<iframe>` açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** `X-Workspace-Id` header'ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace'in sözleşmesi için 404 döner (IDOR koruması). Kurum çalışma alanında sözleşme düzenleme ile aynı rol kuralı geçerlidir: OWNER ve ADMIN kurumun tüm sözleşmeleri için, MEMBER yalnız kendi oluşturduğu sözleşmeler için oturum alır. Erişim yoksa 404 döner. 

        :param id: Sözleşme (demand) ID (required)
        :type id: UUID
        :param api_v1_demands_id_embed_session_post_request: (required)
        :type api_v1_demands_id_embed_session_post_request: ApiV1DemandsIdEmbedSessionPostRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_embed_session_post_serialize(
            id=id,
            api_v1_demands_id_embed_session_post_request=api_v1_demands_id_embed_session_post_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdEmbedSessionPost200Response",
            '400': "ApiV1TemplatesIdGet404Response",
            '401': "ApiV1TemplatesGet401Response",
            '403': "ApiError",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "ApiV1TemplatesIdGet404Response",
            '429': "ApiError",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_id_embed_session_post_serialize(
        self,
        id,
        api_v1_demands_id_embed_session_post_request,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if api_v1_demands_id_embed_session_post_request is not None:
            _body_params = api_v1_demands_id_embed_session_post_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/demands/{id}/embed-session',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_id_get(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsIdGet200Response:
        """Sözleşme durumu + imza ilerlemesi

        Kurum çalışma alanında erişim, sözleşme listesi ile aynı rol kuralını izler: OWNER ve ADMIN kurumun tüm sözleşmelerini, MEMBER yalnız kendi oluşturduğu sözleşmeleri görür. Erişim yoksa 404 döner. 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_get_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdGet200Response",
            '404': "ApiV1TemplatesIdGet404Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_id_get_with_http_info(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsIdGet200Response]:
        """Sözleşme durumu + imza ilerlemesi

        Kurum çalışma alanında erişim, sözleşme listesi ile aynı rol kuralını izler: OWNER ve ADMIN kurumun tüm sözleşmelerini, MEMBER yalnız kendi oluşturduğu sözleşmeleri görür. Erişim yoksa 404 döner. 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_get_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdGet200Response",
            '404': "ApiV1TemplatesIdGet404Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_id_get_without_preload_content(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Sözleşme durumu + imza ilerlemesi

        Kurum çalışma alanında erişim, sözleşme listesi ile aynı rol kuralını izler: OWNER ve ADMIN kurumun tüm sözleşmelerini, MEMBER yalnız kendi oluşturduğu sözleşmeleri görür. Erişim yoksa 404 döner. 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_get_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdGet200Response",
            '404': "ApiV1TemplatesIdGet404Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_id_get_serialize(
        self,
        id,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/api/v1/demands/{id}',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_id_items_item_id_stamp_patch(
        self,
        id: UUID,
        item_id: Annotated[int, Field(strict=True, ge=1, description="Kaşe alanının kimliği (`stamp_items[].item_id`)")],
        patch_stamp_item_request: PatchStampItemRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> PatchStampItemResponse:
        """Kaşe alanını doldur (kısmi güncelleme)

        Sözleşmedeki var olan TEK bir kaşe alanını yapılandırılmış veriyle doldurur. `POST /api/v1/demands/{id}/items` tüm alanları yeniden yazar; bu uç ise yalnız belirtilen kaşeye dokunur, diğer alanlar değişmez.  Kaşe alanının kimliği (`itemId`) `GET /api/v1/demands/{id}` yanıtındaki `stamp_items[].item_id` alanından alınır.  ### Kısmi güncelleme kuralları  - `stamp_data` içinde gönderilen alan yazılır (baştaki/sondaki boşluk kırpılır). - `null` veya boş string gönderilen alan kaldırılır. - Gönderilmeyen alan olduğu gibi korunur. - Bilinmeyen alan veya string olmayan değer `400 INVALID_STAMP_DATA` döner.  ### Kaşe kaynağına göre davranış  - `FILLER_PROVIDES`: veri imzalayanın kaşesine önceden doldurulur;   zorunlu kaşe bu veriyle karşılanmış sayılır. İmzalayan imza   sayfasında bu değeri görür ve imzalamadan önce düzenleyebilir;   düzenlerse onun verisi kaydedilir. Kayıtta verinin gönderen   tarafından mı imzalayan tarafından mı girildiği ayrıca tutulur. - `INLINE`: kaşe verisi güncellenir. - `FROM_SAVED`: veri artık kayıtlı kaşeyle aynı olmadığı için kaşe   `INLINE` olur. Görsel kaşe (logo) varsa korunur ve belgede logo gösterilir.  ### Durum kontrolü  Yalnız `DRAFT` ve `PENDING` sözleşmeler güncellenebilir. Taraflardan biri imzaladıysa veya bir belge için karar verdiyse kaşe değiştirilemez.  ### Erişim  Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerinde, MEMBER yalnız kendi oluşturduğu sözleşmelerde kaşe doldurabilir. Erişim yoksa `404 DEMAND_NOT_FOUND` döner.  ### Örnek  ```bash curl -X PATCH https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items/$STAMP_ITEM_ID/stamp \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{     \"stamp_data\": {       \"companyName\": \"Örnek Ltd.\",       \"taxNumber\": \"1234567890\",       \"taxOffice\": \"Kadıköy\",       \"companyPhone\": \"+905551112233\"     }   }' ``` 

        :param id: (required)
        :type id: UUID
        :param item_id: Kaşe alanının kimliği (`stamp_items[].item_id`) (required)
        :type item_id: int
        :param patch_stamp_item_request: (required)
        :type patch_stamp_item_request: PatchStampItemRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_items_item_id_stamp_patch_serialize(
            id=id,
            item_id=item_id,
            patch_stamp_item_request=patch_stamp_item_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PatchStampItemResponse",
            '400': None,
            '401': "ApiV1TemplatesGet401Response",
            '403': None,
            '404': None,
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_id_items_item_id_stamp_patch_with_http_info(
        self,
        id: UUID,
        item_id: Annotated[int, Field(strict=True, ge=1, description="Kaşe alanının kimliği (`stamp_items[].item_id`)")],
        patch_stamp_item_request: PatchStampItemRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[PatchStampItemResponse]:
        """Kaşe alanını doldur (kısmi güncelleme)

        Sözleşmedeki var olan TEK bir kaşe alanını yapılandırılmış veriyle doldurur. `POST /api/v1/demands/{id}/items` tüm alanları yeniden yazar; bu uç ise yalnız belirtilen kaşeye dokunur, diğer alanlar değişmez.  Kaşe alanının kimliği (`itemId`) `GET /api/v1/demands/{id}` yanıtındaki `stamp_items[].item_id` alanından alınır.  ### Kısmi güncelleme kuralları  - `stamp_data` içinde gönderilen alan yazılır (baştaki/sondaki boşluk kırpılır). - `null` veya boş string gönderilen alan kaldırılır. - Gönderilmeyen alan olduğu gibi korunur. - Bilinmeyen alan veya string olmayan değer `400 INVALID_STAMP_DATA` döner.  ### Kaşe kaynağına göre davranış  - `FILLER_PROVIDES`: veri imzalayanın kaşesine önceden doldurulur;   zorunlu kaşe bu veriyle karşılanmış sayılır. İmzalayan imza   sayfasında bu değeri görür ve imzalamadan önce düzenleyebilir;   düzenlerse onun verisi kaydedilir. Kayıtta verinin gönderen   tarafından mı imzalayan tarafından mı girildiği ayrıca tutulur. - `INLINE`: kaşe verisi güncellenir. - `FROM_SAVED`: veri artık kayıtlı kaşeyle aynı olmadığı için kaşe   `INLINE` olur. Görsel kaşe (logo) varsa korunur ve belgede logo gösterilir.  ### Durum kontrolü  Yalnız `DRAFT` ve `PENDING` sözleşmeler güncellenebilir. Taraflardan biri imzaladıysa veya bir belge için karar verdiyse kaşe değiştirilemez.  ### Erişim  Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerinde, MEMBER yalnız kendi oluşturduğu sözleşmelerde kaşe doldurabilir. Erişim yoksa `404 DEMAND_NOT_FOUND` döner.  ### Örnek  ```bash curl -X PATCH https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items/$STAMP_ITEM_ID/stamp \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{     \"stamp_data\": {       \"companyName\": \"Örnek Ltd.\",       \"taxNumber\": \"1234567890\",       \"taxOffice\": \"Kadıköy\",       \"companyPhone\": \"+905551112233\"     }   }' ``` 

        :param id: (required)
        :type id: UUID
        :param item_id: Kaşe alanının kimliği (`stamp_items[].item_id`) (required)
        :type item_id: int
        :param patch_stamp_item_request: (required)
        :type patch_stamp_item_request: PatchStampItemRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_items_item_id_stamp_patch_serialize(
            id=id,
            item_id=item_id,
            patch_stamp_item_request=patch_stamp_item_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PatchStampItemResponse",
            '400': None,
            '401': "ApiV1TemplatesGet401Response",
            '403': None,
            '404': None,
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_id_items_item_id_stamp_patch_without_preload_content(
        self,
        id: UUID,
        item_id: Annotated[int, Field(strict=True, ge=1, description="Kaşe alanının kimliği (`stamp_items[].item_id`)")],
        patch_stamp_item_request: PatchStampItemRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Kaşe alanını doldur (kısmi güncelleme)

        Sözleşmedeki var olan TEK bir kaşe alanını yapılandırılmış veriyle doldurur. `POST /api/v1/demands/{id}/items` tüm alanları yeniden yazar; bu uç ise yalnız belirtilen kaşeye dokunur, diğer alanlar değişmez.  Kaşe alanının kimliği (`itemId`) `GET /api/v1/demands/{id}` yanıtındaki `stamp_items[].item_id` alanından alınır.  ### Kısmi güncelleme kuralları  - `stamp_data` içinde gönderilen alan yazılır (baştaki/sondaki boşluk kırpılır). - `null` veya boş string gönderilen alan kaldırılır. - Gönderilmeyen alan olduğu gibi korunur. - Bilinmeyen alan veya string olmayan değer `400 INVALID_STAMP_DATA` döner.  ### Kaşe kaynağına göre davranış  - `FILLER_PROVIDES`: veri imzalayanın kaşesine önceden doldurulur;   zorunlu kaşe bu veriyle karşılanmış sayılır. İmzalayan imza   sayfasında bu değeri görür ve imzalamadan önce düzenleyebilir;   düzenlerse onun verisi kaydedilir. Kayıtta verinin gönderen   tarafından mı imzalayan tarafından mı girildiği ayrıca tutulur. - `INLINE`: kaşe verisi güncellenir. - `FROM_SAVED`: veri artık kayıtlı kaşeyle aynı olmadığı için kaşe   `INLINE` olur. Görsel kaşe (logo) varsa korunur ve belgede logo gösterilir.  ### Durum kontrolü  Yalnız `DRAFT` ve `PENDING` sözleşmeler güncellenebilir. Taraflardan biri imzaladıysa veya bir belge için karar verdiyse kaşe değiştirilemez.  ### Erişim  Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerinde, MEMBER yalnız kendi oluşturduğu sözleşmelerde kaşe doldurabilir. Erişim yoksa `404 DEMAND_NOT_FOUND` döner.  ### Örnek  ```bash curl -X PATCH https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items/$STAMP_ITEM_ID/stamp \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{     \"stamp_data\": {       \"companyName\": \"Örnek Ltd.\",       \"taxNumber\": \"1234567890\",       \"taxOffice\": \"Kadıköy\",       \"companyPhone\": \"+905551112233\"     }   }' ``` 

        :param id: (required)
        :type id: UUID
        :param item_id: Kaşe alanının kimliği (`stamp_items[].item_id`) (required)
        :type item_id: int
        :param patch_stamp_item_request: (required)
        :type patch_stamp_item_request: PatchStampItemRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_items_item_id_stamp_patch_serialize(
            id=id,
            item_id=item_id,
            patch_stamp_item_request=patch_stamp_item_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PatchStampItemResponse",
            '400': None,
            '401': "ApiV1TemplatesGet401Response",
            '403': None,
            '404': None,
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_id_items_item_id_stamp_patch_serialize(
        self,
        id,
        item_id,
        patch_stamp_item_request,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        if item_id is not None:
            _path_params['itemId'] = item_id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if patch_stamp_item_request is not None:
            _body_params = patch_stamp_item_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='PATCH',
            resource_path='/api/v1/demands/{id}/items/{itemId}/stamp',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_id_items_post(
        self,
        id: UUID,
        upsert_items_request: UpsertItemsRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> UpsertItemsResponse:
        """Sözleşmeye alan yerleştir (replace)

        Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: `POST /api/v1/demands/upload` ile demand yarat (`dispatch_notifications=false` ile auto-dispatch'i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da `POST /api/v1/demands/{id}/reminders` ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - `page_ids` **omitted** → demand'in TÜM mevcut item'ları silinir,   body'dekiler yaratılır (full replace). - `page_ids: [N, M, ...]` → sadece bu sayfaların item'ları silinir,   diğer sayfalardaki item'lar korunur. Body'deki `items[].page_id`   değerleri `page_ids` listesinde olmalıdır.  ### Item type'ları  | `item_type` | `party_id` zorunlu? | `config` örneği | |-------------|---------------------|-----------------| | `signature` | ✅ | (yok) veya `{ requireFirstSignatureOtp: true }` | | `text` | ❌ | `{ default_content }` | | `dynamic_text` | ✅ | `{ defaultSource: \"{{signer.full_name}}\" }` | | `cells` | ✅ | `{ cellCount: 11, defaultSource: \"{{signer.government_id}}\" }` | | `date` | ✅ | `{ defaultSource, defaultValue }` | | `dropdown` | ✅ | `{ options: [{label,value}], defaultValue }` | | `checkbox` | ✅ | `{ checkedByDefault: false }` | | `radio` | ✅ | `{ options: [{label,value}], defaultValue }` | | `stamp` | Yalnız `source: FILLER_PROVIDES` ise (zorunlu kaşede şart) | `{ source: \"INLINE\", stampData: { companyName: \"Örnek Ltd.\", taxNumber: \"1234567890\" } }` |  ### Kaşe (`stamp`) alanı  - `config.source`: `INLINE` (varsayılan, kaşe verisi gönderenden gelir),   `FROM_SAVED` (kayıtlı kaşeden kopya) veya `FILLER_PROVIDES` (kaşeyi   `party_id` ile belirtilen imzalayan doldurur). - `config.stampData`: yapılandırılmış kaşe verisi. İzinli alanlar:   `companyName`, `personalName`, `address`, `companyAddress`,   `personalAddress`, `taxNumber`, `taxOffice`, `idNumber`, `phone`,   `companyPhone`, `personalPhone`, `email` (hepsi string). Bilinmeyen alan   veya string olmayan değer `400 INVALID_STAMP_DATA` döner. Eski   dokümandaki data URL string biçimi geriye dönük uyum için hâlâ kabul   edilir, ancak belgede kaşe olarak çizilmez; yapılandırılmış nesneyi kullanın. - `config.isRequired: true` yalnız `FILLER_PROVIDES` kaşede anlamlıdır:   imzalayan kaşeyi doldurmadan imzalayamaz. Zorunlu kaşenin `party_id`'si   olmak zorundadır; çok belgeli zarfta taraf, kaşenin bulunduğu belgeye   atanmış olmalıdır. - Var olan tek bir kaşeyi diğer alanlara dokunmadan doldurmak için bu   uç yerine `PATCH /api/v1/demands/{id}/items/{itemId}/stamp` kullanın.  ### Sistem değişkenleri (dynamic_text/cells/date `config.defaultSource`)  `{{signer.first_name}}`, `{{signer.last_name}}`, `{{signer.full_name}}`, `{{signer.email}}`, `{{signer.phone}}`, `{{signer.government_id}}`, `{{signer.birth_date}}`, `{{signer.sign_date}}`, `{{contract.title}}`, `{{sender.full_name}}`, `{{current.date}}`, `{{current.datetime}}`.  ### Workspace izolasyonu  X-API-Key middleware demand'i workspace'e göre filtreler; başka workspace'in demand'ine item ekleyemezsiniz (404 döner). Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerine, MEMBER yalnız kendi oluşturduğu sözleşmelere alan yerleştirebilir. Erişim yoksa 404 döner.  ### Status kontrolü  Sadece `PENDING` demand edit edilebilir. `COMPLETED`, `EXPIRED`, `REJECTED` için 403.  ### Örnek  ```bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{     \"items\": [       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"signature\",         \"position_x\": 0.5, \"position_y\": 0.85,         \"width\": 0.2, \"height\": 0.05,         \"is_required\": true       },       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"cells\",         \"position_x\": 0.1, \"position_y\": 0.5,         \"width\": 0.4, \"height\": 0.04,         \"slug\": \"tc\",         \"config\": { \"cellCount\": 11, \"defaultSource\": \"{{signer.government_id}}\" }       }     ]   }' ``` 

        :param id: (required)
        :type id: UUID
        :param upsert_items_request: (required)
        :type upsert_items_request: UpsertItemsRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_items_post_serialize(
            id=id,
            upsert_items_request=upsert_items_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "UpsertItemsResponse",
            '400': None,
            '401': "ApiV1TemplatesGet401Response",
            '403': None,
            '404': None,
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_id_items_post_with_http_info(
        self,
        id: UUID,
        upsert_items_request: UpsertItemsRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[UpsertItemsResponse]:
        """Sözleşmeye alan yerleştir (replace)

        Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: `POST /api/v1/demands/upload` ile demand yarat (`dispatch_notifications=false` ile auto-dispatch'i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da `POST /api/v1/demands/{id}/reminders` ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - `page_ids` **omitted** → demand'in TÜM mevcut item'ları silinir,   body'dekiler yaratılır (full replace). - `page_ids: [N, M, ...]` → sadece bu sayfaların item'ları silinir,   diğer sayfalardaki item'lar korunur. Body'deki `items[].page_id`   değerleri `page_ids` listesinde olmalıdır.  ### Item type'ları  | `item_type` | `party_id` zorunlu? | `config` örneği | |-------------|---------------------|-----------------| | `signature` | ✅ | (yok) veya `{ requireFirstSignatureOtp: true }` | | `text` | ❌ | `{ default_content }` | | `dynamic_text` | ✅ | `{ defaultSource: \"{{signer.full_name}}\" }` | | `cells` | ✅ | `{ cellCount: 11, defaultSource: \"{{signer.government_id}}\" }` | | `date` | ✅ | `{ defaultSource, defaultValue }` | | `dropdown` | ✅ | `{ options: [{label,value}], defaultValue }` | | `checkbox` | ✅ | `{ checkedByDefault: false }` | | `radio` | ✅ | `{ options: [{label,value}], defaultValue }` | | `stamp` | Yalnız `source: FILLER_PROVIDES` ise (zorunlu kaşede şart) | `{ source: \"INLINE\", stampData: { companyName: \"Örnek Ltd.\", taxNumber: \"1234567890\" } }` |  ### Kaşe (`stamp`) alanı  - `config.source`: `INLINE` (varsayılan, kaşe verisi gönderenden gelir),   `FROM_SAVED` (kayıtlı kaşeden kopya) veya `FILLER_PROVIDES` (kaşeyi   `party_id` ile belirtilen imzalayan doldurur). - `config.stampData`: yapılandırılmış kaşe verisi. İzinli alanlar:   `companyName`, `personalName`, `address`, `companyAddress`,   `personalAddress`, `taxNumber`, `taxOffice`, `idNumber`, `phone`,   `companyPhone`, `personalPhone`, `email` (hepsi string). Bilinmeyen alan   veya string olmayan değer `400 INVALID_STAMP_DATA` döner. Eski   dokümandaki data URL string biçimi geriye dönük uyum için hâlâ kabul   edilir, ancak belgede kaşe olarak çizilmez; yapılandırılmış nesneyi kullanın. - `config.isRequired: true` yalnız `FILLER_PROVIDES` kaşede anlamlıdır:   imzalayan kaşeyi doldurmadan imzalayamaz. Zorunlu kaşenin `party_id`'si   olmak zorundadır; çok belgeli zarfta taraf, kaşenin bulunduğu belgeye   atanmış olmalıdır. - Var olan tek bir kaşeyi diğer alanlara dokunmadan doldurmak için bu   uç yerine `PATCH /api/v1/demands/{id}/items/{itemId}/stamp` kullanın.  ### Sistem değişkenleri (dynamic_text/cells/date `config.defaultSource`)  `{{signer.first_name}}`, `{{signer.last_name}}`, `{{signer.full_name}}`, `{{signer.email}}`, `{{signer.phone}}`, `{{signer.government_id}}`, `{{signer.birth_date}}`, `{{signer.sign_date}}`, `{{contract.title}}`, `{{sender.full_name}}`, `{{current.date}}`, `{{current.datetime}}`.  ### Workspace izolasyonu  X-API-Key middleware demand'i workspace'e göre filtreler; başka workspace'in demand'ine item ekleyemezsiniz (404 döner). Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerine, MEMBER yalnız kendi oluşturduğu sözleşmelere alan yerleştirebilir. Erişim yoksa 404 döner.  ### Status kontrolü  Sadece `PENDING` demand edit edilebilir. `COMPLETED`, `EXPIRED`, `REJECTED` için 403.  ### Örnek  ```bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{     \"items\": [       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"signature\",         \"position_x\": 0.5, \"position_y\": 0.85,         \"width\": 0.2, \"height\": 0.05,         \"is_required\": true       },       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"cells\",         \"position_x\": 0.1, \"position_y\": 0.5,         \"width\": 0.4, \"height\": 0.04,         \"slug\": \"tc\",         \"config\": { \"cellCount\": 11, \"defaultSource\": \"{{signer.government_id}}\" }       }     ]   }' ``` 

        :param id: (required)
        :type id: UUID
        :param upsert_items_request: (required)
        :type upsert_items_request: UpsertItemsRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_items_post_serialize(
            id=id,
            upsert_items_request=upsert_items_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "UpsertItemsResponse",
            '400': None,
            '401': "ApiV1TemplatesGet401Response",
            '403': None,
            '404': None,
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_id_items_post_without_preload_content(
        self,
        id: UUID,
        upsert_items_request: UpsertItemsRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Sözleşmeye alan yerleştir (replace)

        Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: `POST /api/v1/demands/upload` ile demand yarat (`dispatch_notifications=false` ile auto-dispatch'i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da `POST /api/v1/demands/{id}/reminders` ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - `page_ids` **omitted** → demand'in TÜM mevcut item'ları silinir,   body'dekiler yaratılır (full replace). - `page_ids: [N, M, ...]` → sadece bu sayfaların item'ları silinir,   diğer sayfalardaki item'lar korunur. Body'deki `items[].page_id`   değerleri `page_ids` listesinde olmalıdır.  ### Item type'ları  | `item_type` | `party_id` zorunlu? | `config` örneği | |-------------|---------------------|-----------------| | `signature` | ✅ | (yok) veya `{ requireFirstSignatureOtp: true }` | | `text` | ❌ | `{ default_content }` | | `dynamic_text` | ✅ | `{ defaultSource: \"{{signer.full_name}}\" }` | | `cells` | ✅ | `{ cellCount: 11, defaultSource: \"{{signer.government_id}}\" }` | | `date` | ✅ | `{ defaultSource, defaultValue }` | | `dropdown` | ✅ | `{ options: [{label,value}], defaultValue }` | | `checkbox` | ✅ | `{ checkedByDefault: false }` | | `radio` | ✅ | `{ options: [{label,value}], defaultValue }` | | `stamp` | Yalnız `source: FILLER_PROVIDES` ise (zorunlu kaşede şart) | `{ source: \"INLINE\", stampData: { companyName: \"Örnek Ltd.\", taxNumber: \"1234567890\" } }` |  ### Kaşe (`stamp`) alanı  - `config.source`: `INLINE` (varsayılan, kaşe verisi gönderenden gelir),   `FROM_SAVED` (kayıtlı kaşeden kopya) veya `FILLER_PROVIDES` (kaşeyi   `party_id` ile belirtilen imzalayan doldurur). - `config.stampData`: yapılandırılmış kaşe verisi. İzinli alanlar:   `companyName`, `personalName`, `address`, `companyAddress`,   `personalAddress`, `taxNumber`, `taxOffice`, `idNumber`, `phone`,   `companyPhone`, `personalPhone`, `email` (hepsi string). Bilinmeyen alan   veya string olmayan değer `400 INVALID_STAMP_DATA` döner. Eski   dokümandaki data URL string biçimi geriye dönük uyum için hâlâ kabul   edilir, ancak belgede kaşe olarak çizilmez; yapılandırılmış nesneyi kullanın. - `config.isRequired: true` yalnız `FILLER_PROVIDES` kaşede anlamlıdır:   imzalayan kaşeyi doldurmadan imzalayamaz. Zorunlu kaşenin `party_id`'si   olmak zorundadır; çok belgeli zarfta taraf, kaşenin bulunduğu belgeye   atanmış olmalıdır. - Var olan tek bir kaşeyi diğer alanlara dokunmadan doldurmak için bu   uç yerine `PATCH /api/v1/demands/{id}/items/{itemId}/stamp` kullanın.  ### Sistem değişkenleri (dynamic_text/cells/date `config.defaultSource`)  `{{signer.first_name}}`, `{{signer.last_name}}`, `{{signer.full_name}}`, `{{signer.email}}`, `{{signer.phone}}`, `{{signer.government_id}}`, `{{signer.birth_date}}`, `{{signer.sign_date}}`, `{{contract.title}}`, `{{sender.full_name}}`, `{{current.date}}`, `{{current.datetime}}`.  ### Workspace izolasyonu  X-API-Key middleware demand'i workspace'e göre filtreler; başka workspace'in demand'ine item ekleyemezsiniz (404 döner). Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerine, MEMBER yalnız kendi oluşturduğu sözleşmelere alan yerleştirebilir. Erişim yoksa 404 döner.  ### Status kontrolü  Sadece `PENDING` demand edit edilebilir. `COMPLETED`, `EXPIRED`, `REJECTED` için 403.  ### Örnek  ```bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{     \"items\": [       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"signature\",         \"position_x\": 0.5, \"position_y\": 0.85,         \"width\": 0.2, \"height\": 0.05,         \"is_required\": true       },       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"cells\",         \"position_x\": 0.1, \"position_y\": 0.5,         \"width\": 0.4, \"height\": 0.04,         \"slug\": \"tc\",         \"config\": { \"cellCount\": 11, \"defaultSource\": \"{{signer.government_id}}\" }       }     ]   }' ``` 

        :param id: (required)
        :type id: UUID
        :param upsert_items_request: (required)
        :type upsert_items_request: UpsertItemsRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_items_post_serialize(
            id=id,
            upsert_items_request=upsert_items_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "UpsertItemsResponse",
            '400': None,
            '401': "ApiV1TemplatesGet401Response",
            '403': None,
            '404': None,
            '409': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_id_items_post_serialize(
        self,
        id,
        upsert_items_request,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if upsert_items_request is not None:
            _body_params = upsert_items_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/demands/{id}/items',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_id_parties_party_id_resend_post(
        self,
        id: UUID,
        party_id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsIdPartiesPartyIdResendPost200Response:
        """Tekil tarafa imza davetini tekrar gönder

        Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa, onayını vermiş onaylayana, açık düzeltme talebi olan tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). Sıralı imzada onaylayanın onayı o adımı tamamlar; sıra bir sonraki tarafa geçer. 

        :param id: (required)
        :type id: UUID
        :param party_id: (required)
        :type party_id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_parties_party_id_resend_post_serialize(
            id=id,
            party_id=party_id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdPartiesPartyIdResendPost200Response",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "CodedError",
            '429': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_id_parties_party_id_resend_post_with_http_info(
        self,
        id: UUID,
        party_id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsIdPartiesPartyIdResendPost200Response]:
        """Tekil tarafa imza davetini tekrar gönder

        Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa, onayını vermiş onaylayana, açık düzeltme talebi olan tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). Sıralı imzada onaylayanın onayı o adımı tamamlar; sıra bir sonraki tarafa geçer. 

        :param id: (required)
        :type id: UUID
        :param party_id: (required)
        :type party_id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_parties_party_id_resend_post_serialize(
            id=id,
            party_id=party_id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdPartiesPartyIdResendPost200Response",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "CodedError",
            '429': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_id_parties_party_id_resend_post_without_preload_content(
        self,
        id: UUID,
        party_id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Tekil tarafa imza davetini tekrar gönder

        Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa, onayını vermiş onaylayana, açık düzeltme talebi olan tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). Sıralı imzada onaylayanın onayı o adımı tamamlar; sıra bir sonraki tarafa geçer. 

        :param id: (required)
        :type id: UUID
        :param party_id: (required)
        :type party_id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_parties_party_id_resend_post_serialize(
            id=id,
            party_id=party_id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdPartiesPartyIdResendPost200Response",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "CodedError",
            '429': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_id_parties_party_id_resend_post_serialize(
        self,
        id,
        party_id,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        if party_id is not None:
            _path_params['partyId'] = party_id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/demands/{id}/parties/{partyId}/resend',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_id_pdf_get(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> bytes:
        """İmzalı sözleşme PDF'i (auth'lu indirme)

        Tamamlanmış sözleşmenin imzalı PDF'ini indirir. Public `/sonuc/{id}/pdf`'in aksine API key ownership'i zorunludur. 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_pdf_get_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "bytes",
            '404': "ApiV1TemplatesIdGet404Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_id_pdf_get_with_http_info(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[bytes]:
        """İmzalı sözleşme PDF'i (auth'lu indirme)

        Tamamlanmış sözleşmenin imzalı PDF'ini indirir. Public `/sonuc/{id}/pdf`'in aksine API key ownership'i zorunludur. 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_pdf_get_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "bytes",
            '404': "ApiV1TemplatesIdGet404Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_id_pdf_get_without_preload_content(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """İmzalı sözleşme PDF'i (auth'lu indirme)

        Tamamlanmış sözleşmenin imzalı PDF'ini indirir. Public `/sonuc/{id}/pdf`'in aksine API key ownership'i zorunludur. 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_pdf_get_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "bytes",
            '404': "ApiV1TemplatesIdGet404Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_id_pdf_get_serialize(
        self,
        id,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/pdf', 
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/api/v1/demands/{id}/pdf',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_id_term_patch(
        self,
        id: UUID,
        contract_term_input: ContractTermInput,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsIdTermPatch200Response:
        """Sözleşme süre/yenileme takibini güncelle

        Kısmi güncelleme: yalnız gövdede gönderilen `ContractTermInput` anahtarları değiştirilir; bir anahtarı `null` göndermek o alanı temizler. Dashboard'daki sözleşme detay sayfasıyla AYNI çekirdek kuralı uygular.  Bitişi etkileyen bir alan (`term_start_mode`, `term_start_date`, `term_duration_months`, `term_fixed_end_date`) kayıttakinden farklı bir DEĞERLE gönderilirse `term_end_date` bilinen başlangıçtan yeniden hesaplanır; aynı değerle gelen alan no-op'tur (otomatik olarak ileri alınmış bitiş korunur). 

        :param id: (required)
        :type id: UUID
        :param contract_term_input: (required)
        :type contract_term_input: ContractTermInput
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_term_patch_serialize(
            id=id,
            contract_term_input=contract_term_input,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdTermPatch200Response",
            '400': "CodedError",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "CodedError",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_id_term_patch_with_http_info(
        self,
        id: UUID,
        contract_term_input: ContractTermInput,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsIdTermPatch200Response]:
        """Sözleşme süre/yenileme takibini güncelle

        Kısmi güncelleme: yalnız gövdede gönderilen `ContractTermInput` anahtarları değiştirilir; bir anahtarı `null` göndermek o alanı temizler. Dashboard'daki sözleşme detay sayfasıyla AYNI çekirdek kuralı uygular.  Bitişi etkileyen bir alan (`term_start_mode`, `term_start_date`, `term_duration_months`, `term_fixed_end_date`) kayıttakinden farklı bir DEĞERLE gönderilirse `term_end_date` bilinen başlangıçtan yeniden hesaplanır; aynı değerle gelen alan no-op'tur (otomatik olarak ileri alınmış bitiş korunur). 

        :param id: (required)
        :type id: UUID
        :param contract_term_input: (required)
        :type contract_term_input: ContractTermInput
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_term_patch_serialize(
            id=id,
            contract_term_input=contract_term_input,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdTermPatch200Response",
            '400': "CodedError",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "CodedError",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_id_term_patch_without_preload_content(
        self,
        id: UUID,
        contract_term_input: ContractTermInput,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Sözleşme süre/yenileme takibini güncelle

        Kısmi güncelleme: yalnız gövdede gönderilen `ContractTermInput` anahtarları değiştirilir; bir anahtarı `null` göndermek o alanı temizler. Dashboard'daki sözleşme detay sayfasıyla AYNI çekirdek kuralı uygular.  Bitişi etkileyen bir alan (`term_start_mode`, `term_start_date`, `term_duration_months`, `term_fixed_end_date`) kayıttakinden farklı bir DEĞERLE gönderilirse `term_end_date` bilinen başlangıçtan yeniden hesaplanır; aynı değerle gelen alan no-op'tur (otomatik olarak ileri alınmış bitiş korunur). 

        :param id: (required)
        :type id: UUID
        :param contract_term_input: (required)
        :type contract_term_input: ContractTermInput
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_term_patch_serialize(
            id=id,
            contract_term_input=contract_term_input,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdTermPatch200Response",
            '400': "CodedError",
            '404': "ApiV1TemplatesIdGet404Response",
            '409': "CodedError",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_id_term_patch_serialize(
        self,
        id,
        contract_term_input,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if contract_term_input is not None:
            _body_params = contract_term_input


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='PATCH',
            resource_path='/api/v1/demands/{id}/term',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_id_timeline_get(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsIdTimelineGet200Response:
        """İmza denetim izi (maskeli)

        Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP `ip_masked` (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez.  `event_type` değerleri: `CREATED`, `SENT`, `VIEWED`, `FIELDS_FILLED`, `COMMENT_ADDED`, `SIGNED`, `APPROVED`, `REJECTED`, `TIMESTAMPED`, `COMPLETED`, `OTP_SENT` (SMS doğrulama kodu gönderildi), `OTP_VERIFIED` (SMS doğrulama kodu doğrulandı), `OTP_LOCKED` (deneme sınırı doldu), `MOBILE_SIGNATURE_CAPTURED` (imza QR kod ile telefonda çizildi; bu olayın `ip_masked` ve `device_label` alanları telefona aittir), `REAPPROVAL_REQUIRED` (gönderen açık bir değişiklik talebi sırasında sözleşme içeriğini güncelledi, önceki onaylar sıfırlandı ve onaylayanların yeniden onay vermesi gerekiyor). Liste ileride genişleyebilir; tanımadığınız değeri yok saymanız önerilir. 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_timeline_get_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdTimelineGet200Response",
            '404': "ApiV1TemplatesIdGet404Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_id_timeline_get_with_http_info(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsIdTimelineGet200Response]:
        """İmza denetim izi (maskeli)

        Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP `ip_masked` (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez.  `event_type` değerleri: `CREATED`, `SENT`, `VIEWED`, `FIELDS_FILLED`, `COMMENT_ADDED`, `SIGNED`, `APPROVED`, `REJECTED`, `TIMESTAMPED`, `COMPLETED`, `OTP_SENT` (SMS doğrulama kodu gönderildi), `OTP_VERIFIED` (SMS doğrulama kodu doğrulandı), `OTP_LOCKED` (deneme sınırı doldu), `MOBILE_SIGNATURE_CAPTURED` (imza QR kod ile telefonda çizildi; bu olayın `ip_masked` ve `device_label` alanları telefona aittir), `REAPPROVAL_REQUIRED` (gönderen açık bir değişiklik talebi sırasında sözleşme içeriğini güncelledi, önceki onaylar sıfırlandı ve onaylayanların yeniden onay vermesi gerekiyor). Liste ileride genişleyebilir; tanımadığınız değeri yok saymanız önerilir. 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_timeline_get_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdTimelineGet200Response",
            '404': "ApiV1TemplatesIdGet404Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_id_timeline_get_without_preload_content(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """İmza denetim izi (maskeli)

        Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP `ip_masked` (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez.  `event_type` değerleri: `CREATED`, `SENT`, `VIEWED`, `FIELDS_FILLED`, `COMMENT_ADDED`, `SIGNED`, `APPROVED`, `REJECTED`, `TIMESTAMPED`, `COMPLETED`, `OTP_SENT` (SMS doğrulama kodu gönderildi), `OTP_VERIFIED` (SMS doğrulama kodu doğrulandı), `OTP_LOCKED` (deneme sınırı doldu), `MOBILE_SIGNATURE_CAPTURED` (imza QR kod ile telefonda çizildi; bu olayın `ip_masked` ve `device_label` alanları telefona aittir), `REAPPROVAL_REQUIRED` (gönderen açık bir değişiklik talebi sırasında sözleşme içeriğini güncelledi, önceki onaylar sıfırlandı ve onaylayanların yeniden onay vermesi gerekiyor). Liste ileride genişleyebilir; tanımadığınız değeri yok saymanız önerilir. 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_timeline_get_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdTimelineGet200Response",
            '404': "ApiV1TemplatesIdGet404Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_id_timeline_get_serialize(
        self,
        id,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/api/v1/demands/{id}/timeline',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_id_unarchive_post(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsIdUnarchivePost200Response:
        """Sözleşmeyi arşivden çıkar

        `archived_at`'i temizler; sözleşme yeniden mutasyona açılır. Zaten arşivsizse idempotent (200, `archived_at: null`). 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_unarchive_post_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdUnarchivePost200Response",
            '404': "ApiV1TemplatesIdGet404Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_id_unarchive_post_with_http_info(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsIdUnarchivePost200Response]:
        """Sözleşmeyi arşivden çıkar

        `archived_at`'i temizler; sözleşme yeniden mutasyona açılır. Zaten arşivsizse idempotent (200, `archived_at: null`). 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_unarchive_post_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdUnarchivePost200Response",
            '404': "ApiV1TemplatesIdGet404Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_id_unarchive_post_without_preload_content(
        self,
        id: UUID,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Sözleşmeyi arşivden çıkar

        `archived_at`'i temizler; sözleşme yeniden mutasyona açılır. Zaten arşivsizse idempotent (200, `archived_at: null`). 

        :param id: (required)
        :type id: UUID
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_id_unarchive_post_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1DemandsIdUnarchivePost200Response",
            '404': "ApiV1TemplatesIdGet404Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_id_unarchive_post_serialize(
        self,
        id,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/demands/{id}/unarchive',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_post(
        self,
        create_demand_request: CreateDemandRequest,
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. ")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsPost201Response:
        """Sözleşme oluştur (şablondan)

        Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field'ları `variables` payload'undan doldurur ve imzalama URL'lerini döner.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa oluşturma anında yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez. Ayrıntı: açıklamadaki \"Onaylayan (onay adımı)\" bölümü.  **Variable resolution:** - Item'ın `template_party_id` non-null → `party_mapping[i].variables`'ta   o slug var ise oradan uygulanır - Yoksa root `variables`'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı  **Kredi (çok belgeli zarf):** Şablon çok-belgeli bir zarf tanımlıyorsa (her belge farklı imzacı(lar)a atanabilir), imzacı başına kredi o imzacıya ATANMIŞ belge sayısına göre hesaplanır ve belge sayısına göre azalan birim fiyatlı bir indirim uygulanır: 1 belge %0, 2 belge %30, 3 ve üzeri %50 (indirim imzacı başına ayrı hesaplanır ve yukarı yuvarlanır). AB nitelikli zaman damgası (`eidas_timestamp`) seçiliyse aynı indirim zarftaki farklı belge sayısına göre toplam üzerinden uygulanır. Tek belgeli şablonlarda tutar eski formülle bayt-aynıdır.  **Atamasız imzacı:** `dispatch_notifications` `false` gönderilmediği sürece, eşlenen bir taraf şablonun hiçbir belgesine atanmamışsa sözleşme HİÇ oluşturulmaz (409 `PARTY_WITHOUT_DOCUMENTS`). Bu kontrolü atlamak için `dispatch_notifications: false` gönderip belge atamalarını `PUT .../documents/{docId}/assignments` ile düzelttikten sonra `POST .../dispatch` ile gönderin (o uç aynı kapıyı yeniden uygular). 

        :param create_demand_request: (required)
        :type create_demand_request: CreateDemandRequest
        :param idempotency_key: Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
        :type idempotency_key: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_post_serialize(
            create_demand_request=create_demand_request,
            idempotency_key=idempotency_key,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "ApiV1DemandsPost201Response",
            '400': "ApiV1DemandsPost400Response",
            '401': "ApiV1TemplatesGet401Response",
            '402': "ApiV1DemandsPost402Response",
            '403': "ApiV1TemplatesGet401Response",
            '409': None,
            '500': "ApiV1DemandsPost500Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_post_with_http_info(
        self,
        create_demand_request: CreateDemandRequest,
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. ")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsPost201Response]:
        """Sözleşme oluştur (şablondan)

        Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field'ları `variables` payload'undan doldurur ve imzalama URL'lerini döner.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa oluşturma anında yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez. Ayrıntı: açıklamadaki \"Onaylayan (onay adımı)\" bölümü.  **Variable resolution:** - Item'ın `template_party_id` non-null → `party_mapping[i].variables`'ta   o slug var ise oradan uygulanır - Yoksa root `variables`'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı  **Kredi (çok belgeli zarf):** Şablon çok-belgeli bir zarf tanımlıyorsa (her belge farklı imzacı(lar)a atanabilir), imzacı başına kredi o imzacıya ATANMIŞ belge sayısına göre hesaplanır ve belge sayısına göre azalan birim fiyatlı bir indirim uygulanır: 1 belge %0, 2 belge %30, 3 ve üzeri %50 (indirim imzacı başına ayrı hesaplanır ve yukarı yuvarlanır). AB nitelikli zaman damgası (`eidas_timestamp`) seçiliyse aynı indirim zarftaki farklı belge sayısına göre toplam üzerinden uygulanır. Tek belgeli şablonlarda tutar eski formülle bayt-aynıdır.  **Atamasız imzacı:** `dispatch_notifications` `false` gönderilmediği sürece, eşlenen bir taraf şablonun hiçbir belgesine atanmamışsa sözleşme HİÇ oluşturulmaz (409 `PARTY_WITHOUT_DOCUMENTS`). Bu kontrolü atlamak için `dispatch_notifications: false` gönderip belge atamalarını `PUT .../documents/{docId}/assignments` ile düzelttikten sonra `POST .../dispatch` ile gönderin (o uç aynı kapıyı yeniden uygular). 

        :param create_demand_request: (required)
        :type create_demand_request: CreateDemandRequest
        :param idempotency_key: Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
        :type idempotency_key: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_post_serialize(
            create_demand_request=create_demand_request,
            idempotency_key=idempotency_key,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "ApiV1DemandsPost201Response",
            '400': "ApiV1DemandsPost400Response",
            '401': "ApiV1TemplatesGet401Response",
            '402': "ApiV1DemandsPost402Response",
            '403': "ApiV1TemplatesGet401Response",
            '409': None,
            '500': "ApiV1DemandsPost500Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_post_without_preload_content(
        self,
        create_demand_request: CreateDemandRequest,
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. ")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Sözleşme oluştur (şablondan)

        Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field'ları `variables` payload'undan doldurur ve imzalama URL'lerini döner.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa oluşturma anında yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez. Ayrıntı: açıklamadaki \"Onaylayan (onay adımı)\" bölümü.  **Variable resolution:** - Item'ın `template_party_id` non-null → `party_mapping[i].variables`'ta   o slug var ise oradan uygulanır - Yoksa root `variables`'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı  **Kredi (çok belgeli zarf):** Şablon çok-belgeli bir zarf tanımlıyorsa (her belge farklı imzacı(lar)a atanabilir), imzacı başına kredi o imzacıya ATANMIŞ belge sayısına göre hesaplanır ve belge sayısına göre azalan birim fiyatlı bir indirim uygulanır: 1 belge %0, 2 belge %30, 3 ve üzeri %50 (indirim imzacı başına ayrı hesaplanır ve yukarı yuvarlanır). AB nitelikli zaman damgası (`eidas_timestamp`) seçiliyse aynı indirim zarftaki farklı belge sayısına göre toplam üzerinden uygulanır. Tek belgeli şablonlarda tutar eski formülle bayt-aynıdır.  **Atamasız imzacı:** `dispatch_notifications` `false` gönderilmediği sürece, eşlenen bir taraf şablonun hiçbir belgesine atanmamışsa sözleşme HİÇ oluşturulmaz (409 `PARTY_WITHOUT_DOCUMENTS`). Bu kontrolü atlamak için `dispatch_notifications: false` gönderip belge atamalarını `PUT .../documents/{docId}/assignments` ile düzelttikten sonra `POST .../dispatch` ile gönderin (o uç aynı kapıyı yeniden uygular). 

        :param create_demand_request: (required)
        :type create_demand_request: CreateDemandRequest
        :param idempotency_key: Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
        :type idempotency_key: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_post_serialize(
            create_demand_request=create_demand_request,
            idempotency_key=idempotency_key,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "ApiV1DemandsPost201Response",
            '400': "ApiV1DemandsPost400Response",
            '401': "ApiV1TemplatesGet401Response",
            '402': "ApiV1DemandsPost402Response",
            '403': "ApiV1TemplatesGet401Response",
            '409': None,
            '500': "ApiV1DemandsPost500Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_post_serialize(
        self,
        create_demand_request,
        idempotency_key,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        # process the query parameters
        # process the header parameters
        if idempotency_key is not None:
            _header_params['Idempotency-Key'] = idempotency_key
        # process the form parameters
        # process the body parameter
        if create_demand_request is not None:
            _body_params = create_demand_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/demands',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_demands_upload_post(
        self,
        files: Annotated[List[Union[StrictBytes, StrictStr, Tuple[StrictStr, StrictBytes]]], Field(description="1 belge VEYA 1-20 görsel")],
        parties: Annotated[StrictStr, Field(description="JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  `field_template_id` gönderildiğinde her party AYRICA `template_party_id` (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel `locale` (`tr`, `en` veya `null`): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. `PartyMappingInput.locale`). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer `400 INVALID_LOCALE` döner, sözleşme oluşturulmaz. ")],
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. ")] = None,
        order: Annotated[Optional[StrictStr], Field(description="Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\")")] = None,
        title: Annotated[Optional[StrictStr], Field(description="Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS'lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın. ")] = None,
        description: Optional[StrictStr] = None,
        field_template_id: Annotated[Optional[UUID], Field(description="Alan Şablonu (`kind: FIELD_LAYOUT`) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - `template_id` ile **birlikte gönderilemez**   (400 `FIELD_TEMPLATE_CONFLICT`). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için `POST /api/v1/field-templates/{id}/preview-layout`. ")] = None,
        force: Annotated[Optional[StrictStr], Field(description="Kopya kapısını bilerek geç. Yalnız `Idempotency-Key` GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için. ")] = None,
        send_invitations: Annotated[Optional[StrictStr], Field(description="`\\\"true\\\"` (veya `\\\"1\\\"`) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca `POST /api/v1/demands/{id}/parties/{partyId}/resend` çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 `INVALID_SEND_INVITATIONS`   döner (fail-closed). - **Açık değerler:** `\\\"true\\\"`, `\\\"1\\\"`, `\\\"all\\\"`, `\\\"email\\\"`, `\\\"sms\\\"`. - **Kapalı değerler:** `\\\"false\\\"`, `\\\"0\\\"`, `\\\"off\\\"`, `\\\"no\\\"`,   `\\\"hayir\\\"`, `\\\"hayır\\\"`. - Ad bilerek `dispatch_notifications` **değildir**: o   parametre `POST /api/v1/demands` ve `/demands/bulk`   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: `\\\"true\\\"`, `\\\"1\\\"` ve `\\\"all\\\"` tüm kanalları kullanır;   `\\\"email\\\"` yalnız e-posta, `\\\"sms\\\"` telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   `send_sms`/`send_email` bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (`dispatch.error = DISPATCH_TOO_MANY`), tarafları   `resend` ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği `Idempotency-Key`   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı `DUPLICATE_SUSPECTED` yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte `Idempotency-Key` gönderin;   anahtar yoksa yeniden göndermek yerine   `GET /api/v1/demands?...` ile sonucu doğrulayın. ")] = None,
        on_anchor_miss: Annotated[Optional[StrictStr], Field(description="Çapa bulunamadığında ne yapılacağı. Yalnız `field_template_id` ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, `block` uygulanır. - `drop`, yalnız şablonun İLGİLİ TÜM alanları zaten `DROP`   ise uygulanır; aksi halde yok sayılır, `block` uygulanır   ve yanıtta `ON_ANCHOR_MISS_NOT_RELAXED` uyarısı döner. - İmza alanlarında `drop` hiçbir koşulda uygulanmaz. - `fallback` bu API'de **yoktur** (400 `INVALID_ON_ANCHOR_MISS`).  Yanıttaki `data.field_layout.on_anchor_miss` istenen değil, **uygulanan** değeri taşır. ")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1DemandsUploadPost201Response:
        """Dosya upload ile sözleşme oluştur (şablonsuz)

        Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF'e birleştirilir, office formatları LibreOffice ile PDF'e çevrilir. 

        :param files: 1 belge VEYA 1-20 görsel (required)
        :type files: List[bytes]
        :param parties: JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  `field_template_id` gönderildiğinde her party AYRICA `template_party_id` (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel `locale` (`tr`, `en` veya `null`): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. `PartyMappingInput.locale`). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer `400 INVALID_LOCALE` döner, sözleşme oluşturulmaz.  (required)
        :type parties: str
        :param idempotency_key: Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
        :type idempotency_key: str
        :param order: Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\")
        :type order: str
        :param title: Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS'lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın. 
        :type title: str
        :param description:
        :type description: str
        :param field_template_id: Alan Şablonu (`kind: FIELD_LAYOUT`) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - `template_id` ile **birlikte gönderilemez**   (400 `FIELD_TEMPLATE_CONFLICT`). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için `POST /api/v1/field-templates/{id}/preview-layout`. 
        :type field_template_id: UUID
        :param force: Kopya kapısını bilerek geç. Yalnız `Idempotency-Key` GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için. 
        :type force: str
        :param send_invitations: `\\\"true\\\"` (veya `\\\"1\\\"`) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca `POST /api/v1/demands/{id}/parties/{partyId}/resend` çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 `INVALID_SEND_INVITATIONS`   döner (fail-closed). - **Açık değerler:** `\\\"true\\\"`, `\\\"1\\\"`, `\\\"all\\\"`, `\\\"email\\\"`, `\\\"sms\\\"`. - **Kapalı değerler:** `\\\"false\\\"`, `\\\"0\\\"`, `\\\"off\\\"`, `\\\"no\\\"`,   `\\\"hayir\\\"`, `\\\"hayır\\\"`. - Ad bilerek `dispatch_notifications` **değildir**: o   parametre `POST /api/v1/demands` ve `/demands/bulk`   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: `\\\"true\\\"`, `\\\"1\\\"` ve `\\\"all\\\"` tüm kanalları kullanır;   `\\\"email\\\"` yalnız e-posta, `\\\"sms\\\"` telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   `send_sms`/`send_email` bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (`dispatch.error = DISPATCH_TOO_MANY`), tarafları   `resend` ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği `Idempotency-Key`   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı `DUPLICATE_SUSPECTED` yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte `Idempotency-Key` gönderin;   anahtar yoksa yeniden göndermek yerine   `GET /api/v1/demands?...` ile sonucu doğrulayın. 
        :type send_invitations: str
        :param on_anchor_miss: Çapa bulunamadığında ne yapılacağı. Yalnız `field_template_id` ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, `block` uygulanır. - `drop`, yalnız şablonun İLGİLİ TÜM alanları zaten `DROP`   ise uygulanır; aksi halde yok sayılır, `block` uygulanır   ve yanıtta `ON_ANCHOR_MISS_NOT_RELAXED` uyarısı döner. - İmza alanlarında `drop` hiçbir koşulda uygulanmaz. - `fallback` bu API'de **yoktur** (400 `INVALID_ON_ANCHOR_MISS`).  Yanıttaki `data.field_layout.on_anchor_miss` istenen değil, **uygulanan** değeri taşır. 
        :type on_anchor_miss: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_upload_post_serialize(
            files=files,
            parties=parties,
            idempotency_key=idempotency_key,
            order=order,
            title=title,
            description=description,
            field_template_id=field_template_id,
            force=force,
            send_invitations=send_invitations,
            on_anchor_miss=on_anchor_miss,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "ApiV1DemandsUploadPost201Response",
            '400': "ApiV1TemplatesIdGet404Response",
            '401': "ApiV1TemplatesGet401Response",
            '402': "ApiV1DemandsUploadPost402Response",
            '403': None,
            '404': None,
            '409': None,
            '413': None,
            '415': None,
            '422': "FieldLayoutUnresolved",
            '429': None,
            '500': "ApiV1DemandsUploadPost500Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_demands_upload_post_with_http_info(
        self,
        files: Annotated[List[Union[StrictBytes, StrictStr, Tuple[StrictStr, StrictBytes]]], Field(description="1 belge VEYA 1-20 görsel")],
        parties: Annotated[StrictStr, Field(description="JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  `field_template_id` gönderildiğinde her party AYRICA `template_party_id` (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel `locale` (`tr`, `en` veya `null`): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. `PartyMappingInput.locale`). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer `400 INVALID_LOCALE` döner, sözleşme oluşturulmaz. ")],
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. ")] = None,
        order: Annotated[Optional[StrictStr], Field(description="Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\")")] = None,
        title: Annotated[Optional[StrictStr], Field(description="Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS'lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın. ")] = None,
        description: Optional[StrictStr] = None,
        field_template_id: Annotated[Optional[UUID], Field(description="Alan Şablonu (`kind: FIELD_LAYOUT`) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - `template_id` ile **birlikte gönderilemez**   (400 `FIELD_TEMPLATE_CONFLICT`). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için `POST /api/v1/field-templates/{id}/preview-layout`. ")] = None,
        force: Annotated[Optional[StrictStr], Field(description="Kopya kapısını bilerek geç. Yalnız `Idempotency-Key` GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için. ")] = None,
        send_invitations: Annotated[Optional[StrictStr], Field(description="`\\\"true\\\"` (veya `\\\"1\\\"`) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca `POST /api/v1/demands/{id}/parties/{partyId}/resend` çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 `INVALID_SEND_INVITATIONS`   döner (fail-closed). - **Açık değerler:** `\\\"true\\\"`, `\\\"1\\\"`, `\\\"all\\\"`, `\\\"email\\\"`, `\\\"sms\\\"`. - **Kapalı değerler:** `\\\"false\\\"`, `\\\"0\\\"`, `\\\"off\\\"`, `\\\"no\\\"`,   `\\\"hayir\\\"`, `\\\"hayır\\\"`. - Ad bilerek `dispatch_notifications` **değildir**: o   parametre `POST /api/v1/demands` ve `/demands/bulk`   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: `\\\"true\\\"`, `\\\"1\\\"` ve `\\\"all\\\"` tüm kanalları kullanır;   `\\\"email\\\"` yalnız e-posta, `\\\"sms\\\"` telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   `send_sms`/`send_email` bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (`dispatch.error = DISPATCH_TOO_MANY`), tarafları   `resend` ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği `Idempotency-Key`   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı `DUPLICATE_SUSPECTED` yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte `Idempotency-Key` gönderin;   anahtar yoksa yeniden göndermek yerine   `GET /api/v1/demands?...` ile sonucu doğrulayın. ")] = None,
        on_anchor_miss: Annotated[Optional[StrictStr], Field(description="Çapa bulunamadığında ne yapılacağı. Yalnız `field_template_id` ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, `block` uygulanır. - `drop`, yalnız şablonun İLGİLİ TÜM alanları zaten `DROP`   ise uygulanır; aksi halde yok sayılır, `block` uygulanır   ve yanıtta `ON_ANCHOR_MISS_NOT_RELAXED` uyarısı döner. - İmza alanlarında `drop` hiçbir koşulda uygulanmaz. - `fallback` bu API'de **yoktur** (400 `INVALID_ON_ANCHOR_MISS`).  Yanıttaki `data.field_layout.on_anchor_miss` istenen değil, **uygulanan** değeri taşır. ")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1DemandsUploadPost201Response]:
        """Dosya upload ile sözleşme oluştur (şablonsuz)

        Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF'e birleştirilir, office formatları LibreOffice ile PDF'e çevrilir. 

        :param files: 1 belge VEYA 1-20 görsel (required)
        :type files: List[bytes]
        :param parties: JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  `field_template_id` gönderildiğinde her party AYRICA `template_party_id` (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel `locale` (`tr`, `en` veya `null`): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. `PartyMappingInput.locale`). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer `400 INVALID_LOCALE` döner, sözleşme oluşturulmaz.  (required)
        :type parties: str
        :param idempotency_key: Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
        :type idempotency_key: str
        :param order: Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\")
        :type order: str
        :param title: Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS'lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın. 
        :type title: str
        :param description:
        :type description: str
        :param field_template_id: Alan Şablonu (`kind: FIELD_LAYOUT`) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - `template_id` ile **birlikte gönderilemez**   (400 `FIELD_TEMPLATE_CONFLICT`). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için `POST /api/v1/field-templates/{id}/preview-layout`. 
        :type field_template_id: UUID
        :param force: Kopya kapısını bilerek geç. Yalnız `Idempotency-Key` GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için. 
        :type force: str
        :param send_invitations: `\\\"true\\\"` (veya `\\\"1\\\"`) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca `POST /api/v1/demands/{id}/parties/{partyId}/resend` çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 `INVALID_SEND_INVITATIONS`   döner (fail-closed). - **Açık değerler:** `\\\"true\\\"`, `\\\"1\\\"`, `\\\"all\\\"`, `\\\"email\\\"`, `\\\"sms\\\"`. - **Kapalı değerler:** `\\\"false\\\"`, `\\\"0\\\"`, `\\\"off\\\"`, `\\\"no\\\"`,   `\\\"hayir\\\"`, `\\\"hayır\\\"`. - Ad bilerek `dispatch_notifications` **değildir**: o   parametre `POST /api/v1/demands` ve `/demands/bulk`   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: `\\\"true\\\"`, `\\\"1\\\"` ve `\\\"all\\\"` tüm kanalları kullanır;   `\\\"email\\\"` yalnız e-posta, `\\\"sms\\\"` telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   `send_sms`/`send_email` bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (`dispatch.error = DISPATCH_TOO_MANY`), tarafları   `resend` ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği `Idempotency-Key`   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı `DUPLICATE_SUSPECTED` yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte `Idempotency-Key` gönderin;   anahtar yoksa yeniden göndermek yerine   `GET /api/v1/demands?...` ile sonucu doğrulayın. 
        :type send_invitations: str
        :param on_anchor_miss: Çapa bulunamadığında ne yapılacağı. Yalnız `field_template_id` ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, `block` uygulanır. - `drop`, yalnız şablonun İLGİLİ TÜM alanları zaten `DROP`   ise uygulanır; aksi halde yok sayılır, `block` uygulanır   ve yanıtta `ON_ANCHOR_MISS_NOT_RELAXED` uyarısı döner. - İmza alanlarında `drop` hiçbir koşulda uygulanmaz. - `fallback` bu API'de **yoktur** (400 `INVALID_ON_ANCHOR_MISS`).  Yanıttaki `data.field_layout.on_anchor_miss` istenen değil, **uygulanan** değeri taşır. 
        :type on_anchor_miss: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_upload_post_serialize(
            files=files,
            parties=parties,
            idempotency_key=idempotency_key,
            order=order,
            title=title,
            description=description,
            field_template_id=field_template_id,
            force=force,
            send_invitations=send_invitations,
            on_anchor_miss=on_anchor_miss,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "ApiV1DemandsUploadPost201Response",
            '400': "ApiV1TemplatesIdGet404Response",
            '401': "ApiV1TemplatesGet401Response",
            '402': "ApiV1DemandsUploadPost402Response",
            '403': None,
            '404': None,
            '409': None,
            '413': None,
            '415': None,
            '422': "FieldLayoutUnresolved",
            '429': None,
            '500': "ApiV1DemandsUploadPost500Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_demands_upload_post_without_preload_content(
        self,
        files: Annotated[List[Union[StrictBytes, StrictStr, Tuple[StrictStr, StrictBytes]]], Field(description="1 belge VEYA 1-20 görsel")],
        parties: Annotated[StrictStr, Field(description="JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  `field_template_id` gönderildiğinde her party AYRICA `template_party_id` (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel `locale` (`tr`, `en` veya `null`): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. `PartyMappingInput.locale`). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer `400 INVALID_LOCALE` döner, sözleşme oluşturulmaz. ")],
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. ")] = None,
        order: Annotated[Optional[StrictStr], Field(description="Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\")")] = None,
        title: Annotated[Optional[StrictStr], Field(description="Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS'lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın. ")] = None,
        description: Optional[StrictStr] = None,
        field_template_id: Annotated[Optional[UUID], Field(description="Alan Şablonu (`kind: FIELD_LAYOUT`) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - `template_id` ile **birlikte gönderilemez**   (400 `FIELD_TEMPLATE_CONFLICT`). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için `POST /api/v1/field-templates/{id}/preview-layout`. ")] = None,
        force: Annotated[Optional[StrictStr], Field(description="Kopya kapısını bilerek geç. Yalnız `Idempotency-Key` GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için. ")] = None,
        send_invitations: Annotated[Optional[StrictStr], Field(description="`\\\"true\\\"` (veya `\\\"1\\\"`) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca `POST /api/v1/demands/{id}/parties/{partyId}/resend` çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 `INVALID_SEND_INVITATIONS`   döner (fail-closed). - **Açık değerler:** `\\\"true\\\"`, `\\\"1\\\"`, `\\\"all\\\"`, `\\\"email\\\"`, `\\\"sms\\\"`. - **Kapalı değerler:** `\\\"false\\\"`, `\\\"0\\\"`, `\\\"off\\\"`, `\\\"no\\\"`,   `\\\"hayir\\\"`, `\\\"hayır\\\"`. - Ad bilerek `dispatch_notifications` **değildir**: o   parametre `POST /api/v1/demands` ve `/demands/bulk`   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: `\\\"true\\\"`, `\\\"1\\\"` ve `\\\"all\\\"` tüm kanalları kullanır;   `\\\"email\\\"` yalnız e-posta, `\\\"sms\\\"` telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   `send_sms`/`send_email` bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (`dispatch.error = DISPATCH_TOO_MANY`), tarafları   `resend` ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği `Idempotency-Key`   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı `DUPLICATE_SUSPECTED` yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte `Idempotency-Key` gönderin;   anahtar yoksa yeniden göndermek yerine   `GET /api/v1/demands?...` ile sonucu doğrulayın. ")] = None,
        on_anchor_miss: Annotated[Optional[StrictStr], Field(description="Çapa bulunamadığında ne yapılacağı. Yalnız `field_template_id` ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, `block` uygulanır. - `drop`, yalnız şablonun İLGİLİ TÜM alanları zaten `DROP`   ise uygulanır; aksi halde yok sayılır, `block` uygulanır   ve yanıtta `ON_ANCHOR_MISS_NOT_RELAXED` uyarısı döner. - İmza alanlarında `drop` hiçbir koşulda uygulanmaz. - `fallback` bu API'de **yoktur** (400 `INVALID_ON_ANCHOR_MISS`).  Yanıttaki `data.field_layout.on_anchor_miss` istenen değil, **uygulanan** değeri taşır. ")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Dosya upload ile sözleşme oluştur (şablonsuz)

        Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF'e birleştirilir, office formatları LibreOffice ile PDF'e çevrilir. 

        :param files: 1 belge VEYA 1-20 görsel (required)
        :type files: List[bytes]
        :param parties: JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  `field_template_id` gönderildiğinde her party AYRICA `template_party_id` (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel `locale` (`tr`, `en` veya `null`): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. `PartyMappingInput.locale`). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer `400 INVALID_LOCALE` döner, sözleşme oluşturulmaz.  (required)
        :type parties: str
        :param idempotency_key: Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
        :type idempotency_key: str
        :param order: Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\")
        :type order: str
        :param title: Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS'lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın. 
        :type title: str
        :param description:
        :type description: str
        :param field_template_id: Alan Şablonu (`kind: FIELD_LAYOUT`) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - `template_id` ile **birlikte gönderilemez**   (400 `FIELD_TEMPLATE_CONFLICT`). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için `POST /api/v1/field-templates/{id}/preview-layout`. 
        :type field_template_id: UUID
        :param force: Kopya kapısını bilerek geç. Yalnız `Idempotency-Key` GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için. 
        :type force: str
        :param send_invitations: `\\\"true\\\"` (veya `\\\"1\\\"`) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca `POST /api/v1/demands/{id}/parties/{partyId}/resend` çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 `INVALID_SEND_INVITATIONS`   döner (fail-closed). - **Açık değerler:** `\\\"true\\\"`, `\\\"1\\\"`, `\\\"all\\\"`, `\\\"email\\\"`, `\\\"sms\\\"`. - **Kapalı değerler:** `\\\"false\\\"`, `\\\"0\\\"`, `\\\"off\\\"`, `\\\"no\\\"`,   `\\\"hayir\\\"`, `\\\"hayır\\\"`. - Ad bilerek `dispatch_notifications` **değildir**: o   parametre `POST /api/v1/demands` ve `/demands/bulk`   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: `\\\"true\\\"`, `\\\"1\\\"` ve `\\\"all\\\"` tüm kanalları kullanır;   `\\\"email\\\"` yalnız e-posta, `\\\"sms\\\"` telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   `send_sms`/`send_email` bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (`dispatch.error = DISPATCH_TOO_MANY`), tarafları   `resend` ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği `Idempotency-Key`   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı `DUPLICATE_SUSPECTED` yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte `Idempotency-Key` gönderin;   anahtar yoksa yeniden göndermek yerine   `GET /api/v1/demands?...` ile sonucu doğrulayın. 
        :type send_invitations: str
        :param on_anchor_miss: Çapa bulunamadığında ne yapılacağı. Yalnız `field_template_id` ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, `block` uygulanır. - `drop`, yalnız şablonun İLGİLİ TÜM alanları zaten `DROP`   ise uygulanır; aksi halde yok sayılır, `block` uygulanır   ve yanıtta `ON_ANCHOR_MISS_NOT_RELAXED` uyarısı döner. - İmza alanlarında `drop` hiçbir koşulda uygulanmaz. - `fallback` bu API'de **yoktur** (400 `INVALID_ON_ANCHOR_MISS`).  Yanıttaki `data.field_layout.on_anchor_miss` istenen değil, **uygulanan** değeri taşır. 
        :type on_anchor_miss: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_demands_upload_post_serialize(
            files=files,
            parties=parties,
            idempotency_key=idempotency_key,
            order=order,
            title=title,
            description=description,
            field_template_id=field_template_id,
            force=force,
            send_invitations=send_invitations,
            on_anchor_miss=on_anchor_miss,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "ApiV1DemandsUploadPost201Response",
            '400': "ApiV1TemplatesIdGet404Response",
            '401': "ApiV1TemplatesGet401Response",
            '402': "ApiV1DemandsUploadPost402Response",
            '403': None,
            '404': None,
            '409': None,
            '413': None,
            '415': None,
            '422': "FieldLayoutUnresolved",
            '429': None,
            '500': "ApiV1DemandsUploadPost500Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_demands_upload_post_serialize(
        self,
        files,
        parties,
        idempotency_key,
        order,
        title,
        description,
        field_template_id,
        force,
        send_invitations,
        on_anchor_miss,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
            'files': 'csv',
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        # process the query parameters
        # process the header parameters
        if idempotency_key is not None:
            _header_params['Idempotency-Key'] = idempotency_key
        # process the form parameters
        if files is not None:
            _files['files'] = files
        if order is not None:
            _form_params.append(('order', order))
        if title is not None:
            _form_params.append(('title', title))
        if description is not None:
            _form_params.append(('description', description))
        if parties is not None:
            _form_params.append(('parties', parties))
        if field_template_id is not None:
            _form_params.append(('field_template_id', field_template_id))
        if force is not None:
            _form_params.append(('force', force))
        if send_invitations is not None:
            _form_params.append(('send_invitations', send_invitations))
        if on_anchor_miss is not None:
            _form_params.append(('on_anchor_miss', on_anchor_miss))
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'multipart/form-data'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/demands/upload',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def api_v1_field_templates_id_preview_layout_post(
        self,
        id: Annotated[UUID, Field(description="Alan Şablonu (FIELD_LAYOUT) kimliği")],
        files: Annotated[List[Union[StrictBytes, StrictStr, Tuple[StrictStr, StrictBytes]]], Field(description="Tek PDF belge")],
        on_anchor_miss: Annotated[Optional[StrictStr], Field(description="`POST /api/v1/demands/upload` ile aynı semantik (yalnız sıkılaştırır, gönderilmezse `block`). ")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiV1FieldTemplatesIdPreviewLayoutPost200Response:
        """Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener

        Bir Alan Şablonunun (`kind: FIELD_LAYOUT`) yüklediğiniz PDF'e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  `POST /api/v1/demands/upload` + `field_template_id` çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API'de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (`data.resolvable: false`) — kuru koşumun cevabı \"uygulanamaz\"dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 `RATE_LIMITED`). 

        :param id: Alan Şablonu (FIELD_LAYOUT) kimliği (required)
        :type id: UUID
        :param files: Tek PDF belge (required)
        :type files: List[bytes]
        :param on_anchor_miss: `POST /api/v1/demands/upload` ile aynı semantik (yalnız sıkılaştırır, gönderilmezse `block`). 
        :type on_anchor_miss: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_field_templates_id_preview_layout_post_serialize(
            id=id,
            files=files,
            on_anchor_miss=on_anchor_miss,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1FieldTemplatesIdPreviewLayoutPost200Response",
            '400': None,
            '401': "ApiV1TemplatesGet401Response",
            '403': None,
            '404': None,
            '413': None,
            '415': None,
            '422': None,
            '429': None,
            '503': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def api_v1_field_templates_id_preview_layout_post_with_http_info(
        self,
        id: Annotated[UUID, Field(description="Alan Şablonu (FIELD_LAYOUT) kimliği")],
        files: Annotated[List[Union[StrictBytes, StrictStr, Tuple[StrictStr, StrictBytes]]], Field(description="Tek PDF belge")],
        on_anchor_miss: Annotated[Optional[StrictStr], Field(description="`POST /api/v1/demands/upload` ile aynı semantik (yalnız sıkılaştırır, gönderilmezse `block`). ")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[ApiV1FieldTemplatesIdPreviewLayoutPost200Response]:
        """Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener

        Bir Alan Şablonunun (`kind: FIELD_LAYOUT`) yüklediğiniz PDF'e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  `POST /api/v1/demands/upload` + `field_template_id` çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API'de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (`data.resolvable: false`) — kuru koşumun cevabı \"uygulanamaz\"dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 `RATE_LIMITED`). 

        :param id: Alan Şablonu (FIELD_LAYOUT) kimliği (required)
        :type id: UUID
        :param files: Tek PDF belge (required)
        :type files: List[bytes]
        :param on_anchor_miss: `POST /api/v1/demands/upload` ile aynı semantik (yalnız sıkılaştırır, gönderilmezse `block`). 
        :type on_anchor_miss: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_field_templates_id_preview_layout_post_serialize(
            id=id,
            files=files,
            on_anchor_miss=on_anchor_miss,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1FieldTemplatesIdPreviewLayoutPost200Response",
            '400': None,
            '401': "ApiV1TemplatesGet401Response",
            '403': None,
            '404': None,
            '413': None,
            '415': None,
            '422': None,
            '429': None,
            '503': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def api_v1_field_templates_id_preview_layout_post_without_preload_content(
        self,
        id: Annotated[UUID, Field(description="Alan Şablonu (FIELD_LAYOUT) kimliği")],
        files: Annotated[List[Union[StrictBytes, StrictStr, Tuple[StrictStr, StrictBytes]]], Field(description="Tek PDF belge")],
        on_anchor_miss: Annotated[Optional[StrictStr], Field(description="`POST /api/v1/demands/upload` ile aynı semantik (yalnız sıkılaştırır, gönderilmezse `block`). ")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener

        Bir Alan Şablonunun (`kind: FIELD_LAYOUT`) yüklediğiniz PDF'e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  `POST /api/v1/demands/upload` + `field_template_id` çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API'de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (`data.resolvable: false`) — kuru koşumun cevabı \"uygulanamaz\"dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 `RATE_LIMITED`). 

        :param id: Alan Şablonu (FIELD_LAYOUT) kimliği (required)
        :type id: UUID
        :param files: Tek PDF belge (required)
        :type files: List[bytes]
        :param on_anchor_miss: `POST /api/v1/demands/upload` ile aynı semantik (yalnız sıkılaştırır, gönderilmezse `block`). 
        :type on_anchor_miss: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._api_v1_field_templates_id_preview_layout_post_serialize(
            id=id,
            files=files,
            on_anchor_miss=on_anchor_miss,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ApiV1FieldTemplatesIdPreviewLayoutPost200Response",
            '400': None,
            '401': "ApiV1TemplatesGet401Response",
            '403': None,
            '404': None,
            '413': None,
            '415': None,
            '422': None,
            '429': None,
            '503': None,
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _api_v1_field_templates_id_preview_layout_post_serialize(
        self,
        id,
        files,
        on_anchor_miss,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
            'files': 'csv',
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        if files is not None:
            _files['files'] = files
        if on_anchor_miss is not None:
            _form_params.append(('on_anchor_miss', on_anchor_miss))
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'multipart/form-data'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/field-templates/{id}/preview-layout',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )


