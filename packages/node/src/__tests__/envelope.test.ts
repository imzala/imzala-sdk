import http from 'node:http';
import type { AddressInfo } from 'node:net';
import { afterEach, describe, expect, it } from 'vitest';
import { Imzala } from '../index';
import { ImzalaError, ImzalaRateLimitError, ImzalaValidationError } from '../errors';

/**
 * These tests run the real generated client and axios against a local HTTP
 * server, so they check what actually goes over the wire: the path slots, the
 * multipart field that carries the idempotency key, and how a 409 replay body
 * is read back.
 */

interface Captured {
  method: string;
  url: string;
  headers: http.IncomingHttpHeaders;
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
      calls.push({ method: req.method ?? '', url: req.url ?? '', headers: req.headers, body: Buffer.concat(chunks).toString('utf8') });
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
const DOC = '22222222-2222-4222-8222-222222222222';
const DOC_B = '33333333-3333-4333-8333-333333333333';
const PARTY = '44444444-4444-4444-8444-444444444444';

const pdf = { content: Buffer.from('%PDF-1.7 test'), filename: 'kira.pdf', contentType: 'application/pdf' };
const doc = { id: DOC, order: 1, title: 'Kira sözleşmesi', doc_kind: 'CONTRACT' };
const ok = (data: unknown): Reply => ({ status: 200, body: { success: true, data } });
const rateLimited: Reply = {
  status: 429,
  body: { success: false, error: 'Çok fazla istek', code: 'RATE_LIMIT_EXCEEDED' },
  headers: { 'retry-after': '0' },
};
const disabled: Reply = {
  status: 409,
  body: { success: false, error: 'Kapalı', code: 'ENVELOPE_MULTI_DOC_DISABLED' },
};

/** Value of one multipart form field in a captured request body. */
function formField(body: string, name: string): string | undefined {
  const match = body.match(new RegExp(`name="${name}"\\r\\n(?:[^\\r\\n]+\\r\\n)*\\r\\n([\\s\\S]*?)\\r\\n--`));
  return match?.[1];
}

describe('demands.documents: path slots and bodies on the wire', () => {
  it('list sends demandId in the path and view as a query parameter, and unwraps', async () => {
    const srv = await startServer([ok({ documents: [doc] })]);
    const result = await client(srv.baseUrl).demands.documents.list(DEMAND, { view: 'wizard' });
    expect(result).toEqual({ documents: [doc] });
    expect(srv.calls[0].method).toBe('GET');
    expect(srv.calls[0].url).toBe(`/api/v1/demands/${DEMAND}/documents?view=wizard`);
  });

  it('create posts the JSON body', async () => {
    const srv = await startServer([{ status: 201, body: { success: true, data: { document: doc } } }]);
    const result = await client(srv.baseUrl).demands.documents.create(DEMAND, {
      title: 'KVKK aydınlatma',
      doc_kind: 'KVKK_NOTICE',
      is_required: false,
      signature_required: false,
    });
    expect(result).toEqual({ document: doc });
    expect(srv.calls[0].method).toBe('POST');
    expect(srv.calls[0].url).toBe(`/api/v1/demands/${DEMAND}/documents`);
    expect(JSON.parse(srv.calls[0].body)).toEqual({
      title: 'KVKK aydınlatma',
      doc_kind: 'KVKK_NOTICE',
      is_required: false,
      signature_required: false,
    });
  });

  it('update keeps demandId and docId in their own slots', async () => {
    const srv = await startServer([ok({ document: doc })]);
    await client(srv.baseUrl).demands.documents.update(DEMAND, DOC, { title: 'Yeni başlık' });
    expect(srv.calls[0].method).toBe('PATCH');
    expect(srv.calls[0].url).toBe(`/api/v1/demands/${DEMAND}/documents/${DOC}`);
    expect(JSON.parse(srv.calls[0].body)).toEqual({ title: 'Yeni başlık' });
  });

  it('delete keeps demandId and docId in their own slots', async () => {
    const srv = await startServer([ok({ id: DOC, deleted: true })]);
    const result = await client(srv.baseUrl).demands.documents.delete(DEMAND, DOC);
    expect(result).toEqual({ id: DOC, deleted: true });
    expect(srv.calls[0].method).toBe('DELETE');
    expect(srv.calls[0].url).toBe(`/api/v1/demands/${DEMAND}/documents/${DOC}`);
  });

  it('reorder sends document_ids in the given order', async () => {
    const srv = await startServer([ok({ documents: [doc] })]);
    await client(srv.baseUrl).demands.documents.reorder(DEMAND, [DOC_B, DOC]);
    expect(srv.calls[0].method).toBe('PUT');
    expect(srv.calls[0].url).toBe(`/api/v1/demands/${DEMAND}/documents/order`);
    expect(JSON.parse(srv.calls[0].body)).toEqual({ document_ids: [DOC_B, DOC] });
  });

  it('setAssignments keeps demandId and docId apart and sends party_ids', async () => {
    const srv = await startServer([ok({ document: doc })]);
    await client(srv.baseUrl).demands.documents.setAssignments(DEMAND, DOC, [PARTY]);
    expect(srv.calls[0].method).toBe('PUT');
    expect(srv.calls[0].url).toBe(`/api/v1/demands/${DEMAND}/documents/${DOC}/assignments`);
    expect(JSON.parse(srv.calls[0].body)).toEqual({ party_ids: [PARTY] });
  });

  it('dispatch sends no send_invitations field by default', async () => {
    const srv = await startServer([ok({ demand_id: DEMAND, status: 'PENDING', dispatched: true })]);
    const result = await client(srv.baseUrl).demands.dispatch(DEMAND);
    expect(result).toMatchObject({ demand_id: DEMAND, dispatched: true });
    expect(srv.calls[0].method).toBe('POST');
    expect(srv.calls[0].url).toBe(`/api/v1/demands/${DEMAND}/dispatch`);
    const sent = srv.calls[0].body ? JSON.parse(srv.calls[0].body) : {};
    expect(sent).not.toHaveProperty('send_invitations');
  });

  it('dispatch forwards a boolean or a string sendInvitations value unchanged', async () => {
    const srv = await startServer([ok({ dispatched: true })]);
    await client(srv.baseUrl).demands.dispatch(DEMAND, { sendInvitations: false });
    await client(srv.baseUrl).demands.dispatch(DEMAND, { sendInvitations: 'email' });
    expect(JSON.parse(srv.calls[0].body)).toEqual({ send_invitations: false });
    expect(JSON.parse(srv.calls[1].body)).toEqual({ send_invitations: 'email' });
  });
});

describe('demands.documents.upload', () => {
  it('sends the idempotency key as the idempotency_key body field, not as a header', async () => {
    const srv = await startServer([ok({ document: doc })]);
    const result = await client(srv.baseUrl).demands.documents.upload(DEMAND, {
      file: pdf,
      title: 'Kira sözleşmesi',
      idempotencyKey: 'siparis-42',
      docKind: 'CONTRACT',
      isRequired: false,
    });
    expect(result).toEqual({ document: doc });
    const call = srv.calls[0];
    expect(call.url).toBe(`/api/v1/demands/${DEMAND}/documents/upload`);
    expect(call.headers['idempotency-key']).toBeUndefined();
    expect(formField(call.body, 'idempotency_key')).toBe('siparis-42');
    expect(formField(call.body, 'title')).toBe('Kira sözleşmesi');
    expect(formField(call.body, 'doc_kind')).toBe('CONTRACT');
    expect(formField(call.body, 'is_required')).toBe('false');
    expect(call.body).toContain('filename="kira.pdf"');
    expect(call.body).toContain('%PDF-1.7 test');
  });

  it.each([
    ['missing', undefined],
    ['empty', ''],
    ['non-ASCII', 'sipariş-1'],
    ['line break', 'a\r\nX-Evil: 1'],
  ])('rejects a %s idempotency key locally without sending anything', async (_label, key) => {
    const srv = await startServer([ok({ document: doc })]);
    await expect(
      client(srv.baseUrl).demands.documents.upload(DEMAND, { file: pdf, title: 'T', idempotencyKey: key as any }),
    ).rejects.toBeInstanceOf(ImzalaValidationError);
    expect(srv.calls).toHaveLength(0);
  });

  it('retries once after a 429 and sends the file again on the second attempt', async () => {
    const srv = await startServer([rateLimited, ok({ document: doc })]);
    const result = await client(srv.baseUrl).demands.documents.upload(DEMAND, { file: pdf, title: 'T', idempotencyKey: 'k-1' });
    expect(result).toEqual({ document: doc });
    expect(srv.calls).toHaveLength(2);
    expect(srv.calls[1].body).toContain('%PDF-1.7 test');
    expect(formField(srv.calls[1].body, 'idempotency_key')).toBe('k-1');
  });

  it('does not retry when Retry-After exceeds the 60 second cap', async () => {
    const srv = await startServer([{ ...rateLimited, headers: { 'retry-after': '61' } }]);
    await expect(
      client(srv.baseUrl).demands.documents.upload(DEMAND, { file: pdf, title: 'T', idempotencyKey: 'k-1' }),
    ).rejects.toBeInstanceOf(ImzalaRateLimitError);
    expect(srv.calls).toHaveLength(1);
  });

  it('returns the earlier document on a 409 IDEMPOTENT_REPLAY instead of throwing', async () => {
    const srv = await startServer([
      {
        status: 409,
        body: {
          success: false,
          error: 'Bu belge bu anahtarla zaten yüklendi',
          code: 'IDEMPOTENT_REPLAY',
          data: { document: doc },
        },
      },
    ]);
    const result = await client(srv.baseUrl).demands.documents.upload(DEMAND, { file: pdf, title: 'T', idempotencyKey: 'k-1' });
    expect(result).toEqual({ document: doc });
    expect(srv.calls).toHaveLength(1);
  });

  it('treats a replay that follows a 429 retry as success too', async () => {
    const srv = await startServer([
      rateLimited,
      { status: 409, body: { success: false, error: 'x', code: 'IDEMPOTENT_REPLAY', data: { document: doc } } },
    ]);
    const result = await client(srv.baseUrl).demands.documents.upload(DEMAND, { file: pdf, title: 'T', idempotencyKey: 'k-1' });
    expect(result).toEqual({ document: doc });
    expect(srv.calls).toHaveLength(2);
  });

  it('throws any other 409 code, for example SIGNING_ALREADY_STARTED', async () => {
    const srv = await startServer([
      { status: 409, body: { success: false, error: 'İmza başladı', code: 'SIGNING_ALREADY_STARTED', data: { document: doc } } },
    ]);
    const err = await client(srv.baseUrl)
      .demands.documents.upload(DEMAND, { file: pdf, title: 'T', idempotencyKey: 'k-1' })
      .catch((e) => e);
    expect(err).toBeInstanceOf(ImzalaError);
    expect(err.code).toBe('SIGNING_ALREADY_STARTED');
    expect(err.statusCode).toBe(409);
  });

  it('throws ENVELOPE_MULTI_DOC_DISABLED', async () => {
    const srv = await startServer([disabled]);
    const err = await client(srv.baseUrl)
      .demands.documents.upload(DEMAND, { file: pdf, title: 'T', idempotencyKey: 'k-1' })
      .catch((e) => e);
    expect(err).toBeInstanceOf(ImzalaError);
    expect(err.code).toBe('ENVELOPE_MULTI_DOC_DISABLED');
  });
});

describe('demands.documents: feature switched off', () => {
  it('list throws ENVELOPE_MULTI_DOC_DISABLED and does not return an empty list', async () => {
    const srv = await startServer([disabled]);
    const err = await client(srv.baseUrl).demands.documents.list(DEMAND).catch((e) => e);
    expect(err).toBeInstanceOf(ImzalaError);
    expect(err.code).toBe('ENVELOPE_MULTI_DOC_DISABLED');
    expect(srv.calls).toHaveLength(1);
  });

  it.each([
    ['create', (c: Imzala) => c.demands.documents.create(DEMAND, { title: 'T' })],
    ['update', (c: Imzala) => c.demands.documents.update(DEMAND, DOC, { title: 'T' })],
    ['delete', (c: Imzala) => c.demands.documents.delete(DEMAND, DOC)],
    ['reorder', (c: Imzala) => c.demands.documents.reorder(DEMAND, [DOC])],
    ['setAssignments', (c: Imzala) => c.demands.documents.setAssignments(DEMAND, DOC, [PARTY])],
  ])('%s throws ENVELOPE_MULTI_DOC_DISABLED', async (_name, call) => {
    const srv = await startServer([disabled]);
    const err = await call(client(srv.baseUrl)).catch((e) => e);
    expect(err).toBeInstanceOf(ImzalaError);
    expect(err.code).toBe('ENVELOPE_MULTI_DOC_DISABLED');
  });
});

describe('demands.updateStamp', () => {
  it('PATCHes one stamp item with demandId and itemId in their own slots', async () => {
    const updated = { item_id: 42, source: 'INLINE', stamp_data: { companyName: 'Örnek Ltd.' } };
    const srv = await startServer([ok(updated)]);
    const result = await client(srv.baseUrl).demands.updateStamp(DEMAND, 42, {
      stamp_data: { companyName: 'Örnek Ltd.', companyPhone: '+905551112233', taxOffice: null },
      document_id: DOC,
    });
    expect(result).toEqual(updated);
    expect(srv.calls[0].method).toBe('PATCH');
    expect(srv.calls[0].url).toBe(`/api/v1/demands/${DEMAND}/items/42/stamp`);
    expect(JSON.parse(srv.calls[0].body)).toEqual({
      stamp_data: { companyName: 'Örnek Ltd.', companyPhone: '+905551112233', taxOffice: null },
      document_id: DOC,
    });
  });

  it('surfaces DEMAND_PARTIALLY_SIGNED as a typed error', async () => {
    const srv = await startServer([{ status: 409, body: { success: false, error: 'x', code: 'DEMAND_PARTIALLY_SIGNED' } }]);
    const err = await client(srv.baseUrl).demands.updateStamp(DEMAND, 42, { stamp_data: { companyName: 'X' } }).catch((e) => e);
    expect(err).toBeInstanceOf(ImzalaError);
    expect(err.code).toBe('DEMAND_PARTIALLY_SIGNED');
  });
});

describe('writes without an idempotency key are never retried', () => {
  it.each([
    ['documents.create', (c: Imzala) => c.demands.documents.create(DEMAND, { title: 'T' })],
    ['documents.update', (c: Imzala) => c.demands.documents.update(DEMAND, DOC, { title: 'T' })],
    ['documents.delete', (c: Imzala) => c.demands.documents.delete(DEMAND, DOC)],
    ['documents.reorder', (c: Imzala) => c.demands.documents.reorder(DEMAND, [DOC])],
    ['documents.setAssignments', (c: Imzala) => c.demands.documents.setAssignments(DEMAND, DOC, [PARTY])],
    ['dispatch', (c: Imzala) => c.demands.dispatch(DEMAND)],
    ['updateStamp', (c: Imzala) => c.demands.updateStamp(DEMAND, 42, { stamp_data: { companyName: 'X' } })],
  ])('%s: a 429 is thrown after exactly one request', async (_name, call) => {
    const srv = await startServer([rateLimited, ok({})]);
    await expect(call(client(srv.baseUrl))).rejects.toBeInstanceOf(ImzalaRateLimitError);
    expect(srv.calls).toHaveLength(1);
  });

  it('list (a GET) is retried after a 429', async () => {
    const srv = await startServer([rateLimited, ok({ documents: [] })]);
    await expect(client(srv.baseUrl).demands.documents.list(DEMAND)).resolves.toEqual({ documents: [] });
    expect(srv.calls).toHaveLength(2);
  });
});

describe('demands.dispatch errors', () => {
  it.each(['DISPATCH_NO_PARTIES', 'DISPATCH_TOO_MANY', 'QES_NOT_SUPPORTED_MULTI_DOCUMENT'])('throws %s', async (code) => {
    const srv = await startServer([{ status: 409, body: { success: false, error: 'x', code } }]);
    const err = await client(srv.baseUrl).demands.dispatch(DEMAND).catch((e) => e);
    expect(err).toBeInstanceOf(ImzalaError);
    expect(err.code).toBe(code);
    expect(srv.calls).toHaveLength(1);
  });
});
