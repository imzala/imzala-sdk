/* tslint:disable */
/* eslint-disable */
/**
 * imzala External API
 * imzala.org dış API\'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.6.0 · **Son güncelleme:** 2026-06-30  ## Auth Tüm istekler `X-API-Key` header\'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API key kullanıyorsanız `X-Workspace-Id` header\'ı göndermeniz gerekir (organizasyon UUID\'si). Kişisel anahtarlar için bu header gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field\'lar) `POST /api/v1/demands` payload\'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field\'lar   (örn. Kira sözleşmesinde Kiraya Veren\'in `address`, `iban` field\'ları) - `variables` (root) — **partilerden bağımsız** field\'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item\'ın template_party_id\'si var ve o parti slug\'ı göndermişse → uygula 2. Yoksa root `variables`\'tan ara → varsa uygula 3. Yoksa atla  Dashboard\'daki **API Kullanımı** tab\'ı (`/sablonlar/<id>`) hangi field\'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint\'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array\'ı, gönderdiğiniz ama şablonda eşleşmeyen slug\'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log\'ta veya dashboard\'da kontrol edin.  ## Rate Limit - 60 istek/dakika per API key - Aşılırsa 429 döner  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard\'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta\'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker\'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body\'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default\'unu   ezer, sadece bu demand\'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint\'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response\'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility\'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d \'{}\'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d \'{\"channels\": [\"sms\"], \"force\": true}\' ```  Detay için **Reminders** tag\'i altındaki endpoint\'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL\'ye `POST` ile JSON payload gönderir. Webhook\'lar dashboard\'dan yönetilir: **Ayarlar -> Webhook\'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook\'u** (org workspace\'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event\'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace\'te) → sadece sizin kendi   event\'lerinizde tetiklenir  ### Olay tipleri (6) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti |  ### Header\'lar Her istekte aşağıdaki header\'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB\'de unique key). - `type` — yukarıdaki 6 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header\'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require(\'crypto\');  function verify(rawBody, header, secret) {   const expected = \'sha256=\' + crypto     .createHmac(\'sha256\', secret)     .update(rawBody, \'utf8\')     .digest(\'hex\');   return crypto.timingSafeEqual(     Buffer.from(header || \'\', \'utf8\'),     Buffer.from(expected, \'utf8\')   ); }  // Express app.post(\'/webhook\', express.raw({ type: \'application/json\' }), (req, res) => {   const sig = req.header(\'X-Imzala-Signature-256\');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send(\'invalid signature\');   }   const event = JSON.parse(req.body.toString(\'utf8\'));   // ... event\'i kuyruğa koy ve hemen 2xx dön   res.status(200).send(\'ok\'); }); ```  > **Önemli:** Body\'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware\'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard\'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint\'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB\'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix\'inden sonra kayıp event\'leri yakalamak) için bazı payload\'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow\'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send(\'replay accepted\'); } ```  ### Manuel yeniden gönderim Dashboard\'da `Ayarlar -> Webhook\'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5\'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`\'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn\'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload\'larda side-effect\'leri atla. 5. `X-Imzala-Delivery` UUID\'sini log\'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret\'i env var\'da sakla, koda gömme. 
 *
 * The version of the OpenAPI document: 1.6.0
 * Contact: destek@imzala.org
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


import type { Configuration } from './configuration';
import type { AxiosPromise, AxiosInstance, RawAxiosRequestConfig } from 'axios';
import globalAxios from 'axios';
// Some imports not used depending on template conditions
// @ts-ignore
import { DUMMY_BASE_URL, assertParamExists, setApiKeyToObject, setBasicAuthToObject, setBearerAuthToObject, setOAuthToObject, setSearchParams, serializeDataIfNeeded, toPathString, createRequestFunction, replaceWithSerializableTypeIfNeeded } from './common';
import type { RequestArgs } from './base';
// @ts-ignore
import { BASE_PATH, COLLECTION_FORMATS, BaseAPI, RequiredError, operationServerMap } from './base';

/**
 * Standart hata zarfı. `success: false`, `error` makinece okunabilir hata kodu, `message` kullanıcıya dönük açıklama. 
 */
export interface ApiError {
    'success'?: boolean;
    'error'?: string;
    'message'?: string;
}
export interface ApiV1DemandsIdEmbedSessionPost200Response {
    'success'?: boolean;
    'data'?: ApiV1DemandsIdEmbedSessionPost200ResponseData;
}
export interface ApiV1DemandsIdEmbedSessionPost200ResponseData {
    /**
     * Tek kullanımlık, kısa ömürlü gömülü imza token\'ı
     */
    'embed_token'?: string;
    /**
     * Token geçerlilik bitiş zamanı (ISO 8601 UTC)
     */
    'expires_at'?: string;
    /**
     * `<iframe src=\"\">` alanına yerleştirilecek tam URL. `https://e.imzala.org/embed/sign?token=<embed_token>` formatında. 
     */
    'embed_url'?: string;
}
export interface ApiV1DemandsIdEmbedSessionPostRequest {
    /**
     * Token üretilecek tarafın ID\'si. `POST /api/v1/demands` veya `GET /api/v1/demands/{id}` cevabındaki `signing_urls[].party_id` alanından alınır. 
     */
    'party_id': string;
}
export interface ApiV1DemandsIdGet200Response {
    'success'?: boolean;
    'data'?: DemandStatus;
}
export interface ApiV1DemandsIdRemindersPost200Response {
    'success'?: boolean;
    'data'?: ApiV1DemandsIdRemindersPost200ResponseData;
}
export interface ApiV1DemandsIdRemindersPost200ResponseData {
    'demand_id'?: string;
    'dispatched'?: Array<ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner>;
    /**
     * Hatırlatma gönderilmeyen partilerin nedenleriyle birlikte (telefon/email yok, opt-out vs.)
     */
    'skipped'?: Array<ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner>;
    'last_reminder_sent_at'?: string;
}
export interface ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner {
    'party_id'?: string;
    'first_name'?: string;
    'last_name'?: string;
    'channels'?: Array<ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInnerChannelsEnum>;
}

export const ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInnerChannelsEnum = {
    Email: 'email',
    Sms: 'sms',
} as const;

export type ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInnerChannelsEnum = typeof ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInnerChannelsEnum[keyof typeof ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInnerChannelsEnum];

export interface ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner {
    'party_id'?: string;
    'reason'?: string;
}
export interface ApiV1DemandsIdRemindersPost429Response {
    'success'?: boolean;
    'error'?: ApiV1DemandsIdRemindersPost429ResponseError;
}
export interface ApiV1DemandsIdRemindersPost429ResponseError {
    'code'?: ApiV1DemandsIdRemindersPost429ResponseErrorCodeEnum;
    'message'?: string;
    /**
     * Sadece RATE_LIMITED\'de
     */
    'retry_after_seconds'?: number;
}

export const ApiV1DemandsIdRemindersPost429ResponseErrorCodeEnum = {
    RateLimited: 'RATE_LIMITED',
    MaxSmsRemindersReached: 'MAX_SMS_REMINDERS_REACHED',
} as const;

export type ApiV1DemandsIdRemindersPost429ResponseErrorCodeEnum = typeof ApiV1DemandsIdRemindersPost429ResponseErrorCodeEnum[keyof typeof ApiV1DemandsIdRemindersPost429ResponseErrorCodeEnum];

export interface ApiV1DemandsPost201Response {
    'success'?: boolean;
    'data'?: CreatedDemand;
}
export interface ApiV1DemandsUploadPost201Response {
    'success'?: boolean;
    'data'?: CreatedDemandUpload;
}
export interface ApiV1MeGet200Response {
    'success'?: boolean;
    'data'?: ApiV1MeGet200ResponseData;
}
export interface ApiV1MeGet200ResponseData {
    'id'?: string;
    'email'?: string;
    'first_name'?: string;
    'last_name'?: string;
    'workspace'?: ApiV1MeGet200ResponseDataWorkspace;
    'credits'?: ApiV1MeGet200ResponseDataCredits;
}
export interface ApiV1MeGet200ResponseDataCredits {
    'remaining'?: number;
}
export interface ApiV1MeGet200ResponseDataWorkspace {
    'type'?: ApiV1MeGet200ResponseDataWorkspaceTypeEnum;
    'organization_id'?: string | null;
}

export const ApiV1MeGet200ResponseDataWorkspaceTypeEnum = {
    Personal: 'personal',
    Organization: 'organization',
} as const;

export type ApiV1MeGet200ResponseDataWorkspaceTypeEnum = typeof ApiV1MeGet200ResponseDataWorkspaceTypeEnum[keyof typeof ApiV1MeGet200ResponseDataWorkspaceTypeEnum];

export interface ApiV1TemplatesGet200Response {
    'success'?: boolean;
    'data'?: ApiV1TemplatesGet200ResponseData;
}
export interface ApiV1TemplatesGet200ResponseData {
    'templates'?: Array<TemplateSummary>;
    'total'?: number;
    'page'?: number;
    'limit'?: number;
}
export interface ApiV1TemplatesGet401Response {
    'success'?: boolean;
    'error'?: string;
    'message'?: string;
}
export interface ApiV1TemplatesIdGet200Response {
    'success'?: boolean;
    'data'?: TemplateDetail;
}
export interface ApiV1TemplatesIdGet404Response {
    'success'?: boolean;
    'error'?: string;
}
export interface ApiV1TemplatesIdUsageGet200Response {
    'success'?: boolean;
    'data'?: TemplateUsage;
}
export interface ApiV1TimestampsPost201Response {
    'success'?: boolean;
    'data'?: TimestampRecord;
}
export interface ApiV1TimestampsPostRequest1 {
    /**
     * Standart Base64 kodlanmış dosya içeriği (RFC 4648 §4 — A-Za-z0-9+/ alfabesi, \'=\' padding). data URL öneki (`data:...;base64,`) ve URL-safe alfabe (`-_`) kabul edilmez. 
     */
    'file_base64': string;
    /**
     * Orijinal dosya adı (uzantısıyla, ör. \"belge.pdf\")
     */
    'file_name': string;
    /**
     * Kayıt açıklaması (opsiyonel, max 500 karakter)
     */
    'description'?: string;
    /**
     * Dosya sahibinin adı (opsiyonel, kullanıcı beyanı)
     */
    'owner_first_name'?: string;
    /**
     * Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı)
     */
    'owner_last_name'?: string;
}
export interface CreateDemandRequest {
    /**
     * GET /api/v1/templates listesinden veya dashboard\'dan kopyalayın
     */
    'template_id': string;
    /**
     * Sözleşme başlığı (yoksa template adı kullanılır)
     */
    'title'?: string;
    'description'?: string;
    'party_mapping': Array<PartyMappingInput>;
    /**
     * **Root scope** — partilerden bağımsız field\'lara gönderilen değerler. Item\'ın template_party_id\'si NULL ise (partisiz) buradan dolar. Multi-party şablonda kira_baslangic_tarihi gibi paylaşılan field\'lar. 
     */
    'variables'?: { [key: string]: PartyMappingInputVariablesValue; };
    /**
     * TÜBİTAK zaman damgası
     */
    'has_timestamp'?: boolean;
    'send_sms_notifications'?: boolean;
    'send_email_notifications'?: boolean;
    /**
     * SMS gönderici adı
     */
    'sms_title'?: string;
    /**
     * Custom SMS gövdesi. **Sadece** çağıran organizasyon **PRO veya ENTERPRISE planda** ise ve aktif `OrganizationSmsConfig` (sender_name dolu) varsa kabul edilir; aksi halde 403 `SMS_CUSTOMIZATION_NOT_ALLOWED` döner.  FREE/BASIC planda olan veya kendi SMS sağlayıcısı tanımlı olmayan müşterilerin marka itibarını korumak için sistem default sağlayıcısı (Codeck NetGSM) ile gönderim yapılır ve özel metin reddedilir. Kendi sağlayıcınızı tanımlamak için Dashboard → Organizasyon → SMS Ayarları sayfasını kullanın.  Boş string / null gönderirseniz \"clear\" olarak yorumlanır (gating\'den geçer). 
     */
    'sms_content'?: string;
    /**
     * Custom e-posta gövdesi
     */
    'email_content'?: string;
    'expiry_date'?: string;
    'require_tc_verification'?: boolean;
    'require_biometric_verification'?: boolean;
    /**
     * Bu sözleşme için hatırlatma ayarlarını **şablon default\'unu override** ederek belirtir. Yollanmazsa şablonun `reminder_*` alanları kullanılır (PUT /api/templates/:id ile dashboard\'dan kaydedilen değerler); şablonda da yoksa `{enabled:true, intervals_hours:[48], max_reminders:1, channels:[\"email\"]}` default\'u uygulanır. Demand oluşumunda `ReminderConfig` satırı yaratılır ve BullMQ kuyruğuna scheduled hatırlatmalar yazılır. 
     */
    'reminder_settings'?: ReminderSettings;
}
export interface CreatedDemand {
    'id'?: string;
    'title'?: string;
    'status'?: CreatedDemandStatusEnum;
    'template_id'?: string;
    'signing_urls'?: Array<CreatedDemandSigningUrlsInner>;
    'result_url'?: string;
    /**
     * Uygulanan TÜM slug\'ların unique union\'ı (sorted). Geriye dönük uyumluluk için korunur — yeni entegrasyonlar variables_applied_root + variables_applied_by_party kullanmalı. 
     */
    'variables_applied'?: Array<string>;
    /**
     * Root variables\'tan uygulanan slug listesi (sorted).
     */
    'variables_applied_root'?: Array<string>;
    /**
     * template_party_id → o partiye uygulanan slug listesi (sorted per party). 
     */
    'variables_applied_by_party'?: { [key: string]: Array<string>; };
    /**
     * Gönderdiğiniz AMA hiçbir item\'a uygulanmayan slug\'lar (unique, sorted). Boş olmayınca yazım hatası yapmışsınız demektir — kontrol edin. 
     */
    'variables_ignored'?: Array<string>;
}

export const CreatedDemandStatusEnum = {
    Draft: 'DRAFT',
    Pending: 'PENDING',
} as const;

export type CreatedDemandStatusEnum = typeof CreatedDemandStatusEnum[keyof typeof CreatedDemandStatusEnum];

export interface CreatedDemandSigningUrlsInner {
    'party_id'?: string;
    'first_name'?: string;
    'last_name'?: string;
    'email'?: string | null;
    'phone'?: string | null;
    'signing_url'?: string;
}
export interface CreatedDemandUpload {
    'id'?: string;
    'title'?: string;
    'status'?: string;
    /**
     * Oluşturulan sözleşmedeki her sayfanın `id` ve `order` bilgisi. `POST /api/v1/demands/{id}/items` endpoint\'ine alan yerleştirmek için `page_id` parametresi olarak kullanın. 
     */
    'pages'?: Array<DemandPage>;
    'signing_urls'?: Array<CreatedDemandSigningUrlsInner>;
    'result_url'?: string;
}
export interface DemandPage {
    'id': number;
    /**
     * 1-based sayfa sırası
     */
    'order': number;
}
export interface DemandStatus {
    'id'?: string;
    'title'?: string;
    'status'?: DemandStatusStatusEnum;
    'created_at'?: string;
    'completed_at'?: string | null;
    'parties'?: Array<DemandStatusPartiesInner>;
    'result_url'?: string;
    /**
     * Sadece status=COMPLETED iken dolu
     */
    'pdf_url'?: string | null;
}

export const DemandStatusStatusEnum = {
    Draft: 'DRAFT',
    Pending: 'PENDING',
    Completed: 'COMPLETED',
    Expired: 'EXPIRED',
    Cancelled: 'CANCELLED',
} as const;

export type DemandStatusStatusEnum = typeof DemandStatusStatusEnum[keyof typeof DemandStatusStatusEnum];

export interface DemandStatusPartiesInner {
    'party_id'?: string;
    'first_name'?: string;
    'last_name'?: string;
    'email'?: string | null;
    'signed'?: boolean;
    'signed_at'?: string | null;
    'signing_url'?: string;
}
/**
 * Sözleşme sayfasına yerleştirilen alan tanımı. Tüm koordinatlar sayfa boyutuna göre normalize edilmiş [0,1] aralığında. Origin top-left (PDF/canvas standardı). 
 */
export interface PageItem {
    /**
     * AgreementPage.id (`/upload` response\'undaki `pages[].id`)
     */
    'page_id': number;
    /**
     * `signature` ve doldurulabilir alanlar (`dynamic_text`, `cells`, `date`, `dropdown`, `checkbox`, `radio`) için **zorunlu** — alanı dolduracak/imzalayacak partinin id\'si (`signing_urls[].party_id`). `text` ve `stamp` için null. 
     */
    'party_id'?: string | null;
    'item_type': PageItemItemTypeEnum;
    /**
     * Sayfa genişliğine göre x koordinatı (sol=0)
     */
    'position_x': number;
    /**
     * Sayfa yüksekliğine göre y koordinatı (üst=0)
     */
    'position_y': number;
    'width': number;
    'height': number;
    /**
     * İmza/alan zorunlu mu — tarafın bu alanı doldurmadan imzalayamadığı
     */
    'is_required'?: boolean;
    /**
     * Alan tanımlayıcı (snake_case, 2-50 karakter). Doldurulabilir alanlar için **önerilir**. `dynamic_text`/`cells` gibi değişken alanlarda `config.defaultSource` ile system değişkenleri (`{{signer.full_name}}`, `{{signer.government_id}}` vb.) bağlanır. 
     */
    'slug'?: string | null;
    /**
     * Kullanıcıya gösterilecek etiket
     */
    'label'?: string | null;
    /**
     * Item type\'a özgü konfigürasyon: - `dynamic_text`: `{ defaultSource, defaultValue }` - `cells`: `{ cellCount, defaultSource }` - `date`: `{ defaultSource, defaultValue }` - `dropdown`/`radio`: `{ options: [{label, value}], defaultValue }` - `checkbox`: `{ checkedByDefault }` - `stamp`: `{ stampData }` (base64 data URL) 
     */
    'config'?: object | null;
}

export const PageItemItemTypeEnum = {
    Signature: 'signature',
    Text: 'text',
    DynamicText: 'dynamic_text',
    Cells: 'cells',
    Date: 'date',
    Dropdown: 'dropdown',
    Checkbox: 'checkbox',
    Radio: 'radio',
    Stamp: 'stamp',
} as const;

export type PageItemItemTypeEnum = typeof PageItemItemTypeEnum[keyof typeof PageItemItemTypeEnum];

export interface PartyMappingInput {
    /**
     * GET /api/v1/templates/{id} cevabındaki parties[].id
     */
    'template_party_id': string;
    'first_name': string;
    'last_name': string;
    /**
     * email VEYA phone\'dan en az biri zorunlu
     */
    'email'?: string;
    /**
     * E.164 format (örn. \"+905551234567\")
     */
    'phone'?: string;
    /**
     * TC kimlik no (11 hane)
     */
    'government_id'?: string;
    /**
     * ISO 8601 (örn. \"1990-05-15\")
     */
    'birth_date'?: string;
    'send_sms'?: boolean;
    'send_email'?: boolean;
    /**
     * Bu PARTİYE AİT dynamic field\'lara gönderilen değerler. Slug bazında eşleşir. Item\'ın template_party_id\'si bu partiyle aynı olmalı; değilse değişken atlanır ve variables_ignored\'a düşürülür. 
     */
    'variables'?: { [key: string]: PartyMappingInputVariablesValue; };
}
/**
 * @type PartyMappingInputVariablesValue
 */
export type PartyMappingInputVariablesValue = boolean | number | string;

/**
 * Hatırlatma yapılandırması. Şablon (`Template.reminder_*`) ve sözleşme (`ReminderConfig`) arasında aynı şemaya sahiptir. 
 */
export interface ReminderSettings {
    /**
     * false → hiç hatırlatma gönderilmez (cron schedule edilmez)
     */
    'enabled'?: boolean;
    /**
     * Sözleşmenin oluşturulduğu andan itibaren saat cinsinden hatırlatma aralıkları. Örn. `[24, 72, 168]` → 24 saat sonra, 3 gün sonra ve 7 gün sonra. `max_reminders` ile limit edilir. 
     */
    'intervals_hours'?: Array<number>;
    /**
     * Bir parti için maksimum gönderilecek hatırlatma sayısı
     */
    'max_reminders'?: number;
    /**
     * Hatırlatma gönderim kanalı. Birden fazla seçilebilir (`[\"email\",\"sms\"]`). SMS kanalı için partinin `phone` alanı dolu ve `send_sms: true` olmalı; email kanalı için `email` dolu ve `send_email: true` olmalı, ayrıca demand\'in `send_sms_notifications` / `send_email_notifications` global toggle\'ı açık olmalı. 
     */
    'channels'?: Array<ReminderSettingsChannelsEnum>;
}

export const ReminderSettingsChannelsEnum = {
    Email: 'email',
    Sms: 'sms',
} as const;

export type ReminderSettingsChannelsEnum = typeof ReminderSettingsChannelsEnum[keyof typeof ReminderSettingsChannelsEnum];

/**
 * `code`/`message` formatında standart hata gövdesi. Reminder trigger (`POST /api/v1/demands/{id}/reminders`) bu formatı kullanır. 
 */
export interface StandardError {
    'success'?: boolean;
    'error'?: StandardErrorError;
}
export interface StandardErrorError {
    'code': StandardErrorErrorCodeEnum;
    'message': string;
    /**
     * Sadece RATE_LIMITED\'de doludur
     */
    'retry_after_seconds'?: number;
}

export const StandardErrorErrorCodeEnum = {
    InvalidChannels: 'INVALID_CHANNELS',
    DemandNotFound: 'DEMAND_NOT_FOUND',
    AlreadyCompleted: 'ALREADY_COMPLETED',
    RateLimited: 'RATE_LIMITED',
    MaxSmsRemindersReached: 'MAX_SMS_REMINDERS_REACHED',
    Unauthorized: 'UNAUTHORIZED',
    InternalError: 'INTERNAL_ERROR',
} as const;

export type StandardErrorErrorCodeEnum = typeof StandardErrorErrorCodeEnum[keyof typeof StandardErrorErrorCodeEnum];

export interface TemplateDetail {
    'id'?: string;
    'name'?: string;
    'description'?: string | null;
    'category'?: string | null;
    'usage_count'?: number;
    'parties'?: Array<TemplatePartySummary>;
    'pages_count'?: number;
    'variables'?: Array<TemplateVariable>;
}
export interface TemplatePartySummary {
    'id'?: string;
    'order'?: number;
    'label'?: string;
    'is_required'?: boolean;
}
export interface TemplateSummary {
    'id'?: string;
    'name'?: string;
    'description'?: string | null;
    'category'?: string | null;
    'usage_count'?: number;
    'parties'?: Array<TemplateSummaryPartiesInner>;
}
export interface TemplateSummaryPartiesInner {
    'id'?: string;
    'order'?: number;
    'label'?: string;
    'is_required'?: boolean;
}
export interface TemplateUsage {
    'template'?: TemplateUsageTemplate;
    'endpoint'?: TemplateUsageEndpoint;
    'required_headers'?: { [key: string]: string; };
    'parties'?: Array<TemplateUsagePartiesInner>;
    'variables'?: Array<TemplateUsageVariablesInner>;
    'example_request'?: TemplateUsageExampleRequest;
}
export interface TemplateUsageEndpoint {
    'method'?: string;
    'url'?: string;
}
export interface TemplateUsageExampleRequest {
    /**
     * curl komutu, slug\'lar dolu
     */
    'curl'?: string;
    /**
     * JSON payload, multi-party-aware (party_mapping[].variables + root variables)
     */
    'json'?: object;
}
export interface TemplateUsagePartiesInner {
    'template_party_id'?: string;
    'order'?: number;
    'label'?: string;
    'is_required'?: boolean;
    'supported_fields'?: Array<TemplateUsagePartiesInnerSupportedFieldsInner>;
}
export interface TemplateUsagePartiesInnerSupportedFieldsInner {
    'key'?: string;
    'type'?: string;
    'required'?: boolean;
    'required_if'?: string;
    'default'?: any;
}
export interface TemplateUsageTemplate {
    'id'?: string;
    'name'?: string;
    'description'?: string | null;
    'category'?: string | null;
}
export interface TemplateUsageVariablesInner {
    'slug'?: string;
    'label'?: string;
    'item_type'?: string;
    'is_required'?: boolean;
    'default_source'?: string | null;
    'auto_filled'?: boolean;
    'template_party_id'?: string | null;
    'note'?: string;
}
export interface TemplateVariable {
    'slug'?: string;
    'label'?: string;
    'item_type'?: TemplateVariableItemTypeEnum;
    'is_required'?: boolean;
    /**
     * Doluysa item değeri otomatik olarak bu kaynaktan dolar (örn. signer.full_name → party_mapping\'teki ad+soyad). variables payload\'ında override edilebilir.  **Kullanılabilir sistem değişkenleri:**  İmzalayan (party-bağlı, render anında çözülür): - `signer.first_name`, `signer.last_name`, `signer.full_name` - `signer.email`, `signer.phone`, `signer.government_id` - `signer.birth_date` — İmzalayanın doğum tarihi (gg.aa.yyyy).   Source: `party_mapping[i].birth_date` API alanı. - `signer.sign_date` — İmzalayanın imza tarihi (gg.aa.yyyy);   imzalanmadıysa boş. Source: server-computed   (`DemandContractParty.sign_timestamp`), API üzerinden   settable DEĞİL.  Sözleşme: `contract.title`, `contract.created_date`, `contract.expiry_date`, `contract.id`  Gönderen: `sender.full_name`, `sender.email`, `sender.company_name`  Tarih: `current.date`, `current.datetime`  **Precedence — TC alanı ve diğer slug çakışmaları:** Eğer `party_mapping[i].government_id` ile `party_mapping[i].variables.tc_kimlik` (veya başka slug-eşleşmeli variable) aynı anda gönderilirse, **`variables.<slug>` öncelikli** olur. Bunun nedeni `applyPartyAwareVariables` slug yazımını önce uygular; system variable autofill (`signer.government_id` defaultSource\'u) sonra çalışır ve dolu alanları atlar. 
     */
    'default_source'?: string | null;
    /**
     * Bu field\'in sahibi olan template parti id\'si. NULL ise root scope (party_mapping dışında) — kök variables\'a göndermeniz gerekir. Doluysa party_mapping[i].variables\'ta i bu id ile eşleşen partinin altına göndermeniz gerekir. 
     */
    'template_party_id'?: string | null;
}

export const TemplateVariableItemTypeEnum = {
    DynamicText: 'dynamic_text',
    Cells: 'cells',
    Date: 'date',
    Dropdown: 'dropdown',
    Text: 'text',
} as const;

export type TemplateVariableItemTypeEnum = typeof TemplateVariableItemTypeEnum[keyof typeof TemplateVariableItemTypeEnum];

export interface TimestampRecord {
    'id'?: string;
    /**
     * TÜBİTAK KAMU SM\'nin onayladığı damga zamanı (UTC)
     */
    'timestamp_time'?: string;
    /**
     * Zaman damgası sağlayıcısı
     */
    'tsa_authority'?: string;
    /**
     * Damgalanan dosyanın SHA-256 hash değeri (hex, 64 karakter)
     */
    'file_sha256'?: string;
    /**
     * Damgayı doğrulamak için URL
     */
    'verify_url'?: string;
    /**
     * Damga sertifikası URL\'i
     */
    'certificate_url'?: string;
    /**
     * Bu damga için harcanan kredi miktarı
     */
    'credits_used'?: number;
    /**
     * İşlem sonrası kalan kredi bakiyesi
     */
    'credits_remaining'?: number;
}
/**
 * Anlık hatırlatma tetikleme isteği gövdesi (tüm alanlar opsiyonel).
 */
export interface TriggerReminderRequest {
    /**
     * Bu çağrıda kullanılacak kanal(lar). Yollanmazsa default `[\"sms\",\"email\"]` ile her iki kanalda da gönderilir (parti `send_sms`/`send_email` toggle\'ı + iletişim alanı + demand global toggle uygunluğuna göre). 
     */
    'channels'?: Array<TriggerReminderRequestChannelsEnum>;
    /**
     * `true` ise 5 dk anti-spam penceresini override eder (son hatırlatma 5 dk içinde gönderilmiş olsa bile gönderir). Production akışlarında yanlışlıkla spam atmamak için sadece kasıtlı admin operasyonlarında kullanın. 
     */
    'force'?: boolean;
}

export const TriggerReminderRequestChannelsEnum = {
    Email: 'email',
    Sms: 'sms',
} as const;

export type TriggerReminderRequestChannelsEnum = typeof TriggerReminderRequestChannelsEnum[keyof typeof TriggerReminderRequestChannelsEnum];

export interface UpsertItemsRequest {
    'items': Array<PageItem>;
    /**
     * **Opsiyonel.** Verilirse sadece bu sayfaların item\'ları replace edilir; diğer sayfalardaki item\'lar korunur. Body\'deki `items[].page_id` değerleri bu listede olmalıdır. Omitted ise tüm sayfaların item\'ları replace edilir. 
     */
    'page_ids'?: Array<number>;
}
export interface UpsertItemsResponse {
    'success'?: boolean;
    'data'?: UpsertItemsResponseData;
}
export interface UpsertItemsResponseData {
    'items'?: Array<UpsertItemsResponseDataItemsInner>;
    'items_count'?: number;
}
/**
 * Yaratılan AgreementPageItem snapshot\'ı
 */
export interface UpsertItemsResponseDataItemsInner {
    'id'?: string;
    'page_id'?: number;
    'party_id'?: string | null;
    'item_type'?: string;
    'position_x'?: number;
    'position_y'?: number;
    'width'?: number;
    'height'?: number;
    'is_required'?: boolean;
    'slug'?: string | null;
    'label'?: string | null;
    'config'?: object | null;
}
export interface WebhookDataDemandCompleted {
    'demand_id': string;
    'title': string;
    'status': WebhookDataDemandCompletedStatusEnum;
    'completed_at': string;
    'parties': Array<WebhookDataDemandCompletedPartiesInner>;
    /**
     * Replay bayrağı (yan etkileri atla)
     */
    '_backfill'?: boolean;
}

export const WebhookDataDemandCompletedStatusEnum = {
    Completed: 'COMPLETED',
} as const;

export type WebhookDataDemandCompletedStatusEnum = typeof WebhookDataDemandCompletedStatusEnum[keyof typeof WebhookDataDemandCompletedStatusEnum];

export interface WebhookDataDemandCompletedPartiesInner {
    'id'?: string;
    /**
     * first_name + last_name
     */
    'name'?: string;
    'email'?: string | null;
    'signed_at'?: string | null;
}
export interface WebhookDataDemandCreated {
    'demand_id': string;
    'title': string;
    /**
     * Geçmiş event\'i replay etmek için bayrak. Receiver yan etkileri (e-posta gönderme, ödeme tetikleme vs.) atlamalı. 
     */
    '_backfill'?: boolean;
}
export interface WebhookDataDemandExpired {
    'demand_id': string;
    'title': string;
    'expiry_date': string;
    'parties': Array<WebhookDataDemandExpiredPartiesInner>;
}
export interface WebhookDataDemandExpiredPartiesInner {
    'id'?: string;
    'name'?: string;
    'email'?: string | null;
    /**
     * Süre dolduğunda imzalamış mıydı?
     */
    'signed'?: boolean;
}
export interface WebhookDataPartyRejected {
    'demand_id': string;
    'party': WebhookDataPartyRejectedParty;
    'rejected_at': string;
}
export interface WebhookDataPartyRejectedParty {
    'id': string;
    'first_name': string;
    'last_name': string;
    'email'?: string | null;
    /**
     * Tarafın belirttiği sebep (opsiyonel, modal textarea)
     */
    'rejection_reason'?: string | null;
}
export interface WebhookDataPartySigned {
    'demand_id': string;
    'party_id': string;
    /**
     * Replay bayrağı
     */
    '_backfill'?: boolean;
}
export interface WebhookDataPartyViewed {
    'demand_id': string;
    'party_id': string;
}
/**
 * Tüm webhook payload\'larının ortak zarfı. `data` alanı olay tipine göre değişir; her olay için ayrı veri şeması yukarıda dokümante. 
 */
export interface WebhookEnvelope {
    /**
     * Olay benzersiz id\'si. Receiver tarafında idempotency için kullanın (DB\'de unique key). 
     */
    'id': string;
    'type': WebhookEnvelopeTypeEnum;
    /**
     * Olay zamanı (ISO 8601 UTC).
     */
    'created_at': string;
    /**
     * Olay tipine özel veri (aşağıdaki şemalar)
     */
    'data': object;
}

export const WebhookEnvelopeTypeEnum = {
    DemandCreated: 'demand.created',
    DemandCompleted: 'demand.completed',
    DemandExpired: 'demand.expired',
    PartySigned: 'party.signed',
    PartyViewed: 'party.viewed',
    PartyRejected: 'party.rejected',
} as const;

export type WebhookEnvelopeTypeEnum = typeof WebhookEnvelopeTypeEnum[keyof typeof WebhookEnvelopeTypeEnum];


/**
 * AccountApi - axios parameter creator
 */
export const AccountApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Çağrıyı yapan API key\'in sahibi hakkında temel bilgileri döner: kullanıcı kimliği, e-posta, isim, aktif workspace ve kalan kredi. `timestamps` scope\'u gerektirir; scope yoksa 403 `INSUFFICIENT_SCOPE` döner. 
         * @summary API key sahibi bilgisi
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1MeGet: async (options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/api/v1/me`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'GET', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)

            localVarHeaderParameter['Accept'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * AccountApi - functional programming interface
 */
export const AccountApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = AccountApiAxiosParamCreator(configuration)
    return {
        /**
         * Çağrıyı yapan API key\'in sahibi hakkında temel bilgileri döner: kullanıcı kimliği, e-posta, isim, aktif workspace ve kalan kredi. `timestamps` scope\'u gerektirir; scope yoksa 403 `INSUFFICIENT_SCOPE` döner. 
         * @summary API key sahibi bilgisi
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1MeGet(options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1MeGet200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1MeGet(options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['AccountApi.apiV1MeGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * AccountApi - factory interface
 */
export const AccountApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = AccountApiFp(configuration)
    return {
        /**
         * Çağrıyı yapan API key\'in sahibi hakkında temel bilgileri döner: kullanıcı kimliği, e-posta, isim, aktif workspace ve kalan kredi. `timestamps` scope\'u gerektirir; scope yoksa 403 `INSUFFICIENT_SCOPE` döner. 
         * @summary API key sahibi bilgisi
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1MeGet(options?: RawAxiosRequestConfig): AxiosPromise<ApiV1MeGet200Response> {
            return localVarFp.apiV1MeGet(options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * AccountApi - object-oriented interface
 */
export class AccountApi extends BaseAPI {
    /**
     * Çağrıyı yapan API key\'in sahibi hakkında temel bilgileri döner: kullanıcı kimliği, e-posta, isim, aktif workspace ve kalan kredi. `timestamps` scope\'u gerektirir; scope yoksa 403 `INSUFFICIENT_SCOPE` döner. 
     * @summary API key sahibi bilgisi
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1MeGet(options?: RawAxiosRequestConfig) {
        return AccountApiFp(this.configuration).apiV1MeGet(options).then((request) => request(this.axios, this.basePath));
    }
}



/**
 * DemandsApi - axios parameter creator
 */
export const DemandsApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token\'ı üretir. Dönen `embed_url` bir `<iframe>` içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \"güvenli\" veya \"nitelikli\" sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: `expires_at` alanında belirtilen sürede geçersiz olur. - `embed_allowed_origins` kısıtı: API anahtarına tanımlanmış   izin verilen origin\'ler dışından `<iframe>` açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** `X-Workspace-Id` header\'ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace\'in sözleşmesi için 404 döner (IDOR koruması). 
         * @summary Gömülü imza oturumu başlat (embed token mint)
         * @param {string} id Sözleşme (demand) ID
         * @param {ApiV1DemandsIdEmbedSessionPostRequest} apiV1DemandsIdEmbedSessionPostRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdEmbedSessionPost: async (id: string, apiV1DemandsIdEmbedSessionPostRequest: ApiV1DemandsIdEmbedSessionPostRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1DemandsIdEmbedSessionPost', 'id', id)
            // verify required parameter 'apiV1DemandsIdEmbedSessionPostRequest' is not null or undefined
            assertParamExists('apiV1DemandsIdEmbedSessionPost', 'apiV1DemandsIdEmbedSessionPostRequest', apiV1DemandsIdEmbedSessionPostRequest)
            const localVarPath = `/api/v1/demands/{id}/embed-session`
                .replace('{id}', encodeURIComponent(String(id)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)

            localVarHeaderParameter['Content-Type'] = 'application/json';
            localVarHeaderParameter['Accept'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(apiV1DemandsIdEmbedSessionPostRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * 
         * @summary Sözleşme durumu + imza ilerlemesi
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdGet: async (id: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1DemandsIdGet', 'id', id)
            const localVarPath = `/api/v1/demands/{id}`
                .replace('{id}', encodeURIComponent(String(id)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'GET', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)

            localVarHeaderParameter['Accept'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: `POST /api/v1/demands/upload` ile demand yarat (`dispatch_notifications=false` ile auto-dispatch\'i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da `POST /api/v1/demands/{id}/reminders` ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - `page_ids` **omitted** → demand\'in TÜM mevcut item\'ları silinir,   body\'dekiler yaratılır (full replace). - `page_ids: [N, M, ...]` → sadece bu sayfaların item\'ları silinir,   diğer sayfalardaki item\'lar korunur. Body\'deki `items[].page_id`   değerleri `page_ids` listesinde olmalıdır.  ### Item type\'ları  | `item_type` | `party_id` zorunlu? | `config` örneği | |-------------|---------------------|-----------------| | `signature` | ✅ | (yok) | | `text` | ❌ | `{ default_content }` | | `dynamic_text` | ✅ | `{ defaultSource: \"{{signer.full_name}}\" }` | | `cells` | ✅ | `{ cellCount: 11, defaultSource: \"{{signer.government_id}}\" }` | | `date` | ✅ | `{ defaultSource, defaultValue }` | | `dropdown` | ✅ | `{ options: [{label,value}], defaultValue }` | | `checkbox` | ✅ | `{ checkedByDefault: false }` | | `radio` | ✅ | `{ options: [{label,value}], defaultValue }` | | `stamp` | ❌ | `{ stampData: \"data:image/png;base64,...\" }` |  ### Sistem değişkenleri (dynamic_text/cells/date `config.defaultSource`)  `{{signer.first_name}}`, `{{signer.last_name}}`, `{{signer.full_name}}`, `{{signer.email}}`, `{{signer.phone}}`, `{{signer.government_id}}`, `{{signer.birth_date}}`, `{{signer.sign_date}}`, `{{contract.title}}`, `{{sender.full_name}}`, `{{current.date}}`, `{{current.datetime}}`.  ### Workspace izolasyonu  X-API-Key middleware demand\'i workspace\'e göre filtreler; başka workspace\'in demand\'ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece `PENDING` demand edit edilebilir. `COMPLETED`, `EXPIRED`, `REJECTED` için 403.  ### Örnek  ```bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d \'{     \"items\": [       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"signature\",         \"position_x\": 0.5, \"position_y\": 0.85,         \"width\": 0.2, \"height\": 0.05,         \"is_required\": true       },       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"cells\",         \"position_x\": 0.1, \"position_y\": 0.5,         \"width\": 0.4, \"height\": 0.04,         \"slug\": \"tc\",         \"config\": { \"cellCount\": 11, \"defaultSource\": \"{{signer.government_id}}\" }       }     ]   }\' ``` 
         * @summary Sözleşmeye alan yerleştir (replace)
         * @param {string} id 
         * @param {UpsertItemsRequest} upsertItemsRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdItemsPost: async (id: string, upsertItemsRequest: UpsertItemsRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1DemandsIdItemsPost', 'id', id)
            // verify required parameter 'upsertItemsRequest' is not null or undefined
            assertParamExists('apiV1DemandsIdItemsPost', 'upsertItemsRequest', upsertItemsRequest)
            const localVarPath = `/api/v1/demands/{id}/items`
                .replace('{id}', encodeURIComponent(String(id)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)

            localVarHeaderParameter['Content-Type'] = 'application/json';
            localVarHeaderParameter['Accept'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(upsertItemsRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field\'ları `variables` payload\'undan doldurur ve imzalama URL\'lerini döner.  **Variable resolution:** - Item\'ın `template_party_id` non-null → `party_mapping[i].variables`\'ta   o slug var ise oradan uygulanır - Yoksa root `variables`\'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value\'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı 
         * @summary Sözleşme oluştur (şablondan)
         * @param {CreateDemandRequest} createDemandRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsPost: async (createDemandRequest: CreateDemandRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'createDemandRequest' is not null or undefined
            assertParamExists('apiV1DemandsPost', 'createDemandRequest', createDemandRequest)
            const localVarPath = `/api/v1/demands`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)

            localVarHeaderParameter['Content-Type'] = 'application/json';
            localVarHeaderParameter['Accept'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(createDemandRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF\'e birleştirilir, office formatları LibreOffice ile PDF\'e çevrilir. 
         * @summary Dosya upload ile sözleşme oluştur (şablonsuz)
         * @param {Array<File>} files 1 belge VEYA 1-20 görsel
         * @param {string} parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu). 
         * @param {string} [order] Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;)
         * @param {string} [title] 
         * @param {string} [description] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsUploadPost: async (files: Array<File>, parties: string, order?: string, title?: string, description?: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'files' is not null or undefined
            assertParamExists('apiV1DemandsUploadPost', 'files', files)
            // verify required parameter 'parties' is not null or undefined
            assertParamExists('apiV1DemandsUploadPost', 'parties', parties)
            const localVarPath = `/api/v1/demands/upload`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;
            const localVarFormParams = new ((configuration && configuration.formDataCtor) || FormData)();

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)

            if (files) {
                files.forEach((element) => {
                    localVarFormParams.append('files', element as any);
                })
            }


            if (order !== undefined) { 
                localVarFormParams.append('order', order as any);
            }

            if (title !== undefined) { 
                localVarFormParams.append('title', title as any);
            }

            if (description !== undefined) { 
                localVarFormParams.append('description', description as any);
            }

            if (parties !== undefined) { 
                localVarFormParams.append('parties', parties as any);
            }
            localVarHeaderParameter['Content-Type'] = 'multipart/form-data';
            localVarHeaderParameter['Accept'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = localVarFormParams;

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * DemandsApi - functional programming interface
 */
export const DemandsApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = DemandsApiAxiosParamCreator(configuration)
    return {
        /**
         * Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token\'ı üretir. Dönen `embed_url` bir `<iframe>` içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \"güvenli\" veya \"nitelikli\" sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: `expires_at` alanında belirtilen sürede geçersiz olur. - `embed_allowed_origins` kısıtı: API anahtarına tanımlanmış   izin verilen origin\'ler dışından `<iframe>` açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** `X-Workspace-Id` header\'ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace\'in sözleşmesi için 404 döner (IDOR koruması). 
         * @summary Gömülü imza oturumu başlat (embed token mint)
         * @param {string} id Sözleşme (demand) ID
         * @param {ApiV1DemandsIdEmbedSessionPostRequest} apiV1DemandsIdEmbedSessionPostRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsIdEmbedSessionPost(id: string, apiV1DemandsIdEmbedSessionPostRequest: ApiV1DemandsIdEmbedSessionPostRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsIdEmbedSessionPost200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsIdEmbedSessionPost(id, apiV1DemandsIdEmbedSessionPostRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsIdEmbedSessionPost']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * 
         * @summary Sözleşme durumu + imza ilerlemesi
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsIdGet(id: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsIdGet200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsIdGet(id, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsIdGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: `POST /api/v1/demands/upload` ile demand yarat (`dispatch_notifications=false` ile auto-dispatch\'i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da `POST /api/v1/demands/{id}/reminders` ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - `page_ids` **omitted** → demand\'in TÜM mevcut item\'ları silinir,   body\'dekiler yaratılır (full replace). - `page_ids: [N, M, ...]` → sadece bu sayfaların item\'ları silinir,   diğer sayfalardaki item\'lar korunur. Body\'deki `items[].page_id`   değerleri `page_ids` listesinde olmalıdır.  ### Item type\'ları  | `item_type` | `party_id` zorunlu? | `config` örneği | |-------------|---------------------|-----------------| | `signature` | ✅ | (yok) | | `text` | ❌ | `{ default_content }` | | `dynamic_text` | ✅ | `{ defaultSource: \"{{signer.full_name}}\" }` | | `cells` | ✅ | `{ cellCount: 11, defaultSource: \"{{signer.government_id}}\" }` | | `date` | ✅ | `{ defaultSource, defaultValue }` | | `dropdown` | ✅ | `{ options: [{label,value}], defaultValue }` | | `checkbox` | ✅ | `{ checkedByDefault: false }` | | `radio` | ✅ | `{ options: [{label,value}], defaultValue }` | | `stamp` | ❌ | `{ stampData: \"data:image/png;base64,...\" }` |  ### Sistem değişkenleri (dynamic_text/cells/date `config.defaultSource`)  `{{signer.first_name}}`, `{{signer.last_name}}`, `{{signer.full_name}}`, `{{signer.email}}`, `{{signer.phone}}`, `{{signer.government_id}}`, `{{signer.birth_date}}`, `{{signer.sign_date}}`, `{{contract.title}}`, `{{sender.full_name}}`, `{{current.date}}`, `{{current.datetime}}`.  ### Workspace izolasyonu  X-API-Key middleware demand\'i workspace\'e göre filtreler; başka workspace\'in demand\'ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece `PENDING` demand edit edilebilir. `COMPLETED`, `EXPIRED`, `REJECTED` için 403.  ### Örnek  ```bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d \'{     \"items\": [       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"signature\",         \"position_x\": 0.5, \"position_y\": 0.85,         \"width\": 0.2, \"height\": 0.05,         \"is_required\": true       },       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"cells\",         \"position_x\": 0.1, \"position_y\": 0.5,         \"width\": 0.4, \"height\": 0.04,         \"slug\": \"tc\",         \"config\": { \"cellCount\": 11, \"defaultSource\": \"{{signer.government_id}}\" }       }     ]   }\' ``` 
         * @summary Sözleşmeye alan yerleştir (replace)
         * @param {string} id 
         * @param {UpsertItemsRequest} upsertItemsRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsIdItemsPost(id: string, upsertItemsRequest: UpsertItemsRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<UpsertItemsResponse>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsIdItemsPost(id, upsertItemsRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsIdItemsPost']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field\'ları `variables` payload\'undan doldurur ve imzalama URL\'lerini döner.  **Variable resolution:** - Item\'ın `template_party_id` non-null → `party_mapping[i].variables`\'ta   o slug var ise oradan uygulanır - Yoksa root `variables`\'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value\'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı 
         * @summary Sözleşme oluştur (şablondan)
         * @param {CreateDemandRequest} createDemandRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsPost(createDemandRequest: CreateDemandRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsPost201Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsPost(createDemandRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsPost']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF\'e birleştirilir, office formatları LibreOffice ile PDF\'e çevrilir. 
         * @summary Dosya upload ile sözleşme oluştur (şablonsuz)
         * @param {Array<File>} files 1 belge VEYA 1-20 görsel
         * @param {string} parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu). 
         * @param {string} [order] Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;)
         * @param {string} [title] 
         * @param {string} [description] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsUploadPost(files: Array<File>, parties: string, order?: string, title?: string, description?: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsUploadPost201Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsUploadPost(files, parties, order, title, description, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsUploadPost']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * DemandsApi - factory interface
 */
export const DemandsApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = DemandsApiFp(configuration)
    return {
        /**
         * Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token\'ı üretir. Dönen `embed_url` bir `<iframe>` içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \"güvenli\" veya \"nitelikli\" sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: `expires_at` alanında belirtilen sürede geçersiz olur. - `embed_allowed_origins` kısıtı: API anahtarına tanımlanmış   izin verilen origin\'ler dışından `<iframe>` açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** `X-Workspace-Id` header\'ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace\'in sözleşmesi için 404 döner (IDOR koruması). 
         * @summary Gömülü imza oturumu başlat (embed token mint)
         * @param {DemandsApiApiV1DemandsIdEmbedSessionPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdEmbedSessionPost(requestParameters: DemandsApiApiV1DemandsIdEmbedSessionPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsIdEmbedSessionPost200Response> {
            return localVarFp.apiV1DemandsIdEmbedSessionPost(requestParameters.id, requestParameters.apiV1DemandsIdEmbedSessionPostRequest, options).then((request) => request(axios, basePath));
        },
        /**
         * 
         * @summary Sözleşme durumu + imza ilerlemesi
         * @param {DemandsApiApiV1DemandsIdGetRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdGet(requestParameters: DemandsApiApiV1DemandsIdGetRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsIdGet200Response> {
            return localVarFp.apiV1DemandsIdGet(requestParameters.id, options).then((request) => request(axios, basePath));
        },
        /**
         * Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: `POST /api/v1/demands/upload` ile demand yarat (`dispatch_notifications=false` ile auto-dispatch\'i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da `POST /api/v1/demands/{id}/reminders` ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - `page_ids` **omitted** → demand\'in TÜM mevcut item\'ları silinir,   body\'dekiler yaratılır (full replace). - `page_ids: [N, M, ...]` → sadece bu sayfaların item\'ları silinir,   diğer sayfalardaki item\'lar korunur. Body\'deki `items[].page_id`   değerleri `page_ids` listesinde olmalıdır.  ### Item type\'ları  | `item_type` | `party_id` zorunlu? | `config` örneği | |-------------|---------------------|-----------------| | `signature` | ✅ | (yok) | | `text` | ❌ | `{ default_content }` | | `dynamic_text` | ✅ | `{ defaultSource: \"{{signer.full_name}}\" }` | | `cells` | ✅ | `{ cellCount: 11, defaultSource: \"{{signer.government_id}}\" }` | | `date` | ✅ | `{ defaultSource, defaultValue }` | | `dropdown` | ✅ | `{ options: [{label,value}], defaultValue }` | | `checkbox` | ✅ | `{ checkedByDefault: false }` | | `radio` | ✅ | `{ options: [{label,value}], defaultValue }` | | `stamp` | ❌ | `{ stampData: \"data:image/png;base64,...\" }` |  ### Sistem değişkenleri (dynamic_text/cells/date `config.defaultSource`)  `{{signer.first_name}}`, `{{signer.last_name}}`, `{{signer.full_name}}`, `{{signer.email}}`, `{{signer.phone}}`, `{{signer.government_id}}`, `{{signer.birth_date}}`, `{{signer.sign_date}}`, `{{contract.title}}`, `{{sender.full_name}}`, `{{current.date}}`, `{{current.datetime}}`.  ### Workspace izolasyonu  X-API-Key middleware demand\'i workspace\'e göre filtreler; başka workspace\'in demand\'ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece `PENDING` demand edit edilebilir. `COMPLETED`, `EXPIRED`, `REJECTED` için 403.  ### Örnek  ```bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d \'{     \"items\": [       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"signature\",         \"position_x\": 0.5, \"position_y\": 0.85,         \"width\": 0.2, \"height\": 0.05,         \"is_required\": true       },       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"cells\",         \"position_x\": 0.1, \"position_y\": 0.5,         \"width\": 0.4, \"height\": 0.04,         \"slug\": \"tc\",         \"config\": { \"cellCount\": 11, \"defaultSource\": \"{{signer.government_id}}\" }       }     ]   }\' ``` 
         * @summary Sözleşmeye alan yerleştir (replace)
         * @param {DemandsApiApiV1DemandsIdItemsPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdItemsPost(requestParameters: DemandsApiApiV1DemandsIdItemsPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<UpsertItemsResponse> {
            return localVarFp.apiV1DemandsIdItemsPost(requestParameters.id, requestParameters.upsertItemsRequest, options).then((request) => request(axios, basePath));
        },
        /**
         * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field\'ları `variables` payload\'undan doldurur ve imzalama URL\'lerini döner.  **Variable resolution:** - Item\'ın `template_party_id` non-null → `party_mapping[i].variables`\'ta   o slug var ise oradan uygulanır - Yoksa root `variables`\'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value\'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı 
         * @summary Sözleşme oluştur (şablondan)
         * @param {DemandsApiApiV1DemandsPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsPost(requestParameters: DemandsApiApiV1DemandsPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsPost201Response> {
            return localVarFp.apiV1DemandsPost(requestParameters.createDemandRequest, options).then((request) => request(axios, basePath));
        },
        /**
         * Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF\'e birleştirilir, office formatları LibreOffice ile PDF\'e çevrilir. 
         * @summary Dosya upload ile sözleşme oluştur (şablonsuz)
         * @param {DemandsApiApiV1DemandsUploadPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsUploadPost(requestParameters: DemandsApiApiV1DemandsUploadPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsUploadPost201Response> {
            return localVarFp.apiV1DemandsUploadPost(requestParameters.files, requestParameters.parties, requestParameters.order, requestParameters.title, requestParameters.description, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for apiV1DemandsIdEmbedSessionPost operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsIdEmbedSessionPostRequest {
    /**
     * Sözleşme (demand) ID
     */
    readonly id: string

    readonly apiV1DemandsIdEmbedSessionPostRequest: ApiV1DemandsIdEmbedSessionPostRequest
}

/**
 * Request parameters for apiV1DemandsIdGet operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsIdGetRequest {
    readonly id: string
}

/**
 * Request parameters for apiV1DemandsIdItemsPost operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsIdItemsPostRequest {
    readonly id: string

    readonly upsertItemsRequest: UpsertItemsRequest
}

/**
 * Request parameters for apiV1DemandsPost operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsPostRequest {
    readonly createDemandRequest: CreateDemandRequest
}

/**
 * Request parameters for apiV1DemandsUploadPost operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsUploadPostRequest {
    /**
     * 1 belge VEYA 1-20 görsel
     */
    readonly files: Array<File>

    /**
     * JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu). 
     */
    readonly parties: string

    /**
     * Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;)
     */
    readonly order?: string

    readonly title?: string

    readonly description?: string
}

/**
 * DemandsApi - object-oriented interface
 */
export class DemandsApi extends BaseAPI {
    /**
     * Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token\'ı üretir. Dönen `embed_url` bir `<iframe>` içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \"güvenli\" veya \"nitelikli\" sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: `expires_at` alanında belirtilen sürede geçersiz olur. - `embed_allowed_origins` kısıtı: API anahtarına tanımlanmış   izin verilen origin\'ler dışından `<iframe>` açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** `X-Workspace-Id` header\'ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace\'in sözleşmesi için 404 döner (IDOR koruması). 
     * @summary Gömülü imza oturumu başlat (embed token mint)
     * @param {DemandsApiApiV1DemandsIdEmbedSessionPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsIdEmbedSessionPost(requestParameters: DemandsApiApiV1DemandsIdEmbedSessionPostRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsIdEmbedSessionPost(requestParameters.id, requestParameters.apiV1DemandsIdEmbedSessionPostRequest, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * 
     * @summary Sözleşme durumu + imza ilerlemesi
     * @param {DemandsApiApiV1DemandsIdGetRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsIdGet(requestParameters: DemandsApiApiV1DemandsIdGetRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsIdGet(requestParameters.id, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: `POST /api/v1/demands/upload` ile demand yarat (`dispatch_notifications=false` ile auto-dispatch\'i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da `POST /api/v1/demands/{id}/reminders` ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - `page_ids` **omitted** → demand\'in TÜM mevcut item\'ları silinir,   body\'dekiler yaratılır (full replace). - `page_ids: [N, M, ...]` → sadece bu sayfaların item\'ları silinir,   diğer sayfalardaki item\'lar korunur. Body\'deki `items[].page_id`   değerleri `page_ids` listesinde olmalıdır.  ### Item type\'ları  | `item_type` | `party_id` zorunlu? | `config` örneği | |-------------|---------------------|-----------------| | `signature` | ✅ | (yok) | | `text` | ❌ | `{ default_content }` | | `dynamic_text` | ✅ | `{ defaultSource: \"{{signer.full_name}}\" }` | | `cells` | ✅ | `{ cellCount: 11, defaultSource: \"{{signer.government_id}}\" }` | | `date` | ✅ | `{ defaultSource, defaultValue }` | | `dropdown` | ✅ | `{ options: [{label,value}], defaultValue }` | | `checkbox` | ✅ | `{ checkedByDefault: false }` | | `radio` | ✅ | `{ options: [{label,value}], defaultValue }` | | `stamp` | ❌ | `{ stampData: \"data:image/png;base64,...\" }` |  ### Sistem değişkenleri (dynamic_text/cells/date `config.defaultSource`)  `{{signer.first_name}}`, `{{signer.last_name}}`, `{{signer.full_name}}`, `{{signer.email}}`, `{{signer.phone}}`, `{{signer.government_id}}`, `{{signer.birth_date}}`, `{{signer.sign_date}}`, `{{contract.title}}`, `{{sender.full_name}}`, `{{current.date}}`, `{{current.datetime}}`.  ### Workspace izolasyonu  X-API-Key middleware demand\'i workspace\'e göre filtreler; başka workspace\'in demand\'ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece `PENDING` demand edit edilebilir. `COMPLETED`, `EXPIRED`, `REJECTED` için 403.  ### Örnek  ```bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d \'{     \"items\": [       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"signature\",         \"position_x\": 0.5, \"position_y\": 0.85,         \"width\": 0.2, \"height\": 0.05,         \"is_required\": true       },       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"cells\",         \"position_x\": 0.1, \"position_y\": 0.5,         \"width\": 0.4, \"height\": 0.04,         \"slug\": \"tc\",         \"config\": { \"cellCount\": 11, \"defaultSource\": \"{{signer.government_id}}\" }       }     ]   }\' ``` 
     * @summary Sözleşmeye alan yerleştir (replace)
     * @param {DemandsApiApiV1DemandsIdItemsPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsIdItemsPost(requestParameters: DemandsApiApiV1DemandsIdItemsPostRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsIdItemsPost(requestParameters.id, requestParameters.upsertItemsRequest, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field\'ları `variables` payload\'undan doldurur ve imzalama URL\'lerini döner.  **Variable resolution:** - Item\'ın `template_party_id` non-null → `party_mapping[i].variables`\'ta   o slug var ise oradan uygulanır - Yoksa root `variables`\'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value\'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı 
     * @summary Sözleşme oluştur (şablondan)
     * @param {DemandsApiApiV1DemandsPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsPost(requestParameters: DemandsApiApiV1DemandsPostRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsPost(requestParameters.createDemandRequest, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF\'e birleştirilir, office formatları LibreOffice ile PDF\'e çevrilir. 
     * @summary Dosya upload ile sözleşme oluştur (şablonsuz)
     * @param {DemandsApiApiV1DemandsUploadPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsUploadPost(requestParameters: DemandsApiApiV1DemandsUploadPostRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsUploadPost(requestParameters.files, requestParameters.parties, requestParameters.order, requestParameters.title, requestParameters.description, options).then((request) => request(this.axios, this.basePath));
    }
}



/**
 * RemindersApi - axios parameter creator
 */
export const RemindersApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Sözleşmenin **henüz imzalamamış** taraflarına SMS ve/veya e-posta hatırlatması anlık olarak gönderir. Şablon-/sözleşme-seviyesi `reminder_settings` ayarından **bağımsız** olarak çalışır — istediğiniz zaman elle tetiklenebilir.  **Anti-spam koruması:** Aynı sözleşme için son hatırlatmanın üzerinden 5 dakika geçmemişse 429 `RATE_LIMITED` döner ve `Retry-After` header\'ı ile `retry_after_seconds` alanı bilgilendirir. Override için body\'de `force: true` yollayın (yanlışlıkla spam atmamak için kasıtlı kullanın).  **Kişi başına sert sınırlar (override edilemez):** Spam ve gönderici reputasyon koruması için kişi-başına reminder sayısı sınırlıdır:  - **Bir kişiye en fazla 3 SMS reminder** gönderilebilir (otomatik   scheduled + manuel trigger toplam). Sayaç dolu kişi için SMS skip   edilir, `details[]` içinde `reason: \"party_sms_cap_reached (3)\"` görünür. - **Bir kişiye en fazla 3 e-posta reminder** gönderilebilir. Sayaç   dolu kişi için email skip edilir, `reason: \"party_email_cap_reached (3)\"`. - Diğer kişilere gönderim normal şekilde devam eder; tek bir kişinin   sayacı dolu diye tüm çağrı reddedilmez. - `force: true` 5dk anti-spam pencereyi override eder ama kişi-başı   cap\'i ASLA — sert kural. - Sözleşme başına global cap pratikte yok (`999` safety net) — kural   kişi başınadır.  Sayım kaynağı: `ReminderLog` tablosu (channel + party_id, `status=\'SENT\'`). Hem otomatik scheduled (ReminderWorker) hem manuel trigger reminders tek toplamda sayılır.  **Workspace izolasyonu:** `X-Workspace-Id` header\'ıyla erişilen organizasyonun sözleşmesi olmalıdır; aksi halde 404 `DEMAND_NOT_FOUND` (IDOR shield).  **Kanal eligibility:** Bir parti için - `email` kanalı: `party.email` dolu **ve** `party.send_email=true` **ve**   `demand.send_email_notifications=true` ise gönderilir - `sms` kanalı: `party.phone` dolu **ve** `party.send_sms=true` **ve**   `demand.send_sms_notifications=true` ise gönderilir  **Mesaj içeriği:** Şablonun `sms_reminder_message` alanı (varsa) + `signer.first_name` / `{{name}}` / `{{link}}` gibi sistem değişkenleri substitute edilir. Şablonda yoksa sistem default reminder mesajı kullanılır. 
         * @summary Anlık hatırlatma tetikle (imzalanmamış taraflara)
         * @param {string} id Sözleşme (demand) ID
         * @param {TriggerReminderRequest} [triggerReminderRequest] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdRemindersPost: async (id: string, triggerReminderRequest?: TriggerReminderRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1DemandsIdRemindersPost', 'id', id)
            const localVarPath = `/api/v1/demands/{id}/reminders`
                .replace('{id}', encodeURIComponent(String(id)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)

            localVarHeaderParameter['Content-Type'] = 'application/json';
            localVarHeaderParameter['Accept'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(triggerReminderRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * RemindersApi - functional programming interface
 */
export const RemindersApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = RemindersApiAxiosParamCreator(configuration)
    return {
        /**
         * Sözleşmenin **henüz imzalamamış** taraflarına SMS ve/veya e-posta hatırlatması anlık olarak gönderir. Şablon-/sözleşme-seviyesi `reminder_settings` ayarından **bağımsız** olarak çalışır — istediğiniz zaman elle tetiklenebilir.  **Anti-spam koruması:** Aynı sözleşme için son hatırlatmanın üzerinden 5 dakika geçmemişse 429 `RATE_LIMITED` döner ve `Retry-After` header\'ı ile `retry_after_seconds` alanı bilgilendirir. Override için body\'de `force: true` yollayın (yanlışlıkla spam atmamak için kasıtlı kullanın).  **Kişi başına sert sınırlar (override edilemez):** Spam ve gönderici reputasyon koruması için kişi-başına reminder sayısı sınırlıdır:  - **Bir kişiye en fazla 3 SMS reminder** gönderilebilir (otomatik   scheduled + manuel trigger toplam). Sayaç dolu kişi için SMS skip   edilir, `details[]` içinde `reason: \"party_sms_cap_reached (3)\"` görünür. - **Bir kişiye en fazla 3 e-posta reminder** gönderilebilir. Sayaç   dolu kişi için email skip edilir, `reason: \"party_email_cap_reached (3)\"`. - Diğer kişilere gönderim normal şekilde devam eder; tek bir kişinin   sayacı dolu diye tüm çağrı reddedilmez. - `force: true` 5dk anti-spam pencereyi override eder ama kişi-başı   cap\'i ASLA — sert kural. - Sözleşme başına global cap pratikte yok (`999` safety net) — kural   kişi başınadır.  Sayım kaynağı: `ReminderLog` tablosu (channel + party_id, `status=\'SENT\'`). Hem otomatik scheduled (ReminderWorker) hem manuel trigger reminders tek toplamda sayılır.  **Workspace izolasyonu:** `X-Workspace-Id` header\'ıyla erişilen organizasyonun sözleşmesi olmalıdır; aksi halde 404 `DEMAND_NOT_FOUND` (IDOR shield).  **Kanal eligibility:** Bir parti için - `email` kanalı: `party.email` dolu **ve** `party.send_email=true` **ve**   `demand.send_email_notifications=true` ise gönderilir - `sms` kanalı: `party.phone` dolu **ve** `party.send_sms=true` **ve**   `demand.send_sms_notifications=true` ise gönderilir  **Mesaj içeriği:** Şablonun `sms_reminder_message` alanı (varsa) + `signer.first_name` / `{{name}}` / `{{link}}` gibi sistem değişkenleri substitute edilir. Şablonda yoksa sistem default reminder mesajı kullanılır. 
         * @summary Anlık hatırlatma tetikle (imzalanmamış taraflara)
         * @param {string} id Sözleşme (demand) ID
         * @param {TriggerReminderRequest} [triggerReminderRequest] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsIdRemindersPost(id: string, triggerReminderRequest?: TriggerReminderRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsIdRemindersPost200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsIdRemindersPost(id, triggerReminderRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['RemindersApi.apiV1DemandsIdRemindersPost']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * RemindersApi - factory interface
 */
export const RemindersApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = RemindersApiFp(configuration)
    return {
        /**
         * Sözleşmenin **henüz imzalamamış** taraflarına SMS ve/veya e-posta hatırlatması anlık olarak gönderir. Şablon-/sözleşme-seviyesi `reminder_settings` ayarından **bağımsız** olarak çalışır — istediğiniz zaman elle tetiklenebilir.  **Anti-spam koruması:** Aynı sözleşme için son hatırlatmanın üzerinden 5 dakika geçmemişse 429 `RATE_LIMITED` döner ve `Retry-After` header\'ı ile `retry_after_seconds` alanı bilgilendirir. Override için body\'de `force: true` yollayın (yanlışlıkla spam atmamak için kasıtlı kullanın).  **Kişi başına sert sınırlar (override edilemez):** Spam ve gönderici reputasyon koruması için kişi-başına reminder sayısı sınırlıdır:  - **Bir kişiye en fazla 3 SMS reminder** gönderilebilir (otomatik   scheduled + manuel trigger toplam). Sayaç dolu kişi için SMS skip   edilir, `details[]` içinde `reason: \"party_sms_cap_reached (3)\"` görünür. - **Bir kişiye en fazla 3 e-posta reminder** gönderilebilir. Sayaç   dolu kişi için email skip edilir, `reason: \"party_email_cap_reached (3)\"`. - Diğer kişilere gönderim normal şekilde devam eder; tek bir kişinin   sayacı dolu diye tüm çağrı reddedilmez. - `force: true` 5dk anti-spam pencereyi override eder ama kişi-başı   cap\'i ASLA — sert kural. - Sözleşme başına global cap pratikte yok (`999` safety net) — kural   kişi başınadır.  Sayım kaynağı: `ReminderLog` tablosu (channel + party_id, `status=\'SENT\'`). Hem otomatik scheduled (ReminderWorker) hem manuel trigger reminders tek toplamda sayılır.  **Workspace izolasyonu:** `X-Workspace-Id` header\'ıyla erişilen organizasyonun sözleşmesi olmalıdır; aksi halde 404 `DEMAND_NOT_FOUND` (IDOR shield).  **Kanal eligibility:** Bir parti için - `email` kanalı: `party.email` dolu **ve** `party.send_email=true` **ve**   `demand.send_email_notifications=true` ise gönderilir - `sms` kanalı: `party.phone` dolu **ve** `party.send_sms=true` **ve**   `demand.send_sms_notifications=true` ise gönderilir  **Mesaj içeriği:** Şablonun `sms_reminder_message` alanı (varsa) + `signer.first_name` / `{{name}}` / `{{link}}` gibi sistem değişkenleri substitute edilir. Şablonda yoksa sistem default reminder mesajı kullanılır. 
         * @summary Anlık hatırlatma tetikle (imzalanmamış taraflara)
         * @param {RemindersApiApiV1DemandsIdRemindersPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdRemindersPost(requestParameters: RemindersApiApiV1DemandsIdRemindersPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsIdRemindersPost200Response> {
            return localVarFp.apiV1DemandsIdRemindersPost(requestParameters.id, requestParameters.triggerReminderRequest, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for apiV1DemandsIdRemindersPost operation in RemindersApi.
 */
export interface RemindersApiApiV1DemandsIdRemindersPostRequest {
    /**
     * Sözleşme (demand) ID
     */
    readonly id: string

    readonly triggerReminderRequest?: TriggerReminderRequest
}

/**
 * RemindersApi - object-oriented interface
 */
export class RemindersApi extends BaseAPI {
    /**
     * Sözleşmenin **henüz imzalamamış** taraflarına SMS ve/veya e-posta hatırlatması anlık olarak gönderir. Şablon-/sözleşme-seviyesi `reminder_settings` ayarından **bağımsız** olarak çalışır — istediğiniz zaman elle tetiklenebilir.  **Anti-spam koruması:** Aynı sözleşme için son hatırlatmanın üzerinden 5 dakika geçmemişse 429 `RATE_LIMITED` döner ve `Retry-After` header\'ı ile `retry_after_seconds` alanı bilgilendirir. Override için body\'de `force: true` yollayın (yanlışlıkla spam atmamak için kasıtlı kullanın).  **Kişi başına sert sınırlar (override edilemez):** Spam ve gönderici reputasyon koruması için kişi-başına reminder sayısı sınırlıdır:  - **Bir kişiye en fazla 3 SMS reminder** gönderilebilir (otomatik   scheduled + manuel trigger toplam). Sayaç dolu kişi için SMS skip   edilir, `details[]` içinde `reason: \"party_sms_cap_reached (3)\"` görünür. - **Bir kişiye en fazla 3 e-posta reminder** gönderilebilir. Sayaç   dolu kişi için email skip edilir, `reason: \"party_email_cap_reached (3)\"`. - Diğer kişilere gönderim normal şekilde devam eder; tek bir kişinin   sayacı dolu diye tüm çağrı reddedilmez. - `force: true` 5dk anti-spam pencereyi override eder ama kişi-başı   cap\'i ASLA — sert kural. - Sözleşme başına global cap pratikte yok (`999` safety net) — kural   kişi başınadır.  Sayım kaynağı: `ReminderLog` tablosu (channel + party_id, `status=\'SENT\'`). Hem otomatik scheduled (ReminderWorker) hem manuel trigger reminders tek toplamda sayılır.  **Workspace izolasyonu:** `X-Workspace-Id` header\'ıyla erişilen organizasyonun sözleşmesi olmalıdır; aksi halde 404 `DEMAND_NOT_FOUND` (IDOR shield).  **Kanal eligibility:** Bir parti için - `email` kanalı: `party.email` dolu **ve** `party.send_email=true` **ve**   `demand.send_email_notifications=true` ise gönderilir - `sms` kanalı: `party.phone` dolu **ve** `party.send_sms=true` **ve**   `demand.send_sms_notifications=true` ise gönderilir  **Mesaj içeriği:** Şablonun `sms_reminder_message` alanı (varsa) + `signer.first_name` / `{{name}}` / `{{link}}` gibi sistem değişkenleri substitute edilir. Şablonda yoksa sistem default reminder mesajı kullanılır. 
     * @summary Anlık hatırlatma tetikle (imzalanmamış taraflara)
     * @param {RemindersApiApiV1DemandsIdRemindersPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsIdRemindersPost(requestParameters: RemindersApiApiV1DemandsIdRemindersPostRequest, options?: RawAxiosRequestConfig) {
        return RemindersApiFp(this.configuration).apiV1DemandsIdRemindersPost(requestParameters.id, requestParameters.triggerReminderRequest, options).then((request) => request(this.axios, this.basePath));
    }
}



/**
 * TemplatesApi - axios parameter creator
 */
export const TemplatesApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Aktif şablonlarınızı listeler.
         * @summary Şablon listesi
         * @param {number} [page] 
         * @param {number} [limit] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TemplatesGet: async (page?: number, limit?: number, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/api/v1/templates`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'GET', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)

            if (page !== undefined) {
                localVarQueryParameter['page'] = page;
            }

            if (limit !== undefined) {
                localVarQueryParameter['limit'] = limit;
            }

            localVarHeaderParameter['Accept'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Şablonun parties + variables bilgisini döner. variables array\'ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
         * @summary Şablon detay
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TemplatesIdGet: async (id: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1TemplatesIdGet', 'id', id)
            const localVarPath = `/api/v1/templates/{id}`
                .replace('{id}', encodeURIComponent(String(id)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'GET', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)

            localVarHeaderParameter['Accept'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Bu şablonu API üzerinden çağırmak için tam rehber döner: - `endpoint` (POST URL\'i) - `required_headers` (X-API-Key, X-Workspace-Id, Content-Type) - `parties` (her partinin desteklediği field listesi) - `variables` (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - `example_request` (tam curl + JSON örneği, gerçek slug\'larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug\'larla doludur, root `variables` partisiz field\'lar için. 
         * @summary Şablon kullanım kılavuzu (curl + JSON örnek)
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TemplatesIdUsageGet: async (id: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1TemplatesIdUsageGet', 'id', id)
            const localVarPath = `/api/v1/templates/{id}/usage`
                .replace('{id}', encodeURIComponent(String(id)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'GET', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)

            localVarHeaderParameter['Accept'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * TemplatesApi - functional programming interface
 */
export const TemplatesApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = TemplatesApiAxiosParamCreator(configuration)
    return {
        /**
         * Aktif şablonlarınızı listeler.
         * @summary Şablon listesi
         * @param {number} [page] 
         * @param {number} [limit] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1TemplatesGet(page?: number, limit?: number, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1TemplatesGet200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1TemplatesGet(page, limit, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['TemplatesApi.apiV1TemplatesGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Şablonun parties + variables bilgisini döner. variables array\'ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
         * @summary Şablon detay
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1TemplatesIdGet(id: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1TemplatesIdGet200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1TemplatesIdGet(id, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['TemplatesApi.apiV1TemplatesIdGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Bu şablonu API üzerinden çağırmak için tam rehber döner: - `endpoint` (POST URL\'i) - `required_headers` (X-API-Key, X-Workspace-Id, Content-Type) - `parties` (her partinin desteklediği field listesi) - `variables` (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - `example_request` (tam curl + JSON örneği, gerçek slug\'larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug\'larla doludur, root `variables` partisiz field\'lar için. 
         * @summary Şablon kullanım kılavuzu (curl + JSON örnek)
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1TemplatesIdUsageGet(id: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1TemplatesIdUsageGet200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1TemplatesIdUsageGet(id, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['TemplatesApi.apiV1TemplatesIdUsageGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * TemplatesApi - factory interface
 */
export const TemplatesApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = TemplatesApiFp(configuration)
    return {
        /**
         * Aktif şablonlarınızı listeler.
         * @summary Şablon listesi
         * @param {TemplatesApiApiV1TemplatesGetRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TemplatesGet(requestParameters: TemplatesApiApiV1TemplatesGetRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1TemplatesGet200Response> {
            return localVarFp.apiV1TemplatesGet(requestParameters.page, requestParameters.limit, options).then((request) => request(axios, basePath));
        },
        /**
         * Şablonun parties + variables bilgisini döner. variables array\'ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
         * @summary Şablon detay
         * @param {TemplatesApiApiV1TemplatesIdGetRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TemplatesIdGet(requestParameters: TemplatesApiApiV1TemplatesIdGetRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1TemplatesIdGet200Response> {
            return localVarFp.apiV1TemplatesIdGet(requestParameters.id, options).then((request) => request(axios, basePath));
        },
        /**
         * Bu şablonu API üzerinden çağırmak için tam rehber döner: - `endpoint` (POST URL\'i) - `required_headers` (X-API-Key, X-Workspace-Id, Content-Type) - `parties` (her partinin desteklediği field listesi) - `variables` (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - `example_request` (tam curl + JSON örneği, gerçek slug\'larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug\'larla doludur, root `variables` partisiz field\'lar için. 
         * @summary Şablon kullanım kılavuzu (curl + JSON örnek)
         * @param {TemplatesApiApiV1TemplatesIdUsageGetRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TemplatesIdUsageGet(requestParameters: TemplatesApiApiV1TemplatesIdUsageGetRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1TemplatesIdUsageGet200Response> {
            return localVarFp.apiV1TemplatesIdUsageGet(requestParameters.id, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for apiV1TemplatesGet operation in TemplatesApi.
 */
export interface TemplatesApiApiV1TemplatesGetRequest {
    readonly page?: number

    readonly limit?: number
}

/**
 * Request parameters for apiV1TemplatesIdGet operation in TemplatesApi.
 */
export interface TemplatesApiApiV1TemplatesIdGetRequest {
    readonly id: string
}

/**
 * Request parameters for apiV1TemplatesIdUsageGet operation in TemplatesApi.
 */
export interface TemplatesApiApiV1TemplatesIdUsageGetRequest {
    readonly id: string
}

/**
 * TemplatesApi - object-oriented interface
 */
export class TemplatesApi extends BaseAPI {
    /**
     * Aktif şablonlarınızı listeler.
     * @summary Şablon listesi
     * @param {TemplatesApiApiV1TemplatesGetRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1TemplatesGet(requestParameters: TemplatesApiApiV1TemplatesGetRequest = {}, options?: RawAxiosRequestConfig) {
        return TemplatesApiFp(this.configuration).apiV1TemplatesGet(requestParameters.page, requestParameters.limit, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Şablonun parties + variables bilgisini döner. variables array\'ı tüm FILLABLE_TYPES tiplerini içerir (dynamic_text, cells, date, dropdown, text). Slug bazında dedupe; multi-party şablonlarda aynı slug birden fazla partide olabilir, her parti için ayrı satır. 
     * @summary Şablon detay
     * @param {TemplatesApiApiV1TemplatesIdGetRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1TemplatesIdGet(requestParameters: TemplatesApiApiV1TemplatesIdGetRequest, options?: RawAxiosRequestConfig) {
        return TemplatesApiFp(this.configuration).apiV1TemplatesIdGet(requestParameters.id, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Bu şablonu API üzerinden çağırmak için tam rehber döner: - `endpoint` (POST URL\'i) - `required_headers` (X-API-Key, X-Workspace-Id, Content-Type) - `parties` (her partinin desteklediği field listesi) - `variables` (her field için slug, label, item_type, is_required,   default_source, auto_filled, template_party_id) - `example_request` (tam curl + JSON örneği, gerçek slug\'larla)  Multi-party şablonlarda example.party_mapping[i].variables uygun slug\'larla doludur, root `variables` partisiz field\'lar için. 
     * @summary Şablon kullanım kılavuzu (curl + JSON örnek)
     * @param {TemplatesApiApiV1TemplatesIdUsageGetRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1TemplatesIdUsageGet(requestParameters: TemplatesApiApiV1TemplatesIdUsageGetRequest, options?: RawAxiosRequestConfig) {
        return TemplatesApiFp(this.configuration).apiV1TemplatesIdUsageGet(requestParameters.id, options).then((request) => request(this.axios, this.basePath));
    }
}



/**
 * TimestampsApi - axios parameter creator
 */
export const TimestampsApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Dosyanın SHA-256 hash\'ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - `owner_first_name` / `owner_last_name` alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Nitelikli elektronik imza (QES) için ayrı imzalama akışını kullanın.  **İdempotency:** `Idempotency-Key` header\'ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı `id` döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - `multipart/form-data`: `file` alanıyla ikili dosya yükleme - `application/json`: `file_base64` alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 
         * @summary Zaman damgası oluştur (eser tescil)
         * @param {File} file Damgalanacak dosya (maks. 50 MB)
         * @param {string} [idempotencyKey] Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez. 
         * @param {string} [description] Kayıt açıklaması (opsiyonel, max 500 karakter)
         * @param {string} [ownerFirstName] Dosya sahibinin adı (opsiyonel, kullanıcı beyanı)
         * @param {string} [ownerLastName] Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı)
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TimestampsPost: async (file: File, idempotencyKey?: string, description?: string, ownerFirstName?: string, ownerLastName?: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'file' is not null or undefined
            assertParamExists('apiV1TimestampsPost', 'file', file)
            const localVarPath = `/api/v1/timestamps`;
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'POST', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;
            const localVarFormParams = new ((configuration && configuration.formDataCtor) || FormData)();

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)


            if (file !== undefined) { 
                localVarFormParams.append('file', file as any);
            }

            if (description !== undefined) { 
                localVarFormParams.append('description', description as any);
            }

            if (ownerFirstName !== undefined) { 
                localVarFormParams.append('owner_first_name', ownerFirstName as any);
            }

            if (ownerLastName !== undefined) { 
                localVarFormParams.append('owner_last_name', ownerLastName as any);
            }
            localVarHeaderParameter['Content-Type'] = 'multipart/form-data';
            localVarHeaderParameter['Accept'] = 'application/json';

            if (idempotencyKey != null) {
                localVarHeaderParameter['Idempotency-Key'] = String(idempotencyKey);
            }
            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = localVarFormParams;

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * TimestampsApi - functional programming interface
 */
export const TimestampsApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = TimestampsApiAxiosParamCreator(configuration)
    return {
        /**
         * Dosyanın SHA-256 hash\'ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - `owner_first_name` / `owner_last_name` alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Nitelikli elektronik imza (QES) için ayrı imzalama akışını kullanın.  **İdempotency:** `Idempotency-Key` header\'ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı `id` döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - `multipart/form-data`: `file` alanıyla ikili dosya yükleme - `application/json`: `file_base64` alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 
         * @summary Zaman damgası oluştur (eser tescil)
         * @param {File} file Damgalanacak dosya (maks. 50 MB)
         * @param {string} [idempotencyKey] Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez. 
         * @param {string} [description] Kayıt açıklaması (opsiyonel, max 500 karakter)
         * @param {string} [ownerFirstName] Dosya sahibinin adı (opsiyonel, kullanıcı beyanı)
         * @param {string} [ownerLastName] Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı)
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1TimestampsPost(file: File, idempotencyKey?: string, description?: string, ownerFirstName?: string, ownerLastName?: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1TimestampsPost201Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1TimestampsPost(file, idempotencyKey, description, ownerFirstName, ownerLastName, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['TimestampsApi.apiV1TimestampsPost']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * TimestampsApi - factory interface
 */
export const TimestampsApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = TimestampsApiFp(configuration)
    return {
        /**
         * Dosyanın SHA-256 hash\'ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - `owner_first_name` / `owner_last_name` alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Nitelikli elektronik imza (QES) için ayrı imzalama akışını kullanın.  **İdempotency:** `Idempotency-Key` header\'ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı `id` döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - `multipart/form-data`: `file` alanıyla ikili dosya yükleme - `application/json`: `file_base64` alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 
         * @summary Zaman damgası oluştur (eser tescil)
         * @param {TimestampsApiApiV1TimestampsPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TimestampsPost(requestParameters: TimestampsApiApiV1TimestampsPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1TimestampsPost201Response> {
            return localVarFp.apiV1TimestampsPost(requestParameters.file, requestParameters.idempotencyKey, requestParameters.description, requestParameters.ownerFirstName, requestParameters.ownerLastName, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for apiV1TimestampsPost operation in TimestampsApi.
 */
export interface TimestampsApiApiV1TimestampsPostRequest {
    /**
     * Damgalanacak dosya (maks. 50 MB)
     */
    readonly file: File

    /**
     * Tekrar eden istekleri önlemek için istemci tarafında üretilen benzersiz anahtar (UUID önerilir). 5 dakika içinde aynı key ile yapılan istek önceki sonucu döner; yeni damga alınmaz, kredi kesilmez. 
     */
    readonly idempotencyKey?: string

    /**
     * Kayıt açıklaması (opsiyonel, max 500 karakter)
     */
    readonly description?: string

    /**
     * Dosya sahibinin adı (opsiyonel, kullanıcı beyanı)
     */
    readonly ownerFirstName?: string

    /**
     * Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı)
     */
    readonly ownerLastName?: string
}

/**
 * TimestampsApi - object-oriented interface
 */
export class TimestampsApi extends BaseAPI {
    /**
     * Dosyanın SHA-256 hash\'ini RFC 3161 standardında TÜBİTAK KAMU SM TSA ile imzalar ve bir zaman damgası kaydı oluşturur.  **Damga neyi kanıtlar:** - Dosyanın bu tarih ve saatte var olduğunu (varlık kanıtı) - Dosya içeriğinin o andan bu yana değişmediğini (bütünlük kanıtı)  **Damga neyi kanıtlamaz:** - Dosyanın kim tarafından yazıldığını (yazarlık kanıtı DEĞİL) - `owner_first_name` / `owner_last_name` alanları bilgilendirme amaçlıdır;   sahiplik beyanı kullanıcı tarafından yapılır, API tarafından doğrulanmaz.  Damga elektronik imza DEĞİLDİR. Nitelikli elektronik imza (QES) için ayrı imzalama akışını kullanın.  **İdempotency:** `Idempotency-Key` header\'ı (UUID önerilir) ile aynı istek tekrar gönderilirse 5 dakika içinde aynı `id` döner, yeni damga alınmaz ve kredi kesilmez.  **İki içerik formatı desteklenir:** - `multipart/form-data`: `file` alanıyla ikili dosya yükleme - `application/json`: `file_base64` alanıyla standart Base64 (RFC 4648 §4,   canonical alfabe — data URL öneki veya URL-safe alfabe kabul edilmez) 
     * @summary Zaman damgası oluştur (eser tescil)
     * @param {TimestampsApiApiV1TimestampsPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1TimestampsPost(requestParameters: TimestampsApiApiV1TimestampsPostRequest, options?: RawAxiosRequestConfig) {
        return TimestampsApiFp(this.configuration).apiV1TimestampsPost(requestParameters.file, requestParameters.idempotencyKey, requestParameters.description, requestParameters.ownerFirstName, requestParameters.ownerLastName, options).then((request) => request(this.axios, this.basePath));
    }
}



