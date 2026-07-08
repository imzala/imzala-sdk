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
 * uploadDocument / cancel / resendParty / delete / update / timestamps.create)
 * yorum satırıdır: gerçek kredi harcar ya da durum değiştirir.
 *
 * İmzala sözleşmeleri varsayılan olarak dijital imza (SES) üretir; SDK imza
 * geçerliliği hakkında hukuki bir iddiada bulunmaz.
 */
public final class Main {

  public static void main(String[] args) {
    String apiKey = System.getenv("IMZALA_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("IMZALA_API_KEY gerekli (imz_...). Panel, Gelistirici, API Anahtarlari.");
      System.exit(1);
    }
    String baseUrl = System.getenv("IMZALA_BASE_URL");

    // baseUrl bos ise SDK varsayilanini (api-prd) kullan.
    Imzala imzala = (baseUrl == null || baseUrl.isEmpty())
        ? new Imzala(apiKey)
        : new Imzala(apiKey, baseUrl);

    try {
      // 1) Kimlik + kredi bakiyesi
      var me = imzala.me();
      System.out.println("\nKimlik: " + me.getEmail()
          + "  |  workspace: " + (me.getWorkspace() != null ? me.getWorkspace().getType() : "-")
          + "  |  kalan kredi: " + (me.getCredits() != null ? me.getCredits().getRemaining() : "-"));

      // 2) Sablonlar (tek sayfa)
      var templates = imzala.templates().list();
      System.out.println("\nSablon sayisi: " + templates.getTotal());
      if (templates.getTemplates() != null) {
        for (var t : templates.getTemplates()) {
          System.out.println("   - " + t.getId() + "  " + t.getName());
        }
      }

      // 2b) Tum sablonlari sayfa sayfa gezen iterator (salt-okuma):
      //   for (var t : imzala.templates().listAll()) { ... }
      // 2c) Bir sablonun API kullanim kilavuzu (ornek curl + JSON):
      //   var usage = imzala.templates().usage(templateId);

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
        System.out.println("\n(Sozlesme yok, asagidaki create ornegini acin.)");
        return;
      }
      var first = list.getDemands().get(0);
      UUID firstId = first.getId(); // getId() zaten UUID doner

      // 4) Sozlesme detay (imzaci adi kisaltilmis, e-posta maskeli, KVKK)
      var demand = imzala.demands().get(firstId);
      System.out.println("\nDetay: " + firstId + "  [" + demand.getStatus() + "]");
      if (demand.getParties() != null) {
        for (var p : demand.getParties()) {
          System.out.println("   - " + p.getName() + "  " + p.getEmailMasked()
              + "  imzaladi=" + p.getSigned());
        }
      }

      // 5) Imza denetim izi (maskeli olaylar)
      var timeline = imzala.demands().getTimeline(firstId);
      System.out.println("\nDenetim izi olay sayisi: "
          + (timeline.getEvents() != null ? timeline.getEvents().size() : 0));

      // 6) Tamamlanmis sozlesmenin imzali PDF'ini indir (binary, byte[])
      if ("COMPLETED".equals(first.getStatus())) {
        byte[] pdf = imzala.demands().getPdf(firstId);
        Path out = Path.of("demand-" + firstId + ".pdf");
        Files.write(out, pdf);
        System.out.println("\nImzali PDF kaydedildi: " + out + " (" + pdf.length + " bayt)");

        // Tamamlanma sertifikasi (PAdES B-T), tr/en:
        //   byte[] cert = imzala.demands().getCertificate(firstId, "tr");
        //   Files.write(Path.of("sertifika-" + firstId + ".pdf"), cert);
      }

      // ---- Veri degistiren islemler (bilerek yorumlu) ------------------------
      // Sablondan sozlesme olustur (imza daveti otomatik gider):
      //   var body = new org.imzala.client.generated.model.CreateDemandRequest()
      //       .templateId(templateId)
      //       .partyMapping(java.util.List.of(
      //           new org.imzala.client.generated.model.PartyMappingInput()
      //               .templatePartyId(templatePartyId)
      //               .firstName("Ahmet").lastName("Yilmaz").email("ahmet@example.com")));
      //   var created = imzala.demands().create(body);
      //   System.out.println(created.getSigningUrls());
      //
      // Sablonsuz, dosya yukleyerek sozlesme:
      //   var params = new org.imzala.UploadDemandParams(
      //       java.util.List.of(new org.imzala.FileInput(pdfBytes, "sozlesme.pdf", "application/pdf")),
      //       java.util.List.of(new org.imzala.UploadPartyInput("Ada", "Lovelace", "ada@example.com", null)));
      //   imzala.demands().uploadDocument(params);
      //
      // Hatirlatma / iptal / tekil davet tekrar / silme:
      //   imzala.demands().sendReminder(firstId);
      //   imzala.demands().cancel(firstId);
      //   imzala.demands().resendParty(firstId, partyId);
      //   imzala.demands().delete(firstId);       // yalnizca tamamlanmamis
      //
      // Sablon metadata guncelle / sil:
      //   imzala.templates().update(templateId,
      //       new org.imzala.client.generated.model.ApiV1TemplatesIdPatchRequest().name("Yeni Ad"));
      //   imzala.templates().delete(templateId);
      //
      // Gomulu imza oturumu (tarayici iframe icin embed_url):
      //   var session = imzala.embed().createSession(firstId, partyId);
      //
      // Zaman damgasi (RFC 3161, var-olma + degismezlik kaniti):
      //   var ts = imzala.timestamps().create(
      //       new org.imzala.CreateTimestampParams(fileBytes, "belge.pdf")
      //           .idempotencyKey(UUID.randomUUID().toString()));

    } catch (ImzalaException e) {
      System.err.println("\nImzalaException: " + e.getStatusCode() + " " + e.getMessage());
      System.exit(1);
    } catch (Exception e) {
      System.err.println("\nHata: " + e.getMessage());
      System.exit(1);
    }
  }

  private Main() {}
}
