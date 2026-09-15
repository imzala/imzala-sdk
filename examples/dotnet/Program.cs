// İmzala .NET SDK örnekleri: senaryo numarasıyla çalıştırın, ör. `dotnet run --project dotnet -- 01`.
// Ortak kurulum: anahtar ve taban adres ortam değişkeninden okunur (IMZALA_API_KEY, IMZALA_BASE_URL).
// Her senaryonun kapsam ve kredi notu kendi dosyasının başındadır.

using ImzalaOrnekleri;
using ImzalaSdk;

var apiKey = Environment.GetEnvironmentVariable("IMZALA_API_KEY");
if (string.IsNullOrEmpty(apiKey))
{
    Console.Error.WriteLine("IMZALA_API_KEY tanımlı değil.");
    return 1;
}
var baseUrl = Environment.GetEnvironmentVariable("IMZALA_BASE_URL");
if (string.IsNullOrEmpty(baseUrl))
{
    baseUrl = "https://api-prd.imzala.org"; // SDK varsayılanı
}

var senaryolar = new Dictionary<string, ISenaryo>
{
    ["01"] = new SablondanSozlesme(),
    ["02"] = new BelgeVeAlanSablonu(),
    ["03"] = new ZamanDamgasi(),
    ["04"] = new KisilerVeRaporlar(),
    ["05"] = new TopluVeTekrarKoruma(),
    ["06"] = new Zarf(),
};

var secim = args.FirstOrDefault() ?? "01";
if (!senaryolar.TryGetValue(secim, out var senaryo))
{
    Console.WriteLine($"Bilinmeyen senaryo: {secim}. Seçenekler: {string.Join(", ", senaryolar.Keys)}");
    return 1;
}

// Senaryo 05, 429'u kendisi görmek için GET yinelemesini kapatır (maxRetries: 0).
var imzala = senaryo.YinelemeKapali
    ? new Imzala(apiKey, baseUrl, maxRetries: 0)
    : new Imzala(apiKey, baseUrl);

Console.WriteLine($"[{secim}] {senaryo.Ad}");
await senaryo.CalistirAsync(imzala);
return 0;
