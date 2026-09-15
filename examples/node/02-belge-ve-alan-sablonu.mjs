// Kendi PDF'inizi Alan Şablonuyla kuru koşumda deneme, sonra sözleşme olarak yükleme.
// Kapsam: templates:read + demands:write (kuru koşum da bu kapsamı ister).
// Kuru koşum kredi harcamaz ve hiçbir şey oluşturmaz; yükleme kredi harcar (yorumda).
import { existsSync, readFileSync } from 'node:fs';
import { randomUUID } from 'node:crypto';
import { Imzala } from '@imzala/node';

const imzala = new Imzala({
  apiKey: process.env.IMZALA_API_KEY,
  baseUrl: process.env.IMZALA_BASE_URL,
});

// 1) Alan Şablonunu seç. Alan Şablonu, sözleşme şablonundan ayrıdır: belgedeki
//    çapa metinlerine göre alanların nereye düşeceğini tarif eder.
let fieldTemplateId = process.env.IMZALA_FIELD_TEMPLATE_ID;
if (!fieldTemplateId) {
  const list = await imzala.fieldTemplates.list({ limit: 20 });
  fieldTemplateId = list.field_templates?.[0]?.id;
}
if (!fieldTemplateId) {
  console.log('Tanımlı Alan Şablonu yok. Panelden oluşturabilirsiniz.');
  process.exit(0);
}

// 2) Rolleri ve alan sayılarını oku. Yüklemede her rol için template_party_id
//    bu listeden verilir.
const detail = await imzala.fieldTemplates.get(fieldTemplateId);
console.log(`Alan Şablonu: ${detail.name} · toplam alan: ${detail.total_field_count}`);
for (const party of detail.parties ?? []) {
  console.log(`  Rol ${party.order}: ${party.label ?? '(adsız)'} · ${party.field_count} alan · ${party.id}`);
}

// 3) KURU KOŞUM: yerleşimi belge üzerinde dene. Hiçbir şey oluşturmaz, kredi
//    harcamaz, dosyayı saklamaz. Çözülemeyen çapaları önceden görmenin en ucuz
//    yolu. Çözülemeyen belge de 200 döner: resolvable=false.
const pdfPath = process.env.IMZALA_PDF_PATH ?? new URL('./ornek-sozlesme.pdf', import.meta.url).pathname;
if (!existsSync(pdfPath)) {
  console.log(`PDF bulunamadı: ${pdfPath}. IMZALA_PDF_PATH ile bir PDF verin.`);
  process.exit(0);
}
const pdf = { content: readFileSync(pdfPath), filename: 'ornek-sozlesme.pdf', contentType: 'application/pdf' };

const preview = await imzala.fieldTemplates.previewLayout(detail.id, {
  files: [pdf],
  // onAnchorMiss: 'drop' yalnız şablonun ilgili alanları zaten drop ise uygulanır;
  // verilmezse block geçerlidir. İmza alanları hiçbir koşulda düşürülmez.
});
console.log(`Uygulanabilir: ${preview.resolvable ? 'evet' : 'hayır'} · sayfa: ${preview.page_count} · yerleşen alan: ${preview.placements_summary?.total ?? 0}`);
for (const diagnostic of preview.diagnostics ?? []) {
  console.log(`  ${diagnostic.severity} ${diagnostic.code}: ${diagnostic.message}`);
}
for (const warning of preview.warnings ?? []) {
  console.log(`  Uyarı ${warning.code}: ${warning.message}`);
}
if (!preview.resolvable) {
  console.log('Yerleşim çözülemedi. Belgedeki çapaları düzeltip yeniden deneyin; yükleme 422 ile reddedilir.');
}

// 4) Gerçek yükleme. Kredi harcar (yorumda). fieldTemplateId verildiğinde alanlar
//    bu şablona göre yerleştirilir; her rol tam bir kez eşlenmelidir. Bu uçta
//    davetler varsayılan olarak KAPALIDIR; sendInvitations: 'true' ile aynı
//    istekte gönderilir. idempotencyKey ile tekrar deneme ikinci sözleşme yaratmaz.
//
// const kisiler = [
//   { first_name: 'Ayşe', last_name: 'Yılmaz', email: 'ayse@example.com', phone: '+905551112233' },
//   { first_name: 'Mehmet', last_name: 'Yılmaz', email: 'mehmet@example.com', phone: '+905551112244' },
// ];
// const demand = await imzala.demands.uploadDocument({
//   files: [pdf],
//   title: 'Hizmet Sözleşmesi',
//   parties: (detail.parties ?? []).map((party, i) => ({
//     template_party_id: party.id,
//     ...kisiler[i % kisiler.length],
//   })),
//   fieldTemplateId: detail.id,
//   idempotencyKey: randomUUID(),
//   sendInvitations: 'true',
// });
// console.log(`Sözleşme: ${demand.id} · durum: ${demand.status}`);
// for (const party of demand.signing_urls ?? []) {
//   console.log(`  ${party.first_name} ${party.last_name} → ${party.signing_url}`);
// }
