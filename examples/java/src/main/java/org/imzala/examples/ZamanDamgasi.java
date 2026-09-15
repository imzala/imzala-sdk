package org.imzala.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.imzala.CreateTimestampParams;
import org.imzala.Imzala;
import org.imzala.ListTimestampsParams;

/**
 * Bir dosyanın belirli bir anda var olduğunu ve o andan beri değişmediğini kanıtlayan
 * zaman damgası hizmeti: damga al, damgaları listele, tek kaydı getir.
 * Kapsam: timestamps. Damga almak kredi harcar (yorumda); liste ve getirme harcamaz.
 *
 * <p>Damga dijital imza DEĞİLDİR: dosyayı kimin yazdığını ya da kimin sahiplendiğini
 * kanıtlamaz; yalnız var olma ve değişmezlik kanıtıdır. owner alanları bilgilendirme
 * amaçlıdır ve API tarafından doğrulanmaz.
 */
public final class ZamanDamgasi {

  public static void main(String[] args) throws IOException {
    Imzala imzala = Ornek.istemci();

    // 1) Damga al. Kredi harcar (yorumda). idempotencyKey, ağ koptuğunda ikinci kez
    //    kredi harcamayı önler: beş dakika içinde aynı anahtar aynı kaydı döndürür.
    String pdfOrtam = Ornek.ortam("IMZALA_PDF_PATH");
    Path pdfPath = Path.of(pdfOrtam != null ? pdfOrtam : "ornek-sozlesme.pdf");
    if (Files.isRegularFile(pdfPath)) {
      System.out.println("Damgalanacak dosya hazır: " + pdfPath + " (damga alma adımı yorumda).");
    }
    //
    // var kayit = imzala.timestamps().create(
    //     new CreateTimestampParams(Files.readAllBytes(pdfPath), "ornek-sozlesme.pdf")
    //         .contentType("application/pdf")
    //         .idempotencyKey(UUID.randomUUID().toString())
    //         .description("Taslak sürüm")
    //         .ownerFirstName("Ayşe")
    //         .ownerLastName("Yılmaz"));
    // System.out.println("Damga: " + kayit.getId() + " · " + kayit.getTimestampTime() + " · " + kayit.getTsaAuthority());
    // System.out.println("  SHA-256: " + kayit.getFileSha256() + " · harcanan kredi: " + kayit.getCreditsUsed());

    // 2) Damgaları listele (varsayılan sıralama yeni → eski).
    var liste = imzala.timestamps().list(new ListTimestampsParams().limit(10));
    System.out.println("Toplam damga: " + (liste.getTotal() != null ? liste.getTotal() : 0));
    if (liste.getTimestamps() != null) {
      for (var damga : liste.getTimestamps()) {
        System.out.println(damga.getId() + " · " + damga.getOriginalFileName() + " · " + damga.getTimestampDate() + " · " + damga.getStatus());
      }
    }

    // 3) Tek kaydın ayrıntısı: IMZALA_TIMESTAMP_ID verilmişse o, yoksa listedeki ilk kayıt.
    String ortamId = Ornek.ortam("IMZALA_TIMESTAMP_ID");
    UUID timestampId = ortamId != null ? UUID.fromString(ortamId) : null;
    if (timestampId == null && liste.getTimestamps() != null && !liste.getTimestamps().isEmpty()) {
      timestampId = liste.getTimestamps().get(0).getId();
    }
    if (timestampId == null) {
      System.out.println("Henüz damga kaydı yok.");
      return;
    }
    var detay = imzala.timestamps().get(timestampId);
    System.out.println("Kayıt " + detay.getId() + ": " + detay.getOriginalFileName() + " (" + detay.getOriginalFileSize() + " bayt)");
    System.out.println("  Açıklama: " + (detay.getDescription() != null ? detay.getDescription() : "-") + " · durum: " + detay.getStatus());
    System.out.println("  Damga dosyası: " + detay.getTimestampFileUrl());
  }
}
