// Bir dosyanın belirli bir anda var olduğunu ve o andan beri değişmediğini kanıtlayan
// zaman damgası hizmeti: damga al, damgaları listele, tek kaydı getir.
// Kapsam: timestamps. Damga almak kredi harcar (yorumda); liste ve getirme harcamaz.
//
// Damga dijital imza DEĞİLDİR: dosyayı kimin yazdığını ya da kimin sahiplendiğini
// kanıtlamaz; yalnız var olma ve değişmezlik kanıtıdır. Owner alanları bilgilendirme
// amaçlıdır ve API tarafından doğrulanmaz.

using ImzalaSdk;

namespace ImzalaOrnekleri;

internal sealed class ZamanDamgasi : ISenaryo
{
    public string Ad => "Zaman damgası";

    public async Task CalistirAsync(Imzala imzala)
    {
        // 1) Damga al. Kredi harcar (yorumda). IdempotencyKey, ağ koptuğunda ikinci kez
        //    kredi harcamayı önler: beş dakika içinde aynı anahtar aynı kaydı döndürür.
        var pdfPath = Ortam.Oku("IMZALA_PDF_PATH") ?? "ornek-sozlesme.pdf";
        if (File.Exists(pdfPath))
        {
            Console.WriteLine($"Damgalanacak dosya hazır: {pdfPath} (damga alma adımı yorumda).");
        }
        //
        // var kayit = await imzala.Timestamps.CreateAsync(new CreateTimestampParams
        // {
        //     Content = await File.ReadAllBytesAsync(pdfPath),
        //     FileName = "ornek-sozlesme.pdf",
        //     ContentType = "application/pdf",
        //     IdempotencyKey = Guid.NewGuid().ToString(),
        //     Description = "Taslak sürüm",
        //     OwnerFirstName = "Ayşe",
        //     OwnerLastName = "Yılmaz",
        // });
        // Console.WriteLine($"Damga: {kayit.Id} · {kayit.TimestampTime:O} · {kayit.TsaAuthority}");
        // Console.WriteLine($"  SHA-256: {kayit.FileSha256} · harcanan kredi: {kayit.CreditsUsed}");

        // 2) Damgaları listele (varsayılan sıralama yeni → eski).
        var liste = await imzala.Timestamps.ListAsync(limit: 10);
        Console.WriteLine($"Toplam damga: {liste.Total}");
        foreach (var damga in liste.Timestamps ?? new())
        {
            Console.WriteLine($"{damga.Id} · {damga.OriginalFileName} · {damga.TimestampDate:O} · {damga.Status}");
        }

        // 3) Tek kaydın ayrıntısı: IMZALA_TIMESTAMP_ID verilmişse o, yoksa listedeki ilk kayıt.
        var ortamId = Ortam.Oku("IMZALA_TIMESTAMP_ID");
        Guid? timestampId = ortamId != null ? Guid.Parse(ortamId) : liste.Timestamps?.FirstOrDefault()?.Id;
        if (timestampId == null)
        {
            Console.WriteLine("Henüz damga kaydı yok.");
            return;
        }
        var detay = await imzala.Timestamps.GetAsync(timestampId.Value);
        Console.WriteLine($"Kayıt {detay.Id}: {detay.OriginalFileName} ({detay.OriginalFileSize} bayt)");
        Console.WriteLine($"  Açıklama: {detay.Description ?? "-"} · durum: {detay.Status}");
        Console.WriteLine($"  Damga dosyası: {detay.TimestampFileUrl}");
    }
}
