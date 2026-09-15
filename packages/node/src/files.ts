import { File as BufferFile } from 'node:buffer';

/**
 * Node-native file input for multipart endpoints (`demands.uploadDocument`,
 * `timestamps.create`). Node servers hold file bytes as `Buffer`s (from
 * `multer`, `fs.readFile`, an upstream fetch, ...) — not browser `File`
 * objects — so the facade accepts bytes + filename and builds the
 * generated client's expected `File` itself.
 */
export interface FileInput {
  /** Raw file bytes. */
  content: Buffer | Uint8Array;
  /** Original filename, including extension (e.g. `"sozlesme.pdf"`). Required — the server infers processing (PDF vs image vs office doc) from the extension. */
  filename: string;
  /** MIME type. Best-effort inferred server-side from the extension when omitted. */
  contentType?: string;
}

/**
 * Builds a spec-compliant `File` for multipart upload from Node-native
 * bytes, using `node:buffer`'s `File` (stable since Node 18.13, and the
 * same class as the global `File`/`FormData` used by axios's Node adapter —
 * `@imzala/node` targets servers, so we don't assume a browser global).
 *
 * Cast to the generated client's (DOM-lib-sourced) `File` type: `node:buffer`'s
 * `File` is runtime-identical to `globalThis.File` but TypeScript sees them
 * as distinct declarations, so a structural cast is needed here.
 */
export function toUploadFile(input: FileInput): File {
  const init = input.contentType ? { type: input.contentType } : undefined;
  return new BufferFile([input.content], input.filename, init) as unknown as File;
}

export interface UploadPartyInput {
  first_name: string;
  last_name: string;
  /** Email or phone (or both) required per party. */
  email?: string;
  /** E.164 format (e.g. `"+905551234567"`). */
  phone?: string;
  /**
   * Field template role for this party. Required on every party when
   * `fieldTemplateId` is set; each role of the template must be mapped
   * exactly once.
   */
  template_party_id?: string;
}

export interface UploadDemandParams {
  /** One document OR 1-20 images — merged server-side into a single PDF. */
  files: FileInput[];
  parties: UploadPartyInput[];
  /** Reorders a multi-image upload — indices into `files`. */
  order?: number[];
  title?: string;
  description?: string;
  /**
   * Makes the upload safe to retry: a second request with the same key does
   * not create a second demand. With a key, one retry is made after a 429.
   */
  idempotencyKey?: string;
  /**
   * Field template (`kind: FIELD_LAYOUT`) whose layout is applied to the
   * upload. The upload must then be a single PDF. The layout is resolved
   * before the demand is created or credit is spent; a 422 creates nothing.
   * Dry-run first with `fieldTemplates.previewLayout`.
   */
  fieldTemplateId?: string;
  /**
   * Only with `fieldTemplateId`. Can tighten the template, never relax it:
   * omitted means `block`; `drop` applies only if every affected field is
   * already set to drop in the template (otherwise `block` is used and the
   * response carries an `ON_ANCHOR_MISS_NOT_RELAXED` warning). Signature
   * fields are never dropped.
   */
  onAnchorMiss?: 'block' | 'drop';
  /**
   * Sends signing invitations in the same request. **Off by default on this
   * endpoint.** `'true'` or `'all'` uses every channel, `'email'` limits it to
   * e-mail, `'sms'` to phone channels (SMS and WhatsApp), `'false'` sends
   * nothing. It can only narrow: a channel switched off in the demand's or
   * party's notification settings is not turned back on.
   */
  sendInvitations?: 'true' | 'all' | 'email' | 'sms' | 'false';
  /**
   * Deliberately bypass the duplicate check (`DUPLICATE_SUSPECTED`). Only
   * meaningful for calls without `idempotencyKey`.
   */
  force?: boolean;
}

export interface CreateTimestampParams extends FileInput {
  /** Client-generated idempotency key (UUID recommended) — replays within 5 minutes return the original result without spending a credit. */
  idempotencyKey?: string;
  description?: string;
  ownerFirstName?: string;
  ownerLastName?: string;
}
