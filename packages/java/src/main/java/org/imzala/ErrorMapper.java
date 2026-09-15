package org.imzala;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.imzala.client.generated.ApiException;

import java.net.http.HttpHeaders;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Maps any exception thrown while calling the vendored generated client (or
 * a bare {@code {success:false}} envelope) to the appropriate
 * {@link ImzalaException} subclass, based on HTTP status code.
 *
 * <p>imzala.org error envelopes are not fully uniform across endpoints: most
 * are {@code {success:false, error:"<code>", message:"<text>"}}, but some
 * (e.g. the reminders 429) nest a {@code {code, message, retry_after_seconds}}
 * object under {@code error} instead of a plain string —
 * {@link #extractErrorMessage} / {@link #extractErrorCode} handle both
 * shapes, mirroring {@code errors.ts}'s {@code extractErrorMessage}/
 * {@code extractErrorCode} and {@code errors.py}'s / {@code Errors.cs}'s
 * equivalents.
 */
final class ErrorMapper {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private ErrorMapper() {
  }

  static ImzalaException map(Throwable err) {
    if (err instanceof ImzalaException already) {
      return already;
    }

    if (err instanceof ApiException apiEx) {
      int status = apiEx.getCode();
      String bodyText = apiEx.getResponseBody();
      JsonNode json = tryParseJson(bodyText);
      String message = extractErrorMessage(json);
      if (message == null) {
        message = apiEx.getMessage();
      }
      String code = extractErrorCode(json);

      switch (status) {
        case 401:
        case 403:
          return new ImzalaAuthException(message, status, bodyText, code, apiEx);
        case 429:
          HttpHeaders headers = apiEx.getResponseHeaders();
          Double retryAfter = extractRetryAfter(json, headers);
          return new ImzalaRateLimitException(message, status, bodyText, code, retryAfter, extractRateLimitInfo(headers), apiEx);
        case 422:
          return new ImzalaValidationException(message, status, bodyText, code, apiEx);
        default:
          return new ImzalaException(message, status, bodyText, code, apiEx);
      }
    }

    return new ImzalaException(err.getMessage(), null, null, null, err);
  }

  private static JsonNode tryParseJson(String text) {
    if (text == null || text.isEmpty()) {
      return null;
    }
    try {
      return MAPPER.readTree(text);
    } catch (Exception e) {
      return null;
    }
  }

  static String extractErrorMessage(JsonNode body) {
    if (body == null || !body.isObject()) {
      return null;
    }

    JsonNode msg = body.get("message");
    if (msg != null && msg.isTextual()) {
      return msg.asText();
    }

    JsonNode error = body.get("error");
    if (error != null) {
      if (error.isTextual()) {
        return error.asText();
      }
      if (error.isObject()) {
        JsonNode nestedMsg = error.get("message");
        if (nestedMsg != null && nestedMsg.isTextual()) {
          return nestedMsg.asText();
        }
        JsonNode nestedCode = error.get("code");
        if (nestedCode != null && nestedCode.isTextual()) {
          return nestedCode.asText();
        }
      }
    }

    return null;
  }

  /**
   * Upper case, digits and underscores only, at least three characters:
   * covers {@code TEMPLATE_IN_USE}, {@code BULK_MAX_10} and single-word codes
   * such as {@code UNAUTHORIZED}, never a human-readable sentence.
   */
  private static final Pattern CODE_SHAPE = Pattern.compile("^[A-Z][A-Z0-9_]{2,}$");

  /**
   * Bodies come as {@code {error:"<CODE>", message}}, {@code {error:"<text>",
   * code:"<CODE>"}} (rate limits, CodedError) or {@code {error:{code,
   * message}}} (reminders); some carry only a human-readable {@code error}
   * string and no code at all. {@code code} wins; {@code error} counts only
   * when it is shaped like a code.
   */
  static String extractErrorCode(JsonNode body) {
    if (body == null || !body.isObject()) {
      return null;
    }

    JsonNode direct = body.get("code");
    if (direct != null && direct.isTextual()) {
      return direct.asText();
    }

    JsonNode error = body.get("error");
    if (error != null) {
      if (error.isTextual()) {
        String text = error.asText();
        return CODE_SHAPE.matcher(text).matches() ? text : null;
      }
      if (error.isObject()) {
        JsonNode nestedCode = error.get("code");
        if (nestedCode != null && nestedCode.isTextual()) {
          return nestedCode.asText();
        }
      }
    }

    return null;
  }

  /** Only a finite, non-negative number of seconds is usable; anything else (NaN, Infinity) counts as absent. */
  private static Double extractRetryAfter(JsonNode body, HttpHeaders headers) {
    Double seconds = readRetryAfter(body, headers);
    return seconds != null && Double.isFinite(seconds) && seconds >= 0 ? seconds : null;
  }

  private static Double readRetryAfter(JsonNode body, HttpHeaders headers) {
    if (body != null && body.isObject()) {
      JsonNode direct = body.get("retry_after_seconds");
      if (direct != null && direct.isNumber()) {
        return direct.asDouble();
      }
      JsonNode error = body.get("error");
      if (error != null && error.isObject()) {
        JsonNode nested = error.get("retry_after_seconds");
        if (nested != null && nested.isNumber()) {
          return nested.asDouble();
        }
      }
    }

    String value = header(headers, "Retry-After");
    if (value == null) {
      return null;
    }
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException ignored) {
      // Retry-After may also be an HTTP date.
    }
    try {
      Instant at = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
      long millis = at.toEpochMilli() - System.currentTimeMillis();
      return (double) Math.max(0L, (long) Math.ceil(millis / 1000.0));
    } catch (DateTimeParseException ignored) {
      return null;
    }
  }

  /** Reads the standard {@code RateLimit-*} headers; {@code X-RateLimit-*} is not sent by the server and is ignored. */
  static RateLimitInfo extractRateLimitInfo(HttpHeaders headers) {
    Integer limit = intHeader(headers, "RateLimit-Limit");
    Integer remaining = intHeader(headers, "RateLimit-Remaining");
    Integer reset = intHeader(headers, "RateLimit-Reset");
    String policy = header(headers, "RateLimit-Policy");
    if (limit == null && remaining == null && reset == null && policy == null) {
      return null;
    }
    return new RateLimitInfo(limit, remaining, reset, policy);
  }

  private static Integer intHeader(HttpHeaders headers, String name) {
    String value = header(headers, name);
    if (value == null) {
      return null;
    }
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  /** Case-insensitive lookup of the first non-blank value; does not rely on the header map folding case. */
  private static String header(HttpHeaders headers, String name) {
    if (headers == null) {
      return null;
    }
    for (Map.Entry<String, List<String>> entry : headers.map().entrySet()) {
      if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name) && entry.getValue() != null) {
        for (String value : entry.getValue()) {
          if (value != null && !value.isBlank()) {
            return value.trim();
          }
        }
      }
    }
    return null;
  }
}
