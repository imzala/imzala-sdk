// Çok belgeli zarf: KVKK aydınlatma + açık rıza + ön bilgilendirme ile sözleşme toplama akışı.
// Kapsam: demands:write (taslak, belgeler, gönderim) + demands:read (liste, PDF).
// Belge uçları kredi harcamaz; tahsilat oluşturma ve gönderim adımlarında yapılır. Yazma adımları yorumda.
import { readFileSync, writeFileSync } from 'node:fs';
import { randomUUID } from 'node:crypto';
import { Imzala, ImzalaError } from '@imzala/node';

const imzala = new Imzala({
  apiKey: process.env.IMZALA_API_KEY,
  baseUrl: process.env.IMZALA_BASE_URL,
});

// 1) Davet göndermeden taslak sözleşme oluştur. dispatch_notifications: false ile
//    davetler oluşturma anında ÇIKMAZ; her şey hazır olunca 6. adımdaki dispatch
//    ile tek seferde gönderilir. Kişiler kurgusaldır; rol id'si şablon detayından
//    (templates.get) gelir. Taslağı panelden oluşturduysanız bu adımı atlayıp
//    kimliğini IMZALA_DEMAND_ID ile verebilirsiniz.
//
// const taslak = await imzala.demands.create(
//   {
//     template_id: process.env.IMZALA_TEMPLATE_ID,
//     title: 'Üyelik sözleşmesi ve KVKK belgeleri',
//     dispatch_notifications: false,
//     party_mapping: [{ template_party_id: '<şablon detayından rol id>', first_name: 'Ayşe', last_name: 'Yılmaz', email: 'ayse@example.com', phone: '+905551112233' }],
//   },
//   { idempotencyKey: randomUUID() },
// );
// const zarfId = taslak.id;
// const partyId = taslak.signing_urls[0].party_id;

// 2) Ana sözleşmeyi dosya olarak yükle (doc_kind CONTRACT). Belge başına TEK dosya,
//    her belge için ayrı çağrı. idempotencyKey ZORUNLUDUR ve gövde alanı olarak
//    gider: aynı anahtarla tekrar yükleme yeni belge yaratmaz, önceki belgeyi döndürür.
//
// const sozlesme = await imzala.demands.documents.upload(zarfId, {
//   file: { content: readFileSync('sozlesme.pdf'), filename: 'sozlesme.pdf', contentType: 'application/pdf' },
//   title: 'Üyelik Sözleşmesi',
//   docKind: 'CONTRACT',
//   idempotencyKey: randomUUID(),
// });
// console.log(`Belge: ${sozlesme.document.id} · ${sozlesme.document.title}`);

// 3) KVKK belgelerini ekle (dosyasız, yalnız metadata).
//    KVKK_CONSENT için is_required FALSE olmak zorundadır: açık rıza reddedilebilir
//    olmalıdır (aksi halde 400 CONSENT_CANNOT_BE_REQUIRED).
//    PREINFO için is_required TRUE olmak zorundadır (aksi halde 400 PREINFO_MUST_BE_REQUIRED).
//
// const aydinlatma = await imzala.demands.documents.create(zarfId, {
//   title: 'KVKK Aydınlatma Metni', doc_kind: 'KVKK_NOTICE', is_required: true, signature_required: false,
// });
// const riza = await imzala.demands.documents.create(zarfId, {
//   title: 'KVKK Açık Rıza Beyanı', doc_kind: 'KVKK_CONSENT', is_required: false,
// });
// const onBilgi = await imzala.demands.documents.create(zarfId, {
//   title: 'Ön Bilgilendirme Formu', doc_kind: 'PREINFO', is_required: true,
// });

// 4) Hangi belgeyi kim görecek: belge başına taraf kümesi (küme olduğu gibi değişir).
//
// for (const belge of [sozlesme, aydinlatma, riza, onBilgi]) {
//   await imzala.demands.documents.setAssignments(zarfId, belge.document.id, [partyId]);
// }

// 5) Sıralama: aydınlatma ve ön bilgilendirme önce, sözleşme sonra, rıza en sonda.
//    Liste zarfın TÜM belgelerini içermelidir (ORDER_SET_MISMATCH).
//
// await imzala.demands.documents.reorder(zarfId, [
//   aydinlatma.document.id, onBilgi.document.id, sozlesme.document.id, riza.document.id,
// ]);

// 6) Gönder. Davetler burada çıkar; kredi mutabakatı burada yapılır (aynı sözleşme
//    için ikinci kez düşmez). Bu uçta idempotency anahtarı yoktur; SDK hiçbir
//    koşulda yinelemez. Zaten gönderilmiş bir sözleşmeye tekrar çağrı kredi
//    düşürmez (dispatched: false) ama davetleri yeniden yollar.
//
// const gonderim = await imzala.demands.dispatch(zarfId, { sendInvitations: true });
// console.log(`Gönderildi: ${gonderim.dispatched} · durum: ${gonderim.status} · düşen kredi: ${gonderim.credits?.charged}`);

// 7) Belgeleri listele (salt okuma). Çok belgeli zarf hesabınız için henüz açık
//    değilse tüm belge uçları 409 ENVELOPE_MULTI_DOC_DISABLED döner.
let demandId = process.env.IMZALA_DEMAND_ID;
if (!demandId) {
  const demands = await imzala.demands.list({ limit: 1, sort: 'createdAt:desc' });
  demandId = demands.demands?.[0]?.id;
}
if (!demandId) {
  console.log('Sözleşme yok. IMZALA_DEMAND_ID verin ya da önce bir sözleşme oluşturun.');
  process.exit(0);
}

let belgeler;
try {
  belgeler = await imzala.demands.documents.list(demandId, { view: 'wizard' });
} catch (err) {
  if (err instanceof ImzalaError && err.code === 'ENVELOPE_MULTI_DOC_DISABLED') {
    console.log(`Çok belgeli zarf bu hesap için kapalı: ${err.codeDescription}`);
    process.exit(0);
  }
  throw err;
}
console.log(`Sözleşme ${demandId}: ${belgeler.documents?.length ?? 0} belge`);
for (const belge of belgeler.documents ?? []) {
  console.log(`  ${belge.order}. ${belge.title} · ${belge.doc_kind} · zorunlu: ${belge.is_required} · imza: ${belge.signature_required} · taraf: ${belge.assigned_party_ids?.length ?? 0}`);
}

// 8) Tamamlanınca belge-özgü imzalı PDF (tüm zarfın birleşik PDF'i için getPdf).
const demand = await imzala.demands.get(demandId);
const ilkBelge = belgeler.documents?.[0];
if (demand.status === 'COMPLETED' && ilkBelge) {
  const pdf = await imzala.demands.getDocumentPdf(demandId, ilkBelge.id);
  const dosya = `belge-${ilkBelge.id}.pdf`;
  writeFileSync(dosya, pdf);
  console.log(`İmzalı belge PDF'i kaydedildi: ${dosya} (${pdf.length} bayt)`);
} else {
  console.log(`Sözleşme durumu ${demand.status}; belge PDF'i yalnız COMPLETED durumunda indirilir.`);
}
