/* tslint:disable */
/* eslint-disable */
/**
 * imzala External API
 * imzala.org dış API\'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.8.10 · **Son güncelleme:** 2026-09-15  ## Auth Tüm istekler `X-API-Key` header\'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API key kullanıyorsanız `X-Workspace-Id` header\'ı göndermeniz gerekir (organizasyon UUID\'si). Kişisel anahtarlar için bu header gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field\'lar) `POST /api/v1/demands` payload\'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field\'lar   (örn. Kira sözleşmesinde Kiraya Veren\'in `address`, `iban` field\'ları) - `variables` (root) — **partilerden bağımsız** field\'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item\'ın template_party_id\'si var ve o parti slug\'ı göndermişse → uygula 2. Yoksa root `variables`\'tan ara → varsa uygula 3. Yoksa atla  Dashboard\'daki **API Kullanımı** tab\'ı (`/sablonlar/<id>`) hangi field\'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint\'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array\'ı, gönderdiğiniz ama şablonda eşleşmeyen slug\'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log\'ta veya dashboard\'da kontrol edin.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |----|-------|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard\'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta\'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker\'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body\'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default\'unu   ezer, sadece bu demand\'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint\'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response\'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility\'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d \'{}\'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d \'{\"channels\": [\"sms\"], \"force\": true}\' ```  Detay için **Reminders** tag\'i altındaki endpoint\'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL\'ye `POST` ile JSON payload gönderir. Webhook\'lar dashboard\'dan yönetilir: **Ayarlar -> Webhook\'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook\'u** (org workspace\'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event\'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace\'te) → sadece sizin kendi   event\'lerinizde tetiklenir  ### Olay tipleri (8) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı |  ### Header\'lar Her istekte aşağıdaki header\'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB\'de unique key). - `type` — yukarıdaki 8 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header\'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require(\'crypto\');  function verify(rawBody, header, secret) {   const expected = \'sha256=\' + crypto     .createHmac(\'sha256\', secret)     .update(rawBody, \'utf8\')     .digest(\'hex\');   return crypto.timingSafeEqual(     Buffer.from(header || \'\', \'utf8\'),     Buffer.from(expected, \'utf8\')   ); }  // Express app.post(\'/webhook\', express.raw({ type: \'application/json\' }), (req, res) => {   const sig = req.header(\'X-Imzala-Signature-256\');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send(\'invalid signature\');   }   const event = JSON.parse(req.body.toString(\'utf8\'));   // ... event\'i kuyruğa koy ve hemen 2xx dön   res.status(200).send(\'ok\'); }); ```  > **Önemli:** Body\'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware\'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard\'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint\'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB\'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix\'inden sonra kayıp event\'leri yakalamak) için bazı payload\'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow\'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send(\'replay accepted\'); } ```  ### Manuel yeniden gönderim Dashboard\'da `Ayarlar -> Webhook\'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5\'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`\'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn\'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload\'larda side-effect\'leri atla. 5. `X-Imzala-Delivery` UUID\'sini log\'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret\'i env var\'da sakla, koda gömme. 
 *
 * The version of the OpenAPI document: 1.8.10
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
export interface ApiV1ContactsGet200Response {
    'success'?: boolean;
    'data'?: ApiV1ContactsGet200ResponseData;
}
export interface ApiV1ContactsGet200ResponseData {
    'contacts'?: Array<ContactSummary>;
    'total'?: number;
    'page'?: number;
    'limit'?: number;
}
export interface ApiV1ContactsPost201Response {
    'success'?: boolean;
    'data'?: ContactSummary;
}
export interface ApiV1ContactsPostRequest {
    'first_name': string;
    'last_name': string;
    'email'?: string | null;
    'phone'?: string | null;
    'job_title'?: string | null;
    'company_id'?: string | null;
    'notes'?: string | null;
    'address_country'?: string | null;
    'address_city'?: string | null;
    'address_district'?: string | null;
    'address_line'?: string | null;
}
export interface ApiV1DemandsBulkPost200Response {
    'success'?: boolean;
    'data'?: ApiV1DemandsBulkPost200ResponseData;
}
export interface ApiV1DemandsBulkPost200ResponseData {
    'template_id'?: string;
    'total'?: number;
    'created'?: number;
    'failed'?: number;
    'results'?: Array<ApiV1DemandsBulkPost200ResponseDataResultsInner>;
}
export interface ApiV1DemandsBulkPost200ResponseDataResultsInner {
    'row_index'?: number;
    'status'?: ApiV1DemandsBulkPost200ResponseDataResultsInnerStatusEnum;
    /**
     * status=created ise; veya status=failed olup sözleşme oluşturulduktan SONRA kredi mutabakatı reddettiyse (taslak, davet gönderilmedi)
     */
    'demand_id'?: string;
    'signing_urls'?: Array<ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner>;
    'result_url'?: string;
    /**
     * Gönderilen davet (SMS+e-posta) sayısı
     */
    'dispatched'?: number;
    /**
     * status=failed ise makinece okunabilir kod (VALIDATION, INSUFFICIENT_CREDITS, MEMBER_LIMIT_EXCEEDED, CREATE_FAILED vb.)
     */
    'error'?: string;
    /**
     * status=failed ise açıklama
     */
    'message'?: string;
}

export const ApiV1DemandsBulkPost200ResponseDataResultsInnerStatusEnum = {
    Created: 'created',
    Failed: 'failed',
} as const;

export type ApiV1DemandsBulkPost200ResponseDataResultsInnerStatusEnum = typeof ApiV1DemandsBulkPost200ResponseDataResultsInnerStatusEnum[keyof typeof ApiV1DemandsBulkPost200ResponseDataResultsInnerStatusEnum];

export interface ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner {
    'party_id'?: string;
    'first_name'?: string;
    'last_name'?: string;
    'email'?: string;
    'phone'?: string;
    'signing_url'?: string;
}
export interface ApiV1DemandsBulkPostRequest {
    /**
     * Sözleşmelerin oluşturulacağı şablon
     */
    'template_id': string;
    'options'?: ApiV1DemandsBulkPostRequestOptions;
    /**
     * Her biri ayrı bir sözleşme olacak satırlar (en fazla 10)
     */
    'rows': Array<ApiV1DemandsBulkPostRequestRowsInner>;
}
/**
 * Tüm satırlara uygulanan batch-seviye seçenekler (opsiyonel)
 */
export interface ApiV1DemandsBulkPostRequestOptions {
    /**
     * false ise davet gönderilmez (yalnızca oluşturulur)
     */
    'dispatch_notifications'?: boolean;
    /**
     * true ise taraflar sırayla imzalar
     */
    'ordered'?: boolean;
    /**
     * SMS davet metni özelleştirmesi (yalnızca kendi SMS sağlayıcısı olan org\'lar)
     */
    'sms_content'?: string;
    'language'?: string;
    /**
     * 🔴 Faturayı etkiler. Nitelikli imza (QES) sınıfı; imzacı başına taban ücret 1 yerine 2 kredi.
     */
    'enable_qes'?: boolean;
    /**
     * 🔴 Faturayı etkiler. Yalnız enable_qes ile anlamlıdır: B-T +0, B-LT +0, B-LTA +1 kredi (imzacı başına).
     */
    'qes_pades_level'?: ApiV1DemandsBulkPostRequestOptionsQesPadesLevelEnum;
    /**
     * 🔴 Faturayı etkiler. AB nitelikli zaman damgası; satır (sözleşme) başına +1 kredi.
     */
    'eidas_timestamp'?: boolean;
    /**
     * 🔴 Faturayı etkiler. Kimlik fotoğrafı doğrulaması; imzacı başına +1 kredi.
     */
    'require_id_photo'?: boolean;
    /**
     * 🔴 Faturayı etkiler. Canlılık doğrulaması; imzacı başına +1 kredi.
     */
    'require_liveness_check'?: boolean;
}

export const ApiV1DemandsBulkPostRequestOptionsQesPadesLevelEnum = {
    PadEsBaselineT: 'PAdES_BASELINE_T',
    PadEsBaselineLt: 'PAdES_BASELINE_LT',
    PadEsBaselineLta: 'PAdES_BASELINE_LTA',
} as const;

export type ApiV1DemandsBulkPostRequestOptionsQesPadesLevelEnum = typeof ApiV1DemandsBulkPostRequestOptionsQesPadesLevelEnum[keyof typeof ApiV1DemandsBulkPostRequestOptionsQesPadesLevelEnum];

export interface ApiV1DemandsBulkPostRequestRowsInner {
    /**
     * Bu satırın taraf eşlemesi (POST /demands ile aynı şekil)
     */
    'party_mapping': Array<ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner>;
    /**
     * Satır-kök değişkenler
     */
    'variables'?: object;
}
export interface ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner {
    'template_party_id'?: string;
    'first_name'?: string;
    'last_name'?: string;
    'email'?: string;
    /**
     * E.164 biçiminde telefon (ör. `+905551234567`). Ülke kodu ile birlikte gönderin; fazlalık şehirlerarası öneki (TRde baş sıfır, `+90 0542…`) sunucuda kaldırılır ve numara kanonik E.164 biçiminde saklanır. Tanınmayan girdi olduğu gibi korunur ama SMS teslim edilemeyebilir.
     */
    'phone'?: string;
    /**
     * T.C. kimlik numarası (11 hane)
     */
    'government_id'?: string;
    /**
     * ISO 8601 (ör. 1990-05-15)
     */
    'birth_date'?: string;
    'send_sms'?: boolean;
    'send_email'?: boolean;
    /**
     * Bu tarafa özel davet mesajı
     */
    'custom_message'?: string;
    /**
     * Bu tarafa özel değişkenler
     */
    'variables'?: object;
}
export interface ApiV1DemandsDemandIdDispatchPost200Response {
    'success'?: boolean;
    'data'?: ApiV1DemandsDemandIdDispatchPost200ResponseData;
}
export interface ApiV1DemandsDemandIdDispatchPost200ResponseData {
    'demand_id'?: string;
    'status'?: string;
    /**
     * `false` ise sözleşme zaten yayınlanmıştı (`ALREADY`); yalnız davet fazı tekrar çalıştı, `demand.dispatched` webhook olayı TEKRAR yayılmadı. 
     */
    'dispatched'?: boolean;
    'credits'?: ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits;
    'invitations'?: ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations;
}
export interface ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits {
    /**
     * Bu istekte düşülen kredi.
     */
    'charged'?: number;
    /**
     * Bu istekte iade edilen kredi.
     */
    'refunded'?: number;
    /**
     * Sözleşmenin toplam beklenen maliyeti.
     */
    'expected'?: number;
}
export interface ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations {
    /**
     * En az bir kanaldan başarıyla gönderilen taraf sayısı.
     */
    'sent'?: number;
    'results'?: Array<ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner>;
    /**
     * `DISPATCH_SKIPPED` — sözleşme satırı okunamadı, körlemesine gönderim yapılmadı. (Taraf sayısı tavanı bu uçta davet fazından ÖNCE denetlenir ve **409 `DISPATCH_TOO_MANY`** olarak döner; 200 gövdesinde görünmez.) 
     */
    'error'?: ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsErrorEnum | null;
}

export const ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsErrorEnum = {
    DispatchSkipped: 'DISPATCH_SKIPPED',
} as const;

export type ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsErrorEnum = typeof ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsErrorEnum[keyof typeof ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsErrorEnum];

export interface ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner {
    'party_id'?: string;
    'sms'?: boolean;
    'email'?: boolean;
    'whatsapp'?: boolean;
    /**
     * Yalnız o taraf için dolar. `DISPATCH_FAILED` — gönderim patladı. `RECIPIENT_QUOTA_EXCEEDED` — alıcı ekseni freni (telefon başına 3/sa + 10/gün); yalnız bu taraf ATLANDI, diğerleri gönderilmeye devam etti. 
     */
    'error'?: ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInnerErrorEnum | null;
}

export const ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInnerErrorEnum = {
    DispatchFailed: 'DISPATCH_FAILED',
    RecipientQuotaExceeded: 'RECIPIENT_QUOTA_EXCEEDED',
} as const;

export type ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInnerErrorEnum = typeof ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInnerErrorEnum[keyof typeof ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInnerErrorEnum];

export interface ApiV1DemandsDemandIdDispatchPostRequest {
    'send_invitations'?: ApiV1DemandsDemandIdDispatchPostRequestSendInvitations;
}
/**
 * @type ApiV1DemandsDemandIdDispatchPostRequestSendInvitations
 * Hangi kanaldan davet gideceğini **daraltır** (sözleşmenin kendi bildirim ayarlarını AÇAMAZ, yalnız kapatabilir). **Kapalı:** `false` (boolean), `\"false\"`, `\"0\"`, `\"off\"`, `\"no\"`, `\"hayir\"`, `\"hayır\"` → hiçbir davet gönderilmez, sözleşme yalnız yayına alınır. Tanınmayan değer **400 `INVALID_SEND_INVITATIONS`** döner (fail-closed — davet gitti sanıp gitmemesindense hata görmek yeğdir). 
 */
export type ApiV1DemandsDemandIdDispatchPostRequestSendInvitations = boolean | string;

export interface ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest {
    'party_ids': Array<string>;
}
export interface ApiV1DemandsDemandIdDocumentsDocIdPatchRequest {
    /**
     * Gönderilirse boş olamaz.
     */
    'title'?: string;
    'doc_kind'?: ApiV1DemandsDemandIdDocumentsDocIdPatchRequestDocKindEnum;
    'is_required'?: boolean;
    'signature_required'?: boolean;
}

export const ApiV1DemandsDemandIdDocumentsDocIdPatchRequestDocKindEnum = {
    Contract: 'CONTRACT',
    KvkkNotice: 'KVKK_NOTICE',
    KvkkConsent: 'KVKK_CONSENT',
    Preinfo: 'PREINFO',
    PriceList: 'PRICE_LIST',
    Other: 'OTHER',
} as const;

export type ApiV1DemandsDemandIdDocumentsDocIdPatchRequestDocKindEnum = typeof ApiV1DemandsDemandIdDocumentsDocIdPatchRequestDocKindEnum[keyof typeof ApiV1DemandsDemandIdDocumentsDocIdPatchRequestDocKindEnum];

export interface ApiV1DemandsDemandIdDocumentsGet200Response {
    'success'?: boolean;
    'data'?: ApiV1DemandsDemandIdDocumentsGet200ResponseData;
}
export interface ApiV1DemandsDemandIdDocumentsGet200ResponseData {
    'documents'?: Array<EnvelopeDocument>;
}
export interface ApiV1DemandsDemandIdDocumentsOrderPutRequest {
    /**
     * Zarftaki tüm belge id\'leri, istenen yeni sırada.
     */
    'document_ids': Array<string>;
}
export interface ApiV1DemandsDemandIdDocumentsPost201Response {
    'success'?: boolean;
    'data'?: ApiV1DemandsDemandIdDocumentsPost201ResponseData;
}
export interface ApiV1DemandsDemandIdDocumentsPost201ResponseData {
    'document'?: EnvelopeDocument;
}
export interface ApiV1DemandsDemandIdDocumentsPostRequest {
    /**
     * Belge başlığı. Boş olamaz.
     */
    'title': string;
    /**
     * `KVKK_CONSENT` ile `is_required: true` **birlikte gönderilemez** (400 `CONSENT_CANNOT_BE_REQUIRED` — açık rıza reddedilebilir olmak zorundadır). `PREINFO` ile `is_required: false` **birlikte gönderilemez** (400 `PREINFO_MUST_BE_REQUIRED`). 
     */
    'doc_kind'?: ApiV1DemandsDemandIdDocumentsPostRequestDocKindEnum;
    'is_required'?: boolean;
    'signature_required'?: boolean;
}

export const ApiV1DemandsDemandIdDocumentsPostRequestDocKindEnum = {
    Contract: 'CONTRACT',
    KvkkNotice: 'KVKK_NOTICE',
    KvkkConsent: 'KVKK_CONSENT',
    Preinfo: 'PREINFO',
    PriceList: 'PRICE_LIST',
    Other: 'OTHER',
} as const;

export type ApiV1DemandsDemandIdDocumentsPostRequestDocKindEnum = typeof ApiV1DemandsDemandIdDocumentsPostRequestDocKindEnum[keyof typeof ApiV1DemandsDemandIdDocumentsPostRequestDocKindEnum];

export interface ApiV1DemandsGet200Response {
    'success'?: boolean;
    'data'?: ApiV1DemandsGet200ResponseData;
}
export interface ApiV1DemandsGet200ResponseData {
    'demands'?: Array<ApiV1DemandsGet200ResponseDataDemandsInner>;
    'total'?: number;
    'page'?: number;
    'limit'?: number;
}
export interface ApiV1DemandsGet200ResponseDataDemandsInner {
    'id'?: string;
    'title'?: string | null;
    'status'?: string;
    'created_at'?: string;
    'completed_at'?: string | null;
    'parties_total'?: number;
    'parties_signed'?: number;
    /**
     * COMPLETED ise imzalı PDF public URL\'i
     */
    'pdf_url'?: string | null;
}
export interface ApiV1DemandsIdCancelPost200Response {
    'success'?: boolean;
    'data'?: ApiV1DemandsIdCancelPost200ResponseData;
}
export interface ApiV1DemandsIdCancelPost200ResponseData {
    'id'?: string;
    'title'?: string | null;
    'status'?: string;
    'cancelled_at'?: string;
    'cancellation_reason'?: string | null;
}
export interface ApiV1DemandsIdCancelPostRequest {
    /**
     * İptal nedeni (opsiyonel)
     */
    'reason'?: string;
}
export interface ApiV1DemandsIdDelete409Response {
    'success'?: boolean;
    'error'?: string;
    'code'?: string;
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
export interface ApiV1DemandsIdPartiesPartyIdResendPost200Response {
    'success'?: boolean;
    'data'?: ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData;
}
export interface ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData {
    /**
     * Gönderilen kanallar (sms/email/whatsapp)
     */
    'sent'?: Array<string>;
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

export interface ApiV1DemandsIdTimelineGet200Response {
    'success'?: boolean;
    'data'?: ApiV1DemandsIdTimelineGet200ResponseData;
}
export interface ApiV1DemandsIdTimelineGet200ResponseData {
    'events'?: Array<ApiV1DemandsIdTimelineGet200ResponseDataEventsInner>;
}
export interface ApiV1DemandsIdTimelineGet200ResponseDataEventsInner {
    'id'?: string;
    'event_type'?: string;
    'actor_label'?: string | null;
    'ip_masked'?: string | null;
    'device_label'?: string | null;
    'comment_text'?: string | null;
    'created_at'?: string;
}
export interface ApiV1DemandsPost201Response {
    'success'?: boolean;
    'data'?: CreatedDemand;
}
export interface ApiV1DemandsUploadPost201Response {
    'success'?: boolean;
    'data'?: CreatedDemandUpload;
}
export interface ApiV1FieldTemplatesGet200Response {
    'success'?: boolean;
    'data'?: ApiV1FieldTemplatesGet200ResponseData;
}
export interface ApiV1FieldTemplatesGet200ResponseData {
    'field_templates'?: Array<FieldTemplateListItem>;
    'total'?: number;
    'page'?: number;
    'limit'?: number;
}
export interface ApiV1FieldTemplatesIdGet200Response {
    'success'?: boolean;
    'data'?: FieldTemplateDetail;
}
export interface ApiV1FieldTemplatesIdPreviewLayoutPost200Response {
    'success'?: boolean;
    'data'?: FieldLayoutPreview;
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

export interface ApiV1ReportsGet200Response {
    'success'?: boolean;
    'data'?: ApiV1ReportsGet200ResponseData;
}
export interface ApiV1ReportsGet200ResponseData {
    'contracts'?: ApiV1ReportsGet200ResponseDataContracts;
}
export interface ApiV1ReportsGet200ResponseDataContracts {
    'total'?: number;
    'pending'?: number;
    'completed'?: number;
    'cancelled'?: number;
    'expired'?: number;
    'this_month'?: number;
}
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
export interface ApiV1TemplatesIdDelete200Response {
    'success'?: boolean;
    'data'?: ApiV1TemplatesIdDelete200ResponseData;
}
export interface ApiV1TemplatesIdDelete200ResponseData {
    'id'?: string;
    'deleted'?: boolean;
}
export interface ApiV1TemplatesIdGet200Response {
    'success'?: boolean;
    'data'?: TemplateDetail;
}
export interface ApiV1TemplatesIdGet404Response {
    'success'?: boolean;
    'error'?: string;
}
export interface ApiV1TemplatesIdPatch200Response {
    'success'?: boolean;
    'data'?: ApiV1TemplatesIdPatch200ResponseData;
}
export interface ApiV1TemplatesIdPatch200ResponseData {
    'id'?: string;
    'name'?: string;
    'description'?: string | null;
    'category'?: string | null;
}
export interface ApiV1TemplatesIdPatchRequest {
    'name'?: string;
    'description'?: string;
    'category'?: string;
}
export interface ApiV1TemplatesIdUsageGet200Response {
    'success'?: boolean;
    'data'?: TemplateUsage;
}
export interface ApiV1TimestampsGet200Response {
    'success'?: boolean;
    'data'?: ApiV1TimestampsGet200ResponseData;
}
export interface ApiV1TimestampsGet200ResponseData {
    'timestamps'?: Array<TimestampListItem>;
    'total'?: number;
    'page'?: number;
    'limit'?: number;
}
export interface ApiV1TimestampsIdGet200Response {
    'success'?: boolean;
    'data'?: TimestampListItem;
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
/**
 * `error` alanında insan-okur mesaj, `code` alanında makinece okunur hata kodu taşıyan hata gövdesi. Hangi kodların döndüğü ilgili yanıtın açıklamasında listelenir. 
 */
export interface CodedError {
    'success'?: boolean;
    'error'?: string;
    'code'?: string;
}
/**
 * Kişi (Contact) public görünümü. İç eşleşme alanları (matched_user_id / match_via / matched_at) ve şifreleme sütunları (*_enc / *_hash) DÂHİL DEĞİLDİR. 
 */
export interface ContactSummary {
    'id'?: string;
    'first_name'?: string;
    'last_name'?: string;
    'email'?: string | null;
    'phone'?: string | null;
    'government_id'?: string | null;
    'government_id_serial'?: string | null;
    'job_title'?: string | null;
    'company_id'?: string | null;
    'company'?: ContactSummaryCompany | null;
    'notes'?: string | null;
    'address_country'?: string | null;
    'address_city'?: string | null;
    'address_district'?: string | null;
    'address_line'?: string | null;
    'archived_at'?: string | null;
    'created_at'?: string;
    'updated_at'?: string;
}
export interface ContactSummaryCompany {
    'id'?: string;
    'name'?: string;
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
    /**
     * `Idempotency-Key` başlığının gövde karşılığı; başlık ekleyemeyen istemciler için. İkisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`. 
     */
    'idempotency_key'?: string;
    /**
     * Kopya kapısını bilerek geç. Yalnız idempotency anahtarı GÖNDERİLMEYEN çağrılarda anlamlıdır: aynı şablondan aynı taraflara aynı başlık ve değişkenlerle 10 dakika içinde ikinci bir sözleşmeyi kasten göndermek için. 
     */
    'force'?: boolean;
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
    'dispatch'?: CreatedDemandUploadDispatch;
    'field_layout'?: CreatedDemandUploadFieldLayout;
}
/**
 * YALNIZ istekte `send_invitations` gönderildiğinde döner. Davet gönderimi 201\'i düşürmez (sözleşme yaratılmış ve kredi düşülmüştür), bu yüzden hatalar burada taraf bazında raporlanır. 
 */
export interface CreatedDemandUploadDispatch {
    'requested'?: boolean;
    /**
     * En az bir kanaldan (SMS/e-posta/WhatsApp) davet **gönderilen taraf** sayısı — kanal sayısı DEĞİL. (`POST /api/v1/demands` yanıtındaki `dispatched` alanı kanal sayar; bu alan saymaz.) 
     */
    'sent'?: number;
    'results'?: Array<CreatedDemandUploadDispatchResultsInner>;
    /**
     * `DISPATCH_SKIPPED` — sözleşme satırı okunamadı, bildirim ayarları bilinmediği için hiç davet gönderilmedi. `DISPATCH_TOO_MANY` — taraf sayısı 20 sınırını aşıyor. 
     */
    'error'?: CreatedDemandUploadDispatchErrorEnum;
}

export const CreatedDemandUploadDispatchErrorEnum = {
    DispatchSkipped: 'DISPATCH_SKIPPED',
    DispatchTooMany: 'DISPATCH_TOO_MANY',
} as const;

export type CreatedDemandUploadDispatchErrorEnum = typeof CreatedDemandUploadDispatchErrorEnum[keyof typeof CreatedDemandUploadDispatchErrorEnum];

export interface CreatedDemandUploadDispatchResultsInner {
    'party_id'?: string;
    'sms'?: boolean;
    'email'?: boolean;
    'whatsapp'?: boolean;
    /**
     * Yalnız o taraf için gönderim patladığında döner.
     */
    'error'?: CreatedDemandUploadDispatchResultsInnerErrorEnum;
}

export const CreatedDemandUploadDispatchResultsInnerErrorEnum = {
    DispatchFailed: 'DISPATCH_FAILED',
} as const;

export type CreatedDemandUploadDispatchResultsInnerErrorEnum = typeof CreatedDemandUploadDispatchResultsInnerErrorEnum[keyof typeof CreatedDemandUploadDispatchResultsInnerErrorEnum];

/**
 * YALNIZ `field_template_id` gönderildiğinde döner. Alan yerleşiminin uygulanma özeti. 
 */
export interface CreatedDemandUploadFieldLayout {
    'template_id'?: string;
    /**
     * Sözleşmeye yazılan alan sayısı.
     */
    'applied_fields'?: number;
    /**
     * İstenen değil, **uygulanan** davranış. `drop` istenip uygulanamadıysa burada `block` görürsünüz ve `warnings` içinde `ON_ANCHOR_MISS_NOT_RELAXED` bulunur. 
     */
    'on_anchor_miss'?: CreatedDemandUploadFieldLayoutOnAnchorMissEnum;
    'warnings'?: Array<FieldLayoutWarning>;
    /**
     * Yalnız `WARNING` seviyesindeki tanılar. `ERROR` seviyesinde bir tanı olsaydı istek 422 ile reddedilir ve sözleşme yaratılmazdı. 
     */
    'diagnostics'?: Array<FieldLayoutDiagnostic>;
}

export const CreatedDemandUploadFieldLayoutOnAnchorMissEnum = {
    Block: 'block',
    Drop: 'drop',
} as const;

export type CreatedDemandUploadFieldLayoutOnAnchorMissEnum = typeof CreatedDemandUploadFieldLayoutOnAnchorMissEnum[keyof typeof CreatedDemandUploadFieldLayoutOnAnchorMissEnum];

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
    /**
     * Kısaltılmış görünen ad (Ahmet Y.) — KVKK maskeleme
     */
    'name'?: string;
    /**
     * Maskeli e-posta (ah***@x.com)
     */
    'email_masked'?: string;
    'signed'?: boolean;
    'signed_at'?: string | null;
    'rejected'?: boolean;
    'rejected_at'?: string | null;
    'signing_url'?: string;
}
/**
 * Çok-belgeli imza zarfındaki tek bir belge. `assigned_party_ids` ve `decision_count` yalnız `GET .../documents?view=wizard` yanıtında bulunur; `has_timestamp` yalnız varsayılan (özet) görünümde bulunur — ikisi AYNI ANDA gelmez (dashboard\'un iki ayrı iç DTO\'sunun [`EnvelopeDocumentDTO` / `EnvelopeDocumentSummaryDTO`] tek şema altında birleştirilmiş hâli). 
 */
export interface EnvelopeDocument {
    'id'?: string;
    /**
     * 1\'den başlar.
     */
    'order'?: number;
    'title'?: string;
    'doc_kind'?: EnvelopeDocumentDocKindEnum;
    'is_required'?: boolean;
    'signature_required'?: boolean;
    /**
     * Yalnız `?view=wizard` yanıtında bulunur.
     */
    'assigned_party_ids'?: Array<string>;
    /**
     * Yalnız `?view=wizard` yanıtında bulunur. Bu belge için verilmiş onay/red kararı sayısı. 
     */
    'decision_count'?: number;
    'sealing_status'?: EnvelopeDocumentSealingStatusEnum | null;
    /**
     * Yalnız varsayılan (özet) görünümde bulunur. `sealing_status` `SEALED` değilken daima `false` döner. 
     */
    'has_timestamp'?: boolean;
    'page_count'?: number;
    'completed_at'?: string | null;
}

export const EnvelopeDocumentDocKindEnum = {
    Contract: 'CONTRACT',
    KvkkNotice: 'KVKK_NOTICE',
    KvkkConsent: 'KVKK_CONSENT',
    Preinfo: 'PREINFO',
    PriceList: 'PRICE_LIST',
    Other: 'OTHER',
} as const;

export type EnvelopeDocumentDocKindEnum = typeof EnvelopeDocumentDocKindEnum[keyof typeof EnvelopeDocumentDocKindEnum];
export const EnvelopeDocumentSealingStatusEnum = {
    None: 'NONE',
    Pending: 'PENDING',
    Sealed: 'SEALED',
    Failed: 'FAILED',
} as const;

export type EnvelopeDocumentSealingStatusEnum = typeof EnvelopeDocumentSealingStatusEnum[keyof typeof EnvelopeDocumentSealingStatusEnum];

/**
 * Alan yerleşimi tanısı. `code` makine sözleşmesidir, `message` insan metnidir (TR). **Bilinmeyen `code` toleranslı okunmalıdır**: yeni kod eklemek kırıcı değişiklik sayılmaz, istemci tanımadığı kodda `message` alanına düşmelidir.  Belgenizden çıkarılan hiçbir metin (çapa metni, eşleşen satır, sayfa metni, dosya adı) bu yapıya girmez. 
 */
export interface FieldLayoutDiagnostic {
    'code': string;
    'severity': FieldLayoutDiagnosticSeverityEnum;
    /**
     * Şablon alanının kimliği. `-1` belge/taraf düzeyindeki tanılar için kullanılan sentinel değerdir. 
     */
    'item_id': number;
    'slug'?: string | null;
    'label'?: string | null;
    'item_type': string;
    /**
     * 1 tabanlı sayfa numarası; bilinmiyorsa null.
     */
    'page'?: number | null;
    'message': string;
    /**
     * Koda göre değişen, kapalı bir anahtar kümesi (yalnız sayısal değerler ve alan kimlikleri). 
     */
    'detail'?: { [key: string]: any; } | null;
}

export const FieldLayoutDiagnosticSeverityEnum = {
    Error: 'ERROR',
    Warning: 'WARNING',
} as const;

export type FieldLayoutDiagnosticSeverityEnum = typeof FieldLayoutDiagnosticSeverityEnum[keyof typeof FieldLayoutDiagnosticSeverityEnum];

/**
 * Kuru koşum sonucu — hiçbir yan etki üretilmemiştir.
 */
export interface FieldLayoutPreview {
    'template_id'?: string;
    /**
     * Yüklenen belgenin sayfa sayısı.
     */
    'page_count'?: number;
    /**
     * `true` ise aynı belge + aynı şablon ile `POST /api/v1/demands/upload` çağrısı alan yerleşimi nedeniyle reddedilmez. 
     */
    'resolvable'?: boolean;
    /**
     * Uygulanan davranış (istenen değil).
     */
    'on_anchor_miss'?: FieldLayoutPreviewOnAnchorMissEnum;
    'placements_summary'?: FieldLayoutPreviewPlacementsSummary;
    'diagnostics'?: Array<FieldLayoutDiagnostic>;
    'warnings'?: Array<FieldLayoutWarning>;
}

export const FieldLayoutPreviewOnAnchorMissEnum = {
    Block: 'block',
    Drop: 'drop',
} as const;

export type FieldLayoutPreviewOnAnchorMissEnum = typeof FieldLayoutPreviewOnAnchorMissEnum[keyof typeof FieldLayoutPreviewOnAnchorMissEnum];

export interface FieldLayoutPreviewPlacementsSummary {
    'total'?: number;
    'by_item_type'?: { [key: string]: number; };
    'by_page'?: Array<FieldLayoutPreviewPlacementsSummaryByPageInner>;
}
export interface FieldLayoutPreviewPlacementsSummaryByPageInner {
    /**
     * 1 tabanlı
     */
    'page'?: number;
    'count'?: number;
}
/**
 * `field_template_id` ile gönderilen bir istekte alan yerleşimi çözülemediğinde dönen gövde. **Sözleşme oluşturulmaz, kredi düşülmez.** 
 */
export interface FieldLayoutUnresolved {
    'success'?: boolean;
    'error'?: string;
    'message'?: string;
    'diagnostics'?: Array<FieldLayoutDiagnostic>;
    'warnings'?: Array<FieldLayoutWarning>;
}
export interface FieldLayoutWarning {
    'code': string;
    'message': string;
}
/**
 * Alan Şablonu ayrıntısı. Alan koordinatları TAŞIMAZ; yerleşimin bir belgeye nasıl uygulanacağı için kuru koşum ucunu kullanın. 
 */
export interface FieldTemplateDetail {
    'id'?: string;
    'name'?: string;
    'description'?: string | null;
    'category'?: string | null;
    /**
     * Şablondaki toplam alan sayısı.
     */
    'total_field_count'?: number;
    'parties'?: Array<FieldTemplateDetailPartiesInner>;
}
export interface FieldTemplateDetailPartiesInner {
    'id'?: string;
    'order'?: number;
    'label'?: string | null;
    'is_required'?: boolean;
    /**
     * Bu rolün dolduracağı alan sayısı.
     */
    'field_count'?: number;
}
export interface FieldTemplateListItem {
    'id'?: string;
    'name'?: string;
    'description'?: string | null;
    'category'?: string | null;
    'usage_count'?: number;
    'parties'?: Array<FieldTemplateParty>;
}
/**
 * Alan Şablonundaki tek rol. `order`, `POST /api/v1/demands/upload` gövdesindeki `parties` dizisinin sırasıdır. 
 */
export interface FieldTemplateParty {
    'id'?: string;
    'order'?: number;
    'label'?: string | null;
    'is_required'?: boolean;
}
/**
 * KYC olaylarının zarfı. Sözleşme olaylarıyla aynı alanları taşır; `type` enum\'u yalnız KYC olaylarını içerir. 
 */
export interface KycWebhookEnvelope {
    /**
     * Olay id\'si: receiver tarafında idempotency anahtarı.
     */
    'id': string;
    'type': KycWebhookEnvelopeTypeEnum;
    /**
     * Olay zamanı (ISO 8601 UTC).
     */
    'created_at': string;
    /**
     * Olay verisi (WebhookDataKycTerminal)
     */
    'data': object;
}

export const KycWebhookEnvelopeTypeEnum = {
    KycCompleted: 'kyc.completed',
    KycFailed: 'kyc.failed',
} as const;

export type KycWebhookEnvelopeTypeEnum = typeof KycWebhookEnvelopeTypeEnum[keyof typeof KycWebhookEnvelopeTypeEnum];

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
    DemandNotDispatched: 'DEMAND_NOT_DISPATCHED',
    DemandNotDispatchable: 'DEMAND_NOT_DISPATCHABLE',
    DemandExpired: 'DEMAND_EXPIRED',
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
    /**
     * Rehberin anlattığı akış. Bu uç yalnız belgeli şablonu çözer, dolayısıyla burada daima `DOCUMENT` döner (Alan Şablonu bu uçta 404\'tür; kimliği `GET /api/v1/field-templates` ile öğrenilir ve `POST /api/v1/demands/upload` ile kullanılır). 
     */
    'kind'?: TemplateUsageKindEnum;
    /**
     * `multipart/form-data` alanları. Belgeli şablon JSON gövde kullandığı için bu uçta boş dizidir. 
     */
    'form_fields'?: Array<TemplateUsageFormFieldsInner>;
    'template'?: ApiV1TemplatesIdPatch200ResponseData;
    'endpoint'?: TemplateUsageEndpoint;
    'required_headers'?: { [key: string]: string; };
    'parties'?: Array<TemplateUsagePartiesInner>;
    'variables'?: Array<TemplateUsageVariablesInner>;
    'example_request'?: TemplateUsageExampleRequest;
}

export const TemplateUsageKindEnum = {
    Document: 'DOCUMENT',
    FieldLayout: 'FIELD_LAYOUT',
} as const;

export type TemplateUsageKindEnum = typeof TemplateUsageKindEnum[keyof typeof TemplateUsageKindEnum];

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
export interface TemplateUsageFormFieldsInner {
    'key'?: string;
    'type'?: string;
    'required'?: boolean;
    'note'?: string;
}
export interface TemplateUsagePartiesInner {
    'template_party_id'?: string;
    'order'?: number;
    'label'?: string;
    'is_required'?: boolean;
    /**
     * Rolün doldurduğu alan sayısı. Yalnız Alan Şablonu rehberinde döner, bu uçta bulunmaz. 
     */
    'field_count'?: number;
    'supported_fields'?: Array<TemplateUsagePartiesInnerSupportedFieldsInner>;
}
export interface TemplateUsagePartiesInnerSupportedFieldsInner {
    'key'?: string;
    'type'?: string;
    'required'?: boolean;
    'required_if'?: string;
    'default'?: any;
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

/**
 * Zaman damgası liste/detay öğesi. `timestamp_file_url` doğrudan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı olabilir). 
 */
export interface TimestampListItem {
    'id'?: string;
    'original_file_name'?: string;
    'original_file_size'?: number;
    'timestamp_date'?: string;
    'status'?: TimestampListItemStatusEnum;
    'description'?: string | null;
    'created_at'?: string;
    'timestamp_file_url'?: string;
}

export const TimestampListItemStatusEnum = {
    Active: 'ACTIVE',
    Verified: 'VERIFIED',
    Expired: 'EXPIRED',
    Invalid: 'INVALID',
} as const;

export type TimestampListItemStatusEnum = typeof TimestampListItemStatusEnum[keyof typeof TimestampListItemStatusEnum];

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
/**
 * KYC terminal olay verisi. Yalnız doğrulamanın sonucunu taşır: kimlik numarası, telefon, ad soyad ve belge görüntüleri bu gövdede YER ALMAZ. 
 */
export interface WebhookDataKycTerminal {
    /**
     * Doğrulama kaydının kimliği. Talep oluşturulurken dönen kimlikle aynıdır; aynı doğrulamaya ait olayları bu değerle ilişkilendirin. 
     */
    'id': string;
    /**
     * Doğrulamanın toplam sonucu.
     */
    'verified': boolean;
    /**
     * Talepte istenen yöntemler (örn. `id_card`, `phone_otp`).
     */
    'methods': Array<string>;
    'steps': Array<WebhookDataKycTerminalStepsInner>;
    /**
     * Talep oluşturulurken verilen dış referans.
     */
    'external_ref': string | null;
}
export interface WebhookDataKycTerminalStepsInner {
    'method'?: string;
    /**
     * Adım sonucu; adım hiç tamamlanmadıysa `null`.
     */
    'verified'?: boolean | null;
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
         * Çağrıyı yapan API key\'in sahibi hakkında temel bilgileri döner: kullanıcı kimliği, e-posta, isim, aktif workspace ve kalan kredi. Scope gerektirmez — geçerli herhangi bir API key (hangi scope\'a sahip olursa olsun, ör. sadece `demands:read`) bu endpoint\'i çağırabilir. 
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
         * Çağrıyı yapan API key\'in sahibi hakkında temel bilgileri döner: kullanıcı kimliği, e-posta, isim, aktif workspace ve kalan kredi. Scope gerektirmez — geçerli herhangi bir API key (hangi scope\'a sahip olursa olsun, ör. sadece `demands:read`) bu endpoint\'i çağırabilir. 
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
         * Çağrıyı yapan API key\'in sahibi hakkında temel bilgileri döner: kullanıcı kimliği, e-posta, isim, aktif workspace ve kalan kredi. Scope gerektirmez — geçerli herhangi bir API key (hangi scope\'a sahip olursa olsun, ör. sadece `demands:read`) bu endpoint\'i çağırabilir. 
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
     * Çağrıyı yapan API key\'in sahibi hakkında temel bilgileri döner: kullanıcı kimliği, e-posta, isim, aktif workspace ve kalan kredi. Scope gerektirmez — geçerli herhangi bir API key (hangi scope\'a sahip olursa olsun, ör. sadece `demands:read`) bu endpoint\'i çağırabilir. 
     * @summary API key sahibi bilgisi
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1MeGet(options?: RawAxiosRequestConfig) {
        return AccountApiFp(this.configuration).apiV1MeGet(options).then((request) => request(this.axios, this.basePath));
    }
}



/**
 * ContactsApi - axios parameter creator
 */
export const ContactsApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * API key sahibinin workspace\'indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org\'un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 
         * @summary Kişi listesi
         * @param {number} [page] 
         * @param {number} [limit] 
         * @param {string} [q] Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter)
         * @param {string} [sort] Sıralama: createdAt | updatedAt | first_name | last_name | email (\&#39;-\&#39; öneki azalan)
         * @param {string} [companyId] 
         * @param {boolean} [archived] true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif)
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1ContactsGet: async (page?: number, limit?: number, q?: string, sort?: string, companyId?: string, archived?: boolean, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/api/v1/contacts`;
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

            if (q !== undefined) {
                localVarQueryParameter['q'] = q;
            }

            if (sort !== undefined) {
                localVarQueryParameter['sort'] = sort;
            }

            if (companyId !== undefined) {
                localVarQueryParameter['company_id'] = companyId;
            }

            if (archived !== undefined) {
                localVarQueryParameter['archived'] = archived;
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
         * Workspace\'e yeni bir kişi ekler. Aynı workspace\'te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 
         * @summary Kişi oluştur
         * @param {ApiV1ContactsPostRequest} apiV1ContactsPostRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1ContactsPost: async (apiV1ContactsPostRequest: ApiV1ContactsPostRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'apiV1ContactsPostRequest' is not null or undefined
            assertParamExists('apiV1ContactsPost', 'apiV1ContactsPostRequest', apiV1ContactsPostRequest)
            const localVarPath = `/api/v1/contacts`;
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
            localVarRequestOptions.data = serializeDataIfNeeded(apiV1ContactsPostRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
    }
};

/**
 * ContactsApi - functional programming interface
 */
export const ContactsApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = ContactsApiAxiosParamCreator(configuration)
    return {
        /**
         * API key sahibinin workspace\'indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org\'un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 
         * @summary Kişi listesi
         * @param {number} [page] 
         * @param {number} [limit] 
         * @param {string} [q] Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter)
         * @param {string} [sort] Sıralama: createdAt | updatedAt | first_name | last_name | email (\&#39;-\&#39; öneki azalan)
         * @param {string} [companyId] 
         * @param {boolean} [archived] true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif)
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1ContactsGet(page?: number, limit?: number, q?: string, sort?: string, companyId?: string, archived?: boolean, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1ContactsGet200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1ContactsGet(page, limit, q, sort, companyId, archived, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['ContactsApi.apiV1ContactsGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Workspace\'e yeni bir kişi ekler. Aynı workspace\'te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 
         * @summary Kişi oluştur
         * @param {ApiV1ContactsPostRequest} apiV1ContactsPostRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1ContactsPost(apiV1ContactsPostRequest: ApiV1ContactsPostRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1ContactsPost201Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1ContactsPost(apiV1ContactsPostRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['ContactsApi.apiV1ContactsPost']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * ContactsApi - factory interface
 */
export const ContactsApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = ContactsApiFp(configuration)
    return {
        /**
         * API key sahibinin workspace\'indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org\'un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 
         * @summary Kişi listesi
         * @param {ContactsApiApiV1ContactsGetRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1ContactsGet(requestParameters: ContactsApiApiV1ContactsGetRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1ContactsGet200Response> {
            return localVarFp.apiV1ContactsGet(requestParameters.page, requestParameters.limit, requestParameters.q, requestParameters.sort, requestParameters.companyId, requestParameters.archived, options).then((request) => request(axios, basePath));
        },
        /**
         * Workspace\'e yeni bir kişi ekler. Aynı workspace\'te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 
         * @summary Kişi oluştur
         * @param {ContactsApiApiV1ContactsPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1ContactsPost(requestParameters: ContactsApiApiV1ContactsPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1ContactsPost201Response> {
            return localVarFp.apiV1ContactsPost(requestParameters.apiV1ContactsPostRequest, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for apiV1ContactsGet operation in ContactsApi.
 */
export interface ContactsApiApiV1ContactsGetRequest {
    readonly page?: number

    readonly limit?: number

    /**
     * Ad / soyad / e-posta / telefon / TC üzerinde arama (max 100 karakter)
     */
    readonly q?: string

    /**
     * Sıralama: createdAt | updatedAt | first_name | last_name | email (\&#39;-\&#39; öneki azalan)
     */
    readonly sort?: string

    readonly companyId?: string

    /**
     * true → yalnızca arşivlenmiş kişiler; varsayılan false (aktif)
     */
    readonly archived?: boolean
}

/**
 * Request parameters for apiV1ContactsPost operation in ContactsApi.
 */
export interface ContactsApiApiV1ContactsPostRequest {
    readonly apiV1ContactsPostRequest: ApiV1ContactsPostRequest
}

/**
 * ContactsApi - object-oriented interface
 */
export class ContactsApi extends BaseAPI {
    /**
     * API key sahibinin workspace\'indeki kişileri (Contact / adres defteri) listeler. Kişisel workspace yalnızca sahibinin kişilerini, org workspace org\'un kişilerini döner (IDOR-safe). İç eşleşme ve şifreleme alanları yanıta HİÇ konmaz. 
     * @summary Kişi listesi
     * @param {ContactsApiApiV1ContactsGetRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1ContactsGet(requestParameters: ContactsApiApiV1ContactsGetRequest = {}, options?: RawAxiosRequestConfig) {
        return ContactsApiFp(this.configuration).apiV1ContactsGet(requestParameters.page, requestParameters.limit, requestParameters.q, requestParameters.sort, requestParameters.companyId, requestParameters.archived, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Workspace\'e yeni bir kişi ekler. Aynı workspace\'te aynı e-posta veya telefona sahip aktif bir kişi varsa 409 (CONTACT_DUPLICATE) döner.  🔴 KVKK: 3. kişilere ait kişisel veriyi (ad/e-posta/telefon/TC) buraya yazan API key sahibi, o veri için veri sorumlusudur; aydınlatma ve hukuki dayanak yükümlülüğü kendisine aittir. 
     * @summary Kişi oluştur
     * @param {ContactsApiApiV1ContactsPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1ContactsPost(requestParameters: ContactsApiApiV1ContactsPostRequest, options?: RawAxiosRequestConfig) {
        return ContactsApiFp(this.configuration).apiV1ContactsPost(requestParameters.apiV1ContactsPostRequest, options).then((request) => request(this.axios, this.basePath));
    }
}



/**
 * DemandsApi - axios parameter creator
 */
export const DemandsApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \"bulk send\" modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Davranış:** - `rows` en fazla 10 (aşarsa 400 `BULK_MAX_10`). Daha büyük listeler   istemci tarafında 10\'arlı parçalara bölünür. - Kredi: sabit \"1 satır = 1 kredi\" değildir, her satırın maliyeti   `POST /demands` ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; `eidas_timestamp` seçilirse   satır başına +1 kredi eklenir). PAdES seviye eki yalnız QES\'te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   `ocr_id` ve `liveness` +1 kredi, diğerleri 0\'dır. `options`   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır `failed` (`error: \"INSUFFICIENT_CREDITS\"`)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine `failed` döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (`demand_id` response\'ta bulunur, davet   gönderilmemiştir). - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace\'in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar `failed` olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - `X-Workspace-Id` header\'ı ile organizasyon workspace\'i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
         * @summary Toplu sözleşme oluştur (tek şablondan N alıcı)
         * @param {ApiV1DemandsBulkPostRequest} apiV1DemandsBulkPostRequest 
         * @param {string} [xWorkspaceId] Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;). 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsBulkPost: async (apiV1DemandsBulkPostRequest: ApiV1DemandsBulkPostRequest, xWorkspaceId?: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'apiV1DemandsBulkPostRequest' is not null or undefined
            assertParamExists('apiV1DemandsBulkPost', 'apiV1DemandsBulkPostRequest', apiV1DemandsBulkPostRequest)
            const localVarPath = `/api/v1/demands/bulk`;
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

            if (xWorkspaceId != null) {
                localVarHeaderParameter['X-Workspace-Id'] = String(xWorkspaceId);
            }
            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(apiV1DemandsBulkPostRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, `DRAFT` ise sözleşmeyi `PENDING`\'e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  🔴 **Bu uçta yukarıdaki `/documents*` ailesinin bayrak kapısı (`ENVELOPE_DECISION_ENFORCE`) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — `dispatch_notifications: false` ile oluşturulur, ardından `POST .../documents` / `POST .../documents/upload` ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; `send_invitations` gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir.  **Kredi:** tek tahsilat noktası burasıdır (`reconcileDemandSigningCost`). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (`dispatched: false` döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. `v1ResendRateLimiter`, `/parties/{partyId}/resend` ile AYNI dakika-bazlı freni paylaşır). 
         * @summary Zarfı imzaya gönder (yayınla + davet)
         * @param {string} demandId 
         * @param {ApiV1DemandsDemandIdDispatchPostRequest} [apiV1DemandsDemandIdDispatchPostRequest] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDispatchPost: async (demandId: string, apiV1DemandsDemandIdDispatchPostRequest?: ApiV1DemandsDemandIdDispatchPostRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'demandId' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDispatchPost', 'demandId', demandId)
            const localVarPath = `/api/v1/demands/{demandId}/dispatch`
                .replace('{demandId}', encodeURIComponent(String(demandId)));
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
            localVarRequestOptions.data = serializeDataIfNeeded(apiV1DemandsDemandIdDispatchPostRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Belgeye atanmış imzacı kümesini **tam olarak** `party_ids` ile değiştirir (eski atamalar silinir, yenileri yazılır). `party_ids` boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 `INVALID_PARTY_ID` — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 `ASSIGNMENT_HAS_DECISION`). 
         * @summary Belgeye imzacı ata (tam-küme replace)
         * @param {string} demandId 
         * @param {string} docId 
         * @param {ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest} apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut: async (demandId: string, docId: string, apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest: ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'demandId' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut', 'demandId', demandId)
            // verify required parameter 'docId' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut', 'docId', docId)
            // verify required parameter 'apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut', 'apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest', apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest)
            const localVarPath = `/api/v1/demands/{demandId}/documents/{docId}/assignments`
                .replace('{demandId}', encodeURIComponent(String(demandId)))
                .replace('{docId}', encodeURIComponent(String(docId)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'PUT', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)

            localVarHeaderParameter['Content-Type'] = 'application/json';
            localVarHeaderParameter['Accept'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N\'e yeniden sıralanır. **Zarftaki son belge silinemez** (400 `CANNOT_DELETE_LAST_DOCUMENT`) — zarf hiç boş kalamaz. 
         * @summary Belgeyi zarftan sil
         * @param {string} demandId 
         * @param {string} docId 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDocumentsDocIdDelete: async (demandId: string, docId: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'demandId' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsDocIdDelete', 'demandId', demandId)
            // verify required parameter 'docId' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsDocIdDelete', 'docId', docId)
            const localVarPath = `/api/v1/demands/{demandId}/documents/{docId}`
                .replace('{demandId}', encodeURIComponent(String(demandId)))
                .replace('{docId}', encodeURIComponent(String(docId)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'DELETE', ...baseOptions, ...options};
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
         * Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (`PUT .../assignments`). 
         * @summary Belge metadata güncelle
         * @param {string} demandId 
         * @param {string} docId 
         * @param {ApiV1DemandsDemandIdDocumentsDocIdPatchRequest} apiV1DemandsDemandIdDocumentsDocIdPatchRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDocumentsDocIdPatch: async (demandId: string, docId: string, apiV1DemandsDemandIdDocumentsDocIdPatchRequest: ApiV1DemandsDemandIdDocumentsDocIdPatchRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'demandId' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsDocIdPatch', 'demandId', demandId)
            // verify required parameter 'docId' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsDocIdPatch', 'docId', docId)
            // verify required parameter 'apiV1DemandsDemandIdDocumentsDocIdPatchRequest' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsDocIdPatch', 'apiV1DemandsDemandIdDocumentsDocIdPatchRequest', apiV1DemandsDemandIdDocumentsDocIdPatchRequest)
            const localVarPath = `/api/v1/demands/{demandId}/documents/{docId}`
                .replace('{demandId}', encodeURIComponent(String(demandId)))
                .replace('{docId}', encodeURIComponent(String(docId)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'PATCH', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)

            localVarHeaderParameter['Content-Type'] = 'application/json';
            localVarHeaderParameter['Accept'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(apiV1DemandsDemandIdDocumentsDocIdPatchRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm `/documents*` + `/documents/{docId}*` + `/documents/{docId}/assignments` uçları `ENVELOPE_DECISION_ENFORCE` bayrağı **arkasındadır**; bayrak kapalıyken **409 `ENVELOPE_MULTI_DOC_DISABLED`** döner. Bu, dashboard\'un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca `POST /demands/{demandId}/dispatch` çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. `?view=wizard` verilirse tam şekil döner (`EnvelopeDocument` şemasındaki alan notlarına bakın). 
         * @summary Zarf belge listesi
         * @param {string} demandId 
         * @param {ApiV1DemandsDemandIdDocumentsGetViewEnum} [view] &#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil).
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDocumentsGet: async (demandId: string, view?: ApiV1DemandsDemandIdDocumentsGetViewEnum, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'demandId' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsGet', 'demandId', demandId)
            const localVarPath = `/api/v1/demands/{demandId}/documents`
                .replace('{demandId}', encodeURIComponent(String(demandId)));
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

            if (view !== undefined) {
                localVarQueryParameter['view'] = view;
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
         * Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. `document_ids` zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 `ORDER_SET_MISMATCH`** döner. 
         * @summary Zarftaki belgelerin sırasını değiştir
         * @param {string} demandId 
         * @param {ApiV1DemandsDemandIdDocumentsOrderPutRequest} apiV1DemandsDemandIdDocumentsOrderPutRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDocumentsOrderPut: async (demandId: string, apiV1DemandsDemandIdDocumentsOrderPutRequest: ApiV1DemandsDemandIdDocumentsOrderPutRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'demandId' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsOrderPut', 'demandId', demandId)
            // verify required parameter 'apiV1DemandsDemandIdDocumentsOrderPutRequest' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsOrderPut', 'apiV1DemandsDemandIdDocumentsOrderPutRequest', apiV1DemandsDemandIdDocumentsOrderPutRequest)
            const localVarPath = `/api/v1/demands/{demandId}/documents/order`
                .replace('{demandId}', encodeURIComponent(String(demandId)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'PUT', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)

            localVarHeaderParameter['Content-Type'] = 'application/json';
            localVarHeaderParameter['Accept'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(apiV1DemandsDemandIdDocumentsOrderPutRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: `POST /demands/{demandId}/documents/upload`.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`\'te yapılır. 
         * @summary Zarfa metadata-only belge ekle
         * @param {string} demandId 
         * @param {ApiV1DemandsDemandIdDocumentsPostRequest} apiV1DemandsDemandIdDocumentsPostRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDocumentsPost: async (demandId: string, apiV1DemandsDemandIdDocumentsPostRequest: ApiV1DemandsDemandIdDocumentsPostRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'demandId' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsPost', 'demandId', demandId)
            // verify required parameter 'apiV1DemandsDemandIdDocumentsPostRequest' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsPost', 'apiV1DemandsDemandIdDocumentsPostRequest', apiV1DemandsDemandIdDocumentsPostRequest)
            const localVarPath = `/api/v1/demands/{demandId}/documents`
                .replace('{demandId}', encodeURIComponent(String(demandId)));
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
            localVarRequestOptions.data = serializeDataIfNeeded(apiV1DemandsDemandIdDocumentsPostRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  `idempotency_key` **zorunludur** (gövde alanı; `Idempotency-Key` HEADER\'ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı `(demandId, idempotency_key)` çifti ile tekrar çağrı **409 `IDEMPOTENT_REPLAY`** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO\'su döner.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`\'te yapılır. 
         * @summary Zarfa dosya yükle (belge başına tek dosya)
         * @param {string} demandId 
         * @param {File} file PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.
         * @param {string} idempotencyKey Zorunlu tekrar-koruma anahtarı.
         * @param {string} title 
         * @param {ApiV1DemandsDemandIdDocumentsUploadPostDocKindEnum} [docKind] 
         * @param {ApiV1DemandsDemandIdDocumentsUploadPostIsRequiredEnum} [isRequired] Multipart alanı — string olarak gönderilir.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDocumentsUploadPost: async (demandId: string, file: File, idempotencyKey: string, title: string, docKind?: ApiV1DemandsDemandIdDocumentsUploadPostDocKindEnum, isRequired?: ApiV1DemandsDemandIdDocumentsUploadPostIsRequiredEnum, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'demandId' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsUploadPost', 'demandId', demandId)
            // verify required parameter 'file' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsUploadPost', 'file', file)
            // verify required parameter 'idempotencyKey' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsUploadPost', 'idempotencyKey', idempotencyKey)
            // verify required parameter 'title' is not null or undefined
            assertParamExists('apiV1DemandsDemandIdDocumentsUploadPost', 'title', title)
            const localVarPath = `/api/v1/demands/{demandId}/documents/upload`
                .replace('{demandId}', encodeURIComponent(String(demandId)));
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

            if (idempotencyKey !== undefined) { 
                localVarFormParams.append('idempotency_key', idempotencyKey as any);
            }

            if (title !== undefined) { 
                localVarFormParams.append('title', title as any);
            }

            if (docKind !== undefined) { 
                localVarFormParams.append('doc_kind', docKind as any);
            }

            if (isRequired !== undefined) { 
                localVarFormParams.append('is_required', isRequired as any);
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
        /**
         * Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (`parties_total`, `parties_signed`). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için `GET /demands/{id}`. 
         * @summary Sözleşme listesi (counts-only, PII\'siz)
         * @param {ApiV1DemandsGetStatusEnum} [status] 
         * @param {string} [q] Başlık araması
         * @param {string} [from] 
         * @param {string} [to] 
         * @param {string} [templateId] 
         * @param {number} [page] 
         * @param {number} [limit] Sayfa boyutu (page_size ile aynı)
         * @param {string} [sort] alan:yön (ör. createdAt:desc)
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsGet: async (status?: ApiV1DemandsGetStatusEnum, q?: string, from?: string, to?: string, templateId?: string, page?: number, limit?: number, sort?: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/api/v1/demands`;
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

            if (status !== undefined) {
                localVarQueryParameter['status'] = status;
            }

            if (q !== undefined) {
                localVarQueryParameter['q'] = q;
            }

            if (from !== undefined) {
                localVarQueryParameter['from'] = (from as any instanceof Date) ?
                    (from as any).toISOString().substring(0,10) :
                    from;
            }

            if (to !== undefined) {
                localVarQueryParameter['to'] = (to as any instanceof Date) ?
                    (to as any).toISOString().substring(0,10) :
                    to;
            }

            if (templateId !== undefined) {
                localVarQueryParameter['template_id'] = templateId;
            }

            if (page !== undefined) {
                localVarQueryParameter['page'] = page;
            }

            if (limit !== undefined) {
                localVarQueryParameter['limit'] = limit;
            }

            if (sort !== undefined) {
                localVarQueryParameter['sort'] = sort;
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
         * Çok-belgeli zarfta TEK bir belgenin imzalı PDF\'ini indirir. Zarf-geneli `/demands/{id}/pdf` ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND\'lenir (başka zarfın belgesi istenirse 404). 
         * @summary Belge-özgü imzalı PDF (çok-belgeli zarf)
         * @param {string} id 
         * @param {string} documentId Zarftaki belgenin kimliği.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdBelgeDocumentIdPdfGet: async (id: string, documentId: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1DemandsIdBelgeDocumentIdPdfGet', 'id', id)
            // verify required parameter 'documentId' is not null or undefined
            assertParamExists('apiV1DemandsIdBelgeDocumentIdPdfGet', 'documentId', documentId)
            const localVarPath = `/api/v1/demands/{id}/belge/{document_id}/pdf`
                .replace('{id}', encodeURIComponent(String(id)))
                .replace('{document_id}', encodeURIComponent(String(documentId)));
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

            localVarHeaderParameter['Accept'] = 'application/pdf,application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Bekleyen bir sözleşmeyi iptal eder (status=CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
         * @summary Sözleşme iptal (void)
         * @param {string} id 
         * @param {ApiV1DemandsIdCancelPostRequest} [apiV1DemandsIdCancelPostRequest] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdCancelPost: async (id: string, apiV1DemandsIdCancelPostRequest?: ApiV1DemandsIdCancelPostRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1DemandsIdCancelPost', 'id', id)
            const localVarPath = `/api/v1/demands/{id}/cancel`
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
            localVarRequestOptions.data = serializeDataIfNeeded(apiV1DemandsIdCancelPostRequest, localVarRequestOptions, configuration)

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
         * @summary Tamamlanma sertifikası (PAdES B-T)
         * @param {string} id 
         * @param {string} [lang] tr | en
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdCertificateGet: async (id: string, lang?: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1DemandsIdCertificateGet', 'id', id)
            const localVarPath = `/api/v1/demands/{id}/certificate`
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

            if (lang !== undefined) {
                localVarQueryParameter['lang'] = lang;
            }

            localVarHeaderParameter['Accept'] = 'application/pdf,application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API\'den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 `DEMAND_COMPLETED`. 
         * @summary Sözleşme sil (yalnızca tamamlanmamış)
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdDelete: async (id: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1DemandsIdDelete', 'id', id)
            const localVarPath = `/api/v1/demands/{id}`
                .replace('{id}', encodeURIComponent(String(id)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'DELETE', ...baseOptions, ...options};
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
         * Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
         * @summary Tekil tarafa imza davetini tekrar gönder
         * @param {string} id 
         * @param {string} partyId 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdPartiesPartyIdResendPost: async (id: string, partyId: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1DemandsIdPartiesPartyIdResendPost', 'id', id)
            // verify required parameter 'partyId' is not null or undefined
            assertParamExists('apiV1DemandsIdPartiesPartyIdResendPost', 'partyId', partyId)
            const localVarPath = `/api/v1/demands/{id}/parties/{partyId}/resend`
                .replace('{id}', encodeURIComponent(String(id)))
                .replace('{partyId}', encodeURIComponent(String(partyId)));
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
         * Tamamlanmış sözleşmenin imzalı PDF\'ini indirir. Public `/sonuc/{id}/pdf`\'in aksine API key ownership\'i zorunludur. 
         * @summary İmzalı sözleşme PDF\'i (auth\'lu indirme)
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdPdfGet: async (id: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1DemandsIdPdfGet', 'id', id)
            const localVarPath = `/api/v1/demands/{id}/pdf`
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

            localVarHeaderParameter['Accept'] = 'application/pdf,application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};

            return {
                url: toPathString(localVarUrlObj),
                options: localVarRequestOptions,
            };
        },
        /**
         * Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP `ip_masked` (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
         * @summary İmza denetim izi (maskeli)
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdTimelineGet: async (id: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1DemandsIdTimelineGet', 'id', id)
            const localVarPath = `/api/v1/demands/{id}/timeline`
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
         * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field\'ları `variables` payload\'undan doldurur ve imzalama URL\'lerini döner.  **Variable resolution:** - Item\'ın `template_party_id` non-null → `party_mapping[i].variables`\'ta   o slug var ise oradan uygulanır - Yoksa root `variables`\'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value\'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı 
         * @summary Sözleşme oluştur (şablondan)
         * @param {CreateDemandRequest} createDemandRequest 
         * @param {string} [idempotencyKey] Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsPost: async (createDemandRequest: CreateDemandRequest, idempotencyKey?: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
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

            if (idempotencyKey != null) {
                localVarHeaderParameter['Idempotency-Key'] = String(idempotencyKey);
            }
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
         * @param {string} parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir. 
         * @param {string} [idempotencyKey] Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
         * @param {string} [order] Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;)
         * @param {string} [title] 
         * @param {string} [description] 
         * @param {string} [fieldTemplateId] Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;. 
         * @param {ApiV1DemandsUploadPostForceEnum} [force] Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için. 
         * @param {ApiV1DemandsUploadPostSendInvitationsEnum} [sendInvitations] &#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60; ve &#x60;\\\&quot;all\\\&quot;&#x60; tüm kanalları kullanır;   &#x60;\\\&quot;email\\\&quot;&#x60; yalnız e-posta, &#x60;\\\&quot;sms\\\&quot;&#x60; telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği &#x60;Idempotency-Key&#x60;   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı &#x60;DUPLICATE_SUSPECTED&#x60; yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte &#x60;Idempotency-Key&#x60; gönderin;   anahtar yoksa yeniden göndermek yerine   &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın. 
         * @param {ApiV1DemandsUploadPostOnAnchorMissEnum} [onAnchorMiss] Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API\\\&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır. 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsUploadPost: async (files: Array<File>, parties: string, idempotencyKey?: string, order?: string, title?: string, description?: string, fieldTemplateId?: string, force?: ApiV1DemandsUploadPostForceEnum, sendInvitations?: ApiV1DemandsUploadPostSendInvitationsEnum, onAnchorMiss?: ApiV1DemandsUploadPostOnAnchorMissEnum, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
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

            if (fieldTemplateId !== undefined) { 
                localVarFormParams.append('field_template_id', fieldTemplateId as any);
            }

            if (force !== undefined) { 
                localVarFormParams.append('force', force as any);
            }

            if (sendInvitations !== undefined) { 
                localVarFormParams.append('send_invitations', sendInvitations as any);
            }

            if (onAnchorMiss !== undefined) { 
                localVarFormParams.append('on_anchor_miss', onAnchorMiss as any);
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
        /**
         * Bir Alan Şablonunun (`kind: FIELD_LAYOUT`) yüklediğiniz PDF\'e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  `POST /api/v1/demands/upload` + `field_template_id` çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API\'de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (`data.resolvable: false`) — kuru koşumun cevabı \"uygulanamaz\"dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 `RATE_LIMITED`). 
         * @summary Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
         * @param {string} id Alan Şablonu (FIELD_LAYOUT) kimliği
         * @param {Array<File>} files Tek PDF belge
         * @param {ApiV1FieldTemplatesIdPreviewLayoutPostOnAnchorMissEnum} [onAnchorMiss] &#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;). 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1FieldTemplatesIdPreviewLayoutPost: async (id: string, files: Array<File>, onAnchorMiss?: ApiV1FieldTemplatesIdPreviewLayoutPostOnAnchorMissEnum, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1FieldTemplatesIdPreviewLayoutPost', 'id', id)
            // verify required parameter 'files' is not null or undefined
            assertParamExists('apiV1FieldTemplatesIdPreviewLayoutPost', 'files', files)
            const localVarPath = `/api/v1/field-templates/{id}/preview-layout`
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
            const localVarFormParams = new ((configuration && configuration.formDataCtor) || FormData)();

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)

            if (files) {
                files.forEach((element) => {
                    localVarFormParams.append('files', element as any);
                })
            }


            if (onAnchorMiss !== undefined) { 
                localVarFormParams.append('on_anchor_miss', onAnchorMiss as any);
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
         * Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \"bulk send\" modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Davranış:** - `rows` en fazla 10 (aşarsa 400 `BULK_MAX_10`). Daha büyük listeler   istemci tarafında 10\'arlı parçalara bölünür. - Kredi: sabit \"1 satır = 1 kredi\" değildir, her satırın maliyeti   `POST /demands` ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; `eidas_timestamp` seçilirse   satır başına +1 kredi eklenir). PAdES seviye eki yalnız QES\'te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   `ocr_id` ve `liveness` +1 kredi, diğerleri 0\'dır. `options`   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır `failed` (`error: \"INSUFFICIENT_CREDITS\"`)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine `failed` döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (`demand_id` response\'ta bulunur, davet   gönderilmemiştir). - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace\'in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar `failed` olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - `X-Workspace-Id` header\'ı ile organizasyon workspace\'i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
         * @summary Toplu sözleşme oluştur (tek şablondan N alıcı)
         * @param {ApiV1DemandsBulkPostRequest} apiV1DemandsBulkPostRequest 
         * @param {string} [xWorkspaceId] Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;). 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsBulkPost(apiV1DemandsBulkPostRequest: ApiV1DemandsBulkPostRequest, xWorkspaceId?: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsBulkPost200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsBulkPost(apiV1DemandsBulkPostRequest, xWorkspaceId, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsBulkPost']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, `DRAFT` ise sözleşmeyi `PENDING`\'e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  🔴 **Bu uçta yukarıdaki `/documents*` ailesinin bayrak kapısı (`ENVELOPE_DECISION_ENFORCE`) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — `dispatch_notifications: false` ile oluşturulur, ardından `POST .../documents` / `POST .../documents/upload` ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; `send_invitations` gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir.  **Kredi:** tek tahsilat noktası burasıdır (`reconcileDemandSigningCost`). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (`dispatched: false` döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. `v1ResendRateLimiter`, `/parties/{partyId}/resend` ile AYNI dakika-bazlı freni paylaşır). 
         * @summary Zarfı imzaya gönder (yayınla + davet)
         * @param {string} demandId 
         * @param {ApiV1DemandsDemandIdDispatchPostRequest} [apiV1DemandsDemandIdDispatchPostRequest] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsDemandIdDispatchPost(demandId: string, apiV1DemandsDemandIdDispatchPostRequest?: ApiV1DemandsDemandIdDispatchPostRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsDemandIdDispatchPost200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsDemandIdDispatchPost(demandId, apiV1DemandsDemandIdDispatchPostRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsDemandIdDispatchPost']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Belgeye atanmış imzacı kümesini **tam olarak** `party_ids` ile değiştirir (eski atamalar silinir, yenileri yazılır). `party_ids` boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 `INVALID_PARTY_ID` — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 `ASSIGNMENT_HAS_DECISION`). 
         * @summary Belgeye imzacı ata (tam-küme replace)
         * @param {string} demandId 
         * @param {string} docId 
         * @param {ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest} apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut(demandId: string, docId: string, apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest: ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsDemandIdDocumentsPost201Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N\'e yeniden sıralanır. **Zarftaki son belge silinemez** (400 `CANNOT_DELETE_LAST_DOCUMENT`) — zarf hiç boş kalamaz. 
         * @summary Belgeyi zarftan sil
         * @param {string} demandId 
         * @param {string} docId 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsDemandIdDocumentsDocIdDelete(demandId: string, docId: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1TemplatesIdDelete200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsDemandIdDocumentsDocIdDelete(demandId, docId, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsDemandIdDocumentsDocIdDelete']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (`PUT .../assignments`). 
         * @summary Belge metadata güncelle
         * @param {string} demandId 
         * @param {string} docId 
         * @param {ApiV1DemandsDemandIdDocumentsDocIdPatchRequest} apiV1DemandsDemandIdDocumentsDocIdPatchRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsDemandIdDocumentsDocIdPatch(demandId: string, docId: string, apiV1DemandsDemandIdDocumentsDocIdPatchRequest: ApiV1DemandsDemandIdDocumentsDocIdPatchRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsDemandIdDocumentsPost201Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsDemandIdDocumentsDocIdPatch(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdPatchRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsDemandIdDocumentsDocIdPatch']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm `/documents*` + `/documents/{docId}*` + `/documents/{docId}/assignments` uçları `ENVELOPE_DECISION_ENFORCE` bayrağı **arkasındadır**; bayrak kapalıyken **409 `ENVELOPE_MULTI_DOC_DISABLED`** döner. Bu, dashboard\'un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca `POST /demands/{demandId}/dispatch` çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. `?view=wizard` verilirse tam şekil döner (`EnvelopeDocument` şemasındaki alan notlarına bakın). 
         * @summary Zarf belge listesi
         * @param {string} demandId 
         * @param {ApiV1DemandsDemandIdDocumentsGetViewEnum} [view] &#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil).
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsDemandIdDocumentsGet(demandId: string, view?: ApiV1DemandsDemandIdDocumentsGetViewEnum, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsDemandIdDocumentsGet200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsDemandIdDocumentsGet(demandId, view, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsDemandIdDocumentsGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. `document_ids` zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 `ORDER_SET_MISMATCH`** döner. 
         * @summary Zarftaki belgelerin sırasını değiştir
         * @param {string} demandId 
         * @param {ApiV1DemandsDemandIdDocumentsOrderPutRequest} apiV1DemandsDemandIdDocumentsOrderPutRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsDemandIdDocumentsOrderPut(demandId: string, apiV1DemandsDemandIdDocumentsOrderPutRequest: ApiV1DemandsDemandIdDocumentsOrderPutRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsDemandIdDocumentsGet200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsDemandIdDocumentsOrderPut(demandId, apiV1DemandsDemandIdDocumentsOrderPutRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsDemandIdDocumentsOrderPut']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: `POST /demands/{demandId}/documents/upload`.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`\'te yapılır. 
         * @summary Zarfa metadata-only belge ekle
         * @param {string} demandId 
         * @param {ApiV1DemandsDemandIdDocumentsPostRequest} apiV1DemandsDemandIdDocumentsPostRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsDemandIdDocumentsPost(demandId: string, apiV1DemandsDemandIdDocumentsPostRequest: ApiV1DemandsDemandIdDocumentsPostRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsDemandIdDocumentsPost201Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsDemandIdDocumentsPost(demandId, apiV1DemandsDemandIdDocumentsPostRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsDemandIdDocumentsPost']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  `idempotency_key` **zorunludur** (gövde alanı; `Idempotency-Key` HEADER\'ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı `(demandId, idempotency_key)` çifti ile tekrar çağrı **409 `IDEMPOTENT_REPLAY`** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO\'su döner.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`\'te yapılır. 
         * @summary Zarfa dosya yükle (belge başına tek dosya)
         * @param {string} demandId 
         * @param {File} file PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.
         * @param {string} idempotencyKey Zorunlu tekrar-koruma anahtarı.
         * @param {string} title 
         * @param {ApiV1DemandsDemandIdDocumentsUploadPostDocKindEnum} [docKind] 
         * @param {ApiV1DemandsDemandIdDocumentsUploadPostIsRequiredEnum} [isRequired] Multipart alanı — string olarak gönderilir.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsDemandIdDocumentsUploadPost(demandId: string, file: File, idempotencyKey: string, title: string, docKind?: ApiV1DemandsDemandIdDocumentsUploadPostDocKindEnum, isRequired?: ApiV1DemandsDemandIdDocumentsUploadPostIsRequiredEnum, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsDemandIdDocumentsPost201Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsDemandIdDocumentsUploadPost(demandId, file, idempotencyKey, title, docKind, isRequired, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsDemandIdDocumentsUploadPost']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (`parties_total`, `parties_signed`). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için `GET /demands/{id}`. 
         * @summary Sözleşme listesi (counts-only, PII\'siz)
         * @param {ApiV1DemandsGetStatusEnum} [status] 
         * @param {string} [q] Başlık araması
         * @param {string} [from] 
         * @param {string} [to] 
         * @param {string} [templateId] 
         * @param {number} [page] 
         * @param {number} [limit] Sayfa boyutu (page_size ile aynı)
         * @param {string} [sort] alan:yön (ör. createdAt:desc)
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsGet(status?: ApiV1DemandsGetStatusEnum, q?: string, from?: string, to?: string, templateId?: string, page?: number, limit?: number, sort?: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsGet200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsGet(status, q, from, to, templateId, page, limit, sort, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Çok-belgeli zarfta TEK bir belgenin imzalı PDF\'ini indirir. Zarf-geneli `/demands/{id}/pdf` ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND\'lenir (başka zarfın belgesi istenirse 404). 
         * @summary Belge-özgü imzalı PDF (çok-belgeli zarf)
         * @param {string} id 
         * @param {string} documentId Zarftaki belgenin kimliği.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsIdBelgeDocumentIdPdfGet(id: string, documentId: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<File>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsIdBelgeDocumentIdPdfGet(id, documentId, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsIdBelgeDocumentIdPdfGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Bekleyen bir sözleşmeyi iptal eder (status=CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
         * @summary Sözleşme iptal (void)
         * @param {string} id 
         * @param {ApiV1DemandsIdCancelPostRequest} [apiV1DemandsIdCancelPostRequest] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsIdCancelPost(id: string, apiV1DemandsIdCancelPostRequest?: ApiV1DemandsIdCancelPostRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsIdCancelPost200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsIdCancelPost(id, apiV1DemandsIdCancelPostRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsIdCancelPost']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
         * @summary Tamamlanma sertifikası (PAdES B-T)
         * @param {string} id 
         * @param {string} [lang] tr | en
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsIdCertificateGet(id: string, lang?: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<File>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsIdCertificateGet(id, lang, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsIdCertificateGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API\'den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 `DEMAND_COMPLETED`. 
         * @summary Sözleşme sil (yalnızca tamamlanmamış)
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsIdDelete(id: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1TemplatesIdDelete200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsIdDelete(id, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsIdDelete']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
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
         * Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
         * @summary Tekil tarafa imza davetini tekrar gönder
         * @param {string} id 
         * @param {string} partyId 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsIdPartiesPartyIdResendPost(id: string, partyId: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsIdPartiesPartyIdResendPost200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsIdPartiesPartyIdResendPost(id, partyId, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsIdPartiesPartyIdResendPost']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Tamamlanmış sözleşmenin imzalı PDF\'ini indirir. Public `/sonuc/{id}/pdf`\'in aksine API key ownership\'i zorunludur. 
         * @summary İmzalı sözleşme PDF\'i (auth\'lu indirme)
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsIdPdfGet(id: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<File>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsIdPdfGet(id, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsIdPdfGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP `ip_masked` (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
         * @summary İmza denetim izi (maskeli)
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsIdTimelineGet(id: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsIdTimelineGet200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsIdTimelineGet(id, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsIdTimelineGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field\'ları `variables` payload\'undan doldurur ve imzalama URL\'lerini döner.  **Variable resolution:** - Item\'ın `template_party_id` non-null → `party_mapping[i].variables`\'ta   o slug var ise oradan uygulanır - Yoksa root `variables`\'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value\'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı 
         * @summary Sözleşme oluştur (şablondan)
         * @param {CreateDemandRequest} createDemandRequest 
         * @param {string} [idempotencyKey] Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsPost(createDemandRequest: CreateDemandRequest, idempotencyKey?: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsPost201Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsPost(createDemandRequest, idempotencyKey, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsPost']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF\'e birleştirilir, office formatları LibreOffice ile PDF\'e çevrilir. 
         * @summary Dosya upload ile sözleşme oluştur (şablonsuz)
         * @param {Array<File>} files 1 belge VEYA 1-20 görsel
         * @param {string} parties JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir. 
         * @param {string} [idempotencyKey] Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
         * @param {string} [order] Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;)
         * @param {string} [title] 
         * @param {string} [description] 
         * @param {string} [fieldTemplateId] Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;. 
         * @param {ApiV1DemandsUploadPostForceEnum} [force] Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için. 
         * @param {ApiV1DemandsUploadPostSendInvitationsEnum} [sendInvitations] &#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60; ve &#x60;\\\&quot;all\\\&quot;&#x60; tüm kanalları kullanır;   &#x60;\\\&quot;email\\\&quot;&#x60; yalnız e-posta, &#x60;\\\&quot;sms\\\&quot;&#x60; telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği &#x60;Idempotency-Key&#x60;   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı &#x60;DUPLICATE_SUSPECTED&#x60; yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte &#x60;Idempotency-Key&#x60; gönderin;   anahtar yoksa yeniden göndermek yerine   &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın. 
         * @param {ApiV1DemandsUploadPostOnAnchorMissEnum} [onAnchorMiss] Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API\\\&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır. 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1DemandsUploadPost(files: Array<File>, parties: string, idempotencyKey?: string, order?: string, title?: string, description?: string, fieldTemplateId?: string, force?: ApiV1DemandsUploadPostForceEnum, sendInvitations?: ApiV1DemandsUploadPostSendInvitationsEnum, onAnchorMiss?: ApiV1DemandsUploadPostOnAnchorMissEnum, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1DemandsUploadPost201Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1DemandsUploadPost(files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1DemandsUploadPost']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Bir Alan Şablonunun (`kind: FIELD_LAYOUT`) yüklediğiniz PDF\'e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  `POST /api/v1/demands/upload` + `field_template_id` çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API\'de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (`data.resolvable: false`) — kuru koşumun cevabı \"uygulanamaz\"dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 `RATE_LIMITED`). 
         * @summary Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
         * @param {string} id Alan Şablonu (FIELD_LAYOUT) kimliği
         * @param {Array<File>} files Tek PDF belge
         * @param {ApiV1FieldTemplatesIdPreviewLayoutPostOnAnchorMissEnum} [onAnchorMiss] &#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;). 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1FieldTemplatesIdPreviewLayoutPost(id: string, files: Array<File>, onAnchorMiss?: ApiV1FieldTemplatesIdPreviewLayoutPostOnAnchorMissEnum, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1FieldTemplatesIdPreviewLayoutPost200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1FieldTemplatesIdPreviewLayoutPost(id, files, onAnchorMiss, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['DemandsApi.apiV1FieldTemplatesIdPreviewLayoutPost']?.[localVarOperationServerIndex]?.url;
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
         * Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \"bulk send\" modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Davranış:** - `rows` en fazla 10 (aşarsa 400 `BULK_MAX_10`). Daha büyük listeler   istemci tarafında 10\'arlı parçalara bölünür. - Kredi: sabit \"1 satır = 1 kredi\" değildir, her satırın maliyeti   `POST /demands` ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; `eidas_timestamp` seçilirse   satır başına +1 kredi eklenir). PAdES seviye eki yalnız QES\'te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   `ocr_id` ve `liveness` +1 kredi, diğerleri 0\'dır. `options`   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır `failed` (`error: \"INSUFFICIENT_CREDITS\"`)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine `failed` döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (`demand_id` response\'ta bulunur, davet   gönderilmemiştir). - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace\'in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar `failed` olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - `X-Workspace-Id` header\'ı ile organizasyon workspace\'i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
         * @summary Toplu sözleşme oluştur (tek şablondan N alıcı)
         * @param {DemandsApiApiV1DemandsBulkPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsBulkPost(requestParameters: DemandsApiApiV1DemandsBulkPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsBulkPost200Response> {
            return localVarFp.apiV1DemandsBulkPost(requestParameters.apiV1DemandsBulkPostRequest, requestParameters.xWorkspaceId, options).then((request) => request(axios, basePath));
        },
        /**
         * Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, `DRAFT` ise sözleşmeyi `PENDING`\'e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  🔴 **Bu uçta yukarıdaki `/documents*` ailesinin bayrak kapısı (`ENVELOPE_DECISION_ENFORCE`) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — `dispatch_notifications: false` ile oluşturulur, ardından `POST .../documents` / `POST .../documents/upload` ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; `send_invitations` gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir.  **Kredi:** tek tahsilat noktası burasıdır (`reconcileDemandSigningCost`). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (`dispatched: false` döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. `v1ResendRateLimiter`, `/parties/{partyId}/resend` ile AYNI dakika-bazlı freni paylaşır). 
         * @summary Zarfı imzaya gönder (yayınla + davet)
         * @param {DemandsApiApiV1DemandsDemandIdDispatchPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDispatchPost(requestParameters: DemandsApiApiV1DemandsDemandIdDispatchPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsDemandIdDispatchPost200Response> {
            return localVarFp.apiV1DemandsDemandIdDispatchPost(requestParameters.demandId, requestParameters.apiV1DemandsDemandIdDispatchPostRequest, options).then((request) => request(axios, basePath));
        },
        /**
         * Belgeye atanmış imzacı kümesini **tam olarak** `party_ids` ile değiştirir (eski atamalar silinir, yenileri yazılır). `party_ids` boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 `INVALID_PARTY_ID` — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 `ASSIGNMENT_HAS_DECISION`). 
         * @summary Belgeye imzacı ata (tam-küme replace)
         * @param {DemandsApiApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut(requestParameters: DemandsApiApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsDemandIdDocumentsPost201Response> {
            return localVarFp.apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut(requestParameters.demandId, requestParameters.docId, requestParameters.apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, options).then((request) => request(axios, basePath));
        },
        /**
         * Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N\'e yeniden sıralanır. **Zarftaki son belge silinemez** (400 `CANNOT_DELETE_LAST_DOCUMENT`) — zarf hiç boş kalamaz. 
         * @summary Belgeyi zarftan sil
         * @param {DemandsApiApiV1DemandsDemandIdDocumentsDocIdDeleteRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDocumentsDocIdDelete(requestParameters: DemandsApiApiV1DemandsDemandIdDocumentsDocIdDeleteRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1TemplatesIdDelete200Response> {
            return localVarFp.apiV1DemandsDemandIdDocumentsDocIdDelete(requestParameters.demandId, requestParameters.docId, options).then((request) => request(axios, basePath));
        },
        /**
         * Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (`PUT .../assignments`). 
         * @summary Belge metadata güncelle
         * @param {DemandsApiApiV1DemandsDemandIdDocumentsDocIdPatchRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDocumentsDocIdPatch(requestParameters: DemandsApiApiV1DemandsDemandIdDocumentsDocIdPatchRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsDemandIdDocumentsPost201Response> {
            return localVarFp.apiV1DemandsDemandIdDocumentsDocIdPatch(requestParameters.demandId, requestParameters.docId, requestParameters.apiV1DemandsDemandIdDocumentsDocIdPatchRequest, options).then((request) => request(axios, basePath));
        },
        /**
         * Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm `/documents*` + `/documents/{docId}*` + `/documents/{docId}/assignments` uçları `ENVELOPE_DECISION_ENFORCE` bayrağı **arkasındadır**; bayrak kapalıyken **409 `ENVELOPE_MULTI_DOC_DISABLED`** döner. Bu, dashboard\'un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca `POST /demands/{demandId}/dispatch` çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. `?view=wizard` verilirse tam şekil döner (`EnvelopeDocument` şemasındaki alan notlarına bakın). 
         * @summary Zarf belge listesi
         * @param {DemandsApiApiV1DemandsDemandIdDocumentsGetRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDocumentsGet(requestParameters: DemandsApiApiV1DemandsDemandIdDocumentsGetRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsDemandIdDocumentsGet200Response> {
            return localVarFp.apiV1DemandsDemandIdDocumentsGet(requestParameters.demandId, requestParameters.view, options).then((request) => request(axios, basePath));
        },
        /**
         * Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. `document_ids` zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 `ORDER_SET_MISMATCH`** döner. 
         * @summary Zarftaki belgelerin sırasını değiştir
         * @param {DemandsApiApiV1DemandsDemandIdDocumentsOrderPutRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDocumentsOrderPut(requestParameters: DemandsApiApiV1DemandsDemandIdDocumentsOrderPutRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsDemandIdDocumentsGet200Response> {
            return localVarFp.apiV1DemandsDemandIdDocumentsOrderPut(requestParameters.demandId, requestParameters.apiV1DemandsDemandIdDocumentsOrderPutRequest, options).then((request) => request(axios, basePath));
        },
        /**
         * Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: `POST /demands/{demandId}/documents/upload`.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`\'te yapılır. 
         * @summary Zarfa metadata-only belge ekle
         * @param {DemandsApiApiV1DemandsDemandIdDocumentsPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDocumentsPost(requestParameters: DemandsApiApiV1DemandsDemandIdDocumentsPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsDemandIdDocumentsPost201Response> {
            return localVarFp.apiV1DemandsDemandIdDocumentsPost(requestParameters.demandId, requestParameters.apiV1DemandsDemandIdDocumentsPostRequest, options).then((request) => request(axios, basePath));
        },
        /**
         * Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  `idempotency_key` **zorunludur** (gövde alanı; `Idempotency-Key` HEADER\'ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı `(demandId, idempotency_key)` çifti ile tekrar çağrı **409 `IDEMPOTENT_REPLAY`** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO\'su döner.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`\'te yapılır. 
         * @summary Zarfa dosya yükle (belge başına tek dosya)
         * @param {DemandsApiApiV1DemandsDemandIdDocumentsUploadPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsDemandIdDocumentsUploadPost(requestParameters: DemandsApiApiV1DemandsDemandIdDocumentsUploadPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsDemandIdDocumentsPost201Response> {
            return localVarFp.apiV1DemandsDemandIdDocumentsUploadPost(requestParameters.demandId, requestParameters.file, requestParameters.idempotencyKey, requestParameters.title, requestParameters.docKind, requestParameters.isRequired, options).then((request) => request(axios, basePath));
        },
        /**
         * Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (`parties_total`, `parties_signed`). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için `GET /demands/{id}`. 
         * @summary Sözleşme listesi (counts-only, PII\'siz)
         * @param {DemandsApiApiV1DemandsGetRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsGet(requestParameters: DemandsApiApiV1DemandsGetRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsGet200Response> {
            return localVarFp.apiV1DemandsGet(requestParameters.status, requestParameters.q, requestParameters.from, requestParameters.to, requestParameters.templateId, requestParameters.page, requestParameters.limit, requestParameters.sort, options).then((request) => request(axios, basePath));
        },
        /**
         * Çok-belgeli zarfta TEK bir belgenin imzalı PDF\'ini indirir. Zarf-geneli `/demands/{id}/pdf` ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND\'lenir (başka zarfın belgesi istenirse 404). 
         * @summary Belge-özgü imzalı PDF (çok-belgeli zarf)
         * @param {DemandsApiApiV1DemandsIdBelgeDocumentIdPdfGetRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdBelgeDocumentIdPdfGet(requestParameters: DemandsApiApiV1DemandsIdBelgeDocumentIdPdfGetRequest, options?: RawAxiosRequestConfig): AxiosPromise<File> {
            return localVarFp.apiV1DemandsIdBelgeDocumentIdPdfGet(requestParameters.id, requestParameters.documentId, options).then((request) => request(axios, basePath));
        },
        /**
         * Bekleyen bir sözleşmeyi iptal eder (status=CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
         * @summary Sözleşme iptal (void)
         * @param {DemandsApiApiV1DemandsIdCancelPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdCancelPost(requestParameters: DemandsApiApiV1DemandsIdCancelPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsIdCancelPost200Response> {
            return localVarFp.apiV1DemandsIdCancelPost(requestParameters.id, requestParameters.apiV1DemandsIdCancelPostRequest, options).then((request) => request(axios, basePath));
        },
        /**
         * Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
         * @summary Tamamlanma sertifikası (PAdES B-T)
         * @param {DemandsApiApiV1DemandsIdCertificateGetRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdCertificateGet(requestParameters: DemandsApiApiV1DemandsIdCertificateGetRequest, options?: RawAxiosRequestConfig): AxiosPromise<File> {
            return localVarFp.apiV1DemandsIdCertificateGet(requestParameters.id, requestParameters.lang, options).then((request) => request(axios, basePath));
        },
        /**
         * Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API\'den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 `DEMAND_COMPLETED`. 
         * @summary Sözleşme sil (yalnızca tamamlanmamış)
         * @param {DemandsApiApiV1DemandsIdDeleteRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdDelete(requestParameters: DemandsApiApiV1DemandsIdDeleteRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1TemplatesIdDelete200Response> {
            return localVarFp.apiV1DemandsIdDelete(requestParameters.id, options).then((request) => request(axios, basePath));
        },
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
         * Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
         * @summary Tekil tarafa imza davetini tekrar gönder
         * @param {DemandsApiApiV1DemandsIdPartiesPartyIdResendPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdPartiesPartyIdResendPost(requestParameters: DemandsApiApiV1DemandsIdPartiesPartyIdResendPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsIdPartiesPartyIdResendPost200Response> {
            return localVarFp.apiV1DemandsIdPartiesPartyIdResendPost(requestParameters.id, requestParameters.partyId, options).then((request) => request(axios, basePath));
        },
        /**
         * Tamamlanmış sözleşmenin imzalı PDF\'ini indirir. Public `/sonuc/{id}/pdf`\'in aksine API key ownership\'i zorunludur. 
         * @summary İmzalı sözleşme PDF\'i (auth\'lu indirme)
         * @param {DemandsApiApiV1DemandsIdPdfGetRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdPdfGet(requestParameters: DemandsApiApiV1DemandsIdPdfGetRequest, options?: RawAxiosRequestConfig): AxiosPromise<File> {
            return localVarFp.apiV1DemandsIdPdfGet(requestParameters.id, options).then((request) => request(axios, basePath));
        },
        /**
         * Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP `ip_masked` (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
         * @summary İmza denetim izi (maskeli)
         * @param {DemandsApiApiV1DemandsIdTimelineGetRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsIdTimelineGet(requestParameters: DemandsApiApiV1DemandsIdTimelineGetRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsIdTimelineGet200Response> {
            return localVarFp.apiV1DemandsIdTimelineGet(requestParameters.id, options).then((request) => request(axios, basePath));
        },
        /**
         * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field\'ları `variables` payload\'undan doldurur ve imzalama URL\'lerini döner.  **Variable resolution:** - Item\'ın `template_party_id` non-null → `party_mapping[i].variables`\'ta   o slug var ise oradan uygulanır - Yoksa root `variables`\'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value\'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı 
         * @summary Sözleşme oluştur (şablondan)
         * @param {DemandsApiApiV1DemandsPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsPost(requestParameters: DemandsApiApiV1DemandsPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsPost201Response> {
            return localVarFp.apiV1DemandsPost(requestParameters.createDemandRequest, requestParameters.idempotencyKey, options).then((request) => request(axios, basePath));
        },
        /**
         * Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF\'e birleştirilir, office formatları LibreOffice ile PDF\'e çevrilir. 
         * @summary Dosya upload ile sözleşme oluştur (şablonsuz)
         * @param {DemandsApiApiV1DemandsUploadPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1DemandsUploadPost(requestParameters: DemandsApiApiV1DemandsUploadPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1DemandsUploadPost201Response> {
            return localVarFp.apiV1DemandsUploadPost(requestParameters.files, requestParameters.parties, requestParameters.idempotencyKey, requestParameters.order, requestParameters.title, requestParameters.description, requestParameters.fieldTemplateId, requestParameters.force, requestParameters.sendInvitations, requestParameters.onAnchorMiss, options).then((request) => request(axios, basePath));
        },
        /**
         * Bir Alan Şablonunun (`kind: FIELD_LAYOUT`) yüklediğiniz PDF\'e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  `POST /api/v1/demands/upload` + `field_template_id` çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API\'de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (`data.resolvable: false`) — kuru koşumun cevabı \"uygulanamaz\"dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 `RATE_LIMITED`). 
         * @summary Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
         * @param {DemandsApiApiV1FieldTemplatesIdPreviewLayoutPostRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1FieldTemplatesIdPreviewLayoutPost(requestParameters: DemandsApiApiV1FieldTemplatesIdPreviewLayoutPostRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1FieldTemplatesIdPreviewLayoutPost200Response> {
            return localVarFp.apiV1FieldTemplatesIdPreviewLayoutPost(requestParameters.id, requestParameters.files, requestParameters.onAnchorMiss, options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * Request parameters for apiV1DemandsBulkPost operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsBulkPostRequest {
    readonly apiV1DemandsBulkPostRequest: ApiV1DemandsBulkPostRequest

    /**
     * Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;). 
     */
    readonly xWorkspaceId?: string
}

/**
 * Request parameters for apiV1DemandsDemandIdDispatchPost operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsDemandIdDispatchPostRequest {
    readonly demandId: string

    readonly apiV1DemandsDemandIdDispatchPostRequest?: ApiV1DemandsDemandIdDispatchPostRequest
}

/**
 * Request parameters for apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest {
    readonly demandId: string

    readonly docId: string

    readonly apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest: ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest
}

/**
 * Request parameters for apiV1DemandsDemandIdDocumentsDocIdDelete operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsDemandIdDocumentsDocIdDeleteRequest {
    readonly demandId: string

    readonly docId: string
}

/**
 * Request parameters for apiV1DemandsDemandIdDocumentsDocIdPatch operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsDemandIdDocumentsDocIdPatchRequest {
    readonly demandId: string

    readonly docId: string

    readonly apiV1DemandsDemandIdDocumentsDocIdPatchRequest: ApiV1DemandsDemandIdDocumentsDocIdPatchRequest
}

/**
 * Request parameters for apiV1DemandsDemandIdDocumentsGet operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsDemandIdDocumentsGetRequest {
    readonly demandId: string

    /**
     * &#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil).
     */
    readonly view?: ApiV1DemandsDemandIdDocumentsGetViewEnum
}

/**
 * Request parameters for apiV1DemandsDemandIdDocumentsOrderPut operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsDemandIdDocumentsOrderPutRequest {
    readonly demandId: string

    readonly apiV1DemandsDemandIdDocumentsOrderPutRequest: ApiV1DemandsDemandIdDocumentsOrderPutRequest
}

/**
 * Request parameters for apiV1DemandsDemandIdDocumentsPost operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsDemandIdDocumentsPostRequest {
    readonly demandId: string

    readonly apiV1DemandsDemandIdDocumentsPostRequest: ApiV1DemandsDemandIdDocumentsPostRequest
}

/**
 * Request parameters for apiV1DemandsDemandIdDocumentsUploadPost operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsDemandIdDocumentsUploadPostRequest {
    readonly demandId: string

    /**
     * PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.
     */
    readonly file: File

    /**
     * Zorunlu tekrar-koruma anahtarı.
     */
    readonly idempotencyKey: string

    readonly title: string

    readonly docKind?: ApiV1DemandsDemandIdDocumentsUploadPostDocKindEnum

    /**
     * Multipart alanı — string olarak gönderilir.
     */
    readonly isRequired?: ApiV1DemandsDemandIdDocumentsUploadPostIsRequiredEnum
}

/**
 * Request parameters for apiV1DemandsGet operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsGetRequest {
    readonly status?: ApiV1DemandsGetStatusEnum

    /**
     * Başlık araması
     */
    readonly q?: string

    readonly from?: string

    readonly to?: string

    readonly templateId?: string

    readonly page?: number

    /**
     * Sayfa boyutu (page_size ile aynı)
     */
    readonly limit?: number

    /**
     * alan:yön (ör. createdAt:desc)
     */
    readonly sort?: string
}

/**
 * Request parameters for apiV1DemandsIdBelgeDocumentIdPdfGet operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsIdBelgeDocumentIdPdfGetRequest {
    readonly id: string

    /**
     * Zarftaki belgenin kimliği.
     */
    readonly documentId: string
}

/**
 * Request parameters for apiV1DemandsIdCancelPost operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsIdCancelPostRequest {
    readonly id: string

    readonly apiV1DemandsIdCancelPostRequest?: ApiV1DemandsIdCancelPostRequest
}

/**
 * Request parameters for apiV1DemandsIdCertificateGet operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsIdCertificateGetRequest {
    readonly id: string

    /**
     * tr | en
     */
    readonly lang?: string
}

/**
 * Request parameters for apiV1DemandsIdDelete operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsIdDeleteRequest {
    readonly id: string
}

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
 * Request parameters for apiV1DemandsIdPartiesPartyIdResendPost operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsIdPartiesPartyIdResendPostRequest {
    readonly id: string

    readonly partyId: string
}

/**
 * Request parameters for apiV1DemandsIdPdfGet operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsIdPdfGetRequest {
    readonly id: string
}

/**
 * Request parameters for apiV1DemandsIdTimelineGet operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsIdTimelineGetRequest {
    readonly id: string
}

/**
 * Request parameters for apiV1DemandsPost operation in DemandsApi.
 */
export interface DemandsApiApiV1DemandsPostRequest {
    readonly createDemandRequest: CreateDemandRequest

    /**
     * Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
     */
    readonly idempotencyKey?: string
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
     * JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir. 
     */
    readonly parties: string

    /**
     * Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
     */
    readonly idempotencyKey?: string

    /**
     * Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;)
     */
    readonly order?: string

    readonly title?: string

    readonly description?: string

    /**
     * Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;. 
     */
    readonly fieldTemplateId?: string

    /**
     * Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için. 
     */
    readonly force?: ApiV1DemandsUploadPostForceEnum

    /**
     * &#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60; ve &#x60;\\\&quot;all\\\&quot;&#x60; tüm kanalları kullanır;   &#x60;\\\&quot;email\\\&quot;&#x60; yalnız e-posta, &#x60;\\\&quot;sms\\\&quot;&#x60; telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği &#x60;Idempotency-Key&#x60;   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı &#x60;DUPLICATE_SUSPECTED&#x60; yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte &#x60;Idempotency-Key&#x60; gönderin;   anahtar yoksa yeniden göndermek yerine   &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın. 
     */
    readonly sendInvitations?: ApiV1DemandsUploadPostSendInvitationsEnum

    /**
     * Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API\\\&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır. 
     */
    readonly onAnchorMiss?: ApiV1DemandsUploadPostOnAnchorMissEnum
}

/**
 * Request parameters for apiV1FieldTemplatesIdPreviewLayoutPost operation in DemandsApi.
 */
export interface DemandsApiApiV1FieldTemplatesIdPreviewLayoutPostRequest {
    /**
     * Alan Şablonu (FIELD_LAYOUT) kimliği
     */
    readonly id: string

    /**
     * Tek PDF belge
     */
    readonly files: Array<File>

    /**
     * &#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;). 
     */
    readonly onAnchorMiss?: ApiV1FieldTemplatesIdPreviewLayoutPostOnAnchorMissEnum
}

/**
 * DemandsApi - object-oriented interface
 */
export class DemandsApi extends BaseAPI {
    /**
     * Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \"bulk send\" modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Davranış:** - `rows` en fazla 10 (aşarsa 400 `BULK_MAX_10`). Daha büyük listeler   istemci tarafında 10\'arlı parçalara bölünür. - Kredi: sabit \"1 satır = 1 kredi\" değildir, her satırın maliyeti   `POST /demands` ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; `eidas_timestamp` seçilirse   satır başına +1 kredi eklenir). PAdES seviye eki yalnız QES\'te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   `ocr_id` ve `liveness` +1 kredi, diğerleri 0\'dır. `options`   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır `failed` (`error: \"INSUFFICIENT_CREDITS\"`)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine `failed` döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (`demand_id` response\'ta bulunur, davet   gönderilmemiştir). - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace\'in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar `failed` olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - `X-Workspace-Id` header\'ı ile organizasyon workspace\'i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 
     * @summary Toplu sözleşme oluştur (tek şablondan N alıcı)
     * @param {DemandsApiApiV1DemandsBulkPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsBulkPost(requestParameters: DemandsApiApiV1DemandsBulkPostRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsBulkPost(requestParameters.apiV1DemandsBulkPostRequest, requestParameters.xWorkspaceId, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, `DRAFT` ise sözleşmeyi `PENDING`\'e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  🔴 **Bu uçta yukarıdaki `/documents*` ailesinin bayrak kapısı (`ENVELOPE_DECISION_ENFORCE`) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — `dispatch_notifications: false` ile oluşturulur, ardından `POST .../documents` / `POST .../documents/upload` ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; `send_invitations` gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir.  **Kredi:** tek tahsilat noktası burasıdır (`reconcileDemandSigningCost`). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (`dispatched: false` döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. `v1ResendRateLimiter`, `/parties/{partyId}/resend` ile AYNI dakika-bazlı freni paylaşır). 
     * @summary Zarfı imzaya gönder (yayınla + davet)
     * @param {DemandsApiApiV1DemandsDemandIdDispatchPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsDemandIdDispatchPost(requestParameters: DemandsApiApiV1DemandsDemandIdDispatchPostRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsDemandIdDispatchPost(requestParameters.demandId, requestParameters.apiV1DemandsDemandIdDispatchPostRequest, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Belgeye atanmış imzacı kümesini **tam olarak** `party_ids` ile değiştirir (eski atamalar silinir, yenileri yazılır). `party_ids` boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 `INVALID_PARTY_ID` — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 `ASSIGNMENT_HAS_DECISION`). 
     * @summary Belgeye imzacı ata (tam-küme replace)
     * @param {DemandsApiApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut(requestParameters: DemandsApiApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut(requestParameters.demandId, requestParameters.docId, requestParameters.apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N\'e yeniden sıralanır. **Zarftaki son belge silinemez** (400 `CANNOT_DELETE_LAST_DOCUMENT`) — zarf hiç boş kalamaz. 
     * @summary Belgeyi zarftan sil
     * @param {DemandsApiApiV1DemandsDemandIdDocumentsDocIdDeleteRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsDemandIdDocumentsDocIdDelete(requestParameters: DemandsApiApiV1DemandsDemandIdDocumentsDocIdDeleteRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsDemandIdDocumentsDocIdDelete(requestParameters.demandId, requestParameters.docId, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (`PUT .../assignments`). 
     * @summary Belge metadata güncelle
     * @param {DemandsApiApiV1DemandsDemandIdDocumentsDocIdPatchRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsDemandIdDocumentsDocIdPatch(requestParameters: DemandsApiApiV1DemandsDemandIdDocumentsDocIdPatchRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsDemandIdDocumentsDocIdPatch(requestParameters.demandId, requestParameters.docId, requestParameters.apiV1DemandsDemandIdDocumentsDocIdPatchRequest, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm `/documents*` + `/documents/{docId}*` + `/documents/{docId}/assignments` uçları `ENVELOPE_DECISION_ENFORCE` bayrağı **arkasındadır**; bayrak kapalıyken **409 `ENVELOPE_MULTI_DOC_DISABLED`** döner. Bu, dashboard\'un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca `POST /demands/{demandId}/dispatch` çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. `?view=wizard` verilirse tam şekil döner (`EnvelopeDocument` şemasındaki alan notlarına bakın). 
     * @summary Zarf belge listesi
     * @param {DemandsApiApiV1DemandsDemandIdDocumentsGetRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsDemandIdDocumentsGet(requestParameters: DemandsApiApiV1DemandsDemandIdDocumentsGetRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsDemandIdDocumentsGet(requestParameters.demandId, requestParameters.view, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. `document_ids` zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 `ORDER_SET_MISMATCH`** döner. 
     * @summary Zarftaki belgelerin sırasını değiştir
     * @param {DemandsApiApiV1DemandsDemandIdDocumentsOrderPutRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsDemandIdDocumentsOrderPut(requestParameters: DemandsApiApiV1DemandsDemandIdDocumentsOrderPutRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsDemandIdDocumentsOrderPut(requestParameters.demandId, requestParameters.apiV1DemandsDemandIdDocumentsOrderPutRequest, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: `POST /demands/{demandId}/documents/upload`.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`\'te yapılır. 
     * @summary Zarfa metadata-only belge ekle
     * @param {DemandsApiApiV1DemandsDemandIdDocumentsPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsDemandIdDocumentsPost(requestParameters: DemandsApiApiV1DemandsDemandIdDocumentsPostRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsDemandIdDocumentsPost(requestParameters.demandId, requestParameters.apiV1DemandsDemandIdDocumentsPostRequest, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  `idempotency_key` **zorunludur** (gövde alanı; `Idempotency-Key` HEADER\'ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı `(demandId, idempotency_key)` çifti ile tekrar çağrı **409 `IDEMPOTENT_REPLAY`** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO\'su döner.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`\'te yapılır. 
     * @summary Zarfa dosya yükle (belge başına tek dosya)
     * @param {DemandsApiApiV1DemandsDemandIdDocumentsUploadPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsDemandIdDocumentsUploadPost(requestParameters: DemandsApiApiV1DemandsDemandIdDocumentsUploadPostRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsDemandIdDocumentsUploadPost(requestParameters.demandId, requestParameters.file, requestParameters.idempotencyKey, requestParameters.title, requestParameters.docKind, requestParameters.isRequired, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (`parties_total`, `parties_signed`). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için `GET /demands/{id}`. 
     * @summary Sözleşme listesi (counts-only, PII\'siz)
     * @param {DemandsApiApiV1DemandsGetRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsGet(requestParameters: DemandsApiApiV1DemandsGetRequest = {}, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsGet(requestParameters.status, requestParameters.q, requestParameters.from, requestParameters.to, requestParameters.templateId, requestParameters.page, requestParameters.limit, requestParameters.sort, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Çok-belgeli zarfta TEK bir belgenin imzalı PDF\'ini indirir. Zarf-geneli `/demands/{id}/pdf` ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND\'lenir (başka zarfın belgesi istenirse 404). 
     * @summary Belge-özgü imzalı PDF (çok-belgeli zarf)
     * @param {DemandsApiApiV1DemandsIdBelgeDocumentIdPdfGetRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsIdBelgeDocumentIdPdfGet(requestParameters: DemandsApiApiV1DemandsIdBelgeDocumentIdPdfGetRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsIdBelgeDocumentIdPdfGet(requestParameters.id, requestParameters.documentId, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Bekleyen bir sözleşmeyi iptal eder (status=CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 
     * @summary Sözleşme iptal (void)
     * @param {DemandsApiApiV1DemandsIdCancelPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsIdCancelPost(requestParameters: DemandsApiApiV1DemandsIdCancelPostRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsIdCancelPost(requestParameters.id, requestParameters.apiV1DemandsIdCancelPostRequest, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 
     * @summary Tamamlanma sertifikası (PAdES B-T)
     * @param {DemandsApiApiV1DemandsIdCertificateGetRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsIdCertificateGet(requestParameters: DemandsApiApiV1DemandsIdCertificateGetRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsIdCertificateGet(requestParameters.id, requestParameters.lang, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API\'den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 `DEMAND_COMPLETED`. 
     * @summary Sözleşme sil (yalnızca tamamlanmamış)
     * @param {DemandsApiApiV1DemandsIdDeleteRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsIdDelete(requestParameters: DemandsApiApiV1DemandsIdDeleteRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsIdDelete(requestParameters.id, options).then((request) => request(this.axios, this.basePath));
    }

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
     * Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 
     * @summary Tekil tarafa imza davetini tekrar gönder
     * @param {DemandsApiApiV1DemandsIdPartiesPartyIdResendPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsIdPartiesPartyIdResendPost(requestParameters: DemandsApiApiV1DemandsIdPartiesPartyIdResendPostRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsIdPartiesPartyIdResendPost(requestParameters.id, requestParameters.partyId, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Tamamlanmış sözleşmenin imzalı PDF\'ini indirir. Public `/sonuc/{id}/pdf`\'in aksine API key ownership\'i zorunludur. 
     * @summary İmzalı sözleşme PDF\'i (auth\'lu indirme)
     * @param {DemandsApiApiV1DemandsIdPdfGetRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsIdPdfGet(requestParameters: DemandsApiApiV1DemandsIdPdfGetRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsIdPdfGet(requestParameters.id, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP `ip_masked` (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 
     * @summary İmza denetim izi (maskeli)
     * @param {DemandsApiApiV1DemandsIdTimelineGetRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsIdTimelineGet(requestParameters: DemandsApiApiV1DemandsIdTimelineGetRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsIdTimelineGet(requestParameters.id, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field\'ları `variables` payload\'undan doldurur ve imzalama URL\'lerini döner.  **Variable resolution:** - Item\'ın `template_party_id` non-null → `party_mapping[i].variables`\'ta   o slug var ise oradan uygulanır - Yoksa root `variables`\'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value\'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı 
     * @summary Sözleşme oluştur (şablondan)
     * @param {DemandsApiApiV1DemandsPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsPost(requestParameters: DemandsApiApiV1DemandsPostRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsPost(requestParameters.createDemandRequest, requestParameters.idempotencyKey, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF\'e birleştirilir, office formatları LibreOffice ile PDF\'e çevrilir. 
     * @summary Dosya upload ile sözleşme oluştur (şablonsuz)
     * @param {DemandsApiApiV1DemandsUploadPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1DemandsUploadPost(requestParameters: DemandsApiApiV1DemandsUploadPostRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1DemandsUploadPost(requestParameters.files, requestParameters.parties, requestParameters.idempotencyKey, requestParameters.order, requestParameters.title, requestParameters.description, requestParameters.fieldTemplateId, requestParameters.force, requestParameters.sendInvitations, requestParameters.onAnchorMiss, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Bir Alan Şablonunun (`kind: FIELD_LAYOUT`) yüklediğiniz PDF\'e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  `POST /api/v1/demands/upload` + `field_template_id` çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API\'de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (`data.resolvable: false`) — kuru koşumun cevabı \"uygulanamaz\"dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 `RATE_LIMITED`). 
     * @summary Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
     * @param {DemandsApiApiV1FieldTemplatesIdPreviewLayoutPostRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1FieldTemplatesIdPreviewLayoutPost(requestParameters: DemandsApiApiV1FieldTemplatesIdPreviewLayoutPostRequest, options?: RawAxiosRequestConfig) {
        return DemandsApiFp(this.configuration).apiV1FieldTemplatesIdPreviewLayoutPost(requestParameters.id, requestParameters.files, requestParameters.onAnchorMiss, options).then((request) => request(this.axios, this.basePath));
    }
}

export const ApiV1DemandsDemandIdDocumentsGetViewEnum = {
    Wizard: 'wizard',
} as const;
export type ApiV1DemandsDemandIdDocumentsGetViewEnum = typeof ApiV1DemandsDemandIdDocumentsGetViewEnum[keyof typeof ApiV1DemandsDemandIdDocumentsGetViewEnum];
export const ApiV1DemandsDemandIdDocumentsUploadPostDocKindEnum = {
    Contract: 'CONTRACT',
    KvkkNotice: 'KVKK_NOTICE',
    KvkkConsent: 'KVKK_CONSENT',
    Preinfo: 'PREINFO',
    PriceList: 'PRICE_LIST',
    Other: 'OTHER',
} as const;
export type ApiV1DemandsDemandIdDocumentsUploadPostDocKindEnum = typeof ApiV1DemandsDemandIdDocumentsUploadPostDocKindEnum[keyof typeof ApiV1DemandsDemandIdDocumentsUploadPostDocKindEnum];
export const ApiV1DemandsDemandIdDocumentsUploadPostIsRequiredEnum = {
    True: 'true',
    False: 'false',
} as const;
export type ApiV1DemandsDemandIdDocumentsUploadPostIsRequiredEnum = typeof ApiV1DemandsDemandIdDocumentsUploadPostIsRequiredEnum[keyof typeof ApiV1DemandsDemandIdDocumentsUploadPostIsRequiredEnum];
export const ApiV1DemandsGetStatusEnum = {
    Draft: 'DRAFT',
    Pending: 'PENDING',
    Completed: 'COMPLETED',
    Cancelled: 'CANCELLED',
    Expired: 'EXPIRED',
} as const;
export type ApiV1DemandsGetStatusEnum = typeof ApiV1DemandsGetStatusEnum[keyof typeof ApiV1DemandsGetStatusEnum];
export const ApiV1DemandsUploadPostForceEnum = {
    True: 'true',
    _1: '1',
} as const;
export type ApiV1DemandsUploadPostForceEnum = typeof ApiV1DemandsUploadPostForceEnum[keyof typeof ApiV1DemandsUploadPostForceEnum];
export const ApiV1DemandsUploadPostSendInvitationsEnum = {
    True: 'true',
    _1: '1',
    All: 'all',
    Email: 'email',
    Sms: 'sms',
    False: 'false',
    _0: '0',
    Off: 'off',
    No: 'no',
    Hayir: 'hayir',
    Hayr: 'hayır',
} as const;
export type ApiV1DemandsUploadPostSendInvitationsEnum = typeof ApiV1DemandsUploadPostSendInvitationsEnum[keyof typeof ApiV1DemandsUploadPostSendInvitationsEnum];
export const ApiV1DemandsUploadPostOnAnchorMissEnum = {
    Block: 'block',
    Drop: 'drop',
} as const;
export type ApiV1DemandsUploadPostOnAnchorMissEnum = typeof ApiV1DemandsUploadPostOnAnchorMissEnum[keyof typeof ApiV1DemandsUploadPostOnAnchorMissEnum];
export const ApiV1FieldTemplatesIdPreviewLayoutPostOnAnchorMissEnum = {
    Block: 'block',
    Drop: 'drop',
} as const;
export type ApiV1FieldTemplatesIdPreviewLayoutPostOnAnchorMissEnum = typeof ApiV1FieldTemplatesIdPreviewLayoutPostOnAnchorMissEnum[keyof typeof ApiV1FieldTemplatesIdPreviewLayoutPostOnAnchorMissEnum];


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
 * ReportsApi - axios parameter creator
 */
export const ReportsApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * API key sahibinin workspace\'indeki sözleşmelerin agrege durum sayılarını döner (bekleyen / tamamlanan / iptal / süresi-dolmuş + bu ay oluşturulan). Yalnızca sayı; PII içermez. Kişisel workspace yalnızca sahibinin, org workspace org\'un sözleşmelerini sayar (IDOR-safe, GET /demands ile aynı SSOT). 
         * @summary Rapor / özet (agrege sözleşme durum sayıları)
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1ReportsGet: async (options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/api/v1/reports`;
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
 * ReportsApi - functional programming interface
 */
export const ReportsApiFp = function(configuration?: Configuration) {
    const localVarAxiosParamCreator = ReportsApiAxiosParamCreator(configuration)
    return {
        /**
         * API key sahibinin workspace\'indeki sözleşmelerin agrege durum sayılarını döner (bekleyen / tamamlanan / iptal / süresi-dolmuş + bu ay oluşturulan). Yalnızca sayı; PII içermez. Kişisel workspace yalnızca sahibinin, org workspace org\'un sözleşmelerini sayar (IDOR-safe, GET /demands ile aynı SSOT). 
         * @summary Rapor / özet (agrege sözleşme durum sayıları)
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1ReportsGet(options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1ReportsGet200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1ReportsGet(options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['ReportsApi.apiV1ReportsGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
    }
};

/**
 * ReportsApi - factory interface
 */
export const ReportsApiFactory = function (configuration?: Configuration, basePath?: string, axios?: AxiosInstance) {
    const localVarFp = ReportsApiFp(configuration)
    return {
        /**
         * API key sahibinin workspace\'indeki sözleşmelerin agrege durum sayılarını döner (bekleyen / tamamlanan / iptal / süresi-dolmuş + bu ay oluşturulan). Yalnızca sayı; PII içermez. Kişisel workspace yalnızca sahibinin, org workspace org\'un sözleşmelerini sayar (IDOR-safe, GET /demands ile aynı SSOT). 
         * @summary Rapor / özet (agrege sözleşme durum sayıları)
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1ReportsGet(options?: RawAxiosRequestConfig): AxiosPromise<ApiV1ReportsGet200Response> {
            return localVarFp.apiV1ReportsGet(options).then((request) => request(axios, basePath));
        },
    };
};

/**
 * ReportsApi - object-oriented interface
 */
export class ReportsApi extends BaseAPI {
    /**
     * API key sahibinin workspace\'indeki sözleşmelerin agrege durum sayılarını döner (bekleyen / tamamlanan / iptal / süresi-dolmuş + bu ay oluşturulan). Yalnızca sayı; PII içermez. Kişisel workspace yalnızca sahibinin, org workspace org\'un sözleşmelerini sayar (IDOR-safe, GET /demands ile aynı SSOT). 
     * @summary Rapor / özet (agrege sözleşme durum sayıları)
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1ReportsGet(options?: RawAxiosRequestConfig) {
        return ReportsApiFp(this.configuration).apiV1ReportsGet(options).then((request) => request(this.axios, this.basePath));
    }
}



/**
 * TemplatesApi - axios parameter creator
 */
export const TemplatesApiAxiosParamCreator = function (configuration?: Configuration) {
    return {
        /**
         * Hesabınızdaki (ya da seçili çalışma alanındaki) **Alan Şablonlarını** (`kind: FIELD_LAYOUT`) döner.  ### Neden ayrı bir uç  `GET /api/v1/templates` Alan Şablonlarını bilerek listelemez ve `GET /api/v1/templates/{id}` onlara `404` döner. Alan Şablonunun referans belgesi hiçbir imzacıya gönderilmez; belgeli şablon sanılması, o referans belgenin karşı tarafa gitmesi demek olurdu. Bu yüzden varsayılan davranış hariç tutmaktır ve Alan Şablonlarını görmek açık bir talep gerektirir.  Döndürülen `id`, `POST /api/v1/demands/upload` çağrısındaki `field_template_id` ve `POST /api/v1/field-templates/{id}/preview-layout` için kullanılır.  `parties` dizisi taraf sırasına göre döner; upload gövdesindeki `parties` dizisini bu sırayla kurun. 
         * @summary Alan Şablonlarını listeler
         * @param {number} [page] 
         * @param {number} [limit] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1FieldTemplatesGet: async (page?: number, limit?: number, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/api/v1/field-templates`;
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
         * Tek bir Alan Şablonunun rollerini ve rol başına alan sayısını döner.  🔴 **Alanların koordinatları dönmez.** Yerleşimin belgeye nasıl uygulanacağını görmek için kuru koşum ucunu kullanın: `POST /api/v1/field-templates/{id}/preview-layout`.  Belgeli bir şablonun kimliği verilirse `404` döner; şablonun türü ifşa edilmez. Var olmayan ve size ait olmayan kimlik de aynı yanıtı alır. 
         * @summary Alan Şablonu ayrıntısı (roller + alan sayıları)
         * @param {string} id Alan Şablonu kimliği
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1FieldTemplatesIdGet: async (id: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1FieldTemplatesIdGet', 'id', id)
            const localVarPath = `/api/v1/field-templates/{id}`
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
         * Şablonu siler. Silinen şablon 30 gün saklanır, sürenin sonunda kalıcı olarak silinir. Mevcut sözleşmeler etkilenmez.  Şablonun aktif (DRAFT veya PENDING) sözleşmesi varsa silinemez; 409 `TEMPLATE_IN_USE` döner. 
         * @summary Şablon sil
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TemplatesIdDelete: async (id: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1TemplatesIdDelete', 'id', id)
            const localVarPath = `/api/v1/templates/{id}`
                .replace('{id}', encodeURIComponent(String(id)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'DELETE', ...baseOptions, ...options};
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
         * Şablonun yalnızca metadata alanlarını (name / description / category) günceller. Sayfa/alan/taraf yapısı bu endpoint\'ten DEĞİŞTİRİLEMEZ (şablon içeriği panelden düzenlenir). 
         * @summary Şablon metadata güncelle
         * @param {string} id 
         * @param {ApiV1TemplatesIdPatchRequest} apiV1TemplatesIdPatchRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TemplatesIdPatch: async (id: string, apiV1TemplatesIdPatchRequest: ApiV1TemplatesIdPatchRequest, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1TemplatesIdPatch', 'id', id)
            // verify required parameter 'apiV1TemplatesIdPatchRequest' is not null or undefined
            assertParamExists('apiV1TemplatesIdPatch', 'apiV1TemplatesIdPatchRequest', apiV1TemplatesIdPatchRequest)
            const localVarPath = `/api/v1/templates/{id}`
                .replace('{id}', encodeURIComponent(String(id)));
            // use dummy base URL string because the URL constructor only accepts absolute URLs.
            const localVarUrlObj = new URL(localVarPath, DUMMY_BASE_URL);
            let baseOptions;
            if (configuration) {
                baseOptions = configuration.baseOptions;
            }

            const localVarRequestOptions = { method: 'PATCH', ...baseOptions, ...options};
            const localVarHeaderParameter = {} as any;
            const localVarQueryParameter = {} as any;

            // authentication ApiKeyAuth required
            await setApiKeyToObject(localVarHeaderParameter, "X-API-Key", configuration)

            localVarHeaderParameter['Content-Type'] = 'application/json';
            localVarHeaderParameter['Accept'] = 'application/json';

            setSearchParams(localVarUrlObj, localVarQueryParameter);
            let headersFromBaseOptions = baseOptions && baseOptions.headers ? baseOptions.headers : {};
            localVarRequestOptions.headers = {...localVarHeaderParameter, ...headersFromBaseOptions, ...options.headers};
            localVarRequestOptions.data = serializeDataIfNeeded(apiV1TemplatesIdPatchRequest, localVarRequestOptions, configuration)

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
         * Hesabınızdaki (ya da seçili çalışma alanındaki) **Alan Şablonlarını** (`kind: FIELD_LAYOUT`) döner.  ### Neden ayrı bir uç  `GET /api/v1/templates` Alan Şablonlarını bilerek listelemez ve `GET /api/v1/templates/{id}` onlara `404` döner. Alan Şablonunun referans belgesi hiçbir imzacıya gönderilmez; belgeli şablon sanılması, o referans belgenin karşı tarafa gitmesi demek olurdu. Bu yüzden varsayılan davranış hariç tutmaktır ve Alan Şablonlarını görmek açık bir talep gerektirir.  Döndürülen `id`, `POST /api/v1/demands/upload` çağrısındaki `field_template_id` ve `POST /api/v1/field-templates/{id}/preview-layout` için kullanılır.  `parties` dizisi taraf sırasına göre döner; upload gövdesindeki `parties` dizisini bu sırayla kurun. 
         * @summary Alan Şablonlarını listeler
         * @param {number} [page] 
         * @param {number} [limit] 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1FieldTemplatesGet(page?: number, limit?: number, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1FieldTemplatesGet200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1FieldTemplatesGet(page, limit, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['TemplatesApi.apiV1FieldTemplatesGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Tek bir Alan Şablonunun rollerini ve rol başına alan sayısını döner.  🔴 **Alanların koordinatları dönmez.** Yerleşimin belgeye nasıl uygulanacağını görmek için kuru koşum ucunu kullanın: `POST /api/v1/field-templates/{id}/preview-layout`.  Belgeli bir şablonun kimliği verilirse `404` döner; şablonun türü ifşa edilmez. Var olmayan ve size ait olmayan kimlik de aynı yanıtı alır. 
         * @summary Alan Şablonu ayrıntısı (roller + alan sayıları)
         * @param {string} id Alan Şablonu kimliği
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1FieldTemplatesIdGet(id: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1FieldTemplatesIdGet200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1FieldTemplatesIdGet(id, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['TemplatesApi.apiV1FieldTemplatesIdGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
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
         * Şablonu siler. Silinen şablon 30 gün saklanır, sürenin sonunda kalıcı olarak silinir. Mevcut sözleşmeler etkilenmez.  Şablonun aktif (DRAFT veya PENDING) sözleşmesi varsa silinemez; 409 `TEMPLATE_IN_USE` döner. 
         * @summary Şablon sil
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1TemplatesIdDelete(id: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1TemplatesIdDelete200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1TemplatesIdDelete(id, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['TemplatesApi.apiV1TemplatesIdDelete']?.[localVarOperationServerIndex]?.url;
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
         * Şablonun yalnızca metadata alanlarını (name / description / category) günceller. Sayfa/alan/taraf yapısı bu endpoint\'ten DEĞİŞTİRİLEMEZ (şablon içeriği panelden düzenlenir). 
         * @summary Şablon metadata güncelle
         * @param {string} id 
         * @param {ApiV1TemplatesIdPatchRequest} apiV1TemplatesIdPatchRequest 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1TemplatesIdPatch(id: string, apiV1TemplatesIdPatchRequest: ApiV1TemplatesIdPatchRequest, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1TemplatesIdPatch200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1TemplatesIdPatch(id, apiV1TemplatesIdPatchRequest, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['TemplatesApi.apiV1TemplatesIdPatch']?.[localVarOperationServerIndex]?.url;
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
         * Hesabınızdaki (ya da seçili çalışma alanındaki) **Alan Şablonlarını** (`kind: FIELD_LAYOUT`) döner.  ### Neden ayrı bir uç  `GET /api/v1/templates` Alan Şablonlarını bilerek listelemez ve `GET /api/v1/templates/{id}` onlara `404` döner. Alan Şablonunun referans belgesi hiçbir imzacıya gönderilmez; belgeli şablon sanılması, o referans belgenin karşı tarafa gitmesi demek olurdu. Bu yüzden varsayılan davranış hariç tutmaktır ve Alan Şablonlarını görmek açık bir talep gerektirir.  Döndürülen `id`, `POST /api/v1/demands/upload` çağrısındaki `field_template_id` ve `POST /api/v1/field-templates/{id}/preview-layout` için kullanılır.  `parties` dizisi taraf sırasına göre döner; upload gövdesindeki `parties` dizisini bu sırayla kurun. 
         * @summary Alan Şablonlarını listeler
         * @param {TemplatesApiApiV1FieldTemplatesGetRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1FieldTemplatesGet(requestParameters: TemplatesApiApiV1FieldTemplatesGetRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1FieldTemplatesGet200Response> {
            return localVarFp.apiV1FieldTemplatesGet(requestParameters.page, requestParameters.limit, options).then((request) => request(axios, basePath));
        },
        /**
         * Tek bir Alan Şablonunun rollerini ve rol başına alan sayısını döner.  🔴 **Alanların koordinatları dönmez.** Yerleşimin belgeye nasıl uygulanacağını görmek için kuru koşum ucunu kullanın: `POST /api/v1/field-templates/{id}/preview-layout`.  Belgeli bir şablonun kimliği verilirse `404` döner; şablonun türü ifşa edilmez. Var olmayan ve size ait olmayan kimlik de aynı yanıtı alır. 
         * @summary Alan Şablonu ayrıntısı (roller + alan sayıları)
         * @param {TemplatesApiApiV1FieldTemplatesIdGetRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1FieldTemplatesIdGet(requestParameters: TemplatesApiApiV1FieldTemplatesIdGetRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1FieldTemplatesIdGet200Response> {
            return localVarFp.apiV1FieldTemplatesIdGet(requestParameters.id, options).then((request) => request(axios, basePath));
        },
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
         * Şablonu siler. Silinen şablon 30 gün saklanır, sürenin sonunda kalıcı olarak silinir. Mevcut sözleşmeler etkilenmez.  Şablonun aktif (DRAFT veya PENDING) sözleşmesi varsa silinemez; 409 `TEMPLATE_IN_USE` döner. 
         * @summary Şablon sil
         * @param {TemplatesApiApiV1TemplatesIdDeleteRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TemplatesIdDelete(requestParameters: TemplatesApiApiV1TemplatesIdDeleteRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1TemplatesIdDelete200Response> {
            return localVarFp.apiV1TemplatesIdDelete(requestParameters.id, options).then((request) => request(axios, basePath));
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
         * Şablonun yalnızca metadata alanlarını (name / description / category) günceller. Sayfa/alan/taraf yapısı bu endpoint\'ten DEĞİŞTİRİLEMEZ (şablon içeriği panelden düzenlenir). 
         * @summary Şablon metadata güncelle
         * @param {TemplatesApiApiV1TemplatesIdPatchRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TemplatesIdPatch(requestParameters: TemplatesApiApiV1TemplatesIdPatchRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1TemplatesIdPatch200Response> {
            return localVarFp.apiV1TemplatesIdPatch(requestParameters.id, requestParameters.apiV1TemplatesIdPatchRequest, options).then((request) => request(axios, basePath));
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
 * Request parameters for apiV1FieldTemplatesGet operation in TemplatesApi.
 */
export interface TemplatesApiApiV1FieldTemplatesGetRequest {
    readonly page?: number

    readonly limit?: number
}

/**
 * Request parameters for apiV1FieldTemplatesIdGet operation in TemplatesApi.
 */
export interface TemplatesApiApiV1FieldTemplatesIdGetRequest {
    /**
     * Alan Şablonu kimliği
     */
    readonly id: string
}

/**
 * Request parameters for apiV1TemplatesGet operation in TemplatesApi.
 */
export interface TemplatesApiApiV1TemplatesGetRequest {
    readonly page?: number

    readonly limit?: number
}

/**
 * Request parameters for apiV1TemplatesIdDelete operation in TemplatesApi.
 */
export interface TemplatesApiApiV1TemplatesIdDeleteRequest {
    readonly id: string
}

/**
 * Request parameters for apiV1TemplatesIdGet operation in TemplatesApi.
 */
export interface TemplatesApiApiV1TemplatesIdGetRequest {
    readonly id: string
}

/**
 * Request parameters for apiV1TemplatesIdPatch operation in TemplatesApi.
 */
export interface TemplatesApiApiV1TemplatesIdPatchRequest {
    readonly id: string

    readonly apiV1TemplatesIdPatchRequest: ApiV1TemplatesIdPatchRequest
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
     * Hesabınızdaki (ya da seçili çalışma alanındaki) **Alan Şablonlarını** (`kind: FIELD_LAYOUT`) döner.  ### Neden ayrı bir uç  `GET /api/v1/templates` Alan Şablonlarını bilerek listelemez ve `GET /api/v1/templates/{id}` onlara `404` döner. Alan Şablonunun referans belgesi hiçbir imzacıya gönderilmez; belgeli şablon sanılması, o referans belgenin karşı tarafa gitmesi demek olurdu. Bu yüzden varsayılan davranış hariç tutmaktır ve Alan Şablonlarını görmek açık bir talep gerektirir.  Döndürülen `id`, `POST /api/v1/demands/upload` çağrısındaki `field_template_id` ve `POST /api/v1/field-templates/{id}/preview-layout` için kullanılır.  `parties` dizisi taraf sırasına göre döner; upload gövdesindeki `parties` dizisini bu sırayla kurun. 
     * @summary Alan Şablonlarını listeler
     * @param {TemplatesApiApiV1FieldTemplatesGetRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1FieldTemplatesGet(requestParameters: TemplatesApiApiV1FieldTemplatesGetRequest = {}, options?: RawAxiosRequestConfig) {
        return TemplatesApiFp(this.configuration).apiV1FieldTemplatesGet(requestParameters.page, requestParameters.limit, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Tek bir Alan Şablonunun rollerini ve rol başına alan sayısını döner.  🔴 **Alanların koordinatları dönmez.** Yerleşimin belgeye nasıl uygulanacağını görmek için kuru koşum ucunu kullanın: `POST /api/v1/field-templates/{id}/preview-layout`.  Belgeli bir şablonun kimliği verilirse `404` döner; şablonun türü ifşa edilmez. Var olmayan ve size ait olmayan kimlik de aynı yanıtı alır. 
     * @summary Alan Şablonu ayrıntısı (roller + alan sayıları)
     * @param {TemplatesApiApiV1FieldTemplatesIdGetRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1FieldTemplatesIdGet(requestParameters: TemplatesApiApiV1FieldTemplatesIdGetRequest, options?: RawAxiosRequestConfig) {
        return TemplatesApiFp(this.configuration).apiV1FieldTemplatesIdGet(requestParameters.id, options).then((request) => request(this.axios, this.basePath));
    }

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
     * Şablonu siler. Silinen şablon 30 gün saklanır, sürenin sonunda kalıcı olarak silinir. Mevcut sözleşmeler etkilenmez.  Şablonun aktif (DRAFT veya PENDING) sözleşmesi varsa silinemez; 409 `TEMPLATE_IN_USE` döner. 
     * @summary Şablon sil
     * @param {TemplatesApiApiV1TemplatesIdDeleteRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1TemplatesIdDelete(requestParameters: TemplatesApiApiV1TemplatesIdDeleteRequest, options?: RawAxiosRequestConfig) {
        return TemplatesApiFp(this.configuration).apiV1TemplatesIdDelete(requestParameters.id, options).then((request) => request(this.axios, this.basePath));
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
     * Şablonun yalnızca metadata alanlarını (name / description / category) günceller. Sayfa/alan/taraf yapısı bu endpoint\'ten DEĞİŞTİRİLEMEZ (şablon içeriği panelden düzenlenir). 
     * @summary Şablon metadata güncelle
     * @param {TemplatesApiApiV1TemplatesIdPatchRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1TemplatesIdPatch(requestParameters: TemplatesApiApiV1TemplatesIdPatchRequest, options?: RawAxiosRequestConfig) {
        return TemplatesApiFp(this.configuration).apiV1TemplatesIdPatch(requestParameters.id, requestParameters.apiV1TemplatesIdPatchRequest, options).then((request) => request(this.axios, this.basePath));
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
         * Hesabın (workspace) manuel oluşturduğu zaman damgalarını listeler. Sözleşme tamamlanınca otomatik üretilen damgalar bu listede GÖSTERİLMEZ (onlar ilgili sözleşme sayfasından indirilir). Yalnızca API key\'in kendi workspace\'indeki kayıtlar döner (IDOR-safe).  `timestamp_file_url` doğrudan tarayıcıdan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı / şifreli olabilir). 
         * @summary Zaman damgası listesi
         * @param {number} [page] 
         * @param {number} [limit] 
         * @param {string} [q] Dosya adı / açıklamada arama (max 100 karakter)
         * @param {string} [status] Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID
         * @param {string} [from] Damga günü alt sınırı (YYYY-MM-DD)
         * @param {string} [to] Damga günü üst sınırı (YYYY-MM-DD)
         * @param {string} [sort] Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (\&#39;-\&#39; öneki azalan)
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TimestampsGet: async (page?: number, limit?: number, q?: string, status?: string, from?: string, to?: string, sort?: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            const localVarPath = `/api/v1/timestamps`;
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

            if (q !== undefined) {
                localVarQueryParameter['q'] = q;
            }

            if (status !== undefined) {
                localVarQueryParameter['status'] = status;
            }

            if (from !== undefined) {
                localVarQueryParameter['from'] = (from as any instanceof Date) ?
                    (from as any).toISOString().substring(0,10) :
                    from;
            }

            if (to !== undefined) {
                localVarQueryParameter['to'] = (to as any instanceof Date) ?
                    (to as any).toISOString().substring(0,10) :
                    to;
            }

            if (sort !== undefined) {
                localVarQueryParameter['sort'] = sort;
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
         * Tek bir zaman damgası kaydının detayını döner. Yalnızca API key\'in kendi workspace\'indeki kayıt getirilebilir (IDOR-safe); başka bir workspace\'in kaydı 404 döner. 
         * @summary Zaman damgası detayı
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TimestampsIdGet: async (id: string, options: RawAxiosRequestConfig = {}): Promise<RequestArgs> => {
            // verify required parameter 'id' is not null or undefined
            assertParamExists('apiV1TimestampsIdGet', 'id', id)
            const localVarPath = `/api/v1/timestamps/{id}`
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
         * Hesabın (workspace) manuel oluşturduğu zaman damgalarını listeler. Sözleşme tamamlanınca otomatik üretilen damgalar bu listede GÖSTERİLMEZ (onlar ilgili sözleşme sayfasından indirilir). Yalnızca API key\'in kendi workspace\'indeki kayıtlar döner (IDOR-safe).  `timestamp_file_url` doğrudan tarayıcıdan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı / şifreli olabilir). 
         * @summary Zaman damgası listesi
         * @param {number} [page] 
         * @param {number} [limit] 
         * @param {string} [q] Dosya adı / açıklamada arama (max 100 karakter)
         * @param {string} [status] Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID
         * @param {string} [from] Damga günü alt sınırı (YYYY-MM-DD)
         * @param {string} [to] Damga günü üst sınırı (YYYY-MM-DD)
         * @param {string} [sort] Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (\&#39;-\&#39; öneki azalan)
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1TimestampsGet(page?: number, limit?: number, q?: string, status?: string, from?: string, to?: string, sort?: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1TimestampsGet200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1TimestampsGet(page, limit, q, status, from, to, sort, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['TimestampsApi.apiV1TimestampsGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
        /**
         * Tek bir zaman damgası kaydının detayını döner. Yalnızca API key\'in kendi workspace\'indeki kayıt getirilebilir (IDOR-safe); başka bir workspace\'in kaydı 404 döner. 
         * @summary Zaman damgası detayı
         * @param {string} id 
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        async apiV1TimestampsIdGet(id: string, options?: RawAxiosRequestConfig): Promise<(axios?: AxiosInstance, basePath?: string) => AxiosPromise<ApiV1TimestampsIdGet200Response>> {
            const localVarAxiosArgs = await localVarAxiosParamCreator.apiV1TimestampsIdGet(id, options);
            const localVarOperationServerIndex = configuration?.serverIndex ?? 0;
            const localVarOperationServerBasePath = operationServerMap['TimestampsApi.apiV1TimestampsIdGet']?.[localVarOperationServerIndex]?.url;
            return (axios, basePath) => createRequestFunction(localVarAxiosArgs, globalAxios, BASE_PATH, configuration)(axios, localVarOperationServerBasePath || basePath);
        },
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
         * Hesabın (workspace) manuel oluşturduğu zaman damgalarını listeler. Sözleşme tamamlanınca otomatik üretilen damgalar bu listede GÖSTERİLMEZ (onlar ilgili sözleşme sayfasından indirilir). Yalnızca API key\'in kendi workspace\'indeki kayıtlar döner (IDOR-safe).  `timestamp_file_url` doğrudan tarayıcıdan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı / şifreli olabilir). 
         * @summary Zaman damgası listesi
         * @param {TimestampsApiApiV1TimestampsGetRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TimestampsGet(requestParameters: TimestampsApiApiV1TimestampsGetRequest = {}, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1TimestampsGet200Response> {
            return localVarFp.apiV1TimestampsGet(requestParameters.page, requestParameters.limit, requestParameters.q, requestParameters.status, requestParameters.from, requestParameters.to, requestParameters.sort, options).then((request) => request(axios, basePath));
        },
        /**
         * Tek bir zaman damgası kaydının detayını döner. Yalnızca API key\'in kendi workspace\'indeki kayıt getirilebilir (IDOR-safe); başka bir workspace\'in kaydı 404 döner. 
         * @summary Zaman damgası detayı
         * @param {TimestampsApiApiV1TimestampsIdGetRequest} requestParameters Request parameters.
         * @param {*} [options] Override http request option.
         * @throws {RequiredError}
         */
        apiV1TimestampsIdGet(requestParameters: TimestampsApiApiV1TimestampsIdGetRequest, options?: RawAxiosRequestConfig): AxiosPromise<ApiV1TimestampsIdGet200Response> {
            return localVarFp.apiV1TimestampsIdGet(requestParameters.id, options).then((request) => request(axios, basePath));
        },
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
 * Request parameters for apiV1TimestampsGet operation in TimestampsApi.
 */
export interface TimestampsApiApiV1TimestampsGetRequest {
    readonly page?: number

    readonly limit?: number

    /**
     * Dosya adı / açıklamada arama (max 100 karakter)
     */
    readonly q?: string

    /**
     * Virgülle ayrılmış durum filtresi: ACTIVE,VERIFIED,EXPIRED,INVALID
     */
    readonly status?: string

    /**
     * Damga günü alt sınırı (YYYY-MM-DD)
     */
    readonly from?: string

    /**
     * Damga günü üst sınırı (YYYY-MM-DD)
     */
    readonly to?: string

    /**
     * Sıralama: createdAt | timestamp_date | original_file_name | original_file_size | status (\&#39;-\&#39; öneki azalan)
     */
    readonly sort?: string
}

/**
 * Request parameters for apiV1TimestampsIdGet operation in TimestampsApi.
 */
export interface TimestampsApiApiV1TimestampsIdGetRequest {
    readonly id: string
}

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
     * Hesabın (workspace) manuel oluşturduğu zaman damgalarını listeler. Sözleşme tamamlanınca otomatik üretilen damgalar bu listede GÖSTERİLMEZ (onlar ilgili sözleşme sayfasından indirilir). Yalnızca API key\'in kendi workspace\'indeki kayıtlar döner (IDOR-safe).  `timestamp_file_url` doğrudan tarayıcıdan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı / şifreli olabilir). 
     * @summary Zaman damgası listesi
     * @param {TimestampsApiApiV1TimestampsGetRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1TimestampsGet(requestParameters: TimestampsApiApiV1TimestampsGetRequest = {}, options?: RawAxiosRequestConfig) {
        return TimestampsApiFp(this.configuration).apiV1TimestampsGet(requestParameters.page, requestParameters.limit, requestParameters.q, requestParameters.status, requestParameters.from, requestParameters.to, requestParameters.sort, options).then((request) => request(this.axios, this.basePath));
    }

    /**
     * Tek bir zaman damgası kaydının detayını döner. Yalnızca API key\'in kendi workspace\'indeki kayıt getirilebilir (IDOR-safe); başka bir workspace\'in kaydı 404 döner. 
     * @summary Zaman damgası detayı
     * @param {TimestampsApiApiV1TimestampsIdGetRequest} requestParameters Request parameters.
     * @param {*} [options] Override http request option.
     * @throws {RequiredError}
     */
    public apiV1TimestampsIdGet(requestParameters: TimestampsApiApiV1TimestampsIdGetRequest, options?: RawAxiosRequestConfig) {
        return TimestampsApiFp(this.configuration).apiV1TimestampsIdGet(requestParameters.id, options).then((request) => request(this.axios, this.basePath));
    }

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



