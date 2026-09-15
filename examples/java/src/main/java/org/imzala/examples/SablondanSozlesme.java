package org.imzala.examples;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.imzala.Imzala;
import org.imzala.ListDemandsParams;
import org.imzala.client.generated.model.CreateDemandRequest;
import org.imzala.client.generated.model.PartyMappingInput;

/**
 * Şablondan sözleşme oluşturma, davet linklerini alma ve webhook imzasını doğrulama.
 * Kapsam: templates:read + demands:read; sözleşme oluşturma için demands:write.
 * Sözleşme oluşturma kredi harcar (yorumda). Diğer adımlar kredi harcamaz.
 */
public final class SablondanSozlesme {

  public static void main(String[] args) {
    Imzala imzala = Ornek.istemci();

    // 1) Hesap ve kalan kredi. Bu uç kapsam istemez.
    var me = imzala.me();
    Object kredi = me.getCredits() != null ? me.getCredits().getRemaining() : "?";
    System.out.println("Hesap: " + me.getEmail() + " · kalan kredi: " + kredi);

    // 2) Şablonu seç: IMZALA_TEMPLATE_ID verilmişse o, yoksa listedeki ilk şablon.
    String templateIdOrtam = Ornek.ortam("IMZALA_TEMPLATE_ID");
    UUID templateId = templateIdOrtam != null ? UUID.fromString(templateIdOrtam) : null;
    if (templateId == null) {
      var templates = imzala.templates().list(null, 10);
      if (templates.getTemplates() != null && !templates.getTemplates().isEmpty()) {
        templateId = templates.getTemplates().get(0).getId();
      }
    }
    if (templateId == null) {
      System.out.println("Aktif şablon yok. Panelden bir şablon oluşturun.");
      return;
    }

    // 3) Şablonun rollerini ve değişkenlerini oku. party_mapping bu rollere göre
    //    kurulur: her rol için template_party_id zorunludur.
    var detail = imzala.templates().get(templateId);
    System.out.println("Şablon: " + detail.getName() + " · " + detail.getPagesCount() + " sayfa");
    if (detail.getParties() != null) {
      for (var party : detail.getParties()) {
        String optional = Boolean.TRUE.equals(party.getIsRequired()) ? "" : " · isteğe bağlı";
        String label = party.getLabel() != null ? party.getLabel() : "(adsız)";
        System.out.println("  Rol " + party.getOrder() + ": " + label + " · " + party.getId() + optional);
      }
    }
    if (detail.getVariables() != null) {
      for (var variable : detail.getVariables()) {
        System.out.println("  Değişken: " + variable.getSlug() + " (" + variable.getItemType() + ")");
      }
    }

    // 4) Sözleşmeyi oluştur. Kredi harcar; açmadan önce test ortamını kullanın.
    //    İkinci parametre Idempotency-Key: ağ koptuğunda aynı anahtarla tekrar denemek
    //    ikinci bir sözleşme yaratmaz; SDK bir 429 sonrasında anahtarlı isteği bir kez yineler.
    //    Kişiler kurgusaldır; her rol için template_party_id şablon detayından gelir.
    //
    // String[][] kisiler = {
    //   {"Ayşe", "Yılmaz", "ayse@example.com", "+905551112233"},
    //   {"Mehmet", "Yılmaz", "mehmet@example.com", "+905551112244"},
    // };
    // List<PartyMappingInput> partyMapping = new ArrayList<>();
    // for (int i = 0; i < detail.getParties().size(); i++) {
    //   String[] kisi = kisiler[i % kisiler.length];
    //   partyMapping.add(new PartyMappingInput()
    //       .templatePartyId(detail.getParties().get(i).getId())
    //       .firstName(kisi[0]).lastName(kisi[1]).email(kisi[2]).phone(kisi[3]));
    // }
    // var demand = imzala.demands().create(
    //     new CreateDemandRequest()
    //         .templateId(detail.getId())
    //         .title("Hizmet Sözleşmesi")
    //         .expiryDate(OffsetDateTime.of(2026, 12, 31, 23, 59, 0, 0, ZoneOffset.ofHours(3)))
    //         .partyMapping(partyMapping),
    //     UUID.randomUUID().toString());
    // System.out.println("Sözleşme: " + demand.getId() + " · durum: " + demand.getStatus());
    // if (demand.getSigningUrls() != null) {
    //   for (var party : demand.getSigningUrls()) {
    //     System.out.println("  " + party.getFirstName() + " " + party.getLastName() + " → " + party.getSigningUrl());
    //   }
    // }

    // 5) Son sözleşmelerin durumu (yalnız sayımlar, kişisel veri içermez).
    var demands = imzala.demands().list(new ListDemandsParams().limit(5).sort("createdAt:desc"));
    if (demands.getDemands() != null) {
      for (var d : demands.getDemands()) {
        System.out.println(d.getId() + " · " + d.getStatus() + " · " + d.getPartiesSigned() + "/" + d.getPartiesTotal() + " imza");
      }
    }

    // 6) Webhook imzası. Gövde HAM olarak okunmalı: JSON'u ayrıştırıp yeniden
    //    serileştirmek baytları değiştirir ve imza tutmaz (servlet'te istek
    //    gövdesini bayt olarak okuyun, ayrıştırmadan önce doğrulayın).
    String secret = Ornek.ortam("IMZALA_WEBHOOK_SECRET");
    String signature = Ornek.ortam("IMZALA_WEBHOOK_SIGNATURE");
    if (secret != null && signature != null) {
      String rawBody = "{\"event\":\"demand.completed\",\"demand_id\":\"3f2a1c88-4a1e-4c1b-9a3a-9d2f7c1e5b40\"}";
      System.out.println("Webhook imzası geçerli mi: " + Imzala.verifyWebhook(secret, rawBody, signature));
    } else {
      System.out.println("Webhook doğrulaması atlandı: IMZALA_WEBHOOK_SECRET ve IMZALA_WEBHOOK_SIGNATURE tanımlı değil.");
    }
  }
}
