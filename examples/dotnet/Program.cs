// İmzala .NET SDK, sözleşme yaşam döngüsü örneği (çalışır).
//
// Çalıştırma:
//   IMZALA_API_KEY=imz_... IMZALA_BASE_URL=https://test-api.imzala.org \
//     dotnet run --project examples/dotnet
//
// Salt-okuma işlemleri doğrudan çalışır. Veri değiştiren işlemler (create /
// cancel / resendParty / delete / update) yorum satırıdır: gerçek kredi harcar
// veya durum değiştirir. Açmadan önce ne yaptığını okuyun.
//
// İmzala sözleşmeleri varsayılan olarak dijital imza (SES) üretir; SDK imza
// geçerliliği hakkında hukuki iddiada bulunmaz.

using ImzalaSdk;
using ImzalaApiClient.Model;

var apiKey = Environment.GetEnvironmentVariable("IMZALA_API_KEY");
if (string.IsNullOrEmpty(apiKey))
{
    Console.Error.WriteLine("IMZALA_API_KEY gerekli (imz_...). Panel, Geliştirici, API Anahtarları.");
    return 1;
}
var baseUrl = Environment.GetEnvironmentVariable("IMZALA_BASE_URL");

// baseUrl boş ise SDK varsayılanını (api-prd.imzala.org) kullan.
var imzala = string.IsNullOrEmpty(baseUrl)
    ? new Imzala(apiKey)
    : new Imzala(apiKey, baseUrl);

try
{
    // 1) Kimlik + kredi bakiyesi
    var me = await imzala.MeAsync();
    Console.WriteLine($"\n👤 {me.Email ?? me.Id.ToString()}, kredi: {me.Credits}");

    // 2) Şablonlar (bir sayfa)
    var templates = await imzala.Templates.ListAsync(limit: 5);
    Console.WriteLine($"\n📄 Şablon sayısı: {templates.Total}");
    foreach (var t in templates.Templates ?? new())
    {
        Console.WriteLine($"   - {t.Id}  {t.Name}");
    }

    // 3) Sözleşme listesi (counts-only, taraf PII'si yok)
    var list = await imzala.Demands.ListAsync(limit: 5, sort: "createdAt:desc");
    Console.WriteLine($"\n📋 Sözleşme sayısı: {list.Total}");
    foreach (var d in list.Demands ?? new())
    {
        Console.WriteLine($"   - {d.Id}  [{d.Status}]  {d.PartiesSigned}/{d.PartiesTotal} imzalı  {d.Title}");
    }

    var first = (list.Demands ?? new()).FirstOrDefault();
    if (first is null)
    {
        Console.WriteLine("\n(Sözleşme yok, aşağıdaki create örneğini açın.)");
        return 0;
    }
    var firstId = first.Id; // zaten Guid

    // 4) Sözleşme detay (imzacı adı kısaltılmış, e-posta maskeli, KVKK)
    var demand = await imzala.Demands.GetAsync(firstId);
    Console.WriteLine($"\n🔎 {demand.Id} taraflar:");
    foreach (var p in demand.Parties ?? new())
    {
        var durum = p.Signed ? "✅ imzaladı" : "⏳ bekliyor";
        Console.WriteLine($"   - {p.Name}  {p.EmailMasked}  {durum}");
    }

    // 5) İmza denetim izi (maskeli, ip_masked, ham IP yok)
    var timeline = await imzala.Demands.GetTimelineAsync(firstId);
    Console.WriteLine($"\n🕒 Denetim izi: {timeline.Events?.Count ?? 0} olay");
    foreach (var e in (timeline.Events ?? new()).Take(5))
    {
        Console.WriteLine($"   - {e.CreatedAt}  {e.EventType}  {e.ActorLabel}  {e.IpMasked}");
    }

    // 6) Tamamlanmış sözleşmenin imzalı PDF'ini indir (binary, byte[])
    if (string.Equals(first.Status, "COMPLETED", StringComparison.Ordinal))
    {
        byte[] pdf = await imzala.Demands.GetPdfAsync(firstId);
        var outPath = $"demand-{firstId}.pdf";
        await File.WriteAllBytesAsync(outPath, pdf);
        Console.WriteLine($"\n💾 İmzalı PDF kaydedildi: {outPath} ({pdf.Length} bayt)");

        // Tamamlanma sertifikası (PAdES B-T):
        // byte[] cert = await imzala.Demands.GetCertificateAsync(firstId, lang: "tr");
        // await File.WriteAllBytesAsync($"demand-{firstId}-sertifika.pdf", cert);
    }

    // ── Veri değiştiren işlemler (bilerek yorumlu) ─────────────────────────────
    //
    // // Şablondan yeni sözleşme oluştur (1 kredi harcar):
    // var created = await imzala.Demands.CreateAsync(new CreateDemandRequest(
    //     templateId: templates.Templates[0].Id,
    //     partyMapping: new List<PartyMappingInput>
    //     {
    //         new(
    //             templatePartyId: templates.Templates[0].Parties[0].Id,
    //             firstName: "Ada", lastName: "Kalkan",
    //             email: "ada@example.com", phone: "+905304636743"),
    //     }));
    // Console.WriteLine($"İmza URL: {created.SigningUrls[0].SigningUrl}");
    //
    // // Bekleyen sözleşmeyi iptal et:
    // await imzala.Demands.CancelAsync(firstId, reason: "Vazgeçildi");
    //
    // // Tekil tarafa daveti tekrar gönder:
    // await imzala.Demands.ResendPartyAsync(firstId, partyId);
    //
    // // Tamamlanmamış sözleşmeyi sil:
    // await imzala.Demands.DeleteAsync(firstId);
    //
    // // Şablon metadata güncelle / sil:
    // await imzala.Templates.UpdateAsync(templateId, name: "Yeni Ad");
    // await imzala.Templates.DeleteAsync(templateId);
    //
    // // Zaman damgası (RFC 3161, TÜBİTAK KAMU SM TSA):
    // var ts = await imzala.Timestamps.CreateAsync(new CreateTimestampParams
    // {
    //     Content = await File.ReadAllBytesAsync("belge.pdf"),
    //     FileName = "belge.pdf",
    //     IdempotencyKey = Guid.NewGuid().ToString(),
    // });

    return 0;
}
catch (ImzalaError e)
{
    Console.Error.WriteLine($"\n❌ ImzalaError [{e.StatusCode}]: {e.Message}");
    return 1;
}

// ── Webhook imza doğrulama (ayrı bir ASP.NET Core handler'da) ────────────────
//
// [HttpPost("webhooks/imzala")]
// public async Task<IActionResult> HandleWebhook()
// {
//     using var reader = new StreamReader(Request.Body);
//     var rawBody = await reader.ReadToEndAsync(); // ham gövde, deserialize ETMEDEN
//     var ok = Imzala.VerifyWebhook(
//         Environment.GetEnvironmentVariable("IMZALA_WEBHOOK_SECRET")!,
//         rawBody,                                     // string / byte[] overload
//         Request.Headers["X-Imzala-Signature-256"]);  // 'sha256=<hex>'
//     if (!ok) return Unauthorized();
//     // ... evt.Type: demand.created / demand.completed / ...
//     return Ok();
// }
