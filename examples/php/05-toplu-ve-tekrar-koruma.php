<?php

declare(strict_types=1);

// Toplu sözleşme oluşturma (10'arlı parçalar), Idempotency-Key ve istek limiti (429) davranışı.
// Kapsam: templates:read + demands:write. Oluşturma adımları kredi harcar (yorumda).
// Limit gösterimi yalnız salt-okuma isteği (şablon listesi) kullanır, kredi harcamaz.

require __DIR__ . '/../../packages/php/vendor/autoload.php';

use Imzala\ErrorCodes;
use Imzala\ImzalaClient;
use Imzala\ImzalaRateLimitException;

// GET isteklerinin 429 sonrası otomatik yinelenmesi kapatıldı (maxRetries: 0):
// 4. adımda 429'u kendimiz görmek istiyoruz. Üretimde varsayılanı (2) bırakın.
$baseUrl = getenv('IMZALA_BASE_URL') ?: ImzalaClient::DEFAULT_BASE_URL;
$imzala = new ImzalaClient((string) getenv('IMZALA_API_KEY'), $baseUrl, maxRetries: 0);

// 1) Şablon: IMZALA_TEMPLATE_ID verilmişse o, yoksa listedeki ilk şablon.
$templateId = getenv('IMZALA_TEMPLATE_ID') ?: null;
if ($templateId === null) {
    $templates = $imzala->templates()->list(limit: 1);
    $templateId = ($templates->getTemplates()[0] ?? null)?->getId();
}
if ($templateId === null) {
    echo "Aktif şablon yok. Panelden bir şablon oluşturun.\n";
    exit(0);
}
$detail = $imzala->templates()->get($templateId);
$rolId = ($detail->getParties()[0] ?? null)?->getId();
echo "Şablon: {$detail->getName()} · ilk rol: {$rolId}\n";

// 2) Tek sözleşmede tekrar koruması. create bir Idempotency-Key kabul eder:
//    aynı anahtar + aynı gövde → aynı sözleşme döner, ikinci kayıt oluşmaz.
//    Aynı anahtar + farklı gövde → 409 IDEMPOTENCY_KEY_REUSED.
//    Anahtarlı bir istek 429 alırsa SDK Retry-After kadar bekleyip bir kez yineler;
//    anahtarsız istek hiç yinelenmez (yineleme ikinci sözleşme yaratırdı).
//
// $anahtar = bin2hex(random_bytes(16)); // ör. kendi sipariş numaranız da olabilir
// $demand = $imzala->demands()->create(
//     [
//         'template_id' => $detail->getId(),
//         'party_mapping' => [['template_party_id' => $rolId, 'first_name' => 'Ayşe', 'last_name' => 'Yılmaz', 'email' => 'ayse@example.com']],
//     ],
//     $anahtar,
// );
// echo "Sözleşme: {$demand->getId()}\n";

// 3) Toplu oluşturma: tek istekte en fazla 10 satır (BULK_MAX_10). Daha uzun
//    listeyi 10'arlı parçalara bölün. Her satır bağımsız bir sözleşmedir; kısmi
//    başarı 200 ile döner, satır durumunu results[i].status'tan okuyun.
//    Bu uçta Idempotency-Key YOKTUR; SDK toplu isteği 429 dahil hiçbir koşulda
//    yinelemez, çünkü sunucu anahtarı tanımadığından yineleme aynı sözleşmeleri
//    ikinci kez yaratır ve krediyi iki kez düşerdi. Parça başarısız olursa
//    demands()->list() ile durumu doğrulayıp yalnız eksik satırları yeniden gönderin.
//
// $alicilar = [
//     ['template_party_id' => $rolId, 'first_name' => 'Ayşe', 'last_name' => 'Yılmaz', 'email' => 'ayse@example.com'],
//     ['template_party_id' => $rolId, 'first_name' => 'Mehmet', 'last_name' => 'Yılmaz', 'email' => 'mehmet@example.com'],
// ];
// foreach (array_chunk($alicilar, 10) as $i => $parca) {
//     $sonuc = $imzala->demands()->createBulk([
//         'template_id' => $detail->getId(),
//         'options' => ['dispatch_notifications' => false], // önce taslak oluştur, gözden geçir, sonra gönder
//         'rows' => array_map(fn (array $alici) => ['party_mapping' => [$alici]], $parca),
//     ]);
//     echo 'Parça ' . ($i + 1) . ": oluşan {$sonuc->getCreated()}, başarısız {$sonuc->getFailed()}\n";
//     foreach ($sonuc->getResults() ?? [] as $satir) {
//         echo "  satır {$satir->getRowIndex()}: {$satir->getStatus()} " . ($satir->getDemandId() ?? $satir->getError() ?? '') . "\n";
//     }
// }

// 4) İstek limiti. Anahtar başına dakikada 60 istek (anahtar bazında düşürülebilir).
//    Aşımda 429 + RATE_LIMIT_EXCEEDED döner.
//
//    NOT: limiti ardışık isteklerle göremezsiniz: her istek yaklaşık bir saniye
//    sürdüğü için 65 ardışık çağrının hepsi 200 döner (60 saniyelik pencere
//    aradan sıfırlanır). Limit ancak eşzamanlı bir patlamayla görünür. PHP'de
//    tek süreç istekleri ardışık gönderir; patlama için bu betiği aynı anda
//    birden çok süreçte (ör. 20 kez arka planda) çalıştırın.
try {
    for ($i = 0; $i < 70; $i++) {
        $imzala->templates()->list(limit: 1);
    }
    echo "Limit tetiklenmedi.\n";
} catch (ImzalaRateLimitException $e) {
    echo 'Limit aşıldı: ' . (ErrorCodes::describe($e->getErrorCode()) ?? $e->getErrorCode()) . "\n";
    echo "  {$e->getRetryAfter()} saniye sonra tekrar deneyin.\n";
    $info = $e->getRateLimit();
    if ($info !== null) {
        echo "  Politika: {$info->policy} · limit: {$info->limit} · kalan: {$info->remaining}\n";
    }
}
