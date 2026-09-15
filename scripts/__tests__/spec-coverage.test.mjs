import { readFileSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
import path from 'node:path';
import yaml from 'js-yaml';
import { describe, expect, it } from 'vitest';

// Locks the surface this repo ships. spec/openapi.v1.yaml is a verbatim copy of the
// backend's public artefact; these assertions catch a silent regression (a lost path,
// an internal operation leaking in, a stale version) at CI time.

const specText = readFileSync(path.resolve('spec/openapi.v1.yaml'), 'utf8');
const spec = yaml.load(specText);
const twin = yaml.load(readFileSync(path.resolve('spec/openapi.v1.3.0.yaml'), 'utf8'));
const upstream = JSON.parse(readFileSync(path.resolve('spec/.upstream.json'), 'utf8'));

const REQUIRED_PATHS = [
  '/api/v1/templates',
  '/api/v1/templates/{id}',
  '/api/v1/templates/{id}/usage',
  '/api/v1/demands',
  '/api/v1/demands/bulk',
  '/api/v1/demands/{id}',
  '/api/v1/demands/{id}/reminders',
  '/api/v1/demands/upload',
  '/api/v1/demands/{id}/items',
  '/api/v1/demands/{id}/embed-session',
  '/api/v1/demands/{id}/pdf',
  '/api/v1/demands/{id}/belge/{document_id}/pdf',
  '/api/v1/demands/{id}/certificate',
  '/api/v1/demands/{id}/timeline',
  '/api/v1/demands/{id}/cancel',
  '/api/v1/demands/{id}/parties/{partyId}/resend',
  '/api/v1/demands/{demandId}/documents',
  '/api/v1/demands/{demandId}/documents/upload',
  '/api/v1/demands/{demandId}/documents/order',
  '/api/v1/demands/{demandId}/documents/{docId}',
  '/api/v1/demands/{demandId}/documents/{docId}/assignments',
  '/api/v1/demands/{demandId}/dispatch',
  '/api/v1/field-templates',
  '/api/v1/field-templates/{id}',
  '/api/v1/field-templates/{id}/preview-layout',
  '/api/v1/me',
  '/api/v1/timestamps',
  '/api/v1/timestamps/{id}',
  '/api/v1/reports',
  '/api/v1/contacts',
];

// Exact set published by the backend. kyc.completed / kyc.failed stay here on
// purpose: the KYC endpoints are internal and not part of the public artefact,
// but the events are real and any integrator can subscribe to them. If the
// backend changes this set, this test must break loudly rather than pass silently.
const REQUIRED_WEBHOOKS = [
  'demand.created',
  'demand.completed',
  'demand.expired',
  'party.signed',
  'party.viewed',
  'party.rejected',
  'kyc.completed',
  'kyc.failed',
];

// Example data in the spec is fictional only. Turkish mobile numbers use the
// 555 exchange (reserved for examples); e-mail addresses use reserved or
// company domains. Real values never appear in this repository, not even in a
// deny-list, so the check is an allow-list.
const FICTIONAL_PHONES = ['+905551112233', '+905551112244', '+905551111111', '+905551234567', '+905552222222'];
const FICTIONAL_EMAIL_DOMAINS = ['example.com', 'x.com', 'imzala.org'];

describe('spec coverage', () => {
  it.each(REQUIRED_PATHS)('%s is defined', (p) => {
    expect(Object.keys(spec.paths)).toContain(p);
  });

  it('path count is exactly the required list (no silent additions or losses)', () => {
    expect(Object.keys(spec.paths).sort()).toEqual([...REQUIRED_PATHS].sort());
  });

  it.each(REQUIRED_WEBHOOKS)('webhook %s is defined', (evt) => {
    expect(Object.keys(spec.webhooks)).toContain(evt);
  });

  it('webhook set is exactly the required list (no silent additions or losses)', () => {
    expect(Object.keys(spec.webhooks).sort()).toEqual([...REQUIRED_WEBHOOKS].sort());
  });

  it('granular demand scopes are used, no wholesale "demands" scope remains', () => {
    expect(specText).toContain('demands:read');
    expect(specText).toContain('demands:write');
    expect(specText).not.toMatch(/x-required-scope: demands\s*$/m);
  });

  it('version matches spec/.upstream.json', () => {
    expect(spec.info.version).toBe(upstream.version);
  });

  it('3.0.3 twin is generated from the same body: same version and same path set', () => {
    expect(twin.openapi).toBe('3.0.3');
    expect(twin.info.version).toBe(spec.info.version);
    expect(Object.keys(twin.paths).sort()).toEqual(Object.keys(spec.paths).sort());
  });
});

describe('public boundary', () => {
  it('no KYC path leaks into the public derivative', () => {
    expect(Object.keys(spec.paths).some((p) => p.includes('/kyc/'))).toBe(false);
    expect(specText).not.toContain('/api/v1/kyc');
  });

  it('no internal-only markers (x-internal, x-imzala-*) are present', () => {
    // Case-sensitive on purpose: lowercase x-imzala-* are vendor extensions for
    // internal tooling; the X-Imzala-* HTTP headers (e.g. X-Imzala-Event) are
    // part of the public contract and legitimately appear in the spec.
    expect(specText).not.toMatch(/x-internal/);
    expect(specText).not.toMatch(/x-imzala/);
  });

  it('the standard fictional example person is present', () => {
    expect(specText).toContain('Ayşe Yılmaz');
    expect(specText).toContain('ayse@example.com');
    expect(specText).toContain('+905551112233');
  });

  it('every Turkish mobile number in the spec is a known fictional one', () => {
    const found = [...new Set(specText.match(/\+?905[0-9]{9}/g) ?? [])];
    expect(found.length).toBeGreaterThan(0);
    for (const n of found) expect(FICTIONAL_PHONES).toContain(n);
  });

  it('every e-mail address in the spec uses a reserved or company domain', () => {
    const found = [...new Set(specText.match(/[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[a-z]{2,}/g) ?? [])];
    expect(found.length).toBeGreaterThan(0);
    for (const e of found) expect(FICTIONAL_EMAIL_DOMAINS).toContain(e.split('@')[1]);
  });
});

// The same allow-lists apply to everything written by hand: READMEs, examples,
// facades and their tests. Generated code and the vendored spec are covered by
// the spec checks above; lockfiles carry no example data.
const HAND_WRITTEN_EXCLUDE = [/(^|\/)generated\//, /^spec\//, /(^|\/)(package-lock\.json|composer\.lock)$/];
// git@github.com appears in clone instructions; it is a host, not a person.
const DOC_EMAIL_DOMAINS = [...FICTIONAL_EMAIL_DOMAINS, 'github.com'];

function handWrittenFiles() {
  return execFileSync('git', ['ls-files'], { encoding: 'utf8' })
    .split('\n')
    .filter((f) => f && !HAND_WRITTEN_EXCLUDE.some((re) => re.test(f)));
}

describe('example data outside the spec', () => {
  const files = handWrittenFiles().map((f) => {
    try {
      return [f, readFileSync(path.resolve(f), 'utf8')];
    } catch {
      return [f, ''];
    }
  });

  it('scans a meaningful set of files', () => {
    expect(files.length).toBeGreaterThan(50);
    expect(files.map(([f]) => f)).toContain('README.md');
  });

  it('every Turkish mobile number is a known fictional one', () => {
    const bad = [];
    for (const [f, text] of files) {
      for (const n of new Set(text.match(/\+?905[0-9]{9}/g) ?? [])) {
        if (!FICTIONAL_PHONES.includes(n.startsWith('+') ? n : `+${n}`)) bad.push(`${f}: ${n}`);
      }
    }
    expect(bad).toEqual([]);
  });

  it('every e-mail address uses a reserved or company domain', () => {
    const bad = [];
    for (const [f, text] of files) {
      for (const e of new Set(text.match(/[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[a-z]{2,}/g) ?? [])) {
        if (!DOC_EMAIL_DOMAINS.includes(e.split('@')[1])) bad.push(`${f}: ${e}`);
      }
    }
    expect(bad).toEqual([]);
  });
});
