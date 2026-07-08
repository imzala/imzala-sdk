# @imzala/embed-react

[![npm](https://img.shields.io/npm/v/@imzala/embed-react.svg)](https://www.npmjs.com/package/@imzala/embed-react)
[![npm downloads](https://img.shields.io/npm/dm/@imzala/embed-react.svg)](https://www.npmjs.com/package/@imzala/embed-react)
[![types](https://img.shields.io/npm/types/@imzala/embed-react.svg)](https://www.npmjs.com/package/@imzala/embed-react)

İmzala dijital imza platformunun resmi **React** SDK'sı. [`@imzala/embed`](../embed) tarayıcı widget'ının üzerine ince bir `<ImzalaSign />` bileşeni: backend'in ürettiği tek kullanımlık bir embed token'ı verirsiniz, bileşen gömülü imza iframe'ini mount eder ve tüm olayları (hazır / tamamlandı / reddedildi / yeniden boyutlandı) React callback prop'ları olarak yüzeye çıkarır.

```bash
npm install @imzala/embed-react
```

> ⚠️ Geliştirme aşamasında, yayın öncesi avukat onayı bekliyor. Bu bileşenin dayandığı `embed.createSession()` backend ucu şu an yalnızca test ortamında çalışır. Ayrıntı: [Önizleme ve kapsam](#önizleme-ve-kapsam) ve kök [RELEASING.md](../../RELEASING.md).

## İçindekiler

- [Gereksinimler](#gereksinimler)
- [Kurulum](#kurulum)
- [Hızlı başlangıç](#hızlı-başlangıç)
- [Next.js ve Sunucu Bileşenleri](#nextjs-ve-sunucu-bileşenleri)
- [Props (`ImzalaSignProps`)](#props-imzalasignprops)
- [Olaylar (callback payloadları)](#olaylar-callback-payloadları)
- [Yaşam döngüsü davranışı](#yaşam-döngüsü-davranışı)
- [Güvenlik](#güvenlik)
- [Sunucu-taraflı olan kısım ayrı](#️-sunucu-taraflı-olan-kısım-ayrı)
- [İmza sınıfı](#imza-sınıfı)
- [Önizleme ve kapsam](#önizleme-ve-kapsam)

## Gereksinimler

- **React 18 veya 19** (peer dependency). Bileşen React'e karşı derlenir, kendi React kopyasını paketlemez.
- Backend'te üretilmiş bir **embed token'ı**. Token'ı bir server SDK ile (örneğin [`@imzala/node`](../node)) `imzala.embed.createSession(...)` çağrısıyla alırsınız.

`@imzala/embed` bağımlılığı `@imzala/embed-react` ile birlikte otomatik kurulur; ayrıca kurmanıza gerek yoktur.

## Kurulum

```bash
npm install @imzala/embed-react
# react zaten projenizde peer dependency olarak bulunur
```

## Hızlı başlangıç

Akış her zaman iki adımdır: **(1)** sunucunuzda o taraf için tek kullanımlık bir embed token üretin, **(2)** token'ı tarayıcıya (bileşene) verin. API anahtarı hiçbir zaman tarayıcıya inmez.

```tsx
import { ImzalaSign } from '@imzala/embed-react';

// `embedToken` sizin kendi API'nizden (kimliği doğrulanmış bir istekle) gelir.
// Örneğin backend'te: const { embed_token } = await imzala.embed.createSession(demandId, { partyId });
function SigningStep({ embedToken }: { embedToken: string }) {
  return (
    <ImzalaSign
      token={embedToken}
      locale="tr"
      onReady={() => console.log('İmza yüzeyi hazır')}
      onComplete={({ demandId, partyId, signedAt }) => {
        console.log('İmzalandı:', demandId, partyId, signedAt);
        // kendi başarı ekranınıza yönlendirin
      }}
      onDecline={({ reason }) => console.log('Reddedildi:', reason)}
      onError={({ code, message }) => console.error('Hata:', code, message)}
    />
  );
}
```

Token'ı kendi backend'inizden çekmenin tipik hali:

```tsx
import { useEffect, useState } from 'react';
import { ImzalaSign } from '@imzala/embed-react';

function EmbeddedSigning({ demandId, partyId }: { demandId: string; partyId: string }) {
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    // Kendi API'niz: içeride server SDK ile embed session açar, token'ı döndürür.
    fetch('/api/imza/embed-token', {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ demandId, partyId }),
    })
      .then((r) => r.json())
      .then((d) => setToken(d.embed_token));
  }, [demandId, partyId]);

  if (!token) return <p>İmza yüzeyi hazırlanıyor…</p>;

  return (
    <ImzalaSign
      token={token}
      locale="tr"
      onComplete={({ demandId, partyId }) => {
        console.log('Tamamlandı:', demandId, partyId);
      }}
    />
  );
}
```

Bileşen kendi mount noktasını (`<div>`) yönetir; iframe bu div'in içine yerleştirilir. `token` değiştiğinde widget otomatik olarak kapanıp yeni token ile yeniden açılır, bileşen unmount olduğunda ise iframe ve olay dinleyicisi temizlenir.

## Next.js ve Sunucu Bileşenleri

`ImzalaSign` `'use client'` ile işaretlidir, yani Next.js App Router ile doğrudan uyumludur. `postMessage` ve iframe DOM'una eriştiği için yalnızca tarayıcıda (client component olarak) render edilir. Server component ağacınızda kullanacaksanız üst bileşeninizin de client olduğundan emin olun.

## Props (`ImzalaSignProps`)

`ImzalaSignProps`, [`@imzala/embed`](../embed)'in `EmbedOptions` tipinin (`container` hariç, çünkü bileşen mount noktasını kendisi yönetir) tamamını devralır ve tek zorunlu alan olarak `token` ekler.

| Prop | Tip | Varsayılan | Açıklama |
|---|---|---|---|
| `token` | `string` | Zorunlu | Backend'in ürettiği tek kullanımlık embed token'ı. |
| `baseUrl` | `string` | `https://e.imzala.org` | İmza yüzeyinin origin'i. Origin allowlist'in kaynağıdır. |
| `locale` | `'tr' \| 'en'` | (sunucu varsayılanı) | İmza sayfası dili. |
| `autoResize` | `boolean` | `true` | Iframe yüksekliğini içerikle otomatik ayarlar. |
| `onReady` | `(payload) => void` | yok | İmza yüzeyi yüklendi. |
| `onComplete` | `(p) => void` | yok | Taraf imzasını tamamladı (widget otomatik kapanır). |
| `onDecline` | `(p) => void` | yok | Taraf sözleşmeyi reddetti (widget otomatik kapanır). |
| `onCancel` | `() => void` | yok | Kullanıcı iptal etti / kapattı. |
| `onTimeout` | `(p) => void` | yok | Oturum veya token süresi doldu (widget otomatik kapanır). |
| `onError` | `(p) => void` | yok | Bir hata oluştu. |
| `onResize` | `(p) => void` | yok | Iframe içeriği yeniden boyutlandı. |
| `onFieldSigned` | `(p) => void` | yok | Tek bir alan imzalandı (canlı ilerleme takibi). |
| `onFieldUnsigned` | `(p) => void` | yok | Tek bir alan geri alındı. |

Callback prop'larını render başına yeniden oluşturmanız güvenlidir (satır içi arrow function verebilirsiniz): bileşen callback'leri bir ref'te tutar, bu yüzden `token` değişmedikçe iframe yeniden mount edilmez ama her zaman en güncel callback çağrılır.

## Olaylar (callback payload'ları)

Her callback, imza yüzeyinden gelen olayın payload'ını alır:

| Callback | Payload |
|---|---|
| `onReady` | `{ ... }` (yüzey meta bilgisi) |
| `onComplete` | `{ demandId: string; partyId: string; signedAt?: string }` |
| `onDecline` | `{ reason?: string }` |
| `onCancel` | (payload yok) |
| `onTimeout` | `{ code: 'TOKEN_EXPIRED' \| 'SESSION_TIMEOUT' }` |
| `onError` | `{ code: EmbedErrorCode; message?: string }` |
| `onResize` | `{ height: number }` |
| `onFieldSigned` / `onFieldUnsigned` | `{ fieldId?: string; fieldType?: string }` |

`EmbedErrorCode`: `TOKEN_EXPIRED` \| `TOKEN_USED` \| `ORIGIN_DENIED` \| `NETWORK` \| `UNKNOWN`. Tüm tip tanımları için bkz. [`@imzala/embed` README'si](../embed#seçenekler-embedoptions).

## Yaşam döngüsü davranışı

- **`token` değişimi:** eski widget kapanır, yeni token ile yeni bir iframe açılır. Tek seferde tek iframe bulunur.
- **`unmount`:** iframe kaldırılır ve `window` mesaj dinleyicisi temizlenir; unmount sonrası gelen mesajlar callback tetiklemez.
- **Terminal olaylar:** `complete`, `decline` ve `timeout` sonrası widget kendini otomatik kapatır. `resize` gibi olaylar iframe'i açık bırakır.

## Güvenlik

Bu bileşen ince bir React sarmalayıcısıdır ve `@imzala/embed`'in güvenlik modelini olduğu gibi devralır:

- **API anahtarı asla tarayıcıda bulunmaz.** Embed token'ı her zaman sunucuda, bir server SDK ile üretilir. Bileşen yalnızca hazır token'ı tüketir.
- **Token kısa ömürlü ve tek kullanımlıktır.** Token'ı URL query string'inde loglamayın, tarayıcıda kalıcı olarak saklamayın; yalnızca kimliği doğrulanmış kendi API'nizden iletin.
- **Parent origin allowlist.** Iframe yüklendiğinde bileşen, sayfanızın `window.location.origin`'ini bir `connect` handshake mesajıyla iframe'e bildirir; backend embed session'ı bu origin'e karşı doğrular ve beklenenle uyuşmazsa `ORIGIN_DENIED` döner. Ayrıca widget, iframe'den gelen `postMessage` olaylarını **yalnızca** `baseUrl`'in origin'inden ve **yalnızca** açtığı iframe'in `contentWindow`'undan kabul eder; başka origin veya kaynaktan gelen mesajlar sessizce yok sayılır.

Ayrıntı: [`@imzala/embed` güvenlik bölümü](../embed#güvenlik-origin-allowlist).

## ⚠️ Sunucu-taraflı olan kısım ayrı

Bu paket tarayıcı için tasarlanmıştır ve API anahtarı **içermez**. Embed token'ı üretmek (`imzala.embed.createSession(...)`) her zaman sunucu tarafında, bir [server SDK](../node) ile yapılır. Bu bileşen yalnızca o token'ı prop olarak alıp iframe'i açar.

## İmza sınıfı

Gömülü imza **dijital imza (SES/AES)** üretir; her imza zaman damgalıdır. Nitelikli veya güvenli elektronik imza (QES) **değildir**. İmza sınıfı sözleşme akışında belirlenir; bu bileşen imza geçerliliği hakkında ek hukuki iddiada bulunmaz.

## Önizleme ve kapsam

Bu bileşenin dayandığı `embed.createSession()` backend ucu şu an **yalnızca test ortamında** çalışır; gömülü imza özelliği henüz avukat onaylı prod-canlı değildir. Kapsam kararı [RELEASING.md](../../RELEASING.md)'de tutulur.

## Daha fazla

- [`@imzala/embed`](../embed): altta kullanılan saf tarayıcı widget'ı (`EmbedOptions`, olay protokolü, hata kodları)
- [`@imzala/node`](../node): backend'te embed token üretmek için server SDK
- [Monorepo README](../../README.md): tüm paketler ve genel desen
- [RELEASING.md](../../RELEASING.md): sürüm yayınlama ve kapsam kararları
