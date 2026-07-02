# @imzala/node

İmzala dijital imza platformu resmi Node.js / TypeScript SDK'sı.

> ⚠️ Geliştirme aşamasında, yayın öncesi avukat onayı bekliyor. Kapsam ve
> yayın kararları için monorepo kökündeki [RELEASING.md](../../RELEASING.md).

## Kurulum

```bash
npm install @imzala/node
```

Node.js 18.13+ gerektirir.

## Hızlı başlangıç

```ts
import { Imzala } from '@imzala/node';

const imzala = new Imzala({ apiKey: process.env.IMZALA_API_KEY! });

// Aktif şablonlarını listele
const { templates } = await imzala.templates.list();

// Bir şablondan yeni sözleşme (demand) oluştur
const demand = await imzala.demands.create({
  template_id: templates[0].id,
  party_mapping: [
    {
      template_party_id: templates[0].parties[0].id,
      first_name: 'Ahmet',
      last_name: 'Yılmaz',
      email: 'ahmet@example.com',
    },
  ],
});

console.log(demand.signing_urls); // her taraf için imzalama linki
```

`baseUrl` varsayılan olarak `https://api-prd.imzala.org`'dur; test ortamı
için `new Imzala({ apiKey, baseUrl: 'https://test-api.imzala.org' })`.

## Kaynaklar

- `imzala.templates.list({ page?, limit? })` / `.get(id)` / `.usage(id)`
- `imzala.demands.create(body)` / `.get(id)` / `.addItems(id, body)` /
  `.uploadDocument({ files, parties, order?, title?, description? })` /
  `.sendReminder(id, { force? })`
- `imzala.embed.createSession(demandId, { partyId })`: bkz. aşağıdaki
  gömülü imza notu
- `imzala.timestamps.create({ content, filename, idempotencyKey?, ... })`
- `imzala.me()`: API anahtarının sahibi (id, e-posta, workspace, kalan kredi)

Dosya yükleyen metodlar (`demands.uploadDocument`, `timestamps.create`)
Node `Buffer`/`Uint8Array` + dosya adı kabul eder (`FileInput` tipi);
`multer`'dan veya `fs.readFile`'dan gelen bytes'ı doğrudan geçebilirsiniz.

## Webhook doğrulama

```ts
import express from 'express';
import { verifyWebhook } from '@imzala/node';

app.post(
  '/webhooks/imzala',
  express.raw({ type: 'application/json' }), // ham body, JSON.parse ETMEDEN önce
  (req, res) => {
    const valid = verifyWebhook(
      process.env.IMZALA_WEBHOOK_SECRET!,
      req.body, // Buffer
      req.header('X-Imzala-Signature-256'),
    );
    if (!valid) return res.status(401).send('invalid signature');

    const event = JSON.parse(req.body.toString('utf8'));
    // ... event.type'a göre işle
    res.sendStatus(200);
  },
);
```

`verifyWebhook` asla exception fırlatmaz; geçersiz/eksik imzada `false` döner.
Body'yi parse edip yeniden serialize etmeyin, imza doğrulaması byte-byte
karşılaştırma yapar.

## Hata yönetimi

```ts
import {
  ImzalaError,
  ImzalaAuthError,
  ImzalaRateLimitError,
  ImzalaValidationError,
} from '@imzala/node';

try {
  await imzala.demands.get(id);
} catch (err) {
  if (err instanceof ImzalaRateLimitError) {
    console.log(`Rate limit: ${err.retryAfter} sn sonra tekrar dene`);
  } else if (err instanceof ImzalaAuthError) {
    console.log('API anahtarı geçersiz veya yetkisiz');
  } else if (err instanceof ImzalaValidationError) {
    console.log('İstek doğrulanamadı:', err.body);
  } else if (err instanceof ImzalaError) {
    console.log('İmzala API hatası:', err.statusCode, err.message);
  } else {
    throw err;
  }
}
```

Tüm hatalar `ImzalaError`'dan türer (`statusCode`, `body`, `code` alanları
ortak); 401/403 → `ImzalaAuthError`, 429 → `ImzalaRateLimitError`
(`retryAfter` saniye), 422 → `ImzalaValidationError`.

## ⚠️ Sunucu-taraflı

Bu paket **sadece sunucuda** kullanılır. API anahtarınızı tarayıcı
bundle'ına veya mobil uygulamaya asla gömmeyin: sızarsa hesabınızdaki tüm
sözleşme/şablon/zaman damgası işlemlerine erişim kazanılır. Paket, bir
tarayıcı ortamında (`window` tanımlıysa) constructor'da hata fırlatarak bunu
zorunlu kılar. Tarayıcıda imza almak için [`@imzala/embed`](../embed) veya
[`@imzala/embed-react`](../embed-react) kullanın.

## İmza sınıfı notu

İmzala dijital imza (SES) üretir; her imza zaman damgalıdır. Nitelikli/
güvenli elektronik imza (QES) DEĞİLDİR.

`imzala.embed.createSession(...)` şu an **yalnızca test ortamında** çalışır;
gömülü imza özelliği henüz avukat onaylı prod-canlı değildir. Kapsam kararı
[RELEASING.md](../../RELEASING.md)'de.

## Daha fazla

- [Monorepo README](../../README.md): tüm paketler + genel desen
- [RELEASING.md](../../RELEASING.md): sürüm yayınlama, kapsam kararları
