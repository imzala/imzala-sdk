# Değişiklik günlüğü

Bu paket [Semantic Versioning](https://semver.org/lang/tr/) izler. API sürüm uyumu için README'deki "Sürüm uyumu" tablosuna bakın.

## 1.0.0

İlk kararlı sürüm; semver sözü bu sürümle başlar. Konuştuğu API: v1 `1.8.14`.

**0.x'ten yükseltme, facade (`org.imzala.Imzala` ve kaynak sınıfları) kullananlar için kod değişikliği gerektirmez.** 0.x'in kapsadığı uçların istek ve yanıt gövdeleri aynıdır; 1.0.0 eksik yüzeyi tamamlar ve aşağıdaki hataları düzeltir. Üretilmiş istemciyi doğrudan çağıranlar için aşağıdaki nota bakın.

### Eklendi

- Alan Şablonları: `fieldTemplates().list()`, `fieldTemplates().get()`, `fieldTemplates().previewLayout(id, files, onAnchorMiss)` (yerleşimi tam bir PDF üzerinde kuru koşumla dener, sözleşme oluşturmaz, kredi harcamaz).
- Kişiler: `contacts().list()`, `contacts().listAll()` (`Iterable`), `contacts().create()`; `ListContactsParams` fluent builder.
- Raporlar: `reports().get()` (sözleşme durumlarının toplu sayımı, parametre almaz).
- Zaman damgası listesi ve detayı: `timestamps().list()`, `timestamps().get()`; `ListTimestampsParams` fluent builder.
- Toplu sözleşme oluşturma: `demands().createBulk()` (tek istekte en çok 10 satır).
- Çok belgeli zarf: `demands().documents().list/create/upload/update/delete/reorder/setAssignments` ve `demands().dispatch()`. `upload` `UploadEnvelopeDocumentParams(file, title, idempotencyKey)` alır, anahtar zorunludur; sunucunun 409 `IDEMPOTENT_REPLAY` yanıtı mevcut belge olarak döner. Özellik açık değilse `ENVELOPE_MULTI_DOC_DISABLED` fırlatılır, `list` boş liste döndürmez.
- Zarfta belge-özgü imzalı PDF: `demands().getDocumentPdf(id, documentId)`.
- `Idempotency-Key` parametreleri: `demands().create(body, idempotencyKey)` ve `UploadDemandParams.idempotencyKey()`; `CreateTimestampParams.idempotencyKey()` zaten vardı. Anahtarla gönderilen bu yazmalar 429 sonrasında tam bir kez yeniden denenir.
- `UploadDemandParams` yeni seçenekler: `fieldTemplateId(UUID)`, `onAnchorMiss(String)` (`"block"` | `"drop"`), `sendInvitations(String)` (bu uçta varsayılan kapalı; `"sms"` SMS ve WhatsApp'ı kapsar, yalnız daraltır), `force(boolean)`; `UploadPartyInput` beş parametreli kurucu ile `templatePartyId` (Alan Şablonu ile yüklemede her tarafta zorunlu).
- `CreateDemandRequest.dispatchNotifications` tipli modelde (sessiz taslak, sonra `dispatch`).
- Hata kodu kataloğu: `org.imzala.ErrorCodes.CODES` (73 kod, tek satırlık Türkçe açıklama, salt-okunur `Map`), `ErrorCodes.describe()`, `ErrorCodes.isKnown()`; `ImzalaException.getCodeDescription()`. Katalog vendorlanmış spesifikasyona karşı iki yönlü test edilir.
- İstek limiti bilgisi: `ImzalaRateLimitException.getRateLimit()` (`RateLimitInfo`: `RateLimit-Limit` / `-Remaining` / `-Reset` / `-Policy`; `X-RateLimit-*` okunmaz, sunucu göndermez).
- Yerel doğrulama: API anahtarı (kurucuda) ve `Idempotency-Key` yazdırılabilir ASCII değilse istek gönderilmeden `ImzalaValidationException` (dosyadan okunan anahtarın sonundaki satır sonu tuzağı). Önce JDK `HttpRequest` kurucusunun ham `IllegalArgumentException`'ı facade'dan kaçıyordu.
- `RetryConfig` üzerinden enjekte edilebilir bekleme fonksiyonu (testler uyumaz).

### Değişti

- Yalnız düz metin taşıyan hata gövdesinde (`{"error": "metin"}`) `getCode()` artık `null` döner; önce mesajın kopyasıydı.
- GET yeniden denemesinde bekleme tavanı 60 saniye: `Retry-After` daha uzunsa SDK beklemek yerine 429'u fırlatır. Anahtarlı yazma tekrarı aynı tavanı uygular.
- Üretilmiş istemcinin istek gönderilmeden fırlattığı parametre hataları artık `ImzalaValidationException` olarak gelir (`getStatusCode()` `null`, orijinal istisna `getCause()`'da).
- Ağ hatasında (HTTP alışverişi olmadan) `getStatusCode()` dokümandaki gibi `null` döner; önce `0` dönüyordu.
- Dokümanlarda standart kurgusal örnek kişi (Ayşe Yılmaz, `ayse@example.com`, `+905551112233`).

### Düzeltildi

- Multipart metin alanları UTF-8 gönderilir. Üretilmiş istemci metin parçalarını ISO-8859-1 ile yazıyordu: "Kira sözleşmesi" başlığı sunucuya "Kira s?zle?mesi" olarak ulaşıyordu; açıklama ve taraf adları (belge yükleme, zaman damgası, zarf belgesi yükleme) aynı şekilde bozuluyordu. Yayınlanmış sürümlerde de vardı.
- Hata kodu çıkarma: `{"error": "metin", "code": "KOD"}` gövdesinde kod artık `code` alanından okunur; önce metin kodun yerine geçiyordu.
- `Retry-After` `NaN`, sonsuz veya negatif geldiğinde yok sayılır (`NaN` hemen tekrarlıyor, taşan değer 60 sn tavanını aşabiliyordu); HTTP tarihi biçimi desteklenir.
- `templates().delete()` dokümanı: kayıt 30 gün saklanır; aktif sözleşmesi olan şablon silinemez (`409 TEMPLATE_IN_USE`).
- `me()` dokümanı: hiçbir kapsam (scope) istemez (önce `timestamps` kapsamı gerektiği yazıyordu).
- `previewLayout` dokümanı: `onAnchorMiss` verilmezse sunucu `block` uygular (şablonun kendi ayarı değil). Zaman damgası listesinde `limit` 10..100 belgelendi.

### Üretilmiş istemciyi doğrudan kullananlar için

Bu sürümde `org.imzala.client.generated` altındaki sınıflar API `1.8.14` spesifikasyonundan yeniden üretildi (paketin önceki yayınından bu yana ilk yeniden üretim). İki imza değişti:

- `DemandsApi.apiV1DemandsPost(CreateDemandRequest)` tek argümanlı biçim kalktı; yeni imza `apiV1DemandsPost(CreateDemandRequest, String idempotencyKey)` (anahtar için `null` geçilebilir).
- `DemandsApi.apiV1DemandsUploadPost(...)` 5 argümandan 10 argümana çıktı ve opsiyonel `idempotencyKey` mevcut parametrelerin **arasına** girdi: `files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss`. Eski çağrılar derlenmez; aynı tipli komşu argümanlar kaydırılırsa derlenir ama yanlış alana gider.

Facade kullanan kod etkilenmez; facade her yuvayı etiketli geçer ve testle kilitler.
