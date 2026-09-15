// Adres defterini sayfalı gezme, kişi oluşturma ve sözleşme durum sayımlarını okuma.
// Kapsam: contacts:read + contacts:write (kişi oluşturma) + demands:read (rapor).
// Kredi harcamaz. Kişi oluşturma veri değiştirir (yorumda).
import { Imzala, ImzalaError } from '@imzala/node';

const imzala = new Imzala({
  apiKey: process.env.IMZALA_API_KEY,
  baseUrl: process.env.IMZALA_BASE_URL,
});

// 1) Sayfalı gezinme: listAll sayfaları şeffaf biçimde dolaşır (limit 10..100).
let sayac = 0;
for await (const kisi of imzala.contacts.listAll({ limit: 50 })) {
  sayac += 1;
  if (sayac <= 5) console.log(`${kisi.id} · ${kisi.first_name} ${kisi.last_name}`);
}
console.log(`Toplam kişi: ${sayac}`);

// 2) Arama: tek sayfa, metin filtresiyle.
const arama = await imzala.contacts.list({ q: 'ayse', limit: 10 });
console.log(`"ayse" için ${arama.total ?? 0} sonuç`);

// 3) Kişi oluştur. Aynı e-posta veya telefona sahip aktif bir kişi varsa 409
//    CONTACT_DUPLICATE döner. Bu uçta idempotency anahtarı yoktur; SDK isteği
//    hiçbir koşulda yinelemez. Kişisel veriyi buraya yazan, o veri için veri
//    sorumlusudur (aydınlatma yükümlülüğü size aittir).
//
// try {
//   const yeni = await imzala.contacts.create({
//     first_name: 'Ayşe',
//     last_name: 'Yılmaz',
//     email: 'ayse@example.com',
//     phone: '+905551112233',
//   });
//   console.log(`Kişi oluşturuldu: ${yeni.id}`);
// } catch (err) {
//   if (err instanceof ImzalaError && err.code === 'CONTACT_DUPLICATE') {
//     console.log(`Kişi zaten var: ${err.codeDescription}`);
//   } else {
//     throw err;
//   }
// }

// 4) Rapor: sözleşme durumlarının toplu sayımı. Kişisel veri içermez.
const rapor = await imzala.reports.get();
const c = rapor.contracts ?? {};
console.log(`Sözleşmeler: toplam ${c.total} · bekleyen ${c.pending} · tamamlanan ${c.completed}`);
console.log(`  iptal ${c.cancelled} · süresi dolan ${c.expired} · bu ay oluşturulan ${c.this_month}`);
