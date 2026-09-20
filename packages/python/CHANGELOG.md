# Değişiklik günlüğü

Bu paket [Semantic Versioning](https://semver.org/lang/tr/) izler. API sürüm uyumu için README'deki "Sürüm uyumu" tablosuna bakın.

## Yayınlanmamış

- API 1.8.18: tek sözleşme isteğinde `allowed_signature_variants` ve toplu istekte `options.allowed_signature_variants`. Sıra korunur; `null` veya atlanan alan sunucu varsayılanını devralır. Yalnız `phone` seçimi `SIGNATURE_VARIANTS_PHONE_ONLY` ile reddedilir.
- Üretilmiş istemci modelleri ve hata kataloğu aynı sözleşmeyle güncellendi.

## 1.0.0

İlk kararlı sürüm; semver sözü bu sürümle başlar. Konuştuğu API: v1 `1.8.17`.

**0.x'ten yükseltme kod değişikliği gerektirmez.** 0.x'in kapsadığı uçların istek ve yanıt gövdeleri aynıdır; 1.0.0 eksik yüzeyi tamamlar ve aşağıdaki hataları düzeltir.

### Eklendi

- Alan Şablonları: `field_templates.list()`, `field_templates.get()`, `field_templates.preview_layout(id, files=..., on_anchor_miss=...)` (yerleşimi tam bir PDF üzerinde kuru koşumla dener, sözleşme oluşturmaz, kredi harcamaz).
- Kişiler: `contacts.list()`, `contacts.list_all()` (iterator), `contacts.create()`.
- Raporlar: `reports.get()` (sözleşme durumlarının toplu sayımı, parametre almaz).
- Zaman damgası listesi ve detayı: `timestamps.list()`, `timestamps.get()`.
- Toplu sözleşme oluşturma: `demands.create_bulk()` (tek istekte en çok 10 satır).
- Çok belgeli zarf: `demands.documents.list/create/upload/update/delete/reorder/set_assignments` ve `demands.dispatch()`. `upload` zorunlu `idempotency_key` alır; sunucunun 409 `IDEMPOTENT_REPLAY` yanıtı mevcut belge olarak döner. Özellik açık değilse `ENVELOPE_MULTI_DOC_DISABLED` fırlatılır, `list` boş liste döndürmez.
- Zarfta belge-özgü imzalı PDF: `demands.get_document_pdf(demand_id, document_id)`.
- `Idempotency-Key` parametreleri: `demands.create(body, idempotency_key=...)` ve `upload_document(idempotency_key=...)`; `timestamps.create` zaten alıyordu. Anahtarla gönderilen bu yazmalar 429 sonrasında tam bir kez yeniden denenir.
- `upload_document` yeni alanlar: `field_template_id`, `on_anchor_miss` (`"block"` | `"drop"`), `send_invitations` (bu uçta varsayılan kapalı; `"sms"` SMS ve WhatsApp'ı kapsar, yalnız daraltır), `force`; `UploadPartyInput.template_party_id` (Alan Şablonu ile yüklemede her tarafta zorunlu; önce tipli taraflarla ifade edilemiyordu).
- `CreateDemandRequest.dispatch_notifications` tipli modelde (sessiz taslak, sonra `dispatch`).
- Hata kodu kataloğu: `IMZALA_ERROR_CODES` (73 kod, tek satırlık Türkçe açıklama), `describe_error_code()`, `is_known_error_code()`; `ImzalaError.code_description`. Katalog vendorlanmış spesifikasyona karşı iki yönlü test edilir.
- İstek limiti bilgisi: `ImzalaRateLimitError.rate_limit` (`ImzalaRateLimitInfo`: `RateLimit-Limit` / `-Remaining` / `-Reset` / `-Policy`; `X-RateLimit-*` okunmaz, sunucu göndermez).
- Yerel doğrulama: API anahtarı (kurucuda) ve `Idempotency-Key` yazdırılabilir ASCII değilse istek gönderilmeden `ImzalaValidationError` (dosyadan okunan anahtarın sonundaki satır sonu tuzağı). Önce ilgisiz bir istisna türüyle yerelde patlıyordu.

### Değişti

- Yalnız düz metin taşıyan hata gövdesinde (`{"error": "metin"}`) `code` artık `None`'dır; önce mesajın kopyasıydı.
- Taşıma katmanının (urllib3) kendi tekrarı kapatıldı (`retries=0`). Yeniden deneme yalnız SDK'nın kurallarıyla olur: GET'ler ve anahtarlı yazmalar.
- GET yeniden denemesinde bekleme tavanı 60 saniye: `Retry-After` daha uzunsa SDK beklemek yerine 429'u fırlatır (`Retry-After` HTTP tarihi de olabildiği için saatlik sessiz bir uyku mümkündü). Anahtarlı yazma tekrarı aynı tavanı uygular.
- Dokümanlarda standart kurgusal örnek kişi (Ayşe Yılmaz, `ayse@example.com`, `+905551112233`).

### Düzeltildi

- `timestamps.create` dosyayı göndermiyordu: uç hem multipart hem JSON gövde kabul ettiği için üretilmiş istemci JSON'u seçiyor ve boş bir gövde gidiyordu. Özellik yayınlanmış sürümlerde de çalışmıyordu. Artık multipart gönderilir; gerçek istemciyle test kilitli.
- urllib3 varsayılan tekrarı 429 ve 503'te `Retry-After`'a uyup isteği, yazmalar dahil, SDK görmeden sessizce tekrarlayabiliyordu; kapatıldı (yukarıda).
- Hata kodu çıkarma: `{"error": "metin", "code": "KOD"}` gövdesinde kod artık `code` alanından okunur; önce metin kodun yerine geçiyordu. 2xx yanıtta `success: false` gövdesi de `code` ve mesaj taşır.
- `Retry-After` `NaN`, sonsuz veya negatif geldiğinde yok sayılır; HTTP tarihi biçimi desteklenir.
- `templates.delete` dokümanı: kayıt 30 gün saklanır; aktif sözleşmesi olan şablon silinemez (`409 TEMPLATE_IN_USE`).
- `me()` dokümanı: hiçbir kapsam (scope) istemez.
- Taraf JSON'unda Türkçe karakterler artık kaçışsız gider (`ensure_ascii=False`).
