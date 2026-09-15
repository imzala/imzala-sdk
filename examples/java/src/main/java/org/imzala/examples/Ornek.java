package org.imzala.examples;

import org.imzala.Imzala;

/** Örneklerin ortak kurulumu: anahtar ve taban adres ortam değişkeninden okunur. */
final class Ornek {

  private Ornek() {}

  /** Varsayılan yeniden deneme ayarlarıyla istemci. */
  static Imzala istemci() {
    String apiKey = zorunluOrtam("IMZALA_API_KEY");
    String baseUrl = ortam("IMZALA_BASE_URL");
    // baseUrl boşsa SDK varsayılanı (api-prd.imzala.org) kullanılır.
    return baseUrl == null ? new Imzala(apiKey) : new Imzala(apiKey, baseUrl);
  }

  /** GET isteklerinin 429 sonrası otomatik yinelenmesi kapalı istemci (senaryo 05). */
  static Imzala yinelemesizIstemci() {
    String apiKey = zorunluOrtam("IMZALA_API_KEY");
    String baseUrl = ortam("IMZALA_BASE_URL");
    String etkinBaseUrl = baseUrl == null ? "https://api-prd.imzala.org" : baseUrl;
    return new Imzala(apiKey, etkinBaseUrl, 30_000, 0, 300);
  }

  static String ortam(String ad) {
    String deger = System.getenv(ad);
    return deger == null || deger.isEmpty() ? null : deger;
  }

  private static String zorunluOrtam(String ad) {
    String deger = ortam(ad);
    if (deger == null) {
      throw new IllegalStateException(ad + " tanımlı değil.");
    }
    return deger;
  }
}
