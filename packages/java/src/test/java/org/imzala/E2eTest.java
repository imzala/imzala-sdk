package org.imzala;

import org.imzala.client.generated.model.ApiV1DemandsGet200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsGet200ResponseDataDemandsInner;
import org.imzala.client.generated.model.ApiV1DemandsIdTimelineGet200ResponseData;
import org.imzala.client.generated.model.ApiV1MeGet200ResponseData;
import org.imzala.client.generated.model.ApiV1TemplatesGet200ResponseData;
import org.imzala.client.generated.model.DemandStatus;
import org.imzala.client.generated.model.DemandStatusPartiesInner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Uçtan uca (e2e) testler, GERÇEK İmzala API'sine karşı çalışır.
 *
 * <p>Varsayılan olarak ATLANIR (aşağıdaki {@code @EnabledIfEnvironmentVariable}
 * gate'leri). Çalıştırmak için ortam değişkenleri:
 *
 * <pre>
 *   IMZALA_E2E=1
 *   IMZALA_API_KEY=imz_...
 *   IMZALA_BASE_URL=https://test-api.imzala.org   (opsiyonel; varsayılan prod)
 *
 *   IMZALA_E2E=1 IMZALA_API_KEY=imz_... IMZALA_BASE_URL=https://test-api.imzala.org \
 *     mvn -q -Dtest=E2eTest test
 * </pre>
 *
 * <p>Yalnızca SALT-OKUMA uçları çağrılır (kredi harcamaz, veri değiştirmez):
 * {@code me} / {@code templates().list} / {@code demands().list} /
 * {@code demands().get} / {@code getTimeline}, ve geçersiz bir id'nin tipli
 * {@link ImzalaException} fırlattığı. Böylece herhangi bir gerçek hesaba karşı
 * güvenle koşturulabilir. İki env değişkeni de yoksa test container'ı
 * "disabled" olur (kırmızı değil), tıpkı Node facade'ının {@code describe.skip}
 * deseni gibi. PII maskeleme (counts-only listede ham e-posta yok, detayda
 * {@code email_masked}) burada da doğrulanır.
 */
@EnabledIfEnvironmentVariable(named = "IMZALA_E2E", matches = "1")
@EnabledIfEnvironmentVariable(named = "IMZALA_API_KEY", matches = ".+")
class E2eTest {

  /** Ham e-posta sızıntısını yakalar (maskeli e-posta {@code a***@x.com} eşleşmez). */
  private static final Pattern RAW_EMAIL = Pattern.compile("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}", Pattern.CASE_INSENSITIVE);

  private static Imzala imzala;

  @BeforeAll
  static void setup() {
    String apiKey = System.getenv("IMZALA_API_KEY");
    String baseUrl = System.getenv("IMZALA_BASE_URL");
    imzala = (baseUrl == null || baseUrl.isEmpty())
        ? new Imzala(apiKey)
        : new Imzala(apiKey, baseUrl);
  }

  @Test
  void me_returns_owner_and_credits() {
    ApiV1MeGet200ResponseData me = imzala.me();
    assertNotNull(me);
    // e-posta ya da id alanlarından biri dolu olmalı
    assertTrue(me.getEmail() != null || me.getId() != null, "me() sahibi bir id ya da e-posta döndürmeli");
  }

  @Test
  void templates_list_unwraps_envelope() {
    ApiV1TemplatesGet200ResponseData res = imzala.templates().list(1, 3);
    assertNotNull(res.getTemplates(), "templates listesi (zarf açılmış) null olmamalı");
    assertNotNull(res.getTotal(), "total sayaç alanı dolu olmalı");
  }

  @Test
  void demands_list_is_counts_only_and_leaks_no_pii() {
    ApiV1DemandsGet200ResponseData res = imzala.demands().list(new ListDemandsParams().limit(3));
    assertNotNull(res.getDemands(), "demands listesi (zarf açılmış) null olmamalı");
    // counts-only liste ham e-posta/telefon içermemeli
    assertFalse(RAW_EMAIL.matcher(String.valueOf(res)).find(), "counts-only liste ham e-posta sızdırmamalı");
  }

  @Test
  void demand_get_and_timeline_when_a_demand_exists() {
    ApiV1DemandsGet200ResponseData list = imzala.demands().list(new ListDemandsParams().limit(1));
    List<ApiV1DemandsGet200ResponseDataDemandsInner> demands = list.getDemands();
    if (demands == null || demands.isEmpty()) {
      return; // sözleşme yoksa atla
    }

    UUID firstId = demands.get(0).getId();
    DemandStatus demand = imzala.demands().get(firstId);
    assertEquals(firstId, demand.getId());

    // detay maskeli: ham e-posta yok, email_masked var
    if (demand.getParties() != null) {
      for (DemandStatusPartiesInner p : demand.getParties()) {
        String masked = p.getEmailMasked();
        if (masked != null) {
          assertFalse(masked.matches("^[^*]+@.*"), "party e-postası maskelenmemiş görünüyor: " + masked);
        }
      }
    }

    ApiV1DemandsIdTimelineGet200ResponseData timeline = imzala.demands().getTimeline(firstId);
    assertNotNull(timeline.getEvents(), "denetim izi events dizisi null olmamalı");
  }

  @Test
  void invalid_id_throws_typed_ImzalaException() {
    UUID missing = UUID.fromString("00000000-0000-0000-0000-000000000000");
    assertThrows(ImzalaException.class, () -> imzala.demands().get(missing));
  }
}
