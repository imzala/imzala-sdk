import { Configuration } from '../generated/configuration';
import { AccountApi, ContactsApi, DemandsApi, RemindersApi, ReportsApi, TemplatesApi, TimestampsApi } from '../generated/api';
import type {
  ApiV1ContactsGet200ResponseData,
  ApiV1DemandsDemandIdDispatchPost200ResponseData,
  ApiV1DemandsDemandIdDocumentsDocIdPatchRequest,
  ApiV1DemandsDemandIdDocumentsGet200ResponseData,
  ApiV1DemandsDemandIdDocumentsPost201ResponseData,
  ApiV1DemandsDemandIdDocumentsPostRequest,
  ApiV1ContactsPostRequest,
  ApiV1DemandsBulkPost200ResponseData,
  ApiV1DemandsBulkPostRequest,
  ApiV1FieldTemplatesGet200ResponseData,
  ApiV1ReportsGet200ResponseData,
  ApiV1TimestampsGet200ResponseData,
  ContactSummary,
  FieldLayoutPreview,
  FieldTemplateDetail,
  TimestampListItem,
  ApiV1DemandsGet200ResponseData,
  ApiV1DemandsIdCancelPost200ResponseData,
  ApiV1DemandsIdCancelPostRequest,
  ApiV1DemandsIdEmbedSessionPost200ResponseData,
  ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData,
  ApiV1DemandsIdRemindersPost200ResponseData,
  ApiV1DemandsIdTimelineGet200ResponseData,
  ApiV1MeGet200ResponseData,
  ApiV1TemplatesGet200ResponseData,
  ApiV1TemplatesIdDelete200ResponseData,
  ApiV1TemplatesIdPatch200ResponseData,
  ApiV1TemplatesIdPatchRequest,
  CreateDemandRequest,
  CreatedDemand,
  CreatedDemandUpload,
  DemandStatus,
  TemplateDetail,
  TemplateSummary,
  TemplateUsage,
  TimestampRecord,
  TriggerReminderRequest,
  UpsertItemsRequest,
  UpsertItemsResponseData,
} from '../generated/api';
import { assertIdempotencyKey, retryableBinaryGet, unwrap, unwrapIdempotentWrite, unwrapRetryableGet } from './http';
import { ImzalaError } from './errors';
import type { RetryConfig } from './http';
import { toUploadFile } from './files';
import type { CreateTimestampParams, FileInput, UploadDemandParams } from './files';

export * from './errors';
export * from './errorCodes';
export * from './webhook';
export type { FileInput, UploadDemandParams, UploadPartyInput, CreateTimestampParams } from './files';

// Re-export the generated request/response model types under friendlier
// names, so consumers get full typing without reaching into
// `@imzala/node/../generated` themselves (that path isn't part of the
// public package export map).
export type {
  ApiV1ContactsGet200ResponseData as ContactList,
  ApiV1DemandsDemandIdDispatchPost200ResponseData as DispatchResult,
  ApiV1DemandsDemandIdDocumentsDocIdPatchRequest as UpdateEnvelopeDocumentRequest,
  ApiV1DemandsDemandIdDocumentsGet200ResponseData as EnvelopeDocumentList,
  ApiV1DemandsDemandIdDocumentsPost201ResponseData as EnvelopeDocumentResult,
  ApiV1DemandsDemandIdDocumentsPostRequest as CreateEnvelopeDocumentRequest,
  EnvelopeDocument,
  ApiV1ContactsPostRequest as CreateContactRequest,
  ApiV1DemandsBulkPost200ResponseData as BulkCreateResult,
  ApiV1DemandsBulkPostRequest as BulkCreateDemandsRequest,
  ApiV1FieldTemplatesGet200ResponseData as FieldTemplateList,
  ApiV1ReportsGet200ResponseData as ReportSummary,
  ApiV1TimestampsGet200ResponseData as TimestampList,
  ContactSummary,
  FieldLayoutDiagnostic,
  FieldLayoutPreview,
  FieldLayoutUnresolved,
  FieldLayoutWarning,
  FieldTemplateDetail,
  FieldTemplateListItem,
  FieldTemplateParty,
  TimestampListItem,
  ApiV1DemandsIdEmbedSessionPost200ResponseData as EmbedSession,
  ApiV1DemandsIdRemindersPost200ResponseData as ReminderDispatchResult,
  ApiV1MeGet200ResponseData as MeInfo,
  ApiV1TemplatesGet200ResponseData as TemplateList,
  CreateDemandRequest,
  CreatedDemand,
  CreatedDemandUpload,
  DemandStatus,
  TemplateDetail,
  TemplateSummary,
  TemplateUsage,
  TimestampRecord,
  TriggerReminderRequest,
  UpsertItemsRequest,
  UpsertItemsResponseData,
} from '../generated/api';

const DEFAULT_BASE_URL = 'https://api-prd.imzala.org';
const DEFAULT_TIMEOUT_MS = 30_000;
const DEFAULT_MAX_RETRIES = 2;
const DEFAULT_RETRY_BASE_DELAY_MS = 300;

export interface ImzalaOptions {
  /** `imz_<64 hex>` — from Dashboard → Geliştirici → API Anahtarları, or Hesap Ayarları → API Anahtarları. */
  apiKey: string;
  /** Defaults to `https://api-prd.imzala.org`. Use `https://test-api.imzala.org` for the test environment. */
  baseUrl?: string;
  /** Per-request axios timeout, in milliseconds. Defaults to 30000. */
  timeoutMs?: number;
  /**
   * Max auto-retry attempts for safe, idempotent **GET** requests that fail
   * with 429 (rate limited) or 5xx (server error). Defaults to 2. Set to
   * `0` to disable. Writes (`demands.create`, `sendReminder`, ...) are
   * never retried by this setting. A write sent with an idempotency key is
   * retried once after a 429, independently of it; see the SDK README.
   */
  maxRetries?: number;
  /** Base delay (ms) for the exponential backoff between retries. Defaults to 300. */
  retryBaseDelayMs?: number;
}

export interface ListTemplatesParams {
  page?: number;
  limit?: number;
}

export interface ListDemandsParams {
  /** Filter by demand status (DRAFT / PENDING / COMPLETED / CANCELLED / EXPIRED). */
  status?: string;
  /** Title search. */
  q?: string;
  /** ISO date (YYYY-MM-DD) lower bound on creation. */
  from?: string;
  /** ISO date (YYYY-MM-DD) upper bound on creation. */
  to?: string;
  /** Only demands created from this template. */
  templateId?: string;
  page?: number;
  limit?: number;
  /** `field:direction`, e.g. `createdAt:desc`. */
  sort?: string;
}

export interface UpdateTemplateParams {
  name?: string;
  description?: string;
  category?: string;
}

export interface ListFieldTemplatesParams {
  page?: number;
  /** 1 to 100; larger values are clamped, not rejected. */
  limit?: number;
}

export interface PreviewLayoutParams {
  /** Exactly one PDF to try the layout on (the field is a list on the wire). */
  files: FileInput[];
  /** See `UploadDemandParams.onAnchorMiss`. */
  onAnchorMiss?: 'block' | 'drop';
}

export interface ListContactsParams {
  q?: string;
  page?: number;
  /** 10 to 100, default 25. */
  limit?: number;
  sort?: string;
  companyId?: string;
  archived?: boolean;
}

export interface ListTimestampsParams {
  q?: string;
  status?: string;
  /** ISO date (YYYY-MM-DD) lower bound. */
  from?: string;
  /** ISO date (YYYY-MM-DD) upper bound. */
  to?: string;
  page?: number;
  limit?: number;
  sort?: string;
}

export type EnvelopeDocumentKind = 'CONTRACT' | 'KVKK_NOTICE' | 'KVKK_CONSENT' | 'PREINFO' | 'PRICE_LIST' | 'OTHER';

export interface ListEnvelopeDocumentsParams {
  /** `wizard` returns the full shape (`assigned_party_ids`, `decision_count`). */
  view?: 'wizard';
}

export interface UploadEnvelopeDocumentParams {
  /** Exactly one file per document. Call `upload` again for each further document. */
  file: FileInput;
  title: string;
  /**
   * Required. Sent as the `idempotency_key` form field (not as a header).
   * Printable ASCII only. Uploading again with the same key creates no new
   * document; the earlier document is returned instead.
   */
  idempotencyKey: string;
  docKind?: EnvelopeDocumentKind;
  /** Defaults to `true` on the server. */
  isRequired?: boolean;
}

export interface DispatchParams {
  /**
   * Narrows which channels invitations go out on. Omitted means the server
   * default (invitations on). `false`, `'false'`, `'0'`, `'off'`, `'no'`,
   * `'hayir'`, `'hayır'` send no invitations; `'email'` and `'sms'` limit the
   * channel. It can only narrow the demand's own notification settings. An
   * unknown value throws `INVALID_SEND_INVITATIONS`.
   */
  sendInvitations?:
    | boolean
    | 'true'
    | '1'
    | 'all'
    | 'email'
    | 'sms'
    | 'false'
    | '0'
    | 'off'
    | 'no'
    | 'hayir'
    | 'hayır';
}

export interface WriteOptions {
  /**
   * Makes the request safe to retry: the server does not create a second
   * record for a repeated key. With a key, one retry is made after a 429.
   */
  idempotencyKey?: string;
}

class TemplatesResource {
  constructor(
    private readonly api: TemplatesApi,
    private readonly retryConfig: RetryConfig,
  ) {}

  /**
   * Lists your active templates (one page). `limit` is clamped to 1..100;
   * `page` below 1 throws `INVALID_PAGE`. GET, safe to auto-retry.
   */
  list(params: ListTemplatesParams = {}): Promise<ApiV1TemplatesGet200ResponseData> {
    return unwrapRetryableGet(
      () => this.api.apiV1TemplatesGet({ page: params.page, limit: params.limit }),
      this.retryConfig,
    );
  }

  /** Returns a template's parties + fillable variables. GET — safe to auto-retry. */
  get(id: string): Promise<TemplateDetail> {
    return unwrapRetryableGet(() => this.api.apiV1TemplatesIdGet({ id }), this.retryConfig);
  }

  /** Returns a ready-to-use integration guide (endpoint, required headers, example curl+JSON) for a template. GET — safe to auto-retry. */
  usage(id: string): Promise<TemplateUsage> {
    return unwrapRetryableGet(() => this.api.apiV1TemplatesIdUsageGet({ id }), this.retryConfig);
  }

  /**
   * Walks every page of your active templates, transparently, yielding one
   * template at a time. Internally calls `list({page, limit})` and
   * increments `page` until a page comes back short (fewer items than the
   * requested page size) or the response's `total` has been reached —
   * whichever happens first — so it always terminates even against a
   * misbehaving/empty result set.
   *
   * @example
   * ```ts
   * for await (const template of imzala.templates.listAll()) {
   *   console.log(template.id, template.name);
   * }
   * ```
   */
  async *listAll(params: ListTemplatesParams = {}): AsyncGenerator<TemplateSummary, void, undefined> {
    const requestedLimit = params.limit;
    let page = params.page ?? 1;
    let yielded = 0;

    for (;;) {
      const result = await this.list({ page, limit: requestedLimit });
      const templates = result.templates ?? [];

      for (const template of templates) {
        yield template;
      }
      yielded += templates.length;

      if (templates.length === 0) break;

      const total = result.total;
      if (typeof total === 'number' && yielded >= total) break;

      const effectiveLimit = result.limit ?? requestedLimit;
      if (typeof effectiveLimit === 'number' && templates.length < effectiveLimit) break;

      page = (result.page ?? page) + 1;
    }
  }

  /**
   * Updates a template's metadata (name / description / category). The page/
   * field/party structure can't be changed via the API — edit that in the
   * dashboard. PATCH — never auto-retried.
   */
  update(
    id: string,
    body: ApiV1TemplatesIdPatchRequest,
  ): Promise<ApiV1TemplatesIdPatch200ResponseData> {
    return unwrap(this.api.apiV1TemplatesIdPatch({ id, apiV1TemplatesIdPatchRequest: body }));
  }

  /**
   * Deletes a template. The record is not erased immediately: it is marked
   * deleted and kept for 30 days. Existing demands created from it are
   * unaffected. A template with active (draft or pending) demands cannot be
   * deleted and throws `TEMPLATE_IN_USE`. DELETE, never auto-retried.
   */
  delete(id: string): Promise<ApiV1TemplatesIdDelete200ResponseData> {
    return unwrap(this.api.apiV1TemplatesIdDelete({ id }));
  }
}

class EnvelopeDocumentsResource {
  constructor(
    private readonly api: DemandsApi,
    private readonly retryConfig: RetryConfig,
  ) {}

  /**
   * Lists the documents of a multi-document envelope. While multi-document
   * envelopes are not enabled for the account, this and every other
   * `documents` method throws `ENVELOPE_MULTI_DOC_DISABLED` (409); it never
   * returns an empty list in that case. GET, safe to auto-retry.
   */
  list(demandId: string, params: ListEnvelopeDocumentsParams = {}): Promise<ApiV1DemandsDemandIdDocumentsGet200ResponseData> {
    return unwrapRetryableGet(
      () => this.api.apiV1DemandsDemandIdDocumentsGet({ demandId, view: params.view }),
      this.retryConfig,
    );
  }

  /**
   * Adds a document without a file (metadata only); its order is assigned
   * automatically. Document methods spend no credit; credit is charged on
   * `demands.dispatch`. No idempotency key, so never retried. POST.
   */
  create(demandId: string, body: ApiV1DemandsDemandIdDocumentsPostRequest): Promise<ApiV1DemandsDemandIdDocumentsPost201ResponseData> {
    return unwrap(
      this.api.apiV1DemandsDemandIdDocumentsPost({ demandId, apiV1DemandsDemandIdDocumentsPostRequest: body }),
    );
  }

  /**
   * Uploads one file as one document.
   *
   * `idempotencyKey` is required and checked before anything is sent (missing,
   * empty, or not printable ASCII throws `ImzalaValidationError`). Because the
   * server keeps the key, one retry is made after a 429 (waiting at most 60
   * seconds). If a document was already uploaded with the same key, the
   * server answers 409 `IDEMPOTENT_REPLAY` with that document; this method
   * returns it as a normal result. Any other 409 is thrown. No credit is spent.
   */
  async upload(demandId: string, params: UploadEnvelopeDocumentParams): Promise<ApiV1DemandsDemandIdDocumentsPost201ResponseData> {
    assertIdempotencyKey(params.idempotencyKey, 'idempotencyKey');
    const isRequired = params.isRequired === undefined ? undefined : params.isRequired ? 'true' : 'false';
    try {
      return await unwrapIdempotentWrite(
        () =>
          this.api.apiV1DemandsDemandIdDocumentsUploadPost({
            demandId,
            file: toUploadFile(params.file),
            idempotencyKey: params.idempotencyKey,
            title: params.title,
            docKind: params.docKind,
            isRequired,
          }),
        { idempotencyKey: params.idempotencyKey, retryBaseDelayMs: this.retryConfig.retryBaseDelayMs },
      );
    } catch (err) {
      const replayed = replayedDocument(err);
      if (replayed) return replayed;
      throw err;
    }
  }

  /** Updates only the fields you send (title, doc_kind, is_required, signature_required). Never retried. PATCH. */
  update(
    demandId: string,
    docId: string,
    body: ApiV1DemandsDemandIdDocumentsDocIdPatchRequest,
  ): Promise<ApiV1DemandsDemandIdDocumentsPost201ResponseData> {
    return unwrap(
      this.api.apiV1DemandsDemandIdDocumentsDocIdPatch({ demandId, docId, apiV1DemandsDemandIdDocumentsDocIdPatchRequest: body }),
    );
  }

  /** Deletes a document; the last document of an envelope cannot be deleted (`CANNOT_DELETE_LAST_DOCUMENT`). Never retried. DELETE. */
  delete(demandId: string, docId: string): Promise<ApiV1TemplatesIdDelete200ResponseData> {
    return unwrap(this.api.apiV1DemandsDemandIdDocumentsDocIdDelete({ demandId, docId }));
  }

  /** Sets the order of all documents. `documentIds` must contain exactly the envelope's documents (`ORDER_SET_MISMATCH`). Never retried. PUT. */
  reorder(demandId: string, documentIds: string[]): Promise<ApiV1DemandsDemandIdDocumentsGet200ResponseData> {
    return unwrap(
      this.api.apiV1DemandsDemandIdDocumentsOrderPut({
        demandId,
        apiV1DemandsDemandIdDocumentsOrderPutRequest: { document_ids: documentIds },
      }),
    );
  }

  /** Replaces the set of parties assigned to a document. Never retried. PUT. */
  setAssignments(demandId: string, docId: string, partyIds: string[]): Promise<ApiV1DemandsDemandIdDocumentsPost201ResponseData> {
    return unwrap(
      this.api.apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut({
        demandId,
        docId,
        apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest: { party_ids: partyIds },
      }),
    );
  }
}

/** The earlier document from a 409 `IDEMPOTENT_REPLAY` upload answer, or `undefined` for any other error. */
function replayedDocument(err: unknown): ApiV1DemandsDemandIdDocumentsPost201ResponseData | undefined {
  if (!(err instanceof ImzalaError) || err.statusCode !== 409 || err.code !== 'IDEMPOTENT_REPLAY') return undefined;
  const data = (err.body as { data?: ApiV1DemandsDemandIdDocumentsPost201ResponseData } | undefined)?.data;
  return data && data.document ? data : undefined;
}

class DemandsResource {
  readonly documents: EnvelopeDocumentsResource;

  constructor(
    private readonly api: DemandsApi,
    private readonly remindersApi: RemindersApi,
    private readonly retryConfig: RetryConfig,
  ) {
    this.documents = new EnvelopeDocumentsResource(api, retryConfig);
  }

  /**
   * Sends the demand for signing: reconciles credit, moves a `DRAFT` to
   * `PENDING` and sends invitations. This is where credit is charged; the
   * `documents` methods charge nothing. Calling it again for a demand that is
   * already out charges nothing more (`dispatched: false`). Throws for
   * `DISPATCH_NO_PARTIES`, `DISPATCH_TOO_MANY`,
   * `QES_NOT_SUPPORTED_MULTI_DOCUMENT`, `INSUFFICIENT_CREDITS` and others.
   * No idempotency key, so never retried, not even after a 429. POST.
   */
  dispatch(demandId: string, params: DispatchParams = {}): Promise<ApiV1DemandsDemandIdDispatchPost200ResponseData> {
    const body = params.sendInvitations === undefined ? {} : { send_invitations: params.sendInvitations };
    return unwrap(this.api.apiV1DemandsDemandIdDispatchPost({ demandId, apiV1DemandsDemandIdDispatchPostRequest: body }));
  }

  /**
   * Creates a new demand (contract) from a template.
   *
   * Without `idempotencyKey` this is a single attempt: a retried create
   * would produce a duplicate demand. With a key, a repeated request does
   * not create a second demand, so one retry is made after a 429. A key
   * reused with a different body throws `IDEMPOTENCY_KEY_REUSED`.
   *
   * An `expiry_date` that is not a real calendar day throws
   * `INVALID_EXPIRY_DATE`.
   */
  create(body: CreateDemandRequest, options: WriteOptions = {}): Promise<CreatedDemand> {
    return unwrapIdempotentWrite(
      () => this.api.apiV1DemandsPost({ createDemandRequest: body, idempotencyKey: options.idempotencyKey }),
      { idempotencyKey: options.idempotencyKey, retryBaseDelayMs: this.retryConfig.retryBaseDelayMs },
    );
  }

  /**
   * Creates up to 10 demands from one template in a single request. Rows are
   * created independently; check `failed` and each result's `status`.
   *
   * This endpoint has no idempotency key, so it is never retried: a retried
   * batch would create the demands again. Split larger lists into batches
   * of 10 yourself.
   */
  createBulk(body: ApiV1DemandsBulkPostRequest): Promise<ApiV1DemandsBulkPost200ResponseData> {
    return unwrap(this.api.apiV1DemandsBulkPost({ apiV1DemandsBulkPostRequest: body }));
  }

  /** Returns a demand's status + per-party signing progress. GET — safe to auto-retry. */
  get(id: string): Promise<DemandStatus> {
    return unwrapRetryableGet(() => this.api.apiV1DemandsIdGet({ id }), this.retryConfig);
  }

  /**
   * Places (replaces) signature/form fields on a demand's pages.
   * See `UpsertItemsRequest.page_ids` for full-replace vs per-page-replace semantics.
   * Every item needs an integer `page_id` (`PAGE_ID_REQUIRED`); an unknown
   * `item_type` throws `INVALID_ITEM_TYPE`.
   */
  addItems(id: string, body: UpsertItemsRequest): Promise<UpsertItemsResponseData> {
    return unwrap(this.api.apiV1DemandsIdItemsPost({ id, upsertItemsRequest: body }));
  }

  /**
   * Creates a demand directly from an uploaded document (no template) —
   * a single PDF/DOC/DOCX/ODT/RTF/TXT, or 1-20 images merged into one PDF.
   */
  uploadDocument(params: UploadDemandParams): Promise<CreatedDemandUpload> {
    const files = params.files.map(toUploadFile);
    return unwrapIdempotentWrite(
      () =>
        this.api.apiV1DemandsUploadPost({
          files,
          parties: JSON.stringify(params.parties),
          order: params.order ? JSON.stringify(params.order) : undefined,
          title: params.title,
          description: params.description,
          idempotencyKey: params.idempotencyKey,
          fieldTemplateId: params.fieldTemplateId,
          onAnchorMiss: params.onAnchorMiss,
          sendInvitations: params.sendInvitations,
          force: params.force ? 'true' : undefined,
        }),
      { idempotencyKey: params.idempotencyKey, retryBaseDelayMs: this.retryConfig.retryBaseDelayMs },
    );
  }

  /**
   * Triggers an immediate SMS/email reminder to a demand's unsigned
   * parties. Independent of the template/demand's scheduled
   * `reminder_settings`. Subject to a 5-minute anti-spam window (429
   * `RATE_LIMITED`, override with `{force: true}`) and a hard per-person cap
   * of 3 reminders per channel (not overridable). A draft, completed,
   * cancelled or expired demand throws 409 (`DEMAND_NOT_DISPATCHED`,
   * `DEMAND_NOT_DISPATCHABLE`, `DEMAND_EXPIRED`). POST, never auto-retried
   * (a retried call could double-send).
   */
  sendReminder(
    id: string,
    body: TriggerReminderRequest = {},
  ): Promise<ApiV1DemandsIdRemindersPost200ResponseData> {
    return unwrap(this.remindersApi.apiV1DemandsIdRemindersPost({ id, triggerReminderRequest: body }));
  }

  /**
   * Lists your demands — counts-only (id/title/status/timestamps +
   * `parties_total`/`parties_signed`, NO party names/emails/phones). Filter by
   * status/date/template, paginate with page/limit. GET — safe to auto-retry.
   * For per-party detail use `get(id)`.
   */
  list(params: ListDemandsParams = {}): Promise<ApiV1DemandsGet200ResponseData> {
    return unwrapRetryableGet(
      () =>
        this.api.apiV1DemandsGet({
          status: params.status as any,
          q: params.q,
          from: params.from,
          to: params.to,
          templateId: params.templateId,
          page: params.page,
          limit: params.limit,
          sort: params.sort,
        }),
      this.retryConfig,
    );
  }

  /**
   * Downloads the signed contract PDF (only once `status === 'COMPLETED'`).
   * Returns the raw bytes as a `Buffer` — write it to disk or stream it on.
   * Requires the API key's owner to own the demand. GET — safe to auto-retry.
   */
  getPdf(id: string): Promise<Buffer> {
    return retryableBinaryGet(
      () => this.api.apiV1DemandsIdPdfGet({ id }, { responseType: 'arraybuffer' }),
      this.retryConfig,
    );
  }

  /**
   * Downloads the PDF of one document in a multi-document envelope as a
   * `Buffer`. For the whole contract use `getPdf(id)`. GET, safe to auto-retry.
   */
  getDocumentPdf(id: string, documentId: string): Promise<Buffer> {
    return retryableBinaryGet(
      () => this.api.apiV1DemandsIdBelgeDocumentIdPdfGet({ id, documentId }, { responseType: 'arraybuffer' }),
      this.retryConfig,
    );
  }

  /**
   * Downloads the completion certificate (PAdES B-T sealed audit document) as
   * a `Buffer`. Only produced for `COMPLETED` demands. Pass `{lang: 'en'}` for
   * English. GET — safe to auto-retry.
   */
  getCertificate(id: string, params: { lang?: string } = {}): Promise<Buffer> {
    return retryableBinaryGet(
      () => this.api.apiV1DemandsIdCertificateGet({ id, lang: params.lang }, { responseType: 'arraybuffer' }),
      this.retryConfig,
    );
  }

  /**
   * Returns the signing audit trail (view/sign/reject events). PII-masked:
   * `ip_masked` (last octet hidden), actor name+email masked, no raw
   * IP/device. GET — safe to auto-retry.
   */
  getTimeline(id: string): Promise<ApiV1DemandsIdTimelineGet200ResponseData> {
    return unwrapRetryableGet(() => this.api.apiV1DemandsIdTimelineGet({ id }), this.retryConfig);
  }

  /**
   * Cancels (voids) a pending demand — sets it to `CANCELLED` and stops any
   * scheduled reminders. A `COMPLETED` (or already-cancelled) demand can't be
   * cancelled (throws). POST — never auto-retried.
   */
  cancel(
    id: string,
    body: ApiV1DemandsIdCancelPostRequest = {},
  ): Promise<ApiV1DemandsIdCancelPost200ResponseData> {
    return unwrap(this.api.apiV1DemandsIdCancelPost({ id, apiV1DemandsIdCancelPostRequest: body }));
  }

  /**
   * Re-sends the signing invitation to a single party (by `party_id` from the
   * demand's create/get response). Can't resend to a party who has already
   * signed or declined, or one whose turn hasn't come in ordered signing
   * (throws). POST — never auto-retried.
   */
  resendParty(
    id: string,
    partyId: string,
  ): Promise<ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData> {
    return unwrap(this.api.apiV1DemandsIdPartiesPartyIdResendPost({ id, partyId }));
  }

  /**
   * Deletes a demand and all its data. Only NON-completed demands can be
   * deleted via the API — a `COMPLETED` demand (signed document + audit trail)
   * returns 409 and must be removed from the dashboard. DELETE — never
   * auto-retried.
   */
  delete(id: string): Promise<ApiV1TemplatesIdDelete200ResponseData> {
    return unwrap(this.api.apiV1DemandsIdDelete({ id }));
  }
}

export interface CreateEmbedSessionParams {
  /** The party to mint an embed session for — from `signing_urls[].party_id` in the demand's create/get response. */
  partyId: string;
}

class EmbedResource {
  constructor(private readonly api: DemandsApi) {}

  /**
   * Mints a short-lived, single-use embed signing token for a demand's
   * party. The returned `embed_url` is meant for an `<iframe>` — see
   * `@imzala/embed` for a ready-made browser mount helper.
   *
   * Signatures obtained this way are SES by default (AES if TC/biometric
   * verification ran) — this flow never produces QES.
   */
  createSession(
    demandId: string,
    params: CreateEmbedSessionParams,
  ): Promise<ApiV1DemandsIdEmbedSessionPost200ResponseData> {
    return unwrap(
      this.api.apiV1DemandsIdEmbedSessionPost({
        id: demandId,
        apiV1DemandsIdEmbedSessionPostRequest: { party_id: params.partyId },
      }),
    );
  }
}

class TimestampsResource {
  constructor(
    private readonly api: TimestampsApi,
    private readonly retryConfig: RetryConfig,
  ) {}

  /**
   * RFC 3161-timestamps a file via TÜBİTAK KAMU SM TSA (existence +
   * integrity proof — not a signature; see `TimestampRecord` for details).
   * Pass `idempotencyKey` to make retries safe (5-minute window, no
   * duplicate credit spend); with a key, one retry is made after a 429.
   */
  create(params: CreateTimestampParams): Promise<TimestampRecord> {
    const file = toUploadFile(params);
    return unwrapIdempotentWrite(
      () =>
        this.api.apiV1TimestampsPost({
          file,
          idempotencyKey: params.idempotencyKey,
          description: params.description,
          ownerFirstName: params.ownerFirstName,
          ownerLastName: params.ownerLastName,
        }),
      { idempotencyKey: params.idempotencyKey, retryBaseDelayMs: this.retryConfig.retryBaseDelayMs },
    );
  }

  /** Lists your timestamp records (one page). GET, safe to auto-retry. */
  list(params: ListTimestampsParams = {}): Promise<ApiV1TimestampsGet200ResponseData> {
    return unwrapRetryableGet(
      () =>
        this.api.apiV1TimestampsGet({
          q: params.q,
          status: params.status,
          from: params.from,
          to: params.to,
          page: params.page,
          limit: params.limit,
          sort: params.sort,
        }),
      this.retryConfig,
    );
  }

  /** Returns one timestamp record. GET, safe to auto-retry. */
  get(id: string): Promise<TimestampListItem> {
    return unwrapRetryableGet(() => this.api.apiV1TimestampsIdGet({ id }), this.retryConfig);
  }
}

class FieldTemplatesResource {
  constructor(
    private readonly templatesApi: TemplatesApi,
    private readonly demandsApi: DemandsApi,
    private readonly retryConfig: RetryConfig,
  ) {}

  /**
   * Lists your field templates. A field template is separate from a contract
   * template: it describes where fields land on an uploaded PDF, located by
   * anchor text. GET, safe to auto-retry.
   */
  list(params: ListFieldTemplatesParams = {}): Promise<ApiV1FieldTemplatesGet200ResponseData> {
    return unwrapRetryableGet(
      () => this.templatesApi.apiV1FieldTemplatesGet({ page: params.page, limit: params.limit }),
      this.retryConfig,
    );
  }

  /**
   * Returns a field template's roles and field counts. A contract template
   * id throws `TEMPLATE_NOT_FOUND`: the two are different kinds. GET, safe
   * to auto-retry.
   */
  get(id: string): Promise<FieldTemplateDetail> {
    return unwrapRetryableGet(() => this.templatesApi.apiV1FieldTemplatesIdGet({ id }), this.retryConfig);
  }

  /**
   * Dry run: tries the field template's layout on a PDF without creating
   * anything or spending credit, and reports resolved fields and unresolved
   * anchors separately. The cheapest way to avoid surprises before
   * `demands.uploadDocument({ fieldTemplateId })`. Rate limited per user.
   * POST, but side-effect free.
   */
  previewLayout(id: string, params: PreviewLayoutParams): Promise<FieldLayoutPreview> {
    return unwrap(
      this.demandsApi.apiV1FieldTemplatesIdPreviewLayoutPost({
        id,
        files: params.files.map(toUploadFile),
        onAnchorMiss: params.onAnchorMiss,
      }),
    );
  }
}

class ContactsResource {
  constructor(
    private readonly api: ContactsApi,
    private readonly retryConfig: RetryConfig,
  ) {}

  /** Lists contacts in your workspace (one page). GET, safe to auto-retry. */
  list(params: ListContactsParams = {}): Promise<ApiV1ContactsGet200ResponseData> {
    return unwrapRetryableGet(
      () =>
        this.api.apiV1ContactsGet({
          q: params.q,
          archived: params.archived,
          companyId: params.companyId,
          sort: params.sort,
          page: params.page,
          limit: params.limit,
        }),
      this.retryConfig,
    );
  }

  /** Walks every page of contacts, yielding one contact at a time. */
  async *listAll(params: ListContactsParams = {}): AsyncGenerator<ContactSummary, void, undefined> {
    const requestedLimit = params.limit;
    let page = params.page ?? 1;
    let yielded = 0;

    for (;;) {
      const result = await this.list({ ...params, page, limit: requestedLimit });
      const contacts = result.contacts ?? [];
      for (const contact of contacts) {
        yield contact;
      }
      yielded += contacts.length;

      if (contacts.length === 0) break;
      const total = result.total;
      if (typeof total === 'number' && yielded >= total) break;
      const effectiveLimit = result.limit ?? requestedLimit;
      if (typeof effectiveLimit === 'number' && contacts.length < effectiveLimit) break;
      page = (result.page ?? page) + 1;
    }
  }

  /**
   * Adds a contact. An active contact with the same e-mail or phone throws
   * `CONTACT_DUPLICATE`. This endpoint has no idempotency key, so it is
   * never retried. POST.
   */
  create(body: ApiV1ContactsPostRequest): Promise<ContactSummary> {
    return unwrap(this.api.apiV1ContactsPost({ apiV1ContactsPostRequest: body }));
  }
}

class ReportsResource {
  constructor(
    private readonly api: ReportsApi,
    private readonly retryConfig: RetryConfig,
  ) {}

  /**
   * Returns aggregate demand counts for your workspace (pending, completed,
   * cancelled, expired, created this month). Counts only, no personal data.
   * GET, safe to auto-retry.
   */
  get(): Promise<ApiV1ReportsGet200ResponseData> {
    return unwrapRetryableGet(() => this.api.apiV1ReportsGet(), this.retryConfig);
  }
}

/**
 * imzala.org server-side SDK — an ergonomic, hand-written facade over the
 * generated (typescript-axios) client in `../generated`. Every method
 * unwraps the `{success, data}` response envelope and throws a typed
 * `ImzalaError` (see ./errors) on failure, instead of returning raw axios
 * responses.
 *
 * **Server-only.** Constructed with a raw `X-API-Key`, so it must never run
 * in a browser bundle — see the constructor's guard below. For browser-side
 * embedded signing UIs, use `@imzala/embed` instead.
 *
 * @example
 * ```ts
 * const imzala = new Imzala({ apiKey: process.env.IMZALA_API_KEY! });
 * const demand = await imzala.demands.create({ template_id, party_mapping });
 * ```
 */
export class Imzala {
  readonly templates: TemplatesResource;
  readonly demands: DemandsResource;
  readonly embed: EmbedResource;
  readonly timestamps: TimestampsResource;
  readonly fieldTemplates: FieldTemplatesResource;
  readonly contacts: ContactsResource;
  readonly reports: ReportsResource;

  private readonly accountApi: AccountApi;
  private readonly retryConfig: RetryConfig;

  constructor(options: ImzalaOptions) {
    if (typeof window !== 'undefined') {
      throw new Error(
        '@imzala/node is server-only — do not use in a browser (API key would leak). Use @imzala/embed for browser signing.',
      );
    }
    if (!options?.apiKey) {
      throw new Error('new Imzala({ apiKey }) — apiKey is required.');
    }
    // A key read from a file often ends with a newline; sent as a header it
    // would corrupt the request (or, in some HTTP stacks, start a new header).
    if (/[^\x20-\x7e]/.test(options.apiKey)) {
      throw new Error('new Imzala({ apiKey }): apiKey may only contain printable ASCII characters (check for a trailing newline).');
    }

    const configuration = new Configuration({
      apiKey: options.apiKey,
      basePath: options.baseUrl ?? DEFAULT_BASE_URL,
      baseOptions: { timeout: options.timeoutMs ?? DEFAULT_TIMEOUT_MS },
    });

    this.retryConfig = {
      maxRetries: Math.max(0, options.maxRetries ?? DEFAULT_MAX_RETRIES),
      retryBaseDelayMs: Math.max(0, options.retryBaseDelayMs ?? DEFAULT_RETRY_BASE_DELAY_MS),
    };

    this.accountApi = new AccountApi(configuration);
    const demandsApi = new DemandsApi(configuration);
    const remindersApi = new RemindersApi(configuration);
    const templatesApi = new TemplatesApi(configuration);
    const timestampsApi = new TimestampsApi(configuration);
    const contactsApi = new ContactsApi(configuration);
    const reportsApi = new ReportsApi(configuration);

    this.templates = new TemplatesResource(templatesApi, this.retryConfig);
    this.demands = new DemandsResource(demandsApi, remindersApi, this.retryConfig);
    this.embed = new EmbedResource(demandsApi);
    this.timestamps = new TimestampsResource(timestampsApi, this.retryConfig);
    this.fieldTemplates = new FieldTemplatesResource(templatesApi, demandsApi, this.retryConfig);
    this.contacts = new ContactsResource(contactsApi, this.retryConfig);
    this.reports = new ReportsResource(reportsApi, this.retryConfig);
  }

  /** Returns the calling API key's owner info (id, email, name, workspace, remaining credits). Works with any valid key; no scope is required. GET, safe to auto-retry. */
  me(): Promise<ApiV1MeGet200ResponseData> {
    return unwrapRetryableGet(() => this.accountApi.apiV1MeGet(), this.retryConfig);
  }
}

export default Imzala;
