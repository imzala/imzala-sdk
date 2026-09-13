import { mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import path from 'node:path';
import { describe, expect, it, vi } from 'vitest';
import {
  compareUpstream,
  fetchBackendLock,
  hashSpecBody,
  main,
  resolveUpstreamBranch,
  verifyLocalSpec,
} from '../upstream-check.mjs';

const HASH_A = 'a'.repeat(64);
const HASH_B = 'b'.repeat(64);
const HASH_P = 'c'.repeat(64);

const LOCAL = {
  version: '1.8.5',
  upstream_ssot_sha256: HASH_A,
  public_sha256: HASH_P,
  fetched_at: '2026-09-13T00:00:00Z',
  source: {
    repo: 'imzala/imzala-service',
    branch: 'test',
    lock: 'openapi/openapi.v1.lock.json',
    spec: 'openapi/openapi.v1.public.yaml',
  },
};

const REMOTE = { version: '1.8.5', sha256: HASH_A, previous: [] };

describe('hashSpecBody (backend ile birebir aynı yöntem)', () => {
  it('info.version hariç tutulur: yalnız sürüm değişince hash değişmez', () => {
    const a = { openapi: '3.1.0', info: { title: 't', version: '1.0.0' }, paths: {} };
    const b = { openapi: '3.1.0', info: { title: 't', version: '9.9.9' }, paths: {} };
    expect(hashSpecBody(a)).toBe(hashSpecBody(b));
  });

  it('anahtar sırası hash\'i etkilemez (kanonik JSON)', () => {
    const a = { paths: { '/x': { get: {} } }, info: { title: 't' }, openapi: '3.1.0' };
    const b = { openapi: '3.1.0', info: { title: 't' }, paths: { '/x': { get: {} } } };
    expect(hashSpecBody(a)).toBe(hashSpecBody(b));
  });

  it('gövde değişince hash değişir', () => {
    const a = { openapi: '3.1.0', info: { title: 't' }, paths: { '/x': {} } };
    const b = { openapi: '3.1.0', info: { title: 't' }, paths: { '/y': {} } };
    expect(hashSpecBody(a)).not.toBe(hashSpecBody(b));
  });
});

describe('compareUpstream', () => {
  it('aynı sürüm + aynı hash → yeşil', () => {
    expect(compareUpstream(LOCAL, REMOTE).ok).toBe(true);
  });

  // KIRMIZI-KANIT: kilit gerçekten kilitliyor mu?
  it('hash farkı → kırmızı (HASH_MISMATCH)', () => {
    const r = compareUpstream(LOCAL, { ...REMOTE, sha256: HASH_B });
    expect(r.ok).toBe(false);
    expect(r.reason).toBe('HASH_MISMATCH');
    expect(r.message).toContain('spec/openapi.v1.yaml');
  });

  it('sürüm farkı → kırmızı (VERSION_MISMATCH)', () => {
    const r = compareUpstream(LOCAL, { ...REMOTE, version: '1.9.0' });
    expect(r.ok).toBe(false);
    expect(r.reason).toBe('VERSION_MISMATCH');
  });

  it('bozuk yerel kayıt → kırmızı, sessizce yeşile düşmez', () => {
    expect(compareUpstream({}, REMOTE).reason).toBe('MALFORMED_LOCAL');
    expect(compareUpstream(null, REMOTE).reason).toBe('MALFORMED_LOCAL');
    expect(compareUpstream({ ...LOCAL, upstream_ssot_sha256: 'kısa' }, REMOTE).reason).toBe('MALFORMED_LOCAL');
  });

  it('bozuk uzak kilit → kırmızı (ağ hatası yeşil sayılmaz)', () => {
    expect(compareUpstream(LOCAL, { version: '1.8.5' }).reason).toBe('MALFORMED_REMOTE');
    expect(compareUpstream(LOCAL, null).reason).toBe('MALFORMED_REMOTE');
  });
});

describe('verifyLocalSpec (elle düzenleme tespiti)', () => {
  const doc = { openapi: '3.1.0', info: { title: 't', version: '1.8.5' }, paths: {} };

  it('spec hash\'i public_sha256 ile aynı ve sürüm aynı → yeşil', () => {
    const local = { ...LOCAL, public_sha256: hashSpecBody(doc) };
    expect(verifyLocalSpec(doc, local).ok).toBe(true);
  });

  it('spec gövdesi değişmiş → kırmızı (LOCAL_SPEC_MODIFIED), mesajı HASH_MISMATCH\'ten farklı', () => {
    const local = { ...LOCAL, public_sha256: hashSpecBody(doc) };
    const tampered = { ...doc, paths: { '/api/v1/elle': {} } };
    const r = verifyLocalSpec(tampered, local);
    expect(r.ok).toBe(false);
    expect(r.reason).toBe('LOCAL_SPEC_MODIFIED');
    expect(r.message).not.toBe(compareUpstream(LOCAL, { ...REMOTE, sha256: HASH_B }).message);
  });

  it('spec info.version ile .upstream.json sürümü farklı → kırmızı (LOCAL_VERSION_MISMATCH)', () => {
    const local = { ...LOCAL, public_sha256: hashSpecBody(doc), version: '1.7.0' };
    expect(verifyLocalSpec(doc, local).reason).toBe('LOCAL_VERSION_MISMATCH');
  });

  it('spec okunamadı → kırmızı (MALFORMED_SPEC)', () => {
    expect(verifyLocalSpec(null, LOCAL).reason).toBe('MALFORMED_SPEC');
  });
});

describe('resolveUpstreamBranch (yapılandırılabilir dal)', () => {
  it('varsayılan: .upstream.json source.branch, o da yoksa "test"', () => {
    expect(resolveUpstreamBranch({ argv: [], env: {}, local: LOCAL })).toBe('test');
    expect(resolveUpstreamBranch({ argv: [], env: {}, local: null })).toBe('test');
  });

  it('env IMZALA_UPSTREAM_BRANCH kaydı ezer, --branch bayrağı env\'i ezer', () => {
    expect(resolveUpstreamBranch({ argv: [], env: { IMZALA_UPSTREAM_BRANCH: 'main' }, local: LOCAL })).toBe('main');
    expect(
      resolveUpstreamBranch({ argv: ['--branch', 'release'], env: { IMZALA_UPSTREAM_BRANCH: 'main' }, local: LOCAL }),
    ).toBe('release');
  });
});

describe('fetchBackendLock', () => {
  it('200 olmayan yanıt fırlatır (fail-closed)', async () => {
    const fake = vi.fn().mockResolvedValue({ ok: false, status: 404, text: async () => '' });
    await expect(fetchBackendLock('https://example.invalid/lock.json', { fetchImpl: fake, token: 'x' })).rejects.toThrow(/404/);
  });

  it('JSON gövdeyi ayrıştırır ve token\'ı Authorization başlığına koyar', async () => {
    const fake = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      text: async () => JSON.stringify(REMOTE),
    });
    await expect(fetchBackendLock('https://example.invalid/lock.json', { fetchImpl: fake, token: 'tkn' })).resolves.toEqual(REMOTE);
    const [, init] = fake.mock.calls[0];
    expect(init.headers.authorization).toBe('Bearer tkn');
  });

  it('token yoksa ağa çıkmadan net hata verir (sessiz geçiş yok)', async () => {
    const fake = vi.fn();
    await expect(fetchBackendLock('https://example.invalid/lock.json', { fetchImpl: fake, token: '' })).rejects.toThrow(/IMZALA_UPSTREAM_TOKEN|GITHUB_TOKEN/);
    expect(fake).not.toHaveBeenCalled();
  });

  it('yerel dosya yolu verilirse ağa çıkmadan dosyayı okur', async () => {
    const dir = mkdtempSync(path.join(tmpdir(), 'upstream-'));
    const file = path.join(dir, 'lock.json');
    writeFileSync(file, JSON.stringify(REMOTE));
    const fake = vi.fn();
    await expect(fetchBackendLock(file, { fetchImpl: fake, token: '' })).resolves.toEqual(REMOTE);
    expect(fake).not.toHaveBeenCalled();
  });
});

describe('main', () => {
  it('ağ hatasında 1 döner, asla 0', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('ENETUNREACH')));
    const code = await main(['--local', 'scripts/__tests__/fixtures/upstream.ok.json', '--spec', 'scripts/__tests__/fixtures/spec.ok.yaml'], {
      env: { IMZALA_UPSTREAM_TOKEN: 'x' },
      log: () => {},
      error: () => {},
    });
    expect(code).toBe(1);
    vi.unstubAllGlobals();
  });

  it('yerel spec bozulmuşsa ağa hiç çıkmadan 1 döner', async () => {
    const fake = vi.fn();
    vi.stubGlobal('fetch', fake);
    const code = await main(['--local', 'scripts/__tests__/fixtures/upstream.ok.json', '--spec', 'scripts/__tests__/fixtures/spec.tampered.yaml'], {
      env: { IMZALA_UPSTREAM_TOKEN: 'x' },
      log: () => {},
      error: () => {},
    });
    expect(code).toBe(1);
    expect(fake).not.toHaveBeenCalled();
    vi.unstubAllGlobals();
  });

  it('yerel spec + uzak kilit hizalıysa 0 döner', async () => {
    const dir = mkdtempSync(path.join(tmpdir(), 'upstream-'));
    const lockFile = path.join(dir, 'lock.json');
    writeFileSync(lockFile, JSON.stringify({ version: '1.8.5', sha256: 'f'.repeat(64), previous: [] }));
    const code = await main(
      ['--local', 'scripts/__tests__/fixtures/upstream.ok.json', '--spec', 'scripts/__tests__/fixtures/spec.ok.yaml', '--remote', lockFile],
      { env: {}, log: () => {}, error: () => {} },
    );
    expect(code).toBe(0);
  });
});
