# coding: utf-8

# flake8: noqa
"""
    imzala External API

    imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.6.0 · **Son güncelleme:** 2026-06-30  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API key kullanıyorsanız `X-Workspace-Id` header'ı göndermeniz gerekir (organizasyon UUID'si). Kişisel anahtarlar için bu header gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - 60 istek/dakika per API key - Aşılırsa 429 döner  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (6) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti |  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type` — yukarıdaki 6 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 

    The version of the OpenAPI document: 1.7.0
    Contact: destek@imzala.org
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501

# import models into model package
from imzala_client.models.api_error import ApiError
from imzala_client.models.api_v1_demands_get200_response import ApiV1DemandsGet200Response
from imzala_client.models.api_v1_demands_get200_response_data import ApiV1DemandsGet200ResponseData
from imzala_client.models.api_v1_demands_get200_response_data_demands_inner import ApiV1DemandsGet200ResponseDataDemandsInner
from imzala_client.models.api_v1_demands_id_cancel_post200_response import ApiV1DemandsIdCancelPost200Response
from imzala_client.models.api_v1_demands_id_cancel_post200_response_data import ApiV1DemandsIdCancelPost200ResponseData
from imzala_client.models.api_v1_demands_id_cancel_post_request import ApiV1DemandsIdCancelPostRequest
from imzala_client.models.api_v1_demands_id_delete409_response import ApiV1DemandsIdDelete409Response
from imzala_client.models.api_v1_demands_id_embed_session_post200_response import ApiV1DemandsIdEmbedSessionPost200Response
from imzala_client.models.api_v1_demands_id_embed_session_post200_response_data import ApiV1DemandsIdEmbedSessionPost200ResponseData
from imzala_client.models.api_v1_demands_id_embed_session_post_request import ApiV1DemandsIdEmbedSessionPostRequest
from imzala_client.models.api_v1_demands_id_get200_response import ApiV1DemandsIdGet200Response
from imzala_client.models.api_v1_demands_id_parties_party_id_resend_post200_response import ApiV1DemandsIdPartiesPartyIdResendPost200Response
from imzala_client.models.api_v1_demands_id_parties_party_id_resend_post200_response_data import ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData
from imzala_client.models.api_v1_demands_id_reminders_post200_response import ApiV1DemandsIdRemindersPost200Response
from imzala_client.models.api_v1_demands_id_reminders_post200_response_data import ApiV1DemandsIdRemindersPost200ResponseData
from imzala_client.models.api_v1_demands_id_reminders_post200_response_data_dispatched_inner import ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner
from imzala_client.models.api_v1_demands_id_reminders_post200_response_data_skipped_inner import ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner
from imzala_client.models.api_v1_demands_id_reminders_post429_response import ApiV1DemandsIdRemindersPost429Response
from imzala_client.models.api_v1_demands_id_reminders_post429_response_error import ApiV1DemandsIdRemindersPost429ResponseError
from imzala_client.models.api_v1_demands_id_timeline_get200_response import ApiV1DemandsIdTimelineGet200Response
from imzala_client.models.api_v1_demands_id_timeline_get200_response_data import ApiV1DemandsIdTimelineGet200ResponseData
from imzala_client.models.api_v1_demands_id_timeline_get200_response_data_events_inner import ApiV1DemandsIdTimelineGet200ResponseDataEventsInner
from imzala_client.models.api_v1_demands_post201_response import ApiV1DemandsPost201Response
from imzala_client.models.api_v1_demands_upload_post201_response import ApiV1DemandsUploadPost201Response
from imzala_client.models.api_v1_me_get200_response import ApiV1MeGet200Response
from imzala_client.models.api_v1_me_get200_response_data import ApiV1MeGet200ResponseData
from imzala_client.models.api_v1_me_get200_response_data_credits import ApiV1MeGet200ResponseDataCredits
from imzala_client.models.api_v1_me_get200_response_data_workspace import ApiV1MeGet200ResponseDataWorkspace
from imzala_client.models.api_v1_templates_get200_response import ApiV1TemplatesGet200Response
from imzala_client.models.api_v1_templates_get200_response_data import ApiV1TemplatesGet200ResponseData
from imzala_client.models.api_v1_templates_get401_response import ApiV1TemplatesGet401Response
from imzala_client.models.api_v1_templates_id_delete200_response import ApiV1TemplatesIdDelete200Response
from imzala_client.models.api_v1_templates_id_delete200_response_data import ApiV1TemplatesIdDelete200ResponseData
from imzala_client.models.api_v1_templates_id_get200_response import ApiV1TemplatesIdGet200Response
from imzala_client.models.api_v1_templates_id_get404_response import ApiV1TemplatesIdGet404Response
from imzala_client.models.api_v1_templates_id_patch200_response import ApiV1TemplatesIdPatch200Response
from imzala_client.models.api_v1_templates_id_patch200_response_data import ApiV1TemplatesIdPatch200ResponseData
from imzala_client.models.api_v1_templates_id_patch_request import ApiV1TemplatesIdPatchRequest
from imzala_client.models.api_v1_templates_id_usage_get200_response import ApiV1TemplatesIdUsageGet200Response
from imzala_client.models.api_v1_timestamps_post201_response import ApiV1TimestampsPost201Response
from imzala_client.models.api_v1_timestamps_post_request1 import ApiV1TimestampsPostRequest1
from imzala_client.models.create_demand_request import CreateDemandRequest
from imzala_client.models.created_demand import CreatedDemand
from imzala_client.models.created_demand_signing_urls_inner import CreatedDemandSigningUrlsInner
from imzala_client.models.created_demand_upload import CreatedDemandUpload
from imzala_client.models.demand_page import DemandPage
from imzala_client.models.demand_status import DemandStatus
from imzala_client.models.demand_status_parties_inner import DemandStatusPartiesInner
from imzala_client.models.page_item import PageItem
from imzala_client.models.party_mapping_input import PartyMappingInput
from imzala_client.models.party_mapping_input_variables_value import PartyMappingInputVariablesValue
from imzala_client.models.reminder_settings import ReminderSettings
from imzala_client.models.standard_error import StandardError
from imzala_client.models.standard_error_error import StandardErrorError
from imzala_client.models.template_detail import TemplateDetail
from imzala_client.models.template_party_summary import TemplatePartySummary
from imzala_client.models.template_summary import TemplateSummary
from imzala_client.models.template_summary_parties_inner import TemplateSummaryPartiesInner
from imzala_client.models.template_usage import TemplateUsage
from imzala_client.models.template_usage_endpoint import TemplateUsageEndpoint
from imzala_client.models.template_usage_example_request import TemplateUsageExampleRequest
from imzala_client.models.template_usage_parties_inner import TemplateUsagePartiesInner
from imzala_client.models.template_usage_parties_inner_supported_fields_inner import TemplateUsagePartiesInnerSupportedFieldsInner
from imzala_client.models.template_usage_variables_inner import TemplateUsageVariablesInner
from imzala_client.models.template_variable import TemplateVariable
from imzala_client.models.timestamp_record import TimestampRecord
from imzala_client.models.trigger_reminder_request import TriggerReminderRequest
from imzala_client.models.upsert_items_request import UpsertItemsRequest
from imzala_client.models.upsert_items_response import UpsertItemsResponse
from imzala_client.models.upsert_items_response_data import UpsertItemsResponseData
from imzala_client.models.upsert_items_response_data_items_inner import UpsertItemsResponseDataItemsInner
from imzala_client.models.webhook_data_demand_completed import WebhookDataDemandCompleted
from imzala_client.models.webhook_data_demand_completed_parties_inner import WebhookDataDemandCompletedPartiesInner
from imzala_client.models.webhook_data_demand_created import WebhookDataDemandCreated
from imzala_client.models.webhook_data_demand_expired import WebhookDataDemandExpired
from imzala_client.models.webhook_data_demand_expired_parties_inner import WebhookDataDemandExpiredPartiesInner
from imzala_client.models.webhook_data_party_rejected import WebhookDataPartyRejected
from imzala_client.models.webhook_data_party_rejected_party import WebhookDataPartyRejectedParty
from imzala_client.models.webhook_data_party_signed import WebhookDataPartySigned
from imzala_client.models.webhook_data_party_viewed import WebhookDataPartyViewed
from imzala_client.models.webhook_envelope import WebhookEnvelope

