# Imzala (.NET)

İmzala dijital imza platformu resmi .NET SDK'sı.

> ⚠️ Geliştirme aşamasında, yayın öncesi avukat onayı bekliyor. Kapsam ve
> yayın kararları için monorepo kökündeki [RELEASING.md](../../RELEASING.md).

## Kurulum

```bash
dotnet add package Imzala
```

.NET 8.0+ gerektirir.

## Hızlı başlangıç

```csharp
using ImzalaSdk;
using ImzalaApiClient.Model;

var imzala = new Imzala(Environment.GetEnvironmentVariable("IMZALA_API_KEY")!);

// Aktif şablonlarını listele
var templateList = await imzala.Templates.ListAsync();

// Bir şablondan yeni sözleşme (demand) oluştur
var demand = await imzala.Demands.CreateAsync(new CreateDemandRequest(
    templateId: templateList.Templates[0].Id,
    partyMapping: new List<PartyMappingInput>
    {
        new(
            templatePartyId: templateList.Templates[0].Parties[0].Id,
            firstName: "Ahmet",
            lastName: "Yılmaz",
            email: "ahmet@example.com"),
    }));

Console.WriteLine(demand.SigningUrls); // her taraf için imzalama linki
```

`baseUrl` varsayılan olarak `https://api-prd.imzala.org`'dur; test ortamı
için `new Imzala(apiKey, baseUrl: "https://test-api.imzala.org")`.

## Kaynaklar

- `imzala.Templates.ListAsync(page?, limit?)` / `.GetAsync(id)` /
  `.UsageAsync(id)` / `.ListAllAsync(page?, limit?)` (bkz. aşağıdaki
  sayfalama iteratörü)
- `imzala.Demands.CreateAsync(body)` / `.GetAsync(id)` /
  `.AddItemsAsync(id, body)` / `.UploadDocumentAsync(new UploadDemandParams {...})` /
  `.SendReminderAsync(id, body?)`
- `imzala.Embed.CreateSessionAsync(demandId, partyId)`: bkz. aşağıdaki
  gömülü imza notu
- `imzala.Timestamps.CreateAsync(new CreateTimestampParams {...})`
- `imzala.MeAsync()`: API anahtarının sahibi (id, e-posta, workspace, kalan kredi)

Her metod bir `CancellationToken` opsiyonel parametresi alır. Dosya
yükleyen metodlar (`Demands.UploadDocumentAsync`, `Timestamps.CreateAsync`)
`FileInput { Content: byte[], FileName: string, ContentType?: string }`
kabul eder.

## Otomatik yeniden deneme

`Templates.ListAsync/GetAsync/UsageAsync`, `Demands.GetAsync` ve `MeAsync`
(hepsi **GET**, idempotent) `429` (rate limit — `Retry-After`'a uyar) veya
`5xx` (sunucu hatası) aldığında, jitter'lı exponential backoff ile otomatik
yeniden dener. Başka her durum (400/401/404/409/422/...) hemen fırlatılır.

```csharp
var imzala = new Imzala(apiKey, maxRetries: 2, retryBaseDelayMs: 300); // varsayılanlar
var imzalaNoRetry = new Imzala(apiKey, maxRetries: 0); // yeniden denemeyi kapat
```

**🔒 Güvenlik — yazma (POST) metodları ASLA yeniden denenmez:**
`Demands.CreateAsync`, `.AddItemsAsync`, `.UploadDocumentAsync`,
`.SendReminderAsync`, `Embed.CreateSessionAsync`, `Timestamps.CreateAsync`
her zaman tek seferlik `Http.Unwrap` kullanır — yeniden denenen bir
`Demands.CreateAsync` çağrısı **yinelenen bir sözleşme** oluşturabilir. Bu
kural yapısaldır (çalışma zamanı bayrağı değil): `Http.UnwrapRetryableGet`'in
`method` parametresi yoktur, bu yüzden bir yazma çağrısını yeniden
denemeye "opt-in" etmenin hiçbir yolu yoktur — her kaynak metodu kaynak
kodu seviyesinde ya `Http.Unwrap` (yazma, tek deneme) ya da
`Http.UnwrapRetryableGet` (okuma) kullanacak şekilde sabitlenmiştir.

## Sayfalama iteratörü

```csharp
await foreach (var template in imzala.Templates.ListAllAsync(limit: 50))
{
    Console.WriteLine($"{template.Id} {template.Name}");
}
```

`Templates.ListAllAsync(page?, limit?)`, `ListAsync(page, limit)`'i
(yeniden denemeli) tekrar tekrar çağırıp `IAsyncEnumerable<TemplateSummary>`
olarak tek tek şablon döndürür; bir sayfa istenen boyuttan kısa gelene
(`Count < limit`) **veya** yanıtın `Total`'ı ulaşılana kadar — hangisi önce
gerçekleşirse — devam eder, böylece bozuk/boş bir sonuç kümesine karşı bile
asla sonsuz döngüye girmez. `ListAsync` kendisi değişmedi (hâlâ tek sayfa).

## Webhook doğrulama

```csharp
[HttpPost("webhooks/imzala")]
public async Task<IActionResult> HandleWebhook()
{
    using var reader = new StreamReader(Request.Body);
    var rawBody = await reader.ReadToEndAsync(); // ham body, deserialize ETMEDEN önce

    var valid = Imzala.VerifyWebhook(
        _config["IMZALA_WEBHOOK_SECRET"]!,
        rawBody,
        Request.Headers["X-Imzala-Signature-256"]);

    if (!valid) return Unauthorized();

    var evt = JsonSerializer.Deserialize<WebhookEvent>(rawBody);
    // ... evt.Type'a göre işle
    return Ok();
}
```

`VerifyWebhook` (statik, `byte[]` veya `string` overload) asla exception
fırlatmaz; geçersiz/eksik imzada `false` döner. Body'yi model'e deserialize
edip yeniden serialize etmeyin, imza doğrulaması byte-byte karşılaştırma
yapar.

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

Tüm hatalar `ImzalaError`'dan türer (`StatusCode`, `Body`, `Code` alanları
ortak); 401/403 → `ImzalaAuthError`, 429 → `ImzalaRateLimitError`
(`RetryAfter` saniye), 422 → `ImzalaValidationError`.

## ⚠️ Sunucu-taraflı

Bu paket **sadece sunucuda** kullanılır. API anahtarınızı istemci-taraflı
(WASM, MAUI, Unity) bir uygulamaya asla gömmeyin: sızarsa hesabınızdaki
tüm sözleşme/şablon/zaman damgası işlemlerine erişim kazanılır. Tarayıcıda
imza almak için [`@imzala/embed`](../embed) veya
[`@imzala/embed-react`](../embed-react) kullanın.

## İmza sınıfı notu

İmzala dijital imza (SES) üretir; her imza zaman damgalıdır. Nitelikli/
güvenli elektronik imza (QES) DEĞİLDİR.

`imzala.Embed.CreateSessionAsync(...)` şu an **yalnızca test ortamında**
çalışır; gömülü imza özelliği henüz avukat onaylı prod-canlı değildir.
Kapsam kararı [RELEASING.md](../../RELEASING.md)'de.

## Daha fazla

- [Monorepo README](../../README.md): tüm paketler + genel desen
- [RELEASING.md](../../RELEASING.md): sürüm yayınlama, kapsam kararları
