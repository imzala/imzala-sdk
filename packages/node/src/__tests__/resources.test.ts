import { afterEach, describe, expect, it, vi } from 'vitest';
import { ContactsApi, DemandsApi, ReportsApi, TemplatesApi, TimestampsApi } from '../../generated/api';
import { Imzala } from '../index';
import { ImzalaError, ImzalaRateLimitError } from '../errors';

afterEach(() => {
  vi.restoreAllMocks();
});

const ok = (data: unknown, status = 200) => ({ data: { success: true, data }, status }) as any;

function rateLimited() {
  return {
    isAxiosError: true,
    message: 'Request failed with status code 429',
    response: {
      status: 429,
      data: { success: false, error: 'Çok fazla istek', code: 'RATE_LIMIT_EXCEEDED', retry_after_seconds: 0 },
      headers: { 'retry-after': '0' },
    },
  };
}

const client = () => new Imzala({ apiKey: 'imz_test', maxRetries: 0, retryBaseDelayMs: 1 });
const pdf = { content: Buffer.from('%PDF-1.7'), filename: 'sozlesme.pdf', contentType: 'application/pdf' };

describe('fieldTemplates', () => {
  it('list forwards paging and unwraps the envelope', async () => {
    const spy = vi
      .spyOn(TemplatesApi.prototype, 'apiV1FieldTemplatesGet')
      .mockResolvedValue(ok({ field_templates: [{ id: 'ft1' }], total: 1, page: 2, limit: 10 }));
    await expect(client().fieldTemplates.list({ page: 2, limit: 10 })).resolves.toMatchObject({ total: 1 });
    expect(spy.mock.calls[0][0]).toEqual({ page: 2, limit: 10 });
  });

  it('get unwraps the detail', async () => {
    vi.spyOn(TemplatesApi.prototype, 'apiV1FieldTemplatesIdGet').mockResolvedValue(ok({ id: 'ft1', name: 'Kira' }));
    await expect(client().fieldTemplates.get('ft1')).resolves.toEqual({ id: 'ft1', name: 'Kira' });
  });

  it('previewLayout sends files and on_anchor_miss', async () => {
    const spy = vi
      .spyOn(DemandsApi.prototype, 'apiV1FieldTemplatesIdPreviewLayoutPost')
      .mockResolvedValue(ok({ placements_summary: { total: 2 } }));
    await client().fieldTemplates.previewLayout('ft1', { files: [pdf], onAnchorMiss: 'drop' });
    const args = spy.mock.calls[0][0] as any;
    expect(args.id).toBe('ft1');
    expect(args.files).toHaveLength(1);
    expect(args.files[0].name).toBe('sozlesme.pdf');
    expect(args.onAnchorMiss).toBe('drop');
  });
});

describe('contacts', () => {
  it('list forwards filters', async () => {
    const spy = vi
      .spyOn(ContactsApi.prototype, 'apiV1ContactsGet')
      .mockResolvedValue(ok({ contacts: [], total: 0, page: 1, limit: 20 }));
    await client().contacts.list({ q: 'Ayşe', archived: false, companyId: 'c1', sort: 'createdAt:desc' });
    expect(spy.mock.calls[0][0]).toEqual({
      q: 'Ayşe',
      archived: false,
      companyId: 'c1',
      sort: 'createdAt:desc',
      page: undefined,
      limit: undefined,
    });
  });

  it('listAll walks every page', async () => {
    const spy = vi
      .spyOn(ContactsApi.prototype, 'apiV1ContactsGet')
      .mockResolvedValueOnce(ok({ contacts: [{ id: 'a' }, { id: 'b' }], total: 3, page: 1, limit: 2 }))
      .mockResolvedValueOnce(ok({ contacts: [{ id: 'c' }], total: 3, page: 2, limit: 2 }));
    const ids: string[] = [];
    for await (const c of client().contacts.listAll({ limit: 2 })) ids.push(c.id as string);
    expect(ids).toEqual(['a', 'b', 'c']);
    expect(spy).toHaveBeenCalledTimes(2);
  });

  it('create sends the body and is NOT retried on 429 (the endpoint has no idempotency key)', async () => {
    const spy = vi.spyOn(ContactsApi.prototype, 'apiV1ContactsPost').mockRejectedValue(rateLimited());
    await expect(client().contacts.create({ first_name: 'Ayşe', last_name: 'Yılmaz', email: 'ayse@example.com' })).rejects.toBeInstanceOf(
      ImzalaRateLimitError,
    );
    expect(spy).toHaveBeenCalledTimes(1);
    expect((spy.mock.calls[0][0] as any).apiV1ContactsPostRequest.email).toBe('ayse@example.com');
  });
});

describe('reports', () => {
  it('get unwraps the counts', async () => {
    vi.spyOn(ReportsApi.prototype, 'apiV1ReportsGet').mockResolvedValue(ok({ contracts: { pending: 2 } }));
    await expect(client().reports.get()).resolves.toEqual({ contracts: { pending: 2 } });
  });
});

describe('timestamps', () => {
  it('list forwards filters and get unwraps one record', async () => {
    const list = vi
      .spyOn(TimestampsApi.prototype, 'apiV1TimestampsGet')
      .mockResolvedValue(ok({ timestamps: [], total: 0, page: 1, limit: 10 }));
    vi.spyOn(TimestampsApi.prototype, 'apiV1TimestampsIdGet').mockResolvedValue(ok({ id: 'ts1' }));
    await client().timestamps.list({ limit: 10, status: 'COMPLETED', from: '2026-01-01' });
    expect(list.mock.calls[0][0]).toMatchObject({ limit: 10, status: 'COMPLETED', from: '2026-01-01' });
    await expect(client().timestamps.get('ts1')).resolves.toEqual({ id: 'ts1' });
  });

  it('create with an idempotencyKey is retried once after 429', async () => {
    const spy = vi
      .spyOn(TimestampsApi.prototype, 'apiV1TimestampsPost')
      .mockRejectedValueOnce(rateLimited())
      .mockResolvedValueOnce(ok({ id: 'ts1' }, 201));
    await expect(client().timestamps.create({ ...pdf, idempotencyKey: 'k-ts' })).resolves.toEqual({ id: 'ts1' });
    expect(spy).toHaveBeenCalledTimes(2);
  });

  it('create without an idempotencyKey is not retried', async () => {
    const spy = vi.spyOn(TimestampsApi.prototype, 'apiV1TimestampsPost').mockRejectedValue(rateLimited());
    await expect(client().timestamps.create(pdf)).rejects.toBeInstanceOf(ImzalaRateLimitError);
    expect(spy).toHaveBeenCalledTimes(1);
  });
});

describe('demands: new write options', () => {
  it('create passes idempotencyKey as the header parameter and retries once after 429', async () => {
    const spy = vi
      .spyOn(DemandsApi.prototype, 'apiV1DemandsPost')
      .mockRejectedValueOnce(rateLimited())
      .mockResolvedValueOnce(ok({ id: 'd1' }, 201));
    await expect(client().demands.create({ template_id: 't1', party_mapping: [] } as any, { idempotencyKey: 'order-42' })).resolves.toEqual({
      id: 'd1',
    });
    expect(spy).toHaveBeenCalledTimes(2);
    expect((spy.mock.calls[0][0] as any).idempotencyKey).toBe('order-42');
  });

  it('create without a key keeps the old single-attempt behaviour', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsPost').mockRejectedValue(rateLimited());
    await expect(client().demands.create({ template_id: 't1', party_mapping: [] } as any)).rejects.toBeInstanceOf(ImzalaRateLimitError);
    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('uploadDocument forwards the new optional fields', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsUploadPost').mockResolvedValue(ok({ id: 'd2' }, 201));
    await client().demands.uploadDocument({
      files: [pdf],
      parties: [{ first_name: 'Ayşe', last_name: 'Yılmaz', email: 'ayse@example.com' }],
      idempotencyKey: 'up-1',
      fieldTemplateId: 'ft1',
      onAnchorMiss: 'block',
      sendInvitations: 'email',
      force: true,
    });
    const args = spy.mock.calls[0][0] as any;
    expect(args).toMatchObject({
      idempotencyKey: 'up-1',
      fieldTemplateId: 'ft1',
      onAnchorMiss: 'block',
      sendInvitations: 'email',
      force: 'true',
    });
  });

  it('uploadDocument with an idempotencyKey is retried once after 429', async () => {
    const spy = vi
      .spyOn(DemandsApi.prototype, 'apiV1DemandsUploadPost')
      .mockRejectedValueOnce(rateLimited())
      .mockResolvedValueOnce(ok({ id: 'd3' }, 201));
    await expect(
      client().demands.uploadDocument({
        files: [pdf],
        parties: [{ first_name: 'Ayşe', last_name: 'Yılmaz', email: 'ayse@example.com' }],
        idempotencyKey: 'up-2',
      }),
    ).resolves.toEqual({ id: 'd3' });
    expect(spy).toHaveBeenCalledTimes(2);
  });

  it('uploadDocument without an idempotencyKey is a single attempt on 429', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsUploadPost').mockRejectedValue(rateLimited());
    await expect(
      client().demands.uploadDocument({ files: [pdf], parties: [{ first_name: 'Ayşe', last_name: 'Yılmaz', email: 'ayse@example.com' }] }),
    ).rejects.toBeInstanceOf(ImzalaRateLimitError);
    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('uploadDocument with a field template accepts template_party_id on parties', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsUploadPost').mockResolvedValue(ok({ id: 'd4' }, 201));
    await client().demands.uploadDocument({
      files: [pdf],
      fieldTemplateId: 'ft1',
      parties: [{ first_name: 'Ayşe', last_name: 'Yılmaz', email: 'ayse@example.com', template_party_id: 'role-1' }],
    });
    expect(JSON.parse((spy.mock.calls[0][0] as any).parties)[0].template_party_id).toBe('role-1');
  });

  it('uploadDocument omits force when not requested', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsUploadPost').mockResolvedValue(ok({ id: 'd2' }, 201));
    await client().demands.uploadDocument({ files: [pdf], parties: [{ first_name: 'Ayşe', last_name: 'Yılmaz', email: 'ayse@example.com' }] });
    expect((spy.mock.calls[0][0] as any).force).toBeUndefined();
  });

  it('createBulk sends the body and is NOT retried on 429 (the endpoint has no idempotency key)', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsBulkPost').mockRejectedValue(rateLimited());
    await expect(client().demands.createBulk({ template_id: 't1', rows: [] } as any)).rejects.toBeInstanceOf(ImzalaRateLimitError);
    expect(spy).toHaveBeenCalledTimes(1);
    expect((spy.mock.calls[0][0] as any).apiV1DemandsBulkPostRequest.template_id).toBe('t1');
  });

  it('getDocumentPdf returns the raw bytes of one envelope document', async () => {
    const spy = vi
      .spyOn(DemandsApi.prototype, 'apiV1DemandsIdBelgeDocumentIdPdfGet')
      .mockResolvedValue({ data: new TextEncoder().encode('%PDF-1.7').buffer, status: 200 } as any);
    const bytes = await client().demands.getDocumentPdf('d1', 'doc1');
    expect(bytes.subarray(0, 5).toString()).toBe('%PDF-');
    expect(spy.mock.calls[0][0]).toEqual({ id: 'd1', documentId: 'doc1' });
    expect(spy.mock.calls[0][1]).toEqual({ responseType: 'arraybuffer' });
  });
});

describe('binary GETs: errors are mapped and 429/5xx retried like other GETs', () => {
  const rateLimited = () => ({
    isAxiosError: true,
    message: 'Request failed with status code 429',
    response: { status: 429, data: { success: false, code: 'RATE_LIMIT_EXCEEDED', retry_after_seconds: 0 }, headers: { 'retry-after': '0' } },
  });

  it('getPdf maps a 404 to ImzalaError instead of leaking the axios error', async () => {
    vi.spyOn(DemandsApi.prototype, 'apiV1DemandsIdPdfGet').mockRejectedValue({
      isAxiosError: true,
      message: 'Request failed with status code 404',
      response: { status: 404, data: { success: false, error: 'Sözleşme bulunamadı', code: 'DEMAND_NOT_FOUND' }, headers: {} },
    });
    await expect(client().demands.getPdf('d1')).rejects.toMatchObject({ name: 'ImzalaError', statusCode: 404, code: 'DEMAND_NOT_FOUND' });
  });

  it('getDocumentPdf retries a 429 and returns the bytes', async () => {
    const spy = vi
      .spyOn(DemandsApi.prototype, 'apiV1DemandsIdBelgeDocumentIdPdfGet')
      .mockRejectedValueOnce(rateLimited())
      .mockResolvedValueOnce({ data: new TextEncoder().encode('%PDF-1.7').buffer, status: 200 } as any);
    const c = new Imzala({ apiKey: 'imz_test', maxRetries: 1, retryBaseDelayMs: 1 });
    const bytes = await c.demands.getDocumentPdf('d1', 'doc1');
    expect(bytes.subarray(0, 5).toString()).toBe('%PDF-');
    expect(spy).toHaveBeenCalledTimes(2);
  });

  it('getCertificate does not retry when maxRetries is 0', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsIdCertificateGet').mockRejectedValue(rateLimited());
    await expect(client().demands.getCertificate('d1')).rejects.toBeInstanceOf(ImzalaRateLimitError);
    expect(spy).toHaveBeenCalledTimes(1);
  });
});

describe('demands: template document selection', () => {
  const partyMapping = [
    { template_party_id: 'role-1', first_name: 'Ayşe', last_name: 'Yılmaz', email: 'ayse@example.com', phone: '+905551112233' },
  ];

  it('create forwards documents.include / documents.exclude untouched', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsPost').mockResolvedValue(ok({ id: 'd1' }, 201));
    await client().demands.create({
      template_id: 't1',
      party_mapping: partyMapping,
      documents: { include: ['doc-1'], exclude: ['doc-2', 'doc-3'] },
    } as any);
    expect((spy.mock.calls[0][0] as any).createDemandRequest).toEqual({
      template_id: 't1',
      party_mapping: partyMapping,
      documents: { include: ['doc-1'], exclude: ['doc-2', 'doc-3'] },
    });
  });

  it('create without documents sends the same body as before (no injected key)', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsPost').mockResolvedValue(ok({ id: 'd1' }, 201));
    await client().demands.create({ template_id: 't1', party_mapping: partyMapping } as any);
    const body = (spy.mock.calls[0][0] as any).createDemandRequest;
    expect(body).toEqual({ template_id: 't1', party_mapping: partyMapping });
    expect('documents' in body).toBe(false);
  });

  it('createBulk carries the selection per row, not batch-wide', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsBulkPost').mockResolvedValue(ok({ created: 1, failed: 0, results: [] }));
    await client().demands.createBulk({
      template_id: 't1',
      rows: [
        { party_mapping: partyMapping, documents: { exclude: ['doc-2'] } },
        { party_mapping: partyMapping },
      ],
    } as any);
    const rows = (spy.mock.calls[0][0] as any).apiV1DemandsBulkPostRequest.rows;
    expect(rows[0].documents).toEqual({ exclude: ['doc-2'] });
    expect('documents' in rows[1]).toBe(false);
  });

  it('templates.get returns the template documents the ids come from', async () => {
    vi.spyOn(TemplatesApi.prototype, 'apiV1TemplatesIdGet').mockResolvedValue(
      ok({
        id: 't1',
        documents: [
          {
            id: 'doc-1',
            order: 1,
            title: 'Sözleşme',
            doc_kind: 'CONTRACT',
            is_required: true,
            signature_required: true,
            default_included: true,
            assigned_template_party_ids: ['role-1'],
          },
        ],
      }),
    );
    const template = await client().templates.get('t1');
    expect(template.documents?.[0]).toMatchObject({ id: 'doc-1', default_included: true, doc_kind: 'CONTRACT' });
  });

  it('a rejected selection surfaces as a coded ImzalaError with the reason details', async () => {
    vi.spyOn(DemandsApi.prototype, 'apiV1DemandsPost').mockRejectedValue({
      isAxiosError: true,
      message: 'Request failed with status code 400',
      response: {
        status: 400,
        data: {
          success: false,
          error: 'Belge seçimi geçersiz',
          code: 'INVALID_DOCUMENT_SELECTION',
          details: { reason: 'unknown_document', document_ids: ['doc-9'] },
        },
        headers: {},
      },
    });
    const err = await client()
      .demands.create({ template_id: 't1', party_mapping: partyMapping, documents: { include: ['doc-9'] } } as any)
      .catch((e) => e);
    expect(err).toBeInstanceOf(ImzalaError);
    expect(err.statusCode).toBe(400);
    expect(err.code).toBe('INVALID_DOCUMENT_SELECTION');
    expect(err.codeDescription).toContain('unknown_document');
    expect((err.body as any).details).toEqual({ reason: 'unknown_document', document_ids: ['doc-9'] });
  });

  it('a party left without documents surfaces as a coded ImzalaError', async () => {
    vi.spyOn(DemandsApi.prototype, 'apiV1DemandsPost').mockRejectedValue({
      isAxiosError: true,
      message: 'Request failed with status code 409',
      response: {
        status: 409,
        data: {
          success: false,
          error: 'Eşlenen bir tarafa imzalayacak belge düşmüyor',
          code: 'PARTY_WITHOUT_DOCUMENTS',
          template_party_ids: ['role-2'],
        },
        headers: {},
      },
    });
    await expect(
      client().demands.create({ template_id: 't1', party_mapping: partyMapping, documents: { exclude: ['doc-2'] } } as any),
    ).rejects.toMatchObject({ name: 'ImzalaError', statusCode: 409, code: 'PARTY_WITHOUT_DOCUMENTS' });
  });
});
