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
  - [Şablonlar (Templates)](#şablonlar-templates)
  - [Gömülü imza (Embed)](#gömülü-imza-embed)
  - [Zaman damgası (Timestamps)](#zaman-damgası-timestamps)
  - [Hesap (Me)](#hesap-me)
- [İmzalı PDF ve sertifika (binary)](#imzalı-pdf-ve-sertifika-binary)
- [Otomatik yeniden deneme](#otomatik-yeniden-deneme)
- [Sayfalama iteratörü](#sayfalama-iteratörü)
- [Webhook doğrulama](#webhook-doğrulama)
- [Hata yönetimi](#hata-yönetimi)
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
            firstName: "Ahmet",
            lastName: "Yılmaz",
            email: "ahmet@example.com",
            phone: "+905301112233"),
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
| `maxRetries` | `int` | `2` | Yalnızca idempotent GET'ler; `0` kapatır |
| `retryBaseDelayMs` | `int` | `300` | Exponential backoff temel gecikmesi (ms) |

Her kaynak metodu son parametre olarak opsiyonel bir `CancellationToken` alır.

## API referansı

Tüm metodlar `{ success, data }` zarfını açar ve `data`'yı döndürür; hata durumunda tipli bir `ImzalaError` fırlatır (bkz. [Hata yönetimi](#hata-yönetimi)). Metod adları C# geleneğine uygun **PascalCase** ve **`Async`** sonekli, dönüş tipleri `Task<T>`'dir.

### Sözleşmeler (Demands)

| Metod | Açıklama | Retry |
|---|---|---|
| `Demands.CreateAsync(CreateDemandRequest body)` | Şablondan sözleşme oluştur + imza daveti gönder | ❌ POST |
| `Demands.UploadDocumentAsync(UploadDemandParams request)` | Şablonsuz, dosya yükleyerek sözleşme (1 PDF/DOC ya da 1-20 görsel) | ❌ POST |
| `Demands.ListAsync(status?, q?, from?, to?, templateId?, page?, limit?, sort?)` | Sözleşme listesi (counts-only, taraf PII'siz) | ✅ GET |
| `Demands.GetAsync(Guid id)` | Sözleşme detayı + taraf imza durumu (maskeli) | ✅ GET |
| `Demands.GetPdfAsync(Guid id)` | İmzalı sözleşme PDF'i → `byte[]` | GET (binary) |
| `Demands.GetCertificateAsync(Guid id, string? lang)` | Tamamlanma sertifikası (PAdES B-T) → `byte[]` | GET (binary) |
| `Demands.GetTimelineAsync(Guid id)` | İmza denetim izi (maskeli olaylar) | ✅ GET |
| `Demands.CancelAsync(Guid id, string? reason)` | Bekleyen sözleşmeyi iptal et | ❌ POST |
| `Demands.ResendPartyAsync(Guid id, Guid partyId)` | Tekil tarafa daveti tekrar gönder | ❌ POST |
| `Demands.DeleteAsync(Guid id)` | Tamamlanmamış sözleşmeyi sil | ❌ DELETE |
| `Demands.AddItemsAsync(Guid id, UpsertItemsRequest body)` | Sayfa alanlarını (imza/form) yerleştir | ❌ POST |
| `Demands.SendReminderAsync(Guid id, TriggerReminderRequest? body)` | İmzalamamış taraflara hatırlatma | ❌ POST |

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

> `GetPdfAsync` ve `GetCertificateAsync` GET olsalar da ham baytları (`byte[]`) döndürdükleri için yeniden denenmez: bir kez okunan yanıt akışı tekrar oynatılamaz. Ayrıntı: [İmzalı PDF ve sertifika](#imzalı-pdf-ve-sertifika-binary).

### Şablonlar (Templates)

| Metod | Açıklama | Retry |
|---|---|---|
| `Templates.ListAsync(page?, limit?)` | Aktif şablonlar (tek sayfa) | ✅ GET |
| `Templates.ListAllAsync(page?, limit?)` | Tüm şablonları gezen `IAsyncEnumerable<TemplateSummary>` | ✅ GET |
| `Templates.GetAsync(Guid id)` | Şablon detayı + taraflar + doldurulabilir alanlar | ✅ GET |
| `Templates.UsageAsync(Guid id)` | API kullanım kılavuzu (curl + JSON örneği) | ✅ GET |
| `Templates.UpdateAsync(Guid id, name?, description?, category?)` | Şablon metadata güncelle (yalnızca dolu argümanlar gönderilir) | ❌ PATCH |
| `Templates.DeleteAsync(Guid id)` | Şablon sil (soft-delete) | ❌ DELETE |

```csharp
// Metadata güncelle
await imzala.Templates.UpdateAsync(templateId, name: "Yeni Ad", category: "İK");

// Şablon sil
await imzala.Templates.DeleteAsync(templateId);
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
```

TÜBİTAK KAMU SM TSA ile RFC 3161 zaman damgası (var-olma + değişmezlik kanıtı; imza değildir).

### Hesap (Me)

```csharp
var me = await imzala.MeAsync(); // { Id, Email, FirstName, LastName, Workspace, Credits }
Console.WriteLine($"{me.Email}, kalan kredi: {me.Credits}");
```

## İmzalı PDF ve sertifika (binary)

`GetPdfAsync` ve `GetCertificateAsync` ham baytları `byte[]` olarak döndürür (JSON zarfı değil). Diske yazın veya stream'leyin:

```csharp
byte[] pdf = await imzala.Demands.GetPdfAsync(id);
await File.WriteAllBytesAsync("sozlesme.pdf", pdf);

byte[] cert = await imzala.Demands.GetCertificateAsync(id, lang: "tr");
await File.WriteAllBytesAsync("sertifika.pdf", cert);
```

Her iki metod da yalnızca `Status == COMPLETED` sözleşmeler için sonuç üretir; API anahtarının sahibi sözleşmenin sahibi olmalıdır.

## Otomatik yeniden deneme

Yalnızca **GET (okuma)** uçları, yani `Templates.ListAsync/GetAsync/UsageAsync`, `Demands.ListAsync/GetAsync/GetTimelineAsync` ve `MeAsync`, `429` (rate limit, `Retry-After`'a uyarak) veya `5xx` (sunucu hatası) aldığında jitter'lı exponential backoff ile yeniden denenir. Başka her durum (400/401/404/409/422/...) hemen fırlatılır.

```csharp
var imzala = new Imzala(apiKey, maxRetries: 2, retryBaseDelayMs: 300); // varsayılanlar
var imzalaNoRetry = new Imzala(apiKey, maxRetries: 0);                 // yeniden denemeyi kapat
```

**🔒 POST/PATCH/DELETE (yazma) uçları ASLA yeniden denenmez** ve bu yapılandırılamaz: `Demands.CreateAsync`, `AddItemsAsync`, `UploadDocumentAsync`, `SendReminderAsync`, `CancelAsync`, `ResendPartyAsync`, `DeleteAsync`, `Templates.UpdateAsync`, `Templates.DeleteAsync`, `Embed.CreateSessionAsync`, `Timestamps.CreateAsync` her zaman tek seferliktir. Yeniden denenen bir `Demands.CreateAsync` çağrısı mükerrer sözleşme oluşturur. Bu kural yapısaldır (çalışma zamanı bayrağı değil): iç `Http.UnwrapRetryableGet` yardımcısının bir `method` parametresi yoktur, bu yüzden bir yazma çağrısını yeniden denemeye "opt-in" etmenin hiçbir yolu yoktur. `GetPdfAsync` ve `GetCertificateAsync` de GET olmakla birlikte ham binary döndürdükleri için yeniden denenmez.

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
    // evt.Type: demand.created / demand.completed / demand.cancelled ...
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

Tüm hatalar `ImzalaError`'dan türer (`StatusCode`, `Body`, `Code` alanları ortak). 401/403 → `ImzalaAuthError`, 429 → `ImzalaRateLimitError` (`RetryAfter` saniye), 422 → `ImzalaValidationError`. Diğer statüler (400/404/409/500/...) doğrudan taban `ImzalaError` olarak fırlatılır. Ağ/timeout hataları da `ImzalaError`'a sarılır (`InnerException` orijinali taşır).

## ⚠️ Sunucu-taraflı

Bu paket **yalnızca sunucuda** kullanılır. API anahtarınızı istemci-taraflı (WASM, MAUI, Unity) bir uygulamaya asla gömmeyin: sızarsa hesabınızdaki tüm sözleşme/şablon/zaman damgası işlemlerine erişilir. Tarayıcıda imza almak için [`@imzala/embed`](../embed) veya [`@imzala/embed-react`](../embed-react) kullanın.

## İmza sınıfı

İmzala **dijital imza (SES)** üretir; her imza zaman damgalıdır. Nitelikli/güvenli elektronik imza (QES) DEĞİLDİR. Gömülü imza da SES/AES üretir. SDK imza sınıfı hakkında hukuki bir iddiada bulunmaz; imza sınıfı sözleşme akışında belirlenir.

## Daha fazla

- Tam API referansı: [api-docs.imzala.org](https://api-docs.imzala.org)
- Kullanım kılavuzu: [imzala.org/docs/api-sozlesme-yasam-dongusu](https://imzala.org/docs/api-sozlesme-yasam-dongusu)
- Çalışan örnek: [`examples/dotnet`](../../examples/dotnet)
- [Monorepo README](../../README.md)
