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
// uploadDocument / cancel / resendParty / delete / sendReminder / timestamps)
// yorum satırıdır: gerçek kredi harcar / durum değiştirir.
//
// İmzala sözleşmeleri varsayılan olarak dijital imza (SES) üretir; SDK imza
// geçerliliği hakkında hukuki iddiada bulunmaz.

require __DIR__ . '/../../packages/php/vendor/autoload.php';

use Imzala\CreateTimestampParams;
use Imzala\FileInput;
use Imzala\ImzalaAuthException;
use Imzala\ImzalaException;
use Imzala\ImzalaRateLimitException;
use Imzala\ImzalaValidationException;
use Imzala\ImzalaClient;
use Imzala\UploadDemandParams;
use Imzala\UploadPartyInput;

$apiKey = getenv('IMZALA_API_KEY') ?: '';
if ($apiKey === '') {
    fwrite(STDERR, "IMZALA_API_KEY gerekli (imz_...). Panel, Geliştirici, API Anahtarları.\n");
    exit(1);
}
$baseUrl = getenv('IMZALA_BASE_URL') ?: '';

// baseUrl boş ise SDK varsayılanını (api-prd) kullan. Otomatik yeniden deneme
// isimli argümanlarla ayarlanabilir: new ImzalaClient($apiKey, $baseUrl,
// maxRetries: 2, retryBaseDelayMs: 300). Yalnızca GET (okuma) uçları retry olur.
$imzala = $baseUrl !== ''
    ? new ImzalaClient($apiKey, $baseUrl)
    : new ImzalaClient($apiKey);

try {
    // 1) Kimlik + kredi bakiyesi (me)
    $me = $imzala->me();
    echo "\nKimlik: " . ($me->getEmail() ?? $me->getId())
        . '  (kalan kredi: ' . $me->getCredits() . ")\n";

    // 2) Şablonlar (bir sayfa)
    $templates = $imzala->templates()->list(limit: 5);
    echo "\nŞablon sayısı: " . $templates->getTotal() . "\n";
    foreach ($templates->getTemplates() ?? [] as $t) {
        echo '   - ' . $t->getId() . '  ' . $t->getName() . "\n";
    }

    // 2b) Tüm şablonları sayfa sayfa gezen generator (salt-okuma, güvenli):
    //   foreach ($imzala->templates()->listAll(limit: 50) as $t) {
    //       echo $t->getId() . ' ' . $t->getName() . "\n";
    //   }
    // Tek şablon detayı (taraflar + doldurulabilir alanlar):
    //   $detail = $imzala->templates()->get($templateId);
    // Hazır entegrasyon kılavuzu (curl + JSON örneği):
    //   $usage  = $imzala->templates()->usage($templateId);

    // 3) Sözleşme listesi (counts-only, taraf PII'si yok)
    $list = $imzala->demands()->list(limit: 5, sort: 'createdAt:desc');
    echo "\nSözleşme sayısı: " . $list->getTotal() . "\n";
    foreach ($list->getDemands() ?? [] as $d) {
        echo '   - ' . $d->getId() . '  [' . $d->getStatus() . ']  '
            . $d->getPartiesSigned() . '/' . $d->getPartiesTotal() . '  ' . $d->getTitle() . "\n";
    }

    $demands = $list->getDemands() ?? [];
    if (count($demands) === 0) {
        echo "\n(Sözleşme yok, aşağıdaki create örneğini açın.)\n";
        exit(0);
    }
    $first = $demands[0];
    $firstId = (string) $first->getId();

    // 4) Sözleşme detay (imzacı adı kısaltılmış, e-posta maskeli, KVKK)
    $demand = $imzala->demands()->get($firstId);
    echo "\nDetay: " . $demand->getId() . '  [' . $demand->getStatus() . "]\n";
    foreach ($demand->getParties() ?? [] as $party) {
        echo '   - ' . $party->getName() . '  '
            . ($party->getEmailMasked() ?? '')
            . ($party->getSigned() ? '  (imzaladı)' : '  (bekliyor)') . "\n";
    }

    // 5) İmza denetim izi (maskeli olaylar: ip son okteti gizli, ad/e-posta maskeli)
    $timeline = $imzala->demands()->getTimeline($firstId);
    echo "\nDenetim izi olayı: " . count($timeline->getEvents() ?? []) . "\n";

    // 6) Tamamlanmış sözleşmenin imzalı PDF + sertifikasını indir (binary → PHP string)
    if ((string) $first->getStatus() === 'COMPLETED') {
        $pdf = $imzala->demands()->getPdf($firstId);
        $out = "demand-{$firstId}.pdf";
        file_put_contents($out, $pdf);
        echo "\nİmzalı PDF kaydedildi: {$out} (" . strlen($pdf) . " bayt)\n";

        // Tamamlanma sertifikası (PAdES B-T), Türkçe:
        $cert = $imzala->demands()->getCertificate($firstId, 'tr');
        file_put_contents("certificate-{$firstId}.pdf", $cert);
        echo "Sertifika kaydedildi (" . strlen($cert) . " bayt)\n";
    }

    // ---------------------------------------------------------------------
    // Veri değiştiren işlemler (bilerek yorumlu: kredi harcar / durum değiştirir):
    //
    // Şablondan sözleşme oluştur (imza daveti otomatik gider):
    //   $created = $imzala->demands()->create([
    //       'template_id'   => $templateId,
    //       'party_mapping' => [[
    //           'template_party_id' => $templatePartyId,
    //           'first_name' => 'Ahmet',
    //           'last_name'  => 'Yılmaz',
    //           'email'      => 'ahmet@example.com',
    //           'phone'      => '+905301112233',
    //       ]],
    //   ]);
    //   print_r($created->getSigningUrls());
    //
    // Şablonsuz (dosya yükleyerek) sözleşme:
    //   $params = new UploadDemandParams(
    //       files:   [new FileInput(file_get_contents('sozlesme.pdf'), 'sozlesme.pdf', 'application/pdf')],
    //       parties: [new UploadPartyInput('Ada', 'Lovelace', 'ada@example.com', '+905551234567')],
    //   );
    //   $uploaded = $imzala->demands()->uploadDocument($params->withTitle('Hizmet Sözleşmesi'));
    //
    // Bekleyen sözleşmeyi iptal et:
    //   $imzala->demands()->cancel($firstId, ['reason' => 'Vazgeçildi']);
    // Tekil tarafa daveti tekrar gönder:
    //   $imzala->demands()->resendParty($firstId, $partyId);
    // İmzalamamış taraflara hatırlatma:
    //   $imzala->demands()->sendReminder($firstId, ['force' => true]);
    // Tamamlanmamış sözleşmeyi sil:
    //   $imzala->demands()->delete($firstId);
    //
    // Şablon metadata güncelle / sil:
    //   $imzala->templates()->update($templateId, ['name' => 'Yeni Ad', 'category' => 'HR']);
    //   $imzala->templates()->delete($templateId);
    //
    // Gömülü imza oturumu (embed_url bir <iframe>'e gömülür, bkz. @imzala/embed):
    //   $session = $imzala->embed()->createSession($firstId, $partyId);
    //   echo $session->getEmbedUrl();
    //
    // RFC 3161 zaman damgası (TÜBİTAK KAMU SM TSA):
    //   $ts = $imzala->timestamps()->create(
    //       (new CreateTimestampParams(file_get_contents('belge.pdf'), 'belge.pdf'))
    //           ->withIdempotencyKey('unique-key')
    //   );
    //   echo $ts->getTimestampTime() . ', ' . $ts->getTsaAuthority();
    //
    // Webhook imza doğrulama (ayrı bir HTTP handler'da, ham gövde şart):
    //   $ok = ImzalaClient::verifyWebhook(
    //       getenv('IMZALA_WEBHOOK_SECRET'),
    //       file_get_contents('php://input'),          // ham gövde, json_decode ETMEDEN
    //       $_SERVER['HTTP_X_IMZALA_SIGNATURE_256'],    // 'sha256=<hex>'
    //   );
} catch (ImzalaRateLimitException $e) {
    fwrite(STDERR, "\nRate limit: {$e->getRetryAfter()} sn sonra tekrar dene\n");
    exit(1);
} catch (ImzalaAuthException $e) {
    fwrite(STDERR, "\nAPI anahtarı geçersiz veya yetkisiz\n");
    exit(1);
} catch (ImzalaValidationException $e) {
    fwrite(STDERR, "\nİstek doğrulanamadı: " . $e->getBody() . "\n");
    exit(1);
} catch (ImzalaException $e) {
    fwrite(STDERR, "\nİmzala API hatası: " . $e->getStatusCode() . ' ' . $e->getMessage() . "\n");
    exit(1);
}
