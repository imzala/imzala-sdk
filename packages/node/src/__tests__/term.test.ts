import http from 'node:http';
import type { AddressInfo } from 'node:net';
import { afterEach, describe, expect, it } from 'vitest';
import { Imzala } from '../index';
import { ImzalaError, ImzalaRateLimitError } from '../errors';

/**
 * Contract term tracking and archive helpers, checked on the wire against a
 * local HTTP server: path slots, the partial-update body (null is sent, an
 * omitted key is not), the archive filter on list, and that none of these
 * writes is retried.
 */

interface Captured {
  method: string;
  url: string;
  body: string;
}

type Reply = { status: number; body: unknown; headers?: Record<string, string> };

const servers: http.Server[] = [];

afterEach(async () => {
  await Promise.all(servers.splice(0).map((s) => new Promise((resolve) => s.close(resolve))));
});

async function startServer(replies: Reply[]): Promise<{ baseUrl: string; calls: Captured[] }> {
  const calls: Captured[] = [];
  const server = http.createServer((req, res) => {
    const chunks: Buffer[] = [];
    req.on('data', (c) => chunks.push(c));
    req.on('end', () => {
      calls.push({ method: req.method ?? '', url: req.url ?? '', body: Buffer.concat(chunks).toString('utf8') });
      const reply = replies[Math.min(calls.length - 1, replies.length - 1)];
      res.writeHead(reply.status, { 'content-type': 'application/json', ...(reply.headers ?? {}) });
      res.end(JSON.stringify(reply.body));
    });
  });
  servers.push(server);
  await new Promise<void>((resolve) => server.listen(0, '127.0.0.1', resolve));
  const { port } = server.address() as AddressInfo;
  return { baseUrl: `http://127.0.0.1:${port}`, calls };
}

const client = (baseUrl: string) => new Imzala({ apiKey: 'imz_test', baseUrl, maxRetries: 2, retryBaseDelayMs: 1 });

const DEMAND = '11111111-1111-4111-8111-111111111111';
const ok = (data: unknown): Reply => ({ status: 200, body: { success: true, data } });
const rateLimited: Reply = {
  status: 429,
  body: { success: false, error: 'Çok fazla istek', code: 'RATE_LIMIT_EXCEEDED' },
  headers: { 'retry-after': '0' },
};

const term = {
  start_mode: 'ON_COMPLETION',
  start_date: null,
  duration_months: 12,
  fixed_end_date: null,
  end_date: null,
  end_date_signed: null,
  renewal_type: 'AUTO_RENEW',
  renewal_period_months: 12,
  notice_days: 30,
  notice_deadline: null,
  reminder_offsets: [30, 7],
  notify_counterparty: false,
  state: 'UNTRACKED',
  days_left: null,
  renewal_stopped_at: null,
};

describe('demands.updateTerm', () => {
  it('PATCHes the term path with only the sent keys, null included', async () => {
    const srv = await startServer([ok({ term })]);
    const result = await client(srv.baseUrl).demands.updateTerm(DEMAND, {
      term_start_mode: 'ON_COMPLETION',
      term_duration_months: 12,
      renewal_type: 'AUTO_RENEW',
      notice_days: null,
    });
    expect(result).toEqual({ term });
    expect(srv.calls[0].method).toBe('PATCH');
    expect(srv.calls[0].url).toBe(`/api/v1/demands/${DEMAND}/term`);
    expect(JSON.parse(srv.calls[0].body)).toEqual({
      term_start_mode: 'ON_COMPLETION',
      term_duration_months: 12,
      renewal_type: 'AUTO_RENEW',
      notice_days: null,
    });
  });

  it('surfaces TERM_INVALID as a typed error and keeps the rejected field in the body', async () => {
    const srv = await startServer([
      { status: 400, body: { success: false, error: 'x', code: 'TERM_INVALID', field: 'term_fixed_end_date' } },
    ]);
    const err = await client(srv.baseUrl)
      .demands.updateTerm(DEMAND, { term_fixed_end_date: '2027-01-31', term_duration_months: 12 })
      .catch((e) => e);
    expect(err).toBeInstanceOf(ImzalaError);
    expect(err.code).toBe('TERM_INVALID');
    expect((err.body as { field?: string }).field).toBe('term_fixed_end_date');
  });
});

describe('demands.archive / unarchive', () => {
  it('archive POSTs to the archive path and unwraps archived_at', async () => {
    const srv = await startServer([ok({ archived_at: '2026-09-28T09:00:00.000Z' })]);
    const result = await client(srv.baseUrl).demands.archive(DEMAND);
    expect(result).toEqual({ archived_at: '2026-09-28T09:00:00.000Z' });
    expect(srv.calls[0].method).toBe('POST');
    expect(srv.calls[0].url).toBe(`/api/v1/demands/${DEMAND}/archive`);
  });

  it('unarchive POSTs to the unarchive path', async () => {
    const srv = await startServer([ok({ archived_at: null })]);
    const result = await client(srv.baseUrl).demands.unarchive(DEMAND);
    expect(result).toEqual({ archived_at: null });
    expect(srv.calls[0].method).toBe('POST');
    expect(srv.calls[0].url).toBe(`/api/v1/demands/${DEMAND}/unarchive`);
  });

  it.each(['DEMAND_NOT_ARCHIVABLE', 'DEMAND_REJECTED_CANCEL_FIRST'])('archive surfaces %s', async (code) => {
    const srv = await startServer([{ status: 409, body: { success: false, error: 'x', code } }]);
    const err = await client(srv.baseUrl).demands.archive(DEMAND).catch((e) => e);
    expect(err).toBeInstanceOf(ImzalaError);
    expect(err.code).toBe(code);
    expect(srv.calls).toHaveLength(1);
  });

  it('delete of an archived demand surfaces DEMAND_ARCHIVED', async () => {
    const srv = await startServer([{ status: 409, body: { success: false, error: 'x', code: 'DEMAND_ARCHIVED' } }]);
    const err = await client(srv.baseUrl).demands.delete(DEMAND).catch((e) => e);
    expect(err.code).toBe('DEMAND_ARCHIVED');
  });
});

describe('demands.list archive filter', () => {
  it('sends archived as a query parameter', async () => {
    const srv = await startServer([ok({ demands: [], pagination: { page: 1, limit: 20, total: 0 } })]);
    await client(srv.baseUrl).demands.list({ archived: 'exclude' });
    expect(new URL(srv.calls[0].url, 'http://x').searchParams.get('archived')).toBe('exclude');
  });

  it('leaves archived out when it is not set, so the server default applies', async () => {
    const srv = await startServer([ok({ demands: [], pagination: { page: 1, limit: 20, total: 0 } })]);
    await client(srv.baseUrl).demands.list();
    expect(new URL(srv.calls[0].url, 'http://x').searchParams.has('archived')).toBe(false);
  });
});

describe('term and archive writes are never retried', () => {
  it.each([
    ['updateTerm', (c: Imzala) => c.demands.updateTerm(DEMAND, { notice_days: 30 })],
    ['archive', (c: Imzala) => c.demands.archive(DEMAND)],
    ['unarchive', (c: Imzala) => c.demands.unarchive(DEMAND)],
  ])('%s: a 429 is thrown after exactly one request', async (_name, call) => {
    const srv = await startServer([rateLimited, ok({})]);
    await expect(call(client(srv.baseUrl))).rejects.toBeInstanceOf(ImzalaRateLimitError);
    expect(srv.calls).toHaveLength(1);
  });
});
