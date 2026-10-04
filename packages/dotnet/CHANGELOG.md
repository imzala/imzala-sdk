# Değişiklik günlüğü

Bu paket [Semantic Versioning](https://semver.org/lang/tr/) izler. API sürüm uyumu için README'deki "Sürüm uyumu" tablosuna bakın.

## 1.0.0

İlk kararlı sürüm; semver sözü bu sürümle başlar. Konuştuğu API: v1 `1.10.1`.

**0.x'ten yükseltme, facade (`ImzalaSdk.Imzala` ve kaynak sınıfları) kullananlar için kod değişikliği gerektirmez.** 0.x'in kapsadığı uçların istek ve yanıt gövdeleri aynıdır; 1.0.0 eksik yüzeyi tamamlar ve aşağıdaki hataları düzeltir. Üretilmiş istemciyi doğrudan çağıranlar için aşağıdaki nota bakın.

### API 1.8.18 - 1.10.1 ile gelenler

- API 1.10.1: imzacı dili. Tarafa dil atanabilir (`party_mapping[i].locale`, `tr` / `en` / `null`); şablonda `default_locale`; sözleşme detayında tarafın `locale` ve imzaladığı andaki `signed_locale` alanları. Bildirim (e-posta, SMS) tarafa atanan dilde gider, atanmamışsa Türkçe. WhatsApp'ta İngilizce için kurumun onaylı İngilizce şablonu gerekir. Geçersiz değer `INVALID_LOCALE` ile reddedilir; toplu oluşturmada yalnız o satır `failed` olur.
- API 1.10.x: hatırlatma yanıtında `reminders_ineligible` sayacı (hatırlatma gönderilemeyen taraflar).
- Hata kataloğuna 3 yeni kod eklendi: `AI_TOOL_ACCESS_DISABLED` (kurum yapay zekâ araçlarının erişimini kapatmış), `INVALID_LOCALE`, `CHANGE_REQUEST_PENDING` (tarafın açık düzeltme talebi var).

- API 1.9.1: sözleşme süresi ve yenileme takibi. Sözleşme oluşturma gövdesinde isteğe bağlı `term_start_mode`, `term_start_date`, `term_duration_months`, `term_fixed_end_date`, `renewal_type`, `renewal_period_months`, `notice_days`, `reminder_offsets` ve `notify_counterparty`; gönderilmeyen alanlar için şablonun takip ayarı kullanılır. Sözleşme detayında `term` ve `archived_at`, listede `term_end_date` ve `archived_at` alanları. `term` platformun takip kaydıdır; sözleşmenin hukuken yenilendiğini veya sona erdiğini belirtmez.
- API 1.9.1: arşivleme. Arşiv sözleşmenin durumunu değiştirmez; arşivdeki sözleşme değiştirilemez ve silinemez (`DEMAND_ARCHIVED`). Liste ucunda `archived` filtresi; verilmezse eskisi gibi arşivli ve arşivsiz tüm sözleşmeler döner.
- API 1.9.1: yeni webhook olayları `contract.expiring`, `contract.ended` ve `contract.advanced` (ortak veri: `WebhookDataContractTerm`). Bunlar da takip kaydıdır.
- Facade yardımcıları: `Demands.UpdateTermAsync(Guid, ContractTermUpdate)` (`PATCH /api/v1/demands/{id}/term`): yalnız set edilen özellikleri gönderir, `ContractTermUpdate.Clear` içindeki alan adlarını `null` olarak gönderip temizler. `Demands.ArchiveAsync(Guid)`, `Demands.UnarchiveAsync(Guid)` ve arşiv filtresi için yeni `Demands.ListAsync(DemandArchiveFilter archived, ...)` aşırı yüklemesi; mevcut `ListAsync` imzası değişmedi. Yazmalar otomatik yeniden denenmez.
- `CreateDemandRequest` içindeki yeni süre alanları kurucuya eklenmedi (yayımlanmış kurucu imzası korunur); nesne başlatıcısıyla ayarlanır ve yalnız ayarlandıklarında gönderilir. `NotifyCounterparty` yalnız `true` olduğunda gönderilir; şablonun ayarını `false` ile kapatmak için oluşturduktan sonra `UpdateTermAsync` kullanın.
- Hata kataloğuna 4 yeni kod eklendi: `TERM_INVALID`, `DEMAND_ARCHIVED`, `DEMAND_NOT_ARCHIVABLE`, `DEMAND_REJECTED_CANCEL_FIRST`. `TERM_INVALID` yanıt gövdesindeki `field` alanı reddedilen anahtarı gösterir.
- Gömülü imza ucunun açıklaması güncellendi: bu akışla atılan dijital imza 5070 sayılı Kanun m.3 anlamında elektronik imzadır; ek doğrulama adımları delil değerini güçlendirir, imza seviyesini değiştirmez.
- Kaşe alanını doldurma ucu için facade yardımcısı: `Demands.UpdateStampAsync(Guid, int, PatchStampItemRequest)` (`PATCH /api/v1/demands/{id}/items/{itemId}/stamp`). PATCH, otomatik yeniden denenmez. Bu ucu `null` alanları göndermeden saran ayrı bir sarmalayıcı kullanır; üretilmiş `StampData` modeli ayarlanmamış alanları `null` olarak yollar ve bu uçta `null` alanı siler.
- API 1.8.23: örnek verilerdeki gerçek alan adlı e-posta adresleri ve standart dışı telefon numaraları kurgusal değerlerle değiştirildi (sözleşme değişmedi).
- API 1.8.19 - 1.8.22: yeni uç `PATCH /api/v1/demands/{id}/items/{itemId}/stamp` (kaşe alanını doldurma); `variables`, `party_mapping[].variables` ve `document_variables` değerlerinde kaşe için yapılandırılmış `StampData` nesnesi; belge başına `document_variables`; yanıtta `stamps_applied`; `allowed_signature_variants` için `phone_draw` (yalnız tek başına) ve zaman akışında `MOBILE_SIGNATURE_CAPTURED` olayı. Alanlar şimdilik üretilmiş istemci modelleri üzerinden kullanılır; uç için facade yardımcısı üstteki maddede.
- Hata kataloğuna 10 yeni kod eklendi: `SIGNATURE_VARIANTS_PHONE_DRAW_EXCLUSIVE`, `INVALID_VARIABLES`, `INVALID_DOCUMENT_VARIABLES`, `INVALID_STAMP_DATA`, `INVALID_ITEM_ID`, `ITEM_NOT_FOUND`, `NOT_A_STAMP_ITEM`, `PARTY_NOT_ASSIGNED_TO_DOCUMENT`, `DEMAND_PARTIALLY_SIGNED`, `FIRST_SIGNATURE_OTP_UNSUPPORTED`.
- API 1.8.18: tek sözleşme isteğinde `allowed_signature_variants` ve toplu istekte `options.allowed_signature_variants`. Sıra korunur; `null` veya atlanan alan sunucu varsayılanını devralır. Yalnız `phone` seçimi `SIGNATURE_VARIANTS_PHONE_ONLY` ile reddedilir.
- Üretilmiş istemci modelleri ve hata kataloğu aynı sözleşmeyle güncellendi.

### Eklendi

- Alan Şablonları: `FieldTemplates.ListAsync()`, `FieldTemplates.GetAsync()`, `FieldTemplates.PreviewLayoutAsync(id, files, onAnchorMiss)` (yerleşimi tam bir PDF üzerinde kuru koşumla dener, sözleşme oluşturmaz, kredi harcamaz).
- Kişiler: `Contacts.ListAsync()`, `Contacts.ListAllAsync()` (`IAsyncEnumerable`), `Contacts.CreateAsync()`.
- Raporlar: `Reports.GetAsync()` (sözleşme durumlarının toplu sayımı, parametre almaz).
- Zaman damgası listesi ve detayı: `Timestamps.ListAsync()`, `Timestamps.GetAsync()`.
- Toplu sözleşme oluşturma: `Demands.CreateBulkAsync()` (tek istekte en çok 10 satır).
- Çok belgeli zarf: `Demands.Documents.ListAsync/CreateAsync/UploadAsync/UpdateAsync/DeleteAsync/ReorderAsync/SetAssignmentsAsync` ve `Demands.DispatchAsync()`. `UploadAsync` `UploadEnvelopeDocumentParams` (`File`, `Title`, zorunlu `IdempotencyKey`, opsiyonel `DocKind` / `IsRequired`) alır; sunucunun 409 `IDEMPOTENT_REPLAY` yanıtı mevcut belge olarak döner. Özellik açık değilse `ENVELOPE_MULTI_DOC_DISABLED` fırlatılır, `ListAsync` boş liste döndürmez. `UpdateAsync` yalnız gönderilen bayrakları değiştirir (`bool?`), başlık-tek güncelleme `is_required` / `signature_required` değerlerini sıfırlamaz.
- Zarfta belge-özgü imzalı PDF: `Demands.GetDocumentPdfAsync(id, documentId)`.
- `Idempotency-Key` parametreleri: `Demands.CreateAsync(body, idempotencyKey)` ve `UploadDemandParams.IdempotencyKey`; `CreateTimestampParams.IdempotencyKey` zaten vardı. Anahtarla gönderilen bu yazmalar 429 sonrasında tam bir kez yeniden denenir; istek (dosya akışları dahil) tekrar için yeniden kurulur, tüketilmiş bir akış boş gönderilmez.
- `UploadDemandParams` yeni özellikler: `FieldTemplateId`, `OnAnchorMiss` (`"block"` | `"drop"`), `SendInvitations` (bu uçta varsayılan kapalı; `"sms"` SMS ve WhatsApp'ı kapsar, yalnız daraltır), `Force`; `UploadPartyInput.TemplatePartyId` (Alan Şablonu ile yüklemede her tarafta zorunlu).
- `CreateDemandRequest.DispatchNotifications` tipli modelde (sessiz taslak, sonra `DispatchAsync`).
- Hata kodu kataloğu: `ImzalaSdk.ErrorCodes.Codes` (73 kod, tek satırlık Türkçe açıklama, salt-okunur sözlük), `ErrorCodes.Describe()`, `ErrorCodes.IsKnown()`; `ImzalaError.CodeDescription`. Katalog vendorlanmış spesifikasyona karşı iki yönlü test edilir.
- İstek limiti bilgisi: `ImzalaRateLimitError.RateLimit` (`RateLimitInfo`: `RateLimit-Limit` / `-Remaining` / `-Reset` / `-Policy`; `X-RateLimit-*` okunmaz, sunucu göndermez). Sayılar invariant kültürle ayrıştırılır.
- Yerel doğrulama: API anahtarı (kurucuda) ve `Idempotency-Key` yazdırılabilir ASCII değilse istek gönderilmeden `ImzalaValidationError` (dosyadan okunan anahtarın sonundaki satır sonu tuzağı). Üretilmiş istemci başlık değerlerini denetlemeden ekliyordu: anahtardaki bir satır sonu telde ayrı bir başlık olarak gidiyordu.
- Beklemeler çağıranın `CancellationToken`'ına uyar.

### Değişti

- Yalnız düz metin taşıyan hata gövdesinde (`{"error": "metin"}`) `Code` artık `null`'dır; önce mesajın kopyasıydı.
- GET yeniden denemesinde bekleme tavanı 60 saniye: `Retry-After` daha uzunsa SDK beklemek yerine 429'u fırlatır. Anahtarlı yazma tekrarı aynı tavanı uygular.
- Üretilmiş istemcinin istek gönderilmeden fırlattığı parametre hataları (eksik zorunlu parametre, `ArgumentException`) artık `ImzalaValidationError` olarak gelir (`StatusCode` `null`, orijinal istisna `InnerException`'da).
- Dokümanlarda standart kurgusal örnek kişi (Ayşe Yılmaz, `ayse@example.com`, `+905551112233`).

### Düzeltildi

- `Timestamps.CreateAsync` dosyayı göndermiyordu: uç hem multipart hem JSON gövde kabul ettiği için üretilmiş istemci JSON'u seçiyor ve boş bir gövde gidiyordu. Özellik yayınlanmış sürümlerde de çalışmıyordu. Artık multipart gönderilir; gerçek istemciyle test kilitli.
- Hata kodu çıkarma: `{"error": "metin", "code": "KOD"}` gövdesinde kod artık `code` alanından okunur; önce metin kodun yerine geçiyordu.
- `Retry-After` `NaN`, sonsuz veya negatif geldiğinde yok sayılır (`NaN` hemen tekrarlıyor, x64'te taşan değer 60 sn tavanını aşabiliyordu); HTTP tarihi biçimi desteklenir (asla negatif olmaz).
- Taraf JSON'unda Türkçe karakterler artık kaçışsız gider (`UnicodeRanges.All`; HTML'e duyarlı karakterler kaçışlı kalır).
- `Templates.DeleteAsync` dokümanı: kayıt 30 gün saklanır; aktif sözleşmesi olan şablon silinemez (`409 TEMPLATE_IN_USE`).
- `MeAsync` dokümanı: hiçbir kapsam (scope) istemez. `Imzala` sınıfı örneği derlenir.

### Üretilmiş istemciyi doğrudan kullananlar için

Bu sürümde `ImzalaApiClient` altındaki sınıflar API `1.8.17` spesifikasyonundan yeniden üretildi. `CreateDemandRequest` kurucusuna `idempotencyKey` ve `force` parametreleri `description` ile `partyMapping` **arasına**, `dispatchNotifications` ise `hasTimestamp`'ten sonra girdi. Kurucuyu **konumsal** argümanlarla çağıran kod ya derlenmez ya da aynı tipli komşu argümanlar kayarak yanlış alana gider. İsimli argümanlara geçin ya da facade kullanın; facade isimli argümanlarla çağırır ve her yuva testle kilitlidir.

`ImzalaApiClient.Client.RetryConfiguration` statik Polly politikasını ayarlamayın: doluysa üretilmiş istemci her isteği taşıma katmanında tekrarlar ve yazmalar SDK'dan habersiz yinelenir. SDK bu politikayı hiç ayarlamaz.
