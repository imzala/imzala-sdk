# org.imzala:imzala-java

[![Maven Central](https://img.shields.io/maven-central/v/org.imzala/imzala-java.svg?label=org.imzala%3Aimzala-java)](https://central.sonatype.com/artifact/org.imzala/imzala-java)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](#gereksinimler)

İmzala dijital imza platformunun resmi **Java** SDK'sı. Sözleşme oluşturma, imza takibi, imzalı PDF ve sertifika indirme, denetim izi, şablon yönetimi ve zaman damgası işlemlerini tek bir tip-güvenli istemciyle yapın. Yalnızca JDK'nin kendi `java.net.http.HttpClient`'ına dayanır (ek HTTP client bağımlılığı yok).

```xml
<dependency>
  <groupId>org.imzala</groupId>
  <artifactId>imzala-java</artifactId>
  <version>0.1.0</version>
</dependency>
```

> **Sunucu-taraflı paket.** API anahtarınız hesabınızın tamamına erişir; Android uygulamasına ya da başka bir istemci-taraflı derlemeye gömmeyin. Tarayıcıda imza almak için [`@imzala/embed`](../embed) kullanın. Ayrıntı: [Sunucu-taraflı](#️-sunucu-taraflı) bölümü.

## İçindekiler

- [Gereksinimler](#gereksinimler)
- [Hızlı başlangıç](#hızlı-başlangıç)
- [Yapılandırma](#yapılandırma)
- [API referansı](#api-referansı)
  - [Sözleşmeler (demands)](#sözleşmeler-demands)
  - [Şablonlar (templates)](#şablonlar-templates)
  - [Gömülü imza (embed)](#gömülü-imza-embed)
  - [Zaman damgası (timestamps)](#zaman-damgası-timestamps)
  - [Hesap (me)](#hesap-me)
- [İmzalı PDF ve sertifika (binary)](#imzalı-pdf-ve-sertifika-binary)
- [Otomatik yeniden deneme](#otomatik-yeniden-deneme)
- [Sayfalama](#sayfalama)
- [Webhook doğrulama](#webhook-doğrulama)
- [Hata yönetimi](#hata-yönetimi)
- [Sunucu-taraflı](#️-sunucu-taraflı)
- [İmza sınıfı](#imza-sınıfı)
- [Daha fazla](#daha-fazla)

## Gereksinimler

- Java **17+** (artifact JDK 17 bytecode'una derlenir)
- `imz_` ile başlayan bir API anahtarı: Panel, Geliştirici, API Anahtarları (ya da Hesap Ayarları, API Anahtarları)

Dosya baytlarını `byte[]` olarak veren metodlar dışında ek bir bağımlılık gerekmez; multipart uçları (yükleme, zaman damgası) için Apache HttpMime çalışma-zamanı bağımlılığı otomatik gelir.

## Hızlı başlangıç

```java
import org.imzala.Imzala;
import org.imzala.client.generated.model.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

Imzala imzala = new Imzala(System.getenv("IMZALA_API_KEY"));

// 1) Şablonları listele, birini seç
ApiV1TemplatesGet200ResponseData templateList = imzala.templates().list();
TemplateSummary template = templateList.getTemplates().get(0);

// 2) Şablondan sözleşme oluştur (imza daveti otomatik gider)
CreateDemandRequest body = new CreateDemandRequest()
    .templateId(template.getId())
    .partyMapping(List.of(
        new PartyMappingInput()
            .templatePartyId(template.getParties().get(0).getId())
            .firstName("Ahmet")
            .lastName("Yılmaz")
            .email("ahmet@example.com")
            .phone("+905301112233")));

CreatedDemand demand = imzala.demands().create(body);
System.out.println(demand.getSigningUrls()); // her taraf için imzalama linki

// 3) Durumu takip et
DemandStatus status = imzala.demands().get(demand.getId());
long signed = status.getParties().stream().filter(DemandStatusPartiesInner::getSigned).count();
System.out.println(signed + " taraf imzaladı");

// 4) Tamamlanınca imzalı PDF'i indir
if (status.getStatus() == DemandStatus.StatusEnum.COMPLETED) {
    byte[] pdf = imzala.demands().getPdf(demand.getId()); // byte[]
    Files.write(Path.of("sozlesme.pdf"), pdf);
}
```

## Yapılandırma

`Imzala` dört constructor sunar; parametreler soldan sağa opsiyonel varsayılanları alır:

```java
// Yalnızca API anahtarı (prod, 30sn timeout, GET'ler için 2 retry)
Imzala imzala = new Imzala(System.getenv("IMZALA_API_KEY"));

// Test ortamı
Imzala test = new Imzala(System.getenv("IMZALA_API_KEY"), "https://test-api.imzala.org");

// Tam kontrol
Imzala full = new Imzala(
    System.getenv("IMZALA_API_KEY"),
    "https://api-prd.imzala.org", // baseUrl
    30_000,  // timeoutMs: istek başına okuma zaman aşımı
    2,       // maxRetries: güvenli GET'ler için (0 = kapalı)
    300);    // retryBaseDelayMs: exponential backoff temel gecikmesi
```

| Parametre | Tip | Varsayılan | Açıklama |
|---|---|---|---|
| `apiKey` | `String` | (zorunlu) | `imz_<64 hex>` |
| `baseUrl` | `String` | `https://api-prd.imzala.org` | Test: `https://test-api.imzala.org` |
| `timeoutMs` | `long` | `30000` | İstek başına okuma zaman aşımı |
| `maxRetries` | `int` | `2` | Yalnızca idempotent GET'ler; `0` kapatır |
| `retryBaseDelayMs` | `long` | `300` | Exponential backoff + jitter temel gecikmesi |

İki-, üç- ve tek-parametreli constructor'ların hepsi çalışır; belirtilmeyen parametreler varsayılanlarını (2 retry, 300ms) kullanır.

## API referansı

Tüm metodlar sunucunun `{ success, data }` zarfını açar ve `data`'yı döndürür; hata durumunda tipli bir `ImzalaException` fırlatır (bkz. [Hata yönetimi](#hata-yönetimi)). Kaynaklara `imzala.demands()`, `imzala.templates()`, `imzala.embed()`, `imzala.timestamps()` ile erişilir; kimlik için `imzala.me()`.

### Sözleşmeler (demands)

| Metod | Açıklama | Retry |
|---|---|---|
| `demands().create(CreateDemandRequest body)` | Şablondan sözleşme oluştur, imza daveti gönder | ❌ POST |
| `demands().uploadDocument(UploadDemandParams params)` | Şablonsuz, dosya yükleyerek sözleşme (1 PDF/DOC ya da 1-20 görsel) | ❌ POST |
| `demands().list()` / `demands().list(ListDemandsParams params)` | Sözleşme listesi (counts-only, taraf PII'si yok) | ✅ GET |
| `demands().get(UUID id)` | Sözleşme detayı, taraf imza durumu (maskeli) | ✅ GET |
| `demands().getPdf(UUID id)` | İmzalı sözleşme PDF'i, `byte[]` | GET \* |
| `demands().getCertificate(UUID id)` / `getCertificate(UUID id, String lang)` | Tamamlanma sertifikası (PAdES B-T), `byte[]` | GET \* |
| `demands().getTimeline(UUID id)` | İmza denetim izi (maskeli olaylar) | ✅ GET |
| `demands().cancel(UUID id)` / `cancel(UUID id, ApiV1DemandsIdCancelPostRequest body)` | Bekleyen sözleşmeyi iptal et | ❌ POST |
| `demands().resendParty(UUID id, UUID partyId)` | Tekil tarafa daveti tekrar gönder | ❌ POST |
| `demands().delete(UUID id)` | Tamamlanmamış sözleşmeyi sil | ❌ DELETE |
| `demands().addItems(UUID id, UpsertItemsRequest body)` | Sayfa alanlarını (imza/form) yerleştir | ❌ POST |
| `demands().sendReminder(UUID id)` / `sendReminder(UUID id, TriggerReminderRequest body)` | İmzalamamış taraflara hatırlatma | ❌ POST |

\* `getPdf` ve `getCertificate` GET'tir ama otomatik **yeniden denenmez**: bkz. [İmzalı PDF ve sertifika (binary)](#imzalı-pdf-ve-sertifika-binary).

```java
// Filtreli liste (fluent builder)
ApiV1DemandsGet200ResponseData page = imzala.demands().list(
    new ListDemandsParams().status("PENDING").limit(20).sort("createdAt:desc"));

// İptal (opsiyonel gerekçe)
imzala.demands().cancel(id, new ApiV1DemandsIdCancelPostRequest().reason("Anlaşma değişti"));

// Tekil tarafa daveti tekrar gönder
imzala.demands().resendParty(id, partyId);

// Denetim izi (maskeli)
ApiV1DemandsIdTimelineGet200ResponseData timeline = imzala.demands().getTimeline(id);

// Tamamlanmamış sözleşmeyi sil
imzala.demands().delete(id);
```

`ListDemandsParams` alanları (hepsi opsiyonel): `status`, `q` (başlık araması), `from` / `to` (`LocalDate`), `templateId` (`UUID`, `template_id` olarak gider), `page`, `limit`, `sort` (`alan:yön`, ör. `createdAt:desc`).

Şablonsuz yükleme, `byte[]` girdiyle:

```java
UploadDemandParams params = new UploadDemandParams(
        List.of(new FileInput(pdfBytes, "sozlesme.pdf", "application/pdf")),
        List.of(new UploadPartyInput("Ada", "Lovelace", "ada@example.com", null)))
    .title("Kira Sözleşmesi");
CreatedDemandUpload uploaded = imzala.demands().uploadDocument(params);
```

`FileInput` bayt + dosya adı alır (sunucu işlemeyi uzantıdan çıkarır); SDK içeride geçici bir dosyaya yazıp çağrı bitince (başarı ya da hata) siler. Çağıran taraf dosya sistemiyle uğraşmaz.

### Şablonlar (templates)

| Metod | Açıklama | Retry |
|---|---|---|
| `templates().list()` / `list(Integer page, Integer limit)` | Aktif şablonlar (tek sayfa) | ✅ GET |
| `templates().listAll()` / `listAll(Integer page, Integer limit)` | Tüm şablonları gezen `Iterable` | ✅ GET |
| `templates().get(UUID id)` | Şablon detayı, taraflar, doldurulabilir alanlar | ✅ GET |
| `templates().usage(UUID id)` | API kullanım kılavuzu (örnek curl ve JSON) | ✅ GET |
| `templates().update(UUID id, ApiV1TemplatesIdPatchRequest body)` | Şablon metadata güncelle (ad/açıklama/kategori) | ❌ PATCH |
| `templates().delete(UUID id)` | Şablon sil (soft-delete) | ❌ DELETE |

```java
// Metadata güncelle
imzala.templates().update(templateId,
    new ApiV1TemplatesIdPatchRequest().name("Yeni Ad").category("kira"));

// Sil
imzala.templates().delete(templateId);
```

`update` yalnızca metadata'yı (ad, açıklama, kategori) değiştirir; sayfa/alan/taraf yapısı panelden düzenlenir. `delete` soft-delete'tir: o şablondan üretilmiş mevcut sözleşmeler etkilenmez.

### Gömülü imza (embed)

```java
ApiV1DemandsIdEmbedSessionPost200ResponseData session =
    imzala.embed().createSession(demandId, partyId);
// session.getEmbedUrl() -> bir <iframe>'e gömün (bkz. @imzala/embed)
```

`partyId`, sözleşmenin create/get yanıtındaki `signing_urls[].party_id` (imza URL'sindeki taraf) değeridir. Gömülü imza yalnızca SES/AES üretir (QES değil). Tarayıcı tarafı için [`@imzala/embed`](../embed).

### Zaman damgası (timestamps)

```java
TimestampRecord ts = imzala.timestamps().create(
    new CreateTimestampParams(fileBytes, "belge.pdf")
        .idempotencyKey(UUID.randomUUID().toString())); // tekrarları güvenli yapar (5dk pencere)
```

TÜBİTAK KAMU SM TSA ile RFC 3161 zaman damgası (var-olma ve değişmezlik kanıtı; imza değildir). `idempotencyKey` verilirse 5 dakikalık pencere içindeki tekrarlar aynı sonucu döndürür, kredi harcamaz. `content` + `fileName` zorunlu; `contentType`, `description`, `ownerFirstName`, `ownerLastName` opsiyoneldir.

### Hesap (me)

```java
ApiV1MeGet200ResponseData me = imzala.me();
System.out.println(me.getEmail() + " · kalan kredi: " + me.getCredits().getRemaining());
```

API anahtarının sahibini döndürür (id, e-posta, ad, workspace, kalan kredi). `timestamps` kapsamı (scope) gerektirir.

## İmzalı PDF ve sertifika (binary)

`getPdf` ve `getCertificate` ham baytları bir `byte[]` olarak döndürür (JSON değil). Diske yazın ya da bir akışa aktarın:

```java
byte[] pdf = imzala.demands().getPdf(id);
Files.write(Path.of("sozlesme.pdf"), pdf);

byte[] cert = imzala.demands().getCertificate(id, "tr"); // "en" için İngilizce
Files.write(Path.of("sertifika.pdf"), cert);
```

Yalnızca `status == COMPLETED` sözleşmelerde üretilir. Bu iki binary indirme GET olsa da **otomatik yeniden denenmez**: yanıt gövdesi vendored istemcinin içinde geçici bir dosyaya materyalize edilir, SDK onu okuyup `byte[]`'e çevirir ve geçici dosyayı siler. Diğer okuma uçlarının (`list` / `get` / `getTimeline` / `templates` / `me`) aksine burada retry sarmalayıcı devrede değildir.

## Otomatik yeniden deneme

Yalnızca **GET (okuma)** uçları: `demands().list` / `get` / `getTimeline`, `templates().list` / `get` / `usage` / `listAll`, ve `me()`, 429 (`Retry-After`'a uyarak) ya da 5xx aldığında jitter'lı exponential backoff ile yeniden denenir. Binary indirmeler (`getPdf` / `getCertificate`) bu kapsamda değildir (yukarıya bakın).

**POST/PATCH/DELETE (yazma) uçları ASLA yeniden denenmez** ve bu yapılandırılamaz: bir `demands().create` tekrarı mükerrer sözleşme oluşturur. Retry mantığı yapısal olarak yalnızca GET'e bağlıdır (`Http.unwrapRetryableGet` yalnızca GET metodlarından çağrılır; yazma metodları düz `Http.unwrap` kullanır), bir bayrakla açılıp kapatılamaz. `maxRetries` yalnızca güvenli GET'lerin deneme sayısını ayarlar; `0` retry'ı kapatır.

## Sayfalama

`templates().list()` tek sayfa döner (`getTemplates()`, `getTotal()`, `getPage()`, `getLimit()`). Tüm şablonları elle sayfalamak yerine `listAll()` `Iterable`'ını kullanın; sayfaları şeffaf şekilde gezer:

```java
for (TemplateSummary template : imzala.templates().listAll()) {
    System.out.println(template.getId() + " " + template.getName());
}

// veya belirli bir sayfa/limit'ten başlayarak:
for (TemplateSummary template : imzala.templates().listAll(1, 50)) {
    // ...
}
```

Sunucunun bildirdiği `total`'a ulaşınca, bir sayfa `limit`'ten az öğe döndürünce ya da bir sayfa boş gelince durur (sonsuz döngü yok). `list()` metodunun kendisi değişmez, hâlâ tek sayfa döner.

## Webhook doğrulama

```java
import org.imzala.Imzala;

// Servlet / Spring: request body'yi RAW bayt olarak oku, deserialize ETMEDEN önce
byte[] rawBody = request.getInputStream().readAllBytes();
String signature = request.getHeader("X-Imzala-Signature-256"); // "sha256=<hex>"

boolean valid = Imzala.verifyWebhook(System.getenv("IMZALA_WEBHOOK_SECRET"), rawBody, signature);
if (!valid) {
    response.setStatus(401);
    return;
}

// rawBody'yi şimdi parse et; event.type: demand.created / demand.completed / party.signed ...
response.setStatus(200);
```

`verifyWebhook` (statik; `byte[]` ve `String` overload'ları var) asla exception fırlatmaz; geçersiz ya da eksik imzada `false` döner. Doğrulama sabit-zamanlı karşılaştırma (`MessageDigest.isEqual`) kullanır. Body'yi parse edip yeniden serialize etmeyin: imza byte-byte karşılaştırılır, ham gövde üzerinden doğrulanır.

## Hata yönetimi

```java
import org.imzala.ImzalaException;
import org.imzala.ImzalaAuthException;
import org.imzala.ImzalaRateLimitException;
import org.imzala.ImzalaValidationException;

try {
    imzala.demands().get(id);
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

Tüm hatalar `ImzalaException`'dan türer; ortak alanlar `getStatusCode()`, `getBody()`, `getCode()`. 401/403 → `ImzalaAuthException`, 429 → `ImzalaRateLimitException` (`getRetryAfter()`, saniye), 422 → `ImzalaValidationException`. Diğer durumlar (400, 404, 409, 500, ...) düz `ImzalaException` olarak fırlatılır. `ImzalaException` unchecked'tir (`RuntimeException` alt sınıfı): her çağrıda zorunlu `try/catch` ya da `throws` gerekmez.

> Not: bu paket, Node/Python/.NET/PHP SDK'larının `ImzalaError`-türevi isimlendirmesinden farklı olarak Java konvansiyonuna uyup `ImzalaException` / `Imzala*Exception` isimlerini kullanır; davranış (dört sınıflı taksonomi, aynı alanlar) aynıdır.

## ⚠️ Sunucu-taraflı

Bu paket **yalnızca sunucuda** kullanılır. API anahtarınız sızarsa hesabınızdaki tüm sözleşme, şablon ve zaman damgası işlemlerine erişilir. Anahtarı Android uygulaması, applet ya da başka bir istemci-taraflı derlemeye asla gömmeyin (binary'den çıkarılabilir). Tarayıcıda imza için [`@imzala/embed`](../embed) ya da [`@imzala/embed-react`](../embed-react).

## İmza sınıfı

İmzala **dijital imza (SES)** üretir; her imza zaman damgalıdır. Nitelikli ya da güvenli elektronik imza (QES) DEĞİLDİR. Gömülü imza da SES/AES üretir. SDK, imza geçerliliği hakkında hukuki bir iddiada bulunmaz.

## Daha fazla

- Tam API referansı: [api-docs.imzala.org](https://api-docs.imzala.org)
- Kullanım kılavuzu: [imzala.org/docs/api-sozlesme-yasam-dongusu](https://imzala.org/docs/api-sozlesme-yasam-dongusu)
- Çalışan örnek: [`examples/java`](../../examples/java)
- [Monorepo README](../../README.md)
