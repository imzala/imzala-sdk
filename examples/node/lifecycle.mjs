// İmzala Node SDK — sözleşme yaşam döngüsü örneği (çalışır).
//
// Çalıştırma:
//   IMZALA_API_KEY=imz_... IMZALA_BASE_URL=https://test-api.imzala.org \
//     node examples/node/lifecycle.mjs
//
// Salt-okuma işlemleri doğrudan çalışır. Veri değiştiren işlemler (create /
// cancel / resendParty / delete) yorum satırıdır: gerçek kredi harcar / durum
// değiştirir. Açmadan önce ne yaptığını okuyun.

import { writeFileSync } from 'node:fs';
import { Imzala, ImzalaError } from '@imzala/node';

const apiKey = process.env.IMZALA_API_KEY;
if (!apiKey) {
  console.error('IMZALA_API_KEY gerekli (imz_...). Panel → Geliştirici → API Anahtarları.');
  process.exit(1);
}

const imzala = new Imzala({
  apiKey,
  // Varsayılan api-prd.imzala.org. Test ortamı için:
  baseUrl: process.env.IMZALA_BASE_URL, // undefined ise SDK varsayılanı kullanır
});

async function main() {
  // 1) Kimlik + kredi bakiyesi
  const me = await imzala.me();
  const credits = typeof me.credits === 'object' ? JSON.stringify(me.credits) : (me.credits ?? '?');
  console.log(`\n👤 ${me.email ?? me.id} — kredi: ${credits}`);

  // 2) Şablonlar (bir sayfa)
  const templates = await imzala.templates.list({ limit: 5 });
  console.log(`\n📄 Şablon sayısı: ${templates.total ?? templates.templates?.length ?? 0}`);
  for (const t of templates.templates ?? []) {
    console.log(`   - ${t.id}  ${t.name}`);
  }

  // 3) Sözleşme listesi (counts-only, taraf PII'si yok)
  const list = await imzala.demands.list({ limit: 5, sort: 'createdAt:desc' });
  console.log(`\n📋 Sözleşme sayısı: ${list.total ?? 0}`);
  for (const d of list.demands ?? []) {
    console.log(`   - ${d.id}  [${d.status}]  ${d.parties_signed}/${d.parties_total} imzalı  ${d.title ?? ''}`);
  }

  const first = (list.demands ?? [])[0];
  if (!first) {
    console.log('\n(Sözleşme yok — create örneğini açın.)');
    return;
  }

  // 4) Sözleşme detay (imzacı adı kısaltılmış, e-posta maskeli — KVKK)
  const demand = await imzala.demands.get(first.id);
  console.log(`\n🔎 ${demand.id} taraflar:`);
  for (const p of demand.parties ?? []) {
    console.log(`   - ${p.name}  ${p.email_masked}  ${p.signed ? '✅ imzaladı' : '⏳ bekliyor'}`);
  }

  // 5) İmza denetim izi (maskeli — ip_masked, ham IP yok)
  const timeline = await imzala.demands.getTimeline(first.id);
  console.log(`\n🕒 Denetim izi: ${timeline.events?.length ?? 0} olay`);
  for (const e of (timeline.events ?? []).slice(0, 5)) {
    console.log(`   - ${e.created_at}  ${e.event_type}  ${e.actor_label ?? ''}  ${e.ip_masked ?? ''}`);
  }

  // 6) Tamamlanmış sözleşmenin imzalı PDF'ini indir (binary → Buffer)
  if (first.status === 'COMPLETED') {
    const pdf = await imzala.demands.getPdf(first.id);
    const out = `demand-${first.id}.pdf`;
    writeFileSync(out, pdf);
    console.log(`\n💾 İmzalı PDF kaydedildi: ${out} (${pdf.length} bayt)`);

    // Tamamlanma sertifikası (PAdES B-T):
    // const cert = await imzala.demands.getCertificate(first.id, { lang: 'tr' });
    // writeFileSync(`demand-${first.id}-sertifika.pdf`, cert);
  }

  // ── Veri değiştiren işlemler (bilerek yorumlu) ─────────────────────────────
  //
  // // Şablondan yeni sözleşme oluştur (1 kredi harcar):
  // const created = await imzala.demands.create({
  //   template_id: templates.templates[0].id,
  //   party_mapping: [{
  //     template_party_id: '<template-party-id>',
  //     first_name: 'Ada', last_name: 'Kalkan',
  //     email: 'ada@example.com', phone: '+905304636743',
  //   }],
  //   variables: { adres: 'Çankaya/Ankara', tutar: '5.000 TL' },
  // });
  // console.log('İmza URL:', created.signing_urls[0].signing_url);
  //
  // // Bekleyen sözleşmeyi iptal et:
  // await imzala.demands.cancel(first.id, { reason: 'Vazgeçildi' });
  //
  // // Tekil tarafa daveti tekrar gönder:
  // await imzala.demands.resendParty(first.id, '<party-id>');
  //
  // // Tamamlanmamış sözleşmeyi sil:
  // await imzala.demands.delete(first.id);
  //
  // // Şablon metadata güncelle / sil:
  // await imzala.templates.update('<template-id>', { name: 'Yeni Ad' });
  // await imzala.templates.delete('<template-id>');
}

// ── Webhook imza doğrulama (ayrı bir HTTP handler'da) ────────────────────────
//
// Express örneği (ham gövde şart):
//   app.post('/webhooks/imzala', express.raw({ type: 'application/json' }), (req, res) => {
//     const ok = Imzala.verifyWebhook(
//       process.env.IMZALA_WEBHOOK_SECRET,
//       req.body,                                   // ham Buffer
//       req.get('X-Imzala-Signature-256'),          // 'sha256=<hex>'
//     );
//     if (!ok) return res.status(401).end();
//     const event = JSON.parse(req.body.toString());
//     // event.type: demand.created / demand.completed / ...
//     res.status(200).end();
//   });

main().catch((err) => {
  if (err instanceof ImzalaError) {
    console.error(`\n❌ ImzalaError [${err.statusCode}]: ${err.message}`);
  } else {
    console.error('\n❌', err);
  }
  process.exit(1);
});
