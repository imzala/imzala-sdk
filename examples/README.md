# İmzala SDK Örnekleri

Her senaryo beş dilde birebir aynıdır: aynı adımlar, aynı çıktı. Bir dilde okuduğunuzu
diğerinde arayabilirsiniz. Her dosyanın başında üç satır bulunur: ne yapar, hangi
kapsamları (`scope`) ister, ne harcar.

## Ortam değişkenleri

| Değişken | Açıklama |
|----------|----------|
| `IMZALA_API_KEY` | `imz_...` anahtarı (Panel, Geliştirici, API Anahtarları) |
| `IMZALA_BASE_URL` | Varsayılan `https://api-prd.imzala.org`. Test için `https://test-api.imzala.org` |
| `IMZALA_WEBHOOK_SECRET` | Senaryo 01: webhook imza doğrulaması için `whsec_...` gizli anahtarı (isteğe bağlı) |
| `IMZALA_WEBHOOK_SIGNATURE` | Senaryo 01: doğrulanacak `X-Imzala-Signature-256` başlık değeri (isteğe bağlı) |
| `IMZALA_TEMPLATE_ID` | Senaryo 01 ve 05: kullanılacak şablon (verilmezse listedeki ilk şablon); Senaryo 06: taslak oluşturma adımının şablonu |
| `IMZALA_FIELD_TEMPLATE_ID` | Senaryo 02: kullanılacak Alan Şablonu (verilmezse listedeki ilk kayıt) |
| `IMZALA_PDF_PATH` | Senaryo 02 ve 03: kuru koşum ve damga için PDF dosyası (varsayılan `ornek-sozlesme.pdf`) |
| `IMZALA_TIMESTAMP_ID` | Senaryo 03: getirilecek damga kaydı (verilmezse listedeki ilk kayıt) |
| `IMZALA_DEMAND_ID` | Senaryo 06: zarf olarak incelenecek sözleşme (verilmezse listedeki ilk sözleşme); panelden oluşturduğunuz taslağı da bu değişkenle verebilirsiniz |

Tüm kimlikler UUID biçimindedir; panelden ya da liste yanıtlarından kopyalayın.

## Senaryolar

| # | Senaryo | Ne gösterir | Kapsam | Kredi |
|---|---------|-------------|--------|-------|
| 01 | Şablondan sözleşme | Şablon seç, rolleri oku, sözleşme oluştur, davet linklerini al, webhook imzasını doğrula | `templates:read`, `demands:read`, `demands:write` | Evet (yorumda) |
| 02 | Belge ve Alan Şablonu | Alan Şablonu yerleşimini kendi PDF'inizde **kuru koşumla** dene, sonra gönder | `templates:read`, `demands:write` | Kuru koşum hayır, gönderim evet (yorumda) |
| 03 | Zaman damgası | Dosyanın var olma ve değişmezlik kanıtı olarak damga al, damgaları listele, tek kaydı getir | `timestamps` | Evet (yorumda) |
| 04 | Kişiler ve raporlar | Adres defterini sayfalı gez, kişi oluştur (`CONTACT_DUPLICATE` yakala), durum sayımlarını al | `contacts:read`, `contacts:write`, `demands:read` | Hayır |
| 05 | Toplu ve tekrar koruma | 10'arlı parçalarla toplu oluşturma, `Idempotency-Key`, istek limiti (429) davranışı | `templates:read`, `demands:write` | Evet (yorumda) |
| 06 | Zarf | Çok belgeli zarf: taslak, sözleşme + KVKK belgeleri, atama, sıralama, gönderim, belge PDF'i | `demands:write`, `demands:read` | Evet (taslak oluşturma ve gönderim adımlarında, yorumda; belge uçları harcamaz) |

## Diller

| Dil | Klasör | Çalıştırma |
|-----|--------|-----------|
| Node.js | [`node/`](./node) | `IMZALA_API_KEY=imz_... node node/01-sablondan-sozlesme.mjs` |
| Python | [`python/`](./python) | `IMZALA_API_KEY=imz_... python python/01_sablondan_sozlesme.py` |
| PHP | [`php/`](./php) | `IMZALA_API_KEY=imz_... php php/01-sablondan-sozlesme.php` |
| Java | [`java/`](./java) | `cd java && IMZALA_API_KEY=imz_... mvn -q compile exec:java -Dexec.mainClass=org.imzala.examples.SablondanSozlesme` |
| .NET | [`dotnet/`](./dotnet) | `IMZALA_API_KEY=imz_... dotnet run --project dotnet -- 01` |

Dosya adları senaryo numarasıyla başlar: `02-belge-ve-alan-sablonu`, `03-zaman-damgasi`,
`04-kisiler-ve-raporlar`, `05-toplu-ve-tekrar-koruma`, `06-zarf` (Python'da alt çizgi,
Java'da sınıf adı: `BelgeVeAlanSablonu`, `ZamanDamgasi`, `KisilerVeRaporlar`,
`TopluVeTekrarKoruma`, `Zarf`; .NET'te `dotnet run --project dotnet -- 02` ... `06`).

Kurulum notları:

- **Node.js:** depo kökünde `npm install` ve `npm run build --workspace packages/node`; örnekler
  `@imzala/node` paketini çalışma alanından çözer.
- **Python:** `pip install -e packages/python`.
- **PHP:** `packages/php` içinde `composer install`; örnekler oradaki `vendor/autoload.php` dosyasını yükler.
- **Java:** `packages/java` içinde `mvn -q install -DskipTests`; örnek projesi SDK'yı yerel Maven deposundan çözer.
- **.NET:** ek kurulum yok; örnek projesi SDK'yı kaynaktan referanslar.

## İstek limiti hakkında bir not

API anahtarı başına dakikada 60 istek sınırı vardır (anahtar bazında düşürülebilir). Aşımda
**429** döner; yanıt gövdesinde `code: "RATE_LIMIT_EXCEEDED"` ve `retry_after_seconds`, başlıklarda
`Retry-After` ile standart `RateLimit-Limit` / `RateLimit-Remaining` / `RateLimit-Reset` bulunur
(`X-RateLimit-*` **yoktur**).

> Limiti **ardışık** isteklerle görmeye çalışmayın: her istek yaklaşık bir saniye sürdüğü için
> 65 ardışık çağrının hepsi 200 döner, çünkü 60 saniyelik pencere aradan sıfırlanır. Limit ancak
> eşzamanlı bir istek patlamasıyla görünür.

## Sözleşme oluşturma ve iptal

Kredi harcayan veya veri değiştiren çağrılar örneklerde yorum satırındadır. Açmadan önce test
ortamını (`IMZALA_BASE_URL=https://test-api.imzala.org`) kullanmanız önerilir.

## İmza sınıfı notu

İmzala sözleşmeleri varsayılan olarak **dijital imza** üretir. SDK imza geçerliliği hakkında
hukuki iddiada bulunmaz; imza sınıfı sözleşme akışında belirlenir. Ayrıntı: `api-docs.imzala.org`.
