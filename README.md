# İmzala SDK

İmzala dijital imza platformu için resmi SDK seti + gömülü imza (embedded signing).

> ⚠️ Geliştirme aşamasında. Yayın öncesi avukat onayı + kapsam onayı tamamlanacaktır.
> Sürüm yayınlama (registry publish CI, gerekli secret'lar, tek seferlik kayıt
> adımları, `embed.createSession` kapsam kararı) için **[RELEASING.md](./RELEASING.md)**.

## Paketler

Sunucu SDK'ları `demands` (oluştur/getir/döküman yükle/kalem ekle/hatırlatma
gönder), `templates` (listele/getir/kullanım), `embed` (gömülü imza
oturumu — bkz. RELEASING.md kapsam notu), `timestamps` (RFC 3161 zaman
damgası) ve `me()` (hesap bilgisi) kaynaklarını + `verifyWebhook` yardımcı
fonksiyonunu sağlar. Tarayıcı paketleri gömülü imza iframe widget'ıdır.

| Paket | Registry | Kurulum | Tek satır kullanım |
|---|---|---|---|
| `@imzala/node` | npm | `npm install @imzala/node` | `import { Imzala } from '@imzala/node'; const imzala = new Imzala({ apiKey: process.env.IMZALA_API_KEY! });` |
| `imzala` | PyPI | `pip install imzala` | `from imzala import Imzala; client = Imzala(api_key="imz_...")` |
| `Imzala` | NuGet | `dotnet add package Imzala` | `var imzala = new Imzala(Environment.GetEnvironmentVariable("IMZALA_API_KEY")!);` |
| `imzala/imzala-php` | Packagist | `composer require imzala/imzala-php` | `$imzala = new \Imzala\ImzalaClient($apiKey);` |
| `org.imzala:imzala-java` | Maven Central | `<dependency><groupId>org.imzala</groupId><artifactId>imzala-java</artifactId><version>X.Y.Z</version></dependency>` | `Imzala imzala = new Imzala(System.getenv("IMZALA_API_KEY"));` |
| `@imzala/embed` | npm | `npm install @imzala/embed` | `new ImzalaEmbed({ container }).open(embedToken)` (tarayıcı, iframe widget) |
| `@imzala/embed-react` | npm | `npm install @imzala/embed-react` | `<ImzalaSign token={embedToken} onComplete={...} />` |

Her sunucu SDK'sı aynı deseni izler: `new Imzala(apiKey)` → tipli
kaynak nesneleri (`.demands`, `.templates`, `.embed`, `.timestamps`) →
`{success,data}` zarfını otomatik açan metodlar → hata durumunda tipli
istisna (`ImzalaError` / `ImzalaAuthError` / `ImzalaRateLimitError` /
`ImzalaValidationError`) → 429/5xx için otomatik retry. API anahtarı
Dashboard → Geliştirici → API Anahtarları'ndan alınır (`imz_<64 hex>`,
sunucu tarafı — asla tarayıcı/mobil koduna gömülmez).

Varsayılan base URL prod (`https://api-prd.imzala.org`); test ortamı için
`https://test-api.imzala.org` geçilir (her SDK'nın `Imzala(...)`
constructor'ı `baseUrl` parametresi kabul eder).

## Monorepo yapısı

```
imzala-sdk/
  spec/openapi.v1.yaml              # OpenAPI SSOT (imzala backend ile senkron)
  packages/
    node/          @imzala/node          — TS facade + generated/ (typescript-axios)
    python/        imzala                — Python facade + generated/ (urllib3/pydantic)
    dotnet/        Imzala                — C# facade + generated/ (csharp/httpclient)
    php/           imzala/imzala-php     — PHP facade + generated/ (php)
    java/          org.imzala:imzala-java — Java facade + generated/ (java/native)
    embed/         @imzala/embed         — tarayıcı iframe widget
    embed-react/   @imzala/embed-react   — React sarmalayıcı (@imzala/embed üzerine)
  .github/workflows/
    npm-publish.yml       # @imzala/node + @imzala/embed + @imzala/embed-react
    python-publish.yml    # imzala (PyPI, Trusted Publishing/OIDC)
    dotnet-publish.yml    # Imzala (NuGet)
    php-publish.yml       # imzala/imzala-php (Packagist — validate/test + opsiyonel re-index ping)
    java-publish.yml      # org.imzala:imzala-java (Maven Central, GPG-signed)
```

Her dilde aynı hand-written facade seti (vendored/generated openapi-generator
çıktısının üzerinde ~8 metod) + `verifyWebhook`/`VerifyWebhook`/`verify_webhook`
(HMAC-SHA256, timing-safe compare) — kaynak koduna dokunmadan üretilen ham
istemciyi sarmalar.

## Katkı / geliştirme

Bu README kullanım içindir; kod değişikliği / yeni sürüm çıkarma için
**[RELEASING.md](./RELEASING.md)**'ye bakın (per-paket sürüm adımları,
registry auth mekanizmaları, gerekli GitHub secret'ları, tek seferlik
kayıt checklist'i).
