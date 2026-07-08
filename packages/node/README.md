# @imzala/node

[![npm](https://img.shields.io/npm/v/@imzala/node.svg)](https://www.npmjs.com/package/@imzala/node)
[![npm downloads](https://img.shields.io/npm/dm/@imzala/node.svg)](https://www.npmjs.com/package/@imzala/node)
[![types](https://img.shields.io/npm/types/@imzala/node.svg)](https://www.npmjs.com/package/@imzala/node)

İmzala dijital imza platformunun resmi **Node.js / TypeScript** SDK'sı. Sözleşme oluşturma, imza takibi, imzalı PDF ve sertifika indirme, denetim izi, şablon yönetimi ve zaman damgası işlemlerini tek bir tip-güvenli istemciyle yapın.

```bash
npm install @imzala/node
```

> **Sunucu-taraflı paket.** API anahtarınız hesabınızın tamamına erişir; tarayıcıya veya mobil uygulamaya gömmeyin. Tarayıcıda imza almak için [`@imzala/embed`](../embed) kullanın. Ayrıntı: [Sunucu-taraflı](#️-sunucu-taraflı) bölümü.

## İçindekiler

- [Gereksinimler](#gereksinimler)
- [Hızlı başlangıç](#hızlı-başlangıç)
- [Yapılandırma](#yapılandırma)
- [API referansı](#api-referansı)
  - [Sözleşmeler (demands)](#sözleşmeler-demands)
  - [Şablonlar (templates)](#şablonlar-templates)
  - [Gömülü imza (embed)](#gömülü-imza-embed)
  - [Zaman damgası (timestamps)](#zaman-damgası-timestamps)
  - [Hesap (me)](#hesap-me)
- [İmzalı PDF ve sertifika (binary)](#imzalı-pdf-ve-sertifika-binary)
- [Otomatik yeniden deneme](#otomatik-yeniden-deneme)
- [Sayfalama](#sayfalama)
- [Webhook doğrulama](#webhook-doğrulama)
- [Hata yönetimi](#hata-yönetimi)
- [Sunucu-taraflı](#️-sunucu-taraflı)
- [İmza sınıfı](#imza-sınıfı)

## Gereksinimler

- Node.js **18.13+** (LTS önerilir)
- `imz_` ile başlayan bir API anahtarı (Panel → Geliştirici → API Anahtarları)

## Hızlı başlangıç

```ts
import { Imzala } from '@imzala/node';

const imzala = new Imzala({ apiKey: process.env.IMZALA_API_KEY! });

// 1) Şablonları listele, birini seç
const { templates } = await imzala.templates.list();

// 2) Şablondan sözleşme oluştur (imza daveti otomatik gider)
const demand = await imzala.demands.create({
  template_id: templates[0].id,
  party_mapping: [
    {
      template_party_id: templates[0].parties[0].id,
      first_name: 'Ahmet',
      last_name: 'Yılmaz',
      email: 'ahmet@example.com',
      phone: '+905301112233',
    },
  ],
});
console.log(demand.signing_urls); // her taraf için imzalama linki

// 3) Durumu takip et
const status = await imzala.demands.get(demand.id);
console.log(`${status.parties.filter((p) => p.signed).length} taraf imzaladı`);

// 4) Tamamlanınca imzalı PDF'i indir
if (status.status === 'COMPLETED') {
  const pdf = await imzala.demands.getPdf(demand.id); // Buffer
  await require('node:fs/promises').writeFile('sozlesme.pdf', pdf);
}
```

## Yapılandırma

```ts
const imzala = new Imzala({
  apiKey: process.env.IMZALA_API_KEY!,
  baseUrl: 'https://api-prd.imzala.org', // varsayılan; test için test-api.imzala.org
  timeoutMs: 30_000,      // istek başına zaman aşımı (varsayılan 30sn)
  maxRetries: 2,          // güvenli GET'ler için (varsayılan 2, 0 = kapalı)
  retryBaseDelayMs: 300,  // backoff temel gecikmesi (varsayılan 300ms)
});
```

| Seçenek | Tip | Varsayılan | Açıklama |
|---|---|---|---|
| `apiKey` | `string` | yok (zorunlu) | `imz_<64 hex>` |
| `baseUrl` | `string` | `https://api-prd.imzala.org` | Test: `https://test-api.imzala.org` |
| `timeoutMs` | `number` | `30000` | Axios istek zaman aşımı |
| `maxRetries` | `number` | `2` | Yalnızca idempotent GET'ler; `0` kapatır |
| `retryBaseDelayMs` | `number` | `300` | Exponential backoff + jitter |

## API referansı

Tüm metodlar `{ success, data }` zarfını açar ve `data`'yı döndürür; hata durumunda tipli bir `ImzalaError` fırlatır (bkz. [Hata yönetimi](#hata-yönetimi)).

### Sözleşmeler (demands)

| Metod | Açıklama | Retry |
|---|---|---|
| `demands.create(body)` | Şablondan sözleşme oluştur + imza daveti gönder | ❌ POST |
| `demands.uploadDocument({ files, parties, order?, title?, description? })` | Şablonsuz, dosya yükleyerek sözleşme (1 PDF/DOC ya da 1-20 görsel) | ❌ |
| `demands.list({ status?, q?, from?, to?, templateId?, page?, limit?, sort? })` | Sözleşme listesi (counts-only, taraf PII'siz) | ✅ GET |
| `demands.get(id)` | Sözleşme detayı + taraf imza durumu (maskeli) | ✅ GET |
| `demands.getPdf(id)` | İmzalı sözleşme PDF'i → `Buffer` | GET (binary, retry yok) |
| `demands.getCertificate(id, { lang? })` | Tamamlanma sertifikası (PAdES B-T) → `Buffer` | GET (binary, retry yok) |
| `demands.getTimeline(id)` | İmza denetim izi (maskeli olaylar) | ✅ GET |
| `demands.cancel(id, { reason? })` | Bekleyen sözleşmeyi iptal et | ❌ POST |
| `demands.resendParty(id, partyId)` | Tekil tarafa daveti tekrar gönder | ❌ POST |
| `demands.delete(id)` | Tamamlanmamış sözleşmeyi sil | ❌ DELETE |
| `demands.addItems(id, body)` | Sayfa alanlarını (imza/form) yerleştir | ❌ POST |
| `demands.sendReminder(id, { force? })` | İmzalamamış taraflara hatırlatma | ❌ POST |

```ts
// Filtreli liste
const { demands } = await imzala.demands.list({ status: 'PENDING', limit: 20 });

// İptal
await imzala.demands.cancel(id, { reason: 'Anlaşma değişti' });

// Denetim izi
const { events } = await imzala.demands.getTimeline(id);
```

### Şablonlar (templates)

| Metod | Açıklama | Retry |
|---|---|---|
| `templates.list({ page?, limit? })` | Aktif şablonlar (tek sayfa) | ✅ GET |
| `templates.listAll({ page?, limit? })` | Tüm şablonları gezen async iterator | ✅ GET |
| `templates.get(id)` | Şablon detayı + taraflar + doldurulabilir alanlar | ✅ GET |
| `templates.usage(id)` | API kullanım kılavuzu (curl + JSON örneği) | ✅ GET |
| `templates.update(id, { name?, description?, category? })` | Şablon metadata güncelle | ❌ PATCH |
| `templates.delete(id)` | Şablon sil | ❌ DELETE |

### Gömülü imza (embed)

```ts
const session = await imzala.embed.createSession(demandId, { partyId });
// session.embed_url → bir <iframe>'e gömün (bkz. @imzala/embed)
```

Gömülü imza yalnızca SES/AES üretir (QES değil). Tarayıcı tarafı için [`@imzala/embed`](../embed).

### Zaman damgası (timestamps)

```ts
const ts = await imzala.timestamps.create({
  content: buffer,        // Buffer / Uint8Array
  filename: 'belge.pdf',
  idempotencyKey: 'unique-key', // tekrarları güvenli yapar (5dk pencere)
});
```

TÜBİTAK KAMU SM TSA ile RFC 3161 zaman damgası (var-olma + değişmezlik kanıtı; imza değildir).

### Hesap (me)

```ts
const me = await imzala.me(); // { id, email, workspace, kalan kredi }
```

## İmzalı PDF ve sertifika (binary)

`getPdf` ve `getCertificate` ham baytları bir `Buffer` olarak döndürür (JSON değil). Diske yazın veya stream'leyin:

```ts
import { writeFile } from 'node:fs/promises';

const pdf = await imzala.demands.getPdf(id);
await writeFile('sozlesme.pdf', pdf);

const cert = await imzala.demands.getCertificate(id, { lang: 'tr' });
await writeFile('sertifika.pdf', cert);
```

## Otomatik yeniden deneme

Yalnızca **GET (okuma)** uçları (`list/get/getTimeline`, `templates.*` okuma, `me()`) 429 (`Retry-After`'a uyarak) veya 5xx aldığında jitter'lı exponential backoff ile yeniden denenir. (`getPdf`/`getCertificate` GET'tir ama binary tek-çağrı olduğu için retry edilmez.)

**POST/PATCH/DELETE (yazma) uçları ASLA yeniden denenmez** ve bu yapılandırılamaz: bir `demands.create` tekrarı mükerrer sözleşme oluşturur. Retry mantığı yapısal olarak yalnızca GET'e bağlıdır.

## Sayfalama

```ts
for await (const template of imzala.templates.listAll({ limit: 50 })) {
  console.log(template.id, template.name);
}
```

`total`'a ulaşınca ya da bir sayfa `limit`'ten az öğe döndürünce durur (sonsuz döngü yok).

## Webhook doğrulama

```ts
import express from 'express';
import { verifyWebhook } from '@imzala/node';

app.post(
  '/webhooks/imzala',
  express.raw({ type: 'application/json' }), // ham body, JSON.parse ETMEDEN
  (req, res) => {
    const valid = verifyWebhook(
      process.env.IMZALA_WEBHOOK_SECRET!,
      req.body,                              // Buffer
      req.header('X-Imzala-Signature-256'),  // 'sha256=<hex>'
    );
    if (!valid) return res.status(401).send('invalid signature');

    const event = JSON.parse(req.body.toString('utf8'));
    // event.type: demand.created / demand.completed / demand.cancelled ...
    res.sendStatus(200);
  },
);
```

`verifyWebhook` exception fırlatmaz; geçersiz/eksik imzada `false` döner. Body'yi parse edip yeniden serialize etmeyin; imza byte-byte karşılaştırılır.

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
    console.log(`Rate limit: ${err.retryAfter}sn sonra tekrar dene`);
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

Tüm hatalar `ImzalaError`'dan türer (`statusCode`, `body`, `code`). 401/403 → `ImzalaAuthError`, 429 → `ImzalaRateLimitError` (`retryAfter`), 422 → `ImzalaValidationError`.

## ⚠️ Sunucu-taraflı

Bu paket **yalnızca sunucuda** kullanılır. API anahtarı sızarsa hesabınızdaki tüm sözleşme/şablon/zaman damgası işlemlerine erişilir. Paket, tarayıcı ortamında (`window` tanımlıysa) constructor'da hata fırlatır. Tarayıcıda imza için [`@imzala/embed`](../embed) / [`@imzala/embed-react`](../embed-react).

## İmza sınıfı

İmzala **dijital imza (SES)** üretir; her imza zaman damgalıdır. Nitelikli/güvenli elektronik imza (QES) DEĞİLDİR. Gömülü imza da SES/AES üretir.

## Daha fazla

- Tam API referansı: [api-docs.imzala.org](https://api-docs.imzala.org)
- Kullanım kılavuzu: [imzala.org/docs/api-sozlesme-yasam-dongusu](https://imzala.org/docs/api-sozlesme-yasam-dongusu)
- Çalışan örnekler: [`examples/node`](../../examples/node)
- [Monorepo README](../../README.md) · [RELEASING.md](../../RELEASING.md)
