# coding: utf-8

# flake8: noqa

"""
    imzala External API

    imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.8.23 · **Son güncelleme:** 2026-09-24  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API anahtarı kendi organizasyonuna bağlıdır: `X-Workspace-Id` başlığı gönderilmezse anahtarın organizasyonu otomatik uygulanır; gönderilirse anahtarın organizasyonuyla aynı olmalıdır (aksi halde 403 `WORKSPACE_MISMATCH`). Kişisel anahtarlar için bu başlık gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |----|-------|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (8) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı |  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type` — yukarıdaki 8 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 

    The version of the OpenAPI document: 1.8.23
    Contact: destek@imzala.org
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


__version__ = "1.0.0"

# Define package exports
__all__ = [
    "AccountApi",
    "ContactsApi",
    "DemandsApi",
    "RemindersApi",
    "ReportsApi",
    "TemplatesApi",
    "TimestampsApi",
    "ApiResponse",
    "ApiClient",
    "Configuration",
    "OpenApiException",
    "ApiTypeError",
    "ApiValueError",
    "ApiKeyError",
    "ApiAttributeError",
    "ApiException",
    "ApiError",
    "ApiV1ContactsGet200Response",
    "ApiV1ContactsGet200ResponseData",
    "ApiV1ContactsPost201Response",
    "ApiV1ContactsPostRequest",
    "ApiV1DemandsBulkPost200Response",
    "ApiV1DemandsBulkPost200ResponseData",
    "ApiV1DemandsBulkPost200ResponseDataResultsInner",
    "ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails",
    "ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner",
    "ApiV1DemandsBulkPostRequest",
    "ApiV1DemandsBulkPostRequestOptions",
    "ApiV1DemandsBulkPostRequestRowsInner",
    "ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner",
    "ApiV1DemandsDemandIdDispatchPost200Response",
    "ApiV1DemandsDemandIdDispatchPost200ResponseData",
    "ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits",
    "ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations",
    "ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner",
    "ApiV1DemandsDemandIdDispatchPostRequest",
    "ApiV1DemandsDemandIdDispatchPostRequestSendInvitations",
    "ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest",
    "ApiV1DemandsDemandIdDocumentsDocIdPatchRequest",
    "ApiV1DemandsDemandIdDocumentsGet200Response",
    "ApiV1DemandsDemandIdDocumentsGet200ResponseData",
    "ApiV1DemandsDemandIdDocumentsOrderPutRequest",
    "ApiV1DemandsDemandIdDocumentsPost201Response",
    "ApiV1DemandsDemandIdDocumentsPost201ResponseData",
    "ApiV1DemandsDemandIdDocumentsPostRequest",
    "ApiV1DemandsGet200Response",
    "ApiV1DemandsGet200ResponseData",
    "ApiV1DemandsGet200ResponseDataDemandsInner",
    "ApiV1DemandsIdCancelPost200Response",
    "ApiV1DemandsIdCancelPost200ResponseData",
    "ApiV1DemandsIdCancelPostRequest",
    "ApiV1DemandsIdDelete409Response",
    "ApiV1DemandsIdEmbedSessionPost200Response",
    "ApiV1DemandsIdEmbedSessionPost200ResponseData",
    "ApiV1DemandsIdEmbedSessionPostRequest",
    "ApiV1DemandsIdGet200Response",
    "ApiV1DemandsIdPartiesPartyIdResendPost200Response",
    "ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData",
    "ApiV1DemandsIdRemindersPost200Response",
    "ApiV1DemandsIdRemindersPost200ResponseData",
    "ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner",
    "ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner",
    "ApiV1DemandsIdRemindersPost429Response",
    "ApiV1DemandsIdRemindersPost429ResponseError",
    "ApiV1DemandsIdTimelineGet200Response",
    "ApiV1DemandsIdTimelineGet200ResponseData",
    "ApiV1DemandsIdTimelineGet200ResponseDataEventsInner",
    "ApiV1DemandsPost201Response",
    "ApiV1DemandsPost400Response",
    "ApiV1DemandsPost402Response",
    "ApiV1DemandsPost402ResponseData",
    "ApiV1DemandsPost500Response",
    "ApiV1DemandsUploadPost201Response",
    "ApiV1DemandsUploadPost402Response",
    "ApiV1DemandsUploadPost500Response",
    "ApiV1FieldTemplatesGet200Response",
    "ApiV1FieldTemplatesGet200ResponseData",
    "ApiV1FieldTemplatesIdGet200Response",
    "ApiV1FieldTemplatesIdPreviewLayoutPost200Response",
    "ApiV1MeGet200Response",
    "ApiV1MeGet200ResponseData",
    "ApiV1MeGet200ResponseDataCredits",
    "ApiV1MeGet200ResponseDataWorkspace",
    "ApiV1ReportsGet200Response",
    "ApiV1ReportsGet200ResponseData",
    "ApiV1ReportsGet200ResponseDataContracts",
    "ApiV1TemplatesGet200Response",
    "ApiV1TemplatesGet200ResponseData",
    "ApiV1TemplatesGet401Response",
    "ApiV1TemplatesIdDelete200Response",
    "ApiV1TemplatesIdDelete200ResponseData",
    "ApiV1TemplatesIdGet200Response",
    "ApiV1TemplatesIdGet404Response",
    "ApiV1TemplatesIdPatch200Response",
    "ApiV1TemplatesIdPatch200ResponseData",
    "ApiV1TemplatesIdPatchRequest",
    "ApiV1TemplatesIdUsageGet200Response",
    "ApiV1TimestampsGet200Response",
    "ApiV1TimestampsGet200ResponseData",
    "ApiV1TimestampsIdGet200Response",
    "ApiV1TimestampsPost201Response",
    "ApiV1TimestampsPostRequest1",
    "CodedError",
    "ContactSummary",
    "ContactSummaryCompany",
    "CreateDemandRequest",
    "CreatedDemand",
    "CreatedDemandSigningUrlsInner",
    "CreatedDemandStampsAppliedInner",
    "CreatedDemandUpload",
    "CreatedDemandUploadDispatch",
    "CreatedDemandUploadDispatchResultsInner",
    "CreatedDemandUploadFieldLayout",
    "DemandDocumentStatus",
    "DemandPage",
    "DemandStatus",
    "DemandStatusPartiesInner",
    "DemandStatusStampItemsInner",
    "DocumentPartyDecision",
    "DocumentProgress",
    "DocumentScopedSlugs",
    "DocumentSealing",
    "DocumentSelectionError",
    "DocumentSelectionErrorDetails",
    "DocumentSelectionInput",
    "EnvelopeDocument",
    "FieldLayoutDiagnostic",
    "FieldLayoutPreview",
    "FieldLayoutPreviewPlacementsSummary",
    "FieldLayoutPreviewPlacementsSummaryByPageInner",
    "FieldLayoutUnresolved",
    "FieldLayoutWarning",
    "FieldTemplateDetail",
    "FieldTemplateDetailPartiesInner",
    "FieldTemplateListItem",
    "FieldTemplateParty",
    "KycWebhookEnvelope",
    "PageItem",
    "PartyMappingInput",
    "PartyMappingInputVariablesValue",
    "PatchStampItemRequest",
    "PatchStampItemResponse",
    "PatchStampItemResponseData",
    "ReminderSettings",
    "StampData",
    "StandardError",
    "StandardErrorError",
    "TemplateDetail",
    "TemplateDocumentSummary",
    "TemplatePartySummary",
    "TemplateSummary",
    "TemplateSummaryPartiesInner",
    "TemplateUsage",
    "TemplateUsageDocumentsInner",
    "TemplateUsageEndpoint",
    "TemplateUsageExampleRequest",
    "TemplateUsageFormFieldsInner",
    "TemplateUsagePartiesInner",
    "TemplateUsagePartiesInnerSupportedFieldsInner",
    "TemplateUsageVariablesInner",
    "TemplateVariable",
    "TimestampListItem",
    "TimestampRecord",
    "TriggerReminderRequest",
    "UpsertItemsRequest",
    "UpsertItemsResponse",
    "UpsertItemsResponseData",
    "UpsertItemsResponseDataItemsInner",
    "WebhookDataDemandCompleted",
    "WebhookDataDemandCompletedPartiesInner",
    "WebhookDataDemandCreated",
    "WebhookDataDemandExpired",
    "WebhookDataDemandExpiredPartiesInner",
    "WebhookDataKycTerminal",
    "WebhookDataKycTerminalStepsInner",
    "WebhookDataPartyRejected",
    "WebhookDataPartyRejectedParty",
    "WebhookDataPartySigned",
    "WebhookDataPartyViewed",
    "WebhookEnvelope",
]

# import apis into sdk package
from imzala_client.api.account_api import AccountApi as AccountApi
from imzala_client.api.contacts_api import ContactsApi as ContactsApi
from imzala_client.api.demands_api import DemandsApi as DemandsApi
from imzala_client.api.reminders_api import RemindersApi as RemindersApi
from imzala_client.api.reports_api import ReportsApi as ReportsApi
from imzala_client.api.templates_api import TemplatesApi as TemplatesApi
from imzala_client.api.timestamps_api import TimestampsApi as TimestampsApi

# import ApiClient
from imzala_client.api_response import ApiResponse as ApiResponse
from imzala_client.api_client import ApiClient as ApiClient
from imzala_client.configuration import Configuration as Configuration
from imzala_client.exceptions import OpenApiException as OpenApiException
from imzala_client.exceptions import ApiTypeError as ApiTypeError
from imzala_client.exceptions import ApiValueError as ApiValueError
from imzala_client.exceptions import ApiKeyError as ApiKeyError
from imzala_client.exceptions import ApiAttributeError as ApiAttributeError
from imzala_client.exceptions import ApiException as ApiException

# import models into sdk package
from imzala_client.models.api_error import ApiError as ApiError
from imzala_client.models.api_v1_contacts_get200_response import ApiV1ContactsGet200Response as ApiV1ContactsGet200Response
from imzala_client.models.api_v1_contacts_get200_response_data import ApiV1ContactsGet200ResponseData as ApiV1ContactsGet200ResponseData
from imzala_client.models.api_v1_contacts_post201_response import ApiV1ContactsPost201Response as ApiV1ContactsPost201Response
from imzala_client.models.api_v1_contacts_post_request import ApiV1ContactsPostRequest as ApiV1ContactsPostRequest
from imzala_client.models.api_v1_demands_bulk_post200_response import ApiV1DemandsBulkPost200Response as ApiV1DemandsBulkPost200Response
from imzala_client.models.api_v1_demands_bulk_post200_response_data import ApiV1DemandsBulkPost200ResponseData as ApiV1DemandsBulkPost200ResponseData
from imzala_client.models.api_v1_demands_bulk_post200_response_data_results_inner import ApiV1DemandsBulkPost200ResponseDataResultsInner as ApiV1DemandsBulkPost200ResponseDataResultsInner
from imzala_client.models.api_v1_demands_bulk_post200_response_data_results_inner_details import ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails as ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails
from imzala_client.models.api_v1_demands_bulk_post200_response_data_results_inner_signing_urls_inner import ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner as ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner
from imzala_client.models.api_v1_demands_bulk_post_request import ApiV1DemandsBulkPostRequest as ApiV1DemandsBulkPostRequest
from imzala_client.models.api_v1_demands_bulk_post_request_options import ApiV1DemandsBulkPostRequestOptions as ApiV1DemandsBulkPostRequestOptions
from imzala_client.models.api_v1_demands_bulk_post_request_rows_inner import ApiV1DemandsBulkPostRequestRowsInner as ApiV1DemandsBulkPostRequestRowsInner
from imzala_client.models.api_v1_demands_bulk_post_request_rows_inner_party_mapping_inner import ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner as ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner
from imzala_client.models.api_v1_demands_demand_id_dispatch_post200_response import ApiV1DemandsDemandIdDispatchPost200Response as ApiV1DemandsDemandIdDispatchPost200Response
from imzala_client.models.api_v1_demands_demand_id_dispatch_post200_response_data import ApiV1DemandsDemandIdDispatchPost200ResponseData as ApiV1DemandsDemandIdDispatchPost200ResponseData
from imzala_client.models.api_v1_demands_demand_id_dispatch_post200_response_data_credits import ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits as ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits
from imzala_client.models.api_v1_demands_demand_id_dispatch_post200_response_data_invitations import ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations as ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations
from imzala_client.models.api_v1_demands_demand_id_dispatch_post200_response_data_invitations_results_inner import ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner as ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner
from imzala_client.models.api_v1_demands_demand_id_dispatch_post_request import ApiV1DemandsDemandIdDispatchPostRequest as ApiV1DemandsDemandIdDispatchPostRequest
from imzala_client.models.api_v1_demands_demand_id_dispatch_post_request_send_invitations import ApiV1DemandsDemandIdDispatchPostRequestSendInvitations as ApiV1DemandsDemandIdDispatchPostRequestSendInvitations
from imzala_client.models.api_v1_demands_demand_id_documents_doc_id_assignments_put_request import ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest as ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest
from imzala_client.models.api_v1_demands_demand_id_documents_doc_id_patch_request import ApiV1DemandsDemandIdDocumentsDocIdPatchRequest as ApiV1DemandsDemandIdDocumentsDocIdPatchRequest
from imzala_client.models.api_v1_demands_demand_id_documents_get200_response import ApiV1DemandsDemandIdDocumentsGet200Response as ApiV1DemandsDemandIdDocumentsGet200Response
from imzala_client.models.api_v1_demands_demand_id_documents_get200_response_data import ApiV1DemandsDemandIdDocumentsGet200ResponseData as ApiV1DemandsDemandIdDocumentsGet200ResponseData
from imzala_client.models.api_v1_demands_demand_id_documents_order_put_request import ApiV1DemandsDemandIdDocumentsOrderPutRequest as ApiV1DemandsDemandIdDocumentsOrderPutRequest
from imzala_client.models.api_v1_demands_demand_id_documents_post201_response import ApiV1DemandsDemandIdDocumentsPost201Response as ApiV1DemandsDemandIdDocumentsPost201Response
from imzala_client.models.api_v1_demands_demand_id_documents_post201_response_data import ApiV1DemandsDemandIdDocumentsPost201ResponseData as ApiV1DemandsDemandIdDocumentsPost201ResponseData
from imzala_client.models.api_v1_demands_demand_id_documents_post_request import ApiV1DemandsDemandIdDocumentsPostRequest as ApiV1DemandsDemandIdDocumentsPostRequest
from imzala_client.models.api_v1_demands_get200_response import ApiV1DemandsGet200Response as ApiV1DemandsGet200Response
from imzala_client.models.api_v1_demands_get200_response_data import ApiV1DemandsGet200ResponseData as ApiV1DemandsGet200ResponseData
from imzala_client.models.api_v1_demands_get200_response_data_demands_inner import ApiV1DemandsGet200ResponseDataDemandsInner as ApiV1DemandsGet200ResponseDataDemandsInner
from imzala_client.models.api_v1_demands_id_cancel_post200_response import ApiV1DemandsIdCancelPost200Response as ApiV1DemandsIdCancelPost200Response
from imzala_client.models.api_v1_demands_id_cancel_post200_response_data import ApiV1DemandsIdCancelPost200ResponseData as ApiV1DemandsIdCancelPost200ResponseData
from imzala_client.models.api_v1_demands_id_cancel_post_request import ApiV1DemandsIdCancelPostRequest as ApiV1DemandsIdCancelPostRequest
from imzala_client.models.api_v1_demands_id_delete409_response import ApiV1DemandsIdDelete409Response as ApiV1DemandsIdDelete409Response
from imzala_client.models.api_v1_demands_id_embed_session_post200_response import ApiV1DemandsIdEmbedSessionPost200Response as ApiV1DemandsIdEmbedSessionPost200Response
from imzala_client.models.api_v1_demands_id_embed_session_post200_response_data import ApiV1DemandsIdEmbedSessionPost200ResponseData as ApiV1DemandsIdEmbedSessionPost200ResponseData
from imzala_client.models.api_v1_demands_id_embed_session_post_request import ApiV1DemandsIdEmbedSessionPostRequest as ApiV1DemandsIdEmbedSessionPostRequest
from imzala_client.models.api_v1_demands_id_get200_response import ApiV1DemandsIdGet200Response as ApiV1DemandsIdGet200Response
from imzala_client.models.api_v1_demands_id_parties_party_id_resend_post200_response import ApiV1DemandsIdPartiesPartyIdResendPost200Response as ApiV1DemandsIdPartiesPartyIdResendPost200Response
from imzala_client.models.api_v1_demands_id_parties_party_id_resend_post200_response_data import ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData as ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData
from imzala_client.models.api_v1_demands_id_reminders_post200_response import ApiV1DemandsIdRemindersPost200Response as ApiV1DemandsIdRemindersPost200Response
from imzala_client.models.api_v1_demands_id_reminders_post200_response_data import ApiV1DemandsIdRemindersPost200ResponseData as ApiV1DemandsIdRemindersPost200ResponseData
from imzala_client.models.api_v1_demands_id_reminders_post200_response_data_dispatched_inner import ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner as ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner
from imzala_client.models.api_v1_demands_id_reminders_post200_response_data_skipped_inner import ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner as ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner
from imzala_client.models.api_v1_demands_id_reminders_post429_response import ApiV1DemandsIdRemindersPost429Response as ApiV1DemandsIdRemindersPost429Response
from imzala_client.models.api_v1_demands_id_reminders_post429_response_error import ApiV1DemandsIdRemindersPost429ResponseError as ApiV1DemandsIdRemindersPost429ResponseError
from imzala_client.models.api_v1_demands_id_timeline_get200_response import ApiV1DemandsIdTimelineGet200Response as ApiV1DemandsIdTimelineGet200Response
from imzala_client.models.api_v1_demands_id_timeline_get200_response_data import ApiV1DemandsIdTimelineGet200ResponseData as ApiV1DemandsIdTimelineGet200ResponseData
from imzala_client.models.api_v1_demands_id_timeline_get200_response_data_events_inner import ApiV1DemandsIdTimelineGet200ResponseDataEventsInner as ApiV1DemandsIdTimelineGet200ResponseDataEventsInner
from imzala_client.models.api_v1_demands_post201_response import ApiV1DemandsPost201Response as ApiV1DemandsPost201Response
from imzala_client.models.api_v1_demands_post400_response import ApiV1DemandsPost400Response as ApiV1DemandsPost400Response
from imzala_client.models.api_v1_demands_post402_response import ApiV1DemandsPost402Response as ApiV1DemandsPost402Response
from imzala_client.models.api_v1_demands_post402_response_data import ApiV1DemandsPost402ResponseData as ApiV1DemandsPost402ResponseData
from imzala_client.models.api_v1_demands_post500_response import ApiV1DemandsPost500Response as ApiV1DemandsPost500Response
from imzala_client.models.api_v1_demands_upload_post201_response import ApiV1DemandsUploadPost201Response as ApiV1DemandsUploadPost201Response
from imzala_client.models.api_v1_demands_upload_post402_response import ApiV1DemandsUploadPost402Response as ApiV1DemandsUploadPost402Response
from imzala_client.models.api_v1_demands_upload_post500_response import ApiV1DemandsUploadPost500Response as ApiV1DemandsUploadPost500Response
from imzala_client.models.api_v1_field_templates_get200_response import ApiV1FieldTemplatesGet200Response as ApiV1FieldTemplatesGet200Response
from imzala_client.models.api_v1_field_templates_get200_response_data import ApiV1FieldTemplatesGet200ResponseData as ApiV1FieldTemplatesGet200ResponseData
from imzala_client.models.api_v1_field_templates_id_get200_response import ApiV1FieldTemplatesIdGet200Response as ApiV1FieldTemplatesIdGet200Response
from imzala_client.models.api_v1_field_templates_id_preview_layout_post200_response import ApiV1FieldTemplatesIdPreviewLayoutPost200Response as ApiV1FieldTemplatesIdPreviewLayoutPost200Response
from imzala_client.models.api_v1_me_get200_response import ApiV1MeGet200Response as ApiV1MeGet200Response
from imzala_client.models.api_v1_me_get200_response_data import ApiV1MeGet200ResponseData as ApiV1MeGet200ResponseData
from imzala_client.models.api_v1_me_get200_response_data_credits import ApiV1MeGet200ResponseDataCredits as ApiV1MeGet200ResponseDataCredits
from imzala_client.models.api_v1_me_get200_response_data_workspace import ApiV1MeGet200ResponseDataWorkspace as ApiV1MeGet200ResponseDataWorkspace
from imzala_client.models.api_v1_reports_get200_response import ApiV1ReportsGet200Response as ApiV1ReportsGet200Response
from imzala_client.models.api_v1_reports_get200_response_data import ApiV1ReportsGet200ResponseData as ApiV1ReportsGet200ResponseData
from imzala_client.models.api_v1_reports_get200_response_data_contracts import ApiV1ReportsGet200ResponseDataContracts as ApiV1ReportsGet200ResponseDataContracts
from imzala_client.models.api_v1_templates_get200_response import ApiV1TemplatesGet200Response as ApiV1TemplatesGet200Response
from imzala_client.models.api_v1_templates_get200_response_data import ApiV1TemplatesGet200ResponseData as ApiV1TemplatesGet200ResponseData
from imzala_client.models.api_v1_templates_get401_response import ApiV1TemplatesGet401Response as ApiV1TemplatesGet401Response
from imzala_client.models.api_v1_templates_id_delete200_response import ApiV1TemplatesIdDelete200Response as ApiV1TemplatesIdDelete200Response
from imzala_client.models.api_v1_templates_id_delete200_response_data import ApiV1TemplatesIdDelete200ResponseData as ApiV1TemplatesIdDelete200ResponseData
from imzala_client.models.api_v1_templates_id_get200_response import ApiV1TemplatesIdGet200Response as ApiV1TemplatesIdGet200Response
from imzala_client.models.api_v1_templates_id_get404_response import ApiV1TemplatesIdGet404Response as ApiV1TemplatesIdGet404Response
from imzala_client.models.api_v1_templates_id_patch200_response import ApiV1TemplatesIdPatch200Response as ApiV1TemplatesIdPatch200Response
from imzala_client.models.api_v1_templates_id_patch200_response_data import ApiV1TemplatesIdPatch200ResponseData as ApiV1TemplatesIdPatch200ResponseData
from imzala_client.models.api_v1_templates_id_patch_request import ApiV1TemplatesIdPatchRequest as ApiV1TemplatesIdPatchRequest
from imzala_client.models.api_v1_templates_id_usage_get200_response import ApiV1TemplatesIdUsageGet200Response as ApiV1TemplatesIdUsageGet200Response
from imzala_client.models.api_v1_timestamps_get200_response import ApiV1TimestampsGet200Response as ApiV1TimestampsGet200Response
from imzala_client.models.api_v1_timestamps_get200_response_data import ApiV1TimestampsGet200ResponseData as ApiV1TimestampsGet200ResponseData
from imzala_client.models.api_v1_timestamps_id_get200_response import ApiV1TimestampsIdGet200Response as ApiV1TimestampsIdGet200Response
from imzala_client.models.api_v1_timestamps_post201_response import ApiV1TimestampsPost201Response as ApiV1TimestampsPost201Response
from imzala_client.models.api_v1_timestamps_post_request1 import ApiV1TimestampsPostRequest1 as ApiV1TimestampsPostRequest1
from imzala_client.models.coded_error import CodedError as CodedError
from imzala_client.models.contact_summary import ContactSummary as ContactSummary
from imzala_client.models.contact_summary_company import ContactSummaryCompany as ContactSummaryCompany
from imzala_client.models.create_demand_request import CreateDemandRequest as CreateDemandRequest
from imzala_client.models.created_demand import CreatedDemand as CreatedDemand
from imzala_client.models.created_demand_signing_urls_inner import CreatedDemandSigningUrlsInner as CreatedDemandSigningUrlsInner
from imzala_client.models.created_demand_stamps_applied_inner import CreatedDemandStampsAppliedInner as CreatedDemandStampsAppliedInner
from imzala_client.models.created_demand_upload import CreatedDemandUpload as CreatedDemandUpload
from imzala_client.models.created_demand_upload_dispatch import CreatedDemandUploadDispatch as CreatedDemandUploadDispatch
from imzala_client.models.created_demand_upload_dispatch_results_inner import CreatedDemandUploadDispatchResultsInner as CreatedDemandUploadDispatchResultsInner
from imzala_client.models.created_demand_upload_field_layout import CreatedDemandUploadFieldLayout as CreatedDemandUploadFieldLayout
from imzala_client.models.demand_document_status import DemandDocumentStatus as DemandDocumentStatus
from imzala_client.models.demand_page import DemandPage as DemandPage
from imzala_client.models.demand_status import DemandStatus as DemandStatus
from imzala_client.models.demand_status_parties_inner import DemandStatusPartiesInner as DemandStatusPartiesInner
from imzala_client.models.demand_status_stamp_items_inner import DemandStatusStampItemsInner as DemandStatusStampItemsInner
from imzala_client.models.document_party_decision import DocumentPartyDecision as DocumentPartyDecision
from imzala_client.models.document_progress import DocumentProgress as DocumentProgress
from imzala_client.models.document_scoped_slugs import DocumentScopedSlugs as DocumentScopedSlugs
from imzala_client.models.document_sealing import DocumentSealing as DocumentSealing
from imzala_client.models.document_selection_error import DocumentSelectionError as DocumentSelectionError
from imzala_client.models.document_selection_error_details import DocumentSelectionErrorDetails as DocumentSelectionErrorDetails
from imzala_client.models.document_selection_input import DocumentSelectionInput as DocumentSelectionInput
from imzala_client.models.envelope_document import EnvelopeDocument as EnvelopeDocument
from imzala_client.models.field_layout_diagnostic import FieldLayoutDiagnostic as FieldLayoutDiagnostic
from imzala_client.models.field_layout_preview import FieldLayoutPreview as FieldLayoutPreview
from imzala_client.models.field_layout_preview_placements_summary import FieldLayoutPreviewPlacementsSummary as FieldLayoutPreviewPlacementsSummary
from imzala_client.models.field_layout_preview_placements_summary_by_page_inner import FieldLayoutPreviewPlacementsSummaryByPageInner as FieldLayoutPreviewPlacementsSummaryByPageInner
from imzala_client.models.field_layout_unresolved import FieldLayoutUnresolved as FieldLayoutUnresolved
from imzala_client.models.field_layout_warning import FieldLayoutWarning as FieldLayoutWarning
from imzala_client.models.field_template_detail import FieldTemplateDetail as FieldTemplateDetail
from imzala_client.models.field_template_detail_parties_inner import FieldTemplateDetailPartiesInner as FieldTemplateDetailPartiesInner
from imzala_client.models.field_template_list_item import FieldTemplateListItem as FieldTemplateListItem
from imzala_client.models.field_template_party import FieldTemplateParty as FieldTemplateParty
from imzala_client.models.kyc_webhook_envelope import KycWebhookEnvelope as KycWebhookEnvelope
from imzala_client.models.page_item import PageItem as PageItem
from imzala_client.models.party_mapping_input import PartyMappingInput as PartyMappingInput
from imzala_client.models.party_mapping_input_variables_value import PartyMappingInputVariablesValue as PartyMappingInputVariablesValue
from imzala_client.models.patch_stamp_item_request import PatchStampItemRequest as PatchStampItemRequest
from imzala_client.models.patch_stamp_item_response import PatchStampItemResponse as PatchStampItemResponse
from imzala_client.models.patch_stamp_item_response_data import PatchStampItemResponseData as PatchStampItemResponseData
from imzala_client.models.reminder_settings import ReminderSettings as ReminderSettings
from imzala_client.models.stamp_data import StampData as StampData
from imzala_client.models.standard_error import StandardError as StandardError
from imzala_client.models.standard_error_error import StandardErrorError as StandardErrorError
from imzala_client.models.template_detail import TemplateDetail as TemplateDetail
from imzala_client.models.template_document_summary import TemplateDocumentSummary as TemplateDocumentSummary
from imzala_client.models.template_party_summary import TemplatePartySummary as TemplatePartySummary
from imzala_client.models.template_summary import TemplateSummary as TemplateSummary
from imzala_client.models.template_summary_parties_inner import TemplateSummaryPartiesInner as TemplateSummaryPartiesInner
from imzala_client.models.template_usage import TemplateUsage as TemplateUsage
from imzala_client.models.template_usage_documents_inner import TemplateUsageDocumentsInner as TemplateUsageDocumentsInner
from imzala_client.models.template_usage_endpoint import TemplateUsageEndpoint as TemplateUsageEndpoint
from imzala_client.models.template_usage_example_request import TemplateUsageExampleRequest as TemplateUsageExampleRequest
from imzala_client.models.template_usage_form_fields_inner import TemplateUsageFormFieldsInner as TemplateUsageFormFieldsInner
from imzala_client.models.template_usage_parties_inner import TemplateUsagePartiesInner as TemplateUsagePartiesInner
from imzala_client.models.template_usage_parties_inner_supported_fields_inner import TemplateUsagePartiesInnerSupportedFieldsInner as TemplateUsagePartiesInnerSupportedFieldsInner
from imzala_client.models.template_usage_variables_inner import TemplateUsageVariablesInner as TemplateUsageVariablesInner
from imzala_client.models.template_variable import TemplateVariable as TemplateVariable
from imzala_client.models.timestamp_list_item import TimestampListItem as TimestampListItem
from imzala_client.models.timestamp_record import TimestampRecord as TimestampRecord
from imzala_client.models.trigger_reminder_request import TriggerReminderRequest as TriggerReminderRequest
from imzala_client.models.upsert_items_request import UpsertItemsRequest as UpsertItemsRequest
from imzala_client.models.upsert_items_response import UpsertItemsResponse as UpsertItemsResponse
from imzala_client.models.upsert_items_response_data import UpsertItemsResponseData as UpsertItemsResponseData
from imzala_client.models.upsert_items_response_data_items_inner import UpsertItemsResponseDataItemsInner as UpsertItemsResponseDataItemsInner
from imzala_client.models.webhook_data_demand_completed import WebhookDataDemandCompleted as WebhookDataDemandCompleted
from imzala_client.models.webhook_data_demand_completed_parties_inner import WebhookDataDemandCompletedPartiesInner as WebhookDataDemandCompletedPartiesInner
from imzala_client.models.webhook_data_demand_created import WebhookDataDemandCreated as WebhookDataDemandCreated
from imzala_client.models.webhook_data_demand_expired import WebhookDataDemandExpired as WebhookDataDemandExpired
from imzala_client.models.webhook_data_demand_expired_parties_inner import WebhookDataDemandExpiredPartiesInner as WebhookDataDemandExpiredPartiesInner
from imzala_client.models.webhook_data_kyc_terminal import WebhookDataKycTerminal as WebhookDataKycTerminal
from imzala_client.models.webhook_data_kyc_terminal_steps_inner import WebhookDataKycTerminalStepsInner as WebhookDataKycTerminalStepsInner
from imzala_client.models.webhook_data_party_rejected import WebhookDataPartyRejected as WebhookDataPartyRejected
from imzala_client.models.webhook_data_party_rejected_party import WebhookDataPartyRejectedParty as WebhookDataPartyRejectedParty
from imzala_client.models.webhook_data_party_signed import WebhookDataPartySigned as WebhookDataPartySigned
from imzala_client.models.webhook_data_party_viewed import WebhookDataPartyViewed as WebhookDataPartyViewed
from imzala_client.models.webhook_envelope import WebhookEnvelope as WebhookEnvelope

