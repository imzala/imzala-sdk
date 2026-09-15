package org.imzala.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.imzala.FileInput;
import org.imzala.Imzala;
import org.imzala.UploadDemandParams;
import org.imzala.UploadPartyInput;

/**
 * Kendi PDF'inizi Alan Şablonuyla kuru koşumda deneme, sonra sözleşme olarak yükleme.
 * Kapsam: templates:read + demands:write (kuru koşum da bu kapsamı ister).
 * Kuru koşum kredi harcamaz ve hiçbir şey oluşturmaz; yükleme kredi harcar (yorumda).
 */
public final class BelgeVeAlanSablonu {

  public static void main(String[] args) throws IOException {
    Imzala imzala = Ornek.istemci();

    // 1) Alan Şablonunu seç. Alan Şablonu, sözleşme şablonundan ayrıdır: belgedeki
    //    çapa metinlerine göre alanların nereye düşeceğini tarif eder.
    String ortamId = Ornek.ortam("IMZALA_FIELD_TEMPLATE_ID");
    UUID fieldTemplateId = ortamId != null ? UUID.fromString(ortamId) : null;
    if (fieldTemplateId == null) {
      var list = imzala.fieldTemplates().list(null, 20);
      if (list.getFieldTemplates() != null && !list.getFieldTemplates().isEmpty()) {
        fieldTemplateId = list.getFieldTemplates().get(0).getId();
      }
    }
    if (fieldTemplateId == null) {
      System.out.println("Tanımlı Alan Şablonu yok. Panelden oluşturabilirsiniz.");
      return;
    }

    // 2) Rolleri ve alan sayılarını oku. Yüklemede her rol için template_party_id
    //    bu listeden verilir.
    var detail = imzala.fieldTemplates().get(fieldTemplateId);
    System.out.println("Alan Şablonu: " + detail.getName() + " · toplam alan: " + detail.getTotalFieldCount());
    if (detail.getParties() != null) {
      for (var party : detail.getParties()) {
        String label = party.getLabel() != null ? party.getLabel() : "(adsız)";
        System.out.println("  Rol " + party.getOrder() + ": " + label + " · " + party.getFieldCount() + " alan · " + party.getId());
      }
    }

    // 3) KURU KOŞUM: yerleşimi belge üzerinde dene. Hiçbir şey oluşturmaz, kredi
    //    harcamaz, dosyayı saklamaz. Çözülemeyen çapaları önceden görmenin en ucuz
    //    yolu. Çözülemeyen belge de 200 döner: resolvable=false.
    String pdfOrtam = Ornek.ortam("IMZALA_PDF_PATH");
    Path pdfPath = Path.of(pdfOrtam != null ? pdfOrtam : "ornek-sozlesme.pdf");
    if (!Files.isRegularFile(pdfPath)) {
      System.out.println("PDF bulunamadı: " + pdfPath + ". IMZALA_PDF_PATH ile bir PDF verin.");
      return;
    }
    FileInput pdf = new FileInput(Files.readAllBytes(pdfPath), "ornek-sozlesme.pdf", "application/pdf");

    // Üçüncü parametre onAnchorMiss: "drop" yalnız şablonun ilgili alanları zaten drop
    // ise uygulanır; verilmezse block geçerlidir. İmza alanları hiçbir koşulda düşürülmez.
    var preview = imzala.fieldTemplates().previewLayout(detail.getId(), List.of(pdf));
    Object toplam = preview.getPlacementsSummary() != null ? preview.getPlacementsSummary().getTotal() : 0;
    System.out.println("Uygulanabilir: " + (Boolean.TRUE.equals(preview.getResolvable()) ? "evet" : "hayır")
        + " · sayfa: " + preview.getPageCount() + " · yerleşen alan: " + toplam);
    if (preview.getDiagnostics() != null) {
      for (var diagnostic : preview.getDiagnostics()) {
        System.out.println("  " + diagnostic.getSeverity() + " " + diagnostic.getCode() + ": " + diagnostic.getMessage());
      }
    }
    if (preview.getWarnings() != null) {
      for (var warning : preview.getWarnings()) {
        System.out.println("  Uyarı " + warning.getCode() + ": " + warning.getMessage());
      }
    }
    if (!Boolean.TRUE.equals(preview.getResolvable())) {
      System.out.println("Yerleşim çözülemedi. Belgedeki çapaları düzeltip yeniden deneyin; yükleme 422 ile reddedilir.");
    }

    // 4) Gerçek yükleme. Kredi harcar (yorumda). fieldTemplateId verildiğinde alanlar
    //    bu şablona göre yerleştirilir; her rol tam bir kez eşlenmelidir. Bu uçta
    //    davetler varsayılan olarak KAPALIDIR; sendInvitations("true") ile aynı
    //    istekte gönderilir. idempotencyKey ile tekrar deneme ikinci sözleşme yaratmaz.
    //
    // String[][] kisiler = {
    //   {"Ayşe", "Yılmaz", "ayse@example.com", "+905551112233"},
    //   {"Mehmet", "Yılmaz", "mehmet@example.com", "+905551112244"},
    // };
    // List<UploadPartyInput> parties = new ArrayList<>();
    // for (int i = 0; i < detail.getParties().size(); i++) {
    //   String[] kisi = kisiler[i % kisiler.length];
    //   parties.add(new UploadPartyInput(kisi[0], kisi[1], kisi[2], kisi[3], detail.getParties().get(i).getId()));
    // }
    // var demand = imzala.demands().uploadDocument(
    //     new UploadDemandParams(List.of(pdf), parties)
    //         .title("Hizmet Sözleşmesi")
    //         .fieldTemplateId(detail.getId())
    //         .idempotencyKey(UUID.randomUUID().toString())
    //         .sendInvitations("true"));
    // System.out.println("Sözleşme: " + demand.getId() + " · durum: " + demand.getStatus());
    // if (demand.getSigningUrls() != null) {
    //   for (var party : demand.getSigningUrls()) {
    //     System.out.println("  " + party.getFirstName() + " " + party.getLastName() + " → " + party.getSigningUrl());
    //   }
    // }
  }
}
