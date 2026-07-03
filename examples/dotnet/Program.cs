// İmzala .NET SDK, sözleşme yaşam döngüsü örneği.
//
// Çalıştırma:
//   IMZALA_API_KEY=imz_... IMZALA_BASE_URL=https://test-api.imzala.org \
//     dotnet run --project examples/dotnet
//
// Salt-okuma işlemleri doğrudan çalışır. Veri değiştiren işlemler (create /
// cancel / resendParty / delete) yorum satırıdır: gerçek kredi harcar / durum
// değiştirir.
//
// İmzala sözleşmeleri varsayılan olarak dijital imza (SES) üretir; SDK imza
// geçerliliği hakkında hukuki iddiada bulunmaz.

using ImzalaSdk;

var apiKey = Environment.GetEnvironmentVariable("IMZALA_API_KEY");
if (string.IsNullOrEmpty(apiKey))
{
    Console.Error.WriteLine("IMZALA_API_KEY gerekli (imz_...). Panel, Geliştirici, API Anahtarları.");
    return 1;
}
var baseUrl = Environment.GetEnvironmentVariable("IMZALA_BASE_URL");

// baseUrl boş ise SDK varsayılanını (api-prd) kullan.
var imzala = string.IsNullOrEmpty(baseUrl)
    ? new Imzala(apiKey)
    : new Imzala(apiKey, baseUrl);

try
{
    // 1) Kimlik + kredi bakiyesi
    var me = await imzala.MeAsync();
    Console.WriteLine($"\nKimlik: {me}");

    // 2) Şablonlar (bir sayfa)
    var templates = await imzala.Templates.ListAsync(limit: 5);
    Console.WriteLine($"\nŞablonlar: {templates.Total}");

    // 3) Sözleşme listesi (counts-only, taraf PII'si yok)
    var list = await imzala.Demands.ListAsync(limit: 5, sort: "createdAt:desc");
    Console.WriteLine($"\nSözleşme sayısı: {list.Total}");
    foreach (var d in list.Demands ?? new())
    {
        Console.WriteLine($"   - {d.Id}  [{d.Status}]  {d.PartiesSigned}/{d.PartiesTotal}  {d.Title}");
    }

    var first = (list.Demands ?? new()).FirstOrDefault();
    if (first is null)
    {
        Console.WriteLine("\n(Sözleşme yok, create örneğini açın.)");
        return 0;
    }
    var firstId = Guid.Parse(first.Id);

    // 4) Sözleşme detay (imzacı adı kısaltılmış, e-posta maskeli, KVKK)
    var demand = await imzala.Demands.GetAsync(firstId);
    Console.WriteLine($"\nDetay: {demand}");

    // 5) İmza denetim izi (maskeli)
    var timeline = await imzala.Demands.GetTimelineAsync(firstId);
    Console.WriteLine($"\nDenetim izi: {timeline}");

    // 6) Tamamlanmış sözleşmenin imzalı PDF'ini indir (binary, byte[])
    if (string.Equals(first.Status?.ToString(), "COMPLETED"))
    {
        var pdf = await imzala.Demands.GetPdfAsync(firstId);
        var outPath = $"demand-{firstId}.pdf";
        await File.WriteAllBytesAsync(outPath, pdf);
        Console.WriteLine($"\nİmzalı PDF kaydedildi: {outPath} ({pdf.Length} bayt)");

        // Tamamlanma sertifikası (PAdES B-T):
        // var cert = await imzala.Demands.GetCertificateAsync(firstId, lang: "tr");
    }

    // Veri değiştiren işlemler (bilerek yorumlu):
    // await imzala.Demands.CancelAsync(firstId, reason: "Vazgeçildi");
    // await imzala.Demands.ResendPartyAsync(firstId, partyId);
    // await imzala.Demands.DeleteAsync(firstId);
    // await imzala.Templates.UpdateAsync(templateId, name: "Yeni Ad");
    // await imzala.Templates.DeleteAsync(templateId);

    return 0;
}
catch (ImzalaError e)
{
    Console.Error.WriteLine($"\nImzalaError [{e.StatusCode}]: {e.Message}");
    return 1;
}
