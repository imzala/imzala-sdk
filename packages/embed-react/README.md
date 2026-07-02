# @imzala/embed-react

İmzala dijital imza platformu resmi React SDK'sı: [`@imzala/embed`](../embed)
üzerine ince bir `<ImzalaSign />` sarmalayıcı bileşeni.

> ⚠️ Geliştirme aşamasında, yayın öncesi avukat onayı bekliyor.
> **Backend `embed.createSession()` şu an yalnızca test ortamında
> çalışır.** Bkz. aşağıdaki önizleme/test notu ve kök
> [RELEASING.md](../../RELEASING.md).

## Kurulum

```bash
npm install @imzala/embed-react
```

`react` peer dependency olarak beklenir (React 18+ önerilir). `@imzala/embed`
otomatik olarak kurulur.

## Hızlı başlangıç

Önce **sunucunuzda** (bir server SDK ile) o taraf için tek kullanımlık bir
embed token alın, sonra tarayıcıya (bileşene) geçirin:

```tsx
import { ImzalaSign } from '@imzala/embed-react';

function SigningStep({ embedToken }: { embedToken: string }) {
  return (
    <ImzalaSign
      token={embedToken}
      locale="tr"
      onComplete={({ demandId, partyId, signedAt }) => {
        console.log('İmzalandı:', demandId, partyId, signedAt);
      }}
      onDecline={({ reason }) => console.log('Reddedildi:', reason)}
      onError={({ code, message }) => console.error('Hata:', code, message)}
    />
  );
}
```

Bileşen, `'use client'` işaretlidir (Next.js App Router uyumlu). `token`
değiştiğinde widget otomatik olarak kapanıp yeniden açılır; unmount
olduğunda temizlenir.

## Props (`ImzalaSignProps`)

`token` (zorunlu, `string`) dışında, [`@imzala/embed`](../embed)'in
`EmbedOptions`'ının tamamını (`container` hariç, bileşen kendi mount
noktasını yönetir) doğrudan prop olarak alır: `baseUrl`, `locale`,
`autoResize`, `onReady`, `onComplete`, `onDecline`, `onCancel`, `onTimeout`,
`onError`, `onResize`, `onFieldSigned`, `onFieldUnsigned`. Tüm callback
tipleri için bkz. [`@imzala/embed` README'si](../embed#seçenekler-embedoptions).

## Güvenlik

Origin allowlist ve token yaşam döngüsü kuralları `@imzala/embed` ile
aynıdır. Bkz. [o paketin güvenlik bölümü](../embed#güvenlik-origin-allowlist).
Bu bileşen sadece bir React sarmalayıcısıdır, ek bir güvenlik yüzeyi
eklemez/çıkarmaz.

## ⚠️ Sunucu-taraflı olan kısım ayrı

Bu paket tarayıcı için tasarlanmıştır. API anahtarı **içermez** ve
içermemelidir. Embed token'ı üretmek (`imzala.embed.createSession(...)`)
her zaman sunucu tarafında, bir [server SDK](../node) ile yapılır; bu
bileşen yalnızca o token'ı prop olarak alıp iframe'i açar.

## İmza sınıfı notu

İmzala dijital imza (SES) üretir; her imza zaman damgalıdır. Nitelikli/
güvenli elektronik imza (QES) DEĞİLDİR.

**Önizleme/test:** bu bileşenin dayandığı `embed.createSession()` backend
endpoint'i şu an **yalnızca test ortamında** çalışır; gömülü imza özelliği
henüz avukat onaylı prod-canlı değildir. Kapsam kararı
[RELEASING.md](../../RELEASING.md)'de.

## Daha fazla

- [Monorepo README](../../README.md): tüm paketler + genel desen
- [`@imzala/embed`](../embed): altta kullanılan saf tarayıcı widget'ı
- [RELEASING.md](../../RELEASING.md): sürüm yayınlama, kapsam kararları
