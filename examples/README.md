# İmzala SDK Örnekleri

Bu klasör, İmzala API'sini SDK'larla uçtan uca kullanan çalışır örnekler içerir.
Senaryo her dilde aynıdır: kimliğini doğrula, şablonları listele, sözleşmeleri
listele, bir sözleşmenin durumunu + denetim izini oku, imzalı PDF'i indir,
webhook imzasını doğrula.

## Ortam değişkenleri

| Değişken | Açıklama |
|----------|----------|
| `IMZALA_API_KEY` | `imz_...` anahtarı (Panel → Geliştirici → API Anahtarları) |
| `IMZALA_BASE_URL` | Varsayılan `https://api-prd.imzala.org`. Test için `https://test-api.imzala.org` |

## Diller

| Dil | Klasör | Çalıştırma |
|-----|--------|-----------|
| Node.js | [`node/`](./node) | `IMZALA_API_KEY=imz_... node node/lifecycle.mjs` |
| Python | [`python/`](./python) | `IMZALA_API_KEY=imz_... python python/lifecycle.py` |
| .NET | [`dotnet/`](./dotnet) | `dotnet run --project dotnet` |
| PHP | [`php/`](./php) | `IMZALA_API_KEY=imz_... php php/lifecycle.php` |
| Java | [`java/`](./java) | Maven exec (bkz. klasör README) |

> Yeni sözleşme oluşturma (`demands.create`) ve iptal (`demands.cancel`) örnekleri
> yorum satırı olarak bırakıldı: gerçek kredi harcar / veri değiştirir. Salt-okuma
> işlemleri (liste, durum, denetim izi, PDF indir) doğrudan çalışır.

## İmza sınıfı notu

İmzala sözleşmeleri varsayılan olarak **dijital imza** (SES) üretir. SDK imza
geçerliliği hakkında hukuki iddiada bulunmaz; imza sınıfı sözleşme akışında
belirlenir. Ayrıntı: `api-docs.imzala.org` ve `/docs/api-sozlesme-yasam-dongusu`.
