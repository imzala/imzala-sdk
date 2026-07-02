# imzala/imzala-php

İmzala dijital imza platformu resmi PHP SDK'sı.

> ⚠️ Geliştirme aşamasında, yayın öncesi avukat onayı bekliyor. Kapsam ve
> yayın kararları için monorepo kökündeki [RELEASING.md](../../RELEASING.md).

## Kurulum

```bash
composer require imzala/imzala-php
```

PHP 8.1+ gerektirir.

## Hızlı başlangıç

```php
<?php

use Imzala\ImzalaClient;

$imzala = new ImzalaClient(getenv('IMZALA_API_KEY'));

// Aktif şablonlarını listele
$templateList = $imzala->templates()->list();

// Bir şablondan yeni sözleşme (demand) oluştur
$demand = $imzala->demands()->create([
    'template_id' => $templateList->getTemplates()[0]->getId(),
    'party_mapping' => [
        [
            'template_party_id' => $templateList->getTemplates()[0]->getParties()[0]->getId(),
            'first_name' => 'Ahmet',
            'last_name' => 'Yılmaz',
            'email' => 'ahmet@example.com',
        ],
    ],
]);

print_r($demand->getSigningUrls()); // her taraf için imzalama linki
```

`baseUrl` varsayılan olarak `https://api-prd.imzala.org`'dur; test ortamı
için `new ImzalaClient($apiKey, 'https://test-api.imzala.org')`. Otomatik
yeniden deneme (`maxRetries`, `retryBaseDelayMs`) aşağıda ayrı bölümde.

## Kaynaklar

- `$imzala->templates()->list($page = null, $limit = null)` /
  `->get($id)` / `->usage($id)` / `->listAll($page = null, $limit = null)`
  (bkz. aşağıdaki sayfalama notu)
- `$imzala->demands()->create($body)` / `->get($id)` /
  `->addItems($id, $body)` /
  `->uploadDocument(new UploadDemandParams($files, $parties))` /
  `->sendReminder($id, $body = null)`
- `$imzala->embed()->createSession($demandId, $partyId)`: bkz. aşağıdaki
  gömülü imza notu
- `$imzala->timestamps()->create(new CreateTimestampParams($content, $fileName))`
- `$imzala->me()`: API anahtarının sahibi (id, e-posta, workspace, kalan kredi)

`create`/`addItems` metodları vendored generated model sınıfı ya da düz
associative array (snake_case anahtarlarla) kabul eder. Dosya yükleyen
metodlar `FileInput($content, $fileName, $contentType = null)` kullanır;
SDK içeride geçici bir dosyaya yazıp temizler, çağıran taraf dosya
sistemiyle uğraşmaz.

## Otomatik yeniden deneme (safe auto-retry)

`templates()->list/get/usage`, `demands()->get` ve `me()` — yani **sadece
GET** (okuma) uçları — 429 (rate limit, `Retry-After`'a uyarak) veya 5xx
(sunucu hatası) aldığında, jitter'lı exponential backoff ile otomatik
olarak yeniden denenir:

```php
<?php

use Imzala\ImzalaClient;

$imzala = new ImzalaClient(
    apiKey: getenv('IMZALA_API_KEY'),
    maxRetries: 2, // varsayılan 2, 0 = kapalı
    retryBaseDelayMs: 300, // varsayılan 300ms (backoff: 300ms, 600ms, ...)
);
```

**Güvenlik:** `demands()->create()`, `sendReminder()`, `uploadDocument()`,
`addItems()`, `embed()->createSession()`, `timestamps()->create()` gibi
**yazma** (POST) uçları **asla otomatik yeniden denenmez** — bu davranış
yapılandırılamaz. Bir `demands()->create()` çağrısının otomatik tekrarı,
mükerrer bir sözleşme oluşturur; bu yüzden retry mantığı sadece GET
isteklerine bağlıdır (caller tarafından açılıp kapatılabilecek bir bayrak
değildir — bkz. `src/Http.php`'deki `unwrapRetryableGet()`, hiçbir yazma
metodu bu fonksiyona yönlendirilmez).

## Sayfalama iterator'ı

`templates()->list()` tek sayfa döner (`{templates, total, page, limit}`).
Tüm şablonları sayfa sayfa elle çekmek yerine `listAll()` generator'ını
kullanabilirsiniz — sayfaları şeffaf şekilde gezer:

```php
<?php

foreach ($imzala->templates()->listAll(limit: 50) as $template) {
    echo $template->getId() . ' ' . $template->getName() . "\n";
}
```

`total` alanına ulaşıldığında veya bir sayfa `limit`'ten az öğe
döndürdüğünde durur — sonsuz döngüye girmez. `list()` (tek sayfa)
davranışı değişmedi.

## Webhook doğrulama

```php
<?php

use Imzala\ImzalaClient;

$rawBody = file_get_contents('php://input'); // ham body, json_decode ETMEDEN önce

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
// ... $event['type']'a göre işle
http_response_code(200);
```

`verifyWebhook` (statik) asla exception fırlatmaz; geçersiz/eksik imzada
`false` döner. Body'yi decode edip yeniden encode etmeyin, imza doğrulaması
byte-byte karşılaştırma yapar.

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

Tüm hatalar `ImzalaException`'dan türer (`getStatusCode()`, `getBody()`,
`getErrorCode()` ortak); 401/403 → `ImzalaAuthException`, 429 →
`ImzalaRateLimitException` (`getRetryAfter()` saniye), 422 →
`ImzalaValidationException`.

> Not: bu paket, diğer dört dil SDK'sının (Node/Python/.NET/Java)
> `ImzalaError`/`ImzalaError`-türevi isimlendirmesinden farklı olarak PHP
> konvansiyonuna uyup `ImzalaException`/`Imzala*Exception` isimlerini
> kullanır; davranış (4'lü taksonomi, aynı alanlar) aynıdır.

## ⚠️ Sunucu-taraflı

Bu paket **sadece sunucuda** kullanılır. API anahtarınızı tarayıcı
JavaScript'ine veya mobil uygulamaya asla gömmeyin: sızarsa hesabınızdaki
tüm sözleşme/şablon/zaman damgası işlemlerine erişim kazanılır. Tarayıcıda
imza almak için [`@imzala/embed`](../embed) veya
[`@imzala/embed-react`](../embed-react) kullanın.

## İmza sınıfı notu

İmzala dijital imza (SES) üretir; her imza zaman damgalıdır. Nitelikli/
güvenli elektronik imza (QES) DEĞİLDİR.

`$imzala->embed()->createSession(...)` şu an **yalnızca test ortamında**
çalışır; gömülü imza özelliği henüz avukat onaylı prod-canlı değildir.
Kapsam kararı [RELEASING.md](../../RELEASING.md)'de.

## Daha fazla

- [Monorepo README](../../README.md): tüm paketler + genel desen
- [RELEASING.md](../../RELEASING.md): sürüm yayınlama, kapsam kararları
