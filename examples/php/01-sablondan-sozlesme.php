<?php

declare(strict_types=1);

// Şablondan sözleşme oluşturma, davet linklerini alma ve webhook imzasını doğrulama.
// Kapsam: templates:read + demands:read; sözleşme oluşturma için demands:write.
// Sözleşme oluşturma kredi harcar (yorumda). Diğer adımlar kredi harcamaz.

require __DIR__ . '/../../packages/php/vendor/autoload.php';

use Imzala\ImzalaClient;

$baseUrl = getenv('IMZALA_BASE_URL');
$imzala = $baseUrl
    ? new ImzalaClient((string) getenv('IMZALA_API_KEY'), $baseUrl)
    : new ImzalaClient((string) getenv('IMZALA_API_KEY')); // boşsa SDK varsayılanı (api-prd.imzala.org)

// 1) Hesap ve kalan kredi. Bu uç kapsam istemez.
$me = $imzala->me();
$kredi = $me->getCredits() !== null ? $me->getCredits()->getRemaining() : '?';
echo "Hesap: {$me->getEmail()} · kalan kredi: {$kredi}\n";

// 2) Şablonu seç: IMZALA_TEMPLATE_ID verilmişse o, yoksa listedeki ilk şablon.
$templateId = getenv('IMZALA_TEMPLATE_ID') ?: null;
if ($templateId === null) {
    $templates = $imzala->templates()->list(limit: 10);
    $templateId = ($templates->getTemplates()[0] ?? null)?->getId();
}
if ($templateId === null) {
    echo "Aktif şablon yok. Panelden bir şablon oluşturun.\n";
    exit(0);
}

// 3) Şablonun rollerini ve değişkenlerini oku. party_mapping bu rollere göre
//    kurulur: her rol için template_party_id zorunludur.
$detail = $imzala->templates()->get($templateId);
echo "Şablon: {$detail->getName()} · {$detail->getPagesCount()} sayfa\n";
foreach ($detail->getParties() ?? [] as $party) {
    $optional = $party->getIsRequired() ? '' : ' · isteğe bağlı';
    echo "  Rol {$party->getOrder()}: " . ($party->getLabel() ?? '(adsız)') . " · {$party->getId()}{$optional}\n";
}
foreach ($detail->getVariables() ?? [] as $variable) {
    echo "  Değişken: {$variable->getSlug()} ({$variable->getItemType()})\n";
}

// 4) Sözleşmeyi oluştur. Kredi harcar; açmadan önce test ortamını kullanın.
//    İkinci parametre Idempotency-Key: ağ koptuğunda aynı anahtarla tekrar denemek
//    ikinci bir sözleşme yaratmaz; SDK bir 429 sonrasında anahtarlı isteği bir kez yineler.
//    Kişiler kurgusaldır; her rol için template_party_id şablon detayından gelir.
//
// $kisiler = [
//     ['first_name' => 'Ayşe', 'last_name' => 'Yılmaz', 'email' => 'ayse@example.com', 'phone' => '+905551112233'],
//     ['first_name' => 'Mehmet', 'last_name' => 'Yılmaz', 'email' => 'mehmet@example.com', 'phone' => '+905551112244'],
// ];
// $partyMapping = [];
// foreach ($detail->getParties() ?? [] as $i => $party) {
//     $partyMapping[] = ['template_party_id' => $party->getId()] + $kisiler[$i % count($kisiler)];
// }
// $demand = $imzala->demands()->create(
//     [
//         'template_id' => $detail->getId(),
//         'title' => 'Hizmet Sözleşmesi',
//         'expiry_date' => '2026-12-31',
//         'party_mapping' => $partyMapping,
//     ],
//     bin2hex(random_bytes(16)),
// );
// echo "Sözleşme: {$demand->getId()} · durum: {$demand->getStatus()}\n";
// foreach ($demand->getSigningUrls() ?? [] as $party) {
//     echo "  {$party->getFirstName()} {$party->getLastName()} → {$party->getSigningUrl()}\n";
// }

// 5) Son sözleşmelerin durumu (yalnız sayımlar, kişisel veri içermez).
$demands = $imzala->demands()->list(limit: 5, sort: 'createdAt:desc');
foreach ($demands->getDemands() ?? [] as $d) {
    echo "{$d->getId()} · {$d->getStatus()} · {$d->getPartiesSigned()}/{$d->getPartiesTotal()} imza\n";
}

// 6) Webhook imzası. Gövde HAM olarak okunmalı: JSON'u ayrıştırıp yeniden
//    serileştirmek baytları değiştirir ve imza tutmaz. Bir HTTP işleyicide
//    file_get_contents('php://input') ham gövdeyi verir.
$secret = getenv('IMZALA_WEBHOOK_SECRET');
$signature = getenv('IMZALA_WEBHOOK_SIGNATURE');
if ($secret && $signature) {
    $rawBody = '{"event":"demand.completed","demand_id":"3f2a1c88-4a1e-4c1b-9a3a-9d2f7c1e5b40"}';
    $gecerli = ImzalaClient::verifyWebhook($secret, $rawBody, $signature);
    echo 'Webhook imzası geçerli mi: ' . ($gecerli ? 'true' : 'false') . "\n";
} else {
    echo "Webhook doğrulaması atlandı: IMZALA_WEBHOOK_SECRET ve IMZALA_WEBHOOK_SIGNATURE tanımlı değil.\n";
}
