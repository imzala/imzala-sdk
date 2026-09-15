// Şablondan sözleşme oluşturma, davet linklerini alma ve webhook imzasını doğrulama.
// Kapsam: templates:read + demands:read; sözleşme oluşturma için demands:write.
// Sözleşme oluşturma kredi harcar (yorumda). Diğer adımlar kredi harcamaz.
import { randomUUID } from 'node:crypto';
import { Imzala, verifyWebhook } from '@imzala/node';

const imzala = new Imzala({
  apiKey: process.env.IMZALA_API_KEY,
  baseUrl: process.env.IMZALA_BASE_URL, // boşsa SDK varsayılanı (api-prd.imzala.org)
});

// 1) Hesap ve kalan kredi. Bu uç kapsam istemez.
const me = await imzala.me();
console.log(`Hesap: ${me.email} · kalan kredi: ${me.credits?.remaining ?? '?'}`);

// 2) Şablonu seç: IMZALA_TEMPLATE_ID verilmişse o, yoksa listedeki ilk şablon.
let templateId = process.env.IMZALA_TEMPLATE_ID;
if (!templateId) {
  const templates = await imzala.templates.list({ limit: 10 });
  templateId = templates.templates?.[0]?.id;
}
if (!templateId) {
  console.log('Aktif şablon yok. Panelden bir şablon oluşturun.');
  process.exit(0);
}

// 3) Şablonun rollerini ve değişkenlerini oku. party_mapping bu rollere göre
//    kurulur: her rol için template_party_id zorunludur.
const detail = await imzala.templates.get(templateId);
console.log(`Şablon: ${detail.name} · ${detail.pages_count} sayfa`);
for (const party of detail.parties ?? []) {
  console.log(`  Rol ${party.order}: ${party.label ?? '(adsız)'} · ${party.id}${party.is_required ? '' : ' · isteğe bağlı'}`);
}
for (const variable of detail.variables ?? []) {
  console.log(`  Değişken: ${variable.slug} (${variable.item_type})`);
}

// 4) Sözleşmeyi oluştur. Kredi harcar; açmadan önce test ortamını kullanın.
//    idempotency_key: ağ koptuğunda aynı anahtarla tekrar denemek ikinci bir
//    sözleşme yaratmaz; SDK bir 429 sonrasında anahtarlı isteği bir kez yineler.
//    Kişiler kurgusaldır; her rol için template_party_id şablon detayından gelir.
//
// const kisiler = [
//   { first_name: 'Ayşe', last_name: 'Yılmaz', email: 'ayse@example.com', phone: '+905551112233' },
//   { first_name: 'Mehmet', last_name: 'Yılmaz', email: 'mehmet@example.com', phone: '+905551112244' },
// ];
// const demand = await imzala.demands.create(
//   {
//     template_id: detail.id,
//     title: 'Hizmet Sözleşmesi',
//     expiry_date: '2026-12-31',
//     party_mapping: (detail.parties ?? []).map((party, i) => ({
//       template_party_id: party.id,
//       ...kisiler[i % kisiler.length],
//     })),
//   },
//   { idempotencyKey: randomUUID() },
// );
// console.log(`Sözleşme: ${demand.id} · durum: ${demand.status}`);
// for (const party of demand.signing_urls ?? []) {
//   console.log(`  ${party.first_name} ${party.last_name} → ${party.signing_url}`);
// }

// 5) Son sözleşmelerin durumu (yalnız sayımlar, kişisel veri içermez).
const demands = await imzala.demands.list({ limit: 5, sort: 'createdAt:desc' });
for (const d of demands.demands ?? []) {
  console.log(`${d.id} · ${d.status} · ${d.parties_signed}/${d.parties_total} imza`);
}

// 6) Webhook imzası. Gövde HAM olarak okunmalı: JSON'u ayrıştırıp yeniden
//    serileştirmek baytları değiştirir ve imza tutmaz. Express'te
//    express.raw({ type: 'application/json' }) ile ham gövdeyi alın.
const secret = process.env.IMZALA_WEBHOOK_SECRET;
const signature = process.env.IMZALA_WEBHOOK_SIGNATURE;
if (secret && signature) {
  const rawBody = '{"event":"demand.completed","demand_id":"3f2a1c88-4a1e-4c1b-9a3a-9d2f7c1e5b40"}';
  console.log('Webhook imzası geçerli mi:', verifyWebhook(secret, rawBody, signature));
} else {
  console.log('Webhook doğrulaması atlandı: IMZALA_WEBHOOK_SECRET ve IMZALA_WEBHOOK_SIGNATURE tanımlı değil.');
}
