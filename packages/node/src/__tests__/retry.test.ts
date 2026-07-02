import { afterEach, describe, expect, it, vi } from 'vitest';
import { DemandsApi, TemplatesApi } from '../../generated/api';
import { Imzala } from '../index';
import { ImzalaError, ImzalaRateLimitError } from '../errors';

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
