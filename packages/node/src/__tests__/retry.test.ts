import { afterEach, describe, expect, it, vi } from 'vitest';
import { DemandsApi, TemplatesApi } from '../../generated/api';
import { Imzala } from '../index';
import { ImzalaError, ImzalaRateLimitError } from '../errors';
import { unwrapIdempotentWrite } from '../http';

afterEach(() => {
  vi.restoreAllMocks();
});

/** Shaped like an axios error — enough for `axios.isAxiosError()` plus what `mapAxiosError` reads off it. Mirrors errors.test.ts's fixture. */
function fakeAxiosError(status: number, data: unknown = { success: false }) {
  return {
    isAxiosError: true,
    message: `Request failed with status code ${status}`,
    response: { status, data, headers: {} },
  };
}

// Keep retries near-instant in tests — real prod default is 300ms.
const FAST_RETRY = { maxRetries: 2, retryBaseDelayMs: 1 };

describe('safe auto-retry — GET requests', () => {
  it('retries a GET twice on 429 then succeeds on the 3rd attempt (call count = 3)', async () => {
    const spy = vi
      .spyOn(TemplatesApi.prototype, 'apiV1TemplatesGet')
      .mockRejectedValueOnce(fakeAxiosError(429))
      .mockRejectedValueOnce(fakeAxiosError(429))
      .mockResolvedValueOnce({
        data: { success: true, data: { templates: [{ id: 't1' }], total: 1, page: 1, limit: 10 } },
        status: 200,
      } as any);

    const imzala = new Imzala({ apiKey: 'imz_test', ...FAST_RETRY });
    const result = await imzala.templates.list();

    expect(spy).toHaveBeenCalledTimes(3);
    expect(result.templates).toEqual([{ id: 't1' }]);
  });

  it('retries a GET on 5xx (server error) and succeeds', async () => {
    const spy = vi
      .spyOn(DemandsApi.prototype, 'apiV1DemandsIdGet')
      .mockRejectedValueOnce(fakeAxiosError(503))
      .mockResolvedValueOnce({
        data: { success: true, data: { id: 'd1', status: 'PENDING' } },
        status: 200,
      } as any);

    const imzala = new Imzala({ apiKey: 'imz_test', ...FAST_RETRY });
    const result = await imzala.demands.get('d1');

    expect(spy).toHaveBeenCalledTimes(2);
    expect(result).toEqual({ id: 'd1', status: 'PENDING' });
  });

  it('does NOT retry a GET on a non-429 4xx (e.g. 404) — thrown immediately', async () => {
    const spy = vi
      .spyOn(TemplatesApi.prototype, 'apiV1TemplatesIdGet')
      .mockRejectedValue(fakeAxiosError(404, { success: false, error: 'TEMPLATE_NOT_FOUND' }));

    const imzala = new Imzala({ apiKey: 'imz_test', ...FAST_RETRY });

    await expect(imzala.templates.get('missing')).rejects.toBeInstanceOf(ImzalaError);
    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('maxRetries: 0 disables retry entirely, even on a retryable 429', async () => {
    const spy = vi.spyOn(TemplatesApi.prototype, 'apiV1TemplatesGet').mockRejectedValue(fakeAxiosError(429));

    const imzala = new Imzala({ apiKey: 'imz_test', maxRetries: 0, retryBaseDelayMs: 1 });

    await expect(imzala.templates.list()).rejects.toBeInstanceOf(ImzalaRateLimitError);
    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('exhausts retries and throws the typed error when every attempt fails', async () => {
    const spy = vi.spyOn(TemplatesApi.prototype, 'apiV1TemplatesGet').mockRejectedValue(fakeAxiosError(503));

    const imzala = new Imzala({ apiKey: 'imz_test', maxRetries: 2, retryBaseDelayMs: 1 });

    await expect(imzala.templates.list()).rejects.toBeInstanceOf(ImzalaError);
    // initial attempt + 2 retries = 3 calls total
    expect(spy).toHaveBeenCalledTimes(3);
  });
});

describe('safe auto-retry — SAFETY: writes are never retried', () => {
  it('a POST that returns 429 throws immediately — NO retry (call count = 1)', async () => {
    const spy = vi
      .spyOn(DemandsApi.prototype, 'apiV1DemandsPost')
      .mockRejectedValue(fakeAxiosError(429, { success: false, error: 'RATE_LIMITED' }));

    const imzala = new Imzala({ apiKey: 'imz_test', ...FAST_RETRY });

    await expect(
      imzala.demands.create({ template_id: 't1', party_mapping: [] } as any),
    ).rejects.toBeInstanceOf(ImzalaRateLimitError);
    // A retried demands.create() POST would create a DUPLICATE demand — must never retry.
    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('a POST that returns 503 (server error) also throws immediately — NO retry', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsPost').mockRejectedValue(fakeAxiosError(503));

    const imzala = new Imzala({ apiKey: 'imz_test', ...FAST_RETRY });

    await expect(
      imzala.demands.create({ template_id: 't1', party_mapping: [] } as any),
    ).rejects.toBeInstanceOf(ImzalaError);
    expect(spy).toHaveBeenCalledTimes(1);
  });
});

describe('unwrapIdempotentWrite: one safe retry for writes that carry an Idempotency-Key', () => {
  function rateLimited(retryAfterSeconds: number) {
    return {
      isAxiosError: true,
      message: 'Request failed with status code 429',
      response: {
        status: 429,
        data: { success: false, error: 'Çok fazla istek', code: 'RATE_LIMIT_EXCEEDED', retry_after_seconds: retryAfterSeconds },
        headers: { 'retry-after': String(retryAfterSeconds) },
      },
    };
  }
  const ok = (data: unknown) => ({ status: 200, data: { success: true, data } }) as any;

  it('a write WITHOUT an Idempotency-Key is never retried on 429', async () => {
    const call = vi.fn().mockRejectedValue(rateLimited(0));
    await expect(unwrapIdempotentWrite(call, { retryBaseDelayMs: 1 })).rejects.toBeInstanceOf(ImzalaRateLimitError);
    expect(call).toHaveBeenCalledTimes(1);
  });

  it('a write WITH an Idempotency-Key is retried exactly once on 429', async () => {
    const call = vi.fn().mockRejectedValueOnce(rateLimited(0)).mockResolvedValueOnce(ok({ id: 'ok' }));
    await expect(unwrapIdempotentWrite(call, { idempotencyKey: 'k-1', retryBaseDelayMs: 1 })).resolves.toEqual({ id: 'ok' });
    expect(call).toHaveBeenCalledTimes(2);
  });

  it('a second 429 is thrown, not retried again', async () => {
    const call = vi.fn().mockRejectedValue(rateLimited(0));
    await expect(unwrapIdempotentWrite(call, { idempotencyKey: 'k-2', retryBaseDelayMs: 1 })).rejects.toBeInstanceOf(
      ImzalaRateLimitError,
    );
    expect(call).toHaveBeenCalledTimes(2);
  });

  it('non-429 errors are not retried, even 5xx', async () => {
    const conflict = vi.fn().mockRejectedValue(fakeAxiosError(409, { success: false, error: 'x', code: 'TEMPLATE_IN_USE' }));
    await expect(unwrapIdempotentWrite(conflict, { idempotencyKey: 'k-3', retryBaseDelayMs: 1 })).rejects.toMatchObject({
      code: 'TEMPLATE_IN_USE',
    });
    expect(conflict).toHaveBeenCalledTimes(1);

    const serverError = vi.fn().mockRejectedValue(fakeAxiosError(503));
    await expect(unwrapIdempotentWrite(serverError, { idempotencyKey: 'k-4', retryBaseDelayMs: 1 })).rejects.toBeInstanceOf(
      ImzalaError,
    );
    expect(serverError).toHaveBeenCalledTimes(1);
  });

  it('throws instead of waiting when Retry-After exceeds the cap', async () => {
    const call = vi.fn().mockRejectedValue(rateLimited(3600));
    await expect(unwrapIdempotentWrite(call, { idempotencyKey: 'k-6', retryBaseDelayMs: 1 })).rejects.toBeInstanceOf(
      ImzalaRateLimitError,
    );
    expect(call).toHaveBeenCalledTimes(1);

    const retried = vi.fn().mockRejectedValueOnce(rateLimited(2)).mockResolvedValueOnce(ok({ id: 'x' }));
    await expect(
      unwrapIdempotentWrite(retried, { idempotencyKey: 'k-7', retryBaseDelayMs: 1, maxWaitMs: 1000 }),
    ).rejects.toBeInstanceOf(ImzalaRateLimitError);
    expect(retried).toHaveBeenCalledTimes(1);
  });

  it('waits for Retry-After before the retry', async () => {
    vi.useFakeTimers();
    try {
      const call = vi.fn().mockRejectedValueOnce(rateLimited(2)).mockResolvedValueOnce(ok({ id: 'later' }));
      const pending = unwrapIdempotentWrite(call, { idempotencyKey: 'k-5', retryBaseDelayMs: 1 });
      await vi.advanceTimersByTimeAsync(1999);
      expect(call).toHaveBeenCalledTimes(1);
      await vi.advanceTimersByTimeAsync(1);
      await expect(pending).resolves.toEqual({ id: 'later' });
      expect(call).toHaveBeenCalledTimes(2);
    } finally {
      vi.useRealTimers();
    }
  });
});
