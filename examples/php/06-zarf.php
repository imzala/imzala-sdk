<?php

declare(strict_types=1);

// Çok belgeli zarf: KVKK aydınlatma + açık rıza + ön bilgilendirme ile sözleşme toplama akışı.
// Kapsam: demands:write (taslak, belgeler, gönderim) + demands:read (liste, PDF).
// Belge uçları kredi harcamaz; tahsilat oluşturma ve gönderim adımlarında yapılır. Yazma adımları yorumda.

require __DIR__ . '/../../packages/php/vendor/autoload.php';

use Imzala\FileInput;
use Imzala\ImzalaClient;
use Imzala\ImzalaException;

$baseUrl = getenv('IMZALA_BASE_URL');
$imzala = $baseUrl
    ? new ImzalaClient((string) getenv('IMZALA_API_KEY'), $baseUrl)
    : new ImzalaClient((string) getenv('IMZALA_API_KEY'));

// 1) Davet göndermeden taslak sözleşme oluştur. dispatch_notifications: false ile
//    davetler oluşturma anında ÇIKMAZ; her şey hazır olunca 6. adımdaki dispatch
//    ile tek seferde gönderilir. Kişiler kurgusaldır; rol id'si şablon detayından
//    (templates()->get()) gelir. Taslağı panelden oluşturduysanız bu adımı atlayıp
//    kimliğini IMZALA_DEMAND_ID ile verebilirsiniz.
//
// $taslak = $imzala->demands()->create(
//     [
//         'template_id' => (string) getenv('IMZALA_TEMPLATE_ID'),
//         'title' => 'Üyelik sözleşmesi ve KVKK belgeleri',
//         'dispatch_notifications' => false,
//         'party_mapping' => [['template_party_id' => '<şablon detayından rol id>', 'first_name' => 'Ayşe', 'last_name' => 'Yılmaz', 'email' => 'ayse@example.com', 'phone' => '+905551112233']],
//     ],
//     bin2hex(random_bytes(16)),
// );
// $zarfId = $taslak->getId();
// $partyId = $taslak->getSigningUrls()[0]->getPartyId();

// 2) Ana sözleşmeyi dosya olarak yükle (doc_kind CONTRACT). Belge başına TEK dosya,
//    her belge için ayrı çağrı. idempotencyKey ZORUNLUDUR ve gövde alanı olarak
//    gider: aynı anahtarla tekrar yükleme yeni belge yaratmaz, önceki belgeyi döndürür.
//
// $sozlesme = $imzala->demands()->documents()->upload(
//     $zarfId,
//     new FileInput((string) file_get_contents('sozlesme.pdf'), 'sozlesme.pdf', 'application/pdf'),
//     'Üyelik Sözleşmesi',
//     bin2hex(random_bytes(16)),
//     'CONTRACT',
// );
// echo "Belge: {$sozlesme->getDocument()->getId()} · {$sozlesme->getDocument()->getTitle()}\n";

// 3) KVKK belgelerini ekle (dosyasız, yalnız metadata).
//    KVKK_CONSENT için is_required FALSE olmak zorundadır: açık rıza reddedilebilir
//    olmalıdır (aksi halde 400 CONSENT_CANNOT_BE_REQUIRED).
//    PREINFO için is_required TRUE olmak zorundadır (aksi halde 400 PREINFO_MUST_BE_REQUIRED).
//
// $aydinlatma = $imzala->demands()->documents()->create($zarfId, [
//     'title' => 'KVKK Aydınlatma Metni', 'doc_kind' => 'KVKK_NOTICE', 'is_required' => true, 'signature_required' => false,
// ]);
// $riza = $imzala->demands()->documents()->create($zarfId, [
//     'title' => 'KVKK Açık Rıza Beyanı', 'doc_kind' => 'KVKK_CONSENT', 'is_required' => false,
// ]);
// $onBilgi = $imzala->demands()->documents()->create($zarfId, [
//     'title' => 'Ön Bilgilendirme Formu', 'doc_kind' => 'PREINFO', 'is_required' => true,
// ]);

// 4) Hangi belgeyi kim görecek: belge başına taraf kümesi (küme olduğu gibi değişir).
//
// foreach ([$sozlesme, $aydinlatma, $riza, $onBilgi] as $belge) {
//     $imzala->demands()->documents()->setAssignments($zarfId, $belge->getDocument()->getId(), [$partyId]);
// }

// 5) Sıralama: aydınlatma ve ön bilgilendirme önce, sözleşme sonra, rıza en sonda.
//    Liste zarfın TÜM belgelerini içermelidir (ORDER_SET_MISMATCH).
//
// $imzala->demands()->documents()->reorder($zarfId, [
//     $aydinlatma->getDocument()->getId(), $onBilgi->getDocument()->getId(), $sozlesme->getDocument()->getId(), $riza->getDocument()->getId(),
// ]);

// 6) Gönder. Davetler burada çıkar; kredi mutabakatı burada yapılır (aynı sözleşme
//    için ikinci kez düşmez). Bu uçta idempotency anahtarı yoktur; SDK hiçbir
//    koşulda yinelemez. Zaten gönderilmiş bir sözleşmeye tekrar çağrı kredi
//    düşürmez (dispatched: false) ama davetleri yeniden yollar.
//
// $gonderim = $imzala->demands()->dispatch($zarfId, true);
// $dusen = $gonderim->getCredits() !== null ? $gonderim->getCredits()->getCharged() : '?';
// echo 'Gönderildi: ' . ($gonderim->getDispatched() ? 'true' : 'false') . " · durum: {$gonderim->getStatus()} · düşen kredi: {$dusen}\n";

// 7) Belgeleri listele (salt okuma). Çok belgeli zarf hesabınız için henüz açık
//    değilse tüm belge uçları 409 ENVELOPE_MULTI_DOC_DISABLED döner.
$demandId = getenv('IMZALA_DEMAND_ID') ?: null;
if ($demandId === null) {
    $demands = $imzala->demands()->list(limit: 1, sort: 'createdAt:desc');
    $demandId = ($demands->getDemands()[0] ?? null)?->getId();
}
if ($demandId === null) {
    echo "Sözleşme yok. IMZALA_DEMAND_ID verin ya da önce bir sözleşme oluşturun.\n";
    exit(0);
}

try {
    $belgeler = $imzala->demands()->documents()->list($demandId, 'wizard');
} catch (ImzalaException $e) {
    if ($e->getErrorCode() === 'ENVELOPE_MULTI_DOC_DISABLED') {
        echo "Çok belgeli zarf bu hesap için kapalı: {$e->getCodeDescription()}\n";
        exit(0);
    }
    throw $e;
}
echo "Sözleşme {$demandId}: " . count($belgeler->getDocuments() ?? []) . " belge\n";
foreach ($belgeler->getDocuments() ?? [] as $belge) {
    $zorunlu = $belge->getIsRequired() ? 'true' : 'false';
    $imza = $belge->getSignatureRequired() ? 'true' : 'false';
    $taraf = count($belge->getAssignedPartyIds() ?? []);
    echo "  {$belge->getOrder()}. {$belge->getTitle()} · {$belge->getDocKind()} · zorunlu: {$zorunlu} · imza: {$imza} · taraf: {$taraf}\n";
}

// 8) Tamamlanınca belge-özgü imzalı PDF (tüm zarfın birleşik PDF'i için getPdf).
$demand = $imzala->demands()->get($demandId);
$ilkBelge = $belgeler->getDocuments()[0] ?? null;
if ($demand->getStatus() === 'COMPLETED' && $ilkBelge !== null) {
    $pdf = $imzala->demands()->getDocumentPdf($demandId, $ilkBelge->getId());
    $dosya = "belge-{$ilkBelge->getId()}.pdf";
    file_put_contents($dosya, $pdf);
    echo "İmzalı belge PDF'i kaydedildi: {$dosya} (" . strlen($pdf) . " bayt)\n";
} else {
    echo "Sözleşme durumu {$demand->getStatus()}; belge PDF'i yalnız COMPLETED durumunda indirilir.\n";
}
