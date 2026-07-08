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

> **Yayın durumu:** Paket Packagist'e yayın için hazırlanıyor (yayın ve kapsam kararları: [RELEASING.md](../../RELEASING.md)). SDK, canlı prod API'sine (`api-prd.imzala.org`) karşı çalışır.

## İçindekiler

- [Gereksinimler](#gereksinimler)
- [Hızlı başlangıç](#hızlı-başlangıç)
- [Yapılandırma](#yapılandırma)
- [API referansı](#api-referansı)
  - [Sözleşmeler (demands)](#sözleşmeler-demands)
  - [Şablonlar (templates)](#şablonlar-templates)
  - [Gömülü imza (embed)](#gömülü-imza-embed)
  - [Zaman damgası (timestamps)](#zaman-damgası-timestamps)
  - [Hesap (me)](#hesap-me)
- [İmzalı PDF ve sertifika (binary)](#imzalı-pdf-ve-sertifika-binary)
- [Otomatik yeniden deneme](#otomatik-yeniden-deneme)
- [Sayfalama](#sayfalama)
- [Webhook doğrulama](#webhook-doğrulama)
- [Hata yönetimi](#hata-yönetimi)
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
            'first_name' => 'Ahmet',
            'last_name' => 'Yılmaz',
            'email' => 'ahmet@example.com',
            'phone' => '+905301112233',
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
| `maxRetries` | `int` | `2` | Yalnızca idempotent GET'ler; `0` kapatır |
| `retryBaseDelayMs` | `int` | `300` | Exponential backoff temel gecikmesi |

## API referansı

Tüm resource metodları `{ success, data }` zarfını açar ve `data`'yı (vendored generated model nesnesi) döndürür; hata durumunda tipli bir `ImzalaException` fırlatır (bkz. [Hata yönetimi](#hata-yönetimi)). Resource'lara metod çağrısıyla erişilir: `$imzala->demands()`, `$imzala->templates()`, `$imzala->embed()`, `$imzala->timestamps()`.

### Sözleşmeler (demands)

| Metod | Açıklama | Retry |
|---|---|---|
| `demands()->create($body)` | Şablondan sözleşme oluştur + imza daveti gönder | ❌ POST |
| `demands()->uploadDocument($params)` | Şablonsuz, dosya yükleyerek sözleşme (1 PDF/DOC ya da 1-20 görsel) | ❌ POST |
| `demands()->list($status?, $q?, $from?, $to?, $templateId?, $page?, $limit?, $sort?)` | Sözleşme listesi (counts-only, taraf PII'siz) | ✅ GET |
| `demands()->get($id)` | Sözleşme detayı + taraf imza durumu (maskeli) | ✅ GET |
| `demands()->getPdf($id)` | İmzalı sözleşme PDF'i → ham bayt (`string`) | ✅ GET |
| `demands()->getCertificate($id, $lang?)` | Tamamlanma sertifikası (PAdES B-T) → ham bayt (`string`) | ✅ GET |
| `demands()->getTimeline($id)` | İmza denetim izi (maskeli olaylar) | ✅ GET |
| `demands()->cancel($id, $body?)` | Bekleyen sözleşmeyi iptal et | ❌ POST |
| `demands()->resendParty($id, $partyId)` | Tekil tarafa daveti tekrar gönder | ❌ POST |
| `demands()->delete($id)` | Tamamlanmamış sözleşmeyi sil | ❌ DELETE |
| `demands()->addItems($id, $body)` | Sayfa alanlarını (imza/form) yerleştir | ❌ POST |
| `demands()->sendReminder($id, $body?)` | İmzalamamış taraflara hatırlatma | ❌ POST |

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
    parties: [new UploadPartyInput('Ada', 'Lovelace', 'ada@example.com', '+905551234567')],
);
$demand = $imzala->demands()->uploadDocument($params->withTitle('Hizmet Sözleşmesi'));
```

### Şablonlar (templates)

| Metod | Açıklama | Retry |
|---|---|---|
| `templates()->list($page?, $limit?)` | Aktif şablonlar (tek sayfa) | ✅ GET |
| `templates()->listAll($page?, $limit?)` | Tüm şablonları gezen generator (bkz. [Sayfalama](#sayfalama)) | ✅ GET |
| `templates()->get($id)` | Şablon detayı + taraflar + doldurulabilir alanlar | ✅ GET |
| `templates()->usage($id)` | API kullanım kılavuzu (curl + JSON örneği) | ✅ GET |
| `templates()->update($id, $body)` | Şablon metadata güncelle (name / description / category) | ❌ PATCH |
| `templates()->delete($id)` | Şablon sil (soft-delete; mevcut sözleşmeler etkilenmez) | ❌ DELETE |

```php
<?php

// Metadata güncelle (düz array ya da typed request)
$imzala->templates()->update($templateId, ['name' => 'Yeni Ad', 'category' => 'HR']);

// Şablon sil
$imzala->templates()->delete($templateId);
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
```

TÜBİTAK KAMU SM TSA ile RFC 3161 zaman damgası (var-olma + değişmezlik kanıtı; imza değildir). SDK içeride geçici bir dosyaya yazıp temizler.

### Hesap (me)

```php
<?php

$me = $imzala->me();
echo $me->getEmail() . ', kalan kredi: ' . $me->getCredits();
```

`me()`, API anahtarının sahibini döndürür (id, e-posta, ad, workspace, kalan kredi). GET, güvenle otomatik yeniden denenir.

## İmzalı PDF ve sertifika (binary)

`getPdf` ve `getCertificate` ham baytları bir PHP `string` olarak döndürür (JSON değil; PHP ikili veriyi bayt-string olarak modeller). Diske yazın veya stream'leyin:

```php
<?php

// İmzalı sözleşme PDF'i (yalnızca status === 'COMPLETED' iken)
file_put_contents('sozlesme.pdf', $imzala->demands()->getPdf($id));

// Tamamlanma sertifikası (PAdES B-T), Türkçe/İngilizce
file_put_contents('sertifika.pdf', $imzala->demands()->getCertificate($id, 'tr'));
```

## Otomatik yeniden deneme

Yalnızca **GET (okuma)** uçları, yani `demands()->list/get/getPdf/getCertificate/getTimeline`, `templates()->list/get/usage/listAll` ve `me()`, 429 (`Retry-After`'a uyarak) veya 5xx aldığında jitter'lı exponential backoff ile yeniden denenir.

**POST/PATCH/DELETE (yazma) uçları ASLA yeniden denenmez** ve bu yapılandırılamaz: bir `demands()->create` tekrarı mükerrer sözleşme oluşturur. Retry mantığı yapısal olarak yalnızca GET'e bağlıdır (`src/Http.php` içindeki `unwrapRetryableGet()`; hiçbir yazma metodu bu fonksiyona yönlendirilmez).

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
// $event['type']: demand.created / demand.completed / demand.cancelled ...
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

Tüm hatalar `ImzalaException`'dan türer (`getStatusCode()`, `getBody()`, `getErrorCode()` ortak). 401/403 → `ImzalaAuthException`, 429 → `ImzalaRateLimitException` (`getRetryAfter()` saniye), 422 → `ImzalaValidationException`. Diğer statüler (400, 404, 409, 500, ...) doğrudan `ImzalaException` olarak fırlatılır.

> Not: Bu paket, diğer dört dil SDK'sının (Node/Python/.NET/Java) `ImzalaError` isimlendirmesinden farklı olarak PHP konvansiyonuna uyup `ImzalaException` / `Imzala*Exception` isimlerini kullanır; davranış (4'lü taksonomi, aynı alanlar) aynıdır.

## ⚠️ Sunucu-taraflı

Bu paket **yalnızca sunucuda** kullanılır. API anahtarı sızarsa hesabınızdaki tüm sözleşme/şablon/zaman damgası işlemlerine erişilir. API anahtarınızı tarayıcı JavaScript'ine veya mobil uygulamaya asla gömmeyin. Tarayıcıda imza için [`@imzala/embed`](../embed) / [`@imzala/embed-react`](../embed-react).

## İmza sınıfı

İmzala **dijital imza (SES)** üretir; her imza zaman damgalıdır. Nitelikli/güvenli elektronik imza (QES) DEĞİLDİR. Gömülü imza da SES/AES üretir.

## Daha fazla

- Tam API referansı: [api-docs.imzala.org](https://api-docs.imzala.org)
- Kullanım kılavuzu: [imzala.org/docs/api-sozlesme-yasam-dongusu](https://imzala.org/docs/api-sozlesme-yasam-dongusu)
- Çalışan örnek: [`examples/php/lifecycle.php`](../../examples/php/lifecycle.php)
- [Monorepo README](../../README.md) · [RELEASING.md](../../RELEASING.md)
