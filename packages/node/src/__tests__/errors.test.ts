import { describe, expect, it } from 'vitest';
import {
  ImzalaAuthError,
  ImzalaError,
  ImzalaRateLimitError,
  ImzalaValidationError,
  mapAxiosError,
} from '../errors';

/** Shaped like an axios error — enough for `axios.isAxiosError()` (checks `isAxiosError === true`) plus what `mapAxiosError` reads off it. */
function fakeAxiosError(status: number, data: unknown, headers: Record<string, string> = {}) {
  return {
    isAxiosError: true,
    message: `Request failed with status code ${status}`,
    response: { status, data, headers },
  };
}

describe('mapAxiosError', () => {
  it('maps 401 to ImzalaAuthError', () => {
    const err = mapAxiosError(
      fakeAxiosError(401, { success: false, error: 'INVALID_API_KEY', message: 'Invalid API key' }),
    );
    expect(err).toBeInstanceOf(ImzalaAuthError);
    expect(err).toBeInstanceOf(ImzalaError);
    expect(err.statusCode).toBe(401);
    expect(err.code).toBe('INVALID_API_KEY');
    expect(err.message).toBe('Invalid API key');
  });

  it('maps 403 to ImzalaAuthError', () => {
    const err = mapAxiosError(fakeAxiosError(403, { success: false, error: 'INSUFFICIENT_SCOPE' }));
    expect(err).toBeInstanceOf(ImzalaAuthError);
    expect(err.statusCode).toBe(403);
    expect(err.code).toBe('INSUFFICIENT_SCOPE');
  });

  it('maps 429 to ImzalaRateLimitError, reading retryAfter from a nested error object', () => {
    const err = mapAxiosError(
      fakeAxiosError(429, {
        success: false,
        error: { code: 'RATE_LIMITED', message: 'Too many requests', retry_after_seconds: 42 },
      }),
    ) as ImzalaRateLimitError;

    expect(err).toBeInstanceOf(ImzalaRateLimitError);
    expect(err).toBeInstanceOf(ImzalaError);
    expect(err.retryAfter).toBe(42);
    expect(err.code).toBe('RATE_LIMITED');
    expect(err.message).toBe('Too many requests');
  });

  it('maps 429 retryAfter from the Retry-After header when the body omits it', () => {
    const err = mapAxiosError(
      fakeAxiosError(429, { success: false, error: 'RATE_LIMITED' }, { 'retry-after': '30' }),
    ) as ImzalaRateLimitError;

    expect(err.retryAfter).toBe(30);
  });

  it('maps 422 to ImzalaValidationError', () => {
    const err = mapAxiosError(fakeAxiosError(422, { success: false, message: 'Invalid payload' }));
    expect(err).toBeInstanceOf(ImzalaValidationError);
    expect(err).toBeInstanceOf(ImzalaError);
    expect(err.statusCode).toBe(422);
    expect(err.message).toBe('Invalid payload');
  });

  it('falls back to the base ImzalaError for unmapped statuses (404, 500)', () => {
    const err404 = mapAxiosError(fakeAxiosError(404, { success: false, error: 'DEMAND_NOT_FOUND' }));
    expect(err404).toBeInstanceOf(ImzalaError);
    expect(err404).not.toBeInstanceOf(ImzalaAuthError);
    expect(err404).not.toBeInstanceOf(ImzalaRateLimitError);
    expect(err404).not.toBeInstanceOf(ImzalaValidationError);
    expect(err404.statusCode).toBe(404);
    expect(err404.code).toBe('DEMAND_NOT_FOUND');

    const err500 = mapAxiosError(fakeAxiosError(500, { success: false }));
    expect(err500.statusCode).toBe(500);
  });

  it('wraps a plain Error (e.g. network/timeout failure) as a base ImzalaError', () => {
    const err = mapAxiosError(new Error('ECONNREFUSED'));
    expect(err).toBeInstanceOf(ImzalaError);
    expect(err).not.toBeInstanceOf(ImzalaAuthError);
    expect(err.message).toBe('ECONNREFUSED');
    expect(err.statusCode).toBeUndefined();
    expect(err.cause).toBeInstanceOf(Error);
  });

  it('passes an already-typed ImzalaError through unchanged', () => {
    const original = new ImzalaValidationError('bad payload', { statusCode: 422 });
    expect(mapAxiosError(original)).toBe(original);
  });

  it('every subclass is an instanceof ImzalaError and Error', () => {
    for (const err of [
      new ImzalaAuthError('a'),
      new ImzalaRateLimitError('b'),
      new ImzalaValidationError('c'),
    ]) {
      expect(err).toBeInstanceOf(ImzalaError);
      expect(err).toBeInstanceOf(Error);
    }
  });
});
