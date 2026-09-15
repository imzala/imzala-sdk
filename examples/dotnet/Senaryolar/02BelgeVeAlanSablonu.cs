// Kendi PDF'inizi Alan Şablonuyla kuru koşumda deneme, sonra sözleşme olarak yükleme.
// Kapsam: templates:read + demands:write (kuru koşum da bu kapsamı ister).
// Kuru koşum kredi harcamaz ve hiçbir şey oluşturmaz; yükleme kredi harcar (yorumda).

using ImzalaSdk;

namespace ImzalaOrnekleri;

internal sealed class BelgeVeAlanSablonu : ISenaryo
{
    public string Ad => "Belge ve Alan Şablonu";

    public async Task CalistirAsync(Imzala imzala)
    {
        // 1) Alan Şablonunu seç. Alan Şablonu, sözleşme şablonundan ayrıdır: belgedeki
        //    çapa metinlerine göre alanların nereye düşeceğini tarif eder.
        var ortamId = Ortam.Oku("IMZALA_FIELD_TEMPLATE_ID");
        Guid? fieldTemplateId = ortamId != null ? Guid.Parse(ortamId) : null;
        if (fieldTemplateId == null)
        {
            var list = await imzala.FieldTemplates.ListAsync(limit: 20);
            fieldTemplateId = list.FieldTemplates?.FirstOrDefault()?.Id;
        }
        if (fieldTemplateId == null)
        {
            Console.WriteLine("Tanımlı Alan Şablonu yok. Panelden oluşturabilirsiniz.");
            return;
        }

        // 2) Rolleri ve alan sayılarını oku. Yüklemede her rol için template_party_id
        //    bu listeden verilir.
        var detail = await imzala.FieldTemplates.GetAsync(fieldTemplateId.Value);
        Console.WriteLine($"Alan Şablonu: {detail.Name} · toplam alan: {detail.TotalFieldCount}");
        foreach (var party in detail.Parties ?? new())
        {
            Console.WriteLine($"  Rol {party.Order}: {party.Label ?? "(adsız)"} · {party.FieldCount} alan · {party.Id}");
        }

        // 3) KURU KOŞUM: yerleşimi belge üzerinde dene. Hiçbir şey oluşturmaz, kredi
        //    harcamaz, dosyayı saklamaz. Çözülemeyen çapaları önceden görmenin en ucuz
        //    yolu. Çözülemeyen belge de 200 döner: Resolvable=false.
        var pdfPath = Ortam.Oku("IMZALA_PDF_PATH") ?? "ornek-sozlesme.pdf";
        if (!File.Exists(pdfPath))
        {
            Console.WriteLine($"PDF bulunamadı: {pdfPath}. IMZALA_PDF_PATH ile bir PDF verin.");
            return;
        }
        var pdf = new FileInput { Content = await File.ReadAllBytesAsync(pdfPath), FileName = "ornek-sozlesme.pdf", ContentType = "application/pdf" };

        // Üçüncü parametre onAnchorMiss: "drop" yalnız şablonun ilgili alanları zaten drop
        // ise uygulanır; verilmezse block geçerlidir. İmza alanları hiçbir koşulda düşürülmez.
        var preview = await imzala.FieldTemplates.PreviewLayoutAsync(detail.Id, new[] { pdf });
        var toplam = preview.PlacementsSummary?.Total ?? 0;
        Console.WriteLine($"Uygulanabilir: {(preview.Resolvable ? "evet" : "hayır")} · sayfa: {preview.PageCount} · yerleşen alan: {toplam}");
        foreach (var diagnostic in preview.Diagnostics ?? new())
        {
            Console.WriteLine($"  {diagnostic.Severity} {diagnostic.Code}: {diagnostic.Message}");
        }
        foreach (var warning in preview.Warnings ?? new())
        {
            Console.WriteLine($"  Uyarı {warning.Code}: {warning.Message}");
        }
        if (!preview.Resolvable)
        {
            Console.WriteLine("Yerleşim çözülemedi. Belgedeki çapaları düzeltip yeniden deneyin; yükleme 422 ile reddedilir.");
        }

        // 4) Gerçek yükleme. Kredi harcar (yorumda). FieldTemplateId verildiğinde alanlar
        //    bu şablona göre yerleştirilir; her rol tam bir kez eşlenmelidir. Bu uçta
        //    davetler varsayılan olarak KAPALIDIR; SendInvitations = "true" ile aynı
        //    istekte gönderilir. IdempotencyKey ile tekrar deneme ikinci sözleşme yaratmaz.
        //
        // var kisiler = new[]
        // {
        //     ("Ayşe", "Yılmaz", "ayse@example.com", "+905551112233"),
        //     ("Mehmet", "Yılmaz", "mehmet@example.com", "+905551112244"),
        // };
        // var parties = (detail.Parties ?? new()).Select((party, i) =>
        // {
        //     var (ad, soyad, eposta, telefon) = kisiler[i % kisiler.Length];
        //     return new UploadPartyInput { FirstName = ad, LastName = soyad, Email = eposta, Phone = telefon, TemplatePartyId = party.Id };
        // }).ToList();
        // var demand = await imzala.Demands.UploadDocumentAsync(new UploadDemandParams
        // {
        //     Files = new[] { pdf },
        //     Parties = parties,
        //     Title = "Hizmet Sözleşmesi",
        //     FieldTemplateId = detail.Id,
        //     IdempotencyKey = Guid.NewGuid().ToString(),
        //     SendInvitations = "true",
        // });
        // Console.WriteLine($"Sözleşme: {demand.Id} · durum: {demand.Status}");
        // foreach (var party in demand.SigningUrls ?? new())
        // {
        //     Console.WriteLine($"  {party.FirstName} {party.LastName} → {party.SigningUrl}");
        // }
    }
}
