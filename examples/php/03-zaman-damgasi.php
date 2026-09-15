<?php

declare(strict_types=1);

// Bir dosyanın belirli bir anda var olduğunu ve o andan beri değişmediğini kanıtlayan
// zaman damgası hizmeti: damga al, damgaları listele, tek kaydı getir.
// Kapsam: timestamps. Damga almak kredi harcar (yorumda); liste ve getirme harcamaz.
//
// Damga dijital imza DEĞİLDİR: dosyayı kimin yazdığını ya da kimin sahiplendiğini
// kanıtlamaz; yalnız var olma ve değişmezlik kanıtıdır. owner alanları bilgilendirme
// amaçlıdır ve API tarafından doğrulanmaz.

require __DIR__ . '/../../packages/php/vendor/autoload.php';

use Imzala\CreateTimestampParams;
use Imzala\ImzalaClient;

$baseUrl = getenv('IMZALA_BASE_URL');
$imzala = $baseUrl
    ? new ImzalaClient((string) getenv('IMZALA_API_KEY'), $baseUrl)
    : new ImzalaClient((string) getenv('IMZALA_API_KEY'));

// 1) Damga al. Kredi harcar (yorumda). withIdempotencyKey, ağ koptuğunda ikinci kez
//    kredi harcamayı önler: beş dakika içinde aynı anahtar aynı kaydı döndürür.
$pdfPath = getenv('IMZALA_PDF_PATH') ?: __DIR__ . '/ornek-sozlesme.pdf';
if (is_file($pdfPath)) {
    echo "Damgalanacak dosya hazır: {$pdfPath} (damga alma adımı yorumda).\n";
}
//
// $kayit = $imzala->timestamps()->create(
//     (new CreateTimestampParams((string) file_get_contents($pdfPath), 'ornek-sozlesme.pdf'))
//         ->withContentType('application/pdf')
//         ->withIdempotencyKey(bin2hex(random_bytes(16)))
//         ->withDescription('Taslak sürüm')
//         ->withOwnerFirstName('Ayşe')
//         ->withOwnerLastName('Yılmaz'),
// );
// echo "Damga: {$kayit->getId()} · {$kayit->getTimestampTime()?->format(DATE_ATOM)} · {$kayit->getTsaAuthority()}\n";
// echo "  SHA-256: {$kayit->getFileSha256()} · harcanan kredi: {$kayit->getCreditsUsed()}\n";

// 2) Damgaları listele (varsayılan sıralama yeni → eski).
$liste = $imzala->timestamps()->list(limit: 10);
echo 'Toplam damga: ' . ($liste->getTotal() ?? 0) . "\n";
foreach ($liste->getTimestamps() ?? [] as $kayit) {
    echo "{$kayit->getId()} · {$kayit->getOriginalFileName()} · {$kayit->getTimestampDate()?->format(DATE_ATOM)} · {$kayit->getStatus()}\n";
}

// 3) Tek kaydın ayrıntısı: IMZALA_TIMESTAMP_ID verilmişse o, yoksa listedeki ilk kayıt.
$timestampId = getenv('IMZALA_TIMESTAMP_ID') ?: ($liste->getTimestamps()[0] ?? null)?->getId();
if ($timestampId === null) {
    echo "Henüz damga kaydı yok.\n";
    exit(0);
}
$detay = $imzala->timestamps()->get($timestampId);
echo "Kayıt {$detay->getId()}: {$detay->getOriginalFileName()} ({$detay->getOriginalFileSize()} bayt)\n";
echo '  Açıklama: ' . ($detay->getDescription() ?? '-') . " · durum: {$detay->getStatus()}\n";
echo "  Damga dosyası: {$detay->getTimestampFileUrl()}\n";
