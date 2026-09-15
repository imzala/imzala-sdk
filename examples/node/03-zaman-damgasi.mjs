// Bir dosyanın belirli bir anda var olduğunu ve o andan beri değişmediğini kanıtlayan
// zaman damgası hizmeti: damga al, damgaları listele, tek kaydı getir.
// Kapsam: timestamps. Damga almak kredi harcar (yorumda); liste ve getirme harcamaz.
//
// Damga dijital imza DEĞİLDİR: dosyayı kimin yazdığını ya da kimin sahiplendiğini
// kanıtlamaz; yalnız var olma ve değişmezlik kanıtıdır. owner alanları bilgilendirme
// amaçlıdır ve API tarafından doğrulanmaz.
import { existsSync, readFileSync } from 'node:fs';
import { randomUUID } from 'node:crypto';
import { Imzala } from '@imzala/node';

const imzala = new Imzala({
  apiKey: process.env.IMZALA_API_KEY,
  baseUrl: process.env.IMZALA_BASE_URL,
});

// 1) Damga al. Kredi harcar (yorumda). idempotencyKey, ağ koptuğunda ikinci kez
//    kredi harcamayı önler: beş dakika içinde aynı anahtar aynı kaydı döndürür.
const pdfPath = process.env.IMZALA_PDF_PATH ?? new URL('./ornek-sozlesme.pdf', import.meta.url).pathname;
if (existsSync(pdfPath)) {
  console.log(`Damgalanacak dosya hazır: ${pdfPath} (damga alma adımı yorumda).`);
}
//
// const kayit = await imzala.timestamps.create({
//   content: readFileSync(pdfPath),
//   filename: 'ornek-sozlesme.pdf',
//   contentType: 'application/pdf',
//   idempotencyKey: randomUUID(),
//   description: 'Taslak sürüm',
//   ownerFirstName: 'Ayşe',
//   ownerLastName: 'Yılmaz',
// });
// console.log(`Damga: ${kayit.id} · ${kayit.timestamp_time} · ${kayit.tsa_authority}`);
// console.log(`  SHA-256: ${kayit.file_sha256} · harcanan kredi: ${kayit.credits_used}`);

// 2) Damgaları listele (varsayılan sıralama yeni → eski).
const liste = await imzala.timestamps.list({ limit: 10 });
console.log(`Toplam damga: ${liste.total ?? 0}`);
for (const kayit of liste.timestamps ?? []) {
  console.log(`${kayit.id} · ${kayit.original_file_name} · ${kayit.timestamp_date} · ${kayit.status}`);
}

// 3) Tek kaydın ayrıntısı: IMZALA_TIMESTAMP_ID verilmişse o, yoksa listedeki ilk kayıt.
const timestampId = process.env.IMZALA_TIMESTAMP_ID ?? liste.timestamps?.[0]?.id;
if (!timestampId) {
  console.log('Henüz damga kaydı yok.');
  process.exit(0);
}
const detay = await imzala.timestamps.get(timestampId);
console.log(`Kayıt ${detay.id}: ${detay.original_file_name} (${detay.original_file_size} bayt)`);
console.log(`  Açıklama: ${detay.description ?? '-'} · durum: ${detay.status}`);
console.log(`  Damga dosyası: ${detay.timestamp_file_url}`);
