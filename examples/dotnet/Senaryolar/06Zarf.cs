// Çok belgeli zarf: KVKK aydınlatma + açık rıza + ön bilgilendirme ile sözleşme toplama akışı.
// Kapsam: demands:write (taslak, belgeler, gönderim) + demands:read (liste, PDF).
// Belge uçları kredi harcamaz; tahsilat oluşturma ve gönderim adımlarında yapılır. Yazma adımları yorumda.

using ImzalaApiClient.Model;
using ImzalaSdk;

namespace ImzalaOrnekleri;

internal sealed class Zarf : ISenaryo
{
    public string Ad => "Zarf";

    public async Task CalistirAsync(Imzala imzala)
    {
        // 1) Davet göndermeden taslak sözleşme oluştur. dispatchNotifications: false ile
        //    davetler oluşturma anında ÇIKMAZ; her şey hazır olunca 6. adımdaki dispatch
        //    ile tek seferde gönderilir. Kişiler kurgusaldır; rol id'si şablon detayından
        //    (Templates.GetAsync) gelir. Taslağı panelden oluşturduysanız bu adımı atlayıp
        //    kimliğini IMZALA_DEMAND_ID ile verebilirsiniz.
        //
        // var taslak = await imzala.Demands.CreateAsync(
        //     new CreateDemandRequest(
        //         templateId: Guid.Parse(Ortam.Oku("IMZALA_TEMPLATE_ID")!),
        //         title: "Üyelik sözleşmesi ve KVKK belgeleri",
        //         dispatchNotifications: false,
        //         partyMapping: new List<PartyMappingInput>
        //         {
        //             new(templatePartyId: Guid.Parse("<şablon detayından rol id>"), firstName: "Ayşe", lastName: "Yılmaz", email: "ayse@example.com", phone: "+905551112233"),
        //         }),
        //     Guid.NewGuid().ToString());
        // var zarfId = taslak.Id;
        // var partyId = taslak.SigningUrls[0].PartyId;

        // 2) Ana sözleşmeyi dosya olarak yükle (doc_kind CONTRACT). Belge başına TEK dosya,
        //    her belge için ayrı çağrı. IdempotencyKey ZORUNLUDUR ve gövde alanı olarak
        //    gider: aynı anahtarla tekrar yükleme yeni belge yaratmaz, önceki belgeyi döndürür.
        //
        // var sozlesme = await imzala.Demands.Documents.UploadAsync(zarfId, new UploadEnvelopeDocumentParams
        // {
        //     File = new FileInput { Content = await File.ReadAllBytesAsync("sozlesme.pdf"), FileName = "sozlesme.pdf", ContentType = "application/pdf" },
        //     Title = "Üyelik Sözleşmesi",
        //     DocKind = "CONTRACT",
        //     IdempotencyKey = Guid.NewGuid().ToString(),
        // });
        // Console.WriteLine($"Belge: {sozlesme.Document.Id} · {sozlesme.Document.Title}");

        // 3) KVKK belgelerini ekle (dosyasız, yalnız metadata).
        //    KVKK_CONSENT için is_required FALSE olmak zorundadır: açık rıza reddedilebilir
        //    olmalıdır (aksi halde 400 CONSENT_CANNOT_BE_REQUIRED).
        //    PREINFO için is_required TRUE olmak zorundadır (aksi halde 400 PREINFO_MUST_BE_REQUIRED).
        //
        // var aydinlatma = await imzala.Demands.Documents.CreateAsync(zarfId, new ApiV1DemandsDemandIdDocumentsPostRequest(
        //     title: "KVKK Aydınlatma Metni", docKind: ApiV1DemandsDemandIdDocumentsPostRequest.DocKindEnum.KVKKNOTICE, isRequired: true, signatureRequired: false));
        // var riza = await imzala.Demands.Documents.CreateAsync(zarfId, new ApiV1DemandsDemandIdDocumentsPostRequest(
        //     title: "KVKK Açık Rıza Beyanı", docKind: ApiV1DemandsDemandIdDocumentsPostRequest.DocKindEnum.KVKKCONSENT, isRequired: false));
        // var onBilgi = await imzala.Demands.Documents.CreateAsync(zarfId, new ApiV1DemandsDemandIdDocumentsPostRequest(
        //     title: "Ön Bilgilendirme Formu", docKind: ApiV1DemandsDemandIdDocumentsPostRequest.DocKindEnum.PREINFO, isRequired: true));

        // 4) Hangi belgeyi kim görecek: belge başına taraf kümesi (küme olduğu gibi değişir).
        //
        // foreach (var belge in new[] { sozlesme, aydinlatma, riza, onBilgi })
        // {
        //     await imzala.Demands.Documents.SetAssignmentsAsync(zarfId, belge.Document.Id, new[] { partyId });
        // }

        // 5) Sıralama: aydınlatma ve ön bilgilendirme önce, sözleşme sonra, rıza en sonda.
        //    Liste zarfın TÜM belgelerini içermelidir (ORDER_SET_MISMATCH).
        //
        // await imzala.Demands.Documents.ReorderAsync(zarfId, new[]
        // {
        //     aydinlatma.Document.Id, onBilgi.Document.Id, sozlesme.Document.Id, riza.Document.Id,
        // });

        // 6) Gönder. Davetler burada çıkar; kredi mutabakatı burada yapılır (aynı sözleşme
        //    için ikinci kez düşmez). Bu uçta idempotency anahtarı yoktur; SDK hiçbir
        //    koşulda yinelemez. Zaten gönderilmiş bir sözleşmeye tekrar çağrı kredi
        //    düşürmez (Dispatched: false) ama davetleri yeniden yollar.
        //
        // var gonderim = await imzala.Demands.DispatchAsync(zarfId, true);
        // var dusen = gonderim.Credits != null ? gonderim.Credits.Charged.ToString() : "?";
        // Console.WriteLine($"Gönderildi: {gonderim.Dispatched} · durum: {gonderim.Status} · düşen kredi: {dusen}");

        // 7) Belgeleri listele (salt okuma). Çok belgeli zarf hesabınız için henüz açık
        //    değilse tüm belge uçları 409 ENVELOPE_MULTI_DOC_DISABLED döner.
        var ortamId = Ortam.Oku("IMZALA_DEMAND_ID");
        Guid? demandId = ortamId != null ? Guid.Parse(ortamId) : null;
        if (demandId == null)
        {
            var demands = await imzala.Demands.ListAsync(limit: 1, sort: "createdAt:desc");
            demandId = demands.Demands?.FirstOrDefault()?.Id;
        }
        if (demandId == null)
        {
            Console.WriteLine("Sözleşme yok. IMZALA_DEMAND_ID verin ya da önce bir sözleşme oluşturun.");
            return;
        }

        ApiV1DemandsDemandIdDocumentsGet200ResponseData liste;
        try
        {
            liste = await imzala.Demands.Documents.ListAsync(demandId.Value, "wizard");
        }
        catch (ImzalaError err) when (err.Code == "ENVELOPE_MULTI_DOC_DISABLED")
        {
            Console.WriteLine($"Çok belgeli zarf bu hesap için kapalı: {err.CodeDescription}");
            return;
        }
        var documents = liste.Documents ?? new();
        Console.WriteLine($"Sözleşme {demandId}: {documents.Count} belge");
        foreach (var belge in documents)
        {
            var taraf = belge.AssignedPartyIds?.Count ?? 0;
            Console.WriteLine($"  {belge.Order}. {belge.Title} · {belge.DocKind} · zorunlu: {belge.IsRequired} · imza: {belge.SignatureRequired} · taraf: {taraf}");
        }

        // 8) Tamamlanınca belge-özgü imzalı PDF (tüm zarfın birleşik PDF'i için GetPdfAsync).
        var demand = await imzala.Demands.GetAsync(demandId.Value);
        if (demand.Status == DemandStatus.StatusEnum.COMPLETED && documents.Count > 0)
        {
            var ilkBelge = documents[0];
            var pdf = await imzala.Demands.GetDocumentPdfAsync(demandId.Value, ilkBelge.Id);
            var dosya = $"belge-{ilkBelge.Id}.pdf";
            await File.WriteAllBytesAsync(dosya, pdf);
            Console.WriteLine($"İmzalı belge PDF'i kaydedildi: {dosya} ({pdf.Length} bayt)");
        }
        else
        {
            Console.WriteLine($"Sözleşme durumu {demand.Status}; belge PDF'i yalnız COMPLETED durumunda indirilir.");
        }
    }
}
