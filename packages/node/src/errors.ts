import axios from 'axios';
import { describeErrorCode, type ImzalaErrorCode } from './errorCodes';

export interface ImzalaErrorOptions {
  /** HTTP status code, when the error originated from an HTTP response. */
  statusCode?: number;
  /** Raw response body (already-parsed JSON), when available. */
  body?: unknown;
  /** Machine-readable error code from the response envelope, when present. */
  code?: string;
  /** The underlying error (axios error, network error, ...), for `Error.cause`. */
  cause?: unknown;
}

/**
 * Base error type thrown by every `@imzala/node` facade method. Normalizes
 * axios errors, network failures, and `{success:false}` response envelopes
 * into a single throwable shape so callers never need to reach into axios
 * internals.
 *
 * Thrown directly (not as a subclass) for statuses that don't have a
 * dedicated subclass below (400, 404, 409, 500, ...).
 */
export class ImzalaError extends Error {
  readonly statusCode?: number;
  readonly body?: unknown;
  /** Machine-readable code, e.g. `TEMPLATE_IN_USE`. Undefined when the response carried none. */
  readonly code?: ImzalaErrorCode | (string & {});
  /** One-line explanation of `code` from the SDK's catalogue; undefined for codes it does not know. */
  readonly codeDescription?: string;

  constructor(message: string, options: ImzalaErrorOptions = {}) {
    super(message, options.cause !== undefined ? { cause: options.cause } : undefined);
    this.name = 'ImzalaError';
    this.statusCode = options.statusCode;
    this.body = options.body;
    this.code = options.code;
    this.codeDescription = describeErrorCode(options.code);
    // Restore the prototype chain — down-leveled targets (e.g. ES5) break
    // `instanceof` for classes extending built-ins like Error otherwise.
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

/** Missing/invalid API key (401) or disabled key / insufficient scope (403). */
export class ImzalaAuthError extends ImzalaError {
  constructor(message: string, options: ImzalaErrorOptions = {}) {
    super(message, options);
    this.name = 'ImzalaAuthError';
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

/** Standard `RateLimit-*` response headers. The server does not send `X-RateLimit-*`. */
export interface ImzalaRateLimitInfo {
  /** `ratelimit-limit`: requests allowed per window. Defaults to 60 but can be lowered per API key, so read it rather than assuming. */
  limit?: number;
  /** `ratelimit-remaining`: requests left in the current window. */
  remaining?: number;
  /** `ratelimit-reset`: seconds until the window resets. */
  reset?: number;
  /** `ratelimit-policy`: raw policy string, e.g. `60;w=60`. */
  policy?: string;
}

/**
 * Rate limited (429). `retryAfter` is seconds, when the server provided one.
 *
 * Several different limits answer with 429 and each has its own `code`
 * (e.g. `RATE_LIMIT_EXCEEDED`, `TOO_MANY_REQUESTS`, `RATE_LIMITED`,
 * `RECIPIENT_RESEND_LIMIT`, `MAX_SMS_REMINDERS_REACHED`), so branch on this
 * class or on `statusCode`, not on one particular code.
 */
export class ImzalaRateLimitError extends ImzalaError {
  readonly retryAfter?: number;
  readonly rateLimit?: ImzalaRateLimitInfo;

  constructor(
    message: string,
    options: ImzalaErrorOptions & { retryAfter?: number; rateLimit?: ImzalaRateLimitInfo } = {},
  ) {
    super(message, options);
    this.name = 'ImzalaRateLimitError';
    this.retryAfter = options.retryAfter;
    this.rateLimit = options.rateLimit;
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

/** Request payload failed validation (422). */
export class ImzalaValidationError extends ImzalaError {
  constructor(message: string, options: ImzalaErrorOptions = {}) {
    super(message, options);
    this.name = 'ImzalaValidationError';
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

/**
 * imzala.org error envelopes come in three shapes:
 * - `{success:false, error:"<CODE>", message:"<text>"}`
 * - `{success:false, error:"<text>", code:"<CODE>"}` (rate limits, `CodedError`)
 * - `{success:false, error:{code, message, retry_after_seconds}}` (reminders)
 * and some errors carry only a human-readable `error` string with no code.
 * These helpers handle all of them.
 */
function asRecord(value: unknown): Record<string, unknown> | undefined {
  return value && typeof value === 'object' ? (value as Record<string, unknown>) : undefined;
}

export function extractErrorMessage(body: unknown): string | undefined {
  const b = asRecord(body);
  if (!b) return undefined;
  if (typeof b.message === 'string') return b.message;
  if (typeof b.error === 'string') return b.error;
  const nested = asRecord(b.error);
  if (nested) {
    if (typeof nested.message === 'string') return nested.message;
    if (typeof nested.code === 'string') return nested.code;
  }
  return undefined;
}

// Upper case, digits and underscores only, at least three characters: covers
// `TEMPLATE_IN_USE`, `BULK_MAX_10` and single-word codes such as `UNAUTHORIZED`,
// never a human-readable sentence.
const CODE_SHAPE = /^[A-Z][A-Z0-9_]{2,}$/;

export function extractErrorCode(body: unknown): string | undefined {
  const b = asRecord(body);
  if (!b) return undefined;
  if (typeof b.code === 'string') return b.code;
  // `error` holds either a code or a human-readable sentence; only the
  // former is a code.
  if (typeof b.error === 'string') return CODE_SHAPE.test(b.error) ? b.error : undefined;
  const nested = asRecord(b.error);
  if (nested && typeof nested.code === 'string') return nested.code;
  return undefined;
}

function extractRetryAfter(body: unknown, headers: unknown): number | undefined {
  const b = asRecord(body);
  const direct = b?.retry_after_seconds;
  if (typeof direct === 'number') return direct;
  const nested = asRecord(b?.error);
  const nestedRetry = nested?.retry_after_seconds;
  if (typeof nestedRetry === 'number') return nestedRetry;

  const h = asRecord(headers);
  const header = h?.['retry-after'] ?? h?.['Retry-After'];
  if (typeof header === 'string' || typeof header === 'number') {
    const n = Number(header);
    if (!Number.isNaN(n)) return n;
    // Retry-After may also be an HTTP date.
    const at = Date.parse(String(header));
    if (!Number.isNaN(at)) return Math.max(0, Math.ceil((at - Date.now()) / 1000));
  }
  return undefined;
}

function extractRateLimitInfo(headers: unknown): ImzalaRateLimitInfo | undefined {
  const h = asRecord(headers);
  if (!h) return undefined;
  const num = (key: string): number | undefined => {
    const raw = h[key];
    if (raw === undefined || raw === null || raw === '') return undefined;
    const n = Number(raw);
    return Number.isNaN(n) ? undefined : n;
  };
  const info: ImzalaRateLimitInfo = {
    limit: num('ratelimit-limit'),
    remaining: num('ratelimit-remaining'),
    reset: num('ratelimit-reset'),
    policy: typeof h['ratelimit-policy'] === 'string' ? h['ratelimit-policy'] : undefined,
  };
  if (Object.values(info).every((v) => v === undefined)) return undefined;
  return Object.fromEntries(Object.entries(info).filter(([, v]) => v !== undefined)) as ImzalaRateLimitInfo;
}

/**
 * Maps a raw axios error (or any thrown value) to the appropriate
 * `ImzalaError` subclass, based on HTTP status code.
 */
export function mapAxiosError(err: unknown): ImzalaError {
  if (axios.isAxiosError(err)) {
    const status = err.response?.status;
    const body = err.response?.data;
    const headers = err.response?.headers;
    const message = extractErrorMessage(body) ?? err.message;
    const code = extractErrorCode(body);

    if (status === 401 || status === 403) {
      return new ImzalaAuthError(message, { statusCode: status, body, code, cause: err });
    }
    if (status === 429) {
      return new ImzalaRateLimitError(message, {
        statusCode: status,
        body,
        code,
        retryAfter: extractRetryAfter(body, headers),
        rateLimit: extractRateLimitInfo(headers),
        cause: err,
      });
    }
    if (status === 422) {
      return new ImzalaValidationError(message, { statusCode: status, body, code, cause: err });
    }
    return new ImzalaError(message, { statusCode: status, body, code, cause: err });
  }

  if (err instanceof ImzalaError) return err;
  if (err instanceof Error) return new ImzalaError(err.message, { cause: err });
  return new ImzalaError('Unknown error calling the imzala.org API', { cause: err });
}
