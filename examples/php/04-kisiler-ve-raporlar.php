<?php

declare(strict_types=1);

// Adres defterini sayfalı gezme, kişi oluşturma ve sözleşme durum sayımlarını okuma.
// Kapsam: contacts:read + contacts:write (kişi oluşturma) + demands:read (rapor).
// Kredi harcamaz. Kişi oluşturma veri değiştirir (yorumda).

require __DIR__ . '/../../packages/php/vendor/autoload.php';

use Imzala\ImzalaClient;
use Imzala\ImzalaException;

$baseUrl = getenv('IMZALA_BASE_URL');
$imzala = $baseUrl
    ? new ImzalaClient((string) getenv('IMZALA_API_KEY'), $baseUrl)
    : new ImzalaClient((string) getenv('IMZALA_API_KEY'));

// 1) Sayfalı gezinme: listAll sayfaları şeffaf biçimde dolaşır (limit 10..100).
$sayac = 0;
foreach ($imzala->contacts()->listAll(limit: 50) as $kisi) {
    $sayac++;
    if ($sayac <= 5) {
        echo "{$kisi->getId()} · {$kisi->getFirstName()} {$kisi->getLastName()}\n";
    }
}
echo "Toplam kişi: {$sayac}\n";

// 2) Arama: tek sayfa, metin filtresiyle.
$arama = $imzala->contacts()->list(q: 'ayse', limit: 10);
echo '"ayse" için ' . ($arama->getTotal() ?? 0) . " sonuç\n";

// 3) Kişi oluştur. Aynı e-posta veya telefona sahip aktif bir kişi varsa 409
//    CONTACT_DUPLICATE döner. Bu uçta idempotency anahtarı yoktur; SDK isteği
//    hiçbir koşulda yinelemez. Kişisel veriyi buraya yazan, o veri için veri
//    sorumlusudur (aydınlatma yükümlülüğü size aittir).
//
// try {
//     $yeni = $imzala->contacts()->create([
//         'first_name' => 'Ayşe',
//         'last_name' => 'Yılmaz',
//         'email' => 'ayse@example.com',
//         'phone' => '+905551112233',
//     ]);
//     echo "Kişi oluşturuldu: {$yeni->getId()}\n";
// } catch (ImzalaException $e) {
//     if ($e->getErrorCode() === 'CONTACT_DUPLICATE') {
//         echo "Kişi zaten var: {$e->getCodeDescription()}\n";
//     } else {
//         throw $e;
//     }
// }

// 4) Rapor: sözleşme durumlarının toplu sayımı. Kişisel veri içermez.
$rapor = $imzala->reports()->get();
$c = $rapor->getContracts();
echo "Sözleşmeler: toplam {$c->getTotal()} · bekleyen {$c->getPending()} · tamamlanan {$c->getCompleted()}\n";
echo "  iptal {$c->getCancelled()} · süresi dolan {$c->getExpired()} · bu ay oluşturulan {$c->getThisMonth()}\n";
