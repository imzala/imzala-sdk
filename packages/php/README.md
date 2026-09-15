# imzala/imzala-php

[![Packagist Version](https://img.shields.io/packagist/v/imzala/imzala-php.svg)](https://packagist.org/packages/imzala/imzala-php)
[![Packagist Downloads](https://img.shields.io/packagist/dm/imzala/imzala-php.svg)](https://packagist.org/packages/imzala/imzala-php)
[![PHP Version](https://img.shields.io/packagist/php-v/imzala/imzala-php.svg)](https://packagist.org/packages/imzala/imzala-php)
[![License](https://img.shields.io/packagist/l/imzala/imzala-php.svg)](https://packagist.org/packages/imzala/imzala-php)

İmzala dijital imza platformunun resmi **PHP** SDK'sı. Sözleşme oluşturma, imza takibi, imzalı PDF ve sertifika indirme, denetim izi, şablon yönetimi ve zaman damgası işlemlerini tek bir tip-güvenli istemciyle yapın.

```bash
composer require imzala/imzala-php
```

> **Sunucu-taraflı paket.** API anahtarınız hesabınızın tamamına erişir; tarayıcıya veya mobil uygulamaya gömmeyin. Tarayıcıda imza almak için [`@imzala/embed`](../embed) kullanın. Ayrıntı: [Sunucu-taraflı](#️-sunucu-taraflı) bölümü.

## İçindekiler

- [Gereksinimler](#gereksinimler)
- [Hızlı başlangıç](#hızlı-başlangıç)
- [Yapılandırma](#yapılandırma)
- [API referansı](#api-referansı)
  - [Sözleşmeler (demands)](#sözleşmeler-demands)
  - [Çok belgeli zarf (documents, dispatch)](#çok-belgeli-zarf-documents-dispatch)
  - [Şablonlar (templates)](#şablonlar-templates)
  - [Alan Şablonları (fieldTemplates)](#alan-şablonları-fieldtemplates)
  - [Kişiler (contacts)](#kişiler-contacts)
  - [Raporlar (reports)](#raporlar-reports)
  - [Gömülü imza (embed)](#gömülü-imza-embed)
  - [Zaman damgası (timestamps)](#zaman-damgası-timestamps)
  - [Hesap (me)](#hesap-me)
- [İmzalı PDF ve sertifika (binary)](#imzalı-pdf-ve-sertifika-binary)
- [Otomatik yeniden deneme](#otomatik-yeniden-deneme)
- [Sayfalama](#sayfalama)
- [Webhook doğrulama](#webhook-doğrulama)
- [Hata yönetimi](#hata-yönetimi)
- [Sık karşılaşılan hatalar](#sık-karşılaşılan-hatalar)
- [Sürüm uyumu](#sürüm-uyumu)
- [Sunucu-taraflı](#️-sunucu-taraflı)
- [İmza sınıfı](#imza-sınıfı)

## Gereksinimler

- PHP **8.1+**, `ext-json` etkin
- `imz_` ile başlayan bir API anahtarı: Panel, Geliştirici, API Anahtarları

## Hızlı başlangıç

```php
<?php

use Imzala\ImzalaClient;

$imzala = new ImzalaClient(getenv('IMZALA_API_KEY'));

// 1) Şablonları listele, birini seç
$templates = $imzala->templates()->list();
$template = $templates->getTemplates()[0];

// 2) Şablondan sözleşme oluştur (imza daveti otomatik gider)
$demand = $imzala->demands()->create([
    'template_id' => $template->getId(),
    'party_mapping' => [
        [
            'template_party_id' => $template->getParties()[0]->getId(),
            'first_name' => 'Ayşe',
            'last_name' => 'Yılmaz',
            'email' => 'ayse@example.com',
            'phone' => '+905551112233',
        ],
    ],
]);
print_r($demand->getSigningUrls()); // her taraf için imzalama linki

// 3) Durumu takip et
$status = $imzala->demands()->get($demand->getId());
echo $status->getStatus() . "\n";

// 4) Tamamlanınca imzalı PDF'i indir
if ((string) $status->getStatus() === 'COMPLETED') {
    file_put_contents('sozlesme.pdf', $imzala->demands()->getPdf($demand->getId()));
}
```

## Yapılandırma

Constructor konumsal veya isimli argümanlarla çağrılabilir:

```php
<?php

use Imzala\ImzalaClient;

$imzala = new ImzalaClient(
    apiKey: getenv('IMZALA_API_KEY'),
    baseUrl: 'https://api-prd.imzala.org', // varsayılan; test için test-api.imzala.org
    timeoutSeconds: 30.0,   // istek başına zaman aşımı (varsayılan 30sn)
    maxRetries: 2,          // güvenli GET'ler için (varsayılan 2, 0 = kapalı)
    retryBaseDelayMs: 300,  // backoff temel gecikmesi (varsayılan 300ms)
);
```

| Seçenek | Tip | Varsayılan | Açıklama |
|---|---|---|---|
| `apiKey` | `string` | (zorunlu) | `imz_<64 hex>` |
| `baseUrl` | `string` | `https://api-prd.imzala.org` | Test: `https://test-api.imzala.org` |
| `timeoutSeconds` | `float` | `30.0` | Guzzle istek zaman aşımı (saniye) |
| `maxRetries` | `int` | `2` | Yalnızca GET'ler; `0` kapatır (bkz. [Otomatik yeniden deneme](#otomatik-yeniden-deneme)) |
| `retryBaseDelayMs` | `int` | `300` | Exponential backoff temel gecikmesi |

API anahtarı ve `Idempotency-Key` değerleri yazdırılabilir ASCII olmalıdır; dosyadan okunan anahtarın sonundaki satır sonu gibi karakterler istek gönderilmeden `ImzalaValidationException` fırlatır (`getStatusCode()` `null`).

`X-Workspace-Id` başlığı için bir seçenek yoktur: organizasyon içinde üretilmiş anahtar kendi organizasyonuna bağlıdır, ayrıca başlık gerekmez. Kişisel anahtarla bir organizasyon adına çalışmak bu sürümde desteklenmez.

## API referansı

Tüm resource metodları `{ success, data }` zarfını açar ve `data`'yı (vendored generated model nesnesi) döndürür; hata durumunda tipli bir `ImzalaException` fırlatır (bkz. [Hata yönetimi](#hata-yönetimi)). Resource'lara metod çağrısıyla erişilir: `$imzala->demands()`, `$imzala->templates()`, `$imzala->fieldTemplates()`, `$imzala->contacts()`, `$imzala->reports()`, `$imzala->embed()`, `$imzala->timestamps()`.

### Sözleşmeler (demands)

| Metod | Açıklama | Retry |
|---|---|---|
| `demands()->create($body, $idempotencyKey?)` | Şablondan sözleşme oluştur + imza daveti gönder (`dispatch_notifications: false` ile sessiz taslak) | Anahtar varsa 429'da 1 kez |
| `demands()->createBulk($body)` | Tek istekte en çok 10 sözleşme; her satır bağımsız, `failed` sayısını ve her satırın durumunu kontrol edin | ❌ POST |
| `demands()->uploadDocument($params)` | Şablonsuz, dosya yükleyerek sözleşme (1 PDF/DOC ya da 1-20 görsel) | Anahtar varsa 429'da 1 kez |
| `demands()->list($status?, $q?, $from?, $to?, $templateId?, $page?, $limit?, $sort?)` | Sözleşme listesi (counts-only, taraf PII'siz); `$sort` biçimi `alan:yön` (ör. `createdAt:desc`) | ✅ GET |
| `demands()->get($id)` | Sözleşme detayı + taraf imza durumu (maskeli) | ✅ GET |
| `demands()->getPdf($id)` | İmzalı sözleşme PDF'i → ham bayt (`string`) | GET (binary, retry yok) |
| `demands()->getDocumentPdf($id, $documentId)` | Çok belgeli zarfta tek belgenin imzalı PDF'i → ham bayt (`string`) | GET (binary, retry yok) |
| `demands()->getCertificate($id, $lang?)` | Tamamlanma sertifikası (PAdES B-T) → ham bayt (`string`) | GET (binary, retry yok) |
| `demands()->getTimeline($id)` | İmza denetim izi (maskeli olaylar) | ✅ GET |
| `demands()->cancel($id, $body?)` | Bekleyen sözleşmeyi iptal et | ❌ POST |
| `demands()->resendParty($id, $partyId)` | Tekil tarafa daveti tekrar gönder | ❌ POST |
| `demands()->delete($id)` | Tamamlanmamış sözleşmeyi sil | ❌ DELETE |
| `demands()->addItems($id, $body)` | Sayfa alanlarını (imza/form) yerleştir (`PAGE_ID_REQUIRED`, `INVALID_ITEM_TYPE`) | ❌ POST |
| `demands()->sendReminder($id, $body?)` | İmzalamamış taraflara hatırlatma (5 dk pencerede `RATE_LIMITED`, `['force' => true]` aşar) | ❌ POST |
| `demands()->dispatch($id, $sendInvitations?)` | Sessiz hazırlanmış sözleşmeyi yayına al, davetleri gönder | ❌ POST |

```php
<?php

// Filtreli liste (isimli argümanlar)
$list = $imzala->demands()->list(status: 'PENDING', limit: 20, sort: 'createdAt:desc');
foreach ($list->getDemands() ?? [] as $d) {
    echo $d->getId() . ' [' . $d->getStatus() . '] '
        . $d->getPartiesSigned() . '/' . $d->getPartiesTotal() . "\n";
}

// İptal (düz array ya da typed request)
$imzala->demands()->cancel($id, ['reason' => 'Anlaşma değişti']);

// Denetim izi (maskeli olaylar)
$timeline = $imzala->demands()->getTimeline($id);

// Tekil tarafa daveti tekrar gönder
$imzala->demands()->resendParty($id, $partyId);

// Tamamlanmamış sözleşmeyi sil (COMPLETED sözleşme 409 döner, panelden silinir)
$imzala->demands()->delete($id);
```

Şablonsuz (dosya yükleyerek) sözleşme oluşturma `FileInput` + `UploadDemandParams` kullanır. SDK içeride geçici bir dosyaya yazıp temizler; çağıran taraf dosya sistemiyle uğraşmaz:

```php
<?php

use Imzala\FileInput;
use Imzala\UploadDemandParams;
use Imzala\UploadPartyInput;

$params = new UploadDemandParams(
    files: [new FileInput(file_get_contents('sozlesme.pdf'), 'sozlesme.pdf', 'application/pdf')],
    parties: [new UploadPartyInput('Ayşe', 'Yılmaz', 'ayse@example.com', '+905551112233')],
);
$demand = $imzala->demands()->uploadDocument($params->withTitle('Hizmet Sözleşmesi'));

// Tekrar-güvenli oluşturma: aynı anahtarla ikinci istek ikinci sözleşme üretmez
$created = $imzala->demands()->create($body, idempotencyKey: "siparis-{$orderId}");

// Alan Şablonu ile yükleme: her tarafta templatePartyId zorunludur
$uploaded = $imzala->demands()->uploadDocument(
    (new UploadDemandParams(
        files: [new FileInput($pdfBytes, 'sozlesme.pdf')],
        parties: [new UploadPartyInput('Ayşe', 'Yılmaz', 'ayse@example.com', null, $partyTemplateId)],
    ))
        ->withFieldTemplateId($fieldTemplateId)
        ->withOnAnchorMiss('block') // çapa bulunamazsa sözleşme oluşturulmaz, kredi düşmez
        ->withIdempotencyKey("yukleme-{$orderId}")
);
```

`UploadDemandParams` diğer seçenekleri: `withSendInvitations()` (bu uçta varsayılan kapalı; `'sms'` SMS ve WhatsApp'ı kapsar, yalnız daraltır) ve `withForce()`.

### Çok belgeli zarf (documents, dispatch)

Bir sözleşme `dispatch_notifications: false` ile sessizce oluşturulur, belgeler eklenir, sonra tek çağrıyla yayına alınır. Belge uçları kimseye bildirim göndermez ve kredi düşmez; kredi yalnız `dispatch` anında düşer.

| Metod | Açıklama | Retry |
|---|---|---|
| `demands()->documents()->list($demandId, $view?)` | Zarftaki belgeler (`'wizard'` atama ve karar sayılarını da verir) | ✅ GET |
| `demands()->documents()->create($demandId, $body)` | Mevcut bir belgeyi zarfa ekle | ❌ POST |
| `demands()->documents()->upload($demandId, $file, $title, $idempotencyKey, $docKind?, $isRequired?)` | Dosya yükleyerek belge ekle; `$idempotencyKey` zorunlu | 429'da 1 kez |
| `demands()->documents()->update($demandId, $docId, $body)` | Başlık / zorunluluk bayrakları | ❌ PATCH |
| `demands()->documents()->delete($demandId, $docId)` | Belgeyi kaldır (son belge silinemez) | ❌ DELETE |
| `demands()->documents()->reorder($demandId, $documentIds)` | Belge sırası (kimlik kümesi birebir eşleşmeli) | ❌ PUT |
| `demands()->documents()->setAssignments($demandId, $docId, $partyIds)` | Belgeyi imzalayacak taraflar | ❌ PUT |
| `demands()->dispatch($demandId, $sendInvitations?)` | Zarfı yayına al (`bool` ya da `'email'` / `'sms'` gibi bir `string`) | ❌ POST |

`upload` aynı `$idempotencyKey` ile tekrar çağrılırsa yeni belge oluşmaz; sunucunun 409 `IDEMPOTENT_REPLAY` yanıtı normal sonuç olarak (mevcut belge) döner. Özellik hesabınızda açık değilse her belge metodu `ENVELOPE_MULTI_DOC_DISABLED` fırlatır; `list` boş liste döndürmez.

### Şablonlar (templates)

| Metod | Açıklama | Retry |
|---|---|---|
| `templates()->list($page?, $limit?)` | Aktif şablonlar (tek sayfa) | ✅ GET |
| `templates()->listAll($page?, $limit?)` | Tüm şablonları gezen generator (bkz. [Sayfalama](#sayfalama)) | ✅ GET |
| `templates()->get($id)` | Şablon detayı + taraflar + doldurulabilir alanlar | ✅ GET |
| `templates()->usage($id)` | API kullanım kılavuzu (curl + JSON örneği) | ✅ GET |
| `templates()->update($id, $body)` | Şablon metadata güncelle (name / description / category) | ❌ PATCH |
| `templates()->delete($id)` | Şablonu sil; kayıt 30 gün saklanır, mevcut sözleşmeler etkilenmez. Aktif (taslak veya imza bekleyen) sözleşmesi olan şablon silinemez (`409 TEMPLATE_IN_USE`) | ❌ DELETE |

```php
<?php

// Metadata güncelle (düz array ya da typed request)
$imzala->templates()->update($templateId, ['name' => 'Yeni Ad', 'category' => 'HR']);

// Şablon sil
$imzala->templates()->delete($templateId);
```

`templates()->list()` Alan Şablonlarını listelemez ve `templates()->get()` bir Alan Şablonu kimliğine `404` döner; onlar için aşağıdaki `fieldTemplates()` kaynağını kullanın.

### Alan Şablonları (fieldTemplates)

| Metod | Açıklama | Retry |
|---|---|---|
| `fieldTemplates()->list($page?, $limit?)` | Alan Şablonları (tek sayfa) | ✅ GET |
| `fieldTemplates()->get($id)` | Alan Şablonu detayı | ✅ GET |
| `fieldTemplates()->previewLayout($id, $files, $onAnchorMiss?)` | Yerleşimi tam bir PDF üzerinde kuru koşumla dener; sözleşme oluşturmaz, kredi harcamaz | ❌ POST |

```php
<?php

$preview = $imzala->fieldTemplates()->previewLayout(
    $fieldTemplateId,
    [new FileInput($pdfBytes, 'sozlesme.pdf')],
    'drop', // verilmezse sunucu 'block' uygular
);
```

### Kişiler (contacts)

| Metod | Açıklama | Retry |
|---|---|---|
| `contacts()->list($q?, $page?, $limit?, $sort?, $companyId?, $archived?)` | Kişiler (tek sayfa); `$limit` 10 ile 100 arası, `$sort` biçimi `-createdAt` (`-` öneki azalan) | ✅ GET |
| `contacts()->listAll(...)` | Tüm kişileri gezen generator | ✅ GET |
| `contacts()->create($body)` | Kişi oluştur (`CONTACT_DUPLICATE`: aynı e-posta veya telefon zaten var) | ❌ POST |

Üretilmiş istemci `$limit` aralığını (10..100) istek gönderilmeden denetler; aralık dışı değer `ImzalaValidationException` fırlatır.

### Raporlar (reports)

```php
<?php

$report = $imzala->reports()->get(); // sözleşme durumlarının toplu sayımı; parametre almaz
```

### Gömülü imza (embed)

```php
<?php

$session = $imzala->embed()->createSession($demandId, $partyId);
echo $session->getEmbedUrl(); // bir <iframe>'e gömün (bkz. @imzala/embed)
```

`$partyId`, sözleşmenin create/get yanıtındaki `signing_urls[].party_id`'sidir. Gömülü imza yalnızca SES/AES üretir (QES değil). Tarayıcı tarafı için [`@imzala/embed`](../embed).

### Zaman damgası (timestamps)

```php
<?php

use Imzala\CreateTimestampParams;

$params = (new CreateTimestampParams(file_get_contents('belge.pdf'), 'belge.pdf'))
    ->withIdempotencyKey('unique-key')       // tekrarları güvenli yapar (5dk pencere)
    ->withDescription('Sözleşme taslağı');

$ts = $imzala->timestamps()->create($params);
echo $ts->getTimestampTime() . ', ' . $ts->getTsaAuthority();

$listing = $imzala->timestamps()->list(status: 'COMPLETED', sort: '-createdAt'); // limit 10..100
$one = $imzala->timestamps()->get($ts->getId());
```

TÜBİTAK KAMU SM TSA ile RFC 3161 zaman damgası (var-olma + değişmezlik kanıtı; imza değildir). SDK içeride geçici bir dosyaya yazıp temizler. Bu uç anahtar başına dakikada 10 istekle sınırlıdır (`RATE_LIMIT_EXCEEDED`).

### Hesap (me)

```php
<?php

$me = $imzala->me();
echo $me->getEmail() . ', kalan kredi: ' . $me->getCredits();
```

`me()`, API anahtarının sahibini döndürür (id, e-posta, ad, workspace, kalan kredi). Hiçbir kapsam (scope) istemez. GET, güvenle otomatik yeniden denenir.

## İmzalı PDF ve sertifika (binary)

`getPdf`, `getDocumentPdf` ve `getCertificate` ham baytları bir PHP `string` olarak döndürür (JSON değil; PHP ikili veriyi bayt-string olarak modeller). GET olsalar da otomatik yeniden denenmezler; hata durumunda diğer metodlar gibi `ImzalaException` fırlatırlar. Diske yazın veya stream'leyin:

```php
<?php

// İmzalı sözleşme PDF'i (yalnızca status === 'COMPLETED' iken)
file_put_contents('sozlesme.pdf', $imzala->demands()->getPdf($id));

// Tamamlanma sertifikası (PAdES B-T), Türkçe/İngilizce
file_put_contents('sertifika.pdf', $imzala->demands()->getCertificate($id, 'tr'));
```

## Otomatik yeniden deneme

Üç kural vardır; hepsi SDK'nın içindedir ve Guzzle'a ayrıca bir tekrar politikası bağlanmaz:

1. **Okumalar (GET):** `demands()->list/get/getTimeline`, `templates()->list/get/usage/listAll`, `fieldTemplates()->list/get`, `contacts()->list/listAll`, `timestamps()->list/get`, `reports()->get()`, `demands()->documents()->list()` ve `me()` 429 veya 5xx aldığında en çok `maxRetries` kez (varsayılan 2) jitter'lı exponential backoff ile yeniden denenir. `0` kapatır. Binary indirmeler (`getPdf` / `getDocumentPdf` / `getCertificate`) bu kapsamda değildir.
2. **`Idempotency-Key` ile gönderilen yazmalar:** `demands()->create($body, $idempotencyKey)`, `uploadDocument()` (`withIdempotencyKey()`), `timestamps()->create()` (`withIdempotencyKey()`) ve `demands()->documents()->upload()` bir 429 sonrasında **tam bir kez** yeniden denenir; sunucu aynı anahtar için ikinci kayıt oluşturmaz. İkinci 429, 5xx ve diğer tüm hatalar doğrudan fırlatılır. Anahtar verilmezse tek denemedir. `maxRetries` bu kuralı etkilemez.
3. **Diğer yazmalar hiç yeniden denenmez:** `createBulk`, `contacts()->create`, `dispatch`, `sendReminder`, `cancel`, `resendParty`, `delete`, `addItems`, `templates()->update/delete`, `embed()->createSession` ve zarf belgesi `create/update/delete/reorder/setAssignments`. Tekrarlanan bir `createBulk` ikinci bir toplu iş, tekrarlanan bir `sendReminder` ikinci bir SMS/e-posta üretir.

Bekleme süresi `Retry-After` başlığından okunur (saniye ya da HTTP tarihi); başlık yoksa backoff gecikmesi uygulanır. **Bekleme tavanı 60 saniyedir:** sunucu daha uzun bir süre isterse SDK beklemek yerine 429'u fırlatır (`ImzalaRateLimitException::getRetryAfter()` süreyi taşır). `NAN`, sonsuz ve negatif `Retry-After` değerleri yok sayılır.

## Sayfalama

`templates()->list()` tek sayfa döner (`{ templates, total, page, limit }`). Tüm şablonları elle sayfalamak yerine `listAll()` generator'ını kullanın:

```php
<?php

foreach ($imzala->templates()->listAll(limit: 50) as $template) {
    echo $template->getId() . ' ' . $template->getName() . "\n";
}
```

`total`'a ulaşınca ya da bir sayfa `limit`'ten az öğe döndürünce durur (sonsuz döngü yok).

## Webhook doğrulama

`verifyWebhook`, imzala.org webhook teslimatının `X-Imzala-Signature-256` başlığını doğrular. Statiktir ve istemci nesnesine gerek duymaz:

```php
<?php

use Imzala\ImzalaClient;

$rawBody = file_get_contents('php://input'); // ham body, json_decode ETMEDEN

$valid = ImzalaClient::verifyWebhook(
    getenv('IMZALA_WEBHOOK_SECRET'),
    $rawBody,
    $_SERVER['HTTP_X_IMZALA_SIGNATURE_256'] ?? null,
);

if (!$valid) {
    http_response_code(401);
    exit;
}

$event = json_decode($rawBody, true);
// $event['type']: demand.created / demand.completed / demand.expired /
//                 party.signed / party.viewed / party.rejected / kyc.completed / kyc.failed
http_response_code(200);
```

`verifyWebhook` exception fırlatmaz; geçersiz/eksik imzada, boş secret'ta veya bozuk başlıkta `false` döner. Body'yi parse edip yeniden serialize etmeyin: imza byte-byte karşılaştırılır (`hash_equals` ile sabit-zamanlı).

## Hata yönetimi

```php
<?php

use Imzala\ImzalaException;
use Imzala\ImzalaAuthException;
use Imzala\ImzalaRateLimitException;
use Imzala\ImzalaValidationException;

try {
    $imzala->demands()->get($demandId);
} catch (ImzalaRateLimitException $e) {
    echo "Rate limit: {$e->getRetryAfter()} sn sonra tekrar dene";
} catch (ImzalaAuthException $e) {
    echo 'API anahtarı geçersiz veya yetkisiz';
} catch (ImzalaValidationException $e) {
    echo 'İstek doğrulanamadı: ' . $e->getBody();
} catch (ImzalaException $e) {
    echo 'İmzala API hatası: ' . $e->getStatusCode() . ' ' . $e->getMessage();
}
```

Tüm hatalar `ImzalaException`'dan türer (`getStatusCode()`, `getBody()`, `getErrorCode()`, `getCodeDescription()` ortak). 401/403 → `ImzalaAuthException`, 429 → `ImzalaRateLimitException` (`getRetryAfter()` saniye, `getRateLimit()`), 422 → `ImzalaValidationException`. Diğer statüler (400, 404, 409, 500, ...) doğrudan `ImzalaException` olarak fırlatılır. İstek gönderilmeden yakalanan yerel doğrulama hataları (geçersiz API anahtarı veya `Idempotency-Key` karakteri, üretilmiş istemcinin parametre denetimleri) de `ImzalaValidationException`'dır; `getStatusCode()` `null` döner.

- **`getErrorCode()`:** sunucunun makine-okunur kodu. `{"error": "metin", "code": "KOD"}` biçimindeki gövdelerde `code` alanından okunur; yalnız metin taşıyan gövdede `null` kalır.
- **`getCodeDescription()`:** kod için katalogdaki tek satırlık Türkçe açıklama. Katalog `Imzala\ErrorCodes::CODES` (73 kod); `ErrorCodes::describe($code)` ve `ErrorCodes::isKnown($code)` ile sorgulanır. Katalogda olmayan bir kod açıklamasız fırlatılır, yani yeni bir sunucu sürümü eski SDK'yı kırmaz.
- **`ImzalaRateLimitException::getRateLimit()`:** standart `RateLimit-Limit` / `RateLimit-Remaining` / `RateLimit-Reset` / `RateLimit-Policy` başlıkları (`RateLimitInfo`). Birden çok sınır 429 döndürür ve farklı kod taşır; bkz. aşağıdaki tablo.

```php
<?php

use Imzala\ErrorCodes;

echo ErrorCodes::describe('TEMPLATE_IN_USE'); // Şablonun aktif (taslak veya imza bekleyen) sözleşmesi olduğu için silinemez.
echo count(ErrorCodes::CODES);                // 73
```

## Sık karşılaşılan hatalar

| Durum | Kod | Ne yapmalı |
|---|---|---|
| 400 | `INVALID_PAGE` | `page` 1 veya daha büyük bir tam sayı olmalı |
| 400 | `INVALID_EXPIRY_DATE` | `expiry_date` geçerli bir takvim tarihi olmalı |
| 400 | `PAGE_ID_REQUIRED` | Alan yerleştirmede her öğede tam sayı `page_id` zorunlu |
| 400 | `INVALID_ITEM_TYPE` | Desteklenen alan tiplerinden birini kullanın (hata mesajı listeler) |
| 404 | (kodsuz) | Alan Şablonu kimliğiyle `templates()->get()` çağrıldı; Alan Şablonları için `fieldTemplates()->get()` kullanın (iki kavram ayrıdır) |
| 409 | `TEMPLATE_IN_USE` | Şablonun taslak veya imza bekleyen sözleşmesi var; tamamlanınca silinebilir |
| 409 | `DEMAND_NOT_DISPATCHABLE` | Sözleşme tamamlanmış veya iptal edilmiş; tekrar gönderilemez |
| 409 | `ENVELOPE_MULTI_DOC_DISABLED` | Çok belgeli zarf bu hesapta açık değil; `demands()->documents()->*` bu kodu fırlatır |
| 409 | `IDEMPOTENCY_KEY_REUSED` | Aynı anahtar farklı gövdeyle kullanıldı; yeni sözleşme için yeni anahtar üretin |
| 409 | `IDEMPOTENCY_UNVERIFIABLE` | Anahtar daha önce sözleşme üretti ama isteğin aynı olduğu doğrulanamadı; gövdedeki `demand_id` ile durumu sorgulayın, yeni anahtarla körlemesine tekrarlamayın |
| 409 | `DUPLICATE_SUSPECTED` | Anahtarsız istek son 10 dakikada gönderilmiş aynı içerikle eşleşti; kasten tekrarlamak için `force` gönderin |
| 422 | `FIELD_LAYOUT_UNRESOLVED` | Alan yerleşimi belgeye uygulanamadı; sözleşme oluşturulmadı, kredi düşülmedi. `withOnAnchorMiss()` seçin veya belgeyi düzeltin |
| 429 | `RATE_LIMIT_EXCEEDED` | Genel sınır: anahtar başına dakikada 60 istek (zaman damgası ucunda 10). `Retry-After` kadar bekleyin |
| 429 | `TOO_MANY_REQUESTS` | Uca özgü sınır: belge yüklemede dakikada 30, davet tekrarı ve zarf gönderiminde saatte 300 |
| 429 | `RECIPIENT_RESEND_LIMIT` | Aynı alıcıya saatte en çok 3, günde en çok 10 davet tekrarı |

Sözleşme, şablon ve Alan Şablonu listelerinde `limit` üst sınırı 100'dür; daha büyük bir değer hata vermez, sessizce 100'e kırpılır. Kişi ve zaman damgası listelerinde `limit` 10 ile 100 arasında olmalıdır (üretilmiş istemci bunu yerelde denetler).

## Sürüm uyumu

| imzala/imzala-php | Konuştuğu API | Durum |
|---|---|---|
| 1.0.0 | v1 (`1.8.14`) | Güncel |
| 0.x | v1 (`1.7.x`) | Bakım dışı; 1.0.0'a yükseltin |

İmzala dış API'si **v1**'dir ve geriye dönük uyumludur: yeni alanlar opsiyonel, yeni davranışlar
opt-in, varsayılan davranış eskisidir. Kırıcı bir değişiklik gerekirse yeni bir major API sürümü
yayımlanır; eski sürüm duyurudan sonra **12 ay** çalışmaya devam eder.

> Not: Bu paket, diğer dört dil SDK'sının (Node/Python/.NET/Java) `ImzalaError` isimlendirmesinden farklı olarak PHP konvansiyonuna uyup `ImzalaException` / `Imzala*Exception` isimlerini kullanır; davranış (4'lü taksonomi, aynı alanlar) aynıdır.

## ⚠️ Sunucu-taraflı

Bu paket **yalnızca sunucuda** kullanılır. API anahtarı sızarsa hesabınızdaki tüm sözleşme/şablon/zaman damgası işlemlerine erişilir. API anahtarınızı tarayıcı JavaScript'ine veya mobil uygulamaya asla gömmeyin. Tarayıcıda imza için [`@imzala/embed`](../embed) / [`@imzala/embed-react`](../embed-react).

## İmza sınıfı

İmzala **dijital imza (SES)** üretir; her imza zaman damgalıdır. Nitelikli/güvenli elektronik imza (QES) DEĞİLDİR. Gömülü imza da SES/AES üretir.

## Daha fazla

- Tam API referansı: [api-docs.imzala.org](https://api-docs.imzala.org)
- Kullanım kılavuzu: [imzala.org/docs/api-sozlesme-yasam-dongusu](https://imzala.org/docs/api-sozlesme-yasam-dongusu)
- Çalışan örnekler: [`examples/php`](../../examples/php) (altı senaryo)
- Değişiklik günlüğü: [CHANGELOG.md](./CHANGELOG.md)
- [Monorepo README](../../README.md)
