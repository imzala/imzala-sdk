// Toplu sözleşme oluşturma (10'arlı parçalar), Idempotency-Key ve istek limiti (429) davranışı.
// Kapsam: templates:read + demands:write. Oluşturma adımları kredi harcar (yorumda).
// Limit gösterimi yalnız salt-okuma isteği (şablon listesi) kullanır, kredi harcamaz.

using ImzalaApiClient.Model;
using ImzalaSdk;

namespace ImzalaOrnekleri;

internal sealed class TopluVeTekrarKoruma : ISenaryo
{
    public string Ad => "Toplu ve tekrar koruma";

    // GET isteklerinin 429 sonrası otomatik yinelenmesi kapatıldı (maxRetries: 0):
    // 4. adımda 429'u kendimiz görmek istiyoruz. Üretimde varsayılanı (2) bırakın.
    public bool YinelemeKapali => true;

    public async Task CalistirAsync(Imzala imzala)
    {
        // 1) Şablon: IMZALA_TEMPLATE_ID verilmişse o, yoksa listedeki ilk şablon.
        var ortamId = Ortam.Oku("IMZALA_TEMPLATE_ID");
        Guid? templateId = ortamId != null ? Guid.Parse(ortamId) : null;
        if (templateId == null)
        {
            var templates = await imzala.Templates.ListAsync(limit: 1);
            templateId = templates.Templates?.FirstOrDefault()?.Id;
        }
        if (templateId == null)
        {
            Console.WriteLine("Aktif şablon yok. Panelden bir şablon oluşturun.");
            return;
        }
        var detail = await imzala.Templates.GetAsync(templateId.Value);
        var rolId = detail.Parties?.FirstOrDefault()?.Id ?? Guid.Empty;
        Console.WriteLine($"Şablon: {detail.Name} · ilk rol: {rolId}");

        // 2) Tek sözleşmede tekrar koruması. CreateAsync bir Idempotency-Key kabul eder:
        //    aynı anahtar + aynı gövde → aynı sözleşme döner, ikinci kayıt oluşmaz.
        //    Aynı anahtar + farklı gövde → 409 IDEMPOTENCY_KEY_REUSED.
        //    Anahtarlı bir istek 429 alırsa SDK Retry-After kadar bekleyip bir kez yineler;
        //    anahtarsız istek hiç yinelenmez (yineleme ikinci sözleşme yaratırdı).
        //
        // var anahtar = Guid.NewGuid().ToString(); // ör. kendi sipariş numaranız da olabilir
        // var demand = await imzala.Demands.CreateAsync(
        //     new CreateDemandRequest(
        //         templateId: detail.Id,
        //         partyMapping: new List<PartyMappingInput>
        //         {
        //             new(templatePartyId: rolId, firstName: "Ayşe", lastName: "Yılmaz", email: "ayse@example.com"),
        //         }),
        //     anahtar);
        // Console.WriteLine($"Sözleşme: {demand.Id}");

        // 3) Toplu oluşturma: tek istekte en fazla 10 satır (BULK_MAX_10). Daha uzun
        //    listeyi 10'arlı parçalara bölün. Her satır bağımsız bir sözleşmedir; kısmi
        //    başarı 200 ile döner, satır durumunu Results[i].Status'tan okuyun.
        //    Bu uçta Idempotency-Key YOKTUR; SDK toplu isteği 429 dahil hiçbir koşulda
        //    yinelemez, çünkü sunucu anahtarı tanımadığından yineleme aynı sözleşmeleri
        //    ikinci kez yaratır ve krediyi iki kez düşerdi. Parça başarısız olursa
        //    Demands.ListAsync ile durumu doğrulayıp yalnız eksik satırları yeniden gönderin.
        //
        // var alicilar = new List<ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner>
        // {
        //     new(templatePartyId: rolId, firstName: "Ayşe", lastName: "Yılmaz", email: "ayse@example.com"),
        //     new(templatePartyId: rolId, firstName: "Mehmet", lastName: "Yılmaz", email: "mehmet@example.com"),
        // };
        // foreach (var (parca, i) in alicilar.Chunk(10).Select((parca, i) => (parca, i)))
        // {
        //     var sonuc = await imzala.Demands.CreateBulkAsync(new ApiV1DemandsBulkPostRequest(
        //         templateId: detail.Id,
        //         options: new ApiV1DemandsBulkPostRequestOptions(dispatchNotifications: false), // önce taslak, sonra gönder
        //         rows: parca.Select(alici => new ApiV1DemandsBulkPostRequestRowsInner(partyMapping: new() { alici })).ToList()));
        //     Console.WriteLine($"Parça {i + 1}: oluşan {sonuc.Created}, başarısız {sonuc.Failed}");
        //     foreach (var satir in sonuc.Results ?? new())
        //     {
        //         Console.WriteLine($"  satır {satir.RowIndex}: {satir.Status} {(satir.DemandId != Guid.Empty ? satir.DemandId.ToString() : satir.Error ?? "")}");
        //     }
        // }

        // 4) İstek limiti. Anahtar başına dakikada 60 istek (anahtar bazında düşürülebilir).
        //    Aşımda 429 + RATE_LIMIT_EXCEEDED döner.
        //
        //    NOT: limiti ardışık isteklerle göremezsiniz: her istek yaklaşık bir saniye
        //    sürdüğü için 65 ardışık çağrının hepsi 200 döner (60 saniyelik pencere
        //    aradan sıfırlanır). Limit ancak eşzamanlı bir patlamayla görünür.
        try
        {
            await Task.WhenAll(Enumerable.Range(0, 70).Select(_ => imzala.Templates.ListAsync(limit: 1)));
            Console.WriteLine("Limit tetiklenmedi.");
        }
        catch (ImzalaRateLimitError err)
        {
            Console.WriteLine($"Limit aşıldı: {ErrorCodes.Describe(err.Code) ?? err.Code}");
            Console.WriteLine($"  {err.RetryAfter} saniye sonra tekrar deneyin.");
            var info = err.RateLimit;
            if (info != null)
            {
                Console.WriteLine($"  Politika: {info.Policy} · limit: {info.Limit} · kalan: {info.Remaining}");
            }
        }
    }
}
