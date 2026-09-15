<?php

declare(strict_types=1);

// Kendi PDF'inizi Alan Şablonuyla kuru koşumda deneme, sonra sözleşme olarak yükleme.
// Kapsam: templates:read + demands:write (kuru koşum da bu kapsamı ister).
// Kuru koşum kredi harcamaz ve hiçbir şey oluşturmaz; yükleme kredi harcar (yorumda).

require __DIR__ . '/../../packages/php/vendor/autoload.php';

use Imzala\FileInput;
use Imzala\ImzalaClient;
use Imzala\UploadDemandParams;
use Imzala\UploadPartyInput;

$baseUrl = getenv('IMZALA_BASE_URL');
$imzala = $baseUrl
    ? new ImzalaClient((string) getenv('IMZALA_API_KEY'), $baseUrl)
    : new ImzalaClient((string) getenv('IMZALA_API_KEY'));

// 1) Alan Şablonunu seç. Alan Şablonu, sözleşme şablonundan ayrıdır: belgedeki
//    çapa metinlerine göre alanların nereye düşeceğini tarif eder.
$fieldTemplateId = getenv('IMZALA_FIELD_TEMPLATE_ID') ?: null;
if ($fieldTemplateId === null) {
    $list = $imzala->fieldTemplates()->list(limit: 20);
    $fieldTemplateId = ($list->getFieldTemplates()[0] ?? null)?->getId();
}
if ($fieldTemplateId === null) {
    echo "Tanımlı Alan Şablonu yok. Panelden oluşturabilirsiniz.\n";
    exit(0);
}

// 2) Rolleri ve alan sayılarını oku. Yüklemede her rol için template_party_id
//    bu listeden verilir.
$detail = $imzala->fieldTemplates()->get($fieldTemplateId);
echo "Alan Şablonu: {$detail->getName()} · toplam alan: {$detail->getTotalFieldCount()}\n";
foreach ($detail->getParties() ?? [] as $party) {
    echo "  Rol {$party->getOrder()}: " . ($party->getLabel() ?? '(adsız)') . " · {$party->getFieldCount()} alan · {$party->getId()}\n";
}

// 3) KURU KOŞUM: yerleşimi belge üzerinde dene. Hiçbir şey oluşturmaz, kredi
//    harcamaz, dosyayı saklamaz. Çözülemeyen çapaları önceden görmenin en ucuz
//    yolu. Çözülemeyen belge de 200 döner: resolvable=false.
$pdfPath = getenv('IMZALA_PDF_PATH') ?: __DIR__ . '/ornek-sozlesme.pdf';
if (!is_file($pdfPath)) {
    echo "PDF bulunamadı: {$pdfPath}. IMZALA_PDF_PATH ile bir PDF verin.\n";
    exit(0);
}
$pdf = new FileInput((string) file_get_contents($pdfPath), 'ornek-sozlesme.pdf', 'application/pdf');

// Üçüncü parametre onAnchorMiss: 'drop' yalnız şablonun ilgili alanları zaten drop
// ise uygulanır; verilmezse block geçerlidir. İmza alanları hiçbir koşulda düşürülmez.
$preview = $imzala->fieldTemplates()->previewLayout($detail->getId(), [$pdf]);
$toplam = $preview->getPlacementsSummary() !== null ? $preview->getPlacementsSummary()->getTotal() : 0;
echo 'Uygulanabilir: ' . ($preview->getResolvable() ? 'evet' : 'hayır') . " · sayfa: {$preview->getPageCount()} · yerleşen alan: {$toplam}\n";
foreach ($preview->getDiagnostics() ?? [] as $diagnostic) {
    echo "  {$diagnostic->getSeverity()} {$diagnostic->getCode()}: {$diagnostic->getMessage()}\n";
}
foreach ($preview->getWarnings() ?? [] as $warning) {
    echo "  Uyarı {$warning->getCode()}: {$warning->getMessage()}\n";
}
if (!$preview->getResolvable()) {
    echo "Yerleşim çözülemedi. Belgedeki çapaları düzeltip yeniden deneyin; yükleme 422 ile reddedilir.\n";
}

// 4) Gerçek yükleme. Kredi harcar (yorumda). withFieldTemplateId verildiğinde alanlar
//    bu şablona göre yerleştirilir; her rol tam bir kez eşlenmelidir. Bu uçta
//    davetler varsayılan olarak KAPALIDIR; withSendInvitations('true') ile aynı
//    istekte gönderilir. withIdempotencyKey ile tekrar deneme ikinci sözleşme yaratmaz.
//
// $kisiler = [
//     ['Ayşe', 'Yılmaz', 'ayse@example.com', '+905551112233'],
//     ['Mehmet', 'Yılmaz', 'mehmet@example.com', '+905551112244'],
// ];
// $parties = [];
// foreach ($detail->getParties() ?? [] as $i => $party) {
//     [$ad, $soyad, $eposta, $telefon] = $kisiler[$i % count($kisiler)];
//     $parties[] = new UploadPartyInput($ad, $soyad, $eposta, $telefon, $party->getId());
// }
// $demand = $imzala->demands()->uploadDocument(
//     (new UploadDemandParams([$pdf], $parties))
//         ->withTitle('Hizmet Sözleşmesi')
//         ->withFieldTemplateId($detail->getId())
//         ->withIdempotencyKey(bin2hex(random_bytes(16)))
//         ->withSendInvitations('true'),
// );
// echo "Sözleşme: {$demand->getId()} · durum: {$demand->getStatus()}\n";
// foreach ($demand->getSigningUrls() ?? [] as $party) {
//     echo "  {$party->getFirstName()} {$party->getLastName()} → {$party->getSigningUrl()}\n";
// }
