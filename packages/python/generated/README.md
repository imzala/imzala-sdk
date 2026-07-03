# imzala-client
imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.

**Sürüm:** 1.6.0 · **Son güncelleme:** 2026-06-30

## Auth
Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden
oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer)
veya **Hesap Ayarları -> API Anahtarları**.

## Workspace (organizasyon)
Organizasyon içinde oluşturulmuş bir API key kullanıyorsanız `X-Workspace-Id`
header'ı göndermeniz gerekir (organizasyon UUID'si). Kişisel anahtarlar için
bu header gerekmez.

## Multi-Party Variables (parti-bazlı ve ortak field'lar)
`POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:

- `party_mapping[i].variables` — **bu partiye ait** field'lar
  (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları)
- `variables` (root) — **partilerden bağımsız** field'lar
  (örn. `kira_baslangic_tarihi`, `kira_bedeli`)

Resolution sırası:
1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula
2. Yoksa root `variables`'tan ara → varsa uygula
3. Yoksa atla

Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in
hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage`
endpoint'i aynı bilgiyi JSON olarak döner.

## Sessiz Başarısızlık Yok
`POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz
ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası
yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.

## Rate Limit
- 60 istek/dakika per API key
- Aşılırsa 429 döner

## Hatalar
Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki),
404 (yok), 429 (rate limit), 500 (sunucu)

## Loglar
Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında
görünür (request body, response body, headers, status code, süre).
30 gün retention.

## Hatırlatma Sistemi
İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:

**1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**

Şablon (Template) seviyesinde `reminder_settings` (interval saatleri,
max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken
bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır
ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.

- Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`
  → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı**
- Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`
  alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu
  ezer, sadece bu demand'a uygulanır)
- Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`

**2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**

`POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması
gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika
geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.

**Kişi başına sert sınırlar (override edilemez):**
- Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +
  manuel trigger toplam).
- Bir kişiye en fazla 3 e-posta reminder gönderilebilir.
- Sınıra ulaşan kişi response'un `details[]` listesinde
  `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya
  `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder.
- `force: true` bu kişi-başı sınırları override etmez.

```bash
# Default — SMS + e-posta birlikte (parti eligibility'sine göre)
curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\
  -H \"X-API-Key: imz_...\" \\
  -H \"Content-Type: application/json\" -d '{}'

# Sadece SMS, anti-spam override
curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\
  -H \"X-API-Key: imz_...\" \\
  -H \"Content-Type: application/json\" \\
  -d '{\"channels\": [\"sms\"], \"force\": true}'
```

Detay için **Reminders** tag'i altındaki endpoint'e bakın.

## Webhooks
imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin
belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar
dashboard'dan yönetilir: **Ayarlar -> Webhook'lar**
(https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.

### Workspace kapsamı
- **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o
  organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir
- **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi
  event'lerinizde tetiklenir

### Olay tipleri (6)
| Olay | Tetikleyici |
|------|-------------|
| `demand.created` | Yeni sözleşme oluşturuldu |
| `demand.completed` | Tüm taraflar imzaladı |
| `demand.expired` | Sözleşme süresi doldu |
| `party.signed` | Bir taraf imzaladı |
| `party.viewed` | Bir taraf imza sayfasını ilk kez açtı |
| `party.rejected` | Bir taraf reddetti |

### Header'lar
Her istekte aşağıdaki header'lar gönderilir:

```
Content-Type: application/json
User-Agent: Imzala-Webhook/1.0
X-Imzala-Event: <olay tipi, örn. demand.completed>
X-Imzala-Delivery: <delivery UUID — idempotency key>
X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex>
```

### Payload zarfı
Tüm olaylar aynı zarfı kullanır:

```json
{
  \"id\": \"evt_abc123...\",
  \"type\": \"demand.completed\",
  \"created_at\": \"2026-05-07T08:30:00.000Z\",
  \"data\": { \"...olay-özel alanlar...\" }
}
```

- `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key).
- `type` — yukarıdaki 6 olay tipinden biri (lowercase).
- `created_at` — olay zamanı (ISO 8601 UTC).
- `data` — her olaya özel (aşağıda her olay için ayrı şema).

### İmza doğrulama (HMAC-SHA256)
Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir
secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.

Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve
`X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama
Node.js örneği:

```js
const crypto = require('crypto');

function verify(rawBody, header, secret) {
  const expected = 'sha256=' + crypto
    .createHmac('sha256', secret)
    .update(rawBody, 'utf8')
    .digest('hex');
  return crypto.timingSafeEqual(
    Buffer.from(header || '', 'utf8'),
    Buffer.from(expected, 'utf8')
  );
}

// Express
app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {
  const sig = req.header('X-Imzala-Signature-256');
  if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {
    return res.status(401).send('invalid signature');
  }
  const event = JSON.parse(req.body.toString('utf8'));
  // ... event'i kuyruğa koy ve hemen 2xx dön
  res.status(200).send('ok');
});
```

> **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu
> framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.

### Yeniden deneme politikası
- **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir.
- **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir.
- **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`).
- **Maksimum deneme:** 6 (ilk + 5 retry).
- **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa.
- **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel
  \"Tekrar Gönder\" mümkün.

Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri
(DB yazma, e-posta vs.) async kuyruğa atın.

### Idempotency
Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver,
backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu
DB'de tek seferlik kayıt için kullanın:

```sql
CREATE TABLE imzala_webhook_seen (
  event_id TEXT PRIMARY KEY,
  received_at TIMESTAMPTZ DEFAULT now()
);
-- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip
```

### Backfill flag
Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra
kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true`
bayrağı bulunur. Bu durumda receiver:

- Loglama için kayıt edebilir
- Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı**
- `id` zaten görülmüşse normal flow'a devam edebilir

```js
if (event.data._backfill === true) {
  await logReplay(event);
  return res.status(200).send('replay accepted');
}
```

### Manuel yeniden gönderim
Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:

- Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için)
- Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir)
- Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)

### En iyi pratikler
1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency).
2. İmzayı **timing-safe compare** ile doğrula (string equality değil).
3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at.
4. `_backfill: true` payload'larda side-effect'leri atla.
5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle
   eşleşmesini kolaylaştırır.
6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme.


This Python package is automatically generated by the [OpenAPI Generator](https://openapi-generator.tech) project:

- API version: 1.7.0
- Package version: 1.0.0
- Generator version: 7.23.0
- Build package: org.openapitools.codegen.languages.PythonClientCodegen
For more information, please visit [https://imzala.org](https://imzala.org)

## Requirements.

Python 3.10+

## Installation & Usage
### pip install

If the python package is hosted on a repository, you can install directly using:

```sh
pip install git+https://github.com/GIT_USER_ID/GIT_REPO_ID.git
```
(you may need to run `pip` with root permission: `sudo pip install git+https://github.com/GIT_USER_ID/GIT_REPO_ID.git`)

Then import the package:
```python
import imzala_client
```

### Setuptools

Install via [Setuptools](http://pypi.python.org/pypi/setuptools).

```sh
python setup.py install --user
```
(or `sudo python setup.py install` to install the package for all users)

Then import the package:
```python
import imzala_client
```

### Tests

Execute `pytest` to run the tests.

## Getting Started

Please follow the [installation procedure](#installation--usage) and then run the following:

```python

import imzala_client
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'


# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.AccountApi(api_client)

    try:
        # API key sahibi bilgisi
        api_response = api_instance.api_v1_me_get()
        print("The response of AccountApi->api_v1_me_get:\n")
        pprint(api_response)
    except ApiException as e:
        print("Exception when calling AccountApi->api_v1_me_get: %s\n" % e)

```

## Documentation for API Endpoints

All URIs are relative to *https://api-prd.imzala.org*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AccountApi* | [**api_v1_me_get**](docs/AccountApi.md#api_v1_me_get) | **GET** /api/v1/me | API key sahibi bilgisi
*DemandsApi* | [**api_v1_demands_get**](docs/DemandsApi.md#api_v1_demands_get) | **GET** /api/v1/demands | Sözleşme listesi (counts-only, PII&#39;siz)
*DemandsApi* | [**api_v1_demands_id_cancel_post**](docs/DemandsApi.md#api_v1_demands_id_cancel_post) | **POST** /api/v1/demands/{id}/cancel | Sözleşme iptal (void)
*DemandsApi* | [**api_v1_demands_id_certificate_get**](docs/DemandsApi.md#api_v1_demands_id_certificate_get) | **GET** /api/v1/demands/{id}/certificate | Tamamlanma sertifikası (PAdES B-T)
*DemandsApi* | [**api_v1_demands_id_delete**](docs/DemandsApi.md#api_v1_demands_id_delete) | **DELETE** /api/v1/demands/{id} | Sözleşme sil (yalnızca tamamlanmamış)
*DemandsApi* | [**api_v1_demands_id_embed_session_post**](docs/DemandsApi.md#api_v1_demands_id_embed_session_post) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint)
*DemandsApi* | [**api_v1_demands_id_get**](docs/DemandsApi.md#api_v1_demands_id_get) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi
*DemandsApi* | [**api_v1_demands_id_items_post**](docs/DemandsApi.md#api_v1_demands_id_items_post) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace)
*DemandsApi* | [**api_v1_demands_id_parties_party_id_resend_post**](docs/DemandsApi.md#api_v1_demands_id_parties_party_id_resend_post) | **POST** /api/v1/demands/{id}/parties/{partyId}/resend | Tekil tarafa imza davetini tekrar gönder
*DemandsApi* | [**api_v1_demands_id_pdf_get**](docs/DemandsApi.md#api_v1_demands_id_pdf_get) | **GET** /api/v1/demands/{id}/pdf | İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
*DemandsApi* | [**api_v1_demands_id_timeline_get**](docs/DemandsApi.md#api_v1_demands_id_timeline_get) | **GET** /api/v1/demands/{id}/timeline | İmza denetim izi (maskeli)
*DemandsApi* | [**api_v1_demands_post**](docs/DemandsApi.md#api_v1_demands_post) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan)
*DemandsApi* | [**api_v1_demands_upload_post**](docs/DemandsApi.md#api_v1_demands_upload_post) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz)
*RemindersApi* | [**api_v1_demands_id_reminders_post**](docs/RemindersApi.md#api_v1_demands_id_reminders_post) | **POST** /api/v1/demands/{id}/reminders | Anlık hatırlatma tetikle (imzalanmamış taraflara)
*TemplatesApi* | [**api_v1_templates_get**](docs/TemplatesApi.md#api_v1_templates_get) | **GET** /api/v1/templates | Şablon listesi
*TemplatesApi* | [**api_v1_templates_id_delete**](docs/TemplatesApi.md#api_v1_templates_id_delete) | **DELETE** /api/v1/templates/{id} | Şablon sil
*TemplatesApi* | [**api_v1_templates_id_get**](docs/TemplatesApi.md#api_v1_templates_id_get) | **GET** /api/v1/templates/{id} | Şablon detay
*TemplatesApi* | [**api_v1_templates_id_patch**](docs/TemplatesApi.md#api_v1_templates_id_patch) | **PATCH** /api/v1/templates/{id} | Şablon metadata güncelle
*TemplatesApi* | [**api_v1_templates_id_usage_get**](docs/TemplatesApi.md#api_v1_templates_id_usage_get) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek)
*TimestampsApi* | [**api_v1_timestamps_post**](docs/TimestampsApi.md#api_v1_timestamps_post) | **POST** /api/v1/timestamps | Zaman damgası oluştur (eser tescil)


## Documentation For Models

 - [ApiError](docs/ApiError.md)
 - [ApiV1DemandsGet200Response](docs/ApiV1DemandsGet200Response.md)
 - [ApiV1DemandsGet200ResponseData](docs/ApiV1DemandsGet200ResponseData.md)
 - [ApiV1DemandsGet200ResponseDataDemandsInner](docs/ApiV1DemandsGet200ResponseDataDemandsInner.md)
 - [ApiV1DemandsIdCancelPost200Response](docs/ApiV1DemandsIdCancelPost200Response.md)
 - [ApiV1DemandsIdCancelPost200ResponseData](docs/ApiV1DemandsIdCancelPost200ResponseData.md)
 - [ApiV1DemandsIdCancelPostRequest](docs/ApiV1DemandsIdCancelPostRequest.md)
 - [ApiV1DemandsIdDelete409Response](docs/ApiV1DemandsIdDelete409Response.md)
 - [ApiV1DemandsIdEmbedSessionPost200Response](docs/ApiV1DemandsIdEmbedSessionPost200Response.md)
 - [ApiV1DemandsIdEmbedSessionPost200ResponseData](docs/ApiV1DemandsIdEmbedSessionPost200ResponseData.md)
 - [ApiV1DemandsIdEmbedSessionPostRequest](docs/ApiV1DemandsIdEmbedSessionPostRequest.md)
 - [ApiV1DemandsIdGet200Response](docs/ApiV1DemandsIdGet200Response.md)
 - [ApiV1DemandsIdPartiesPartyIdResendPost200Response](docs/ApiV1DemandsIdPartiesPartyIdResendPost200Response.md)
 - [ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData](docs/ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData.md)
 - [ApiV1DemandsIdRemindersPost200Response](docs/ApiV1DemandsIdRemindersPost200Response.md)
 - [ApiV1DemandsIdRemindersPost200ResponseData](docs/ApiV1DemandsIdRemindersPost200ResponseData.md)
 - [ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner](docs/ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner.md)
 - [ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner](docs/ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner.md)
 - [ApiV1DemandsIdRemindersPost429Response](docs/ApiV1DemandsIdRemindersPost429Response.md)
 - [ApiV1DemandsIdRemindersPost429ResponseError](docs/ApiV1DemandsIdRemindersPost429ResponseError.md)
 - [ApiV1DemandsIdTimelineGet200Response](docs/ApiV1DemandsIdTimelineGet200Response.md)
 - [ApiV1DemandsIdTimelineGet200ResponseData](docs/ApiV1DemandsIdTimelineGet200ResponseData.md)
 - [ApiV1DemandsIdTimelineGet200ResponseDataEventsInner](docs/ApiV1DemandsIdTimelineGet200ResponseDataEventsInner.md)
 - [ApiV1DemandsPost201Response](docs/ApiV1DemandsPost201Response.md)
 - [ApiV1DemandsUploadPost201Response](docs/ApiV1DemandsUploadPost201Response.md)
 - [ApiV1MeGet200Response](docs/ApiV1MeGet200Response.md)
 - [ApiV1MeGet200ResponseData](docs/ApiV1MeGet200ResponseData.md)
 - [ApiV1MeGet200ResponseDataCredits](docs/ApiV1MeGet200ResponseDataCredits.md)
 - [ApiV1MeGet200ResponseDataWorkspace](docs/ApiV1MeGet200ResponseDataWorkspace.md)
 - [ApiV1TemplatesGet200Response](docs/ApiV1TemplatesGet200Response.md)
 - [ApiV1TemplatesGet200ResponseData](docs/ApiV1TemplatesGet200ResponseData.md)
 - [ApiV1TemplatesGet401Response](docs/ApiV1TemplatesGet401Response.md)
 - [ApiV1TemplatesIdDelete200Response](docs/ApiV1TemplatesIdDelete200Response.md)
 - [ApiV1TemplatesIdDelete200ResponseData](docs/ApiV1TemplatesIdDelete200ResponseData.md)
 - [ApiV1TemplatesIdGet200Response](docs/ApiV1TemplatesIdGet200Response.md)
 - [ApiV1TemplatesIdGet404Response](docs/ApiV1TemplatesIdGet404Response.md)
 - [ApiV1TemplatesIdPatch200Response](docs/ApiV1TemplatesIdPatch200Response.md)
 - [ApiV1TemplatesIdPatch200ResponseData](docs/ApiV1TemplatesIdPatch200ResponseData.md)
 - [ApiV1TemplatesIdPatchRequest](docs/ApiV1TemplatesIdPatchRequest.md)
 - [ApiV1TemplatesIdUsageGet200Response](docs/ApiV1TemplatesIdUsageGet200Response.md)
 - [ApiV1TimestampsPost201Response](docs/ApiV1TimestampsPost201Response.md)
 - [ApiV1TimestampsPostRequest1](docs/ApiV1TimestampsPostRequest1.md)
 - [CreateDemandRequest](docs/CreateDemandRequest.md)
 - [CreatedDemand](docs/CreatedDemand.md)
 - [CreatedDemandSigningUrlsInner](docs/CreatedDemandSigningUrlsInner.md)
 - [CreatedDemandUpload](docs/CreatedDemandUpload.md)
 - [DemandPage](docs/DemandPage.md)
 - [DemandStatus](docs/DemandStatus.md)
 - [DemandStatusPartiesInner](docs/DemandStatusPartiesInner.md)
 - [PageItem](docs/PageItem.md)
 - [PartyMappingInput](docs/PartyMappingInput.md)
 - [PartyMappingInputVariablesValue](docs/PartyMappingInputVariablesValue.md)
 - [ReminderSettings](docs/ReminderSettings.md)
 - [StandardError](docs/StandardError.md)
 - [StandardErrorError](docs/StandardErrorError.md)
 - [TemplateDetail](docs/TemplateDetail.md)
 - [TemplatePartySummary](docs/TemplatePartySummary.md)
 - [TemplateSummary](docs/TemplateSummary.md)
 - [TemplateSummaryPartiesInner](docs/TemplateSummaryPartiesInner.md)
 - [TemplateUsage](docs/TemplateUsage.md)
 - [TemplateUsageEndpoint](docs/TemplateUsageEndpoint.md)
 - [TemplateUsageExampleRequest](docs/TemplateUsageExampleRequest.md)
 - [TemplateUsagePartiesInner](docs/TemplateUsagePartiesInner.md)
 - [TemplateUsagePartiesInnerSupportedFieldsInner](docs/TemplateUsagePartiesInnerSupportedFieldsInner.md)
 - [TemplateUsageVariablesInner](docs/TemplateUsageVariablesInner.md)
 - [TemplateVariable](docs/TemplateVariable.md)
 - [TimestampRecord](docs/TimestampRecord.md)
 - [TriggerReminderRequest](docs/TriggerReminderRequest.md)
 - [UpsertItemsRequest](docs/UpsertItemsRequest.md)
 - [UpsertItemsResponse](docs/UpsertItemsResponse.md)
 - [UpsertItemsResponseData](docs/UpsertItemsResponseData.md)
 - [UpsertItemsResponseDataItemsInner](docs/UpsertItemsResponseDataItemsInner.md)
 - [WebhookDataDemandCompleted](docs/WebhookDataDemandCompleted.md)
 - [WebhookDataDemandCompletedPartiesInner](docs/WebhookDataDemandCompletedPartiesInner.md)
 - [WebhookDataDemandCreated](docs/WebhookDataDemandCreated.md)
 - [WebhookDataDemandExpired](docs/WebhookDataDemandExpired.md)
 - [WebhookDataDemandExpiredPartiesInner](docs/WebhookDataDemandExpiredPartiesInner.md)
 - [WebhookDataPartyRejected](docs/WebhookDataPartyRejected.md)
 - [WebhookDataPartyRejectedParty](docs/WebhookDataPartyRejectedParty.md)
 - [WebhookDataPartySigned](docs/WebhookDataPartySigned.md)
 - [WebhookDataPartyViewed](docs/WebhookDataPartyViewed.md)
 - [WebhookEnvelope](docs/WebhookEnvelope.md)


<a id="documentation-for-authorization"></a>
## Documentation For Authorization


Authentication schemes defined for the API:
<a id="ApiKeyAuth"></a>
### ApiKeyAuth

- **Type**: API key
- **API key parameter name**: X-API-Key
- **Location**: HTTP header


## Author

destek@imzala.org


