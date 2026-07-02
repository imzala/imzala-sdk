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
  `.UsageAsync(id)`
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
