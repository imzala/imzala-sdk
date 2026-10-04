# Değişiklik günlüğü

Bu paket [Semantic Versioning](https://semver.org/lang/tr/) izler. API sürüm uyumu için README'deki "Sürüm uyumu" tablosuna bakın.

## 1.0.0

İlk kararlı sürüm; semver sözü bu sürümle başlar. Konuştuğu API: v1 `1.10.1`.

**0.x'ten yükseltme, facade (`ImzalaClient` ve kaynak sınıfları) kullananlar için kod değişikliği gerektirmez.** 0.x'in kapsadığı uçların istek ve yanıt gövdeleri aynıdır; 1.0.0 eksik yüzeyi tamamlar ve aşağıdaki hataları düzeltir. Üretilmiş istemciyi doğrudan çağıranlar için aşağıdaki nota bakın.

### API 1.8.18 - 1.10.1 ile gelenler

- API 1.10.1: imzacı dili. Tarafa dil atanabilir (`party_mapping[i].locale`, `tr` / `en` / `null`); şablonda `default_locale`; sözleşme detayında tarafın `locale` ve imzaladığı andaki `signed_locale` alanları. Bildirim (e-posta, SMS) tarafa atanan dilde gider, atanmamışsa Türkçe. WhatsApp'ta İngilizce için kurumun onaylı İngilizce şablonu gerekir. Geçersiz değer `INVALID_LOCALE` ile reddedilir; toplu oluşturmada yalnız o satır `failed` olur.
- API 1.10.x: hatırlatma yanıtında `reminders_ineligible` sayacı (hatırlatma gönderilemeyen taraflar).
- Hata kataloğuna 3 yeni kod eklendi: `AI_TOOL_ACCESS_DISABLED` (kurum yapay zekâ araçlarının erişimini kapatmış), `INVALID_LOCALE`, `CHANGE_REQUEST_PENDING` (tarafın açık düzeltme talebi var).

- API 1.9.1: sözleşme süresi ve yenileme takibi. Sözleşme oluşturma gövdesinde isteğe bağlı `term_start_mode`, `term_start_date`, `term_duration_months`, `term_fixed_end_date`, `renewal_type`, `renewal_period_months`, `notice_days`, `reminder_offsets` ve `notify_counterparty`; gönderilmeyen alanlar için şablonun takip ayarı kullanılır. Sözleşme detayında `term` ve `archived_at`, listede `term_end_date` ve `archived_at` alanları. `term` platformun takip kaydıdır; sözleşmenin hukuken yenilendiğini veya sona erdiğini belirtmez.
- API 1.9.1: arşivleme. Arşiv sözleşmenin durumunu değiştirmez; arşivdeki sözleşme değiştirilemez ve silinemez (`DEMAND_ARCHIVED`). Liste ucunda `archived` filtresi; verilmezse eskisi gibi arşivli ve arşivsiz tüm sözleşmeler döner.
- API 1.9.1: yeni webhook olayları `contract.expiring`, `contract.ended` ve `contract.advanced` (ortak veri: `WebhookDataContractTerm`). Bunlar da takip kaydıdır.
- Facade yardımcıları: `demands()->updateTerm($id, $body)` (`PATCH /api/v1/demands/{id}/term`, kısmi güncelleme: `null` alanı temizler, verilmeyen anahtar gönderilmez), `demands()->archive($id)`, `demands()->unarchive($id)` ve `demands()->list()` için sona eklenen isteğe bağlı `$archived` parametresi (`'exclude'` / `'only'` / `'include'`). Yazmalar otomatik yeniden denenmez.
- Hata kataloğuna 4 yeni kod eklendi: `TERM_INVALID`, `DEMAND_ARCHIVED`, `DEMAND_NOT_ARCHIVABLE`, `DEMAND_REJECTED_CANCEL_FIRST`. `TERM_INVALID` yanıt gövdesindeki `field` alanı reddedilen anahtarı gösterir.
- Gömülü imza ucunun açıklaması güncellendi: bu akışla atılan dijital imza 5070 sayılı Kanun m.3 anlamında elektronik imzadır; ek doğrulama adımları delil değerini güçlendirir, imza seviyesini değiştirmez.
- Kaşe alanını doldurma ucu için facade yardımcısı: `demands()->updateStamp($id, $itemId, $body)` (`PATCH /api/v1/demands/{id}/items/{itemId}/stamp`). PATCH, otomatik yeniden denenmez.
- API 1.8.23: örnek verilerdeki gerçek alan adlı e-posta adresleri ve standart dışı telefon numaraları kurgusal değerlerle değiştirildi (sözleşme değişmedi).
- API 1.8.19 - 1.8.22: yeni uç `PATCH /api/v1/demands/{id}/items/{itemId}/stamp` (kaşe alanını doldurma); `variables`, `party_mapping[].variables` ve `document_variables` değerlerinde kaşe için yapılandırılmış `StampData` nesnesi; belge başına `document_variables`; yanıtta `stamps_applied`; `allowed_signature_variants` için `phone_draw` (yalnız tek başına) ve zaman akışında `MOBILE_SIGNATURE_CAPTURED` olayı. Alanlar şimdilik üretilmiş istemci modelleri üzerinden kullanılır; uç için facade yardımcısı üstteki maddede.
- Hata kataloğuna 10 yeni kod eklendi: `SIGNATURE_VARIANTS_PHONE_DRAW_EXCLUSIVE`, `INVALID_VARIABLES`, `INVALID_DOCUMENT_VARIABLES`, `INVALID_STAMP_DATA`, `INVALID_ITEM_ID`, `ITEM_NOT_FOUND`, `NOT_A_STAMP_ITEM`, `PARTY_NOT_ASSIGNED_TO_DOCUMENT`, `DEMAND_PARTIALLY_SIGNED`, `FIRST_SIGNATURE_OTP_UNSUPPORTED`.
- API 1.8.18: tek sözleşme isteğinde `allowed_signature_variants` ve toplu istekte `options.allowed_signature_variants`. Sıra korunur; `null` veya atlanan alan sunucu varsayılanını devralır. Yalnız `phone` seçimi `SIGNATURE_VARIANTS_PHONE_ONLY` ile reddedilir.
- Üretilmiş istemci modelleri ve hata kataloğu aynı sözleşmeyle güncellendi.

### Eklendi

- Alan Şablonları: `fieldTemplates()->list()`, `fieldTemplates()->get()`, `fieldTemplates()->previewLayout($id, $files, $onAnchorMiss)` (yerleşimi tam bir PDF üzerinde kuru koşumla dener, sözleşme oluşturmaz, kredi harcamaz).
- Kişiler: `contacts()->list()`, `contacts()->listAll()` (generator), `contacts()->create()`.
- Raporlar: `reports()->get()` (sözleşme durumlarının toplu sayımı, parametre almaz).
- Zaman damgası listesi ve detayı: `timestamps()->list()`, `timestamps()->get()`.
- Toplu sözleşme oluşturma: `demands()->createBulk()` (tek istekte en çok 10 satır).
- Çok belgeli zarf: `demands()->documents()->list/create/upload/update/delete/reorder/setAssignments` ve `demands()->dispatch()`. `upload` zorunlu `$idempotencyKey` alır; sunucunun 409 `IDEMPOTENT_REPLAY` yanıtı mevcut belge olarak döner. Özellik açık değilse `ENVELOPE_MULTI_DOC_DISABLED` fırlatılır, `list` boş liste döndürmez.
- Zarfta belge-özgü imzalı PDF: `demands()->getDocumentPdf($id, $documentId)`.
- `Idempotency-Key` parametreleri: `demands()->create($body, $idempotencyKey)` ve `UploadDemandParams::withIdempotencyKey()`; `CreateTimestampParams::withIdempotencyKey()` zaten vardı. Anahtarla gönderilen bu yazmalar 429 sonrasında tam bir kez yeniden denenir.
- `UploadDemandParams` yeni seçenekler: `withFieldTemplateId()`, `withOnAnchorMiss()` (`'block'` | `'drop'`), `withSendInvitations()` (bu uçta varsayılan kapalı; `'sms'` SMS ve WhatsApp'ı kapsar, yalnız daraltır), `withForce()`; `UploadPartyInput` beşinci parametre `templatePartyId` (Alan Şablonu ile yüklemede her tarafta zorunlu).
- `CreateDemandRequest` tipli modelinde `dispatch_notifications` (sessiz taslak, sonra `dispatch`).
- Hata kodu kataloğu: `Imzala\ErrorCodes::CODES` (73 kod, tek satırlık Türkçe açıklama), `ErrorCodes::describe()`, `ErrorCodes::isKnown()`; `ImzalaException::getCodeDescription()`. Katalog vendorlanmış spesifikasyona karşı iki yönlü test edilir.
- İstek limiti bilgisi: `ImzalaRateLimitException::getRateLimit()` (`RateLimitInfo`: `RateLimit-Limit` / `-Remaining` / `-Reset` / `-Policy`; `X-RateLimit-*` okunmaz, sunucu göndermez).
- Yerel doğrulama: API anahtarı (kurucuda) ve `Idempotency-Key` yazdırılabilir ASCII değilse istek gönderilmeden `ImzalaValidationException` (dosyadan okunan anahtarın sonundaki satır sonu tuzağı). Önce satır sonu reddediliyor, ASCII dışı baytlar ise telde gidiyordu.

### Değişti

- Yalnız düz metin taşıyan hata gövdesinde (`{"error": "metin"}`) `getErrorCode()` artık `null` döner; önce mesajın kopyasıydı.
- GET yeniden denemesinde bekleme tavanı 60 saniye: `Retry-After` daha uzunsa SDK beklemek yerine 429'u fırlatır. Anahtarlı yazma tekrarı aynı tavanı uygular.
- Üretilmiş istemcinin istek gönderilmeden fırlattığı parametre hataları (`InvalidArgumentException`: kişi ve zaman damgası listelerinde `$limit` 10..100, anahtar uzunluğu) artık `ImzalaValidationException` olarak gelir (`getStatusCode()` `null`, orijinal istisna `getPrevious()`'ta). `ImzalaException` yakalayan kod bunları kaçırmıyor.
- Dokümanlarda standart kurgusal örnek kişi (Ayşe Yılmaz, `ayse@example.com`, `+905551112233`).

### Düzeltildi

- `uploadDocument`: geçersiz UTF-8 içeren taraf listesi telde boş bir dize olarak gidiyordu. Taraflar ve sıra artık `JSON_THROW_ON_ERROR` + `JSON_UNESCAPED_UNICODE` ile kodlanır; geçersiz girdi geçici dosya yazılmadan `JsonException` fırlatır.
- Hata kodu çıkarma: `{"error": "metin", "code": "KOD"}` gövdesinde kod artık `code` alanından okunur; önce metin kodun yerine geçiyordu.
- `Retry-After` `NAN`, `INF` veya negatif geldiğinde yok sayılır (negatif değer hemen tekrarlıyor, sonsuz değer 60 sn tavanına takılıyordu); HTTP tarihi biçimi desteklenir.
- `templates()->delete()` dokümanı: kayıt 30 gün saklanır; aktif sözleşmesi olan şablon silinemez (`409 TEMPLATE_IN_USE`).
- `me()` dokümanı: hiçbir kapsam (scope) istemez.

### Üretilmiş istemciyi doğrudan kullananlar için

Bu sürümde `Imzala\Client\Api` ve `Imzala\Client\Model` altındaki üretilmiş sınıflar API `1.8.17` spesifikasyonundan yeniden üretildi. Sözleşme oluşturma ve belge yükleme uçlarına eklenen opsiyonel `Idempotency-Key` parametresi, üretilen metod imzalarında mevcut parametrelerin **arasına** girer (yükleme: `$files`, `$parties`, `$idempotency_key`, `$order`, ...). Üretilmiş `apiV1DemandsUploadPost...` metodunu **konumsal** argümanlarla çağıran kod derlenmeye devam eder ama argümanlar bir kaydırılır: `order` anahtar, `title` sıra, `description` başlık olarak gider. İsimli argümanlara geçin ya da facade kullanın; facade isimli argümanlarla çağırır ve her yuva testle kilitlidir.
