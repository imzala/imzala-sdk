package org.imzala;

import org.imzala.client.generated.ApiException;

import java.util.function.Function;

/**
 * Every imzala.org API response uses the same envelope: {@code {success:
 * true, data: {...}}} on success, or a non-2xx status with {@code
 * {success: false, error/message: ...}} on failure (surfaced by the
 * vendored generated client as a thrown {@code ApiException}, not a
 * resolved-but-failed response).
 *
 * <p>{@link #unwrap} invokes a generated-client call, unwraps its {@code
 * data}, and normalizes any failure (thrown checked {@code ApiException},
 * or a {@code {success:false}} body on an otherwise-2xx response) into a
 * typed {@link ImzalaException} — see {@link ErrorMapper}. Every resource
 * method in {@code TemplatesResource}/{@code DemandsResource}/{@code
 * EmbedResource}/{@code TimestampsResource}/{@link Imzala#me()} routes
 * through this. Mirrors {@code http.ts}'s {@code unwrap<T>()} (TS), {@code
 * client.py}'s {@code _unwrap()} (Python), and {@code Http.cs}'s {@code
 * Unwrap<TResponse, TData>()} (C#).
 */
final class Http {

  private Http() {
  }

  /** The vendored generated client's *Api methods are synchronous and declare {@code throws ApiException} — this bridges that checked-exception signature into a lambda. */
  @FunctionalInterface
  interface ApiCall<T> {
    T call() throws ApiException;
  }

  static <TResponse, TData> TData unwrap(
      ApiCall<TResponse> call,
      Function<TResponse, Boolean> success,
      Function<TResponse, TData> data) {
    TResponse response;
    try {
      response = call.call();
    } catch (ApiException err) {
      throw ErrorMapper.map(err);
    }

    if (response == null || !Boolean.TRUE.equals(success.apply(response))) {
      throw new ImzalaException("imzala.org API request failed");
    }

    return data.apply(response);
  }
}
