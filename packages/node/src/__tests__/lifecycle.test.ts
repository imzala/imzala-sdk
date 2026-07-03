import { afterEach, describe, expect, it, vi } from 'vitest';
import { DemandsApi, TemplatesApi } from '../../generated/api';
import { Imzala } from '../index';

afterEach(() => {
  vi.restoreAllMocks();
});

const client = () => new Imzala({ apiKey: 'imz_test' });

describe('v1 lifecycle facade — demands', () => {
  it('list() forwards filters and unwraps counts-only data', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsGet').mockResolvedValue({
      data: { success: true, data: { demands: [{ id: 'd1', parties_total: 2, parties_signed: 1 }], total: 1, page: 1, limit: 20 } },
      status: 200,
    } as any);

    const res = await client().demands.list({ status: 'PENDING', templateId: 't1', page: 1, limit: 20 });

    expect(spy).toHaveBeenCalledWith(
      expect.objectContaining({ status: 'PENDING', templateId: 't1', page: 1, limit: 20 }),
    );
    expect(res.demands?.[0]).toEqual({ id: 'd1', parties_total: 2, parties_signed: 1 });
  });

  it('getTimeline() unwraps masked events', async () => {
    vi.spyOn(DemandsApi.prototype, 'apiV1DemandsIdTimelineGet').mockResolvedValue({
      data: { success: true, data: { events: [{ id: 'e1', event_type: 'SIGNED', ip_masked: '1.2.3.***' }] } },
      status: 200,
    } as any);

    const res = await client().demands.getTimeline('d1');
    expect(res.events?.[0].ip_masked).toBe('1.2.3.***');
  });

  it('cancel() posts the reason body and unwraps', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsIdCancelPost').mockResolvedValue({
      data: { success: true, data: { id: 'd1', status: 'CANCELLED' } },
      status: 200,
    } as any);

    const res = await client().demands.cancel('d1', { reason: 'vazgeçildi' });
    expect(spy).toHaveBeenCalledWith({ id: 'd1', apiV1DemandsIdCancelPostRequest: { reason: 'vazgeçildi' } });
    expect(res.status).toBe('CANCELLED');
  });

  it('resendParty() targets a single party', async () => {
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsIdPartiesPartyIdResendPost').mockResolvedValue({
      data: { success: true, data: { sent: ['email'] } },
      status: 200,
    } as any);

    const res = await client().demands.resendParty('d1', 'p9');
    expect(spy).toHaveBeenCalledWith({ id: 'd1', partyId: 'p9' });
    expect(res.sent).toEqual(['email']);
  });

  it('delete() unwraps deletion result', async () => {
    vi.spyOn(DemandsApi.prototype, 'apiV1DemandsIdDelete').mockResolvedValue({
      data: { success: true, data: { id: 'd1', deleted: true } },
      status: 200,
    } as any);

    const res = await client().demands.delete('d1');
    expect(res).toEqual({ id: 'd1', deleted: true });
  });

  it('getPdf() returns raw bytes as a Buffer (arraybuffer responseType)', async () => {
    const bytes = Buffer.from('%PDF-1.7 fake');
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsIdPdfGet').mockResolvedValue({
      data: bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength),
      status: 200,
    } as any);

    const out = await client().demands.getPdf('d1');
    expect(Buffer.isBuffer(out)).toBe(true);
    expect(out.toString()).toContain('%PDF-1.7');
    // ikinci arg { responseType: 'arraybuffer' } geçilmeli (ham byte için)
    expect(spy).toHaveBeenCalledWith({ id: 'd1' }, { responseType: 'arraybuffer' });
  });

  it('getCertificate() forwards lang and returns a Buffer', async () => {
    const bytes = Buffer.from('%PDF cert');
    const spy = vi.spyOn(DemandsApi.prototype, 'apiV1DemandsIdCertificateGet').mockResolvedValue({
      data: bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength),
      status: 200,
    } as any);

    const out = await client().demands.getCertificate('d1', { lang: 'en' });
    expect(Buffer.isBuffer(out)).toBe(true);
    expect(spy).toHaveBeenCalledWith({ id: 'd1', lang: 'en' }, { responseType: 'arraybuffer' });
  });
});

describe('v1 lifecycle facade — templates', () => {
  it('update() patches metadata and unwraps', async () => {
    const spy = vi.spyOn(TemplatesApi.prototype, 'apiV1TemplatesIdPatch').mockResolvedValue({
      data: { success: true, data: { id: 't1', name: 'Yeni Ad' } },
      status: 200,
    } as any);

    const res = await client().templates.update('t1', { name: 'Yeni Ad' });
    expect(spy).toHaveBeenCalledWith({ id: 't1', apiV1TemplatesIdPatchRequest: { name: 'Yeni Ad' } });
    expect(res.name).toBe('Yeni Ad');
  });

  it('delete() soft-deletes and unwraps', async () => {
    vi.spyOn(TemplatesApi.prototype, 'apiV1TemplatesIdDelete').mockResolvedValue({
      data: { success: true, data: { id: 't1', deleted: true } },
      status: 200,
    } as any);

    const res = await client().templates.delete('t1');
    expect(res).toEqual({ id: 't1', deleted: true });
  });
});
