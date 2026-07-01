import axios from 'axios';

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
  readonly code?: string;

  constructor(message: string, options: ImzalaErrorOptions = {}) {
    super(message, options.cause !== undefined ? { cause: options.cause } : undefined);
    this.name = 'ImzalaError';
    this.statusCode = options.statusCode;
    this.body = options.body;
    this.code = options.code;
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

/** Rate limited (429). `retryAfter` is seconds, when the server provided one. */
export class ImzalaRateLimitError extends ImzalaError {
  readonly retryAfter?: number;

  constructor(message: string, options: ImzalaErrorOptions & { retryAfter?: number } = {}) {
    super(message, options);
    this.name = 'ImzalaRateLimitError';
    this.retryAfter = options.retryAfter;
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
 * imzala.org error envelopes are not fully uniform across endpoints: most
 * are `{success:false, error:"<code>", message:"<text>"}`, but some (e.g.
 * the reminders 429) nest a `{code, message, retry_after_seconds}` object
 * under `error` instead of a plain string. These helpers handle both shapes.
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

export function extractErrorCode(body: unknown): string | undefined {
  const b = asRecord(body);
  if (!b) return undefined;
  if (typeof b.error === 'string') return b.error;
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
  }
  return undefined;
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
