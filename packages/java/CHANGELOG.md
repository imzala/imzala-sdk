# Değişiklik günlüğü

Bu paket [Semantic Versioning](https://semver.org/lang/tr/) izler. API sürüm uyumu için README'deki "Sürüm uyumu" tablosuna bakın.

## 1.0.0

İlk kararlı sürüm; semver sözü bu sürümle başlar. Konuştuğu API: v1 `1.9.1`.

**0.x'ten yükseltme, facade (`org.imzala.Imzala` ve kaynak sınıfları) kullananlar için kod değişikliği gerektirmez.** 0.x'in kapsadığı uçların istek ve yanıt gövdeleri aynıdır; 1.0.0 eksik yüzeyi tamamlar ve aşağıdaki hataları düzeltir. Üretilmiş istemciyi doğrudan çağıranlar için aşağıdaki nota bakın.

### API 1.8.18 - 1.9.1 ile gelenler

- API 1.9.1: sözleşme süresi ve yenileme takibi. Sözleşme oluşturma gövdesinde isteğe bağlı `term_start_mode`, `term_start_date`, `term_duration_months`, `term_fixed_end_date`, `renewal_type`, `renewal_period_months`, `notice_days`, `reminder_offsets` ve `notify_counterparty`; gönderilmeyen alanlar için şablonun takip ayarı kullanılır. Sözleşme detayında `term` ve `archived_at`, listede `term_end_date` ve `archived_at` alanları. `term` platformun takip kaydıdır; sözleşmenin hukuken yenilendiğini veya sona erdiğini belirtmez.
- API 1.9.1: arşivleme. Arşiv sözleşmenin durumunu değiştirmez; arşivdeki sözleşme değiştirilemez ve silinemez (`DEMAND_ARCHIVED`). Liste ucunda `archived` filtresi; verilmezse eskisi gibi arşivli ve arşivsiz tüm sözleşmeler döner.
- API 1.9.1: yeni webhook olayları `contract.expiring`, `contract.ended` ve `contract.advanced` (ortak veri: `WebhookDataContractTerm`). Bunlar da takip kaydıdır.
- Facade yardımcıları: `demands().updateTerm(UUID, ContractTermInput)` (`PATCH /api/v1/demands/{id}/term`, kısmi güncelleme: açıkça `null` verilen alan temizlenir, hiç set edilmeyen gönderilmez), `demands().archive(UUID)`, `demands().unarchive(UUID)` ve `ListDemandsParams.archived(String)` (`"exclude"` / `"only"` / `"include"`). Yazmalar otomatik yeniden denenmez.
- Üretilmiş istemciyi doğrudan çağıranlar için: `DemandsApi.apiV1DemandsGet` sona eklenen `archived` parametresini alır (bir argüman fazla). Facade (`demands().list`) etkilenmez.
- Hata kataloğuna 4 yeni kod eklendi: `TERM_INVALID`, `DEMAND_ARCHIVED`, `DEMAND_NOT_ARCHIVABLE`, `DEMAND_REJECTED_CANCEL_FIRST`. `TERM_INVALID` yanıt gövdesindeki `field` alanı reddedilen anahtarı gösterir.
- Gömülü imza ucunun açıklaması güncellendi: bu akışla atılan dijital imza 5070 sayılı Kanun m.3 anlamında elektronik imzadır; ek doğrulama adımları delil değerini güçlendirir, imza seviyesini değiştirmez.
- Kaşe alanını doldurma ucu için facade yardımcısı: `demands().updateStamp(UUID, int, PatchStampItemRequest)` (`PATCH /api/v1/demands/{id}/items/{itemId}/stamp`). PATCH, otomatik yeniden denenmez.
- API 1.8.23: örnek verilerdeki gerçek alan adlı e-posta adresleri ve standart dışı telefon numaraları kurgusal değerlerle değiştirildi (sözleşme değişmedi).
- API 1.8.19 - 1.8.22: yeni uç `PATCH /api/v1/demands/{id}/items/{itemId}/stamp` (kaşe alanını doldurma); `variables`, `party_mapping[].variables` ve `document_variables` değerlerinde kaşe için yapılandırılmış `StampData` nesnesi; belge başına `document_variables`; yanıtta `stamps_applied`; `allowed_signature_variants` için `phone_draw` (yalnız tek başına) ve zaman akışında `MOBILE_SIGNATURE_CAPTURED` olayı. Alanlar şimdilik üretilmiş istemci modelleri üzerinden kullanılır; uç için facade yardımcısı üstteki maddede.
- Hata kataloğuna 10 yeni kod eklendi: `SIGNATURE_VARIANTS_PHONE_DRAW_EXCLUSIVE`, `INVALID_VARIABLES`, `INVALID_DOCUMENT_VARIABLES`, `INVALID_STAMP_DATA`, `INVALID_ITEM_ID`, `ITEM_NOT_FOUND`, `NOT_A_STAMP_ITEM`, `PARTY_NOT_ASSIGNED_TO_DOCUMENT`, `DEMAND_PARTIALLY_SIGNED`, `FIRST_SIGNATURE_OTP_UNSUPPORTED`.
- API 1.8.18: tek sözleşme isteğinde `allowed_signature_variants` ve toplu istekte `options.allowed_signature_variants`. Sıra korunur; `null` veya atlanan alan sunucu varsayılanını devralır. Yalnız `phone` seçimi `SIGNATURE_VARIANTS_PHONE_ONLY` ile reddedilir.
- Üretilmiş istemci modelleri ve hata kataloğu aynı sözleşmeyle güncellendi.

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

Bu sürümde `org.imzala.client.generated` altındaki sınıflar API `1.8.17` spesifikasyonundan yeniden üretildi (paketin önceki yayınından bu yana ilk yeniden üretim). İki imza değişti:

- `DemandsApi.apiV1DemandsPost(CreateDemandRequest)` tek argümanlı biçim kalktı; yeni imza `apiV1DemandsPost(CreateDemandRequest, String idempotencyKey)` (anahtar için `null` geçilebilir).
- `DemandsApi.apiV1DemandsUploadPost(...)` 5 argümandan 10 argümana çıktı ve opsiyonel `idempotencyKey` mevcut parametrelerin **arasına** girdi: `files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss`. Eski çağrılar derlenmez; aynı tipli komşu argümanlar kaydırılırsa derlenir ama yanlış alana gider.

Facade kullanan kod etkilenmez; facade her yuvayı etiketli geçer ve testle kilitler.
