# Değişiklik günlüğü

Bu paket [Semantic Versioning](https://semver.org/lang/tr/) izler. API sürüm uyumu için README'deki "Sürüm uyumu" tablosuna bakın.

## 1.0.0

İlk kararlı sürüm; semver sözü bu sürümle başlar. Konuştuğu API: v1 `1.8.17`.

**0.x'ten yükseltme, facade (`ImzalaClient` ve kaynak sınıfları) kullananlar için kod değişikliği gerektirmez.** 0.x'in kapsadığı uçların istek ve yanıt gövdeleri aynıdır; 1.0.0 eksik yüzeyi tamamlar ve aşağıdaki hataları düzeltir. Üretilmiş istemciyi doğrudan çağıranlar için aşağıdaki nota bakın.

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
