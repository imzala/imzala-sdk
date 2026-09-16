# Imzala (.NET)

[![NuGet](https://img.shields.io/nuget/v/Imzala.svg)](https://www.nuget.org/packages/Imzala)
[![NuGet indirme](https://img.shields.io/nuget/dt/Imzala.svg)](https://www.nuget.org/packages/Imzala)
[![.NET](https://img.shields.io/badge/.NET-8.0%2B-512BD4.svg)](https://dotnet.microsoft.com)

İmzala dijital imza platformunun resmi **.NET / C#** SDK'sı. Sözleşme oluşturma, imza takibi, imzalı PDF ve sertifika indirme, denetim izi, şablon yönetimi ve zaman damgası işlemlerini tek bir tip-güvenli istemciyle yapın.

```bash
dotnet add package Imzala
```

> **Sunucu-taraflı paket.** API anahtarınız hesabınızın tamamına erişir; istemci-taraflı (WASM, MAUI, Unity) bir uygulamaya gömmeyin. Tarayıcıda imza almak için [`@imzala/embed`](../embed) kullanın. Ayrıntı: [Sunucu-taraflı](#️-sunucu-taraflı) bölümü.

## İçindekiler

- [Gereksinimler](#gereksinimler)
- [Hızlı başlangıç](#hızlı-başlangıç)
- [Yapılandırma](#yapılandırma)
- [API referansı](#api-referansı)
  - [Sözleşmeler (Demands)](#sözleşmeler-demands)
  - [Çok belgeli zarf (Demands.Documents, Dispatch)](#çok-belgeli-zarf-demandsdocuments-dispatch)
  - [Şablonlar (Templates)](#şablonlar-templates)
  - [Alan Şablonları (FieldTemplates)](#alan-şablonları-fieldtemplates)
  - [Kişiler (Contacts)](#kişiler-contacts)
  - [Raporlar (Reports)](#raporlar-reports)
  - [Gömülü imza (Embed)](#gömülü-imza-embed)
  - [Zaman damgası (Timestamps)](#zaman-damgası-timestamps)
  - [Hesap (Me)](#hesap-me)
- [İmzalı PDF ve sertifika (binary)](#imzalı-pdf-ve-sertifika-binary)
- [Otomatik yeniden deneme](#otomatik-yeniden-deneme)
- [Sayfalama iteratörü](#sayfalama-iteratörü)
- [Webhook doğrulama](#webhook-doğrulama)
- [Hata yönetimi](#hata-yönetimi)
- [Sık karşılaşılan hatalar](#sık-karşılaşılan-hatalar)
- [Sürüm uyumu](#sürüm-uyumu)
- [Sunucu-taraflı](#️-sunucu-taraflı)
- [İmza sınıfı](#imza-sınıfı)

## Gereksinimler

- .NET **8.0+**
- `imz_` ile başlayan bir API anahtarı, Panel, Geliştirici, API Anahtarları (veya Hesap Ayarları, API Anahtarları) yolundan alınır

## Hızlı başlangıç

```csharp
using ImzalaSdk;
using ImzalaApiClient.Model;

var imzala = new Imzala(Environment.GetEnvironmentVariable("IMZALA_API_KEY")!);

// 1) Şablonları listele, birini seç
var templates = await imzala.Templates.ListAsync();
var template = templates.Templates[0];

// 2) Şablondan sözleşme oluştur (imza daveti otomatik gider)
var demand = await imzala.Demands.CreateAsync(new CreateDemandRequest(
    templateId: template.Id,
    partyMapping: new List<PartyMappingInput>
    {
        new(
            templatePartyId: template.Parties[0].Id,
            firstName: "Ayşe",
            lastName: "Yılmaz",
            email: "ayse@example.com",
            phone: "+905551112233"),
    }));

foreach (var url in demand.SigningUrls) // her taraf için imzalama linki
{
    Console.WriteLine($"{url.FirstName}: {url.SigningUrl}");
}

// 3) Durumu takip et
var status = await imzala.Demands.GetAsync(demand.Id);
var signed = status.Parties.Count(p => p.Signed);
Console.WriteLine($"{signed} taraf imzaladı");

// 4) Tamamlanınca imzalı PDF'i indir
if (status.Status == DemandStatus.StatusEnum.COMPLETED)
{
    byte[] pdf = await imzala.Demands.GetPdfAsync(demand.Id);
    await File.WriteAllBytesAsync("sozlesme.pdf", pdf);
}
```

## Yapılandırma

```csharp
var imzala = new Imzala(
    apiKey: Environment.GetEnvironmentVariable("IMZALA_API_KEY")!,
    baseUrl: "https://api-prd.imzala.org", // varsayılan; test için test-api.imzala.org
    timeoutMs: 30_000,      // istek başına zaman aşımı (varsayılan 30sn)
    maxRetries: 2,          // güvenli GET'ler için (varsayılan 2, 0 = kapalı)
    retryBaseDelayMs: 300); // backoff temel gecikmesi (varsayılan 300ms)
```

| Parametre | Tip | Varsayılan | Açıklama |
|---|---|---|---|
| `apiKey` | `string` | (zorunlu) | `imz_<64 hex>` |
| `baseUrl` | `string` | `https://api-prd.imzala.org` | Test: `https://test-api.imzala.org` |
| `timeoutMs` | `int` | `30000` | İstek başına zaman aşımı (ms) |
| `maxRetries` | `int` | `2` | Yalnızca GET'ler; `0` kapatır (bkz. [Otomatik yeniden deneme](#otomatik-yeniden-deneme)) |
| `retryBaseDelayMs` | `int` | `300` | Exponential backoff temel gecikmesi (ms) |

Her kaynak metodu son parametre olarak opsiyonel bir `CancellationToken` alır.

API anahtarı ve `Idempotency-Key` değerleri yazdırılabilir ASCII olmalıdır; dosyadan okunan anahtarın sonundaki satır sonu gibi karakterler istek gönderilmeden `ImzalaValidationError` fırlatır (`StatusCode` `null`).

`X-Workspace-Id` başlığı için bir seçenek yoktur: organizasyon içinde üretilmiş anahtar kendi organizasyonuna bağlıdır, ayrıca başlık gerekmez. Kişisel anahtarla bir organizasyon adına çalışmak bu sürümde desteklenmez.

## API referansı

Tüm metodlar `{ success, data }` zarfını açar ve `data`'yı döndürür; hata durumunda tipli bir `ImzalaError` fırlatır (bkz. [Hata yönetimi](#hata-yönetimi)). Metod adları C# geleneğine uygun **PascalCase** ve **`Async`** sonekli, dönüş tipleri `Task<T>`'dir.

### Sözleşmeler (Demands)

| Metod | Açıklama | Retry |
|---|---|---|
| `Demands.CreateAsync(CreateDemandRequest body)` / `CreateAsync(body, string? idempotencyKey)` | Şablondan sözleşme oluştur + imza daveti gönder (`dispatchNotifications: false` ile sessiz taslak) | Anahtar varsa 429'da 1 kez |
| `Demands.CreateBulkAsync(ApiV1DemandsBulkPostRequest body)` | Tek istekte en çok 10 sözleşme; her satır bağımsız, `Failed` sayısını ve her satırın durumunu kontrol edin | ❌ POST |
| `Demands.UploadDocumentAsync(UploadDemandParams request)` | Şablonsuz, dosya yükleyerek sözleşme (1 PDF/DOC ya da 1-20 görsel) | Anahtar varsa 429'da 1 kez |
| `Demands.ListAsync(status?, q?, from?, to?, templateId?, page?, limit?, sort?)` | Sözleşme listesi (counts-only, taraf PII'siz); `sort` biçimi `alan:yön` (ör. `createdAt:desc`) | ✅ GET |
| `Demands.GetAsync(Guid id)` | Sözleşme detayı + taraf imza durumu (maskeli) | ✅ GET |
| `Demands.GetPdfAsync(Guid id)` | İmzalı sözleşme PDF'i → `byte[]` | GET (binary) |
| `Demands.GetDocumentPdfAsync(Guid id, Guid documentId)` | Çok belgeli zarfta tek belgenin imzalı PDF'i → `byte[]` | GET (binary) |
| `Demands.GetCertificateAsync(Guid id, string? lang)` | Tamamlanma sertifikası (PAdES B-T) → `byte[]` | GET (binary) |
| `Demands.GetTimelineAsync(Guid id)` | İmza denetim izi (maskeli olaylar) | ✅ GET |
| `Demands.CancelAsync(Guid id, string? reason)` | Bekleyen sözleşmeyi iptal et | ❌ POST |
| `Demands.ResendPartyAsync(Guid id, Guid partyId)` | Tekil tarafa daveti tekrar gönder | ❌ POST |
| `Demands.DeleteAsync(Guid id)` | Tamamlanmamış sözleşmeyi sil | ❌ DELETE |
| `Demands.AddItemsAsync(Guid id, UpsertItemsRequest body)` | Sayfa alanlarını (imza/form) yerleştir (`PAGE_ID_REQUIRED`, `INVALID_ITEM_TYPE`) | ❌ POST |
| `Demands.SendReminderAsync(Guid id, TriggerReminderRequest? body)` | İmzalamamış taraflara hatırlatma (5 dk pencerede `RATE_LIMITED`, `Force = true` aşar) | ❌ POST |
| `Demands.DispatchAsync(Guid id)` / `DispatchAsync(id, bool)` / `DispatchAsync(id, string sendInvitations)` | Sessiz hazırlanmış sözleşmeyi yayına al, davetleri gönder | ❌ POST |

```csharp
// Filtreli liste
var list = await imzala.Demands.ListAsync(status: "PENDING", limit: 20);

// İptal
await imzala.Demands.CancelAsync(id, reason: "Anlaşma değişti");

// Tekil tarafa daveti tekrar gönder
await imzala.Demands.ResendPartyAsync(id, partyId);

// Tamamlanmamış sözleşmeyi sil
await imzala.Demands.DeleteAsync(id);

// Denetim izi
var timeline = await imzala.Demands.GetTimelineAsync(id);
foreach (var e in timeline.Events)
{
    Console.WriteLine($"{e.CreatedAt}  {e.EventType}  {e.ActorLabel}  {e.IpMasked}");
}
```

> `GetPdfAsync`, `GetDocumentPdfAsync` ve `GetCertificateAsync` GET olsalar da ham baytları (`byte[]`) döndürdükleri için yeniden denenmez: bir kez okunan yanıt akışı tekrar oynatılamaz. Ayrıntı: [İmzalı PDF ve sertifika](#imzalı-pdf-ve-sertifika-binary).

```csharp
// Tekrar-güvenli oluşturma: aynı anahtarla ikinci istek ikinci sözleşme üretmez
var created = await imzala.Demands.CreateAsync(body, idempotencyKey: $"siparis-{orderId}");

// Alan Şablonu ile yükleme: her tarafta TemplatePartyId zorunludur
var uploaded = await imzala.Demands.UploadDocumentAsync(new UploadDemandParams
{
    Files = new[] { new FileInput { Content = pdfBytes, FileName = "sozlesme.pdf" } },
    Parties = new[] { new UploadPartyInput { FirstName = "Ayşe", LastName = "Yılmaz", Email = "ayse@example.com", TemplatePartyId = partyTemplateId } },
    FieldTemplateId = fieldTemplateId,
    OnAnchorMiss = "block", // çapa bulunamazsa sözleşme oluşturulmaz, kredi düşmez
    IdempotencyKey = $"yukleme-{orderId}",
});
```

`UploadDemandParams` diğer seçenekleri: `SendInvitations` (bu uçta varsayılan kapalı; `"sms"` SMS ve WhatsApp'ı kapsar, yalnız daraltır) ve `Force`.

### Çok belgeli zarf (Demands.Documents, Dispatch)

Bir sözleşme `dispatchNotifications: false` ile sessizce oluşturulur, belgeler eklenir, sonra tek çağrıyla yayına alınır. Belge uçları kimseye bildirim göndermez ve kredi düşmez; kredi yalnız `Dispatch` anında düşer.

| Metod | Açıklama | Retry |
|---|---|---|
| `Demands.Documents.ListAsync(Guid demandId, string? view)` | Zarftaki belgeler (`"wizard"` atama ve karar sayılarını da verir) | ✅ GET |
| `Demands.Documents.CreateAsync(Guid demandId, ApiV1DemandsDemandIdDocumentsPostRequest body)` | Mevcut bir belgeyi zarfa ekle | ❌ POST |
| `Demands.Documents.UploadAsync(Guid demandId, UploadEnvelopeDocumentParams request)` | Dosya yükleyerek belge ekle; `File`, `Title`, `IdempotencyKey` zorunlu, `DocKind` / `IsRequired` opsiyonel | 429'da 1 kez |
| `Demands.Documents.UpdateAsync(Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdPatchRequest body)` | Başlık / zorunluluk bayrakları (gönderilmeyen bayrak değişmez) | ❌ PATCH |
| `Demands.Documents.DeleteAsync(Guid demandId, Guid docId)` | Belgeyi kaldır (son belge silinemez) | ❌ DELETE |
| `Demands.Documents.ReorderAsync(Guid demandId, IEnumerable<Guid> documentIds)` | Belge sırası (kimlik kümesi birebir eşleşmeli) | ❌ PUT |
| `Demands.Documents.SetAssignmentsAsync(Guid demandId, Guid docId, IEnumerable<Guid> partyIds)` | Belgeyi imzalayacak taraflar | ❌ PUT |
| `Demands.DispatchAsync(Guid demandId)` / `DispatchAsync(demandId, bool)` / `DispatchAsync(demandId, string)` | Zarfı yayına al (`"email"` / `"sms"` kanal daraltır) | ❌ POST |

`UploadAsync` aynı `IdempotencyKey` ile tekrar çağrılırsa yeni belge oluşmaz; sunucunun 409 `IDEMPOTENT_REPLAY` yanıtı normal sonuç olarak (mevcut belge) döner. Özellik hesabınızda açık değilse her belge metodu `ENVELOPE_MULTI_DOC_DISABLED` fırlatır; `ListAsync` boş liste döndürmez.

### Şablonlar (Templates)

| Metod | Açıklama | Retry |
|---|---|---|
| `Templates.ListAsync(page?, limit?)` | Aktif şablonlar (tek sayfa) | ✅ GET |
| `Templates.ListAllAsync(page?, limit?)` | Tüm şablonları gezen `IAsyncEnumerable<TemplateSummary>` | ✅ GET |
| `Templates.GetAsync(Guid id)` | Şablon detayı + taraflar + doldurulabilir alanlar | ✅ GET |
| `Templates.UsageAsync(Guid id)` | API kullanım kılavuzu (curl + JSON örneği) | ✅ GET |
| `Templates.UpdateAsync(Guid id, name?, description?, category?)` | Şablon metadata güncelle (yalnızca dolu argümanlar gönderilir) | ❌ PATCH |
| `Templates.DeleteAsync(Guid id)` | Şablonu sil; kayıt 30 gün saklanır, mevcut sözleşmeler etkilenmez. Aktif (taslak veya imza bekleyen) sözleşmesi olan şablon silinemez (`409 TEMPLATE_IN_USE`) | ❌ DELETE |

```csharp
// Metadata güncelle
await imzala.Templates.UpdateAsync(templateId, name: "Yeni Ad", category: "İK");

// Şablon sil
await imzala.Templates.DeleteAsync(templateId);
```

`Templates.ListAsync` Alan Şablonlarını listelemez ve `Templates.GetAsync` bir Alan Şablonu kimliğine `404` döner; onlar için aşağıdaki `FieldTemplates` kaynağını kullanın.

### Alan Şablonları (FieldTemplates)

| Metod | Açıklama | Retry |
|---|---|---|
| `FieldTemplates.ListAsync(page?, limit?)` | Alan Şablonları (tek sayfa) | ✅ GET |
| `FieldTemplates.GetAsync(Guid id)` | Alan Şablonu detayı | ✅ GET |
| `FieldTemplates.PreviewLayoutAsync(Guid id, IReadOnlyList<FileInput> files, string? onAnchorMiss)` | Yerleşimi tam bir PDF üzerinde kuru koşumla dener; sözleşme oluşturmaz, kredi harcamaz | ❌ POST |

```csharp
var preview = await imzala.FieldTemplates.PreviewLayoutAsync(
    fieldTemplateId,
    new[] { new FileInput { Content = pdfBytes, FileName = "sozlesme.pdf" } },
    onAnchorMiss: "drop"); // verilmezse sunucu "block" uygular
```

### Kişiler (Contacts)

| Metod | Açıklama | Retry |
|---|---|---|
| `Contacts.ListAsync(q?, page?, limit?, sort?, companyId?, archived?)` | Kişiler (tek sayfa); `limit` 10 ile 100 arası, `sort` biçimi `-createdAt` (`-` öneki azalan) | ✅ GET |
| `Contacts.ListAllAsync(...)` | Tüm kişileri gezen `IAsyncEnumerable<ContactSummary>` | ✅ GET |
| `Contacts.CreateAsync(ApiV1ContactsPostRequest body)` | Kişi oluştur (`CONTACT_DUPLICATE`: aynı e-posta veya telefon zaten var) | ❌ POST |

### Raporlar (Reports)

```csharp
var report = await imzala.Reports.GetAsync(); // sözleşme durumlarının toplu sayımı; parametre almaz
```

### Gömülü imza (Embed)

```csharp
var session = await imzala.Embed.CreateSessionAsync(demandId, partyId);
// session.EmbedUrl → bir <iframe>'e gömün (bkz. @imzala/embed)
```

Gömülü imza yalnızca SES/AES üretir (QES değil). Tarayıcı tarafı için [`@imzala/embed`](../embed) veya [`@imzala/embed-react`](../embed-react).

### Zaman damgası (Timestamps)

```csharp
var ts = await imzala.Timestamps.CreateAsync(new CreateTimestampParams
{
    Content = File.ReadAllBytes("belge.pdf"), // byte[]
    FileName = "belge.pdf",
    IdempotencyKey = Guid.NewGuid().ToString(), // tekrarları güvenli yapar (5dk pencere)
});

var listing = await imzala.Timestamps.ListAsync(status: "COMPLETED", sort: "-createdAt"); // limit 10..100
var one = await imzala.Timestamps.GetAsync(ts.Id);
```

TÜBİTAK KAMU SM TSA ile RFC 3161 zaman damgası (var-olma + değişmezlik kanıtı; imza değildir). Bu uç anahtar başına dakikada 10 istekle sınırlıdır (`RATE_LIMIT_EXCEEDED`).

### Hesap (Me)

```csharp
var me = await imzala.MeAsync(); // { Id, Email, FirstName, LastName, Workspace, Credits }
Console.WriteLine($"{me.Email}, kalan kredi: {me.Credits}");
```

`MeAsync` hiçbir kapsam (scope) istemez; geçerli her anahtarla çalışır.

## İmzalı PDF ve sertifika (binary)

`GetPdfAsync`, `GetDocumentPdfAsync` ve `GetCertificateAsync` ham baytları `byte[]` olarak döndürür (JSON zarfı değil). Hata durumunda diğer metodlar gibi `ImzalaError` fırlatırlar. Diske yazın veya stream'leyin:

```csharp
byte[] pdf = await imzala.Demands.GetPdfAsync(id);
await File.WriteAllBytesAsync("sozlesme.pdf", pdf);

byte[] cert = await imzala.Demands.GetCertificateAsync(id, lang: "tr");
await File.WriteAllBytesAsync("sertifika.pdf", cert);
```

Her iki metod da yalnızca `Status == COMPLETED` sözleşmeler için sonuç üretir; API anahtarının sahibi sözleşmenin sahibi olmalıdır.

## Otomatik yeniden deneme

Üç kural vardır; hepsi SDK'nın içindedir:

1. **Okumalar (GET):** `Templates.ListAsync/GetAsync/UsageAsync/ListAllAsync`, `Demands.ListAsync/GetAsync/GetTimelineAsync`, `FieldTemplates.ListAsync/GetAsync`, `Contacts.ListAsync/ListAllAsync`, `Timestamps.ListAsync/GetAsync`, `Reports.GetAsync`, `Demands.Documents.ListAsync` ve `MeAsync` `429` veya `5xx` aldığında en çok `maxRetries` kez (varsayılan 2) jitter'lı exponential backoff ile yeniden denenir. `0` kapatır. Başka her durum (400/401/404/409/422/...) hemen fırlatılır. Binary indirmeler (`GetPdfAsync` / `GetDocumentPdfAsync` / `GetCertificateAsync`) ham akış döndürdükleri için bu kapsamda değildir.
2. **`Idempotency-Key` ile gönderilen yazmalar:** `Demands.CreateAsync(body, idempotencyKey)`, `UploadDocumentAsync` (`UploadDemandParams.IdempotencyKey`), `Timestamps.CreateAsync` (`CreateTimestampParams.IdempotencyKey`) ve `Demands.Documents.UploadAsync` bir 429 sonrasında **tam bir kez** yeniden denenir; sunucu aynı anahtar için ikinci kayıt oluşturmaz. İstek (dosya akışları dahil) tekrar için yeniden kurulur, tüketilmiş bir akış boş gönderilmez. İkinci 429, 5xx ve diğer tüm hatalar doğrudan fırlatılır. Anahtar verilmezse tek denemedir. `maxRetries` bu kuralı etkilemez.
3. **Diğer yazmalar hiç yeniden denenmez:** `CreateBulkAsync`, `Contacts.CreateAsync`, `DispatchAsync`, `SendReminderAsync`, `CancelAsync`, `ResendPartyAsync`, `DeleteAsync`, `AddItemsAsync`, `Templates.UpdateAsync/DeleteAsync`, `Embed.CreateSessionAsync` ve zarf belgesi `CreateAsync/UpdateAsync/DeleteAsync/ReorderAsync/SetAssignmentsAsync`. Tekrarlanan bir `CreateBulkAsync` ikinci bir toplu iş, tekrarlanan bir `SendReminderAsync` ikinci bir SMS/e-posta üretir.

```csharp
var imzala = new Imzala(apiKey, maxRetries: 2, retryBaseDelayMs: 300); // varsayılanlar
var imzalaNoRetry = new Imzala(apiKey, maxRetries: 0);                 // GET yeniden denemesini kapat
```

Bekleme süresi `Retry-After` başlığından okunur (saniye ya da HTTP tarihi); başlık yoksa backoff gecikmesi uygulanır. **Bekleme tavanı 60 saniyedir:** sunucu daha uzun bir süre isterse SDK beklemek yerine 429'u fırlatır (`ImzalaRateLimitError.RetryAfter` süreyi taşır). `NaN`, sonsuz ve negatif `Retry-After` değerleri yok sayılır. Beklemeler çağıranın `CancellationToken`'ına uyar.

> ⚠️ **`ImzalaApiClient.Client.RetryConfiguration` statik Polly politikasını SET ETMEYİN.** Üretilmiş istemci bu statik politika doluysa her isteği taşıma katmanında tekrarlar; yazmalar da (sözleşme oluşturma, hatırlatma, toplu iş) SDK'dan habersiz tekrarlanır ve mükerrer kayıt üretir. SDK bu politikayı hiç ayarlamaz; yeniden deneme yalnız yukarıdaki üç kuralla olur.

## Sayfalama iteratörü

```csharp
await foreach (var template in imzala.Templates.ListAllAsync(limit: 50))
{
    Console.WriteLine($"{template.Id} {template.Name}");
}
```

`ListAllAsync`, `ListAsync`'i (yeniden denemeli) tekrar tekrar çağırıp `IAsyncEnumerable<TemplateSummary>` olarak tek tek şablon döndürür; bir sayfa istenen boyuttan kısa gelince (`Count < limit`) **veya** yanıtın `Total`'ı ulaşılınca, hangisi önce gerçekleşirse, durur (bozuk/boş bir sonuç kümesinde bile sonsuz döngü yok).

## Webhook doğrulama

```csharp
[HttpPost("webhooks/imzala")]
public async Task<IActionResult> HandleWebhook()
{
    // Ham gövde şart: model'e deserialize edip yeniden serialize ETMEDEN oku.
    using var reader = new StreamReader(Request.Body);
    var rawBody = await reader.ReadToEndAsync();

    var valid = Imzala.VerifyWebhook(
        _config["IMZALA_WEBHOOK_SECRET"]!,
        rawBody,                                       // string veya byte[] overload
        Request.Headers["X-Imzala-Signature-256"]);    // 'sha256=<hex>'

    if (!valid) return Unauthorized();

    var evt = JsonSerializer.Deserialize<WebhookEvent>(rawBody);
    // evt.Type: demand.created / demand.completed / demand.expired /
    //           party.signed / party.viewed / party.rejected / kyc.completed / kyc.failed
    return Ok();
}
```

`VerifyWebhook` (statik; `string` ve `byte[]` overload'ları var) asla exception fırlatmaz; geçersiz/eksik imzada `false` döner. İç karşılaştırma sabit-zamanlıdır (`CryptographicOperations.FixedTimeEquals`). Body'yi parse edip yeniden serialize etmeyin: imza byte-byte karşılaştırılır (anahtar sırası/boşluk değişirse doğrulama kırılır).

## Hata yönetimi

```csharp
try
{
    await imzala.Demands.GetAsync(demandId);
}
catch (ImzalaRateLimitError err)
{
    Console.WriteLine($"Rate limit: {err.RetryAfter} sn sonra tekrar dene");
}
catch (ImzalaAuthError)
{
    Console.WriteLine("API anahtarı geçersiz veya yetkisiz");
}
catch (ImzalaValidationError err)
{
    Console.WriteLine($"İstek doğrulanamadı: {err.Body}");
}
catch (ImzalaError err)
{
    Console.WriteLine($"İmzala API hatası: {err.StatusCode} {err.Message}");
}
```

Tüm hatalar `ImzalaError`'dan türer (`StatusCode`, `Body`, `Code`, `CodeDescription` alanları ortak). 401/403 → `ImzalaAuthError`, 429 → `ImzalaRateLimitError` (`RetryAfter` saniye, `RateLimit`), 422 → `ImzalaValidationError`. Diğer statüler (400/404/409/500/...) doğrudan taban `ImzalaError` olarak fırlatılır. Ağ/timeout hataları da `ImzalaError`'a sarılır (`InnerException` orijinali taşır). İstek gönderilmeden yakalanan yerel doğrulama hataları (geçersiz API anahtarı veya `Idempotency-Key` karakteri, üretilmiş istemcinin eksik parametre denetimi) de `ImzalaValidationError`'dır; `StatusCode` `null`, orijinal istisna `InnerException`'da.

- **`Code`:** sunucunun makine-okunur kodu. `{"error": "metin", "code": "KOD"}` biçimindeki gövdelerde `code` alanından okunur; yalnız metin taşıyan gövdede `null` kalır.
- **`CodeDescription`:** kod için katalogdaki tek satırlık Türkçe açıklama. Katalog `ImzalaSdk.ErrorCodes.Codes` (73 kod, salt-okunur sözlük); `ErrorCodes.Describe(code)` ve `ErrorCodes.IsKnown(code)` ile sorgulanır. Katalogda olmayan bir kod açıklamasız fırlatılır, yani yeni bir sunucu sürümü eski SDK'yı kırmaz.
- **`ImzalaRateLimitError.RateLimit`:** standart `RateLimit-Limit` / `RateLimit-Remaining` / `RateLimit-Reset` / `RateLimit-Policy` başlıkları (`RateLimitInfo`). Birden çok sınır 429 döndürür ve farklı kod taşır; bkz. aşağıdaki tablo.

```csharp
using ImzalaSdk;

ErrorCodes.Describe("TEMPLATE_IN_USE"); // "Şablonun aktif (taslak veya imza bekleyen) sözleşmesi olduğu için silinemez."
ErrorCodes.Codes.Count;                 // 73
```

## Sık karşılaşılan hatalar

| Durum | Kod | Ne yapmalı |
|---|---|---|
| 400 | `INVALID_PAGE` | `page` 1 veya daha büyük bir tam sayı olmalı |
| 400 | `INVALID_EXPIRY_DATE` | `expiry_date` geçerli bir takvim tarihi olmalı |
| 400 | `PAGE_ID_REQUIRED` | Alan yerleştirmede her öğede tam sayı `page_id` zorunlu |
| 400 | `INVALID_ITEM_TYPE` | Desteklenen alan tiplerinden birini kullanın (hata mesajı listeler) |
| 404 | (kodsuz) | Alan Şablonu kimliğiyle `Templates.GetAsync` çağrıldı; Alan Şablonları için `FieldTemplates.GetAsync` kullanın (iki kavram ayrıdır) |
| 409 | `TEMPLATE_IN_USE` | Şablonun taslak veya imza bekleyen sözleşmesi var; tamamlanınca silinebilir |
| 409 | `DEMAND_NOT_DISPATCHABLE` | Sözleşme tamamlanmış veya iptal edilmiş; tekrar gönderilemez |
| 409 | `ENVELOPE_MULTI_DOC_DISABLED` | Çok belgeli zarf bu hesapta açık değil; `Demands.Documents.*` bu kodu fırlatır |
| 409 | `IDEMPOTENCY_KEY_REUSED` | Aynı anahtar farklı gövdeyle kullanıldı; yeni sözleşme için yeni anahtar üretin |
| 409 | `IDEMPOTENCY_UNVERIFIABLE` | Anahtar daha önce sözleşme üretti ama isteğin aynı olduğu doğrulanamadı; gövdedeki `demand_id` ile durumu sorgulayın, yeni anahtarla körlemesine tekrarlamayın |
| 409 | `DUPLICATE_SUSPECTED` | Anahtarsız istek son 10 dakikada gönderilmiş aynı içerikle eşleşti; kasten tekrarlamak için `Force` gönderin |
| 422 | `FIELD_LAYOUT_UNRESOLVED` | Alan yerleşimi belgeye uygulanamadı; sözleşme oluşturulmadı, kredi düşülmedi. `OnAnchorMiss` seçin veya belgeyi düzeltin |
| 429 | `RATE_LIMIT_EXCEEDED` | Genel sınır: anahtar başına dakikada 60 istek (zaman damgası ucunda 10). `Retry-After` kadar bekleyin |
| 429 | `TOO_MANY_REQUESTS` | Uca özgü sınır: belge yüklemede dakikada 30, davet tekrarı ve zarf gönderiminde saatte 300 |
| 429 | `RECIPIENT_RESEND_LIMIT` | Aynı alıcıya saatte en çok 3, günde en çok 10 davet tekrarı |

Sözleşme, şablon ve Alan Şablonu listelerinde `limit` üst sınırı 100'dür; daha büyük bir değer hata vermez, sessizce 100'e kırpılır. Kişi ve zaman damgası listelerinde `limit` 10 ile 100 arasında olmalıdır.

## Sürüm uyumu

| Imzala (NuGet) | Konuştuğu API | Durum |
|---|---|---|
| 1.0.0 | v1 (`1.8.17`) | Güncel |
| 0.x | v1 (`1.7.x`) | Bakım dışı; 1.0.0'a yükseltin |

İmzala dış API'si **v1**'dir ve geriye dönük uyumludur: yeni alanlar opsiyonel, yeni davranışlar
opt-in, varsayılan davranış eskisidir. Kırıcı bir değişiklik gerekirse yeni bir major API sürümü
yayımlanır; eski sürüm duyurudan sonra **12 ay** çalışmaya devam eder.

## ⚠️ Sunucu-taraflı

Bu paket **yalnızca sunucuda** kullanılır. API anahtarınızı istemci-taraflı (WASM, MAUI, Unity) bir uygulamaya asla gömmeyin: sızarsa hesabınızdaki tüm sözleşme/şablon/zaman damgası işlemlerine erişilir. Tarayıcıda imza almak için [`@imzala/embed`](../embed) veya [`@imzala/embed-react`](../embed-react) kullanın.

## İmza sınıfı

İmzala **dijital imza (SES)** üretir; her imza zaman damgalıdır. Nitelikli/güvenli elektronik imza (QES) DEĞİLDİR. Gömülü imza da SES/AES üretir. SDK imza sınıfı hakkında hukuki bir iddiada bulunmaz; imza sınıfı sözleşme akışında belirlenir.

## Daha fazla

- Tam API referansı: [api-docs.imzala.org](https://api-docs.imzala.org)
- Kullanım kılavuzu: [imzala.org/docs/api-sozlesme-yasam-dongusu](https://imzala.org/docs/api-sozlesme-yasam-dongusu)
- Çalışan örnekler: [`examples/dotnet`](../../examples/dotnet) (altı senaryo)
- Değişiklik günlüğü: [CHANGELOG.md](./CHANGELOG.md)
- [Monorepo README](../../README.md)
