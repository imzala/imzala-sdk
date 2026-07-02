# @imzala/embed

İmzala dijital imza platformu resmi tarayıcı SDK'sı: gömülü imza (embedded
signing) iframe widget'ı.

> ⚠️ Geliştirme aşamasında, yayın öncesi avukat onayı bekliyor.
> **`embed.createSession()` (backend) şu an yalnızca test ortamında
> çalışır.** Bkz. aşağıdaki önizleme/test notu ve kök
> [RELEASING.md](../../RELEASING.md).

## Kurulum

```bash
npm install @imzala/embed
```

Saf tarayıcı paketidir, framework bağımlılığı yoktur. React kullanıyorsanız
[`@imzala/embed-react`](../embed-react) sarmalayıcısı daha ergonomiktir.

## Hızlı başlangıç

Önce **sunucunuzda** (bir server SDK ile) o taraf için tek kullanımlık bir
embed token alın, sonra tarayıcıya geçirin:

```ts
// sunucu (örn. @imzala/node ile)
const session = await imzala.embed.createSession(demandId, { partyId });
// session.embed_token'ı tarayıcıya döndür (örn. bir API response'unda)
```

```ts
// tarayıcı
import { ImzalaEmbed } from '@imzala/embed';

const embed = new ImzalaEmbed({
  container: document.getElementById('imza-widget'), // yoksa tam-ekran modal açılır
  locale: 'tr',
  onComplete: ({ demandId, partyId, signedAt }) => {
    console.log('İmzalandı:', demandId, partyId, signedAt);
  },
  onDecline: ({ reason }) => console.log('Reddedildi:', reason),
  onError: ({ code, message }) => console.error('Hata:', code, message),
});

embed.open(embedToken); // sunucudan gelen token
```

`container` verilmezse widget, tam ekranı kaplayan bir `<div aria-modal>`
overlay içinde açılır. `embed.close()` iframe'i ve varsa modal'ı kaldırır,
event listener'ı temizler.

## Seçenekler (`EmbedOptions`)

| Alan | Tip | Açıklama |
|---|---|---|
| `container` | `HTMLElement \| null` | Iframe'in mount edileceği element. Yoksa tam-ekran modal. |
| `baseUrl` | `string` | Varsayılan `https://e.imzala.org`. |
| `locale` | `'tr' \| 'en'` | Imza sayfası dili. |
| `autoResize` | `boolean` | Varsayılan `true` (iframe yüksekliği içerikle otomatik ayarlanır). |
| `onReady` | `(payload) => void` | Iframe içeriği yüklendi. |
| `onComplete` | `(p: { demandId, partyId, signedAt? }) => void` | Taraf imzasını tamamladı. |
| `onDecline` | `(p: { reason? }) => void` | Taraf sözleşmeyi reddetti. |
| `onCancel` | `() => void` | Kullanıcı iptal etti / pencereyi kapattı. |
| `onTimeout` | `(p: { code: 'TOKEN_EXPIRED' \| 'SESSION_TIMEOUT' }) => void` | Oturum/token süresi doldu. |
| `onError` | `(p: { code: EmbedErrorCode, message? }) => void` | Hata (bkz. aşağıdaki kodlar). |
| `onResize` | `(p: { height: number }) => void` | Iframe içeriği yeniden boyutlandı. |
| `onFieldSigned` / `onFieldUnsigned` | `(p: { fieldId?, fieldType? }) => void` | Tek bir alan imzalandı/geri alındı (canlı ilerleme takibi için). |

`EmbedErrorCode`: `TOKEN_EXPIRED` \| `TOKEN_USED` \| `ORIGIN_DENIED` \|
`NETWORK` \| `UNKNOWN`.

## Güvenlik (origin allowlist)

Widget, iframe'den gelen `postMessage` olaylarını **yalnızca** `baseUrl`'in
origin'inden (varsayılan `https://e.imzala.org`) ve **yalnızca** açtığı
iframe'in `contentWindow`'undan kabul eder; başka bir origin veya kaynaktan
gelen mesajlar sessizce yok sayılır. Iframe yüklendiğinde widget, kendi
`window.location.origin`'ini bir `connect` handshake mesajıyla iframe'e
bildirir; bu, backend'in embed session'ı **hangi origin'den açıldığını**
doğrulamasını sağlar (`ORIGIN_DENIED` hatası, iframe'i yerleştirdiğiniz
sayfanın origin'i beklenenle uyuşmadığında döner).

Token kısa ömürlü ve tek kullanımlıktır: backend'te üretilir, tarayıcıya
yalnızca kendi API'nizden (kimliği doğrulanmış bir istekle) iletilmelidir.
Token'ı asla URL query string'inde loglamayın veya kalıcı olarak saklamayın.

## ⚠️ Sunucu-taraflı olan kısım ayrı

Bu paket tarayıcı için tasarlanmıştır. API anahtarı **içermez** ve
içermemelidir. Embed token'ı üretmek (`imzala.embed.createSession(...)`)
her zaman sunucu tarafında, bir [server SDK](../node) ile yapılır; bu paket
yalnızca o token'ı tüketip iframe'i açar.

## İmza sınıfı notu

İmzala dijital imza (SES) üretir; her imza zaman damgalıdır. Nitelikli/
güvenli elektronik imza (QES) DEĞİLDİR.

**Önizleme/test:** bu widget'ın dayandığı `embed.createSession()` backend
endpoint'i şu an **yalnızca test ortamında** çalışır; gömülü imza özelliği
henüz avukat onaylı prod-canlı değildir. Kapsam kararı
[RELEASING.md](../../RELEASING.md)'de.

## Daha fazla

- [Monorepo README](../../README.md): tüm paketler + genel desen
- [`@imzala/embed-react`](../embed-react): React sarmalayıcısı
- [RELEASING.md](../../RELEASING.md): sürüm yayınlama, kapsam kararları
