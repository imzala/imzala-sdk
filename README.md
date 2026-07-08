# İmzala SDK

İmzala dijital imza platformunun resmi çok-dilli SDK seti + gömülü imza (embedded signing). Sözleşme oluşturma, imza takibi, imzalı PDF/sertifika indirme, denetim izi, şablon yönetimi ve zaman damgası işlemleri; 5 sunucu dili + 2 tarayıcı paketi.

[![npm](https://img.shields.io/npm/v/@imzala/node.svg?label=%40imzala%2Fnode)](https://www.npmjs.com/package/@imzala/node)
[![PyPI](https://img.shields.io/pypi/v/imzala.svg?label=imzala)](https://pypi.org/project/imzala/)
[![NuGet](https://img.shields.io/nuget/v/Imzala.svg?label=Imzala)](https://www.nuget.org/packages/Imzala/)
[![Packagist](https://img.shields.io/packagist/v/imzala/imzala-php.svg?label=imzala%2Fimzala-php)](https://packagist.org/packages/imzala/imzala-php)
[![Maven Central](https://img.shields.io/maven-central/v/org.imzala/imzala-java.svg?label=org.imzala%3Aimzala-java)](https://central.sonatype.com/artifact/org.imzala/imzala-java)

## Paketler

| Paket | Registry | Kurulum |
|---|---|---|
| [`@imzala/node`](./packages/node/README.md) | npm | `npm install @imzala/node` |
| [`imzala`](./packages/python/README.md) | PyPI | `pip install imzala` |
| [`Imzala`](./packages/dotnet/README.md) | NuGet | `dotnet add package Imzala` |
| [`imzala/imzala-php`](./packages/php/README.md) | Packagist | `composer require imzala/imzala-php` |
| [`org.imzala:imzala-java`](./packages/java/README.md) | Maven Central | `<dependency>org.imzala:imzala-java</dependency>` |
| [`@imzala/embed`](./packages/embed/README.md) | npm | `npm install @imzala/embed` (tarayıcı iframe) |
| [`@imzala/embed-react`](./packages/embed-react/README.md) | npm | `npm install @imzala/embed-react` (React) |

## Hızlı başlangıç (Node)

```ts
import { Imzala } from '@imzala/node';

const imzala = new Imzala({ apiKey: process.env.IMZALA_API_KEY! });

// Şablondan sözleşme oluştur (imza daveti otomatik gider)
const { templates } = await imzala.templates.list();
const demand = await imzala.demands.create({
  template_id: templates[0].id,
  party_mapping: [{
    template_party_id: templates[0].parties[0].id,
    first_name: 'Ahmet', last_name: 'Yılmaz', email: 'ahmet@example.com',
  }],
});

// Durumu takip et, tamamlanınca imzalı PDF'i indir
const status = await imzala.demands.get(demand.id);
if (status.status === 'COMPLETED') {
  const pdf = await imzala.demands.getPdf(demand.id); // Buffer
}
```

Diğer diller aynı deseni izler (kendi idiomlarıyla): `new Imzala(apiKey)` → tipli kaynak nesneleri (`demands`, `templates`, `embed`, `timestamps`) → `{success, data}` zarfını açan metodlar → tipli hata (`ImzalaError` + alt sınıflar) → GET'ler için otomatik retry. Tam örnekler: [`examples/`](./examples).

## Ortak desen

- **Kaynaklar:** `demands` (create · uploadDocument · **list · get · getPdf · getCertificate · getTimeline · cancel · resendParty · delete** · addItems · sendReminder) · `templates` (list · get · usage · **update · delete**) · `embed.createSession` · `timestamps.create` · `me()`
- **Webhook:** `verifyWebhook(secret, rawBody, signatureHeader)` ile HMAC-SHA256, timing-safe.
- **Retry:** yalnızca idempotent GET (429/5xx, exp-backoff); POST/PATCH/DELETE asla otomatik denenmez.
- **Base URL:** varsayılan prod (`https://api-prd.imzala.org`); test için `https://test-api.imzala.org` geçilir.
- **API anahtarı:** Panel → Geliştirici → API Anahtarları (`imz_<64 hex>`, **sunucu tarafı**, tarayıcıya gömülmez; tarayıcı imzası için `@imzala/embed`).

## İmza sınıfı

İmzala **dijital imza (SES)** üretir; her imza zaman damgalıdır. Nitelikli/güvenli elektronik imza (QES) DEĞİLDİR. Gömülü imza da SES/AES üretir.

## Monorepo yapısı

```
imzala-sdk/
  spec/openapi.v1.yaml    # OpenAPI SSOT (imzala backend ile senkron)
  packages/{node,python,dotnet,php,java,embed,embed-react}/
  examples/{node,python,dotnet,php,java}/   # çalışan uçtan uca örnekler
  .github/workflows/       # per-paket publish CI (npm token, PyPI/NuGet OIDC,
                           # Packagist VCS-webhook, Maven GPG+Sonatype)
```

Her sunucu SDK'sı: vendored openapi-generator çıktısının üzerine elle yazılmış ergonomik facade + `verifyWebhook`. Kaynak koda dokunmadan üretilen ham istemciyi sarmalar.

## Daha fazla

- Tam API referansı: [api-docs.imzala.org](https://api-docs.imzala.org)
- Kullanım kılavuzu: [imzala.org/docs/api-sozlesme-yasam-dongusu](https://imzala.org/docs/api-sozlesme-yasam-dongusu)
- Sürüm yayınlama + registry auth: [RELEASING.md](./RELEASING.md)

> Gömülü imza (`@imzala/embed`) backend'i prod'da canlıdır; ilgili Geliştirici/API kullanım sözleşmesi metinleri hukuk onayındadır (kapsam: [RELEASING.md](./RELEASING.md)).
