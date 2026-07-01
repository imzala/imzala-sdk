package org.imzala;

import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.api.AccountApi;
import org.imzala.client.generated.api.DemandsApi;
import org.imzala.client.generated.api.RemindersApi;
import org.imzala.client.generated.api.TemplatesApi;
import org.imzala.client.generated.api.TimestampsApi;
import org.imzala.client.generated.model.ApiV1MeGet200ResponseData;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

/**
 * imzala.org server-side SDK — an ergonomic, hand-written facade over the
 * vendored (openapi-generator {@code java}/{@code native}) client in
 * {@code org.imzala.client.generated}. Every resource method unwraps the
 * {@code {success, data}} response envelope and throws a typed {@link
 * ImzalaException} on failure, instead of returning raw generated-client
 * exceptions.
 *
 * <p><b>Server-only.</b> Constructed with a raw {@code X-API-Key} — never
 * embed this in a client-side (Android app, applet, ...) application where
 * the key could be extracted from the binary.
 *
 * <pre>{@code
 * Imzala imzala = new Imzala(System.getenv("IMZALA_API_KEY"));
 * CreatedDemand demand = imzala.demands().create(
 *     new CreateDemandRequest().templateId(templateId).partyMapping(partyMapping));
 * }</pre>
 */
public final class Imzala {

  private static final String DEFAULT_BASE_URL = "https://api-prd.imzala.org";
  private static final long DEFAULT_TIMEOUT_MS = 30_000;
  private static final String HMAC_ALGORITHM = "HmacSHA256";

  private final AccountResource account;
  private final TemplatesResource templates;
  private final DemandsResource demands;
  private final EmbedResource embed;
  private final TimestampsResource timestamps;

  /** @param apiKey {@code imz_<64 hex>} — from Dashboard → Geliştirici → API Anahtarları, or Hesap Ayarları → API Anahtarları. */
  public Imzala(String apiKey) {
    this(apiKey, DEFAULT_BASE_URL, DEFAULT_TIMEOUT_MS);
  }

  /**
   * @param apiKey {@code imz_<64 hex>} — from Dashboard → Geliştirici → API Anahtarları, or Hesap Ayarları → API Anahtarları.
   * @param baseUrl defaults to {@code https://api-prd.imzala.org}. Use {@code https://test-api.imzala.org} for the test environment.
   */
  public Imzala(String apiKey, String baseUrl) {
    this(apiKey, baseUrl, DEFAULT_TIMEOUT_MS);
  }

  /**
   * @param apiKey {@code imz_<64 hex>} — from Dashboard → Geliştirici → API Anahtarları, or Hesap Ayarları → API Anahtarları.
   * @param baseUrl defaults to {@code https://api-prd.imzala.org}. Use {@code https://test-api.imzala.org} for the test environment.
   * @param timeoutMs per-request read timeout, in milliseconds. Defaults to 30000.
   */
  public Imzala(String apiKey, String baseUrl, long timeoutMs) {
    if (apiKey == null || apiKey.isEmpty()) {
      throw new IllegalArgumentException("new Imzala(apiKey) — apiKey is required.");
    }

    ApiClient apiClient = new ApiClient();
    apiClient.updateBaseUri(baseUrl != null && !baseUrl.isEmpty() ? baseUrl : DEFAULT_BASE_URL);
    apiClient.setReadTimeout(Duration.ofMillis(timeoutMs));
    apiClient.setRequestInterceptor(requestBuilder -> requestBuilder.header("X-API-Key", apiKey));

    this.account = new AccountResource(new AccountApi(apiClient));
    DemandsApi demandsApi = new DemandsApi(apiClient);
    RemindersApi remindersApi = new RemindersApi(apiClient);
    TemplatesApi templatesApi = new TemplatesApi(apiClient);
    TimestampsApi timestampsApi = new TimestampsApi(apiClient);

    this.templates = new TemplatesResource(templatesApi);
    this.demands = new DemandsResource(demandsApi, remindersApi);
    this.embed = new EmbedResource(demandsApi);
    this.timestamps = new TimestampsResource(timestampsApi);
  }

  /** {@code imzala.templates().list()/get(id)/usage(id)}. */
  public TemplatesResource templates() {
    return templates;
  }

  /** {@code imzala.demands().create(...)/get(id)/uploadDocument(...)/addItems(id,...)/sendReminder(id,...)}. */
  public DemandsResource demands() {
    return demands;
  }

  /** {@code imzala.embed().createSession(demandId, partyId)}. */
  public EmbedResource embed() {
    return embed;
  }

  /** {@code imzala.timestamps().create(...)}. */
  public TimestampsResource timestamps() {
    return timestamps;
  }

  /** Returns the calling API key's owner info (id, email, name, workspace, remaining credits). Requires the {@code timestamps} scope. */
  public ApiV1MeGet200ResponseData me() {
    return account.me();
  }

  /**
   * Verifies an imzala.org webhook delivery's {@code X-Imzala-Signature-256}
   * header.
   *
   * <p>Mirrors the backend algorithm exactly ({@code
   * src/services/webhook/WebhookSigner.ts} in imzala-service): {@code
   * 'sha256=' + HMAC-SHA256(rawBody, secret)} as lowercase hex, compared
   * with a fixed-time equality check ({@link MessageDigest#isEqual}, which
   * — since the JDK 6u17 timing-attack fix — compares in time
   * independent of where/whether the arrays first differ, including when
   * their lengths differ).
   *
   * @param secret the {@code whsec_<64-hex>} secret shown once when the webhook was created in the dashboard
   * @param rawBody the <b>exact, unparsed</b> request body bytes. Parsing the JSON and re-serializing it can change byte-for-byte content (key order, whitespace) and break verification — read the raw request body, don't re-serialize a deserialized model
   * @param signatureHeader the raw {@code X-Imzala-Signature-256} header value (e.g. {@code sha256=<hex>})
   * @return {@code true} if the signature is valid. Returns {@code false} — never throws — for a wrong signature, a malformed/missing header, or any other verification failure
   */
  public static boolean verifyWebhook(String secret, byte[] rawBody, String signatureHeader) {
    if (secret == null || secret.isEmpty() || signatureHeader == null || signatureHeader.isEmpty()) {
      return false;
    }

    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
      byte[] digest = mac.doFinal(rawBody != null ? rawBody : new byte[0]);
      String expected = "sha256=" + toHex(digest);

      byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
      byte[] actualBytes = signatureHeader.getBytes(StandardCharsets.UTF_8);

      return MessageDigest.isEqual(expectedBytes, actualBytes);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      return false;
    }
  }

  /** @see #verifyWebhook(String, byte[], String) */
  public static boolean verifyWebhook(String secret, String rawBody, String signatureHeader) {
    return verifyWebhook(
        secret,
        (rawBody != null ? rawBody : "").getBytes(StandardCharsets.UTF_8),
        signatureHeader);
  }

  private static String toHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(Character.forDigit((b >> 4) & 0xF, 16));
      sb.append(Character.forDigit(b & 0xF, 16));
    }
    return sb.toString();
  }
}
