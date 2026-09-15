# OpenAPIClient-php

imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.

**Sürüm:** 1.8.13 · **Son güncelleme:** 2026-09-15

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


For more information, please visit [https://imzala.org](https://imzala.org).

## Installation & Usage

### Requirements

PHP 8.1 and later.

### Composer

To install the bindings via [Composer](https://getcomposer.org/), add the following to `composer.json`:

```json
{
  "repositories": [
    {
      "type": "vcs",
      "url": "https://github.com/GIT_USER_ID/GIT_REPO_ID.git"
    }
  ],
  "require": {
    "GIT_USER_ID/GIT_REPO_ID": "*@dev"
  }
}
```

Then run `composer install`

### Manual Installation

Download the files and include `autoload.php`:

```php
<?php
require_once('/path/to/OpenAPIClient-php/vendor/autoload.php');
```

## Getting Started

Please follow the [installation procedure](#installation--usage) and then run the following:

```php
<?php
require_once(__DIR__ . '/vendor/autoload.php');



// Configure API key authorization: ApiKeyAuth
$config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKey('X-API-Key', 'YOUR_API_KEY');
// Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
// $config = Imzala\Client\Configuration::getDefaultConfiguration()->setApiKeyPrefix('X-API-Key', 'Bearer');


$apiInstance = new Imzala\Client\Api\AccountApi(
    // If you want use custom http client, pass your client which implements `GuzzleHttp\ClientInterface`.
    // This is optional, `GuzzleHttp\Client` will be used as default.
    new GuzzleHttp\Client(),
    $config
);

try {
    $result = $apiInstance->apiV1MeGet();
    print_r($result);
} catch (Exception $e) {
    echo 'Exception when calling AccountApi->apiV1MeGet: ', $e->getMessage(), PHP_EOL;
}

```

## API Endpoints

All URIs are relative to *https://api-prd.imzala.org*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AccountApi* | [**apiV1MeGet**](docs/Api/AccountApi.md#apiv1meget) | **GET** /api/v1/me | API key sahibi bilgisi
*ContactsApi* | [**apiV1ContactsGet**](docs/Api/ContactsApi.md#apiv1contactsget) | **GET** /api/v1/contacts | Kişi listesi
*ContactsApi* | [**apiV1ContactsPost**](docs/Api/ContactsApi.md#apiv1contactspost) | **POST** /api/v1/contacts | Kişi oluştur
*DemandsApi* | [**apiV1DemandsBulkPost**](docs/Api/DemandsApi.md#apiv1demandsbulkpost) | **POST** /api/v1/demands/bulk | Toplu sözleşme oluştur (tek şablondan N alıcı)
*DemandsApi* | [**apiV1DemandsDemandIdDispatchPost**](docs/Api/DemandsApi.md#apiv1demandsdemandiddispatchpost) | **POST** /api/v1/demands/{demandId}/dispatch | Zarfı imzaya gönder (yayınla + davet)
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut**](docs/Api/DemandsApi.md#apiv1demandsdemandiddocumentsdocidassignmentsput) | **PUT** /api/v1/demands/{demandId}/documents/{docId}/assignments | Belgeye imzacı ata (tam-küme replace)
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsDocIdDelete**](docs/Api/DemandsApi.md#apiv1demandsdemandiddocumentsdociddelete) | **DELETE** /api/v1/demands/{demandId}/documents/{docId} | Belgeyi zarftan sil
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsDocIdPatch**](docs/Api/DemandsApi.md#apiv1demandsdemandiddocumentsdocidpatch) | **PATCH** /api/v1/demands/{demandId}/documents/{docId} | Belge metadata güncelle
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsGet**](docs/Api/DemandsApi.md#apiv1demandsdemandiddocumentsget) | **GET** /api/v1/demands/{demandId}/documents | Zarf belge listesi
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsOrderPut**](docs/Api/DemandsApi.md#apiv1demandsdemandiddocumentsorderput) | **PUT** /api/v1/demands/{demandId}/documents/order | Zarftaki belgelerin sırasını değiştir
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsPost**](docs/Api/DemandsApi.md#apiv1demandsdemandiddocumentspost) | **POST** /api/v1/demands/{demandId}/documents | Zarfa metadata-only belge ekle
*DemandsApi* | [**apiV1DemandsDemandIdDocumentsUploadPost**](docs/Api/DemandsApi.md#apiv1demandsdemandiddocumentsuploadpost) | **POST** /api/v1/demands/{demandId}/documents/upload | Zarfa dosya yükle (belge başına tek dosya)
*DemandsApi* | [**apiV1DemandsGet**](docs/Api/DemandsApi.md#apiv1demandsget) | **GET** /api/v1/demands | Sözleşme listesi (counts-only, PII&#39;siz)
*DemandsApi* | [**apiV1DemandsIdBelgeDocumentIdPdfGet**](docs/Api/DemandsApi.md#apiv1demandsidbelgedocumentidpdfget) | **GET** /api/v1/demands/{id}/belge/{document_id}/pdf | Belge-özgü imzalı PDF (çok-belgeli zarf)
*DemandsApi* | [**apiV1DemandsIdCancelPost**](docs/Api/DemandsApi.md#apiv1demandsidcancelpost) | **POST** /api/v1/demands/{id}/cancel | Sözleşme iptal (void)
*DemandsApi* | [**apiV1DemandsIdCertificateGet**](docs/Api/DemandsApi.md#apiv1demandsidcertificateget) | **GET** /api/v1/demands/{id}/certificate | Tamamlanma sertifikası (PAdES B-T)
*DemandsApi* | [**apiV1DemandsIdDelete**](docs/Api/DemandsApi.md#apiv1demandsiddelete) | **DELETE** /api/v1/demands/{id} | Sözleşme sil (yalnızca tamamlanmamış)
*DemandsApi* | [**apiV1DemandsIdEmbedSessionPost**](docs/Api/DemandsApi.md#apiv1demandsidembedsessionpost) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint)
*DemandsApi* | [**apiV1DemandsIdGet**](docs/Api/DemandsApi.md#apiv1demandsidget) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi
*DemandsApi* | [**apiV1DemandsIdItemsPost**](docs/Api/DemandsApi.md#apiv1demandsiditemspost) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace)
*DemandsApi* | [**apiV1DemandsIdPartiesPartyIdResendPost**](docs/Api/DemandsApi.md#apiv1demandsidpartiespartyidresendpost) | **POST** /api/v1/demands/{id}/parties/{partyId}/resend | Tekil tarafa imza davetini tekrar gönder
*DemandsApi* | [**apiV1DemandsIdPdfGet**](docs/Api/DemandsApi.md#apiv1demandsidpdfget) | **GET** /api/v1/demands/{id}/pdf | İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
*DemandsApi* | [**apiV1DemandsIdTimelineGet**](docs/Api/DemandsApi.md#apiv1demandsidtimelineget) | **GET** /api/v1/demands/{id}/timeline | İmza denetim izi (maskeli)
*DemandsApi* | [**apiV1DemandsPost**](docs/Api/DemandsApi.md#apiv1demandspost) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan)
*DemandsApi* | [**apiV1DemandsUploadPost**](docs/Api/DemandsApi.md#apiv1demandsuploadpost) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz)
*DemandsApi* | [**apiV1FieldTemplatesIdPreviewLayoutPost**](docs/Api/DemandsApi.md#apiv1fieldtemplatesidpreviewlayoutpost) | **POST** /api/v1/field-templates/{id}/preview-layout | Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
*RemindersApi* | [**apiV1DemandsIdRemindersPost**](docs/Api/RemindersApi.md#apiv1demandsidreminderspost) | **POST** /api/v1/demands/{id}/reminders | Anlık hatırlatma tetikle (imzalanmamış taraflara)
*ReportsApi* | [**apiV1ReportsGet**](docs/Api/ReportsApi.md#apiv1reportsget) | **GET** /api/v1/reports | Rapor / özet (agrege sözleşme durum sayıları)
*TemplatesApi* | [**apiV1FieldTemplatesGet**](docs/Api/TemplatesApi.md#apiv1fieldtemplatesget) | **GET** /api/v1/field-templates | Alan Şablonlarını listeler
*TemplatesApi* | [**apiV1FieldTemplatesIdGet**](docs/Api/TemplatesApi.md#apiv1fieldtemplatesidget) | **GET** /api/v1/field-templates/{id} | Alan Şablonu ayrıntısı (roller + alan sayıları)
*TemplatesApi* | [**apiV1TemplatesGet**](docs/Api/TemplatesApi.md#apiv1templatesget) | **GET** /api/v1/templates | Şablon listesi
*TemplatesApi* | [**apiV1TemplatesIdDelete**](docs/Api/TemplatesApi.md#apiv1templatesiddelete) | **DELETE** /api/v1/templates/{id} | Şablon sil
*TemplatesApi* | [**apiV1TemplatesIdGet**](docs/Api/TemplatesApi.md#apiv1templatesidget) | **GET** /api/v1/templates/{id} | Şablon detay
*TemplatesApi* | [**apiV1TemplatesIdPatch**](docs/Api/TemplatesApi.md#apiv1templatesidpatch) | **PATCH** /api/v1/templates/{id} | Şablon metadata güncelle
*TemplatesApi* | [**apiV1TemplatesIdUsageGet**](docs/Api/TemplatesApi.md#apiv1templatesidusageget) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek)
*TimestampsApi* | [**apiV1TimestampsGet**](docs/Api/TimestampsApi.md#apiv1timestampsget) | **GET** /api/v1/timestamps | Zaman damgası listesi
*TimestampsApi* | [**apiV1TimestampsIdGet**](docs/Api/TimestampsApi.md#apiv1timestampsidget) | **GET** /api/v1/timestamps/{id} | Zaman damgası detayı
*TimestampsApi* | [**apiV1TimestampsPost**](docs/Api/TimestampsApi.md#apiv1timestampspost) | **POST** /api/v1/timestamps | Zaman damgası oluştur (eser tescil)

## Models

- [ApiError](docs/Model/ApiError.md)
- [ApiV1ContactsGet200Response](docs/Model/ApiV1ContactsGet200Response.md)
- [ApiV1ContactsGet200ResponseData](docs/Model/ApiV1ContactsGet200ResponseData.md)
- [ApiV1ContactsPost201Response](docs/Model/ApiV1ContactsPost201Response.md)
- [ApiV1ContactsPostRequest](docs/Model/ApiV1ContactsPostRequest.md)
- [ApiV1DemandsBulkPost200Response](docs/Model/ApiV1DemandsBulkPost200Response.md)
- [ApiV1DemandsBulkPost200ResponseData](docs/Model/ApiV1DemandsBulkPost200ResponseData.md)
- [ApiV1DemandsBulkPost200ResponseDataResultsInner](docs/Model/ApiV1DemandsBulkPost200ResponseDataResultsInner.md)
- [ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner](docs/Model/ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner.md)
- [ApiV1DemandsBulkPostRequest](docs/Model/ApiV1DemandsBulkPostRequest.md)
- [ApiV1DemandsBulkPostRequestOptions](docs/Model/ApiV1DemandsBulkPostRequestOptions.md)
- [ApiV1DemandsBulkPostRequestRowsInner](docs/Model/ApiV1DemandsBulkPostRequestRowsInner.md)
- [ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner](docs/Model/ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.md)
- [ApiV1DemandsDemandIdDispatchPost200Response](docs/Model/ApiV1DemandsDemandIdDispatchPost200Response.md)
- [ApiV1DemandsDemandIdDispatchPost200ResponseData](docs/Model/ApiV1DemandsDemandIdDispatchPost200ResponseData.md)
- [ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits](docs/Model/ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits.md)
- [ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations](docs/Model/ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations.md)
- [ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner](docs/Model/ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner.md)
- [ApiV1DemandsDemandIdDispatchPostRequest](docs/Model/ApiV1DemandsDemandIdDispatchPostRequest.md)
- [ApiV1DemandsDemandIdDispatchPostRequestSendInvitations](docs/Model/ApiV1DemandsDemandIdDispatchPostRequestSendInvitations.md)
- [ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest](docs/Model/ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest.md)
- [ApiV1DemandsDemandIdDocumentsDocIdPatchRequest](docs/Model/ApiV1DemandsDemandIdDocumentsDocIdPatchRequest.md)
- [ApiV1DemandsDemandIdDocumentsGet200Response](docs/Model/ApiV1DemandsDemandIdDocumentsGet200Response.md)
- [ApiV1DemandsDemandIdDocumentsGet200ResponseData](docs/Model/ApiV1DemandsDemandIdDocumentsGet200ResponseData.md)
- [ApiV1DemandsDemandIdDocumentsOrderPutRequest](docs/Model/ApiV1DemandsDemandIdDocumentsOrderPutRequest.md)
- [ApiV1DemandsDemandIdDocumentsPost201Response](docs/Model/ApiV1DemandsDemandIdDocumentsPost201Response.md)
- [ApiV1DemandsDemandIdDocumentsPost201ResponseData](docs/Model/ApiV1DemandsDemandIdDocumentsPost201ResponseData.md)
- [ApiV1DemandsDemandIdDocumentsPostRequest](docs/Model/ApiV1DemandsDemandIdDocumentsPostRequest.md)
- [ApiV1DemandsGet200Response](docs/Model/ApiV1DemandsGet200Response.md)
- [ApiV1DemandsGet200ResponseData](docs/Model/ApiV1DemandsGet200ResponseData.md)
- [ApiV1DemandsGet200ResponseDataDemandsInner](docs/Model/ApiV1DemandsGet200ResponseDataDemandsInner.md)
- [ApiV1DemandsIdCancelPost200Response](docs/Model/ApiV1DemandsIdCancelPost200Response.md)
- [ApiV1DemandsIdCancelPost200ResponseData](docs/Model/ApiV1DemandsIdCancelPost200ResponseData.md)
- [ApiV1DemandsIdCancelPostRequest](docs/Model/ApiV1DemandsIdCancelPostRequest.md)
- [ApiV1DemandsIdDelete409Response](docs/Model/ApiV1DemandsIdDelete409Response.md)
- [ApiV1DemandsIdEmbedSessionPost200Response](docs/Model/ApiV1DemandsIdEmbedSessionPost200Response.md)
- [ApiV1DemandsIdEmbedSessionPost200ResponseData](docs/Model/ApiV1DemandsIdEmbedSessionPost200ResponseData.md)
- [ApiV1DemandsIdEmbedSessionPostRequest](docs/Model/ApiV1DemandsIdEmbedSessionPostRequest.md)
- [ApiV1DemandsIdGet200Response](docs/Model/ApiV1DemandsIdGet200Response.md)
- [ApiV1DemandsIdPartiesPartyIdResendPost200Response](docs/Model/ApiV1DemandsIdPartiesPartyIdResendPost200Response.md)
- [ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData](docs/Model/ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData.md)
- [ApiV1DemandsIdRemindersPost200Response](docs/Model/ApiV1DemandsIdRemindersPost200Response.md)
- [ApiV1DemandsIdRemindersPost200ResponseData](docs/Model/ApiV1DemandsIdRemindersPost200ResponseData.md)
- [ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner](docs/Model/ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner.md)
- [ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner](docs/Model/ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner.md)
- [ApiV1DemandsIdRemindersPost429Response](docs/Model/ApiV1DemandsIdRemindersPost429Response.md)
- [ApiV1DemandsIdRemindersPost429ResponseError](docs/Model/ApiV1DemandsIdRemindersPost429ResponseError.md)
- [ApiV1DemandsIdTimelineGet200Response](docs/Model/ApiV1DemandsIdTimelineGet200Response.md)
- [ApiV1DemandsIdTimelineGet200ResponseData](docs/Model/ApiV1DemandsIdTimelineGet200ResponseData.md)
- [ApiV1DemandsIdTimelineGet200ResponseDataEventsInner](docs/Model/ApiV1DemandsIdTimelineGet200ResponseDataEventsInner.md)
- [ApiV1DemandsPost201Response](docs/Model/ApiV1DemandsPost201Response.md)
- [ApiV1DemandsUploadPost201Response](docs/Model/ApiV1DemandsUploadPost201Response.md)
- [ApiV1FieldTemplatesGet200Response](docs/Model/ApiV1FieldTemplatesGet200Response.md)
- [ApiV1FieldTemplatesGet200ResponseData](docs/Model/ApiV1FieldTemplatesGet200ResponseData.md)
- [ApiV1FieldTemplatesIdGet200Response](docs/Model/ApiV1FieldTemplatesIdGet200Response.md)
- [ApiV1FieldTemplatesIdPreviewLayoutPost200Response](docs/Model/ApiV1FieldTemplatesIdPreviewLayoutPost200Response.md)
- [ApiV1MeGet200Response](docs/Model/ApiV1MeGet200Response.md)
- [ApiV1MeGet200ResponseData](docs/Model/ApiV1MeGet200ResponseData.md)
- [ApiV1MeGet200ResponseDataCredits](docs/Model/ApiV1MeGet200ResponseDataCredits.md)
- [ApiV1MeGet200ResponseDataWorkspace](docs/Model/ApiV1MeGet200ResponseDataWorkspace.md)
- [ApiV1ReportsGet200Response](docs/Model/ApiV1ReportsGet200Response.md)
- [ApiV1ReportsGet200ResponseData](docs/Model/ApiV1ReportsGet200ResponseData.md)
- [ApiV1ReportsGet200ResponseDataContracts](docs/Model/ApiV1ReportsGet200ResponseDataContracts.md)
- [ApiV1TemplatesGet200Response](docs/Model/ApiV1TemplatesGet200Response.md)
- [ApiV1TemplatesGet200ResponseData](docs/Model/ApiV1TemplatesGet200ResponseData.md)
- [ApiV1TemplatesGet401Response](docs/Model/ApiV1TemplatesGet401Response.md)
- [ApiV1TemplatesIdDelete200Response](docs/Model/ApiV1TemplatesIdDelete200Response.md)
- [ApiV1TemplatesIdDelete200ResponseData](docs/Model/ApiV1TemplatesIdDelete200ResponseData.md)
- [ApiV1TemplatesIdGet200Response](docs/Model/ApiV1TemplatesIdGet200Response.md)
- [ApiV1TemplatesIdGet404Response](docs/Model/ApiV1TemplatesIdGet404Response.md)
- [ApiV1TemplatesIdPatch200Response](docs/Model/ApiV1TemplatesIdPatch200Response.md)
- [ApiV1TemplatesIdPatch200ResponseData](docs/Model/ApiV1TemplatesIdPatch200ResponseData.md)
- [ApiV1TemplatesIdPatchRequest](docs/Model/ApiV1TemplatesIdPatchRequest.md)
- [ApiV1TemplatesIdUsageGet200Response](docs/Model/ApiV1TemplatesIdUsageGet200Response.md)
- [ApiV1TimestampsGet200Response](docs/Model/ApiV1TimestampsGet200Response.md)
- [ApiV1TimestampsGet200ResponseData](docs/Model/ApiV1TimestampsGet200ResponseData.md)
- [ApiV1TimestampsIdGet200Response](docs/Model/ApiV1TimestampsIdGet200Response.md)
- [ApiV1TimestampsPost201Response](docs/Model/ApiV1TimestampsPost201Response.md)
- [ApiV1TimestampsPostRequest1](docs/Model/ApiV1TimestampsPostRequest1.md)
- [CodedError](docs/Model/CodedError.md)
- [ContactSummary](docs/Model/ContactSummary.md)
- [ContactSummaryCompany](docs/Model/ContactSummaryCompany.md)
- [CreateDemandRequest](docs/Model/CreateDemandRequest.md)
- [CreatedDemand](docs/Model/CreatedDemand.md)
- [CreatedDemandSigningUrlsInner](docs/Model/CreatedDemandSigningUrlsInner.md)
- [CreatedDemandUpload](docs/Model/CreatedDemandUpload.md)
- [CreatedDemandUploadDispatch](docs/Model/CreatedDemandUploadDispatch.md)
- [CreatedDemandUploadDispatchResultsInner](docs/Model/CreatedDemandUploadDispatchResultsInner.md)
- [CreatedDemandUploadFieldLayout](docs/Model/CreatedDemandUploadFieldLayout.md)
- [DemandPage](docs/Model/DemandPage.md)
- [DemandStatus](docs/Model/DemandStatus.md)
- [DemandStatusPartiesInner](docs/Model/DemandStatusPartiesInner.md)
- [EnvelopeDocument](docs/Model/EnvelopeDocument.md)
- [FieldLayoutDiagnostic](docs/Model/FieldLayoutDiagnostic.md)
- [FieldLayoutPreview](docs/Model/FieldLayoutPreview.md)
- [FieldLayoutPreviewPlacementsSummary](docs/Model/FieldLayoutPreviewPlacementsSummary.md)
- [FieldLayoutPreviewPlacementsSummaryByPageInner](docs/Model/FieldLayoutPreviewPlacementsSummaryByPageInner.md)
- [FieldLayoutUnresolved](docs/Model/FieldLayoutUnresolved.md)
- [FieldLayoutWarning](docs/Model/FieldLayoutWarning.md)
- [FieldTemplateDetail](docs/Model/FieldTemplateDetail.md)
- [FieldTemplateDetailPartiesInner](docs/Model/FieldTemplateDetailPartiesInner.md)
- [FieldTemplateListItem](docs/Model/FieldTemplateListItem.md)
- [FieldTemplateParty](docs/Model/FieldTemplateParty.md)
- [KycWebhookEnvelope](docs/Model/KycWebhookEnvelope.md)
- [PageItem](docs/Model/PageItem.md)
- [PartyMappingInput](docs/Model/PartyMappingInput.md)
- [PartyMappingInputVariablesValue](docs/Model/PartyMappingInputVariablesValue.md)
- [ReminderSettings](docs/Model/ReminderSettings.md)
- [StandardError](docs/Model/StandardError.md)
- [StandardErrorError](docs/Model/StandardErrorError.md)
- [TemplateDetail](docs/Model/TemplateDetail.md)
- [TemplatePartySummary](docs/Model/TemplatePartySummary.md)
- [TemplateSummary](docs/Model/TemplateSummary.md)
- [TemplateSummaryPartiesInner](docs/Model/TemplateSummaryPartiesInner.md)
- [TemplateUsage](docs/Model/TemplateUsage.md)
- [TemplateUsageEndpoint](docs/Model/TemplateUsageEndpoint.md)
- [TemplateUsageExampleRequest](docs/Model/TemplateUsageExampleRequest.md)
- [TemplateUsageFormFieldsInner](docs/Model/TemplateUsageFormFieldsInner.md)
- [TemplateUsagePartiesInner](docs/Model/TemplateUsagePartiesInner.md)
- [TemplateUsagePartiesInnerSupportedFieldsInner](docs/Model/TemplateUsagePartiesInnerSupportedFieldsInner.md)
- [TemplateUsageVariablesInner](docs/Model/TemplateUsageVariablesInner.md)
- [TemplateVariable](docs/Model/TemplateVariable.md)
- [TimestampListItem](docs/Model/TimestampListItem.md)
- [TimestampRecord](docs/Model/TimestampRecord.md)
- [TriggerReminderRequest](docs/Model/TriggerReminderRequest.md)
- [UpsertItemsRequest](docs/Model/UpsertItemsRequest.md)
- [UpsertItemsResponse](docs/Model/UpsertItemsResponse.md)
- [UpsertItemsResponseData](docs/Model/UpsertItemsResponseData.md)
- [UpsertItemsResponseDataItemsInner](docs/Model/UpsertItemsResponseDataItemsInner.md)
- [WebhookDataDemandCompleted](docs/Model/WebhookDataDemandCompleted.md)
- [WebhookDataDemandCompletedPartiesInner](docs/Model/WebhookDataDemandCompletedPartiesInner.md)
- [WebhookDataDemandCreated](docs/Model/WebhookDataDemandCreated.md)
- [WebhookDataDemandExpired](docs/Model/WebhookDataDemandExpired.md)
- [WebhookDataDemandExpiredPartiesInner](docs/Model/WebhookDataDemandExpiredPartiesInner.md)
- [WebhookDataKycTerminal](docs/Model/WebhookDataKycTerminal.md)
- [WebhookDataKycTerminalStepsInner](docs/Model/WebhookDataKycTerminalStepsInner.md)
- [WebhookDataPartyRejected](docs/Model/WebhookDataPartyRejected.md)
- [WebhookDataPartyRejectedParty](docs/Model/WebhookDataPartyRejectedParty.md)
- [WebhookDataPartySigned](docs/Model/WebhookDataPartySigned.md)
- [WebhookDataPartyViewed](docs/Model/WebhookDataPartyViewed.md)
- [WebhookEnvelope](docs/Model/WebhookEnvelope.md)

## Authorization

Authentication schemes defined for the API:
### ApiKeyAuth

- **Type**: API key
- **API key parameter name**: X-API-Key
- **Location**: HTTP header


## Tests

To run the tests, use:

```bash
composer install
vendor/bin/phpunit
```

## Author

destek@imzala.org

## About this package

This PHP package is automatically generated by the [OpenAPI Generator](https://openapi-generator.tech) project:

- API version: `1.8.13`
    - Generator version: `7.23.0`
- Build package: `org.openapitools.codegen.languages.PhpClientCodegen`
