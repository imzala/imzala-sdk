# ImzalaApiClient - the C# library for the imzala External API

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
|- -- -- -|- -- -- -- -- -- --|
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
- - INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip
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


This C# SDK is automatically generated by the [OpenAPI Generator](https://openapi-generator.tech) project:

- API version: 1.6.0
- SDK version: 1.0.0
- Generator version: 7.23.0
- Build package: org.openapitools.codegen.languages.CSharpClientCodegen
    For more information, please visit [https://imzala.org](https://imzala.org)

<a id="frameworks-supported"></a>
## Frameworks supported

<a id="dependencies"></a>
## Dependencies

- [Json.NET](https://www.nuget.org/packages/Newtonsoft.Json/) - 13.0.2 or later
- [JsonSubTypes](https://www.nuget.org/packages/JsonSubTypes/) - 1.8.0 or later
- [System.ComponentModel.Annotations](https://www.nuget.org/packages/System.ComponentModel.Annotations) - 5.0.0 or later

The DLLs included in the package may not be the latest version. We recommend using [NuGet](https://docs.nuget.org/consume/installing-nuget) to obtain the latest version of the packages:
```
Install-Package Newtonsoft.Json
Install-Package JsonSubTypes
Install-Package System.ComponentModel.Annotations
```
<a id="installation"></a>
## Installation
Run the following command to generate the DLL
- [Mac/Linux] `/bin/sh build.sh`
- [Windows] `build.bat`

Then include the DLL (under the `bin` folder) in the C# project, and use the namespaces:
```csharp
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;
```
<a id="packaging"></a>
## Packaging

A `.nuspec` is included with the project. You can follow the Nuget quickstart to [create](https://docs.microsoft.com/en-us/nuget/quickstart/create-and-publish-a-package#create-the-package) and [publish](https://docs.microsoft.com/en-us/nuget/quickstart/create-and-publish-a-package#publish-the-package) packages.

This `.nuspec` uses placeholders from the `.csproj`, so build the `.csproj` directly:

```
nuget pack -Build -OutputDirectory out ImzalaApiClient.csproj
```

Then, publish to a [local feed](https://docs.microsoft.com/en-us/nuget/hosting-packages/local-feeds) or [other host](https://docs.microsoft.com/en-us/nuget/hosting-packages/overview) and consume the new package via Nuget as usual.

<a id="usage"></a>
## Usage

To use the API client with a HTTP proxy, setup a `System.Net.WebProxy`
```csharp
Configuration c = new Configuration();
System.Net.WebProxy webProxy = new System.Net.WebProxy("http://myProxyUrl:80/");
webProxy.Credentials = System.Net.CredentialCache.DefaultCredentials;
c.Proxy = webProxy;
```

### Connections
Each ApiClass (properly the ApiClient inside it) will create an instance of HttpClient. It will use that for the entire lifecycle and dispose it when called the Dispose method.

To better manager the connections it's a common practice to reuse the HttpClient and HttpClientHandler (see [here](https://docs.microsoft.com/en-us/dotnet/architecture/microservices/implement-resilient-applications/use-httpclientfactory-to-implement-resilient-http-requests#issues-with-the-original-httpclient-class-available-in-net) for details). To use your own HttpClient instance just pass it to the ApiClass constructor.

```csharp
HttpClientHandler yourHandler = new HttpClientHandler();
HttpClient yourHttpClient = new HttpClient(yourHandler);
var api = new YourApiClass(yourHttpClient, yourHandler);
```

If you want to use an HttpClient and don't have access to the handler, for example in a DI context in Asp.net Core when using IHttpClientFactory.

```csharp
HttpClient yourHttpClient = new HttpClient();
var api = new YourApiClass(yourHttpClient);
```
You'll loose some configuration settings, the features affected are: Setting and Retrieving Cookies, Client Certificates, Proxy settings. You need to either manually handle those in your setup of the HttpClient or they won't be available.

Here an example of DI setup in a sample web project:

```csharp
services.AddHttpClient<YourApiClass>(httpClient =>
   new PetApi(httpClient));
```


<a id="getting-started"></a>
## Getting Started

```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class Example
    {
        public static void Main()
        {

            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.ApiKey.Add("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.ApiKeyPrefix.Add("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new AccountApi(httpClient, config, httpClientHandler);

            try
            {
                // API key sahibi bilgisi
                ApiV1MeGet200Response result = apiInstance.ApiV1MeGet();
                Debug.WriteLine(result);
            }
            catch (ApiException e)
            {
                Debug.Print("Exception when calling AccountApi.ApiV1MeGet: " + e.Message );
                Debug.Print("Status Code: "+ e.ErrorCode);
                Debug.Print(e.StackTrace);
            }

        }
    }
}
```

<a id="documentation-for-api-endpoints"></a>
## Documentation for API Endpoints

All URIs are relative to *https://api-prd.imzala.org*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AccountApi* | [**ApiV1MeGet**](docs/AccountApi.md#apiv1meget) | **GET** /api/v1/me | API key sahibi bilgisi
*DemandsApi* | [**ApiV1DemandsIdEmbedSessionPost**](docs/DemandsApi.md#apiv1demandsidembedsessionpost) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint)
*DemandsApi* | [**ApiV1DemandsIdGet**](docs/DemandsApi.md#apiv1demandsidget) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi
*DemandsApi* | [**ApiV1DemandsIdItemsPost**](docs/DemandsApi.md#apiv1demandsiditemspost) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace)
*DemandsApi* | [**ApiV1DemandsPost**](docs/DemandsApi.md#apiv1demandspost) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan)
*DemandsApi* | [**ApiV1DemandsUploadPost**](docs/DemandsApi.md#apiv1demandsuploadpost) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz)
*RemindersApi* | [**ApiV1DemandsIdRemindersPost**](docs/RemindersApi.md#apiv1demandsidreminderspost) | **POST** /api/v1/demands/{id}/reminders | Anlık hatırlatma tetikle (imzalanmamış taraflara)
*TemplatesApi* | [**ApiV1TemplatesGet**](docs/TemplatesApi.md#apiv1templatesget) | **GET** /api/v1/templates | Şablon listesi
*TemplatesApi* | [**ApiV1TemplatesIdGet**](docs/TemplatesApi.md#apiv1templatesidget) | **GET** /api/v1/templates/{id} | Şablon detay
*TemplatesApi* | [**ApiV1TemplatesIdUsageGet**](docs/TemplatesApi.md#apiv1templatesidusageget) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek)
*TimestampsApi* | [**ApiV1TimestampsPost**](docs/TimestampsApi.md#apiv1timestampspost) | **POST** /api/v1/timestamps | Zaman damgası oluştur (eser tescil)


<a id="documentation-for-models"></a>
## Documentation for Models

 - [Model.ApiError](docs/ApiError.md)
 - [Model.ApiV1DemandsIdEmbedSessionPost200Response](docs/ApiV1DemandsIdEmbedSessionPost200Response.md)
 - [Model.ApiV1DemandsIdEmbedSessionPost200ResponseData](docs/ApiV1DemandsIdEmbedSessionPost200ResponseData.md)
 - [Model.ApiV1DemandsIdEmbedSessionPostRequest](docs/ApiV1DemandsIdEmbedSessionPostRequest.md)
 - [Model.ApiV1DemandsIdGet200Response](docs/ApiV1DemandsIdGet200Response.md)
 - [Model.ApiV1DemandsIdRemindersPost200Response](docs/ApiV1DemandsIdRemindersPost200Response.md)
 - [Model.ApiV1DemandsIdRemindersPost200ResponseData](docs/ApiV1DemandsIdRemindersPost200ResponseData.md)
 - [Model.ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner](docs/ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner.md)
 - [Model.ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner](docs/ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner.md)
 - [Model.ApiV1DemandsIdRemindersPost429Response](docs/ApiV1DemandsIdRemindersPost429Response.md)
 - [Model.ApiV1DemandsIdRemindersPost429ResponseError](docs/ApiV1DemandsIdRemindersPost429ResponseError.md)
 - [Model.ApiV1DemandsPost201Response](docs/ApiV1DemandsPost201Response.md)
 - [Model.ApiV1DemandsUploadPost201Response](docs/ApiV1DemandsUploadPost201Response.md)
 - [Model.ApiV1MeGet200Response](docs/ApiV1MeGet200Response.md)
 - [Model.ApiV1MeGet200ResponseData](docs/ApiV1MeGet200ResponseData.md)
 - [Model.ApiV1MeGet200ResponseDataCredits](docs/ApiV1MeGet200ResponseDataCredits.md)
 - [Model.ApiV1MeGet200ResponseDataWorkspace](docs/ApiV1MeGet200ResponseDataWorkspace.md)
 - [Model.ApiV1TemplatesGet200Response](docs/ApiV1TemplatesGet200Response.md)
 - [Model.ApiV1TemplatesGet200ResponseData](docs/ApiV1TemplatesGet200ResponseData.md)
 - [Model.ApiV1TemplatesGet401Response](docs/ApiV1TemplatesGet401Response.md)
 - [Model.ApiV1TemplatesIdGet200Response](docs/ApiV1TemplatesIdGet200Response.md)
 - [Model.ApiV1TemplatesIdGet404Response](docs/ApiV1TemplatesIdGet404Response.md)
 - [Model.ApiV1TemplatesIdUsageGet200Response](docs/ApiV1TemplatesIdUsageGet200Response.md)
 - [Model.ApiV1TimestampsPost201Response](docs/ApiV1TimestampsPost201Response.md)
 - [Model.ApiV1TimestampsPostRequest1](docs/ApiV1TimestampsPostRequest1.md)
 - [Model.CreateDemandRequest](docs/CreateDemandRequest.md)
 - [Model.CreatedDemand](docs/CreatedDemand.md)
 - [Model.CreatedDemandSigningUrlsInner](docs/CreatedDemandSigningUrlsInner.md)
 - [Model.CreatedDemandUpload](docs/CreatedDemandUpload.md)
 - [Model.DemandPage](docs/DemandPage.md)
 - [Model.DemandStatus](docs/DemandStatus.md)
 - [Model.DemandStatusPartiesInner](docs/DemandStatusPartiesInner.md)
 - [Model.PageItem](docs/PageItem.md)
 - [Model.PartyMappingInput](docs/PartyMappingInput.md)
 - [Model.PartyMappingInputVariablesValue](docs/PartyMappingInputVariablesValue.md)
 - [Model.ReminderSettings](docs/ReminderSettings.md)
 - [Model.StandardError](docs/StandardError.md)
 - [Model.StandardErrorError](docs/StandardErrorError.md)
 - [Model.TemplateDetail](docs/TemplateDetail.md)
 - [Model.TemplatePartySummary](docs/TemplatePartySummary.md)
 - [Model.TemplateSummary](docs/TemplateSummary.md)
 - [Model.TemplateSummaryPartiesInner](docs/TemplateSummaryPartiesInner.md)
 - [Model.TemplateUsage](docs/TemplateUsage.md)
 - [Model.TemplateUsageEndpoint](docs/TemplateUsageEndpoint.md)
 - [Model.TemplateUsageExampleRequest](docs/TemplateUsageExampleRequest.md)
 - [Model.TemplateUsagePartiesInner](docs/TemplateUsagePartiesInner.md)
 - [Model.TemplateUsagePartiesInnerSupportedFieldsInner](docs/TemplateUsagePartiesInnerSupportedFieldsInner.md)
 - [Model.TemplateUsageTemplate](docs/TemplateUsageTemplate.md)
 - [Model.TemplateUsageVariablesInner](docs/TemplateUsageVariablesInner.md)
 - [Model.TemplateVariable](docs/TemplateVariable.md)
 - [Model.TimestampRecord](docs/TimestampRecord.md)
 - [Model.TriggerReminderRequest](docs/TriggerReminderRequest.md)
 - [Model.UpsertItemsRequest](docs/UpsertItemsRequest.md)
 - [Model.UpsertItemsResponse](docs/UpsertItemsResponse.md)
 - [Model.UpsertItemsResponseData](docs/UpsertItemsResponseData.md)
 - [Model.UpsertItemsResponseDataItemsInner](docs/UpsertItemsResponseDataItemsInner.md)
 - [Model.WebhookDataDemandCompleted](docs/WebhookDataDemandCompleted.md)
 - [Model.WebhookDataDemandCompletedPartiesInner](docs/WebhookDataDemandCompletedPartiesInner.md)
 - [Model.WebhookDataDemandCreated](docs/WebhookDataDemandCreated.md)
 - [Model.WebhookDataDemandExpired](docs/WebhookDataDemandExpired.md)
 - [Model.WebhookDataDemandExpiredPartiesInner](docs/WebhookDataDemandExpiredPartiesInner.md)
 - [Model.WebhookDataPartyRejected](docs/WebhookDataPartyRejected.md)
 - [Model.WebhookDataPartyRejectedParty](docs/WebhookDataPartyRejectedParty.md)
 - [Model.WebhookDataPartySigned](docs/WebhookDataPartySigned.md)
 - [Model.WebhookDataPartyViewed](docs/WebhookDataPartyViewed.md)
 - [Model.WebhookEnvelope](docs/WebhookEnvelope.md)


<a id="documentation-for-authorization"></a>
## Documentation for Authorization


Authentication schemes defined for the API:
<a id="ApiKeyAuth"></a>
### ApiKeyAuth

- **Type**: API key
- **API key parameter name**: X-API-Key
- **Location**: HTTP header

