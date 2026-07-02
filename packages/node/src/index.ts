import { Configuration } from '../generated/configuration';
import { AccountApi, DemandsApi, RemindersApi, TemplatesApi, TimestampsApi } from '../generated/api';
import type {
  ApiV1DemandsIdEmbedSessionPost200ResponseData,
  ApiV1DemandsIdRemindersPost200ResponseData,
  ApiV1MeGet200ResponseData,
  ApiV1TemplatesGet200ResponseData,
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
import { unwrap, unwrapRetryableGet } from './http';
import type { RetryConfig } from './http';
import { toUploadFile } from './files';
import type { CreateTimestampParams, UploadDemandParams } from './files';

export * from './errors';
export * from './webhook';
export type { FileInput, UploadDemandParams, UploadPartyInput, CreateTimestampParams } from './files';

// Re-export the generated request/response model types under friendlier
// names, so consumers get full typing without reaching into
// `@imzala/node/../generated` themselves (that path isn't part of the
// public package export map).
export type {
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
   * never retried, regardless of this setting — see the SDK README.
   */
  maxRetries?: number;
  /** Base delay (ms) for the exponential backoff between retries. Defaults to 300. */
  retryBaseDelayMs?: number;
}

export interface ListTemplatesParams {
  page?: number;
  limit?: number;
}

class TemplatesResource {
  constructor(
    private readonly api: TemplatesApi,
    private readonly retryConfig: RetryConfig,
  ) {}

  /** Lists your active templates (one page). GET — safe to auto-retry. */
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
}

class DemandsResource {
  constructor(
    private readonly api: DemandsApi,
    private readonly remindersApi: RemindersApi,
    private readonly retryConfig: RetryConfig,
  ) {}

  /**
   * Creates a new demand (contract) from a template. POST — never
   * auto-retried (a retried create would produce a duplicate demand).
   */
  create(body: CreateDemandRequest): Promise<CreatedDemand> {
    return unwrap(this.api.apiV1DemandsPost({ createDemandRequest: body }));
  }

  /** Returns a demand's status + per-party signing progress. GET — safe to auto-retry. */
  get(id: string): Promise<DemandStatus> {
    return unwrapRetryableGet(() => this.api.apiV1DemandsIdGet({ id }), this.retryConfig);
  }

  /**
   * Places (replaces) signature/form fields on a demand's pages.
   * See `UpsertItemsRequest.page_ids` for full-replace vs per-page-replace semantics.
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
    return unwrap(
      this.api.apiV1DemandsUploadPost({
        files,
        parties: JSON.stringify(params.parties),
        order: params.order ? JSON.stringify(params.order) : undefined,
        title: params.title,
        description: params.description,
      }),
    );
  }

  /**
   * Triggers an immediate SMS/email reminder to a demand's unsigned
   * parties. Independent of the template/demand's scheduled
   * `reminder_settings`. Subject to a 5-minute anti-spam window (override
   * with `{force: true}`) and a hard per-person cap of 3 reminders per
   * channel (not overridable). POST — never auto-retried (a retried call
   * could double-send).
   */
  sendReminder(
    id: string,
    body: TriggerReminderRequest = {},
  ): Promise<ApiV1DemandsIdRemindersPost200ResponseData> {
    return unwrap(this.remindersApi.apiV1DemandsIdRemindersPost({ id, triggerReminderRequest: body }));
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
  constructor(private readonly api: TimestampsApi) {}

  /**
   * RFC 3161-timestamps a file via TÜBİTAK KAMU SM TSA (existence +
   * integrity proof — not a signature; see `TimestampRecord` for details).
   * Pass `idempotencyKey` to make retries safe (5-minute window, no
   * duplicate credit spend).
   */
  create(params: CreateTimestampParams): Promise<TimestampRecord> {
    const file = toUploadFile(params);
    return unwrap(
      this.api.apiV1TimestampsPost({
        file,
        idempotencyKey: params.idempotencyKey,
        description: params.description,
        ownerFirstName: params.ownerFirstName,
        ownerLastName: params.ownerLastName,
      }),
    );
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

    this.templates = new TemplatesResource(templatesApi, this.retryConfig);
    this.demands = new DemandsResource(demandsApi, remindersApi, this.retryConfig);
    this.embed = new EmbedResource(demandsApi);
    this.timestamps = new TimestampsResource(timestampsApi);
  }

  /** Returns the calling API key's owner info (id, email, name, workspace, remaining credits). Requires the `timestamps` scope. GET — safe to auto-retry. */
  me(): Promise<ApiV1MeGet200ResponseData> {
    return unwrapRetryableGet(() => this.accountApi.apiV1MeGet(), this.retryConfig);
  }
}

export default Imzala;
