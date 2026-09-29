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
  - [Çok belgeli zarf (documents, dispatch)](#çok-belgeli-zarf-documents-dispatch)
  - [Şablonlar (templates)](#şablonlar-templates)
  - [Alan Şablonları (fieldTemplates)](#alan-şablonları-fieldtemplates)
  - [Kişiler (contacts)](#kişiler-contacts)
  - [Raporlar (reports)](#raporlar-reports)
  - [Gömülü imza (embed)](#gömülü-imza-embed)
  - [Zaman damgası (timestamps)](#zaman-damgası-timestamps)
  - [Hesap (me)](#hesap-me)
- [İmzalı PDF ve sertifika (binary)](#imzalı-pdf-ve-sertifika-binary)
- [Otomatik yeniden deneme](#otomatik-yeniden-deneme)
- [Sayfalama](#sayfalama)
- [Webhook doğrulama](#webhook-doğrulama)
- [Hata yönetimi](#hata-yönetimi)
- [Sık karşılaşılan hatalar](#sık-karşılaşılan-hatalar)
- [Sürüm uyumu](#sürüm-uyumu)
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
            .firstName("Ayşe")
            .lastName("Yılmaz")
            .email("ayse@example.com")
            .phone("+905551112233")));

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

API anahtarı ve `Idempotency-Key` değerleri yazdırılabilir ASCII olmalıdır; dosyadan okunan anahtarın sonundaki satır sonu gibi karakterler istek gönderilmeden `ImzalaValidationException` fırlatır (`getStatusCode()` `null`).

`X-Workspace-Id` başlığı için bir seçenek yoktur: organizasyon içinde üretilmiş anahtar kendi organizasyonuna bağlıdır, ayrıca başlık gerekmez. Kişisel anahtarla bir organizasyon adına çalışmak bu sürümde desteklenmez.

## API referansı

Tüm metodlar sunucunun `{ success, data }` zarfını açar ve `data`'yı döndürür; hata durumunda tipli bir `ImzalaException` fırlatır (bkz. [Hata yönetimi](#hata-yönetimi)). Kaynaklara `imzala.demands()`, `imzala.templates()`, `imzala.fieldTemplates()`, `imzala.contacts()`, `imzala.reports()`, `imzala.embed()`, `imzala.timestamps()` ile erişilir; kimlik için `imzala.me()`.

### Sözleşmeler (demands)

| Metod | Açıklama | Retry |
|---|---|---|
| `demands().create(CreateDemandRequest body)` / `create(body, String idempotencyKey)` | Şablondan sözleşme oluştur, imza daveti gönder (`dispatchNotifications(false)` ile sessiz taslak) | Anahtar varsa 429'da 1 kez |
| `demands().createBulk(ApiV1DemandsBulkPostRequest body)` | Tek istekte en çok 10 sözleşme; her satır bağımsız, `getFailed()` sayısını ve her satırın durumunu kontrol edin | ❌ POST |
| `demands().uploadDocument(UploadDemandParams params)` | Şablonsuz, dosya yükleyerek sözleşme (1 PDF/DOC ya da 1-20 görsel) | Anahtar varsa 429'da 1 kez |
| `demands().list()` / `demands().list(ListDemandsParams params)` | Sözleşme listesi (counts-only, taraf PII'si yok); `ListDemandsParams.archived("exclude" | "only" | "include")` arşiv filtresi (verilmezse `include`) | ✅ GET |
| `demands().get(UUID id)` | Sözleşme detayı, taraf imza durumu (maskeli) | ✅ GET |
| `demands().getPdf(UUID id)` | İmzalı sözleşme PDF'i, `byte[]` | GET \* |
| `demands().getDocumentPdf(UUID id, UUID documentId)` | Çok belgeli zarfta tek belgenin imzalı PDF'i, `byte[]` | GET \* |
| `demands().getCertificate(UUID id)` / `getCertificate(UUID id, String lang)` | Tamamlanma sertifikası (PAdES B-T), `byte[]` | GET \* |
| `demands().getTimeline(UUID id)` | İmza denetim izi (maskeli olaylar) | ✅ GET |
| `demands().cancel(UUID id)` / `cancel(UUID id, ApiV1DemandsIdCancelPostRequest body)` | Bekleyen sözleşmeyi iptal et | ❌ POST |
| `demands().resendParty(UUID id, UUID partyId)` | Tekil tarafa daveti tekrar gönder | ❌ POST |
| `demands().delete(UUID id)` | Tamamlanmamış sözleşmeyi sil | ❌ DELETE |
| `demands().addItems(UUID id, UpsertItemsRequest body)` | Sayfa alanlarını (imza/form) yerleştir (`PAGE_ID_REQUIRED`, `INVALID_ITEM_TYPE`) | ❌ POST |
| `demands().updateStamp(UUID id, int itemId, PatchStampItemRequest body)` | Tek bir kaşe alanını `StampData` ile doldur (kısmi güncelleme: `null`/`""` alanı kaldırır, hiç set edilmeyen korunur; `DEMAND_PARTIALLY_SIGNED`) | ❌ PATCH |
| `demands().updateTerm(UUID id, ContractTermInput body)` | Süre ve yenileme takibini güncelle (kısmi: set edilen alan yazılır, açıkça `null` verilen temizlenir, hiç set edilmeyen korunur; `TERM_INVALID`, `DEMAND_ARCHIVED`) | ❌ PATCH |
| `demands().archive(UUID id)` / `demands().unarchive(UUID id)` | Tamamlanmış, iptal edilmiş veya süresi dolmuş sözleşmeyi arşivle / arşivden çıkar (durum değişmez; arşivdeki sözleşme değiştirilemez ve silinemez) | ❌ POST |
| `demands().sendReminder(UUID id)` / `sendReminder(UUID id, TriggerReminderRequest body)` | İmzalamamış taraflara hatırlatma (5 dk pencerede `RATE_LIMITED`, `force(true)` aşar) | ❌ POST |
| `demands().dispatch(UUID id)` / `dispatch(id, boolean)` / `dispatch(id, String sendInvitations)` | Sessiz hazırlanmış sözleşmeyi yayına al, davetleri gönder | ❌ POST |

\* `getPdf`, `getDocumentPdf` ve `getCertificate` GET'tir ama otomatik **yeniden denenmez**: bkz. [İmzalı PDF ve sertifika (binary)](#imzalı-pdf-ve-sertifika-binary).

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
        List.of(new UploadPartyInput("Ayşe", "Yılmaz", "ayse@example.com", null)))
    .title("Kira Sözleşmesi");
CreatedDemandUpload uploaded = imzala.demands().uploadDocument(params);

// Tekrar-güvenli oluşturma: aynı anahtarla ikinci istek ikinci sözleşme üretmez
CreatedDemand created = imzala.demands().create(body, "siparis-" + orderId);

// Alan Şablonu ile yükleme: her tarafta templatePartyId zorunludur
UploadDemandParams layout = new UploadDemandParams(
        List.of(new FileInput(pdfBytes, "sozlesme.pdf", "application/pdf")),
        List.of(new UploadPartyInput("Ayşe", "Yılmaz", "ayse@example.com", null, partyTemplateId)))
    .fieldTemplateId(fieldTemplateId)
    .onAnchorMiss("block") // çapa bulunamazsa sözleşme oluşturulmaz, kredi düşmez
    .idempotencyKey("yukleme-" + orderId);
```

`FileInput` bayt + dosya adı alır (sunucu işlemeyi uzantıdan çıkarır); SDK içeride geçici bir dosyaya yazıp çağrı bitince (başarı ya da hata) siler. Çağıran taraf dosya sistemiyle uğraşmaz. Multipart metin alanları (başlık, açıklama, taraf adları) UTF-8 gönderilir. `UploadDemandParams` diğer seçenekleri: `sendInvitations(String)` (bu uçta varsayılan kapalı; `"sms"` SMS ve WhatsApp'ı kapsar, yalnız daraltır) ve `force(boolean)`.

**Şablonun hangi belgelerinin gönderileceğini seçme.** İstekteki `documents` alanı şablonun varsayılanını bu isteğe özel değiştirir: `include` varsayılanı kapalı bir belgeyi ekler, `exclude` varsayılanı açık bir belgeyi çıkarır (her liste en çok 20 kimlik). Kimlikler `templates().get(id).getDocuments()` içinden gelir. Alanı hiç göndermezseniz şablonun varsayılanı gider; davranış eskisiyle birebir aynıdır. Çıkarılan belge bu imza sürecine hiç girmez: imzacıya gösterilmez, imzalı PDF'te ve tamamlanma sertifikasında yer almaz, kredi hesabına katılmaz. Mevzuat gereği karşı tarafa verilmesi gereken bir belgeyi (ör. `docKind: PREINFO` ya da `KVKK_NOTICE`) başka bir kanaldan vermiyorsanız çıkarmayın; bu yükümlülük sözleşmeyi gönderene aittir.

```java
TemplateDetail template = imzala.templates().get(templateId);
UUID kvkkId = template.getDocuments().stream()
    .filter(d -> d.getDocKind() == TemplateDocumentSummary.DocKindEnum.KVKK_NOTICE)
    .findFirst()
    .orElseThrow()
    .getId();

CreatedDemand demand = imzala.demands().create(new CreateDemandRequest()
    .templateId(templateId)
    // varsayılanı kapalı belgeyi bu isteğe ekle
    .documents(new DocumentSelectionInput().include(List.of(kvkkId))));

// Toplu uçta seçim satır başınadır (batch geneli `options.documents` reddedilir)
imzala.demands().createBulk(new ApiV1DemandsBulkPostRequest()
    .templateId(templateId)
    .rows(List.of(new ApiV1DemandsBulkPostRequestRowsInner()
        .documents(new DocumentSelectionInput().exclude(List.of(ekId))))));
```

Geçersiz seçim `INVALID_DOCUMENT_SELECTION` (400) fırlatır; `details.reason` nedeni verir: `shape`, `unknown_document`, `conflict`, `empty`. Eşlediğiniz bir role hiç belge kalmazsa `PARTY_WITHOUT_DOCUMENTS` (409) döner ve sözleşme oluşmaz.

### Çok belgeli zarf (documents, dispatch)

Bir sözleşme `dispatchNotifications(false)` ile sessizce oluşturulur, belgeler eklenir, sonra tek çağrıyla yayına alınır. Belge uçları kimseye bildirim göndermez ve kredi düşmez; kredi yalnız `dispatch` anında düşer.

| Metod | Açıklama | Retry |
|---|---|---|
| `demands().documents().list(UUID demandId)` / `list(demandId, "wizard")` | Zarftaki belgeler (`"wizard"` atama ve karar sayılarını da verir) | ✅ GET |
| `demands().documents().create(UUID demandId, ApiV1DemandsDemandIdDocumentsPostRequest body)` | Mevcut bir belgeyi zarfa ekle | ❌ POST |
| `demands().documents().upload(UUID demandId, UploadEnvelopeDocumentParams params)` | Dosya yükleyerek belge ekle; `new UploadEnvelopeDocumentParams(file, title, idempotencyKey)`, `idempotencyKey` zorunlu; `.docKind()` / `.isRequired()` opsiyonel | 429'da 1 kez |
| `demands().documents().update(UUID demandId, UUID docId, ApiV1DemandsDemandIdDocumentsDocIdPatchRequest body)` | Başlık / zorunluluk bayrakları | ❌ PATCH |
| `demands().documents().delete(UUID demandId, UUID docId)` | Belgeyi kaldır (son belge silinemez) | ❌ DELETE |
| `demands().documents().reorder(UUID demandId, List<UUID> documentIds)` | Belge sırası (kimlik kümesi birebir eşleşmeli) | ❌ PUT |
| `demands().documents().setAssignments(UUID demandId, UUID docId, List<UUID> partyIds)` | Belgeyi imzalayacak taraflar | ❌ PUT |
| `demands().dispatch(UUID demandId)` / `dispatch(demandId, boolean)` / `dispatch(demandId, String)` | Zarfı yayına al (`"email"` / `"sms"` kanal daraltır) | ❌ POST |

`upload` aynı `idempotencyKey` ile tekrar çağrılırsa yeni belge oluşmaz; sunucunun 409 `IDEMPOTENT_REPLAY` yanıtı normal sonuç olarak (mevcut belge) döner. Özellik hesabınızda açık değilse her belge metodu `ENVELOPE_MULTI_DOC_DISABLED` fırlatır; `list` boş liste döndürmez.

### Şablonlar (templates)

| Metod | Açıklama | Retry |
|---|---|---|
| `templates().list()` / `list(Integer page, Integer limit)` | Aktif şablonlar (tek sayfa) | ✅ GET |
| `templates().listAll()` / `listAll(Integer page, Integer limit)` | Tüm şablonları gezen `Iterable` | ✅ GET |
| `templates().get(UUID id)` | Şablon detayı, taraflar, doldurulabilir alanlar, zarf belgeleri (`getDocuments()`) | ✅ GET |
| `templates().usage(UUID id)` | API kullanım kılavuzu (örnek curl ve JSON) | ✅ GET |
| `templates().update(UUID id, ApiV1TemplatesIdPatchRequest body)` | Şablon metadata güncelle (ad/açıklama/kategori) | ❌ PATCH |
| `templates().delete(UUID id)` | Şablonu sil; kayıt 30 gün saklanır, mevcut sözleşmeler etkilenmez. Aktif sözleşmesi olan şablon silinemez (`409 TEMPLATE_IN_USE`) | ❌ DELETE |

```java
// Metadata güncelle
imzala.templates().update(templateId,
    new ApiV1TemplatesIdPatchRequest().name("Yeni Ad").category("kira"));

// Sil
imzala.templates().delete(templateId);
```

`update` yalnızca metadata'yı (ad, açıklama, kategori) değiştirir; sayfa/alan/taraf yapısı panelden düzenlenir. `delete` kaydı hemen silmez: 30 gün saklanır ve o şablondan üretilmiş mevcut sözleşmeler etkilenmez; aktif (taslak veya imza bekleyen) sözleşmesi olan şablon `TEMPLATE_IN_USE` ile reddedilir.

`templates().list()` Alan Şablonlarını listelemez ve `templates().get()` bir Alan Şablonu kimliğine `404` döner; onlar için aşağıdaki `fieldTemplates()` kaynağını kullanın.

### Alan Şablonları (fieldTemplates)

| Metod | Açıklama | Retry |
|---|---|---|
| `fieldTemplates().list()` / `list(Integer page, Integer limit)` | Alan Şablonları (tek sayfa) | ✅ GET |
| `fieldTemplates().get(UUID id)` | Alan Şablonu detayı | ✅ GET |
| `fieldTemplates().previewLayout(UUID id, List<FileInput> files)` / `previewLayout(id, files, String onAnchorMiss)` | Yerleşimi tam bir PDF üzerinde kuru koşumla dener; sözleşme oluşturmaz, kredi harcamaz | ❌ POST |

```java
FieldLayoutPreview preview = imzala.fieldTemplates().previewLayout(
    fieldTemplateId,
    List.of(new FileInput(pdfBytes, "sozlesme.pdf", "application/pdf")),
    "drop"); // verilmezse sunucu "block" uygular
```

### Kişiler (contacts)

| Metod | Açıklama | Retry |
|---|---|---|
| `contacts().list()` / `list(ListContactsParams params)` | Kişiler (tek sayfa); `limit` 10 ile 100 arası, `sort` biçimi `-createdAt` (`-` öneki azalan) | ✅ GET |
| `contacts().listAll()` / `listAll(ListContactsParams params)` | Tüm kişileri gezen `Iterable<ContactSummary>` | ✅ GET |
| `contacts().create(ApiV1ContactsPostRequest body)` | Kişi oluştur (`CONTACT_DUPLICATE`: aynı e-posta veya telefon zaten var) | ❌ POST |

`ListContactsParams` alanları (fluent, hepsi opsiyonel): `q`, `page`, `limit`, `sort`, `companyId` (`UUID`), `archived`.

### Raporlar (reports)

```java
ApiV1ReportsGet200ResponseData report = imzala.reports().get(); // sözleşme durumlarının toplu sayımı; parametre almaz
```

### Gömülü imza (embed)

```java
ApiV1DemandsIdEmbedSessionPost200ResponseData session =
    imzala.embed().createSession(demandId, partyId);
// session.getEmbedUrl() -> bir <iframe>'e gömün (bkz. @imzala/embed)
```

`partyId`, sözleşmenin create/get yanıtındaki `signing_urls[].party_id` (imza URL'sindeki taraf) değeridir. Gömülü imza, 5070 sayılı Kanun m.3 anlamında elektronik imzadır; güvenli elektronik imza değildir ve AB hukuku bakımından gelişmiş veya nitelikli imza olarak sunulmaz. Tarayıcı tarafı için [`@imzala/embed`](../embed).

### Zaman damgası (timestamps)

```java
TimestampRecord ts = imzala.timestamps().create(
    new CreateTimestampParams(fileBytes, "belge.pdf")
        .idempotencyKey(UUID.randomUUID().toString())); // tekrarları güvenli yapar (5dk pencere)

ApiV1TimestampsGet200ResponseData listing = imzala.timestamps().list(
    new ListTimestampsParams().status("COMPLETED").sort("-createdAt")); // limit 10..100
TimestampListItem one = imzala.timestamps().get(ts.getId());
```

TÜBİTAK KAMU SM TSA ile RFC 3161 zaman damgası (var-olma ve değişmezlik kanıtı; imza değildir). `idempotencyKey` verilirse 5 dakikalık pencere içindeki tekrarlar aynı sonucu döndürür, kredi harcamaz. `content` + `fileName` zorunlu; `contentType`, `description`, `ownerFirstName`, `ownerLastName` opsiyoneldir. Bu uç anahtar başına dakikada 10 istekle sınırlıdır (`RATE_LIMIT_EXCEEDED`).

### Hesap (me)

```java
ApiV1MeGet200ResponseData me = imzala.me();
System.out.println(me.getEmail() + " · kalan kredi: " + me.getCredits().getRemaining());
```

API anahtarının sahibini döndürür (id, e-posta, ad, workspace, kalan kredi). Hiçbir kapsam (scope) istemez; geçerli her anahtarla çalışır.

## İmzalı PDF ve sertifika (binary)

`getPdf`, `getDocumentPdf` ve `getCertificate` ham baytları bir `byte[]` olarak döndürür (JSON değil). Diske yazın ya da bir akışa aktarın:

```java
byte[] pdf = imzala.demands().getPdf(id);
Files.write(Path.of("sozlesme.pdf"), pdf);

byte[] cert = imzala.demands().getCertificate(id, "tr"); // "en" için İngilizce
Files.write(Path.of("sertifika.pdf"), cert);
```

Yalnızca `status == COMPLETED` sözleşmelerde üretilir. Bu binary indirmeler GET olsa da **otomatik yeniden denenmez**: yanıt gövdesi vendored istemcinin içinde geçici bir dosyaya materyalize edilir, SDK onu okuyup `byte[]`'e çevirir ve geçici dosyayı siler. Diğer okuma uçlarının (`list` / `get` / `getTimeline` / `templates` / `me`) aksine burada retry sarmalayıcı devrede değildir. Hata durumunda diğer metodlar gibi `ImzalaException` fırlatırlar.

## Otomatik yeniden deneme

Üç kural vardır; hepsi SDK'nın içindedir ve JDK `HttpClient`'a ayrıca bir tekrar politikası bağlanmaz:

1. **Okumalar (GET):** `demands().list/get/getTimeline`, `templates().list/get/usage/listAll`, `fieldTemplates().list/get`, `contacts().list/listAll`, `timestamps().list/get`, `reports().get()`, `demands().documents().list()` ve `me()` 429 veya 5xx aldığında en çok `maxRetries` kez (varsayılan 2) jitter'lı exponential backoff ile yeniden denenir. `0` kapatır. Binary indirmeler (`getPdf` / `getDocumentPdf` / `getCertificate`) bu kapsamda değildir (yukarıya bakın).
2. **`Idempotency-Key` ile gönderilen yazmalar:** `demands().create(body, idempotencyKey)`, `uploadDocument()` (`UploadDemandParams.idempotencyKey()`), `timestamps().create()` (`CreateTimestampParams.idempotencyKey()`) ve `demands().documents().upload()` bir 429 sonrasında **tam bir kez** yeniden denenir; sunucu aynı anahtar için ikinci kayıt oluşturmaz. İkinci 429, 5xx ve diğer tüm hatalar doğrudan fırlatılır. Anahtar verilmezse tek denemedir. `maxRetries` bu kuralı etkilemez.
3. **Diğer yazmalar hiç yeniden denenmez:** `createBulk`, `contacts().create`, `dispatch`, `sendReminder`, `cancel`, `resendParty`, `delete`, `addItems`, `updateStamp`, `updateTerm`, `archive`, `unarchive`, `templates().update/delete`, `embed().createSession` ve zarf belgesi `create/update/delete/reorder/setAssignments`. Tekrarlanan bir `createBulk` ikinci bir toplu iş, tekrarlanan bir `sendReminder` ikinci bir SMS/e-posta üretir.

Bekleme süresi `Retry-After` başlığından okunur (saniye ya da HTTP tarihi); başlık yoksa backoff gecikmesi uygulanır. **Bekleme tavanı 60 saniyedir:** sunucu daha uzun bir süre isterse SDK beklemek yerine 429'u fırlatır (`ImzalaRateLimitException.getRetryAfter()` süreyi taşır). `NaN`, sonsuz ve negatif `Retry-After` değerleri yok sayılır.

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

// rawBody'yi şimdi parse et; event.type: demand.created / demand.completed / demand.expired /
//   party.signed / party.viewed / party.rejected / kyc.completed / kyc.failed /
//   contract.expiring / contract.ended / contract.advanced
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

Tüm hatalar `ImzalaException`'dan türer; ortak alanlar `getStatusCode()`, `getBody()`, `getCode()`, `getCodeDescription()`. 401/403 → `ImzalaAuthException`, 429 → `ImzalaRateLimitException` (`getRetryAfter()` saniye, `getRateLimit()`), 422 → `ImzalaValidationException`. Diğer durumlar (400, 404, 409, 500, ...) düz `ImzalaException` olarak fırlatılır. `ImzalaException` unchecked'tir (`RuntimeException` alt sınıfı): her çağrıda zorunlu `try/catch` ya da `throws` gerekmez. İstek gönderilmeden yakalanan yerel doğrulama hataları (geçersiz API anahtarı veya `Idempotency-Key` karakteri, üretilmiş istemcinin parametre denetimleri) de `ImzalaValidationException`'dır; `getStatusCode()` `null` döner. Ağ hatasında (HTTP alışverişi olmadan) `getStatusCode()` `null`'dır.

- **`getCode()`:** sunucunun makine-okunur kodu. `{"error": "metin", "code": "KOD"}` biçimindeki gövdelerde `code` alanından okunur; yalnız metin taşıyan gövdede `null` kalır.
- **`getCodeDescription()`:** kod için katalogdaki tek satırlık Türkçe açıklama. Katalog `org.imzala.ErrorCodes.CODES` (73 kod, salt-okunur `Map`); `ErrorCodes.describe(code)` ve `ErrorCodes.isKnown(code)` ile sorgulanır. Katalogda olmayan bir kod açıklamasız fırlatılır, yani yeni bir sunucu sürümü eski SDK'yı kırmaz.
- **`ImzalaRateLimitException.getRateLimit()`:** standart `RateLimit-Limit` / `RateLimit-Remaining` / `RateLimit-Reset` / `RateLimit-Policy` başlıkları (`RateLimitInfo`). Birden çok sınır 429 döndürür ve farklı kod taşır; bkz. aşağıdaki tablo.

```java
import org.imzala.ErrorCodes;

ErrorCodes.describe("TEMPLATE_IN_USE"); // "Şablonun aktif (taslak veya imza bekleyen) sözleşmesi olduğu için silinemez."
ErrorCodes.CODES.size();                // 73
```

## Sık karşılaşılan hatalar

| Durum | Kod | Ne yapmalı |
|---|---|---|
| 400 | `INVALID_PAGE` | `page` 1 veya daha büyük bir tam sayı olmalı |
| 400 | `INVALID_EXPIRY_DATE` | `expiry_date` geçerli bir takvim tarihi olmalı |
| 400 | `PAGE_ID_REQUIRED` | Alan yerleştirmede her öğede tam sayı `page_id` zorunlu |
| 400 | `INVALID_ITEM_TYPE` | Desteklenen alan tiplerinden birini kullanın (hata mesajı listeler) |
| 404 | (kodsuz) | Alan Şablonu kimliğiyle `templates().get()` çağrıldı; Alan Şablonları için `fieldTemplates().get()` kullanın (iki kavram ayrıdır) |
| 409 | `TEMPLATE_IN_USE` | Şablonun taslak veya imza bekleyen sözleşmesi var; tamamlanınca silinebilir |
| 409 | `DEMAND_NOT_DISPATCHABLE` | Sözleşme tamamlanmış veya iptal edilmiş; tekrar gönderilemez |
| 409 | `ENVELOPE_MULTI_DOC_DISABLED` | Çok belgeli zarf bu hesapta açık değil; `demands().documents().*` bu kodu fırlatır |
| 409 | `IDEMPOTENCY_KEY_REUSED` | Aynı anahtar farklı gövdeyle kullanıldı; yeni sözleşme için yeni anahtar üretin |
| 409 | `IDEMPOTENCY_UNVERIFIABLE` | Anahtar daha önce sözleşme üretti ama isteğin aynı olduğu doğrulanamadı; gövdedeki `demand_id` ile durumu sorgulayın, yeni anahtarla körlemesine tekrarlamayın |
| 409 | `DUPLICATE_SUSPECTED` | Anahtarsız istek son 10 dakikada gönderilmiş aynı içerikle eşleşti; kasten tekrarlamak için `force` gönderin |
| 422 | `FIELD_LAYOUT_UNRESOLVED` | Alan yerleşimi belgeye uygulanamadı; sözleşme oluşturulmadı, kredi düşülmedi. `onAnchorMiss` seçin veya belgeyi düzeltin |
| 429 | `RATE_LIMIT_EXCEEDED` | Genel sınır: anahtar başına dakikada 60 istek (zaman damgası ucunda 10). `Retry-After` kadar bekleyin |
| 429 | `TOO_MANY_REQUESTS` | Uca özgü sınır: belge yüklemede dakikada 30, davet tekrarı ve zarf gönderiminde saatte 300 |
| 429 | `RECIPIENT_RESEND_LIMIT` | Aynı alıcıya saatte en çok 3, günde en çok 10 davet tekrarı |

Sözleşme, şablon ve Alan Şablonu listelerinde `limit` üst sınırı 100'dür; daha büyük bir değer hata vermez, sessizce 100'e kırpılır. Kişi ve zaman damgası listelerinde `limit` 10 ile 100 arasında olmalıdır.

## Sürüm uyumu

| org.imzala:imzala-java | Konuştuğu API | Durum |
|---|---|---|
| 1.0.0 | v1 (`1.9.1`) | Güncel: süre ve yenileme takibi, arşivleme, `contract.*` webhook olayları, imza yöntemi listesi ve sırası, kaşe verisi, belge başına değişkenler |
| 0.x | v1 (`1.7.x`) | Bakım dışı; 1.0.0'a yükseltin |

İmzala dış API'si **v1**'dir ve geriye dönük uyumludur: yeni alanlar opsiyonel, yeni davranışlar
opt-in, varsayılan davranış eskisidir. Kırıcı bir değişiklik gerekirse yeni bir major API sürümü
yayımlanır; eski sürüm duyurudan sonra **12 ay** çalışmaya devam eder.

> Not: bu paket, Node/Python/.NET/PHP SDK'larının `ImzalaError`-türevi isimlendirmesinden farklı olarak Java konvansiyonuna uyup `ImzalaException` / `Imzala*Exception` isimlerini kullanır; davranış (dört sınıflı taksonomi, aynı alanlar) aynıdır.

## ⚠️ Sunucu-taraflı

Bu paket **yalnızca sunucuda** kullanılır. API anahtarınız sızarsa hesabınızdaki tüm sözleşme, şablon ve zaman damgası işlemlerine erişilir. Anahtarı Android uygulaması, applet ya da başka bir istemci-taraflı derlemeye asla gömmeyin (binary'den çıkarılabilir). Tarayıcıda imza için [`@imzala/embed`](../embed) ya da [`@imzala/embed-react`](../embed-react).

## İmza sınıfı

Bu API ile tarayıcıdan atılan dijital imza, 5070 sayılı Elektronik İmza Kanunu m.3 anlamında elektronik imzadır ve her imza zaman damgalıdır. Güvenli elektronik imza değildir; AB hukuku bakımından gelişmiş veya nitelikli imza olarak sunulmaz. Gömülü imza da aynı niteliktedir. Telefon doğrulaması veya T.C. kimlik numarası kontrolü gibi ek adımlar delil değerini güçlendirir; imzayı gelişmiş ya da güvenli elektronik imzaya dönüştürmez. SDK, imza geçerliliği hakkında hukuki bir iddiada bulunmaz.

## Daha fazla

- Tam API referansı: [api-docs.imzala.org](https://api-docs.imzala.org)
- Kullanım kılavuzu: [imzala.org/docs/api-sozlesme-yasam-dongusu](https://imzala.org/docs/api-sozlesme-yasam-dongusu)
- Çalışan örnekler: [`examples/java`](../../examples/java) (altı senaryo)
- Değişiklik günlüğü: [CHANGELOG.md](./CHANGELOG.md)
- [Monorepo README](../../README.md)
