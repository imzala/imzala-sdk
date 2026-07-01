import { createHmac, timingSafeEqual } from 'node:crypto';

/**
 * Verifies an imzala.org webhook delivery's `X-Imzala-Signature-256` header.
 *
 * Mirrors the backend algorithm exactly (`src/services/webhook/WebhookSigner.ts`
 * in imzala-service): `'sha256=' + HMAC-SHA256(rawBody, secret)` as lowercase
 * hex, compared with a timing-safe equality check.
 *
 * @param secret - the `whsec_<64-hex>` secret shown once when the webhook
 *   was created in the dashboard
 * @param rawBody - the **exact, unparsed** request body bytes. Parsing the
 *   JSON and re-serializing it can change byte-for-byte content (key order,
 *   whitespace) and break verification — use a raw-body middleware
 *   (e.g. Express's `express.raw({ type: 'application/json' })`)
 * @param signatureHeader - the raw `X-Imzala-Signature-256` header value
 *   (e.g. `sha256=<hex>`)
 * @returns `true` if the signature is valid. Returns `false` — never
 *   throws — for a wrong signature, a malformed/missing header, or any
 *   other verification failure.
 */
export function verifyWebhook(
  secret: string,
  rawBody: string | Buffer,
  signatureHeader: string | undefined | null,
): boolean {
  if (!secret || !signatureHeader) return false;

  try {
    const expected = `sha256=${createHmac('sha256', secret).update(rawBody).digest('hex')}`;

    const expectedBuf = Buffer.from(expected, 'utf8');
    const actualBuf = Buffer.from(signatureHeader, 'utf8');

    // timingSafeEqual throws on length mismatch — checking first keeps this
    // function's contract of "never throws, just returns false".
    if (expectedBuf.length !== actualBuf.length) return false;

    return timingSafeEqual(expectedBuf, actualBuf);
  } catch {
    return false;
  }
}
