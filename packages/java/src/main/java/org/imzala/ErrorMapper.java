package org.imzala;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.imzala.client.generated.ApiException;

import java.net.http.HttpHeaders;
import java.util.Optional;

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
          Double retryAfter = extractRetryAfter(json, apiEx.getResponseHeaders());
          return new ImzalaRateLimitException(message, status, bodyText, code, retryAfter, apiEx);
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

  static String extractErrorCode(JsonNode body) {
    if (body == null || !body.isObject()) {
      return null;
    }

    JsonNode error = body.get("error");
    if (error != null) {
      if (error.isTextual()) {
        return error.asText();
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

  private static Double extractRetryAfter(JsonNode body, HttpHeaders headers) {
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

    if (headers != null) {
      Optional<String> headerValue = headers.firstValue("Retry-After");
      if (headerValue.isPresent()) {
        try {
          return Double.parseDouble(headerValue.get());
        } catch (NumberFormatException ignored) {
          // fall through
        }
      }
    }

    return null;
  }
}
