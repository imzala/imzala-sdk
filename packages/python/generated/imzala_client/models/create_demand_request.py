# coding: utf-8

"""
    imzala External API

    imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.8.22 · **Son güncelleme:** 2026-09-23  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API anahtarı kendi organizasyonuna bağlıdır: `X-Workspace-Id` başlığı gönderilmezse anahtarın organizasyonu otomatik uygulanır; gönderilirse anahtarın organizasyonuyla aynı olmalıdır (aksi halde 403 `WORKSPACE_MISMATCH`). Kişisel anahtarlar için bu başlık gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |----|-------|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (8) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı |  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type` — yukarıdaki 8 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 

    The version of the OpenAPI document: 1.8.22
    Contact: destek@imzala.org
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


from __future__ import annotations
import pprint
import re  # noqa: F401
import json

from datetime import datetime
from pydantic import BaseModel, ConfigDict, Field, StrictBool, StrictStr, field_validator
from typing import Any, ClassVar, Dict, List, Optional
from typing_extensions import Annotated
from uuid import UUID
from imzala_client.models.document_selection_input import DocumentSelectionInput
from imzala_client.models.party_mapping_input import PartyMappingInput
from imzala_client.models.party_mapping_input_variables_value import PartyMappingInputVariablesValue
from imzala_client.models.reminder_settings import ReminderSettings
from typing import Optional, Set
from typing_extensions import Self
from pydantic_core import to_jsonable_python

class CreateDemandRequest(BaseModel):
    """
    CreateDemandRequest
    """ # noqa: E501
    template_id: UUID = Field(description="GET /api/v1/templates listesinden veya dashboard'dan kopyalayın")
    title: Optional[StrictStr] = Field(default=None, description="Sözleşme başlığı (yoksa template adı kullanılır). Başlık, imza davet ve hatırlatma SMS'lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın. ")
    description: Optional[StrictStr] = None
    idempotency_key: Optional[Annotated[str, Field(strict=True, max_length=255)]] = Field(default=None, description="`Idempotency-Key` başlığının gövde karşılığı; başlık ekleyemeyen istemciler için. İkisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`. ")
    force: Optional[StrictBool] = Field(default=None, description="Kopya kapısını bilerek geç. Yalnız idempotency anahtarı GÖNDERİLMEYEN çağrılarda anlamlıdır: aynı şablondan aynı taraflara aynı başlık ve değişkenlerle 10 dakika içinde ikinci bir sözleşmeyi kasten göndermek için. ")
    party_mapping: Annotated[List[PartyMappingInput], Field(min_length=1)]
    documents: Optional[DocumentSelectionInput] = None
    variables: Optional[Dict[str, Optional[PartyMappingInputVariablesValue]]] = Field(default=None, description="**Root scope** — partilerden bağımsız field'lara gönderilen değerler. Item'ın template_party_id'si NULL ise (partisiz) buradan dolar. Multi-party şablonda kira_baslangic_tarihi gibi paylaşılan field'lar.  **Kaşe alanları:** Değer bir `StampData` NESNESİ ise ve slug bir kaşe alanına aitse, sözleşme oluşturulurken o kaşe `PATCH /api/v1/demands/{id}/items/{itemId}/stamp` ile aynı kurallarla doldurulur (gönderenin kaşesi yapılandırılmış veriyle; imzalayanın dolduracağı kaşeye düzenlenebilir ön değer). Aynı slug birden fazla belgede geçiyorsa hepsine yazılır; öncelik kuralları `variables` ile aynıdır (bkz. `document_variables`). Nesne değer yalnız kaşe slug'ına verilebilir: kaşe olmayan ya da gönderilecek belgelerde bulunmayan slug 400 `INVALID_VARIABLES` (`details.reason: object_value_for_non_stamp`), geçersiz kaşe verisi 400 `INVALID_STAMP_DATA` döner; ikisinde de sözleşme oluşturulmaz, kredi düşülmez. Kaşe slug'ına düz metin gönderilirse kaşeye yazılmaz ve `variables_ignored` içinde görünür. Toplu uçlar nesne değeri desteklemez (400 `INVALID_VARIABLES`, `details.reason: unsupported_endpoint`). ", json_schema_extra={"examples": [{"kira_baslangic_tarihi": "2026-10-01", "firma_kasesi": {"companyName": "Örnek Ltd.", "taxNumber": "1234567890", "taxOffice": "Kadıköy", "companyAddress": "Kadıköy, İstanbul"}}]})
    document_variables: Optional[Dict[str, Dict[str, Optional[PartyMappingInputVariablesValue]]]] = Field(default=None, description="Çok belgeli şablonda BELGE BAŞINA ortak değerler (o belgedeki tüm taraflar ve partisiz alanlar için). Opsiyoneldir; göndermezseniz `variables` ve `party_mapping[].variables` eskisi gibi çalışır.  Öncelik (alan bazında; bir seviyede anahtar yoksa bir alttakine düşer, açıkça gönderilen boş metin ya da null o seviyede kalır): 1. `party_mapping[i].document_variables[belge][slug]` 2. `document_variables[belge][slug]` 3. `party_mapping[i].variables[slug]` 4. `variables[slug]`  Şablona ait olmayan ya da bu istekte gönderilmeyen belge kimliği 400 `INVALID_DOCUMENT_VARIABLES` ile reddedilir; sözleşme oluşturulmaz, kredi düşülmez, davet gönderilmez. ")
    has_timestamp: Optional[StrictBool] = Field(default=False, description="TÜBİTAK zaman damgası")
    allowed_signature_variants: Optional[Annotated[List[StrictStr], Field(max_length=4)]] = Field(default=None, description="İmzacının kullanabileceği dijital imza yöntemleri ve görünme sırası. Dizinin SIRASI imza ekranındaki sekme sırasıdır; listede olmayan yöntem o sözleşmede kapalıdır. Değerler: `draw` (parmakla veya fareyle çizerek), `type` (adını yazarak), `upload` (imza görselini dosya olarak yükleyerek), `phone` (telefonda çizerek).  Gönderilmezse (veya `null` gönderilirse) şablonun ayarı, şablonda da tanımlı değilse organizasyonun ayarı geçerli olur; hiçbiri tanımlı değilse dört yöntem varsayılan sırayla açıktır.  Bu alan toleranslı okunur: boş dizi, dizi olmayan bir değer veya tanınmayan bir değer gönderilirse alan YOK SAYILIR ve üst katmandan devralınır. Yani boş dizi göndermek \"tüm yöntemleri kapat\" anlamına GELMEZ; en az bir yöntem her zaman açık kalır. Büyük harfli değerler (`DRAW`) kabul edilir, tekrar eden değerler teke indirilir.  Yalnız `phone` içeren bir liste reddedilir (400 `SIGNATURE_VARIANTS_PHONE_ONLY`): telefonda çizim mobil cihazda kullanılamadığı için tek başına bırakılırsa imzacı yöntemsiz kalır. En az bir `phone` olmayan yöntem bırakın.  `phone_draw` (\"Yalnız telefonda çizerek\") ayrı bir moddur ve YALNIZ tek başına gönderilir: `[\"phone_draw\"]`. Bilgisayardan açan imzacı yalnız QR kod ile telefona yönlendirilir ve imzasını telefonda çizer; telefondan açan imzacı yalnız çizerek imzalar. Başka bir yöntemle birlikte gönderilirse 400 `SIGNATURE_VARIANTS_PHONE_DRAW_EXCLUSIVE` döner.  Ayar sözleşme düzeyindedir; imzacı bazında farklılaştırılamaz. `party_mapping` girdilerine yazılan böyle bir alan yok sayılır. ", json_schema_extra={"examples": [["type", "draw"]]})
    dispatch_notifications: Optional[StrictBool] = Field(default=True, description="`false` verilirse sözleşme yalnız oluşturulur, davet gönderilmez; daha sonra `POST /api/v1/demands/{demandId}/dispatch` ile gönderilir (çok-belgeli zarf akışında belgeler bu arada eklenir). Varsayılan açık: davetler oluşturma anında gider. ")
    send_sms_notifications: Optional[StrictBool] = True
    send_email_notifications: Optional[StrictBool] = True
    sms_title: Optional[StrictStr] = Field(default='CODECK', description="SMS gönderici adı")
    sms_content: Optional[StrictStr] = Field(default=None, description="Custom SMS gövdesi. **Sadece** çağıran organizasyon **PRO veya ENTERPRISE planda** ise ve aktif `OrganizationSmsConfig` (sender_name dolu) varsa kabul edilir; aksi halde 403 `SMS_CUSTOMIZATION_NOT_ALLOWED` döner.  FREE/BASIC planda olan veya kendi SMS sağlayıcısı tanımlı olmayan müşterilerin marka itibarını korumak için sistem default sağlayıcısı (Codeck NetGSM) ile gönderim yapılır ve özel metin reddedilir. Kendi sağlayıcınızı tanımlamak için Dashboard → Organizasyon → SMS Ayarları sayfasını kullanın.  Boş string / null gönderirseniz \"clear\" olarak yorumlanır (gating'den geçer). ")
    email_content: Optional[StrictStr] = Field(default=None, description="Custom e-posta gövdesi")
    expiry_date: Optional[datetime] = None
    require_tc_verification: Optional[StrictBool] = False
    require_biometric_verification: Optional[StrictBool] = False
    reminder_settings: Optional[ReminderSettings] = Field(default=None, description="Bu sözleşme için hatırlatma ayarlarını **şablon default'unu override** ederek belirtir. Yollanmazsa şablonun `reminder_*` alanları kullanılır (PUT /api/templates/:id ile dashboard'dan kaydedilen değerler); şablonda da yoksa `{enabled:true, intervals_hours:[48], max_reminders:1, channels:[\"email\"]}` default'u uygulanır. Demand oluşumunda `ReminderConfig` satırı yaratılır ve BullMQ kuyruğuna scheduled hatırlatmalar yazılır. ")
    __properties: ClassVar[List[str]] = ["template_id", "title", "description", "idempotency_key", "force", "party_mapping", "documents", "variables", "document_variables", "has_timestamp", "allowed_signature_variants", "dispatch_notifications", "send_sms_notifications", "send_email_notifications", "sms_title", "sms_content", "email_content", "expiry_date", "require_tc_verification", "require_biometric_verification", "reminder_settings"]

    @field_validator('allowed_signature_variants')
    def allowed_signature_variants_validate_enum(cls, value):
        """Validates the enum"""
        if value is None:
            return value

        for i in value:
            if i not in set(['draw', 'type', 'upload', 'phone', 'phone_draw']):
                raise ValueError("each list item must be one of ('draw', 'type', 'upload', 'phone', 'phone_draw')")
        return value

    model_config = ConfigDict(
        validate_by_name=True,
        validate_by_alias=True,
        validate_assignment=True,
        protected_namespaces=(),
    )


    def to_str(self) -> str:
        """Returns the string representation of the model using alias"""
        return pprint.pformat(self.model_dump(by_alias=True))

    def to_json(self) -> str:
        """Returns the JSON representation of the model using alias"""
        return json.dumps(to_jsonable_python(self.to_dict()))

    @classmethod
    def from_json(cls, json_str: str) -> Optional[Self]:
        """Create an instance of CreateDemandRequest from a JSON string"""
        return cls.from_dict(json.loads(json_str))

    def to_dict(self) -> Dict[str, Any]:
        """Return the dictionary representation of the model using alias.

        This has the following differences from calling pydantic's
        `self.model_dump(by_alias=True)`:

        * `None` is only added to the output dict for nullable fields that
          were set at model initialization. Other fields with value `None`
          are ignored.
        """
        excluded_fields: Set[str] = set([
        ])

        _dict = self.model_dump(
            by_alias=True,
            exclude=excluded_fields,
            exclude_none=True,
        )
        # override the default output from pydantic by calling `to_dict()` of each item in party_mapping (list)
        _items = []
        if self.party_mapping:
            for _item_party_mapping in self.party_mapping:
                if _item_party_mapping:
                    _items.append(_item_party_mapping.to_dict())
            _dict['party_mapping'] = _items
        # override the default output from pydantic by calling `to_dict()` of documents
        if self.documents:
            _dict['documents'] = self.documents.to_dict()
        # override the default output from pydantic by calling `to_dict()` of each value in variables (dict)
        _field_dict = {}
        if self.variables:
            for _key_variables in self.variables:
                if self.variables[_key_variables]:
                    _field_dict[_key_variables] = self.variables[_key_variables].to_dict()
            _dict['variables'] = _field_dict
        # override the default output from pydantic by calling `to_dict()` of each value in document_variables (dict of dict)
        _field_dict_of_dict = {}
        if self.document_variables:
            for _key_document_variables, _value_document_variables in self.document_variables.items():
                if _value_document_variables is not None:
                    _field_dict_of_dict[_key_document_variables] = {
                        _key: _value.to_dict() for _key, _value in _value_document_variables.items()
                    }
            _dict['document_variables'] = _field_dict_of_dict
        # override the default output from pydantic by calling `to_dict()` of reminder_settings
        if self.reminder_settings:
            _dict['reminder_settings'] = self.reminder_settings.to_dict()
        # set to None if allowed_signature_variants (nullable) is None
        # and model_fields_set contains the field
        if self.allowed_signature_variants is None and "allowed_signature_variants" in self.model_fields_set:
            _dict['allowed_signature_variants'] = None

        return _dict

    @classmethod
    def from_dict(cls, obj: Optional[Dict[str, Any]]) -> Optional[Self]:
        """Create an instance of CreateDemandRequest from a dict"""
        if obj is None:
            return None

        if not isinstance(obj, dict):
            return cls.model_validate(obj)

        _obj = cls.model_validate({
            "template_id": obj.get("template_id"),
            "title": obj.get("title"),
            "description": obj.get("description"),
            "idempotency_key": obj.get("idempotency_key"),
            "force": obj.get("force"),
            "party_mapping": [PartyMappingInput.from_dict(_item) for _item in obj["party_mapping"]] if obj.get("party_mapping") is not None else None,
            "documents": DocumentSelectionInput.from_dict(obj["documents"]) if obj.get("documents") is not None else None,
            "variables": dict(
                (_k, PartyMappingInputVariablesValue.from_dict(_v))
                for _k, _v in obj["variables"].items()
            )
            if obj.get("variables") is not None
            else None,
            "document_variables": dict(
                (_k, dict(
                    (_ik, PartyMappingInputVariablesValue.from_dict(_iv))
                        for _ik, _iv in _v.items()
                    )
                    if _v is not None
                    else None
                )
                for _k, _v in obj["document_variables"].items()
            )
            if obj.get("document_variables") is not None
            else None,
            "has_timestamp": obj.get("has_timestamp") if obj.get("has_timestamp") is not None else False,
            "allowed_signature_variants": obj.get("allowed_signature_variants"),
            "dispatch_notifications": obj.get("dispatch_notifications") if obj.get("dispatch_notifications") is not None else True,
            "send_sms_notifications": obj.get("send_sms_notifications") if obj.get("send_sms_notifications") is not None else True,
            "send_email_notifications": obj.get("send_email_notifications") if obj.get("send_email_notifications") is not None else True,
            "sms_title": obj.get("sms_title") if obj.get("sms_title") is not None else 'CODECK',
            "sms_content": obj.get("sms_content"),
            "email_content": obj.get("email_content"),
            "expiry_date": obj.get("expiry_date"),
            "require_tc_verification": obj.get("require_tc_verification") if obj.get("require_tc_verification") is not None else False,
            "require_biometric_verification": obj.get("require_biometric_verification") if obj.get("require_biometric_verification") is not None else False,
            "reminder_settings": ReminderSettings.from_dict(obj["reminder_settings"]) if obj.get("reminder_settings") is not None else None
        })
        return _obj


