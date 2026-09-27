# coding: utf-8

"""
    imzala External API

    imzala.org dış API'si — şablondan sözleşme oluşturma ve takip.  **Sürüm:** 1.9.1 · **Son güncelleme:** 2026-09-27  ## Auth Tüm istekler `X-API-Key` header'ı gerektirir. API key dashboard üzerinden oluşturulur: **API & Geliştirici** sayfası (https://app.imzala.org/developer) veya **Hesap Ayarları -> API Anahtarları**.  ## Workspace (organizasyon) Organizasyon içinde oluşturulmuş bir API anahtarı kendi organizasyonuna bağlıdır: `X-Workspace-Id` başlığı gönderilmezse anahtarın organizasyonu otomatik uygulanır; gönderilirse anahtarın organizasyonuyla aynı olmalıdır (aksi halde 403 `WORKSPACE_MISMATCH`). Kişisel anahtarlar için bu başlık gerekmez.  ## Multi-Party Variables (parti-bazlı ve ortak field'lar) `POST /api/v1/demands` payload'ında iki tip \"variables\" alanı vardır:  - `party_mapping[i].variables` — **bu partiye ait** field'lar   (örn. Kira sözleşmesinde Kiraya Veren'in `address`, `iban` field'ları) - `variables` (root) — **partilerden bağımsız** field'lar   (örn. `kira_baslangic_tarihi`, `kira_bedeli`)  Resolution sırası: 1. Item'ın template_party_id'si var ve o parti slug'ı göndermişse → uygula 2. Yoksa root `variables`'tan ara → varsa uygula 3. Yoksa atla  Dashboard'daki **API Kullanımı** tab'ı (`/sablonlar/<id>`) hangi field'in hangi gruba gittiğini gösterir. Veya yeni `GET /api/v1/templates/{id}/usage` endpoint'i aynı bilgiyi JSON olarak döner.  ## Sessiz Başarısızlık Yok `POST /api/v1/demands` cevabında `variables_ignored` array'ı, gönderdiğiniz ama şablonda eşleşmeyen slug'ları listeler. Boş olmadığında yazım hatası yapmışsınız demektir — log'ta veya dashboard'da kontrol edin.  ## Rate Limit - Varsayılan: API anahtarı başına **60 istek/dakika**. Aşımda `429` döner ve   gövdede `code: \"RATE_LIMIT_EXCEEDED\"` bulunur. - `Retry-After` başlığı kaç saniye beklemeniz gerektiğini bildirir; gövdedeki   `retry_after_seconds` alanı aynı değeri taşır. - Limit bilgisi standart `RateLimit-*` yanıt başlıklarıyla gelir   (`X-RateLimit-*` **değil**): `RateLimit-Limit`, `RateLimit-Remaining`,   `RateLimit-Reset`, `RateLimit-Policy`. - Anahtar başına limit yükseltilebilir; ihtiyacınız varsa bize yazın. - Bazı uçlarda daha sıkı, uç-bazlı limitler ayrıca geçerlidir:    | Uç | Limit |   |----|-------|   | `POST /api/v1/timestamps` | 10 istek/dakika |   | `POST /api/v1/demands/bulk` | 5 istek/dakika |   | `POST /api/v1/demands/{id}/embed-session` | 5 istek/dakika |   | `POST /api/v1/field-templates/{id}/preview-layout` | 5 istek/dakika |  ## Hatalar Standart HTTP kodları: 400 (geçersiz veri), 401 (auth), 403 (yetki), 404 (yok), 429 (rate limit), 500 (sunucu)  ## Loglar Tüm API istekleriniz dashboard'da `Geliştirici -> Etkinlik Logu` sayfasında görünür (request body, response body, headers, status code, süre). 30 gün retention.  ## Hatırlatma Sistemi İmzalanmamış taraflara hatırlatma SMS/e-posta'sı **iki yolla** gönderilir:  **1. Otomatik (scheduled) hatırlatmalar — şablona/sözleşmeye gömülü**  Şablon (Template) seviyesinde `reminder_settings` (interval saatleri, max sayısı, kanallar) tanımlayabilirsiniz. Şablondan demand oluştururken bu değerler yeni sözleşmenin `ReminderConfig` satırına otomatik kopyalanır ve BullMQ worker'ı zamanı geldiğinde sessiz şekilde hatırlatır.  - Dashboard editörden ayarlanır: `app.imzala.org/sablonlar/<id>/duzenle`   → **Sözleşme Ayarları** → **Otomatik Hatırlatma** + **Hatırlatma Kanalı** - Veya `POST /api/v1/demands` çağrısında body'de `reminder_settings`   alanıyla **bu sözleşme için override** edebilirsiniz (şablon default'unu   ezer, sadece bu demand'a uygulanır) - Default: `{enabled: true, intervals_hours: [48], max_reminders: 1, channels: [\"email\"]}`  **2. Manuel (anlık) hatırlatma — tetikleme endpoint'i**  `POST /api/v1/demands/{id}/reminders` ile **şu an** SMS/e-posta hatırlatması gönderebilirsiniz. Anti-spam: Aynı sözleşme için son hatırlatmadan 5 dakika geçmemişse 429 `RATE_LIMITED` döner; `force: true` ile override edilebilir.  **Kişi başına sert sınırlar (override edilemez):** - Bir kişiye en fazla 3 SMS reminder gönderilebilir (otomatik scheduled +   manuel trigger toplam). - Bir kişiye en fazla 3 e-posta reminder gönderilebilir. - Sınıra ulaşan kişi response'un `details[]` listesinde   `skipped` olarak görünür (`reason: \"party_sms_cap_reached (3)\"` veya   `\"party_email_cap_reached (3)\"`); diğer kişilere gönderim devam eder. - `force: true` bu kişi-başı sınırları override etmez.  ```bash # Default — SMS + e-posta birlikte (parti eligibility'sine göre) curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" -d '{}'  # Sadece SMS, anti-spam override curl -X POST https://api-prd.imzala.org/api/v1/demands/<demand_id>/reminders \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{\"channels\": [\"sms\"], \"force\": true}' ```  Detay için **Reminders** tag'i altındaki endpoint'e bakın.  ## Webhooks imzala olay gerçekleştiğinde (sözleşme tamamlandı, taraf imzaladı vb.) sizin belirlediğiniz HTTPS URL'ye `POST` ile JSON payload gönderir. Webhook'lar dashboard'dan yönetilir: **Ayarlar -> Webhook'lar** (https://app.imzala.org/settings/webhooks). API üzerinden CRUD desteklenmez.  ### Workspace kapsamı - **Organizasyon webhook'u** (org workspace'inde oluşturulduysa) → o   organizasyon altındaki TÜM üyelerin event'lerinde tetiklenir - **Kişisel webhook** (kişisel workspace'te) → sadece sizin kendi   event'lerinizde tetiklenir  ### Olay tipleri (11) | Olay | Tetikleyici | |------|-------------| | `demand.created` | Yeni sözleşme oluşturuldu | | `demand.completed` | Tüm taraflar imzaladı | | `demand.expired` | Sözleşme süresi doldu | | `party.signed` | Bir taraf imzaladı | | `party.viewed` | Bir taraf imza sayfasını ilk kez açtı | | `party.rejected` | Bir taraf reddetti | | `kyc.completed` | Kimlik doğrulama başarıyla tamamlandı | | `kyc.failed` | Kimlik doğrulama başarısız sonuçlandı | | `contract.expiring` | Takip edilen sözleşme bitiş tarihine yaklaşıyor | | `contract.ended` | Takip edilen sözleşmenin platformdaki bitiş tarihi geçti | | `contract.advanced` | Otomatik yenilenen sözleşmenin takip edilen bitişi ileri alındı |  > **Not (contract.\\* olayları):** Bunlar platformun **takip amaçlı** > kayıtlarıdır; sözleşmenin hukuken yenilendiğini veya sona erdiğini > BELİRTMEZ. Detay için `ContractTerm` şemasına ve `state` alanına bakın.  ### Header'lar Her istekte aşağıdaki header'lar gönderilir:  ``` Content-Type: application/json User-Agent: Imzala-Webhook/1.0 X-Imzala-Event: <olay tipi, örn. demand.completed> X-Imzala-Delivery: <delivery UUID — idempotency key> X-Imzala-Signature-256: sha256=<HMAC-SHA256 hex> ```  ### Payload zarfı Tüm olaylar aynı zarfı kullanır:  ```json {   \"id\": \"evt_abc123...\",   \"type\": \"demand.completed\",   \"created_at\": \"2026-05-07T08:30:00.000Z\",   \"data\": { \"...olay-özel alanlar...\" } } ```  - `id` — `evt_<32-hex>`. Idempotency için kullanın (DB'de unique key). - `type`: yukarıdaki 11 olay tipinden biri (lowercase). - `created_at` — olay zamanı (ISO 8601 UTC). - `data` — her olaya özel (aşağıda her olay için ayrı şema).  ### İmza doğrulama (HMAC-SHA256) Webhook oluşturduğunuzda dashboard size `whsec_<64-hex>` formatında bir secret döner — **sadece bir kez gösterilir**, güvenli yere kaydedin.  Her isteğin ham gövdesi (body) bu secret ile HMAC-SHA256 imzalanır ve `X-Imzala-Signature-256: sha256=<hex>` header'ında gönderilir. Doğrulama Node.js örneği:  ```js const crypto = require('crypto');  function verify(rawBody, header, secret) {   const expected = 'sha256=' + crypto     .createHmac('sha256', secret)     .update(rawBody, 'utf8')     .digest('hex');   return crypto.timingSafeEqual(     Buffer.from(header || '', 'utf8'),     Buffer.from(expected, 'utf8')   ); }  // Express app.post('/webhook', express.raw({ type: 'application/json' }), (req, res) => {   const sig = req.header('X-Imzala-Signature-256');   if (!verify(req.body, sig, process.env.IMZALA_WEBHOOK_SECRET)) {     return res.status(401).send('invalid signature');   }   const event = JSON.parse(req.body.toString('utf8'));   // ... event'i kuyruğa koy ve hemen 2xx dön   res.status(200).send('ok'); }); ```  > **Önemli:** Body'yi parse etmeden ham byte üzerinden imzalayın. Çoğu > framework (Express, FastAPI vs.) \"raw body\" middleware'i sağlar.  ### Yeniden deneme politikası - **Başarı:** HTTP 2xx — delivery `SENT` olarak işaretlenir. - **Başarısızlık:** 2xx dışı veya bağlantı hatası — yeniden denenir. - **Per-attempt timeout:** 10 saniye (yapılandırılabilir env: `WEBHOOK_TIMEOUT_MS`). - **Maksimum deneme:** 6 (ilk + 5 retry). - **Backoff (exponential):** 30s → 2dk → 10dk → 30dk → 2sa. - **Tükenirse:** delivery `DEAD_LETTER` olur, dashboard'dan manuel   \"Tekrar Gönder\" mümkün.  Endpoint'iniz **10 saniyeden kısa sürede 2xx dönmelidir**. Ağır işleri (DB yazma, e-posta vs.) async kuyruğa atın.  ### Idempotency Aynı olay birden fazla kez gönderilebilir (network retry, manuel redeliver, backfill). Receiver tarafında **`payload.id`** unique olduğu için bunu DB'de tek seferlik kayıt için kullanın:  ```sql CREATE TABLE imzala_webhook_seen (   event_id TEXT PRIMARY KEY,   received_at TIMESTAMPTZ DEFAULT now() ); -- INSERT ... ON CONFLICT DO NOTHING; sonuç 0 satır ise zaten gördük → skip ```  ### Backfill flag Geçmiş olayları yeniden tetiklemek (örn. webhook bug fix'inden sonra kayıp event'leri yakalamak) için bazı payload'larda `data._backfill: true` bayrağı bulunur. Bu durumda receiver:  - Loglama için kayıt edebilir - Side-effect tetikleyicilerini (ödeme, e-posta gönderme, vs.) **atlamalı** - `id` zaten görülmüşse normal flow'a devam edebilir  ```js if (event.data._backfill === true) {   await logReplay(event);   return res.status(200).send('replay accepted'); } ```  ### Manuel yeniden gönderim Dashboard'da `Ayarlar -> Webhook'lar -> <webhook> -> Teslim Geçmişi`:  - Her satırda **Tekrar Gönder** butonu (PENDING dışında her statü için) - Üstte **Son 5'i Tekrar Gönder** toplu butonu (max 50 değiştirilebilir) - Yeni delivery kaydı oluşur, orijinali bozmaz (audit trail korunur)  ### En iyi pratikler 1. Aynı `id`'yi tekrar görürseniz işlemi atla (idempotency). 2. İmzayı **timing-safe compare** ile doğrula (string equality değil). 3. 10sn'den hızlı 2xx dön; ağır işi kuyruğa at. 4. `_backfill: true` payload'larda side-effect'leri atla. 5. `X-Imzala-Delivery` UUID'sini log'la — destek talebinde bizimkiyle    eşleşmesini kolaylaştırır. 6. HTTPS endpoint kullan; secret'i env var'da sakla, koda gömme. 

    The version of the OpenAPI document: 1.9.1
    Contact: destek@imzala.org
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


from __future__ import annotations
import pprint
import re  # noqa: F401
import json

from datetime import date, datetime
from pydantic import BaseModel, ConfigDict, Field, StrictBool, StrictInt, StrictStr, field_validator
from typing import Any, ClassVar, Dict, List, Optional
from typing_extensions import Annotated
from typing import Optional, Set
from typing_extensions import Self
from pydantic_core import to_jsonable_python

class ContractTerm(BaseModel):
    """
    Sözleşme süre/yenileme takibi. Sözleşmede takip hiç tanımlanmadıysa (`start_mode` VE `end_date` boşsa) bu alan `null` döner.  Bu, hukuki bir yenileme veya sona erme beyanı değildir; platformun takip amaçlı kaydıdır. Sözleşmenin fiilen ne zaman sona erdiği veya yenilendiği, tarafların kendi sözleşme hükümlerine ve yürürlükteki hukuka tabidir. Bu alan yalnız hatırlatma ve görünürlük sağlar. 
    """ # noqa: E501
    start_mode: Optional[StrictStr] = Field(description="Takibin ne zaman başladığı. `FIXED_DATE`: `start_date`'ten. `ON_FIRST_SIGNATURE` / `ON_COMPLETION`: ilk imza / tüm imzalar tamamlandığı anda otomatik (o ana kadar `end_date` bilinmez). ")
    start_date: Optional[date]
    duration_months: Optional[Annotated[int, Field(le=600, strict=True, ge=1)]]
    fixed_end_date: Optional[date]
    end_date: Optional[date] = Field(description="Takip edilen bitiş tarihi. `renewal_type: AUTO_RENEW` sözleşmelerde her dönemde (günlük olarak) bir sonraki döneme ileri alınır; bkz. `contract.advanced` webhook olayı. ")
    end_date_signed: Optional[date] = Field(description="İmzalanan belge metninde bitiş tarihi bir değişken olarak yazılıysa, sözleşme tamamlandığı anda `end_date`'in dondurulmuş kopyası. `end_date` sonradan ileri alınsa bile bu alan DEĞİŞMEZ. İmzalanan metinle takip değeri arasındaki tarihsel referans budur. ")
    renewal_type: Optional[StrictStr]
    renewal_period_months: Optional[Annotated[int, Field(le=600, strict=True, ge=1)]]
    notice_days: Optional[Annotated[int, Field(le=3650, strict=True, ge=0)]] = Field(description="Fesih ihbarı için bitişten önce kaç gün gerektiği.")
    notice_deadline: Optional[date] = Field(description="Hesaplanır (`end_date - notice_days`); `end_date` veya `notice_days` boşsa `null`.")
    reminder_offsets: Optional[Annotated[List[Annotated[int, Field(le=3650, strict=True, ge=0)]], Field(max_length=5)]] = Field(description="Bitişten kaç gün önce hatırlatma gönderileceği (gün sayıları).")
    notify_counterparty: StrictBool = Field(description="Karşı tarafa da hatırlatma gönderilsin mi.")
    state: StrictStr = Field(description="`UNTRACKED`: takip yok (`end_date` bilinmiyor). `ACTIVE`: takip sürüyor. `ENDED`: `FIXED_TERM` sözleşmenin bitiş tarihi geçti. `STOPPED`: yenileme takibi kullanıcı tarafından durduruldu. ")
    days_left: Optional[StrictInt] = Field(description="Bugünden `end_date`'e gün sayısı (negatifse geçmişte).")
    renewal_stopped_at: Optional[datetime]
    __properties: ClassVar[List[str]] = ["start_mode", "start_date", "duration_months", "fixed_end_date", "end_date", "end_date_signed", "renewal_type", "renewal_period_months", "notice_days", "notice_deadline", "reminder_offsets", "notify_counterparty", "state", "days_left", "renewal_stopped_at"]

    @field_validator('start_mode')
    def start_mode_validate_enum(cls, value):
        """Validates the enum"""
        if value is None:
            return value

        if value not in set(['FIXED_DATE', 'ON_FIRST_SIGNATURE', 'ON_COMPLETION']):
            raise ValueError("must be one of enum values ('FIXED_DATE', 'ON_FIRST_SIGNATURE', 'ON_COMPLETION')")
        return value

    @field_validator('renewal_type')
    def renewal_type_validate_enum(cls, value):
        """Validates the enum"""
        if value is None:
            return value

        if value not in set(['AUTO_RENEW', 'FIXED_TERM']):
            raise ValueError("must be one of enum values ('AUTO_RENEW', 'FIXED_TERM')")
        return value

    @field_validator('state')
    def state_validate_enum(cls, value):
        """Validates the enum"""
        if value not in set(['UNTRACKED', 'ACTIVE', 'ENDED', 'STOPPED']):
            raise ValueError("must be one of enum values ('UNTRACKED', 'ACTIVE', 'ENDED', 'STOPPED')")
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
        """Create an instance of ContractTerm from a JSON string"""
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
        # set to None if start_mode (nullable) is None
        # and model_fields_set contains the field
        if self.start_mode is None and "start_mode" in self.model_fields_set:
            _dict['start_mode'] = None

        # set to None if start_date (nullable) is None
        # and model_fields_set contains the field
        if self.start_date is None and "start_date" in self.model_fields_set:
            _dict['start_date'] = None

        # set to None if duration_months (nullable) is None
        # and model_fields_set contains the field
        if self.duration_months is None and "duration_months" in self.model_fields_set:
            _dict['duration_months'] = None

        # set to None if fixed_end_date (nullable) is None
        # and model_fields_set contains the field
        if self.fixed_end_date is None and "fixed_end_date" in self.model_fields_set:
            _dict['fixed_end_date'] = None

        # set to None if end_date (nullable) is None
        # and model_fields_set contains the field
        if self.end_date is None and "end_date" in self.model_fields_set:
            _dict['end_date'] = None

        # set to None if end_date_signed (nullable) is None
        # and model_fields_set contains the field
        if self.end_date_signed is None and "end_date_signed" in self.model_fields_set:
            _dict['end_date_signed'] = None

        # set to None if renewal_type (nullable) is None
        # and model_fields_set contains the field
        if self.renewal_type is None and "renewal_type" in self.model_fields_set:
            _dict['renewal_type'] = None

        # set to None if renewal_period_months (nullable) is None
        # and model_fields_set contains the field
        if self.renewal_period_months is None and "renewal_period_months" in self.model_fields_set:
            _dict['renewal_period_months'] = None

        # set to None if notice_days (nullable) is None
        # and model_fields_set contains the field
        if self.notice_days is None and "notice_days" in self.model_fields_set:
            _dict['notice_days'] = None

        # set to None if notice_deadline (nullable) is None
        # and model_fields_set contains the field
        if self.notice_deadline is None and "notice_deadline" in self.model_fields_set:
            _dict['notice_deadline'] = None

        # set to None if reminder_offsets (nullable) is None
        # and model_fields_set contains the field
        if self.reminder_offsets is None and "reminder_offsets" in self.model_fields_set:
            _dict['reminder_offsets'] = None

        # set to None if days_left (nullable) is None
        # and model_fields_set contains the field
        if self.days_left is None and "days_left" in self.model_fields_set:
            _dict['days_left'] = None

        # set to None if renewal_stopped_at (nullable) is None
        # and model_fields_set contains the field
        if self.renewal_stopped_at is None and "renewal_stopped_at" in self.model_fields_set:
            _dict['renewal_stopped_at'] = None

        return _dict

    @classmethod
    def from_dict(cls, obj: Optional[Dict[str, Any]]) -> Optional[Self]:
        """Create an instance of ContractTerm from a dict"""
        if obj is None:
            return None

        if not isinstance(obj, dict):
            return cls.model_validate(obj)

        _obj = cls.model_validate({
            "start_mode": obj.get("start_mode"),
            "start_date": obj.get("start_date"),
            "duration_months": obj.get("duration_months"),
            "fixed_end_date": obj.get("fixed_end_date"),
            "end_date": obj.get("end_date"),
            "end_date_signed": obj.get("end_date_signed"),
            "renewal_type": obj.get("renewal_type"),
            "renewal_period_months": obj.get("renewal_period_months"),
            "notice_days": obj.get("notice_days"),
            "notice_deadline": obj.get("notice_deadline"),
            "reminder_offsets": obj.get("reminder_offsets"),
            "notify_counterparty": obj.get("notify_counterparty"),
            "state": obj.get("state"),
            "days_left": obj.get("days_left"),
            "renewal_stopped_at": obj.get("renewal_stopped_at")
        })
        return _obj


