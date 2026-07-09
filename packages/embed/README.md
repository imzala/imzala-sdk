# @imzala/embed

[![npm](https://img.shields.io/npm/v/@imzala/embed.svg)](https://www.npmjs.com/package/@imzala/embed)
[![npm downloads](https://img.shields.io/npm/dm/@imzala/embed.svg)](https://www.npmjs.com/package/@imzala/embed)
[![types](https://img.shields.io/npm/types/@imzala/embed.svg)](https://www.npmjs.com/package/@imzala/embed)

İmzala gömülü imza (embedded signing) widget'ı: kendi web uygulamanızın içinde, tek kullanımlık bir token ile İmzala imza sayfasını bir `<iframe>`'de açar ve `postMessage` el sıkışmasını (ready / complete / decline / resize ...) sizin adınıza yönetir. Bağımlılığı yoktur, saf tarayıcı kodudur.

```bash
npm install @imzala/embed
```

> **Tarayıcı paketi.** Bu paket API anahtarı **kullanmaz** ve **içermemelidir**. İmza oturumunu (embed token) her zaman **sunucunuzda** [`@imzala/node`](../node) (veya başka bir server SDK) ile üretir, yalnızca kısa ömürlü token'ı tarayıcıya geçirirsiniz. Ayrıntı: [Güvenlik (origin allowlist)](#güvenlik-origin-allowlist) bölümü.

## İçindekiler

- [Gereksinimler](#gereksinimler)
- [Nasıl çalışır](#nasıl-çalışır)
- [Kurulum](#kurulum)
- [Hızlı başlangıç](#hızlı-başlangıç)
- [Seçenekler (EmbedOptions)](#seçenekler-embedoptions)
- [Olaylar (events)](#olaylar-events)
- [API referansı](#api-referansı)
- [Otomatik yükseklik (auto-resize)](#otomatik-yükseklik-auto-resize)
- [Modal veya gömülü (container)](#modal-veya-gömülü-container)
- [Güvenlik (origin allowlist)](#güvenlik-origin-allowlist)
- [React için](#react-için)
- [İmza sınıfı](#imza-sınıfı)
- [Daha fazla](#daha-fazla)

## Gereksinimler

- Modern bir tarayıcı (ES2019+). `window`, `document` ve `postMessage` gerektirir; Node.js'te veya sunucu tarafında çalışmaz.
- Bir **backend** (imza oturumunu üreten): [`@imzala/node`](../node) ya da diğer server SDK'lar.
- API anahtarınızın `embed_allowed_origins` (izinli origin) listesine, widget'ı gömeceğiniz sayfanın origin'i eklenmiş olmalı. Bkz. [Güvenlik](#güvenlik-origin-allowlist).

## Nasıl çalışır

Üç adımlı, üç katmanlı bir akıştır:

1. **Sunucu (backend):** `imzala.embed.createSession(demandId, { partyId })` çağrısı o taraf için **tek kullanımlık, kısa ömürlü** bir imza oturumu üretir. Dönen alanlar: `embed_token`, `expires_at`, `embed_url`.
2. **Aktarım:** Backend yalnızca `embed_token`'ı (API anahtarı değil) tarayıcıya iletir.
3. **Tarayıcı (bu paket):** `new ImzalaEmbed({ ... }).open(embed_token)` iframe'i mount eder, İmzala origin'i ile `postMessage` el sıkışmasını kurar ve olayları callback'lerinize aktarır.

## Kurulum

### npm / bundler (ESM veya CommonJS)

```bash
npm install @imzala/embed
```

```ts
import { ImzalaEmbed } from '@imzala/embed';
```

Paket üç formatta gelir: **ESM** (`dist/index.js`), **CommonJS** (`dist/index.cjs`) ve tip tanımları (`dist/index.d.ts`). Bundler doğru formatı otomatik seçer. Saf tarayıcı kodudur, framework bağımlılığı yoktur.

### CDN / `<script>` (IIFE build)

Bundler kullanmıyorsanız, IIFE build'i doğrudan bir `<script>` etiketiyle yükleyebilirsiniz. Global değişken `ImzalaEmbed` bir namespace nesnesidir; sınıf `ImzalaEmbed.ImzalaEmbed` altındadır:

```html
<!-- Üretimde sürümü sabitleyin (@X.Y.Z) ve integrity (SRI) + crossorigin ekleyin.
     integrity hash'ini yayınlanan dosyadan hesaplayın:
     curl -s https://unpkg.com/@imzala/embed@0.1.0/dist/index.global.js | openssl dgst -sha384 -binary | openssl base64 -A -->
<script
  src="https://unpkg.com/@imzala/embed@0.1.0/dist/index.global.js"
  integrity="sha384-<yayınlanan-dosyadan-hesaplanan-hash>"
  crossorigin="anonymous"></script>
<script>
  // IIFE global bir namespace nesnesidir; sınıf .ImzalaEmbed altındadır.
  const embed = new ImzalaEmbed.ImzalaEmbed({
    onComplete: ({ demandId, partyId }) => console.log('İmzalandı', demandId, partyId),
  });
  embed.open(EMBED_TOKEN); // backend'den gelen tek kullanımlık token
</script>
```

## Hızlı başlangıç

**1) Sunucuda** (Node.js örneği, imza oturumu + token üretimi):

```ts
import { Imzala } from '@imzala/node';

const imzala = new Imzala({ apiKey: process.env.IMZALA_API_KEY! });

// İlgili taraf için tek kullanımlık gömülü imza oturumu
const session = await imzala.embed.createSession(demandId, { partyId });
// session = { embed_token, expires_at, embed_url }

// Yalnızca embed_token'ı tarayıcıya gönderin (API anahtarını ASLA göndermeyin).
res.json({ embedToken: session.embed_token });
```

**2) Tarayıcıda** (widget'ı mount edin):

```ts
import { ImzalaEmbed } from '@imzala/embed';

const embed = new ImzalaEmbed({
  container: document.getElementById('imza-alani'), // opsiyonel; yoksa tam ekran modal açılır
  locale: 'tr',
  onReady: () => console.log('İmza sayfası hazır'),
  onComplete: ({ demandId, partyId, signedAt }) => {
    console.log('İmza tamamlandı', demandId, partyId, signedAt);
    // widget kendini otomatik kapatır; burada yönlendirme/teşekkür ekranı gösterin
  },
  onDecline: ({ reason }) => console.log('İmza reddedildi', reason),
  onError: ({ code, message }) => console.error('Hata', code, message),
});

embed.open(embedToken); // backend'den aldığınız session.embed_token

// İşiniz bitince veya sayfa değişince temizleyin
// embed.close();
```

## Seçenekler (EmbedOptions)

`new ImzalaEmbed(options)` constructor'ına geçilir. Tümü opsiyoneldir.

| Seçenek | Tip | Varsayılan | Açıklama |
|---|---|---|---|
| `container` | `HTMLElement \| null` | `null` | iframe'in gömüleceği element. Verilmezse tam ekran bir modal overlay oluşturulur. |
| `baseUrl` | `string` | `https://e.imzala.org` | İmza sayfasının kök URL'i. Yalnızca `origin`'i kullanılır. Test için `https://test-esign.imzala.org`. |
| `locale` | `'tr' \| 'en'` | (yok) | İmza arayüzü dili. Verilmezse iframe kendi varsayılanını kullanır. |
| `autoResize` | `boolean` | `true` | `resize` olayı geldiğinde iframe yüksekliğini otomatik ayarla. `false` ise yükseklik sabit kalır (`onResize` callback'i yine de tetiklenir). |
| `onReady` | `(p) => void` | (yok) | İmza sayfası yüklendi ve etkileşime hazır. |
| `onComplete` | `(p: { demandId, partyId, signedAt? }) => void` | (yok) | İmza tamamlandı. Widget bundan sonra otomatik kapanır. |
| `onDecline` | `(p: { reason? }) => void` | (yok) | Kullanıcı imzayı reddetti. Widget otomatik kapanır. |
| `onCancel` | `() => void` | (yok) | Kullanıcı imzalamadan çıktı/vazgeçti (widget açık kalır). |
| `onTimeout` | `(p: { code: 'TOKEN_EXPIRED' \| 'SESSION_TIMEOUT' }) => void` | (yok) | Oturum/token süresi doldu. Widget otomatik kapanır. |
| `onError` | `(p: { code: EmbedErrorCode, message? }) => void` | (yok) | Bir hata oluştu (bkz. `EmbedErrorCode`). |
| `onResize` | `(p: { height: number }) => void` | (yok) | İçerik yüksekliği değişti (px). |
| `onFieldSigned` | `(p: { fieldId?, fieldType? }) => void` | (yok) | Bir alan imzalandı/dolduruldu (canlı ilerleme takibi). |
| `onFieldUnsigned` | `(p: { fieldId?, fieldType? }) => void` | (yok) | Bir alanın imzası/değeri geri alındı. |

`EmbedErrorCode` değerleri: `'TOKEN_EXPIRED' | 'TOKEN_USED' | 'ORIGIN_DENIED' | 'NETWORK' | 'UNKNOWN'`.

## Olaylar (events)

iframe, İmzala origin'inden `postMessage` ile şu olayları gönderir; her biri ilgili callback'e eşlenir. Yalnızca doğru origin **ve** doğru iframe kaynağından gelen, `imzala-embed` namespace'li mesajlar işlenir; diğer her şey sessizce yok sayılır.

| Olay | Callback | Payload | Sonrasında widget |
|---|---|---|---|
| `ready` | `onReady` | (sayfaya özel) | açık kalır |
| `complete` | `onComplete` | `{ demandId, partyId, signedAt? }` | **otomatik kapanır** |
| `decline` | `onDecline` | `{ reason? }` | **otomatik kapanır** |
| `cancel` | `onCancel` | (yok) | açık kalır |
| `timeout` | `onTimeout` | `{ code: 'TOKEN_EXPIRED' \| 'SESSION_TIMEOUT' }` | **otomatik kapanır** |
| `error` | `onError` | `{ code, message? }` | açık kalır |
| `resize` | `onResize` | `{ height }` | açık kalır (yükseklik güncellenir) |
| `field_signed` | `onFieldSigned` | `{ fieldId?, fieldType? }` | açık kalır |
| `field_unsigned` | `onFieldUnsigned` | `{ fieldId?, fieldType? }` | açık kalır |

Bilinmeyen olay adları ve yeni protokol sürümleri ileri-uyumluluk için sessizce yok sayılır (hata fırlatmaz).

## API referansı

```ts
class ImzalaEmbed {
  constructor(options: EmbedOptions);
  open(embedToken: string): void;
  close(): void;
}
```

| Üye | Açıklama |
|---|---|
| `new ImzalaEmbed(options)` | Widget'ı yapılandırır. Henüz DOM'a dokunmaz. |
| `open(embedToken)` | `container` (veya yeni modal) içine iframe mount eder, mesaj dinleyicisini kurar, iframe yüklenince origin'i sabitleyen `connect` el sıkışmasını gönderir. Zaten açıksa önce mevcut örneği kapatır (dinleyici sızıntısını önler). |
| `close()` | Mesaj dinleyicisini kaldırır, iframe'i ve (varsa) modal overlay'i DOM'dan siler. `complete` / `decline` / `timeout` olaylarında otomatik çağrılır. |

`open()` şu URL'i kurar: `${origin}/embed/sign?token=<encodeURIComponent(embedToken)>` (+ `locale` verilmişse `&lang=<locale>`). iframe `camera; clipboard-write` yeteneklerine izin verir ve `referrerpolicy="origin-when-cross-origin"` ile açılır.

## Otomatik yükseklik (auto-resize)

Varsayılan olarak (`autoResize: true`) iframe başlangıçta `600px`'tir; imza sayfası içeriğe göre `resize` olayı gönderdikçe yükseklik otomatik güncellenir, böylece iç kaydırma çubuğu oluşmaz. Yüksekliği kendiniz yönetmek isterseniz `autoResize: false` verin; bu durumda yükseklik sabit kalır ama `onResize` callback'i yine de her değişiklikte tetiklenir (kendi layout mantığınız için).

## Modal veya gömülü (container)

- **`container` verirseniz**, iframe doğrudan o element'e gömülür (sayfa akışının parçası olur).
- **`container` vermezseniz**, `open()` sayfa üstünde tam ekran, yarı saydam bir modal overlay (`aria-modal="true"`) oluşturup iframe'i onun içine yerleştirir. `close()` bu overlay'i tamamen kaldırır.

## Güvenlik (origin allowlist)

Bu widget çok katmanlı bir güven modeli üzerine kuruludur:

- **API anahtarı yalnızca sunucunuzda kalır.** Bu paket API anahtarı almaz; yalnızca kısa ömürlü `embed_token`'ı kullanır. Anahtar tarayıcıya sızarsa hesabınızın tamamı riske girer, o yüzden token'ı her zaman backend'de üretin.
- **Token tek kullanımlık ve kısa ömürlüdür.** `createSession` her çağrıda yeni bir token üretir; kullanıldıktan veya `expires_at` geçtikten sonra geçersizdir (`onTimeout` / `onError: TOKEN_EXPIRED | TOKEN_USED`). Token'ı URL query string'inde loglamayın veya kalıcı olarak saklamayın.
- **Origin allowlist (frame-ancestors).** İmza sayfası yalnızca API anahtarınızın `embed_allowed_origins` listesindeki origin'lere gömülebilir. Sayfanızın origin'i listede yoksa tarayıcı, iframe'i sunucunun `Content-Security-Policy: frame-ancestors` başlığı gereği bloke eder (widget yüklenmez). İzinli origin'leri Panel üzerinden API anahtarınıza ekleyin. iframe yüklendiğinde widget, kendi `window.location.origin`'ini bir `connect` el sıkışması mesajıyla İmzala origin'ine bildirir; origin beklenenle uyuşmazsa `onError: ORIGIN_DENIED` döner.
- **Katı mesaj filtreleme.** Gelen `postMessage`'lar üç kapıdan geçer: (1) origin, İmzala origin'i ile birebir eşleşmeli, (2) mesaj kaynağı tam olarak widget'ın kendi iframe'i olmalı, (3) `type` alanı `imzala-embed` olmalı. Aksi halde mesaj işlenmeden atılır; böylece sayfadaki başka çerçeveler/eklentiler olayları taklit edemez.

## React için

React (özellikle Next.js App Router) kullanıyorsanız, bu widget'ı bir `<ImzalaSign />` bileşenine saran resmi sarmalayıcı: [`@imzala/embed-react`](../embed-react). Aynı origin allowlist ve token yaşam döngüsü kuralları geçerlidir; ek bir güvenlik yüzeyi getirmez.

## İmza sınıfı

İmzala **dijital imza** üretir; her imza zaman damgalıdır. Gömülü imza akışı yalnızca SES/AES sınıfındadır. Nitelikli/güvenli elektronik imza (QES) **değildir** ve bu widget böyle bir iddiada bulunmaz.

## Daha fazla

- [`@imzala/node`](../node): imza oturumunu (`embed.createSession`) üreten sunucu SDK'sı
- [`@imzala/embed-react`](../embed-react): React sarmalayıcı bileşeni
- Çalışan örnek: [`examples/embed`](../../examples/embed)
- [Monorepo README](../../README.md)
