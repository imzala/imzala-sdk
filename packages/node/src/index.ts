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
import { unwrap } from './http';
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

export interface ImzalaOptions {
  /** `imz_<64 hex>` — from Dashboard → Geliştirici → API Anahtarları, or Hesap Ayarları → API Anahtarları. */
  apiKey: string;
  /** Defaults to `https://api-prd.imzala.org`. Use `https://test-api.imzala.org` for the test environment. */
  baseUrl?: string;
  /** Per-request axios timeout, in milliseconds. Defaults to 30000. */
  timeoutMs?: number;
}

export interface ListTemplatesParams {
  page?: number;
  limit?: number;
}

class TemplatesResource {
  constructor(private readonly api: TemplatesApi) {}

  /** Lists your active templates. */
  list(params: ListTemplatesParams = {}): Promise<ApiV1TemplatesGet200ResponseData> {
    return unwrap(this.api.apiV1TemplatesGet({ page: params.page, limit: params.limit }));
  }

  /** Returns a template's parties + fillable variables. */
  get(id: string): Promise<TemplateDetail> {
    return unwrap(this.api.apiV1TemplatesIdGet({ id }));
  }

  /** Returns a ready-to-use integration guide (endpoint, required headers, example curl+JSON) for a template. */
  usage(id: string): Promise<TemplateUsage> {
    return unwrap(this.api.apiV1TemplatesIdUsageGet({ id }));
  }
}

class DemandsResource {
  constructor(
    private readonly api: DemandsApi,
    private readonly remindersApi: RemindersApi,
  ) {}

  /** Creates a new demand (contract) from a template. */
  create(body: CreateDemandRequest): Promise<CreatedDemand> {
    return unwrap(this.api.apiV1DemandsPost({ createDemandRequest: body }));
  }

  /** Returns a demand's status + per-party signing progress. */
  get(id: string): Promise<DemandStatus> {
    return unwrap(this.api.apiV1DemandsIdGet({ id }));
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
   * channel (not overridable).
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

    this.accountApi = new AccountApi(configuration);
    const demandsApi = new DemandsApi(configuration);
    const remindersApi = new RemindersApi(configuration);
    const templatesApi = new TemplatesApi(configuration);
    const timestampsApi = new TimestampsApi(configuration);

    this.templates = new TemplatesResource(templatesApi);
    this.demands = new DemandsResource(demandsApi, remindersApi);
    this.embed = new EmbedResource(demandsApi);
    this.timestamps = new TimestampsResource(timestampsApi);
  }

  /** Returns the calling API key's owner info (id, email, name, workspace, remaining credits). Requires the `timestamps` scope. */
  me(): Promise<ApiV1MeGet200ResponseData> {
    return unwrap(this.accountApi.apiV1MeGet());
  }
}

export default Imzala;
