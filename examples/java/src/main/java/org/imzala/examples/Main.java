package org.imzala.examples;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.imzala.Imzala;
import org.imzala.ImzalaException;
import org.imzala.ListDemandsParams;

/**
 * İmzala Java SDK, sözleşme yaşam döngüsü örneği.
 *
 * Çalıştırma (önce SDK'yı kur: packages/java içinde `mvn install`):
 *   IMZALA_API_KEY=imz_... IMZALA_BASE_URL=https://test-api.imzala.org \
 *     mvn -q compile exec:java
 *
 * Salt-okuma işlemleri doğrudan çalışır. Veri değiştiren işlemler (create /
 * cancel / resendParty / delete) yorum satırıdır: gerçek kredi harcar / durum
 * değiştirir.
 *
 * İmzala sözleşmeleri varsayılan olarak dijital imza (SES) üretir; SDK imza
 * geçerliliği hakkında hukuki iddiada bulunmaz.
 */
public final class Main {

  public static void main(String[] args) {
    String apiKey = System.getenv("IMZALA_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("IMZALA_API_KEY gerekli (imz_...). Panel, Geliştirici, API Anahtarlari.");
      System.exit(1);
    }
    String baseUrl = System.getenv("IMZALA_BASE_URL");

    // baseUrl bos ise SDK varsayilanini (api-prd) kullan.
    Imzala imzala = (baseUrl == null || baseUrl.isEmpty())
        ? new Imzala(apiKey)
        : new Imzala(apiKey, baseUrl);

    try {
      // 1) Kimlik + kredi bakiyesi
      System.out.println("\nKimlik: " + imzala.me());

      // 2) Sablonlar (bir sayfa)
      var templates = imzala.templates().list();
      System.out.println("\nSablonlar: " + templates.getTotal());

      // 3) Sozlesme listesi (counts-only, taraf PII'si yok)
      var list = imzala.demands().list(new ListDemandsParams().limit(5).sort("createdAt:desc"));
      System.out.println("\nSozlesme sayisi: " + list.getTotal());
      if (list.getDemands() != null) {
        for (var d : list.getDemands()) {
          System.out.println("   - " + d.getId() + "  [" + d.getStatus() + "]  "
              + d.getPartiesSigned() + "/" + d.getPartiesTotal() + "  " + d.getTitle());
        }
      }

      if (list.getDemands() == null || list.getDemands().isEmpty()) {
        System.out.println("\n(Sozlesme yok, create ornegini acin.)");
        return;
      }
      var first = list.getDemands().get(0);
      UUID firstId = UUID.fromString(first.getId());

      // 4) Sozlesme detay (imzaci adi kisaltilmis, e-posta maskeli, KVKK)
      var demand = imzala.demands().get(firstId);
      System.out.println("\nDetay: " + demand);

      // 5) Imza denetim izi (maskeli)
      var timeline = imzala.demands().getTimeline(firstId);
      System.out.println("\nDenetim izi: " + timeline);

      // 6) Tamamlanmis sozlesmenin imzali PDF'ini indir (binary, byte[])
      if ("COMPLETED".equals(String.valueOf(first.getStatus()))) {
        byte[] pdf = imzala.demands().getPdf(firstId);
        Path out = Path.of("demand-" + firstId + ".pdf");
        Files.write(out, pdf);
        System.out.println("\nImzali PDF kaydedildi: " + out + " (" + pdf.length + " bayt)");

        // Tamamlanma sertifikasi (PAdES B-T):
        // byte[] cert = imzala.demands().getCertificate(firstId, "tr");
      }

      // Veri degistiren islemler (bilerek yorumlu):
      // imzala.demands().cancel(firstId);                 // iptal
      // imzala.demands().resendParty(firstId, partyId);   // tekil davet tekrar
      // imzala.demands().delete(firstId);                 // tamamlanmamis sil
      // imzala.templates().update(templateId, patchBody); // sablon metadata
      // imzala.templates().delete(templateId);            // sablon sil

    } catch (ImzalaException e) {
      System.err.println("\nImzalaException: " + e.getMessage());
      System.exit(1);
    } catch (Exception e) {
      System.err.println("\nHata: " + e.getMessage());
      System.exit(1);
    }
  }

  private Main() {}
}
