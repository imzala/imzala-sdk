# coding: utf-8

# flake8: noqa

"""
    imzala External API

    imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.6.0 · **Son güncelleme:** 2026-06-30  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API key kullanıyorsanız `X-Workspace-Id` header'ı göndermeniz gerekir (organizasyon UUID'si). Kişisel anahtarlar için bu header gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - 60 istek/dakika per API key - Aşılırsa 429 döner  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (6) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti |  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type` — yukarıdaki 6 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 

    The version of the OpenAPI document: 1.6.0
    Contact: destek@imzala.org
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


__version__ = "1.0.0"

# Define package exports
__all__ = [
    "AccountApi",
    "DemandsApi",
    "RemindersApi",
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
    "ApiV1DemandsIdEmbedSessionPost200Response",
    "ApiV1DemandsIdEmbedSessionPost200ResponseData",
    "ApiV1DemandsIdEmbedSessionPostRequest",
    "ApiV1DemandsIdGet200Response",
    "ApiV1DemandsIdRemindersPost200Response",
    "ApiV1DemandsIdRemindersPost200ResponseData",
    "ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner",
    "ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner",
    "ApiV1DemandsIdRemindersPost429Response",
    "ApiV1DemandsIdRemindersPost429ResponseError",
    "ApiV1DemandsPost201Response",
    "ApiV1DemandsUploadPost201Response",
    "ApiV1MeGet200Response",
    "ApiV1MeGet200ResponseData",
    "ApiV1MeGet200ResponseDataCredits",
    "ApiV1MeGet200ResponseDataWorkspace",
    "ApiV1TemplatesGet200Response",
    "ApiV1TemplatesGet200ResponseData",
    "ApiV1TemplatesGet401Response",
    "ApiV1TemplatesIdGet200Response",
    "ApiV1TemplatesIdGet404Response",
    "ApiV1TemplatesIdUsageGet200Response",
    "ApiV1TimestampsPost201Response",
    "ApiV1TimestampsPostRequest1",
    "CreateDemandRequest",
    "CreatedDemand",
    "CreatedDemandSigningUrlsInner",
    "CreatedDemandUpload",
    "DemandPage",
    "DemandStatus",
    "DemandStatusPartiesInner",
    "PageItem",
    "PartyMappingInput",
    "PartyMappingInputVariablesValue",
    "ReminderSettings",
    "StandardError",
    "StandardErrorError",
    "TemplateDetail",
    "TemplatePartySummary",
    "TemplateSummary",
    "TemplateSummaryPartiesInner",
    "TemplateUsage",
    "TemplateUsageEndpoint",
    "TemplateUsageExampleRequest",
    "TemplateUsagePartiesInner",
    "TemplateUsagePartiesInnerSupportedFieldsInner",
    "TemplateUsageTemplate",
    "TemplateUsageVariablesInner",
    "TemplateVariable",
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
    "WebhookDataPartyRejected",
    "WebhookDataPartyRejectedParty",
    "WebhookDataPartySigned",
    "WebhookDataPartyViewed",
    "WebhookEnvelope",
]

# import apis into sdk package
from imzala_client.api.account_api import AccountApi as AccountApi
from imzala_client.api.demands_api import DemandsApi as DemandsApi
from imzala_client.api.reminders_api import RemindersApi as RemindersApi
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
from imzala_client.models.api_v1_demands_id_embed_session_post200_response import ApiV1DemandsIdEmbedSessionPost200Response as ApiV1DemandsIdEmbedSessionPost200Response
from imzala_client.models.api_v1_demands_id_embed_session_post200_response_data import ApiV1DemandsIdEmbedSessionPost200ResponseData as ApiV1DemandsIdEmbedSessionPost200ResponseData
from imzala_client.models.api_v1_demands_id_embed_session_post_request import ApiV1DemandsIdEmbedSessionPostRequest as ApiV1DemandsIdEmbedSessionPostRequest
from imzala_client.models.api_v1_demands_id_get200_response import ApiV1DemandsIdGet200Response as ApiV1DemandsIdGet200Response
from imzala_client.models.api_v1_demands_id_reminders_post200_response import ApiV1DemandsIdRemindersPost200Response as ApiV1DemandsIdRemindersPost200Response
from imzala_client.models.api_v1_demands_id_reminders_post200_response_data import ApiV1DemandsIdRemindersPost200ResponseData as ApiV1DemandsIdRemindersPost200ResponseData
from imzala_client.models.api_v1_demands_id_reminders_post200_response_data_dispatched_inner import ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner as ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner
from imzala_client.models.api_v1_demands_id_reminders_post200_response_data_skipped_inner import ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner as ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner
from imzala_client.models.api_v1_demands_id_reminders_post429_response import ApiV1DemandsIdRemindersPost429Response as ApiV1DemandsIdRemindersPost429Response
from imzala_client.models.api_v1_demands_id_reminders_post429_response_error import ApiV1DemandsIdRemindersPost429ResponseError as ApiV1DemandsIdRemindersPost429ResponseError
from imzala_client.models.api_v1_demands_post201_response import ApiV1DemandsPost201Response as ApiV1DemandsPost201Response
from imzala_client.models.api_v1_demands_upload_post201_response import ApiV1DemandsUploadPost201Response as ApiV1DemandsUploadPost201Response
from imzala_client.models.api_v1_me_get200_response import ApiV1MeGet200Response as ApiV1MeGet200Response
from imzala_client.models.api_v1_me_get200_response_data import ApiV1MeGet200ResponseData as ApiV1MeGet200ResponseData
from imzala_client.models.api_v1_me_get200_response_data_credits import ApiV1MeGet200ResponseDataCredits as ApiV1MeGet200ResponseDataCredits
from imzala_client.models.api_v1_me_get200_response_data_workspace import ApiV1MeGet200ResponseDataWorkspace as ApiV1MeGet200ResponseDataWorkspace
from imzala_client.models.api_v1_templates_get200_response import ApiV1TemplatesGet200Response as ApiV1TemplatesGet200Response
from imzala_client.models.api_v1_templates_get200_response_data import ApiV1TemplatesGet200ResponseData as ApiV1TemplatesGet200ResponseData
from imzala_client.models.api_v1_templates_get401_response import ApiV1TemplatesGet401Response as ApiV1TemplatesGet401Response
from imzala_client.models.api_v1_templates_id_get200_response import ApiV1TemplatesIdGet200Response as ApiV1TemplatesIdGet200Response
from imzala_client.models.api_v1_templates_id_get404_response import ApiV1TemplatesIdGet404Response as ApiV1TemplatesIdGet404Response
from imzala_client.models.api_v1_templates_id_usage_get200_response import ApiV1TemplatesIdUsageGet200Response as ApiV1TemplatesIdUsageGet200Response
from imzala_client.models.api_v1_timestamps_post201_response import ApiV1TimestampsPost201Response as ApiV1TimestampsPost201Response
from imzala_client.models.api_v1_timestamps_post_request1 import ApiV1TimestampsPostRequest1 as ApiV1TimestampsPostRequest1
from imzala_client.models.create_demand_request import CreateDemandRequest as CreateDemandRequest
from imzala_client.models.created_demand import CreatedDemand as CreatedDemand
from imzala_client.models.created_demand_signing_urls_inner import CreatedDemandSigningUrlsInner as CreatedDemandSigningUrlsInner
from imzala_client.models.created_demand_upload import CreatedDemandUpload as CreatedDemandUpload
from imzala_client.models.demand_page import DemandPage as DemandPage
from imzala_client.models.demand_status import DemandStatus as DemandStatus
from imzala_client.models.demand_status_parties_inner import DemandStatusPartiesInner as DemandStatusPartiesInner
from imzala_client.models.page_item import PageItem as PageItem
from imzala_client.models.party_mapping_input import PartyMappingInput as PartyMappingInput
from imzala_client.models.party_mapping_input_variables_value import PartyMappingInputVariablesValue as PartyMappingInputVariablesValue
from imzala_client.models.reminder_settings import ReminderSettings as ReminderSettings
from imzala_client.models.standard_error import StandardError as StandardError
from imzala_client.models.standard_error_error import StandardErrorError as StandardErrorError
from imzala_client.models.template_detail import TemplateDetail as TemplateDetail
from imzala_client.models.template_party_summary import TemplatePartySummary as TemplatePartySummary
from imzala_client.models.template_summary import TemplateSummary as TemplateSummary
from imzala_client.models.template_summary_parties_inner import TemplateSummaryPartiesInner as TemplateSummaryPartiesInner
from imzala_client.models.template_usage import TemplateUsage as TemplateUsage
from imzala_client.models.template_usage_endpoint import TemplateUsageEndpoint as TemplateUsageEndpoint
from imzala_client.models.template_usage_example_request import TemplateUsageExampleRequest as TemplateUsageExampleRequest
from imzala_client.models.template_usage_parties_inner import TemplateUsagePartiesInner as TemplateUsagePartiesInner
from imzala_client.models.template_usage_parties_inner_supported_fields_inner import TemplateUsagePartiesInnerSupportedFieldsInner as TemplateUsagePartiesInnerSupportedFieldsInner
from imzala_client.models.template_usage_template import TemplateUsageTemplate as TemplateUsageTemplate
from imzala_client.models.template_usage_variables_inner import TemplateUsageVariablesInner as TemplateUsageVariablesInner
from imzala_client.models.template_variable import TemplateVariable as TemplateVariable
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
from imzala_client.models.webhook_data_party_rejected import WebhookDataPartyRejected as WebhookDataPartyRejected
from imzala_client.models.webhook_data_party_rejected_party import WebhookDataPartyRejectedParty as WebhookDataPartyRejectedParty
from imzala_client.models.webhook_data_party_signed import WebhookDataPartySigned as WebhookDataPartySigned
from imzala_client.models.webhook_data_party_viewed import WebhookDataPartyViewed as WebhookDataPartyViewed
from imzala_client.models.webhook_envelope import WebhookEnvelope as WebhookEnvelope

