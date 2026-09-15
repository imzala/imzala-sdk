package org.imzala.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.imzala.FileInput;
import org.imzala.Imzala;
import org.imzala.ImzalaException;
import org.imzala.ListDemandsParams;
import org.imzala.UploadEnvelopeDocumentParams;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsGet200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDocumentsPostRequest;
import org.imzala.client.generated.model.DemandStatus;
import org.imzala.client.generated.model.EnvelopeDocument;

/**
 * Çok belgeli zarf: KVKK aydınlatma + açık rıza + ön bilgilendirme ile sözleşme toplama akışı.
 * Kapsam: demands:write (taslak, belgeler, gönderim) + demands:read (liste, PDF).
 * Belge uçları kredi harcamaz; tahsilat oluşturma ve gönderim adımlarında yapılır. Yazma adımları yorumda.
 */
public final class Zarf {

  public static void main(String[] args) throws IOException {
    Imzala imzala = Ornek.istemci();

    // 1) Davet göndermeden taslak sözleşme oluştur. Davetlerin oluşturma anında
    //    ÇIKMAMASI için gövdede dispatch_notifications: false gönderilir; her şey hazır
    //    olunca 6. adımdaki dispatch ile tek seferde gönderilir. Bu alan dispatch ucunun
    //    belgelerinde anlatılır; bu sürümün tipli CreateDemandRequest modeli alanı henüz
    //    taşımadığından taslağı panelden ya da doğrudan HTTP ile oluşturup kimliğini
    //    IMZALA_DEMAND_ID ile verin. Aşağıdaki adımlar o taslak üzerinde aynen çalışır.
    //    Kişiler kurgusaldır.
    //
    // UUID zarfId = UUID.fromString(Ornek.ortam("IMZALA_DEMAND_ID"));
    // UUID partyId = imzala.demands().get(zarfId).getParties().get(0).getPartyId();

    // 2) Ana sözleşmeyi dosya olarak yükle (doc_kind CONTRACT). Belge başına TEK dosya,
    //    her belge için ayrı çağrı. idempotencyKey ZORUNLUDUR ve gövde alanı olarak
    //    gider: aynı anahtarla tekrar yükleme yeni belge yaratmaz, önceki belgeyi döndürür.
    //
    // var sozlesme = imzala.demands().documents().upload(zarfId,
    //     new UploadEnvelopeDocumentParams(
    //         new FileInput(Files.readAllBytes(Path.of("sozlesme.pdf")), "sozlesme.pdf", "application/pdf"),
    //         "Üyelik Sözleşmesi",
    //         UUID.randomUUID().toString())
    //         .docKind("CONTRACT"));
    // System.out.println("Belge: " + sozlesme.getDocument().getId() + " · " + sozlesme.getDocument().getTitle());

    // 3) KVKK belgelerini ekle (dosyasız, yalnız metadata).
    //    KVKK_CONSENT için is_required FALSE olmak zorundadır: açık rıza reddedilebilir
    //    olmalıdır (aksi halde 400 CONSENT_CANNOT_BE_REQUIRED).
    //    PREINFO için is_required TRUE olmak zorundadır (aksi halde 400 PREINFO_MUST_BE_REQUIRED).
    //
    // var aydinlatma = imzala.demands().documents().create(zarfId, new ApiV1DemandsDemandIdDocumentsPostRequest()
    //     .title("KVKK Aydınlatma Metni")
    //     .docKind(ApiV1DemandsDemandIdDocumentsPostRequest.DocKindEnum.KVKK_NOTICE)
    //     .isRequired(true).signatureRequired(false));
    // var riza = imzala.demands().documents().create(zarfId, new ApiV1DemandsDemandIdDocumentsPostRequest()
    //     .title("KVKK Açık Rıza Beyanı")
    //     .docKind(ApiV1DemandsDemandIdDocumentsPostRequest.DocKindEnum.KVKK_CONSENT)
    //     .isRequired(false));
    // var onBilgi = imzala.demands().documents().create(zarfId, new ApiV1DemandsDemandIdDocumentsPostRequest()
    //     .title("Ön Bilgilendirme Formu")
    //     .docKind(ApiV1DemandsDemandIdDocumentsPostRequest.DocKindEnum.PREINFO)
    //     .isRequired(true));

    // 4) Hangi belgeyi kim görecek: belge başına taraf kümesi (küme olduğu gibi değişir).
    //
    // for (var belge : List.of(sozlesme, aydinlatma, riza, onBilgi)) {
    //   imzala.demands().documents().setAssignments(zarfId, belge.getDocument().getId(), List.of(partyId));
    // }

    // 5) Sıralama: aydınlatma ve ön bilgilendirme önce, sözleşme sonra, rıza en sonda.
    //    Liste zarfın TÜM belgelerini içermelidir (ORDER_SET_MISMATCH).
    //
    // imzala.demands().documents().reorder(zarfId, List.of(
    //     aydinlatma.getDocument().getId(), onBilgi.getDocument().getId(),
    //     sozlesme.getDocument().getId(), riza.getDocument().getId()));

    // 6) Gönder. Davetler burada çıkar; kredi mutabakatı burada yapılır (aynı sözleşme
    //    için ikinci kez düşmez). Bu uçta idempotency anahtarı yoktur; SDK hiçbir
    //    koşulda yinelemez. Zaten gönderilmiş bir sözleşmeye tekrar çağrı kredi
    //    düşürmez (dispatched: false) ama davetleri yeniden yollar.
    //
    // var gonderim = imzala.demands().dispatch(zarfId, true);
    // Object dusen = gonderim.getCredits() != null ? gonderim.getCredits().getCharged() : "?";
    // System.out.println("Gönderildi: " + gonderim.getDispatched() + " · durum: " + gonderim.getStatus() + " · düşen kredi: " + dusen);

    // 7) Belgeleri listele (salt okuma). Çok belgeli zarf hesabınız için henüz açık
    //    değilse tüm belge uçları 409 ENVELOPE_MULTI_DOC_DISABLED döner.
    String ortamId = Ornek.ortam("IMZALA_DEMAND_ID");
    UUID demandId = ortamId != null ? UUID.fromString(ortamId) : null;
    if (demandId == null) {
      var demands = imzala.demands().list(new ListDemandsParams().limit(1).sort("createdAt:desc"));
      if (demands.getDemands() != null && !demands.getDemands().isEmpty()) {
        demandId = demands.getDemands().get(0).getId();
      }
    }
    if (demandId == null) {
      System.out.println("Sözleşme yok. IMZALA_DEMAND_ID verin ya da önce bir sözleşme oluşturun.");
      return;
    }

    ApiV1DemandsDemandIdDocumentsGet200ResponseData liste;
    try {
      liste = imzala.demands().documents().list(demandId, "wizard");
    } catch (ImzalaException e) {
      if ("ENVELOPE_MULTI_DOC_DISABLED".equals(e.getCode())) {
        System.out.println("Çok belgeli zarf bu hesap için kapalı: " + e.getCodeDescription());
        return;
      }
      throw e;
    }
    var documents = liste.getDocuments() != null ? liste.getDocuments() : List.<EnvelopeDocument>of();
    System.out.println("Sözleşme " + demandId + ": " + documents.size() + " belge");
    for (var belge : documents) {
      int taraf = belge.getAssignedPartyIds() != null ? belge.getAssignedPartyIds().size() : 0;
      System.out.println("  " + belge.getOrder() + ". " + belge.getTitle() + " · " + belge.getDocKind()
          + " · zorunlu: " + belge.getIsRequired() + " · imza: " + belge.getSignatureRequired() + " · taraf: " + taraf);
    }

    // 8) Tamamlanınca belge-özgü imzalı PDF (tüm zarfın birleşik PDF'i için getPdf).
    var demand = imzala.demands().get(demandId);
    if (demand.getStatus() == DemandStatus.StatusEnum.COMPLETED && !documents.isEmpty()) {
      var ilkBelge = documents.get(0);
      byte[] pdf = imzala.demands().getDocumentPdf(demandId, ilkBelge.getId());
      Path dosya = Path.of("belge-" + ilkBelge.getId() + ".pdf");
      Files.write(dosya, pdf);
      System.out.println("İmzalı belge PDF'i kaydedildi: " + dosya + " (" + pdf.length + " bayt)");
    } else {
      System.out.println("Sözleşme durumu " + demand.getStatus() + "; belge PDF'i yalnız COMPLETED durumunda indirilir.");
    }
  }
}
