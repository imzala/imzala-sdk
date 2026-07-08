/**
 * Uçtan uca (e2e) testler — GERÇEK İmzala API'sine karşı çalışır.
 *
 * Varsayılan olarak ATLANIR. Çalıştırmak için ortam değişkenleri:
 *   IMZALA_E2E=1
 *   IMZALA_API_KEY=imz_...
 *   IMZALA_BASE_URL=https://test-api.imzala.org   (opsiyonel; varsayılan prod)
 *
 *   IMZALA_E2E=1 IMZALA_API_KEY=imz_... IMZALA_BASE_URL=https://test-api.imzala.org \
 *     npx vitest run src/__tests__/e2e.test.ts
 *
 * Yalnızca SALT-OKUMA uçları çağrılır (kredi harcamaz, veri değiştirmez):
 * me / templates.list / demands.list / demands.get / getTimeline. Böylece
 * herhangi bir gerçek hesaba karşı güvenle koşturulabilir.
 */

import { describe, expect, it } from 'vitest';
import { Imzala, ImzalaError } from '../index';

const ENABLED = process.env.IMZALA_E2E === '1' && !!process.env.IMZALA_API_KEY;
const d = ENABLED ? describe : describe.skip;

d('e2e — canlı API (salt-okuma)', () => {
  const imzala = new Imzala({
    apiKey: process.env.IMZALA_API_KEY!,
    baseUrl: process.env.IMZALA_BASE_URL,
  });

  it('me() sahibi + kredi döner', async () => {
    const me = await imzala.me();
    expect(me).toBeTruthy();
    // e-posta ya da id alanlarından biri dolu olmalı
    expect((me as any).email ?? (me as any).id).toBeTruthy();
  });

  it('templates.list() zarf açar', async () => {
    const res = await imzala.templates.list({ limit: 3 });
    expect(Array.isArray(res.templates)).toBe(true);
    expect(typeof res.total).toBe('number');
  });

  it('demands.list() counts-only + taraf PII sızdırmaz', async () => {
    const res = await imzala.demands.list({ limit: 3 });
    expect(Array.isArray((res as any).demands)).toBe(true);
    const serialized = JSON.stringify(res);
    // counts-only liste ham e-posta/telefon içermemeli
    expect(serialized).not.toMatch(/@[a-z0-9.-]+\.[a-z]{2,}/i);
  });

  it('demands.get() + getTimeline() bir sözleşme varsa', async () => {
    const list = await imzala.demands.list({ limit: 1 });
    const first = (list as any).demands?.[0];
    if (!first) return; // sözleşme yoksa atла
    const demand = await imzala.demands.get(first.id);
    expect(demand.id).toBe(first.id);
    // detay maskeli: ham e-posta yok, email_masked var
    for (const p of demand.parties ?? []) {
      expect((p as any).email_masked ?? '').not.toMatch(/^[^*]+@/);
    }
    const timeline = await imzala.demands.getTimeline(first.id);
    expect(Array.isArray((timeline as any).events)).toBe(true);
  });

  it('geçersiz id → tipli ImzalaError (404)', async () => {
    await expect(
      imzala.demands.get('00000000-0000-0000-0000-000000000000'),
    ).rejects.toBeInstanceOf(ImzalaError);
  });
});
