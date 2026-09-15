// Adres defterini sayfalı gezme, kişi oluşturma ve sözleşme durum sayımlarını okuma.
// Kapsam: contacts:read + contacts:write (kişi oluşturma) + demands:read (rapor).
// Kredi harcamaz. Kişi oluşturma veri değiştirir (yorumda).

using ImzalaApiClient.Model;
using ImzalaSdk;

namespace ImzalaOrnekleri;

internal sealed class KisilerVeRaporlar : ISenaryo
{
    public string Ad => "Kişiler ve raporlar";

    public async Task CalistirAsync(Imzala imzala)
    {
        // 1) Sayfalı gezinme: ListAllAsync sayfaları şeffaf biçimde dolaşır (limit 10..100).
        var sayac = 0;
        await foreach (var kisi in imzala.Contacts.ListAllAsync(limit: 50))
        {
            sayac++;
            if (sayac <= 5)
            {
                Console.WriteLine($"{kisi.Id} · {kisi.FirstName} {kisi.LastName}");
            }
        }
        Console.WriteLine($"Toplam kişi: {sayac}");

        // 2) Arama: tek sayfa, metin filtresiyle.
        var arama = await imzala.Contacts.ListAsync(q: "ayse", limit: 10);
        Console.WriteLine($"\"ayse\" için {arama.Total} sonuç");

        // 3) Kişi oluştur. Aynı e-posta veya telefona sahip aktif bir kişi varsa 409
        //    CONTACT_DUPLICATE döner. Bu uçta idempotency anahtarı yoktur; SDK isteği
        //    hiçbir koşulda yinelemez. Kişisel veriyi buraya yazan, o veri için veri
        //    sorumlusudur (aydınlatma yükümlülüğü size aittir).
        //
        // try
        // {
        //     var yeni = await imzala.Contacts.CreateAsync(new ApiV1ContactsPostRequest(
        //         firstName: "Ayşe",
        //         lastName: "Yılmaz",
        //         email: "ayse@example.com",
        //         phone: "+905551112233"));
        //     Console.WriteLine($"Kişi oluşturuldu: {yeni.Id}");
        // }
        // catch (ImzalaError err) when (err.Code == "CONTACT_DUPLICATE")
        // {
        //     Console.WriteLine($"Kişi zaten var: {err.CodeDescription}");
        // }

        // 4) Rapor: sözleşme durumlarının toplu sayımı. Kişisel veri içermez.
        var rapor = await imzala.Reports.GetAsync();
        var c = rapor.Contracts;
        Console.WriteLine($"Sözleşmeler: toplam {c.Total} · bekleyen {c.Pending} · tamamlanan {c.Completed}");
        Console.WriteLine($"  iptal {c.Cancelled} · süresi dolan {c.Expired} · bu ay oluşturulan {c.ThisMonth}");
    }
}
