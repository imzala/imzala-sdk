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
  - [Çok belgeli zarf (demands.documents, dispatch)](#çok-belgeli-zarf-demandsdocuments-dispatch)
  - [Şablonlar (templates)](#şablonlar-templates)
  - [Alan Şablonları (fieldTemplates)](#alan-şablonları-fieldtemplates)
  - [Kişiler (contacts)](#kişiler-contacts)
  - [Raporlar (reports)](#raporlar-reports)
  - [Gömülü imza (embed)](#gömülü-imza-embed)
  - [Zaman damgası (timestamps)](#zaman-damgası-timestamps)
  - [Hesap (me)](#hesap-me)
- [İmzalı PDF ve sertifika (binary)](#imzalı-pdf-ve-sertifika-binary)
- [Otomatik yeniden deneme](#otomatik-yeniden-deneme)
- [Sayfalama](#sayfalama)
- [Webhook doğrulama](#webhook-doğrulama)
- [Hata yönetimi](#hata-yönetimi)
- [Sık karşılaşılan hatalar](#sık-karşılaşılan-hatalar)
- [Sürüm uyumu](#sürüm-uyumu)
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
      first_name: 'Ayşe',
      last_name: 'Yılmaz',
      email: 'ayse@example.com',
      phone: '+905551112233',
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
| `maxRetries` | `number` | `2` | Yalnızca GET'ler; `0` kapatır (bkz. [Otomatik yeniden deneme](#otomatik-yeniden-deneme)) |
| `retryBaseDelayMs` | `number` | `300` | Exponential backoff + jitter |

API anahtarı ve `Idempotency-Key` değerleri yazdırılabilir ASCII olmalıdır; dosyadan okunan anahtarın sonundaki satır sonu gibi karakterler istek gönderilmeden `ImzalaValidationError` fırlatır (`statusCode` yok).

`X-Workspace-Id` başlığı için bir seçenek yoktur: organizasyon içinde üretilmiş anahtar kendi organizasyonuna bağlıdır, ayrıca başlık gerekmez. Kişisel anahtarla bir organizasyon adına çalışmak bu sürümde desteklenmez.

## API referansı

Tüm metodlar `{ success, data }` zarfını açar ve `data`'yı döndürür; hata durumunda tipli bir `ImzalaError` fırlatır (bkz. [Hata yönetimi](#hata-yönetimi)).

### Sözleşmeler (demands)

| Metod | Açıklama | Retry |
|---|---|---|
| `demands.create(body, { idempotencyKey? })` | Şablondan sözleşme oluştur + imza daveti gönder (`dispatch_notifications: false` ile sessiz taslak) | Anahtar varsa 429'da 1 kez |
| `demands.createBulk(body)` | Tek istekte en çok 10 sözleşme; her satır bağımsız, `failed` sayısını ve her satırın durumunu kontrol edin | ❌ POST |
| `demands.uploadDocument({ files, parties, order?, title?, description?, idempotencyKey?, fieldTemplateId?, onAnchorMiss?, sendInvitations?, force? })` | Şablonsuz, dosya yükleyerek sözleşme (1 PDF/DOC ya da 1-20 görsel) | Anahtar varsa 429'da 1 kez |
| `demands.list({ status?, q?, from?, to?, templateId?, page?, limit?, sort?, archived? })` | Sözleşme listesi (counts-only, taraf PII'siz); `sort` biçimi `alan:yön` (ör. `createdAt:desc`); `archived`: `'exclude'` / `'only'` / `'include'` (verilmezse `include`) | ✅ GET |
| `demands.get(id)` | Sözleşme detayı + taraf imza durumu (maskeli) | ✅ GET |
| `demands.getPdf(id)` | İmzalı sözleşme PDF'i → `Buffer` | ✅ GET |
| `demands.getDocumentPdf(id, documentId)` | Çok belgeli zarfta tek belgenin imzalı PDF'i → `Buffer` | ✅ GET |
| `demands.getCertificate(id, { lang? })` | Tamamlanma sertifikası (PAdES B-T) → `Buffer` | ✅ GET |
| `demands.getTimeline(id)` | İmza denetim izi (maskeli olaylar) | ✅ GET |
| `demands.cancel(id, { reason? })` | Bekleyen sözleşmeyi iptal et | ❌ POST |
| `demands.resendParty(id, partyId)` | Tekil tarafa daveti tekrar gönder | ❌ POST |
| `demands.delete(id)` | Tamamlanmamış sözleşmeyi sil | ❌ DELETE |
| `demands.addItems(id, body)` | Sayfa alanlarını (imza/form) yerleştir (`PAGE_ID_REQUIRED`, `INVALID_ITEM_TYPE`) | ❌ POST |
| `demands.updateStamp(id, itemId, body)` | Tek bir kaşe alanını `StampData` ile doldur (kısmi güncelleme: `null`/`''` alanı kaldırır, gönderilmeyen korunur; `DEMAND_PARTIALLY_SIGNED`) | ❌ PATCH |
| `demands.updateTerm(id, body)` | Süre ve yenileme takibini güncelle (kısmi: gönderilen anahtar yazılır, `null` temizler, gönderilmeyen korunur; `TERM_INVALID`, `DEMAND_ARCHIVED`) | ❌ PATCH |
| `demands.archive(id)` / `demands.unarchive(id)` | Tamamlanmış, iptal edilmiş veya süresi dolmuş sözleşmeyi arşivle / arşivden çıkar (durum değişmez; arşivdeki sözleşme değiştirilemez ve silinemez) | ❌ POST |
| `demands.sendReminder(id, { force? })` | İmzalamamış taraflara hatırlatma (5 dk pencerede `RATE_LIMITED`, `force: true` aşar) | ❌ POST |
| `demands.dispatch(id, { sendInvitations? })` | Sessiz hazırlanmış sözleşmeyi yayına al, davetleri gönder | ❌ POST |

```ts
// Filtreli liste
const { demands } = await imzala.demands.list({ status: 'PENDING', limit: 20 });

// İptal
await imzala.demands.cancel(id, { reason: 'Anlaşma değişti' });

// Denetim izi
const { events } = await imzala.demands.getTimeline(id);

// Tekrar-güvenli oluşturma: aynı anahtarla ikinci istek ikinci sözleşme üretmez
const created = await imzala.demands.create(body, { idempotencyKey: `siparis-${orderId}` });

// Alan Şablonu ile yükleme: her tarafta template_party_id zorunludur
const uploaded = await imzala.demands.uploadDocument({
  files: [{ content: pdfBuffer, filename: 'sozlesme.pdf' }],
  parties: [{ first_name: 'Ayşe', last_name: 'Yılmaz', email: 'ayse@example.com', template_party_id: partyTemplateId }],
  fieldTemplateId,
  onAnchorMiss: 'block', // çapa bulunamazsa sözleşme oluşturulmaz, kredi düşmez
  idempotencyKey: `yukleme-${orderId}`,
});
```

**Şablonun hangi belgelerinin gönderileceğini seçme.** İstekteki `documents` alanı şablonun varsayılanını bu isteğe özel değiştirir: `include` varsayılanı kapalı bir belgeyi ekler, `exclude` varsayılanı açık bir belgeyi çıkarır (her liste en çok 20 kimlik). Kimlikler `templates.get(id).documents[].id` alanından gelir. Alanı hiç göndermezseniz şablonun varsayılanı gider; davranış eskisiyle birebir aynıdır. Çıkarılan belge bu imza sürecine hiç girmez: imzacıya gösterilmez, imzalı PDF'te ve tamamlanma sertifikasında yer almaz, kredi hesabına katılmaz. Mevzuat gereği karşı tarafa verilmesi gereken bir belgeyi (ör. `doc_kind: PREINFO` ya da `KVKK_NOTICE`) başka bir kanaldan vermiyorsanız çıkarmayın; bu yükümlülük sözleşmeyi gönderene aittir.

```ts
const template = await imzala.templates.get(templateId);
const kvkk = template.documents?.find((d) => d.doc_kind === 'KVKK_NOTICE');

const demand = await imzala.demands.create({
  template_id: templateId,
  party_mapping: [{ template_party_id: roleId, first_name: 'Ayşe', last_name: 'Yılmaz', email: 'ayse@example.com' }],
  documents: { include: [kvkk!.id] }, // varsayılanı kapalı belgeyi bu isteğe ekle
});

// Toplu uçta seçim satır başınadır (batch geneli `options.documents` reddedilir)
await imzala.demands.createBulk({
  template_id: templateId,
  rows: [{ party_mapping: [...], documents: { exclude: [ekId] } }],
});
```

Geçersiz seçim `INVALID_DOCUMENT_SELECTION` (400) fırlatır; `details.reason` nedeni verir: `shape`, `unknown_document`, `conflict`, `empty`. Eşlediğiniz bir role hiç belge kalmazsa `PARTY_WITHOUT_DOCUMENTS` (409) döner ve sözleşme oluşmaz.

### Çok belgeli zarf (demands.documents, dispatch)

Bir sözleşme `dispatch_notifications: false` ile sessizce oluşturulur, belgeler eklenir, sonra tek çağrıyla yayına alınır. Belge uçları kimseye bildirim göndermez ve kredi düşmez; kredi yalnız `dispatch` anında düşer.

| Metod | Açıklama | Retry |
|---|---|---|
| `demands.documents.list(demandId, { view? })` | Zarftaki belgeler (`view: 'wizard'` atama ve karar sayılarını da verir) | ✅ GET |
| `demands.documents.create(demandId, body)` | Mevcut bir belgeyi zarfa ekle | ❌ POST |
| `demands.documents.upload(demandId, { file, title, idempotencyKey, docKind?, isRequired? })` | Dosya yükleyerek belge ekle; `idempotencyKey` zorunlu | 429'da 1 kez |
| `demands.documents.update(demandId, docId, body)` | Başlık / zorunluluk bayrakları | ❌ PATCH |
| `demands.documents.delete(demandId, docId)` | Belgeyi kaldır (son belge silinemez) | ❌ DELETE |
| `demands.documents.reorder(demandId, documentIds)` | Belge sırası (kimlik kümesi birebir eşleşmeli) | ❌ PUT |
| `demands.documents.setAssignments(demandId, docId, partyIds)` | Belgeyi imzalayacak taraflar | ❌ PUT |
| `demands.dispatch(demandId, { sendInvitations? })` | Zarfı yayına al | ❌ POST |

`upload` aynı `idempotencyKey` ile tekrar çağrılırsa yeni belge oluşmaz; sunucunun 409 `IDEMPOTENT_REPLAY` yanıtı normal sonuç olarak (mevcut belge) döner. Özellik hesabınızda açık değilse her belge metodu `ENVELOPE_MULTI_DOC_DISABLED` fırlatır; `list` boş liste döndürmez.

### Şablonlar (templates)

| Metod | Açıklama | Retry |
|---|---|---|
| `templates.list({ page?, limit? })` | Aktif şablonlar (tek sayfa) | ✅ GET |
| `templates.listAll({ page?, limit? })` | Tüm şablonları gezen async iterator | ✅ GET |
| `templates.get(id)` | Şablon detayı + taraflar + doldurulabilir alanlar + zarf belgeleri (`documents`) | ✅ GET |
| `templates.usage(id)` | API kullanım kılavuzu (curl + JSON örneği) | ✅ GET |
| `templates.update(id, { name?, description?, category? })` | Şablon metadata güncelle | ❌ PATCH |
| `templates.delete(id)` | Şablonu sil; kayıt 30 gün saklanır, mevcut sözleşmeler etkilenmez. Aktif (taslak veya imza bekleyen) sözleşmesi olan şablon silinemez (`409 TEMPLATE_IN_USE`) | ❌ DELETE |

`templates.list()` Alan Şablonlarını listelemez ve `templates.get()` bir Alan Şablonu kimliğine `404` döner; onlar için aşağıdaki `fieldTemplates` kaynağını kullanın.

### Alan Şablonları (fieldTemplates)

| Metod | Açıklama | Retry |
|---|---|---|
| `fieldTemplates.list({ page?, limit? })` | Alan Şablonları (tek sayfa) | ✅ GET |
| `fieldTemplates.get(id)` | Alan Şablonu detayı | ✅ GET |
| `fieldTemplates.previewLayout(id, { files, onAnchorMiss? })` | Yerleşimi tam bir PDF üzerinde kuru koşumla dener; sözleşme oluşturmaz, kredi harcamaz | ❌ POST |

```ts
const preview = await imzala.fieldTemplates.previewLayout(fieldTemplateId, {
  files: [{ content: pdfBuffer, filename: 'sozlesme.pdf' }],
  onAnchorMiss: 'drop', // verilmezse sunucu 'block' uygular
});
```

### Kişiler (contacts)

| Metod | Açıklama | Retry |
|---|---|---|
| `contacts.list({ q?, page?, limit?, sort?, companyId?, archived? })` | Kişiler (tek sayfa); `limit` 10 ile 100 arası, `sort` biçimi `-createdAt` (`-` öneki azalan) | ✅ GET |
| `contacts.listAll({ ... })` | Tüm kişileri gezen async iterator | ✅ GET |
| `contacts.create(body)` | Kişi oluştur (`CONTACT_DUPLICATE`: aynı e-posta veya telefon zaten var) | ❌ POST |

### Raporlar (reports)

```ts
const report = await imzala.reports.get(); // sözleşme durumlarının toplu sayımı; parametre almaz
```

### Gömülü imza (embed)

```ts
const session = await imzala.embed.createSession(demandId, { partyId });
// session.embed_url → bir <iframe>'e gömün (bkz. @imzala/embed)
```

Gömülü imza, 5070 sayılı Kanun m.3 anlamında elektronik imzadır; güvenli elektronik imza değildir ve AB hukuku bakımından gelişmiş veya nitelikli imza olarak sunulmaz. Tarayıcı tarafı için [`@imzala/embed`](../embed).

### Zaman damgası (timestamps)

```ts
const ts = await imzala.timestamps.create({
  content: buffer,        // Buffer / Uint8Array
  filename: 'belge.pdf',
  idempotencyKey: 'unique-key', // tekrarları güvenli yapar (5dk pencere)
});

const { timestamps } = await imzala.timestamps.list({ status: 'COMPLETED', sort: '-createdAt' }); // limit 10..100
const one = await imzala.timestamps.get(ts.id);
```

TÜBİTAK KAMU SM TSA ile RFC 3161 zaman damgası (var-olma + değişmezlik kanıtı; imza değildir). Bu uç anahtar başına dakikada 10 istekle sınırlıdır (`RATE_LIMIT_EXCEEDED`).

### Hesap (me)

```ts
const me = await imzala.me(); // { id, email, workspace, kalan kredi }
```

`me()` hiçbir kapsam (scope) istemez; geçerli her anahtarla çalışır.

## İmzalı PDF ve sertifika (binary)

`getPdf`, `getDocumentPdf` ve `getCertificate` ham baytları bir `Buffer` olarak döndürür (JSON değil). Hata durumunda diğer metodlar gibi `ImzalaError` fırlatır ve GET tekrar politikasına uyar. Diske yazın veya stream'leyin:

```ts
import { writeFile } from 'node:fs/promises';

const pdf = await imzala.demands.getPdf(id);
await writeFile('sozlesme.pdf', pdf);

const cert = await imzala.demands.getCertificate(id, { lang: 'tr' });
await writeFile('sertifika.pdf', cert);
```

## Otomatik yeniden deneme

Üç kural vardır; hepsi SDK'nın içindedir ve HTTP katmanına (axios) ayrıca bir tekrar politikası bağlanmaz:

1. **Okumalar (GET):** `demands.list/get/getTimeline/getPdf/getDocumentPdf/getCertificate`, `templates.*` okumaları, `fieldTemplates.list/get`, `contacts.list/listAll`, `timestamps.list/get`, `reports.get()`, `demands.documents.list` ve `me()` 429 veya 5xx aldığında en çok `maxRetries` kez (varsayılan 2) jitter'lı exponential backoff ile yeniden denenir. `0` kapatır.
2. **`Idempotency-Key` ile gönderilen yazmalar:** `demands.create(body, { idempotencyKey })`, `demands.uploadDocument({ idempotencyKey })`, `timestamps.create({ idempotencyKey })` ve `demands.documents.upload({ idempotencyKey })` bir 429 sonrasında **tam bir kez** yeniden denenir; sunucu aynı anahtar için ikinci kayıt oluşturmaz. İkinci 429, 5xx ve diğer tüm hatalar doğrudan fırlatılır. Anahtar verilmezse tek denemedir. `maxRetries` bu kuralı etkilemez.
3. **Diğer yazmalar hiç yeniden denenmez:** `createBulk`, `contacts.create`, `dispatch`, `sendReminder`, `cancel`, `resendParty`, `delete`, `addItems`, `updateStamp`, `updateTerm`, `archive`, `unarchive`, `templates.update/delete`, `embed.createSession` ve zarf belgesi `create/update/delete/reorder/setAssignments`. Tekrarlanan bir `createBulk` ikinci bir toplu iş, tekrarlanan bir `sendReminder` ikinci bir SMS/e-posta üretir.

Bekleme süresi `Retry-After` başlığından okunur (saniye ya da HTTP tarihi); başlık yoksa backoff gecikmesi uygulanır. **Bekleme tavanı 60 saniyedir:** sunucu daha uzun bir süre isterse SDK beklemek yerine 429'u fırlatır (`ImzalaRateLimitError.retryAfter` süreyi taşır). `NaN`, sonsuz ve negatif `Retry-After` değerleri yok sayılır.

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
    // event.type: demand.created / demand.completed / demand.expired /
    //             party.signed / party.viewed / party.rejected / kyc.completed / kyc.failed /
    //             contract.expiring / contract.ended / contract.advanced
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

Tüm hatalar `ImzalaError`'dan türer (`statusCode`, `body`, `code`, `codeDescription`). 401/403 → `ImzalaAuthError`, 429 → `ImzalaRateLimitError` (`retryAfter`, `rateLimit`), 422 → `ImzalaValidationError`. İstek gönderilmeden yakalanan yerel doğrulama hataları (geçersiz API anahtarı veya `Idempotency-Key` karakteri) de `ImzalaValidationError`'dır; `statusCode` tanımsızdır.

- **`code`:** sunucunun makine-okunur kodu. `{ error: "metin", code: "KOD" }` biçimindeki gövdelerde `code` alanından okunur; yalnız metin taşıyan gövdede `code` tanımsız kalır.
- **`codeDescription`:** kod için katalogdaki tek satırlık Türkçe açıklama. Katalog `IMZALA_ERROR_CODES` (73 kod) olarak dışa aktarılır; `describeErrorCode(code)` ve `isKnownErrorCode(code)` ile sorgulanır. Katalogda olmayan bir kod açıklamasız fırlatılır, yani yeni bir sunucu sürümü eski SDK'yı kırmaz.
- **`ImzalaRateLimitError.rateLimit`:** standart `RateLimit-Limit` / `RateLimit-Remaining` / `RateLimit-Reset` / `RateLimit-Policy` başlıkları (`{ limit, remaining, reset, policy }`). Birden çok sınır 429 döndürür ve farklı kod taşır; bkz. aşağıdaki tablo.

```ts
import { IMZALA_ERROR_CODES, describeErrorCode } from '@imzala/node';

describeErrorCode('TEMPLATE_IN_USE'); // 'Şablonun aktif (taslak veya imza bekleyen) sözleşmesi olduğu için silinemez.'
Object.keys(IMZALA_ERROR_CODES).length; // 73
```

## Sık karşılaşılan hatalar

| Durum | Kod | Ne yapmalı |
|---|---|---|
| 400 | `INVALID_PAGE` | `page` 1 veya daha büyük bir tam sayı olmalı |
| 400 | `INVALID_EXPIRY_DATE` | `expiry_date` geçerli bir takvim tarihi olmalı |
| 400 | `PAGE_ID_REQUIRED` | Alan yerleştirmede her öğede tam sayı `page_id` zorunlu |
| 400 | `INVALID_ITEM_TYPE` | Desteklenen alan tiplerinden birini kullanın (hata mesajı listeler) |
| 404 | (kodsuz) | Alan Şablonu kimliğiyle `templates.get()` çağrıldı; Alan Şablonları için `fieldTemplates.get()` kullanın (iki kavram ayrıdır) |
| 409 | `TEMPLATE_IN_USE` | Şablonun taslak veya imza bekleyen sözleşmesi var; tamamlanınca silinebilir |
| 409 | `DEMAND_NOT_DISPATCHABLE` | Sözleşme tamamlanmış veya iptal edilmiş; tekrar gönderilemez |
| 409 | `ENVELOPE_MULTI_DOC_DISABLED` | Çok belgeli zarf bu hesapta açık değil; `demands.documents.*` bu kodu fırlatır |
| 409 | `IDEMPOTENCY_KEY_REUSED` | Aynı anahtar farklı gövdeyle kullanıldı; yeni sözleşme için yeni anahtar üretin |
| 409 | `IDEMPOTENCY_UNVERIFIABLE` | Anahtar daha önce sözleşme üretti ama isteğin aynı olduğu doğrulanamadı; gövdedeki `demand_id` ile durumu sorgulayın, yeni anahtarla körlemesine tekrarlamayın |
| 409 | `DUPLICATE_SUSPECTED` | Anahtarsız istek son 10 dakikada gönderilmiş aynı içerikle eşleşti; kasten tekrarlamak için `force` gönderin |
| 422 | `FIELD_LAYOUT_UNRESOLVED` | Alan yerleşimi belgeye uygulanamadı; sözleşme oluşturulmadı, kredi düşülmedi. `onAnchorMiss` seçin veya belgeyi düzeltin |
| 429 | `RATE_LIMIT_EXCEEDED` | Genel sınır: anahtar başına dakikada 60 istek (zaman damgası ucunda 10). `Retry-After` kadar bekleyin |
| 429 | `TOO_MANY_REQUESTS` | Uca özgü sınır: belge yüklemede dakikada 30, davet tekrarı ve zarf gönderiminde saatte 300 |
| 429 | `RECIPIENT_RESEND_LIMIT` | Aynı alıcıya saatte en çok 3, günde en çok 10 davet tekrarı |

Sözleşme, şablon ve Alan Şablonu listelerinde `limit` üst sınırı 100'dür; daha büyük bir değer hata vermez, sessizce 100'e kırpılır. Kişi ve zaman damgası listelerinde `limit` 10 ile 100 arasında olmalıdır.

## Sürüm uyumu

| @imzala/node | Konuştuğu API | Durum |
|---|---|---|
| 1.0.0 | v1 (`1.10.1`) | Güncel: süre ve yenileme takibi, arşivleme, `contract.*` webhook olayları, imza yöntemi listesi ve sırası, kaşe verisi, belge başına değişkenler |
| 0.x | v1 (`1.7.x`) | Bakım dışı; 1.0.0'a yükseltin |

İmzala dış API'si **v1**'dir ve geriye dönük uyumludur: yeni alanlar opsiyonel, yeni davranışlar
opt-in, varsayılan davranış eskisidir. Kırıcı bir değişiklik gerekirse yeni bir major API sürümü
yayımlanır; eski sürüm duyurudan sonra **12 ay** çalışmaya devam eder.

## ⚠️ Sunucu-taraflı

Bu paket **yalnızca sunucuda** kullanılır. API anahtarı sızarsa hesabınızdaki tüm sözleşme/şablon/zaman damgası işlemlerine erişilir. Paket, tarayıcı ortamında (`window` tanımlıysa) constructor'da hata fırlatır. Tarayıcıda imza için [`@imzala/embed`](../embed) / [`@imzala/embed-react`](../embed-react).

## İmza sınıfı

Bu API ile tarayıcıdan atılan dijital imza, 5070 sayılı Elektronik İmza Kanunu m.3 anlamında elektronik imzadır ve her imza zaman damgalıdır. Güvenli elektronik imza değildir; AB hukuku bakımından gelişmiş veya nitelikli imza olarak sunulmaz. Gömülü imza da aynı niteliktedir. Telefon doğrulaması veya T.C. kimlik numarası kontrolü gibi ek adımlar delil değerini güçlendirir; imzayı gelişmiş ya da güvenli elektronik imzaya dönüştürmez.

## Daha fazla

- Tam API referansı: [api-docs.imzala.org](https://api-docs.imzala.org)
- Kullanım kılavuzu: [imzala.org/docs/api-sozlesme-yasam-dongusu](https://imzala.org/docs/api-sozlesme-yasam-dongusu)
- Çalışan örnekler: [`examples/node`](../../examples/node) (altı senaryo)
- Değişiklik günlüğü: [CHANGELOG.md](./CHANGELOG.md)
- [Monorepo README](../../README.md)
