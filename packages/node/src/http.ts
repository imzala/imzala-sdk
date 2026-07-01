import type { AxiosPromise } from 'axios';
import { ImzalaError, extractErrorMessage, mapAxiosError } from './errors';

/**
 * Every imzala.org API response uses the same envelope:
 * `{success: true, data: {...}}` on success, or a non-2xx status with
 * `{success: false, error/message: ...}` on failure.
 *
 * `unwrap` awaits a generated-client call, unwraps `data`, and normalizes
 * any failure (HTTP error status, network error, or a `{success:false}`
 * body on an otherwise-2xx response) into a typed `ImzalaError` — see
 * ./errors. Every facade method in ./index routes through this.
 */
export async function unwrap<T>(
  promise: AxiosPromise<{ success?: boolean; data?: T }>,
): Promise<T> {
  let response;
  try {
    response = await promise;
  } catch (err) {
    throw mapAxiosError(err);
  }

  const body = response.data;
  if (!body || body.success === false) {
    throw new ImzalaError(extractErrorMessage(body) ?? 'imzala.org API request failed', {
      statusCode: response.status,
      body,
    });
  }

  return body.data as T;
}
