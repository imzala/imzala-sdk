// Toplu sözleşme oluşturma (10'arlı parçalar), Idempotency-Key ve istek limiti (429) davranışı.
// Kapsam: templates:read + demands:write. Oluşturma adımları kredi harcar (yorumda).
// Limit gösterimi yalnız salt-okuma isteği (şablon listesi) kullanır, kredi harcamaz.
import { randomUUID } from 'node:crypto';
import { Imzala, ImzalaRateLimitError, describeErrorCode } from '@imzala/node';

const imzala = new Imzala({
  apiKey: process.env.IMZALA_API_KEY,
  baseUrl: process.env.IMZALA_BASE_URL,
  // GET isteklerinin 429 sonrası otomatik yinelenmesi kapatıldı: 4. adımda
  // 429'u kendimiz görmek istiyoruz. Üretimde varsayılanı (2) bırakın.
  maxRetries: 0,
});

// 1) Şablon: IMZALA_TEMPLATE_ID verilmişse o, yoksa listedeki ilk şablon.
let templateId = process.env.IMZALA_TEMPLATE_ID;
if (!templateId) {
  const templates = await imzala.templates.list({ limit: 1 });
  templateId = templates.templates?.[0]?.id;
}
if (!templateId) {
  console.log('Aktif şablon yok. Panelden bir şablon oluşturun.');
  process.exit(0);
}
const detail = await imzala.templates.get(templateId);
const rolId = detail.parties?.[0]?.id;
console.log(`Şablon: ${detail.name} · ilk rol: ${rolId}`);

// 2) Tek sözleşmede tekrar koruması. create bir Idempotency-Key kabul eder:
//    aynı anahtar + aynı gövde → aynı sözleşme döner, ikinci kayıt oluşmaz.
//    Aynı anahtar + farklı gövde → 409 IDEMPOTENCY_KEY_REUSED.
//    Anahtarlı bir istek 429 alırsa SDK Retry-After kadar bekleyip bir kez yineler;
//    anahtarsız istek hiç yinelenmez (yineleme ikinci sözleşme yaratırdı).
//
// const anahtar = randomUUID(); // ör. kendi sipariş numaranız da olabilir
// const demand = await imzala.demands.create(
//   {
//     template_id: detail.id,
//     party_mapping: [{ template_party_id: rolId, first_name: 'Ayşe', last_name: 'Yılmaz', email: 'ayse@example.com' }],
//   },
//   { idempotencyKey: anahtar },
// );
// console.log(`Sözleşme: ${demand.id}`);

// 3) Toplu oluşturma: tek istekte en fazla 10 satır (BULK_MAX_10). Daha uzun
//    listeyi 10'arlı parçalara bölün. Her satır bağımsız bir sözleşmedir; kısmi
//    başarı 200 ile döner, satır durumunu results[i].status'tan okuyun.
//    Bu uçta Idempotency-Key YOKTUR; SDK toplu isteği 429 dahil hiçbir koşulda
//    yinelemez, çünkü sunucu anahtarı tanımadığından yineleme aynı sözleşmeleri
//    ikinci kez yaratır ve krediyi iki kez düşerdi. Parça başarısız olursa
//    demands.list ile durumu doğrulayıp yalnız eksik satırları yeniden gönderin.
//
// const alicilar = [
//   { template_party_id: rolId, first_name: 'Ayşe', last_name: 'Yılmaz', email: 'ayse@example.com' },
//   { template_party_id: rolId, first_name: 'Mehmet', last_name: 'Yılmaz', email: 'mehmet@example.com' },
// ];
// for (let i = 0; i < alicilar.length; i += 10) {
//   const parca = alicilar.slice(i, i + 10);
//   const sonuc = await imzala.demands.createBulk({
//     template_id: detail.id,
//     options: { dispatch_notifications: false }, // önce taslak oluştur, gözden geçir, sonra gönder
//     rows: parca.map((alici) => ({ party_mapping: [alici] })),
//   });
//   console.log(`Parça ${i / 10 + 1}: oluşan ${sonuc.created}, başarısız ${sonuc.failed}`);
//   for (const satir of sonuc.results ?? []) {
//     console.log(`  satır ${satir.row_index}: ${satir.status} ${satir.demand_id ?? satir.error ?? ''}`);
//   }
// }

// 4) İstek limiti. Anahtar başına dakikada 60 istek (anahtar bazında düşürülebilir).
//    Aşımda 429 + RATE_LIMIT_EXCEEDED döner.
//
//    NOT: limiti ardışık isteklerle göremezsiniz: her istek yaklaşık bir saniye
//    sürdüğü için 65 ardışık çağrının hepsi 200 döner (60 saniyelik pencere
//    aradan sıfırlanır). Limit ancak eşzamanlı bir patlamayla görünür.
try {
  await Promise.all(Array.from({ length: 70 }, () => imzala.templates.list({ limit: 1 })));
  console.log('Limit tetiklenmedi.');
} catch (err) {
  if (err instanceof ImzalaRateLimitError) {
    console.log(`Limit aşıldı: ${describeErrorCode(err.code) ?? err.code}`);
    console.log(`  ${err.retryAfter} saniye sonra tekrar deneyin.`);
    console.log(`  Politika: ${err.rateLimit?.policy} · limit: ${err.rateLimit?.limit} · kalan: ${err.rateLimit?.remaining}`);
  } else {
    throw err;
  }
}
