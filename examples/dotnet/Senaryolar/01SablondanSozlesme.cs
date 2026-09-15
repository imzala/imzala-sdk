// Şablondan sözleşme oluşturma, davet linklerini alma ve webhook imzasını doğrulama.
// Kapsam: templates:read + demands:read; sözleşme oluşturma için demands:write.
// Sözleşme oluşturma kredi harcar (yorumda). Diğer adımlar kredi harcamaz.

using ImzalaApiClient.Model;
using ImzalaSdk;

namespace ImzalaOrnekleri;

internal sealed class SablondanSozlesme : ISenaryo
{
    public string Ad => "Şablondan sözleşme";

    public async Task CalistirAsync(Imzala imzala)
    {
        // 1) Hesap ve kalan kredi. Bu uç kapsam istemez.
        var me = await imzala.MeAsync();
        var kredi = me.Credits != null ? me.Credits.Remaining.ToString() : "?";
        Console.WriteLine($"Hesap: {me.Email} · kalan kredi: {kredi}");

        // 2) Şablonu seç: IMZALA_TEMPLATE_ID verilmişse o, yoksa listedeki ilk şablon.
        var ortamId = Ortam.Oku("IMZALA_TEMPLATE_ID");
        Guid? templateId = ortamId != null ? Guid.Parse(ortamId) : null;
        if (templateId == null)
        {
            var templates = await imzala.Templates.ListAsync(limit: 10);
            templateId = templates.Templates?.FirstOrDefault()?.Id;
        }
        if (templateId == null)
        {
            Console.WriteLine("Aktif şablon yok. Panelden bir şablon oluşturun.");
            return;
        }

        // 3) Şablonun rollerini ve değişkenlerini oku. party_mapping bu rollere göre
        //    kurulur: her rol için template_party_id zorunludur.
        var detail = await imzala.Templates.GetAsync(templateId.Value);
        Console.WriteLine($"Şablon: {detail.Name} · {detail.PagesCount} sayfa");
        foreach (var party in detail.Parties ?? new())
        {
            var optional = party.IsRequired ? "" : " · isteğe bağlı";
            Console.WriteLine($"  Rol {party.Order}: {party.Label ?? "(adsız)"} · {party.Id}{optional}");
        }
        foreach (var variable in detail.Variables ?? new())
        {
            Console.WriteLine($"  Değişken: {variable.Slug} ({variable.ItemType})");
        }

        // 4) Sözleşmeyi oluştur. Kredi harcar; açmadan önce test ortamını kullanın.
        //    İkinci parametre Idempotency-Key: ağ koptuğunda aynı anahtarla tekrar denemek
        //    ikinci bir sözleşme yaratmaz; SDK bir 429 sonrasında anahtarlı isteği bir kez yineler.
        //    Kişiler kurgusaldır; her rol için template_party_id şablon detayından gelir.
        //
        // var kisiler = new[]
        // {
        //     ("Ayşe", "Yılmaz", "ayse@example.com", "+905551112233"),
        //     ("Mehmet", "Yılmaz", "mehmet@example.com", "+905551112244"),
        // };
        // var partyMapping = (detail.Parties ?? new()).Select((party, i) =>
        // {
        //     var (ad, soyad, eposta, telefon) = kisiler[i % kisiler.Length];
        //     return new PartyMappingInput(templatePartyId: party.Id, firstName: ad, lastName: soyad, email: eposta, phone: telefon);
        // }).ToList();
        // var demand = await imzala.Demands.CreateAsync(
        //     new CreateDemandRequest(
        //         templateId: detail.Id,
        //         title: "Hizmet Sözleşmesi",
        //         expiryDate: new DateTime(2026, 12, 31),
        //         partyMapping: partyMapping),
        //     Guid.NewGuid().ToString());
        // Console.WriteLine($"Sözleşme: {demand.Id} · durum: {demand.Status}");
        // foreach (var party in demand.SigningUrls ?? new())
        // {
        //     Console.WriteLine($"  {party.FirstName} {party.LastName} → {party.SigningUrl}");
        // }

        // 5) Son sözleşmelerin durumu (yalnız sayımlar, kişisel veri içermez).
        var demands = await imzala.Demands.ListAsync(limit: 5, sort: "createdAt:desc");
        foreach (var d in demands.Demands ?? new())
        {
            Console.WriteLine($"{d.Id} · {d.Status} · {d.PartiesSigned}/{d.PartiesTotal} imza");
        }

        // 6) Webhook imzası. Gövde HAM olarak okunmalı: JSON'u ayrıştırıp yeniden
        //    serileştirmek baytları değiştirir ve imza tutmaz (ASP.NET'te
        //    Request.Body akışını bayt olarak okuyun, model bağlamadan önce doğrulayın).
        var secret = Ortam.Oku("IMZALA_WEBHOOK_SECRET");
        var signature = Ortam.Oku("IMZALA_WEBHOOK_SIGNATURE");
        if (secret != null && signature != null)
        {
            const string rawBody = "{\"event\":\"demand.completed\",\"demand_id\":\"3f2a1c88-4a1e-4c1b-9a3a-9d2f7c1e5b40\"}";
            Console.WriteLine($"Webhook imzası geçerli mi: {Imzala.VerifyWebhook(secret, rawBody, signature)}");
        }
        else
        {
            Console.WriteLine("Webhook doğrulaması atlandı: IMZALA_WEBHOOK_SECRET ve IMZALA_WEBHOOK_SIGNATURE tanımlı değil.");
        }
    }
}
