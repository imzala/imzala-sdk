# imzala-client-generated

imzala External API

- API version: 1.8.18

- Build date: 2026-09-20T23:59:15.949712+03:00[Europe/Istanbul]

- Generator version: 7.23.0

imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.

**Sürüm:** 1.8.18 · **Son güncelleme:** 2026-09-20

## Auth
Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden
oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer)
veya **Hesap Ayarları -> API Anahtarları**.

## Workspace (organizasyon)
Organizasyon içinde oluşturulmuş bir API anahtarı kendi organizasyonuna
bağlıdır: `X-Workspace-Id` başlığı gönderilmezse anahtarın organizasyonu
otomatik uygulanır; gönderilirse anahtarın organizasyonuyla aynı olmalıdır
(aksi halde 403 `WORKSPACE_MISMATCH`). Kişisel anahtarlar için bu başlık
gerekmez.

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
- Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve
  gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur.
- `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki
  `retry_after_seconds` alanı aynı değeri taşır.
- Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir
  (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,
  `RateLimit-Reset`, `RateLimit-Policy`.
- Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın.
- Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:

  | Uç | Limit |
  |----|-------|
  | `POST /api/v1/timestamps` | 10 istek/dakika |
  | `POST /api/v1/demands/bulk` | 5 istek/dakika |
  | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |
  | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |

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

### Olay tipleri (8)
| Olay | Tetikleyici |
|------|-------------|
| `demand.created` | Yeni sözleşme oluşturuldu |
| `demand.completed` | Tüm taraflar imzaladı |
| `demand.expired` | Sözleşme süresi doldu |
| `party.signed` | Bir taraf imzaladı |
| `party.viewed` | Bir taraf imza sayfasını ilk kez açtı |
| `party.rejected` | Bir taraf reddetti |
| `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı |
| `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı |

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
- `type` — yukarıdaki 8 olay tipinden biri (lowercase).
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


  For more information, please visit [https://imzala.org](https://imzala.org)

*Automatically generated by the [OpenAPI Generator](https://openapi-generator.tech)*

## Requirements

Building the API client library requires:

1. Java 11+
2. Maven/Gradle

## Installation

To install the API client library to your local Maven repository, simply execute:

```shell
mvn clean install
```

To deploy it to a remote Maven repository instead, configure the settings of the repository and execute:

```shell
mvn clean deploy
```

Refer to the [OSSRH Guide](http://central.sonatype.org/pages/ossrh-guide.html) for more information.

### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
  <groupId>org.imzala</groupId>
  <artifactId>imzala-client-generated</artifactId>
  <version>1.8.18</version>
  <scope>compile</scope>
</dependency>
```

### Gradle users

Add this dependency to your project's build file:

```groovy
compile "org.imzala:imzala-client-generated:1.8.18"
```

### Others

At first generate the JAR by executing:

```shell
mvn clean package
```

Then manually install the following JARs:

- `target/imzala-client-generated-1.8.18.jar`
- `target/lib/*.jar`

## Getting Started

Please follow the [installation](#installation) instruction and execute the following Java code:

```java

import org.imzala.client.generated.*;
import org.imzala.client.generated.model.*;
import org.imzala.client.generated.api.AccountApi;

public class AccountApiExample {

    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        // Configure clients using the `defaultClient` object, such as
        // overriding the host and port, timeout, etc.
        AccountApi apiInstance = new AccountApi(defaultClient);
        try {
            ApiV1MeGet200Response result = apiInstance.apiV1MeGet();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AccountApi#apiV1MeGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}

```

## Documentation for API Endpoints

All URIs are relative to *https://api-prd.imzala.org*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AccountApi* | [**apiV1MeGet**](docs/AccountApi.md#apiV1MeGet) | **GET** /api/v1/me | API key sahibi bilgisi
*AccountApi* | [**apiV1MeGetWithHttpInfo**](docs/AccountApi.md#apiV1MeGetWithHttpInfo) | **GET** /api/v1/me | API key sahibi bilgisi
*ContactsApi* | [**apiV1ContactsGet**](docs/ContactsApi.md#apiV1ContactsGet) | **GET** /api/v1/contacts | Kişi listesi
*ContactsApi* | [**apiV1ContactsGetWithHttpInfo**](docs/ContactsApi.md#apiV1ContactsGetWithHttpInfo) | **GET** /api/v1/contacts | Kişi listesi
*ContactsApi* | [**apiV1ContactsPost**](docs/ContactsApi.md#apiV1ContactsPost) | **POST** /api/v1/contacts | Kişi oluştur
*ContactsApi* | [**apiV1ContactsPostWithHttpInfo**](docs/ContactsApi.md#apiV1ContactsPostWithHttpInfo) | **POST** /api/v1/contacts | Kişi oluştur
*DemandsApi* | [**apiV1DemandsBulkPost**](docs/DemandsApi.md#apiV1DemandsBulkPost) | **POST** /api/v1/demands/bulk | Toplu sözleşme oluştur (tek şablondan N alıcı)
*DemandsApi* | [**apiV1DemandsBulkPostWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsBulkPostWithHttpInfo) | **POST** /api/v1/demands/bulk | Toplu sözleşme oluştur (tek şablondan N alıcı)
*DemandsApi* | [**apiV1DemandsDemandIdDispatchPost**](docs/DemandsApi.md#apiV1DemandsDemandIdDispatchPost) | **POST** /api/v1/demands/{demandId}/dispatch | Zarfı imzaya gönder (yayınla + davet)
*DemandsApi* | [**apiV1DemandsDemandIdDispatchPostWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsDemandIdDispatchPostWithHttpInfo) | **POST** /api/v1/demands/{demandId}/dispatch | Zarfı imzaya gönder (yayınla + davet)
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut**](docs/DemandsApi.md#apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut) | **PUT** /api/v1/demands/{demandId}/documents/{docId}/assignments | Belgeye imzacı ata (tam-küme replace)
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo) | **PUT** /api/v1/demands/{demandId}/documents/{docId}/assignments | Belgeye imzacı ata (tam-küme replace)
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsDocIdDelete**](docs/DemandsApi.md#apiV1DemandsDemandIdDocumentsDocIdDelete) | **DELETE** /api/v1/demands/{demandId}/documents/{docId} | Belgeyi zarftan sil
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo) | **DELETE** /api/v1/demands/{demandId}/documents/{docId} | Belgeyi zarftan sil
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsDocIdPatch**](docs/DemandsApi.md#apiV1DemandsDemandIdDocumentsDocIdPatch) | **PATCH** /api/v1/demands/{demandId}/documents/{docId} | Belge metadata güncelle
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo) | **PATCH** /api/v1/demands/{demandId}/documents/{docId} | Belge metadata güncelle
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsGet**](docs/DemandsApi.md#apiV1DemandsDemandIdDocumentsGet) | **GET** /api/v1/demands/{demandId}/documents | Zarf belge listesi
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsGetWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsDemandIdDocumentsGetWithHttpInfo) | **GET** /api/v1/demands/{demandId}/documents | Zarf belge listesi
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsOrderPut**](docs/DemandsApi.md#apiV1DemandsDemandIdDocumentsOrderPut) | **PUT** /api/v1/demands/{demandId}/documents/order | Zarftaki belgelerin sırasını değiştir
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo) | **PUT** /api/v1/demands/{demandId}/documents/order | Zarftaki belgelerin sırasını değiştir
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsPost**](docs/DemandsApi.md#apiV1DemandsDemandIdDocumentsPost) | **POST** /api/v1/demands/{demandId}/documents | Zarfa metadata-only belge ekle
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsPostWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsDemandIdDocumentsPostWithHttpInfo) | **POST** /api/v1/demands/{demandId}/documents | Zarfa metadata-only belge ekle
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsUploadPost**](docs/DemandsApi.md#apiV1DemandsDemandIdDocumentsUploadPost) | **POST** /api/v1/demands/{demandId}/documents/upload | Zarfa dosya yükle (belge başına tek dosya)
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo) | **POST** /api/v1/demands/{demandId}/documents/upload | Zarfa dosya yükle (belge başına tek dosya)
*DemandsApi* | [**apiV1DemandsGet**](docs/DemandsApi.md#apiV1DemandsGet) | **GET** /api/v1/demands | Sözleşme listesi (counts-only, PII&#39;siz)
*DemandsApi* | [**apiV1DemandsGetWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsGetWithHttpInfo) | **GET** /api/v1/demands | Sözleşme listesi (counts-only, PII&#39;siz)
*DemandsApi* | [**apiV1DemandsIdBelgeDocumentIdPdfGet**](docs/DemandsApi.md#apiV1DemandsIdBelgeDocumentIdPdfGet) | **GET** /api/v1/demands/{id}/belge/{document_id}/pdf | Belge-özgü imzalı PDF (çok-belgeli zarf)
*DemandsApi* | [**apiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo) | **GET** /api/v1/demands/{id}/belge/{document_id}/pdf | Belge-özgü imzalı PDF (çok-belgeli zarf)
*DemandsApi* | [**apiV1DemandsIdCancelPost**](docs/DemandsApi.md#apiV1DemandsIdCancelPost) | **POST** /api/v1/demands/{id}/cancel | Sözleşme iptal (void)
*DemandsApi* | [**apiV1DemandsIdCancelPostWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsIdCancelPostWithHttpInfo) | **POST** /api/v1/demands/{id}/cancel | Sözleşme iptal (void)
*DemandsApi* | [**apiV1DemandsIdCertificateGet**](docs/DemandsApi.md#apiV1DemandsIdCertificateGet) | **GET** /api/v1/demands/{id}/certificate | Tamamlanma sertifikası (PAdES B-T)
*DemandsApi* | [**apiV1DemandsIdCertificateGetWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsIdCertificateGetWithHttpInfo) | **GET** /api/v1/demands/{id}/certificate | Tamamlanma sertifikası (PAdES B-T)
*DemandsApi* | [**apiV1DemandsIdDelete**](docs/DemandsApi.md#apiV1DemandsIdDelete) | **DELETE** /api/v1/demands/{id} | Sözleşme sil (yalnızca tamamlanmamış)
*DemandsApi* | [**apiV1DemandsIdDeleteWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsIdDeleteWithHttpInfo) | **DELETE** /api/v1/demands/{id} | Sözleşme sil (yalnızca tamamlanmamış)
*DemandsApi* | [**apiV1DemandsIdEmbedSessionPost**](docs/DemandsApi.md#apiV1DemandsIdEmbedSessionPost) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint)
*DemandsApi* | [**apiV1DemandsIdEmbedSessionPostWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsIdEmbedSessionPostWithHttpInfo) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint)
*DemandsApi* | [**apiV1DemandsIdGet**](docs/DemandsApi.md#apiV1DemandsIdGet) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi
*DemandsApi* | [**apiV1DemandsIdGetWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsIdGetWithHttpInfo) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi
*DemandsApi* | [**apiV1DemandsIdItemsPost**](docs/DemandsApi.md#apiV1DemandsIdItemsPost) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace)
*DemandsApi* | [**apiV1DemandsIdItemsPostWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsIdItemsPostWithHttpInfo) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace)
*DemandsApi* | [**apiV1DemandsIdPartiesPartyIdResendPost**](docs/DemandsApi.md#apiV1DemandsIdPartiesPartyIdResendPost) | **POST** /api/v1/demands/{id}/parties/{partyId}/resend | Tekil tarafa imza davetini tekrar gönder
*DemandsApi* | [**apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo) | **POST** /api/v1/demands/{id}/parties/{partyId}/resend | Tekil tarafa imza davetini tekrar gönder
*DemandsApi* | [**apiV1DemandsIdPdfGet**](docs/DemandsApi.md#apiV1DemandsIdPdfGet) | **GET** /api/v1/demands/{id}/pdf | İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
*DemandsApi* | [**apiV1DemandsIdPdfGetWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsIdPdfGetWithHttpInfo) | **GET** /api/v1/demands/{id}/pdf | İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
*DemandsApi* | [**apiV1DemandsIdTimelineGet**](docs/DemandsApi.md#apiV1DemandsIdTimelineGet) | **GET** /api/v1/demands/{id}/timeline | İmza denetim izi (maskeli)
*DemandsApi* | [**apiV1DemandsIdTimelineGetWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsIdTimelineGetWithHttpInfo) | **GET** /api/v1/demands/{id}/timeline | İmza denetim izi (maskeli)
*DemandsApi* | [**apiV1DemandsPost**](docs/DemandsApi.md#apiV1DemandsPost) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan)
*DemandsApi* | [**apiV1DemandsPostWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsPostWithHttpInfo) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan)
*DemandsApi* | [**apiV1DemandsUploadPost**](docs/DemandsApi.md#apiV1DemandsUploadPost) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz)
*DemandsApi* | [**apiV1DemandsUploadPostWithHttpInfo**](docs/DemandsApi.md#apiV1DemandsUploadPostWithHttpInfo) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz)
*DemandsApi* | [**apiV1FieldTemplatesIdPreviewLayoutPost**](docs/DemandsApi.md#apiV1FieldTemplatesIdPreviewLayoutPost) | **POST** /api/v1/field-templates/{id}/preview-layout | Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
*DemandsApi* | [**apiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo**](docs/DemandsApi.md#apiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo) | **POST** /api/v1/field-templates/{id}/preview-layout | Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
*RemindersApi* | [**apiV1DemandsIdRemindersPost**](docs/RemindersApi.md#apiV1DemandsIdRemindersPost) | **POST** /api/v1/demands/{id}/reminders | Anlık hatırlatma tetikle (imzalanmamış taraflara)
*RemindersApi* | [**apiV1DemandsIdRemindersPostWithHttpInfo**](docs/RemindersApi.md#apiV1DemandsIdRemindersPostWithHttpInfo) | **POST** /api/v1/demands/{id}/reminders | Anlık hatırlatma tetikle (imzalanmamış taraflara)
*ReportsApi* | [**apiV1ReportsGet**](docs/ReportsApi.md#apiV1ReportsGet) | **GET** /api/v1/reports | Rapor / özet (agrege sözleşme durum sayıları)
*ReportsApi* | [**apiV1ReportsGetWithHttpInfo**](docs/ReportsApi.md#apiV1ReportsGetWithHttpInfo) | **GET** /api/v1/reports | Rapor / özet (agrege sözleşme durum sayıları)
*TemplatesApi* | [**apiV1FieldTemplatesGet**](docs/TemplatesApi.md#apiV1FieldTemplatesGet) | **GET** /api/v1/field-templates | Alan Şablonlarını listeler
*TemplatesApi* | [**apiV1FieldTemplatesGetWithHttpInfo**](docs/TemplatesApi.md#apiV1FieldTemplatesGetWithHttpInfo) | **GET** /api/v1/field-templates | Alan Şablonlarını listeler
*TemplatesApi* | [**apiV1FieldTemplatesIdGet**](docs/TemplatesApi.md#apiV1FieldTemplatesIdGet) | **GET** /api/v1/field-templates/{id} | Alan Şablonu ayrıntısı (roller + alan sayıları)
*TemplatesApi* | [**apiV1FieldTemplatesIdGetWithHttpInfo**](docs/TemplatesApi.md#apiV1FieldTemplatesIdGetWithHttpInfo) | **GET** /api/v1/field-templates/{id} | Alan Şablonu ayrıntısı (roller + alan sayıları)
*TemplatesApi* | [**apiV1TemplatesGet**](docs/TemplatesApi.md#apiV1TemplatesGet) | **GET** /api/v1/templates | Şablon listesi
*TemplatesApi* | [**apiV1TemplatesGetWithHttpInfo**](docs/TemplatesApi.md#apiV1TemplatesGetWithHttpInfo) | **GET** /api/v1/templates | Şablon listesi
*TemplatesApi* | [**apiV1TemplatesIdDelete**](docs/TemplatesApi.md#apiV1TemplatesIdDelete) | **DELETE** /api/v1/templates/{id} | Şablon sil
*TemplatesApi* | [**apiV1TemplatesIdDeleteWithHttpInfo**](docs/TemplatesApi.md#apiV1TemplatesIdDeleteWithHttpInfo) | **DELETE** /api/v1/templates/{id} | Şablon sil
*TemplatesApi* | [**apiV1TemplatesIdGet**](docs/TemplatesApi.md#apiV1TemplatesIdGet) | **GET** /api/v1/templates/{id} | Şablon detay
*TemplatesApi* | [**apiV1TemplatesIdGetWithHttpInfo**](docs/TemplatesApi.md#apiV1TemplatesIdGetWithHttpInfo) | **GET** /api/v1/templates/{id} | Şablon detay
*TemplatesApi* | [**apiV1TemplatesIdPatch**](docs/TemplatesApi.md#apiV1TemplatesIdPatch) | **PATCH** /api/v1/templates/{id} | Şablon metadata güncelle
*TemplatesApi* | [**apiV1TemplatesIdPatchWithHttpInfo**](docs/TemplatesApi.md#apiV1TemplatesIdPatchWithHttpInfo) | **PATCH** /api/v1/templates/{id} | Şablon metadata güncelle
*TemplatesApi* | [**apiV1TemplatesIdUsageGet**](docs/TemplatesApi.md#apiV1TemplatesIdUsageGet) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek)
*TemplatesApi* | [**apiV1TemplatesIdUsageGetWithHttpInfo**](docs/TemplatesApi.md#apiV1TemplatesIdUsageGetWithHttpInfo) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek)
*TimestampsApi* | [**apiV1TimestampsGet**](docs/TimestampsApi.md#apiV1TimestampsGet) | **GET** /api/v1/timestamps | Zaman damgası listesi
*TimestampsApi* | [**apiV1TimestampsGetWithHttpInfo**](docs/TimestampsApi.md#apiV1TimestampsGetWithHttpInfo) | **GET** /api/v1/timestamps | Zaman damgası listesi
*TimestampsApi* | [**apiV1TimestampsIdGet**](docs/TimestampsApi.md#apiV1TimestampsIdGet) | **GET** /api/v1/timestamps/{id} | Zaman damgası detayı
*TimestampsApi* | [**apiV1TimestampsIdGetWithHttpInfo**](docs/TimestampsApi.md#apiV1TimestampsIdGetWithHttpInfo) | **GET** /api/v1/timestamps/{id} | Zaman damgası detayı
*TimestampsApi* | [**apiV1TimestampsPost**](docs/TimestampsApi.md#apiV1TimestampsPost) | **POST** /api/v1/timestamps | Zaman damgası oluştur (eser tescil)
*TimestampsApi* | [**apiV1TimestampsPostWithHttpInfo**](docs/TimestampsApi.md#apiV1TimestampsPostWithHttpInfo) | **POST** /api/v1/timestamps | Zaman damgası oluştur (eser tescil)


## Documentation for Models

 - [ApiError](docs/ApiError.md)
 - [ApiV1ContactsGet200Response](docs/ApiV1ContactsGet200Response.md)
 - [ApiV1ContactsGet200ResponseData](docs/ApiV1ContactsGet200ResponseData.md)
 - [ApiV1ContactsPost201Response](docs/ApiV1ContactsPost201Response.md)
 - [ApiV1ContactsPostRequest](docs/ApiV1ContactsPostRequest.md)
 - [ApiV1DemandsBulkPost200Response](docs/ApiV1DemandsBulkPost200Response.md)
 - [ApiV1DemandsBulkPost200ResponseData](docs/ApiV1DemandsBulkPost200ResponseData.md)
 - [ApiV1DemandsBulkPost200ResponseDataResultsInner](docs/ApiV1DemandsBulkPost200ResponseDataResultsInner.md)
 - [ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails](docs/ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails.md)
 - [ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner](docs/ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner.md)
 - [ApiV1DemandsBulkPostRequest](docs/ApiV1DemandsBulkPostRequest.md)
 - [ApiV1DemandsBulkPostRequestOptions](docs/ApiV1DemandsBulkPostRequestOptions.md)
 - [ApiV1DemandsBulkPostRequestRowsInner](docs/ApiV1DemandsBulkPostRequestRowsInner.md)
 - [ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner](docs/ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.md)
 - [ApiV1DemandsDemandIdDispatchPost200Response](docs/ApiV1DemandsDemandIdDispatchPost200Response.md)
 - [ApiV1DemandsDemandIdDispatchPost200ResponseData](docs/ApiV1DemandsDemandIdDispatchPost200ResponseData.md)
 - [ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits](docs/ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits.md)
 - [ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations](docs/ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations.md)
 - [ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner](docs/ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner.md)
 - [ApiV1DemandsDemandIdDispatchPostRequest](docs/ApiV1DemandsDemandIdDispatchPostRequest.md)
 - [ApiV1DemandsDemandIdDispatchPostRequestSendInvitations](docs/ApiV1DemandsDemandIdDispatchPostRequestSendInvitations.md)
 - [ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest](docs/ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest.md)
 - [ApiV1DemandsDemandIdDocumentsDocIdPatchRequest](docs/ApiV1DemandsDemandIdDocumentsDocIdPatchRequest.md)
 - [ApiV1DemandsDemandIdDocumentsGet200Response](docs/ApiV1DemandsDemandIdDocumentsGet200Response.md)
 - [ApiV1DemandsDemandIdDocumentsGet200ResponseData](docs/ApiV1DemandsDemandIdDocumentsGet200ResponseData.md)
 - [ApiV1DemandsDemandIdDocumentsOrderPutRequest](docs/ApiV1DemandsDemandIdDocumentsOrderPutRequest.md)
 - [ApiV1DemandsDemandIdDocumentsPost201Response](docs/ApiV1DemandsDemandIdDocumentsPost201Response.md)
 - [ApiV1DemandsDemandIdDocumentsPost201ResponseData](docs/ApiV1DemandsDemandIdDocumentsPost201ResponseData.md)
 - [ApiV1DemandsDemandIdDocumentsPostRequest](docs/ApiV1DemandsDemandIdDocumentsPostRequest.md)
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
 - [ApiV1DemandsPost400Response](docs/ApiV1DemandsPost400Response.md)
 - [ApiV1DemandsPost402Response](docs/ApiV1DemandsPost402Response.md)
 - [ApiV1DemandsPost402ResponseData](docs/ApiV1DemandsPost402ResponseData.md)
 - [ApiV1DemandsPost500Response](docs/ApiV1DemandsPost500Response.md)
 - [ApiV1DemandsUploadPost201Response](docs/ApiV1DemandsUploadPost201Response.md)
 - [ApiV1DemandsUploadPost402Response](docs/ApiV1DemandsUploadPost402Response.md)
 - [ApiV1DemandsUploadPost500Response](docs/ApiV1DemandsUploadPost500Response.md)
 - [ApiV1FieldTemplatesGet200Response](docs/ApiV1FieldTemplatesGet200Response.md)
 - [ApiV1FieldTemplatesGet200ResponseData](docs/ApiV1FieldTemplatesGet200ResponseData.md)
 - [ApiV1FieldTemplatesIdGet200Response](docs/ApiV1FieldTemplatesIdGet200Response.md)
 - [ApiV1FieldTemplatesIdPreviewLayoutPost200Response](docs/ApiV1FieldTemplatesIdPreviewLayoutPost200Response.md)
 - [ApiV1MeGet200Response](docs/ApiV1MeGet200Response.md)
 - [ApiV1MeGet200ResponseData](docs/ApiV1MeGet200ResponseData.md)
 - [ApiV1MeGet200ResponseDataCredits](docs/ApiV1MeGet200ResponseDataCredits.md)
 - [ApiV1MeGet200ResponseDataWorkspace](docs/ApiV1MeGet200ResponseDataWorkspace.md)
 - [ApiV1ReportsGet200Response](docs/ApiV1ReportsGet200Response.md)
 - [ApiV1ReportsGet200ResponseData](docs/ApiV1ReportsGet200ResponseData.md)
 - [ApiV1ReportsGet200ResponseDataContracts](docs/ApiV1ReportsGet200ResponseDataContracts.md)
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
 - [ApiV1TimestampsGet200Response](docs/ApiV1TimestampsGet200Response.md)
 - [ApiV1TimestampsGet200ResponseData](docs/ApiV1TimestampsGet200ResponseData.md)
 - [ApiV1TimestampsIdGet200Response](docs/ApiV1TimestampsIdGet200Response.md)
 - [ApiV1TimestampsPost201Response](docs/ApiV1TimestampsPost201Response.md)
 - [ApiV1TimestampsPostRequest1](docs/ApiV1TimestampsPostRequest1.md)
 - [CodedError](docs/CodedError.md)
 - [ContactSummary](docs/ContactSummary.md)
 - [ContactSummaryCompany](docs/ContactSummaryCompany.md)
 - [CreateDemandRequest](docs/CreateDemandRequest.md)
 - [CreatedDemand](docs/CreatedDemand.md)
 - [CreatedDemandSigningUrlsInner](docs/CreatedDemandSigningUrlsInner.md)
 - [CreatedDemandUpload](docs/CreatedDemandUpload.md)
 - [CreatedDemandUploadDispatch](docs/CreatedDemandUploadDispatch.md)
 - [CreatedDemandUploadDispatchResultsInner](docs/CreatedDemandUploadDispatchResultsInner.md)
 - [CreatedDemandUploadFieldLayout](docs/CreatedDemandUploadFieldLayout.md)
 - [DemandPage](docs/DemandPage.md)
 - [DemandStatus](docs/DemandStatus.md)
 - [DemandStatusPartiesInner](docs/DemandStatusPartiesInner.md)
 - [DocumentSelectionError](docs/DocumentSelectionError.md)
 - [DocumentSelectionErrorDetails](docs/DocumentSelectionErrorDetails.md)
 - [DocumentSelectionInput](docs/DocumentSelectionInput.md)
 - [EnvelopeDocument](docs/EnvelopeDocument.md)
 - [FieldLayoutDiagnostic](docs/FieldLayoutDiagnostic.md)
 - [FieldLayoutPreview](docs/FieldLayoutPreview.md)
 - [FieldLayoutPreviewPlacementsSummary](docs/FieldLayoutPreviewPlacementsSummary.md)
 - [FieldLayoutPreviewPlacementsSummaryByPageInner](docs/FieldLayoutPreviewPlacementsSummaryByPageInner.md)
 - [FieldLayoutUnresolved](docs/FieldLayoutUnresolved.md)
 - [FieldLayoutWarning](docs/FieldLayoutWarning.md)
 - [FieldTemplateDetail](docs/FieldTemplateDetail.md)
 - [FieldTemplateDetailPartiesInner](docs/FieldTemplateDetailPartiesInner.md)
 - [FieldTemplateListItem](docs/FieldTemplateListItem.md)
 - [FieldTemplateParty](docs/FieldTemplateParty.md)
 - [KycWebhookEnvelope](docs/KycWebhookEnvelope.md)
 - [PageItem](docs/PageItem.md)
 - [PartyMappingInput](docs/PartyMappingInput.md)
 - [PartyMappingInputVariablesValue](docs/PartyMappingInputVariablesValue.md)
 - [ReminderSettings](docs/ReminderSettings.md)
 - [StandardError](docs/StandardError.md)
 - [StandardErrorError](docs/StandardErrorError.md)
 - [TemplateDetail](docs/TemplateDetail.md)
 - [TemplateDocumentSummary](docs/TemplateDocumentSummary.md)
 - [TemplatePartySummary](docs/TemplatePartySummary.md)
 - [TemplateSummary](docs/TemplateSummary.md)
 - [TemplateSummaryPartiesInner](docs/TemplateSummaryPartiesInner.md)
 - [TemplateUsage](docs/TemplateUsage.md)
 - [TemplateUsageEndpoint](docs/TemplateUsageEndpoint.md)
 - [TemplateUsageExampleRequest](docs/TemplateUsageExampleRequest.md)
 - [TemplateUsageFormFieldsInner](docs/TemplateUsageFormFieldsInner.md)
 - [TemplateUsagePartiesInner](docs/TemplateUsagePartiesInner.md)
 - [TemplateUsagePartiesInnerSupportedFieldsInner](docs/TemplateUsagePartiesInnerSupportedFieldsInner.md)
 - [TemplateUsageVariablesInner](docs/TemplateUsageVariablesInner.md)
 - [TemplateVariable](docs/TemplateVariable.md)
 - [TimestampListItem](docs/TimestampListItem.md)
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
 - [WebhookDataKycTerminal](docs/WebhookDataKycTerminal.md)
 - [WebhookDataKycTerminalStepsInner](docs/WebhookDataKycTerminalStepsInner.md)
 - [WebhookDataPartyRejected](docs/WebhookDataPartyRejected.md)
 - [WebhookDataPartyRejectedParty](docs/WebhookDataPartyRejectedParty.md)
 - [WebhookDataPartySigned](docs/WebhookDataPartySigned.md)
 - [WebhookDataPartyViewed](docs/WebhookDataPartyViewed.md)
 - [WebhookEnvelope](docs/WebhookEnvelope.md)


<a id="documentation-for-authorization"></a>
## Documentation for Authorization


Authentication schemes defined for the API:
<a id="ApiKeyAuth"></a>
### ApiKeyAuth


- **Type**: API key
- **API key parameter name**: X-API-Key
- **Location**: HTTP header


## Recommendation

It's recommended to create an instance of `ApiClient` per thread in a multithreaded environment to avoid any potential issues.
However, the instances of the api clients created from the `ApiClient` are thread-safe and can be re-used.

## Author

destek@imzala.org

