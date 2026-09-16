# Değişiklik günlüğü

Bu paket [Semantic Versioning](https://semver.org/lang/tr/) izler. API sürüm uyumu için README'deki "Sürüm uyumu" tablosuna bakın.

## 1.0.0

İlk kararlı sürüm; semver sözü bu sürümle başlar. Konuştuğu API: v1 `1.8.17`.

**0.x'ten yükseltme, facade (`ImzalaSdk.Imzala` ve kaynak sınıfları) kullananlar için kod değişikliği gerektirmez.** 0.x'in kapsadığı uçların istek ve yanıt gövdeleri aynıdır; 1.0.0 eksik yüzeyi tamamlar ve aşağıdaki hataları düzeltir. Üretilmiş istemciyi doğrudan çağıranlar için aşağıdaki nota bakın.

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
