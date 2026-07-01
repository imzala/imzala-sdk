import { createHmac } from 'node:crypto';
import { describe, expect, it } from 'vitest';
import { verifyWebhook } from '../webhook';

// Fixture mirrors the backend's WebhookEnvelope shape (src/services/webhook/WebhookSigner.ts
// in imzala-service) — HMAC-SHA256(rawBody, secret) hex digest, 'sha256=' prefixed.
const secret = 'whsec_1e6f1c2b3a4d5e6f7081920a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6';
const rawBody = JSON.stringify({
  id: 'evt_abc123',
  type: 'demand.completed',
  created_at: '2026-07-01T00:00:00.000Z',
  data: { demand_id: 'd1' },
});

function sign(body: string | Buffer, key: string = secret): string {
  return `sha256=${createHmac('sha256', key).update(body).digest('hex')}`;
}

describe('verifyWebhook', () => {
  it('returns true for a correctly signed string body', () => {
    expect(verifyWebhook(secret, rawBody, sign(rawBody))).toBe(true);
  });

  it('returns true for a correctly signed Buffer body (same bytes as the string case)', () => {
    const buf = Buffer.from(rawBody, 'utf8');
    expect(verifyWebhook(secret, buf, sign(buf))).toBe(true);
    // cross-check: signature computed over the Buffer verifies the string form too
    expect(verifyWebhook(secret, rawBody, sign(buf))).toBe(true);
  });

  it('returns false for the wrong secret', () => {
    expect(verifyWebhook('whsec_wrong_00000000000000000000000000000000000000000000000000', rawBody, sign(rawBody))).toBe(
      false,
    );
  });

  it('returns false when the body was tampered with after signing', () => {
    const tampered = rawBody.replace('d1', 'd2-evil');
    expect(verifyWebhook(secret, tampered, sign(rawBody))).toBe(false);
  });

  it('returns false for a malformed header missing the sha256= prefix (length mismatch)', () => {
    const digestOnly = sign(rawBody).replace('sha256=', '');
    expect(verifyWebhook(secret, rawBody, digestOnly)).toBe(false);
  });

  it('returns false for a garbage header of the same length (no throw on timingSafeEqual)', () => {
    const valid = sign(rawBody);
    const garbage = 'sha256=' + '0'.repeat(valid.length - 'sha256='.length);
    expect(verifyWebhook(secret, rawBody, garbage)).toBe(false);
  });

  it('returns false — never throws — for a missing/empty header', () => {
    expect(verifyWebhook(secret, rawBody, undefined)).toBe(false);
    expect(verifyWebhook(secret, rawBody, null)).toBe(false);
    expect(verifyWebhook(secret, rawBody, '')).toBe(false);
  });

  it('returns false for an empty secret rather than signing with an empty key', () => {
    expect(verifyWebhook('', rawBody, sign(rawBody, ''))).toBe(false);
  });
});
