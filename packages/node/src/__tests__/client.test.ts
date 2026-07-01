import { afterEach, describe, expect, it, vi } from 'vitest';
import {
  AccountApi,
  DemandsApi,
  RemindersApi,
  TemplatesApi,
  TimestampsApi,
} from '../../generated/api';
import { Imzala } from '../index';
import { ImzalaError } from '../errors';

afterEach(() => {
  vi.restoreAllMocks();
});

describe('Imzala facade — envelope unwrap', () => {
  it('demands.get() unwraps {success,data} to the inner data', async () => {
    vi.spyOn(DemandsApi.prototype, 'apiV1DemandsIdGet').mockResolvedValue({
      data: { success: true, data: { id: 'd1', status: 'PENDING' } },
      status: 200,
    } as any);

    const imzala = new Imzala({ apiKey: 'imz_test' });
    const result = await imzala.demands.get('d1');

    expect(result).toEqual({ id: 'd1', status: 'PENDING' });
  });

  it('me() calls AccountApi and unwraps', async () => {
    vi.spyOn(AccountApi.prototype, 'apiV1MeGet').mockResolvedValue({
      data: { success: true, data: { id: 'u1', email: 'a@b.com' } },
      status: 200,
    } as any);

    const imzala = new Imzala({ apiKey: 'imz_test' });
    const result = await imzala.me();

    expect(result).toEqual({ id: 'u1', email: 'a@b.com' });
  });

  it('templates.list() forwards page/limit and unwraps', async () => {
    const spy = vi.spyOn(TemplatesApi.prototype, 'apiV1TemplatesGet').mockResolvedValue({
      data: { success: true, data: { templates: [{ id: 't1' }], total: 1, page: 2, limit: 10 } },
      status: 200,
    } as any);

    const imzala = new Imzala({ apiKey: 'imz_test' });
    const result = await imzala.templates.list({ page: 2, limit: 10 });

    expect(spy).toHaveBeenCalledWith({ page: 2, limit: 10 });
    expect(result.templates).toEqual([{ id: 't1' }]);
  });

  it('demands.sendReminder() routes through RemindersApi (not DemandsApi)', async () => {
    const spy = vi.spyOn(RemindersApi.prototype, 'apiV1DemandsIdRemindersPost').mockResolvedValue({
      data: { success: true, data: { demand_id: 'd1', dispatched: [], skipped: [] } },
      status: 200,
    } as any);

    const imzala = new Imzala({ apiKey: 'imz_test' });
    const result = await imzala.demands.sendReminder('d1', { force: true });

    expect(spy).toHaveBeenCalledWith({ id: 'd1', triggerReminderRequest: { force: true } });
    expect(result).toEqual({ demand_id: 'd1', dispatched: [], skipped: [] });
  });

  it('embed.createSession() maps partyId -> party_id and unwraps', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsIdEmbedSessionPost').mockResolvedValue({
      data: {
        success: true,
        data: { embed_token: 'tok', expires_at: '2026-07-01T00:10:00.000Z', embed_url: 'https://e.imzala.org/embed/sign?token=tok' },
      },
      status: 200,
    } as any);

    const imzala = new Imzala({ apiKey: 'imz_test' });
    const result = await imzala.embed.createSession('d1', { partyId: 'p1' });

    expect(spy).toHaveBeenCalledWith({
      id: 'd1',
      apiV1DemandsIdEmbedSessionPostRequest: { party_id: 'p1' },
    });
    expect(result.embed_token).toBe('tok');
  });

  it('demands.uploadDocument() JSON-stringifies parties/order and builds File objects', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsUploadPost').mockResolvedValue({
      data: { success: true, data: { id: 'd1', pages: [{ id: 1, order: 1 }] } },
      status: 201,
    } as any);

    const imzala = new Imzala({ apiKey: 'imz_test' });
    const result = await imzala.demands.uploadDocument({
      files: [{ content: Buffer.from('hello'), filename: 'a.pdf', contentType: 'application/pdf' }],
      parties: [{ first_name: 'Ada', last_name: 'Lovelace', email: 'ada@example.com' }],
      order: [0],
      title: 'Test',
    });

    expect(spy).toHaveBeenCalledTimes(1);
    const callArgs = spy.mock.calls[0][0] as any;
    expect(callArgs.files).toHaveLength(1);
    expect(callArgs.files[0].name).toBe('a.pdf');
    expect(callArgs.files[0].type).toBe('application/pdf');
    expect(callArgs.parties).toBe(JSON.stringify([{ first_name: 'Ada', last_name: 'Lovelace', email: 'ada@example.com' }]));
    expect(callArgs.order).toBe(JSON.stringify([0]));
    expect(callArgs.title).toBe('Test');
    expect(result.id).toBe('d1');
  });

  it('timestamps.create() builds a File from Buffer content and unwraps', async () => {
    const spy = vi.spyOn(TimestampsApi.prototype, 'apiV1TimestampsPost').mockResolvedValue({
      data: { success: true, data: { id: 'ts1', file_sha256: 'abc' } },
      status: 201,
    } as any);

    const imzala = new Imzala({ apiKey: 'imz_test' });
    const result = await imzala.timestamps.create({
      content: Buffer.from('hello'),
      filename: 'eser.pdf',
      idempotencyKey: 'idem-1',
    });

    const callArgs = spy.mock.calls[0][0] as any;
    expect(callArgs.file.name).toBe('eser.pdf');
    expect(callArgs.idempotencyKey).toBe('idem-1');
    expect(result).toEqual({ id: 'ts1', file_sha256: 'abc' });
  });

  it('throws ImzalaError when the server returns success:false on a 2xx response', async () => {
    vi.spyOn(TemplatesApi.prototype, 'apiV1TemplatesIdGet').mockResolvedValue({
      data: { success: false, message: 'unexpected' },
      status: 200,
    } as any);

    const imzala = new Imzala({ apiKey: 'imz_test' });
    await expect(imzala.templates.get('t1')).rejects.toBeInstanceOf(ImzalaError);
  });
});

describe('Imzala facade — construction', () => {
  it('requires an apiKey', () => {
    expect(() => new Imzala({} as any)).toThrow(/apiKey is required/);
  });

  it('defaults baseUrl to prod', async () => {
    const spy = vi.spyOn(AccountApi.prototype, 'apiV1MeGet').mockResolvedValue({
      data: { success: true, data: {} },
      status: 200,
    } as any);
    const imzala = new Imzala({ apiKey: 'imz_test' });
    await imzala.me();
    // AccountApi reads basePath from `this.basePath`, set from Configuration.basePath in the ctor.
    expect((imzala as any).accountApi.basePath).toBe('https://api-prd.imzala.org');
    spy.mockRestore();
  });

  it('honors a custom baseUrl (e.g. test environment)', () => {
    const imzala = new Imzala({ apiKey: 'imz_test', baseUrl: 'https://test-api.imzala.org' });
    expect((imzala as any).accountApi.basePath).toBe('https://test-api.imzala.org');
  });
});

describe('Imzala facade — browser guard', () => {
  afterEach(() => {
    delete (globalThis as any).window;
  });

  it('throws when `window` is defined (browser context)', () => {
    (globalThis as any).window = {};
    expect(() => new Imzala({ apiKey: 'imz_test' })).toThrow(/server-only/);
  });

  it('constructs fine when `window` is undefined (Node context)', () => {
    delete (globalThis as any).window;
    expect(() => new Imzala({ apiKey: 'imz_test' })).not.toThrow();
  });
});
