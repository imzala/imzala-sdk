# org.imzala:imzala-java

İmzala dijital imza platformu resmi Java SDK'sı.

> ⚠️ Geliştirme aşamasında, yayın öncesi avukat onayı bekliyor. Kapsam ve
> yayın kararları için monorepo kökündeki [RELEASING.md](../../RELEASING.md).

## Kurulum

```xml
<dependency>
  <groupId>org.imzala</groupId>
  <artifactId>imzala-java</artifactId>
  <version>X.Y.Z</version>
</dependency>
```

Java 11+ gerektirir (`java.net.http.HttpClient` tabanlı, ek HTTP client
bağımlılığı yok).

## Hızlı başlangıç

```java
import org.imzala.Imzala;
import org.imzala.client.generated.model.*;

Imzala imzala = new Imzala(System.getenv("IMZALA_API_KEY"));

// Aktif şablonlarını listele
ApiV1TemplatesGet200ResponseData templateList = imzala.templates().list();

// Bir şablondan yeni sözleşme (demand) oluştur
CreateDemandRequest body = new CreateDemandRequest()
    .templateId(templateList.getTemplates().get(0).getId())
    .partyMapping(List.of(
        new PartyMappingInput()
            .templatePartyId(templateList.getTemplates().get(0).getParties().get(0).getId())
            .firstName("Ahmet")
            .lastName("Yılmaz")
            .email("ahmet@example.com")));

CreatedDemand demand = imzala.demands().create(body);
System.out.println(demand.getSigningUrls()); // her taraf için imzalama linki
```

`baseUrl` varsayılan olarak `https://api-prd.imzala.org`'dur; test ortamı
için `new Imzala(apiKey, "https://test-api.imzala.org")`.

## Kaynaklar

- `imzala.templates().list()` / `.list(page, limit)` / `.get(id)` /
  `.usage(id)` / `.listAll()` / `.listAll(page, limit)`
- `imzala.demands().create(body)` / `.get(id)` / `.addItems(id, body)` /
  `.uploadDocument(new UploadDemandParams(files, parties))` /
  `.sendReminder(id)` / `.sendReminder(id, body)`
- `imzala.embed().createSession(demandId, partyId)`: bkz. aşağıdaki
  gömülü imza notu
- `imzala.timestamps().create(new CreateTimestampParams(content, fileName))`
- `imzala.me()`: API anahtarının sahibi (id, e-posta, workspace, kalan kredi)

Dosya yükleyen metodlar `FileInput(byte[] content, String fileName,
String contentType)` kullanır; SDK içeride geçici bir dosyaya yazıp
temizler (vendored generated client `java.io.File` bekliyor), çağıran
taraf dosya sistemiyle uğraşmaz.

## Otomatik yeniden deneme (safe auto-retry)

`templates().list/get/usage`, `demands().get` ve `me()` — yani **sadece
GET** (okuma) uçları — 429 (rate limit, sunucunun bildirdiği bekleme
süresine uyarak) veya 5xx (sunucu hatası) aldığında, jitter'lı exponential
backoff ile otomatik olarak yeniden denenir:

```java
Imzala imzala = new Imzala(
    System.getenv("IMZALA_API_KEY"),
    "https://api-prd.imzala.org",
    30_000, // timeoutMs
    2,      // maxRetries — varsayılan 2, 0 = kapalı
    300);   // retryBaseDelayMs — varsayılan 300ms (backoff: 300ms, 600ms, ...)
```

Bu iki yeni parametre mevcut 3-parametreli constructor'ların (`Imzala(apiKey)`
/ `Imzala(apiKey, baseUrl)` / `Imzala(apiKey, baseUrl, timeoutMs)`) sonuna
eklendi — hepsi hâlâ çalışır, `maxRetries`/`retryBaseDelayMs` belirtilmezse
varsayılanlarını (2 / 300ms) kullanırlar.

**Güvenlik:** `demands().create`, `sendReminder`, `uploadDocument`,
`addItems`, `embed().createSession`, `timestamps().create` gibi **yazma**
(POST) uçları **asla otomatik yeniden denenmez** — bu davranış
yapılandırılamaz. Bir `demands().create` çağrısının otomatik tekrarı,
mükerrer bir sözleşme oluşturur; bu yüzden retry mantığı sadece GET
isteklerine bağlıdır (caller tarafından açılıp kapatılabilecek bir bayrak
değildir) — `Http.unwrapRetryableGet` içeride yalnızca GET'e sarılan
metodlardan çağrılır, POST metodları hâlâ düz `Http.unwrap` kullanır.

## Sayfalama iteratörü

`templates().list()` tek sayfa döner (`{templates, total, page, limit}`).
Tüm şablonları sayfa sayfa elle çekmek yerine `listAll()` iterator'ını
kullanabilirsiniz — sayfaları şeffaf şekilde gezer:

```java
for (TemplateSummary template : imzala.templates().listAll()) {
    System.out.println(template.getId() + " " + template.getName());
}

// veya belirli bir sayfa/limit'ten başlayarak:
for (TemplateSummary template : imzala.templates().listAll(1, 50)) {
    // ...
}
```

`total` alanına ulaşıldığında veya bir sayfa `limit`'ten az öğe
döndürdüğünde durur — sonsuz döngüye girmez. `list()`'in kendisi hâlâ tek
sayfa döner, değişmedi.

## Webhook doğrulama

```java
import org.imzala.Imzala;

// Servlet / Spring: request body'yi RAW bytes olarak oku, deserialize ETMEDEN önce
byte[] rawBody = request.getInputStream().readAllBytes();
String signature = request.getHeader("X-Imzala-Signature-256");

boolean valid = Imzala.verifyWebhook(System.getenv("IMZALA_WEBHOOK_SECRET"), rawBody, signature);
if (!valid) {
    response.setStatus(401);
    return;
}

// ... rawBody'yi parse edip event.type'a göre işle
response.setStatus(200);
```

`verifyWebhook` (statik, `byte[]` veya `String` overload) asla exception
fırlatmaz; geçersiz/eksik imzada `false` döner. Body'yi deserialize edip
yeniden serialize etmeyin, imza doğrulaması byte-byte karşılaştırma yapar.

## Hata yönetimi

```java
import org.imzala.ImzalaException;
import org.imzala.ImzalaAuthException;
import org.imzala.ImzalaRateLimitException;
import org.imzala.ImzalaValidationException;

try {
    imzala.demands().get(demandId);
} catch (ImzalaRateLimitException e) {
    System.out.println("Rate limit: " + e.getRetryAfter() + " sn sonra tekrar dene");
} catch (ImzalaAuthException e) {
    System.out.println("API anahtarı geçersiz veya yetkisiz");
} catch (ImzalaValidationException e) {
    System.out.println("İstek doğrulanamadı: " + e.getBody());
} catch (ImzalaException e) {
    System.out.println("İmzala API hatası: " + e.getStatusCode() + " " + e.getMessage());
}
```

Tüm hatalar `ImzalaException`'dan türer (`getStatusCode()`, `getBody()`,
`getCode()` ortak); 401/403 → `ImzalaAuthException`, 429 →
`ImzalaRateLimitException` (`getRetryAfter()` saniye), 422 →
`ImzalaValidationException`. `ImzalaException` unchecked'tir (`RuntimeException`
alt sınıfı); her çağrıda zorunlu `try/catch` gerekmez.

> Not: bu paket, Node/Python/.NET SDK'larının `ImzalaError`-türevi
> isimlendirmesinden farklı olarak Java konvansiyonuna uyup
> `ImzalaException`/`Imzala*Exception` isimlerini kullanır; davranış
> (4'lü taksonomi, aynı alanlar) aynıdır.

## ⚠️ Sunucu-taraflı

Bu paket **sadece sunucuda** kullanılır. API anahtarınızı istemci-taraflı
(Android, applet) bir uygulamaya asla gömmeyin: sızarsa hesabınızdaki tüm
sözleşme/şablon/zaman damgası işlemlerine erişim kazanılır. Tarayıcıda imza
almak için [`@imzala/embed`](../embed) veya
[`@imzala/embed-react`](../embed-react) kullanın.

## İmza sınıfı notu

İmzala dijital imza (SES) üretir; her imza zaman damgalıdır. Nitelikli/
güvenli elektronik imza (QES) DEĞİLDİR.

`imzala.embed().createSession(...)` şu an **yalnızca test ortamında**
çalışır; gömülü imza özelliği henüz avukat onaylı prod-canlı değildir.
Kapsam kararı [RELEASING.md](../../RELEASING.md)'de.

## Daha fazla

- [Monorepo README](../../README.md): tüm paketler + genel desen
- [RELEASING.md](../../RELEASING.md): sürüm yayınlama, kapsam kararları
