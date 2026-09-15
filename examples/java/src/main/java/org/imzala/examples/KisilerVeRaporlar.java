package org.imzala.examples;

import org.imzala.Imzala;
import org.imzala.ImzalaException;
import org.imzala.ListContactsParams;
import org.imzala.client.generated.model.ApiV1ContactsPostRequest;

/**
 * Adres defterini sayfalı gezme, kişi oluşturma ve sözleşme durum sayımlarını okuma.
 * Kapsam: contacts:read + contacts:write (kişi oluşturma) + demands:read (rapor).
 * Kredi harcamaz. Kişi oluşturma veri değiştirir (yorumda).
 */
public final class KisilerVeRaporlar {

  public static void main(String[] args) {
    Imzala imzala = Ornek.istemci();

    // 1) Sayfalı gezinme: listAll sayfaları şeffaf biçimde dolaşır (limit 10..100).
    int sayac = 0;
    for (var kisi : imzala.contacts().listAll(new ListContactsParams().limit(50))) {
      sayac++;
      if (sayac <= 5) {
        System.out.println(kisi.getId() + " · " + kisi.getFirstName() + " " + kisi.getLastName());
      }
    }
    System.out.println("Toplam kişi: " + sayac);

    // 2) Arama: tek sayfa, metin filtresiyle.
    var arama = imzala.contacts().list(new ListContactsParams().q("ayse").limit(10));
    System.out.println("\"ayse\" için " + (arama.getTotal() != null ? arama.getTotal() : 0) + " sonuç");

    // 3) Kişi oluştur. Aynı e-posta veya telefona sahip aktif bir kişi varsa 409
    //    CONTACT_DUPLICATE döner. Bu uçta idempotency anahtarı yoktur; SDK isteği
    //    hiçbir koşulda yinelemez. Kişisel veriyi buraya yazan, o veri için veri
    //    sorumlusudur (aydınlatma yükümlülüğü size aittir).
    //
    // try {
    //   var yeni = imzala.contacts().create(new ApiV1ContactsPostRequest()
    //       .firstName("Ayşe")
    //       .lastName("Yılmaz")
    //       .email("ayse@example.com")
    //       .phone("+905551112233"));
    //   System.out.println("Kişi oluşturuldu: " + yeni.getId());
    // } catch (ImzalaException e) {
    //   if ("CONTACT_DUPLICATE".equals(e.getCode())) {
    //     System.out.println("Kişi zaten var: " + e.getCodeDescription());
    //   } else {
    //     throw e;
    //   }
    // }

    // 4) Rapor: sözleşme durumlarının toplu sayımı. Kişisel veri içermez.
    var rapor = imzala.reports().get();
    var c = rapor.getContracts();
    System.out.println("Sözleşmeler: toplam " + c.getTotal() + " · bekleyen " + c.getPending() + " · tamamlanan " + c.getCompleted());
    System.out.println("  iptal " + c.getCancelled() + " · süresi dolan " + c.getExpired() + " · bu ay oluşturulan " + c.getThisMonth());
  }
}
