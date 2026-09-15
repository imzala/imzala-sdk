import { readFileSync } from 'node:fs';
import { describe, expect, it } from 'vitest';
import { IMZALA_ERROR_CODES, describeErrorCode, isKnownErrorCode } from '../errorCodes';
import { ImzalaRateLimitError, extractErrorCode, mapAxiosError } from '../errors';

// The catalogue is derived from the vendored public spec. These tests keep the
// two in step: a code the spec no longer mentions must leave the catalogue,
// and a new code-like token in the spec must either join the catalogue or be
// listed below as deliberately not an error code.
const specText = readFileSync(new URL('../../../../spec/openapi.v1.yaml', import.meta.url), 'utf8');

const NOT_ERROR_CODES: Record<string, string> = {
  ANCHOR_TEXT_NOT_FOUND: 'field layout diagnostic code inside a response body',
  CREATE_FAILED: 'per-row result code in the bulk 200 response',
  DISPATCH_FAILED: 'per-party invitation result code in a 200 response',
  DISPATCH_SKIPPED: 'invitation result code in a 200 response',
  RECIPIENT_QUOTA_EXCEEDED: 'per-party invitation result code in a 200 response',
  ON_ANCHOR_MISS_NOT_RELAXED: 'warning in a 200 response, not an error',
  DEAD_LETTER: 'webhook delivery status',
  DEMAND_ID: 'shell variable in a curl example',
  ENVELOPE_DECISION_ENFORCE: 'server feature flag name',
  FIELD_LAYOUT: 'template kind value',
  FILLABLE_TYPES: 'server constant name in prose',
  IMZALA_WEBHOOK_SECRET: 'environment variable in a code sample',
  KVKK_CONSENT: 'doc_kind value',
  KVKK_NOTICE: 'doc_kind value',
  PRICE_LIST: 'doc_kind value',
  WEBHOOK_TIMEOUT_MS: 'server environment variable',
};

// Prose also contains single upper-case words (API, PDF, KVKK), so the
// spec-to-catalogue direction only considers underscore tokens. Single-word
// codes such as UNAUTHORIZED are still checked the other way round.
const CODE_TOKEN = /\b[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)+\b/g;

describe('error code catalogue', () => {
  it('every catalogued code appears in the public spec', () => {
    const missing = Object.keys(IMZALA_ERROR_CODES).filter((c) => !new RegExp(`\\b${c}\\b`).test(specText));
    expect(missing).toEqual([]);
  });

  it('every code-like token in the spec is catalogued or explicitly excluded', () => {
    const tokens = [...new Set(specText.match(CODE_TOKEN) ?? [])];
    const unclassified = tokens.filter((t) => !isKnownErrorCode(t) && !(t in NOT_ERROR_CODES));
    expect(unclassified).toEqual([]);
  });

  it('the exclusion list has no stale or overlapping entries', () => {
    for (const token of Object.keys(NOT_ERROR_CODES)) {
      expect(specText, token).toMatch(new RegExp(`\\b${token}\\b`));
      expect(isKnownErrorCode(token), token).toBe(false);
    }
  });

  it('every description is a non-empty single line without an em dash', () => {
    for (const [code, text] of Object.entries(IMZALA_ERROR_CODES)) {
      expect(text, code).toMatch(/\S/);
      expect(text, code).not.toContain('\n');
      expect(text, code).not.toContain('\u2014');
    }
  });

  it('unknown or missing codes return undefined instead of throwing', () => {
    expect(describeErrorCode('NEVER_A_REAL_CODE')).toBeUndefined();
    expect(describeErrorCode(undefined)).toBeUndefined();
    expect(isKnownErrorCode('toString')).toBe(false);
  });
});

function axiosError(status: number, data: unknown, headers: Record<string, string> = {}) {
  return { isAxiosError: true, message: `Request failed with status code ${status}`, response: { status, data, headers } };
}

describe('error body shapes', () => {
  it('{ error: "CODE", message } reads the code from error', () => {
    expect(extractErrorCode({ success: false, error: 'PAGE_ID_REQUIRED', message: 'Each item must have an integer page_id' })).toBe(
      'PAGE_ID_REQUIRED',
    );
  });

  it('{ error: "human text", code: "CODE" } reads the code from code, never the text', () => {
    const err = mapAxiosError(
      axiosError(400, { success: false, error: 'Geçersiz sayfa numarası (page >= 1 olmalı)', code: 'INVALID_PAGE' }) as never,
    );
    expect(err.code).toBe('INVALID_PAGE');
    expect(err.message).toBe('Geçersiz sayfa numarası (page >= 1 olmalı)');
    expect(err.codeDescription).toBe(IMZALA_ERROR_CODES.INVALID_PAGE);
  });

  it('{ error: { code, message } } reads the nested code', () => {
    expect(extractErrorCode({ success: false, error: { code: 'DEMAND_NOT_DISPATCHABLE', message: 'x' } })).toBe(
      'DEMAND_NOT_DISPATCHABLE',
    );
  });

  it('a single-word code in error is still a code', () => {
    expect(extractErrorCode({ error: 'UNAUTHORIZED' })).toBe('UNAUTHORIZED');
  });

  it('an HTTP-date Retry-After is converted to seconds', () => {
    const at = new Date(Date.now() + 30_000).toUTCString();
    const err = mapAxiosError(axiosError(429, { success: false, code: 'RATE_LIMIT_EXCEEDED' }, { 'retry-after': at }) as never);
    expect((err as ImzalaRateLimitError).retryAfter).toBeGreaterThanOrEqual(28);
    expect((err as ImzalaRateLimitError).retryAfter).toBeLessThanOrEqual(30);
  });

  it('a plain human-readable error string is not reported as a code', () => {
    const err = mapAxiosError(axiosError(404, { success: false, error: 'Sözleşme bulunamadı' }) as never);
    expect(err.code).toBeUndefined();
    expect(err.message).toBe('Sözleşme bulunamadı');
    expect(err.codeDescription).toBeUndefined();
  });
});

describe('rate limit errors', () => {
  it('attaches the description and reads Retry-After from the header', () => {
    const err = mapAxiosError(
      axiosError(429, { success: false, error: 'Çok fazla istek', code: 'RATE_LIMIT_EXCEEDED' }, { 'retry-after': '30' }) as never,
    );
    expect(err).toBeInstanceOf(ImzalaRateLimitError);
    expect(err.code).toBe('RATE_LIMIT_EXCEEDED');
    expect(err.codeDescription).toBe(IMZALA_ERROR_CODES.RATE_LIMIT_EXCEEDED);
    expect((err as ImzalaRateLimitError).retryAfter).toBe(30);
  });

  it('reads retry_after_seconds from the body when there is no header', () => {
    const err = mapAxiosError(
      axiosError(
        429,
        { success: false, error: 'Çok fazla istek gönderildi. Lütfen bir dakika bekleyin.', code: 'RATE_LIMIT_EXCEEDED', retry_after_seconds: 60 },
        { 'ratelimit-limit': '60', 'ratelimit-policy': '60;w=60' },
      ) as never,
    ) as ImzalaRateLimitError;
    expect(err.code).toBe('RATE_LIMIT_EXCEEDED');
    expect(err.retryAfter).toBe(60);
  });

  it('reads the standard RateLimit-* headers and ignores X-RateLimit-*', () => {
    const err = mapAxiosError(
      axiosError(
        429,
        { success: false, code: 'RATE_LIMIT_EXCEEDED', retry_after_seconds: 60 },
        {
          'ratelimit-limit': '5',
          'ratelimit-remaining': '0',
          'ratelimit-reset': '42',
          'ratelimit-policy': '5;w=60',
          'x-ratelimit-limit': '999',
        },
      ) as never,
    ) as ImzalaRateLimitError;
    expect(err.rateLimit).toEqual({ limit: 5, remaining: 0, reset: 42, policy: '5;w=60' });
  });

  it('leaves rateLimit undefined when the server sent no RateLimit-* headers', () => {
    const err = mapAxiosError(axiosError(429, { success: false, error: { code: 'RATE_LIMITED' } }) as never);
    expect((err as ImzalaRateLimitError).rateLimit).toBeUndefined();
  });
});
