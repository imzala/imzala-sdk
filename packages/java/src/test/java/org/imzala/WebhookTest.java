package org.imzala;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mirrors {@code webhook.test.ts} (B1), {@code test_webhook.py} (B2), and
 * {@code WebhookTests.cs} (B3) — the fixture below is the same
 * WebhookEnvelope shape ({@code src/services/webhook/WebhookSigner.ts} in
 * imzala-service): HMAC-SHA256(rawBody, secret) hex digest,
 * {@code 'sha256='}-prefixed. Signed independently here with {@code
 * javax.crypto.Mac} rather than reusing {@link Imzala#verifyWebhook}'s own
 * digest helper, so a bug in that helper can't also hide the same bug in
 * the test fixture.
 */
class WebhookTest {

  private static final String SECRET = "whsec_1e6f1c2b3a4d5e6f7081920a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6";

  private static final String RAW_BODY =
      "{\"id\":\"evt_abc123\",\"type\":\"demand.completed\",\"created_at\":\"2026-07-01T00:00:00.000Z\",\"data\":{\"demand_id\":\"d1\"}}";

  private static String sign(String body, String key) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] digest = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(digest.length * 2);
      for (byte b : digest) {
        hex.append(Character.forDigit((b >> 4) & 0xF, 16));
        hex.append(Character.forDigit(b & 0xF, 16));
      }
      return "sha256=" + hex;
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String sign(String body) {
    return sign(body, SECRET);
  }

  // Note: unlike .NET's HMACSHA256 (which accepts an empty key), Java's
  // javax.crypto.spec.SecretKeySpec throws IllegalArgumentException for an
  // empty key — so this fixture can't compute "what an empty-secret
  // signature would look like" the way WebhookTests.cs (B3) does. Not
  // needed anyway: Imzala.verifyWebhook's empty-secret guard short-circuits
  // to false before ever touching javax.crypto, regardless of what
  // signatureHeader is passed — see the test below, which reuses a
  // real-secret signature purely as an arbitrary non-null header value.

  @Test
  void returns_true_for_a_correctly_signed_string_body() {
    assertTrue(Imzala.verifyWebhook(SECRET, RAW_BODY, sign(RAW_BODY)));
  }

  @Test
  void returns_true_for_a_correctly_signed_byte_array_body_same_bytes_as_string_case() {
    byte[] bytes = RAW_BODY.getBytes(StandardCharsets.UTF_8);
    assertTrue(Imzala.verifyWebhook(SECRET, bytes, sign(RAW_BODY)));
  }

  @Test
  void returns_false_for_the_wrong_secret() {
    assertFalse(Imzala.verifyWebhook(
        "whsec_wrong_00000000000000000000000000000000000000000000000000",
        RAW_BODY,
        sign(RAW_BODY)));
  }

  @Test
  void returns_false_when_the_body_was_tampered_with_after_signing() {
    String tampered = RAW_BODY.replace("d1", "d2-evil");
    assertFalse(Imzala.verifyWebhook(SECRET, tampered, sign(RAW_BODY)));
  }

  @Test
  void returns_false_for_a_malformed_header_missing_the_sha256_prefix_length_mismatch() {
    String digestOnly = sign(RAW_BODY).replace("sha256=", "");
    assertFalse(Imzala.verifyWebhook(SECRET, RAW_BODY, digestOnly));
  }

  @Test
  void returns_false_for_a_garbage_header_of_the_same_length_no_throw() {
    String valid = sign(RAW_BODY);
    String garbage = "sha256=" + "0".repeat(valid.length() - "sha256=".length());
    assertFalse(Imzala.verifyWebhook(SECRET, RAW_BODY, garbage));
  }

  @Test
  void returns_false_never_throws_for_a_missing_or_empty_header() {
    assertFalse(Imzala.verifyWebhook(SECRET, RAW_BODY, null));
    assertFalse(Imzala.verifyWebhook(SECRET, RAW_BODY, ""));
  }

  @Test
  void returns_false_for_an_empty_secret_regardless_of_header_value() {
    assertFalse(Imzala.verifyWebhook("", RAW_BODY, sign(RAW_BODY)));
  }
}
