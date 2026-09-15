package org.imzala.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.imzala.ErrorCodes;
import org.imzala.Imzala;
import org.imzala.ImzalaRateLimitException;
import org.imzala.client.generated.model.ApiV1DemandsBulkPostRequest;
import org.imzala.client.generated.model.ApiV1DemandsBulkPostRequestOptions;
import org.imzala.client.generated.model.ApiV1DemandsBulkPostRequestRowsInner;
import org.imzala.client.generated.model.ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner;
import org.imzala.client.generated.model.CreateDemandRequest;
import org.imzala.client.generated.model.PartyMappingInput;

/**
 * Toplu sözleşme oluşturma (10'arlı parçalar), Idempotency-Key ve istek limiti (429) davranışı.
 * Kapsam: templates:read + demands:write. Oluşturma adımları kredi harcar (yorumda).
 * Limit gösterimi yalnız salt-okuma isteği (şablon listesi) kullanır, kredi harcamaz.
 */
public final class TopluVeTekrarKoruma {

  public static void main(String[] args) throws InterruptedException {
    // GET isteklerinin 429 sonrası otomatik yinelenmesi kapatıldı (maxRetries 0):
    // 4. adımda 429'u kendimiz görmek istiyoruz. Üretimde varsayılanı (2) bırakın.
    Imzala imzala = Ornek.yinelemesizIstemci();

    // 1) Şablon: IMZALA_TEMPLATE_ID verilmişse o, yoksa listedeki ilk şablon.
    String templateIdOrtam = Ornek.ortam("IMZALA_TEMPLATE_ID");
    UUID templateId = templateIdOrtam != null ? UUID.fromString(templateIdOrtam) : null;
    if (templateId == null) {
      var templates = imzala.templates().list(null, 1);
      if (templates.getTemplates() != null && !templates.getTemplates().isEmpty()) {
        templateId = templates.getTemplates().get(0).getId();
      }
    }
    if (templateId == null) {
      System.out.println("Aktif şablon yok. Panelden bir şablon oluşturun.");
      return;
    }
    var detail = imzala.templates().get(templateId);
    UUID rolId = detail.getParties() != null && !detail.getParties().isEmpty() ? detail.getParties().get(0).getId() : null;
    System.out.println("Şablon: " + detail.getName() + " · ilk rol: " + rolId);

    // 2) Tek sözleşmede tekrar koruması. create bir Idempotency-Key kabul eder:
    //    aynı anahtar + aynı gövde → aynı sözleşme döner, ikinci kayıt oluşmaz.
    //    Aynı anahtar + farklı gövde → 409 IDEMPOTENCY_KEY_REUSED.
    //    Anahtarlı bir istek 429 alırsa SDK Retry-After kadar bekleyip bir kez yineler;
    //    anahtarsız istek hiç yinelenmez (yineleme ikinci sözleşme yaratırdı).
    //
    // String anahtar = UUID.randomUUID().toString(); // ör. kendi sipariş numaranız da olabilir
    // var demand = imzala.demands().create(
    //     new CreateDemandRequest()
    //         .templateId(detail.getId())
    //         .partyMapping(List.of(new PartyMappingInput()
    //             .templatePartyId(rolId).firstName("Ayşe").lastName("Yılmaz").email("ayse@example.com"))),
    //     anahtar);
    // System.out.println("Sözleşme: " + demand.getId());

    // 3) Toplu oluşturma: tek istekte en fazla 10 satır (BULK_MAX_10). Daha uzun
    //    listeyi 10'arlı parçalara bölün. Her satır bağımsız bir sözleşmedir; kısmi
    //    başarı 200 ile döner, satır durumunu results[i].status'tan okuyun.
    //    Bu uçta Idempotency-Key YOKTUR; SDK toplu isteği 429 dahil hiçbir koşulda
    //    yinelemez, çünkü sunucu anahtarı tanımadığından yineleme aynı sözleşmeleri
    //    ikinci kez yaratır ve krediyi iki kez düşerdi. Parça başarısız olursa
    //    demands().list() ile durumu doğrulayıp yalnız eksik satırları yeniden gönderin.
    //
    // List<ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner> alicilar = List.of(
    //     new ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner()
    //         .templatePartyId(rolId).firstName("Ayşe").lastName("Yılmaz").email("ayse@example.com"),
    //     new ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner()
    //         .templatePartyId(rolId).firstName("Mehmet").lastName("Yılmaz").email("mehmet@example.com"));
    // for (int i = 0; i < alicilar.size(); i += 10) {
    //   List<ApiV1DemandsBulkPostRequestRowsInner> rows = new ArrayList<>();
    //   for (var alici : alicilar.subList(i, Math.min(i + 10, alicilar.size()))) {
    //     rows.add(new ApiV1DemandsBulkPostRequestRowsInner().partyMapping(List.of(alici)));
    //   }
    //   var sonuc = imzala.demands().createBulk(new ApiV1DemandsBulkPostRequest()
    //       .templateId(detail.getId())
    //       .options(new ApiV1DemandsBulkPostRequestOptions().dispatchNotifications(false)) // önce taslak, sonra gönder
    //       .rows(rows));
    //   System.out.println("Parça " + (i / 10 + 1) + ": oluşan " + sonuc.getCreated() + ", başarısız " + sonuc.getFailed());
    //   if (sonuc.getResults() != null) {
    //     for (var satir : sonuc.getResults()) {
    //       Object ref = satir.getDemandId() != null ? satir.getDemandId() : satir.getError();
    //       System.out.println("  satır " + satir.getRowIndex() + ": " + satir.getStatus() + " " + (ref != null ? ref : ""));
    //     }
    //   }
    // }

    // 4) İstek limiti. Anahtar başına dakikada 60 istek (anahtar bazında düşürülebilir).
    //    Aşımda 429 + RATE_LIMIT_EXCEEDED döner.
    //
    //    NOT: limiti ardışık isteklerle göremezsiniz: her istek yaklaşık bir saniye
    //    sürdüğü için 65 ardışık çağrının hepsi 200 döner (60 saniyelik pencere
    //    aradan sıfırlanır). Limit ancak eşzamanlı bir patlamayla görünür.
    ExecutorService havuz = Executors.newFixedThreadPool(70);
    try {
      List<Future<?>> isler = new ArrayList<>();
      for (int i = 0; i < 70; i++) {
        isler.add(havuz.submit(() -> imzala.templates().list(null, 1)));
      }
      for (Future<?> is : isler) {
        is.get();
      }
      System.out.println("Limit tetiklenmedi.");
    } catch (ExecutionException e) {
      if (e.getCause() instanceof ImzalaRateLimitException err) {
        String aciklama = ErrorCodes.describe(err.getCode());
        System.out.println("Limit aşıldı: " + (aciklama != null ? aciklama : err.getCode()));
        System.out.println("  " + err.getRetryAfter() + " saniye sonra tekrar deneyin.");
        var info = err.getRateLimit();
        if (info != null) {
          System.out.println("  Politika: " + info.getPolicy() + " · limit: " + info.getLimit() + " · kalan: " + info.getRemaining());
        }
      } else if (e.getCause() instanceof RuntimeException cause) {
        throw cause;
      } else {
        throw new IllegalStateException(e.getCause());
      }
    } finally {
      havuz.shutdownNow();
    }
  }
}
