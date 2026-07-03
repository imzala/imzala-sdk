<?php

declare(strict_types=1);

// İmzala PHP SDK, sözleşme yaşam döngüsü örneği.
//
// Çalıştırma (paketin bağımlılıkları kurulu olmalı: packages/php içinde
// `composer install`):
//   IMZALA_API_KEY=imz_... IMZALA_BASE_URL=https://test-api.imzala.org \
//     php examples/php/lifecycle.php
//
// Salt-okuma işlemleri doğrudan çalışır. Veri değiştiren işlemler (create /
// cancel / resendParty / delete) yorum satırıdır: gerçek kredi harcar / durum
// değiştirir.
//
// İmzala sözleşmeleri varsayılan olarak dijital imza (SES) üretir; SDK imza
// geçerliliği hakkında hukuki iddiada bulunmaz.

require __DIR__ . '/../../packages/php/vendor/autoload.php';

use Imzala\ImzalaClient;
use Imzala\ImzalaException;

$apiKey = getenv('IMZALA_API_KEY') ?: '';
if ($apiKey === '') {
    fwrite(STDERR, "IMZALA_API_KEY gerekli (imz_...). Panel, Geliştirici, API Anahtarları.\n");
    exit(1);
}
$baseUrl = getenv('IMZALA_BASE_URL') ?: '';

// baseUrl boş ise SDK varsayılanını (api-prd) kullan.
$imzala = $baseUrl !== ''
    ? new ImzalaClient($apiKey, $baseUrl)
    : new ImzalaClient($apiKey);

try {
    // 1) Kimlik + kredi bakiyesi
    $me = $imzala->me();
    echo "\nKimlik: " . json_encode($me, JSON_UNESCAPED_UNICODE) . "\n";

    // 2) Şablonlar (bir sayfa)
    $templates = $imzala->templates()->list(limit: 5);
    echo "\nŞablon sayısı: " . $templates->getTotal() . "\n";

    // 3) Sözleşme listesi (counts-only, taraf PII'si yok)
    $list = $imzala->demands()->list(limit: 5, sort: 'createdAt:desc');
    echo "\nSözleşme sayısı: " . $list->getTotal() . "\n";
    foreach ($list->getDemands() ?? [] as $d) {
        echo '   - ' . $d->getId() . '  [' . $d->getStatus() . ']  '
            . $d->getPartiesSigned() . '/' . $d->getPartiesTotal() . '  ' . $d->getTitle() . "\n";
    }

    $demands = $list->getDemands() ?? [];
    if (count($demands) === 0) {
        echo "\n(Sözleşme yok, create örneğini açın.)\n";
        exit(0);
    }
    $first = $demands[0];
    $firstId = $first->getId();

    // 4) Sözleşme detay (imzacı adı kısaltılmış, e-posta maskeli, KVKK)
    $demand = $imzala->demands()->get($firstId);
    echo "\nDetay: " . json_encode($demand, JSON_UNESCAPED_UNICODE) . "\n";

    // 5) İmza denetim izi (maskeli)
    $timeline = $imzala->demands()->getTimeline($firstId);
    echo "\nDenetim izi: " . json_encode($timeline, JSON_UNESCAPED_UNICODE) . "\n";

    // 6) Tamamlanmış sözleşmenin imzalı PDF'ini indir (binary, PHP'de string)
    if ((string) $first->getStatus() === 'COMPLETED') {
        $pdf = $imzala->demands()->getPdf($firstId);
        $out = "demand-{$firstId}.pdf";
        file_put_contents($out, $pdf);
        echo "\nİmzalı PDF kaydedildi: {$out} (" . strlen($pdf) . " bayt)\n";

        // Tamamlanma sertifikası (PAdES B-T):
        // $cert = $imzala->demands()->getCertificate($firstId, 'tr');
    }

    // Veri değiştiren işlemler (bilerek yorumlu):
    // $imzala->demands()->cancel($firstId, 'Vazgeçildi');
    // $imzala->demands()->resendParty($firstId, $partyId);
    // $imzala->demands()->delete($firstId);
    // $imzala->templates()->update($templateId, ['name' => 'Yeni Ad']);
    // $imzala->templates()->delete($templateId);

    // Webhook imza doğrulama (ayrı bir HTTP handler'da, ham gövde şart):
    // $ok = ImzalaClient::verifyWebhook(
    //     getenv('IMZALA_WEBHOOK_SECRET'),
    //     file_get_contents('php://input'),          // ham gövde
    //     $_SERVER['HTTP_X_IMZALA_SIGNATURE_256'],    // 'sha256=<hex>'
    // );
} catch (ImzalaException $e) {
    fwrite(STDERR, "\nImzalaException: " . $e->getMessage() . "\n");
    exit(1);
}
