using System.Collections.ObjectModel;

namespace ImzalaSdk;

/// <summary>
/// Machine-readable error codes returned by the imzala.org External API, each
/// with a one-line Turkish explanation taken from the public API reference.
///
/// <see cref="ImzalaError.Code"/> carries the raw code and
/// <see cref="ImzalaError.CodeDescription"/> the matching line from here. A code
/// that is not in this catalogue is still thrown unchanged, only without a
/// description, so a newer server never breaks an older SDK.
///
/// Several different limits answer with HTTP 429, each with its own code
/// (<c>RATE_LIMIT_EXCEEDED</c>, <c>TOO_MANY_REQUESTS</c>, <c>RATE_LIMITED</c>, ...):
/// branch on <see cref="ImzalaRateLimitError"/> or on the status code, not on one
/// particular code.
///
/// Source: spec/openapi.v1.yaml, identical to the Node SDK catalogue
/// (packages/node/src/errorCodes.ts). <c>ErrorCodesTests</c> fails when the spec
/// gains a code this class does not list, loses one it does, or the two
/// catalogues drift apart.
/// </summary>
public static class ErrorCodes
{
    /// <summary>Code to one-line Turkish description. Read-only.</summary>
    public static IReadOnlyDictionary<string, string> Codes { get; } = new ReadOnlyDictionary<string, string>(
        new Dictionary<string, string>(StringComparer.Ordinal)
        {
            // İstek limiti ve tekrar koruması
            ["RATE_LIMIT_EXCEEDED"] =
                "API anahtarının dakikalık istek sınırı aşıldı (genel sınır varsayılan 60, zaman damgası isteklerinde 10). Retry-After kadar bekleyip tekrar deneyin.",
            ["TOO_MANY_REQUESTS"] =
                "Uca özgü istek sınırı aşıldı (ör. belge yüklemede dakikada 30, davet tekrarı ve zarf gönderiminde saatte 300).",
            ["RECIPIENT_RESEND_LIMIT"] =
                "Aynı alıcıya davet tekrarı sınırı aşıldı (saatte 3, günde 10).",
            ["RATE_LIMITED"] =
                "Uca özgü sıklık sınırı aşıldı (ör. aynı sözleşmeye 5 dakika içinde ikinci hatırlatma). Hatırlatmada force: true ile aşılabilir.",
            ["RATE_LIMITER_UNAVAILABLE"] =
                "İstek sınırlayıcı geçici olarak kullanılamıyor; kısa süre sonra tekrar deneyin.",
            ["IDEMPOTENCY_KEY_REUSED"] =
                "Aynı Idempotency-Key daha önce farklı bir içerikle kullanıldı. Yeni sözleşme için yeni bir anahtar gönderin.",
            ["IDEMPOTENCY_UNVERIFIABLE"] =
                "Bu Idempotency-Key daha önce bir sözleşme üretti ama isteğin aynı olduğu doğrulanamadı. Yeni anahtarla körlemesine denemeyin, gövdedeki demand_id ile durumu sorgulayın.",
            ["INVALID_IDEMPOTENCY_KEY"] =
                "Idempotency-Key biçimi geçersiz ya da başlık ile gövdedeki anahtar çelişiyor.",
            ["DUPLICATE_SUSPECTED"] =
                "Anahtarsız istek, son 10 dakikada aynı içerikle gönderilmiş bir sözleşmeyle eşleşti. Bilerek tekrarlamak için force gönderin.",
            ["IDEMPOTENT_REPLAY"] =
                "Aynı idempotency_key ile belge daha önce yüklenmiş; yeni belge oluşturulmadı, gövdede mevcut belge döner.",
            // Yetki, çalışma alanı, kredi
            ["UNAUTHORIZED"] =
                "Kimlik doğrulama gerekli; API anahtarı eksik veya geçersiz.",
            ["INSUFFICIENT_SCOPE"] =
                "API anahtarında bu işlem için gereken yetki (scope) yok.",
            ["WORKSPACE_MISMATCH"] =
                "X-Workspace-Id, API anahtarının bağlı olduğu organizasyonla uyuşmuyor.",
            ["SMS_CUSTOMIZATION_NOT_ALLOWED"] =
                "sms_content yalnız uygun planda ve kendi SMS sağlayıcı ayarı tanımlı organizasyonlarda kullanılabilir.",
            ["INSUFFICIENT_CREDITS"] =
                "Hesabın veya organizasyonun kredisi bu işlem için yetmiyor.",
            ["MEMBER_LIMIT_EXCEEDED"] =
                "Organizasyon üyesinin aylık kredi limiti aşıldı (kredi havuzunda bakiye olsa bile).",
            // Şablon ve alan şablonu
            ["TEMPLATE_NOT_FOUND"] =
                "Şablon bulunamadı, size ait değil ya da bir Alan Şablonu değil.",
            ["TEMPLATE_IN_USE"] =
                "Şablonun aktif (taslak veya imza bekleyen) sözleşmesi olduğu için silinemez.",
            ["NOT_A_FIELD_LAYOUT_TEMPLATE"] =
                "Verilen şablon bir Alan Şablonu değil.",
            ["FIELD_LAYOUT_TEMPLATE_NOT_SENDABLE"] =
                "Verilen kimlik bir Alan Şablonuna ait; Alan Şablonundan doğrudan sözleşme gönderilemez.",
            ["FIELD_TEMPLATE_CONFLICT"] =
                "field_template_id ile template_id birlikte gönderilemez.",
            ["FIELD_LAYOUT_UNRESOLVED"] =
                "Alan yerleşimi bu belgeye uygulanamadı; sözleşme oluşturulmadı ve kredi düşülmedi.",
            ["INVALID_ON_ANCHOR_MISS"] =
                "on_anchor_miss değeri geçersiz.",
            // Şablon belge seçimi
            ["SIGNATURE_VARIANTS_PHONE_ONLY"] = "İmza yöntemi listesi yalnız phone içeremez; draw, type veya upload yöntemlerinden en az biri gereklidir.",
            ["SIGNATURE_VARIANTS_PHONE_DRAW_EXCLUSIVE"] = "phone_draw yalnız tek başına kullanılabilir; başka bir imza yöntemiyle birlikte gönderilemez.",
            ["INVALID_VARIABLES"] = "variables değeri geçersiz; details.reason alanı nedeni verir: object_value_for_non_stamp (kaşe olmayan alana nesne değer), unsupported_endpoint (toplu uçlar nesne değer desteklemez).",
            ["INVALID_DOCUMENT_VARIABLES"] = "document_variables geçersiz; details.reason alanı nedeni verir: shape (biçim hatası), unknown_document (kimlik bu şablonun belgesi değil), unselected_document (belge bu istekte gönderilmiyor), unsupported_endpoint (bu uç desteklemiyor).",
            ["INVALID_STAMP_DATA"] = "Kaşe verisi geçersiz: bilinmeyen alan veya metin olmayan değer içeriyor.",
            ["INVALID_ITEM_ID"] = "Alan kimliği (itemId) pozitif bir tam sayı değil.",
            ["ITEM_NOT_FOUND"] = "Alan bu sözleşmede (veya verilen document_id belgesinde) bulunamadı.",
            ["NOT_A_STAMP_ITEM"] = "Verilen alan bir kaşe alanı değil.",
            ["PARTY_NOT_ASSIGNED_TO_DOCUMENT"] = "Kaşeyi dolduracak taraf, kaşenin bulunduğu belgeye atanmamış.",
            ["DEMAND_PARTIALLY_SIGNED"] = "Taraflardan biri imzaladı veya karar verdi; bu işlem artık yapılamaz.",
            ["FIRST_SIGNATURE_OTP_UNSUPPORTED"] = "İlk imzada SMS kodu doğrulaması bu taraf veya akış için desteklenmiyor (nitelikli e-imza, onaycı ve kendi kendine imza akışları).",
            ["INVALID_DOCUMENT_SELECTION"] =
                "Belge seçimi geçersiz; details.reason alanı nedeni verir: shape (biçim hatası), unknown_document (kimlik bu şablonun belgesi değil), conflict (aynı kimlik iki listede), empty (seçim sonucunda belge kalmadı).",
            ["PARTY_WITHOUT_DOCUMENTS"] =
                "Eşlenen bir tarafa imzalayacak belge düşmüyor. Seçimi değiştirin, o rolü eşlemeden çıkarın ya da dispatch_notifications: false ile oluşturup atamaları düzelttikten sonra gönderin.",
            ["TEMPLATE_DOCUMENTS_NOT_READY"] =
                "Şablonun belge yapısı henüz hazır değil; documents göndermeden deneyin.",
            ["DOCUMENT_SOURCE_UNAVAILABLE"] =
                "Seçim sonrasında kalan ilk belgenin kaynak dosyası yok; ilk belgeyi de gönderin ya da o belgeye dosya yükleyin.",
            // Sözleşme durumu
            ["DEMAND_NOT_FOUND"] =
                "Sözleşme bu çalışma alanında bulunamadı.",
            ["DEMAND_NOT_EDITABLE"] =
                "Sözleşme düzenlemeye açık değil (ör. tamamlanmış, reddedilmiş, iptal edilmiş, süresi geçmiş veya bulunamadı).",
            ["DEMAND_COMPLETED"] =
                "Tamamlanmış sözleşme API üzerinden silinemez.",
            ["DEMAND_NOT_DISPATCHED"] =
                "Sözleşme henüz imzaya gönderilmedi (taslak).",
            ["DEMAND_NOT_DISPATCHABLE"] =
                "Sözleşme tamamlanmış veya iptal edilmiş; tekrar gönderilemez.",
            ["DEMAND_EXPIRED"] =
                "Sözleşmenin imza süresi geçmiş.",
            ["ALREADY_COMPLETED"] =
                "Tüm taraflar imzalamış; hatırlatılacak taraf yok.",
            ["INVALID_EXPIRY_DATE"] =
                "expiry_date çözümlenemiyor veya takvimde olmayan bir gün (ör. 2026-02-30).",
            ["INVALID_PADES_LEVEL"] =
                "options.qes_pades_level satın alınabilir seviyelerden biri değil.",
            ["DEMAND_ARCHIVED"] = "Sözleşme arşivde; değiştirilemez ve silinemez. Önce arşivden çıkarın (unarchive).",
            ["DEMAND_NOT_ARCHIVABLE"] = "Yalnız tamamlanmış, iptal edilmiş veya süresi dolmuş sözleşmeler arşivlenebilir.",
            ["DEMAND_REJECTED_CANCEL_FIRST"] = "Sözleşme reddedilmiş ve imza bekliyor; arşivlemeden önce iptal edin (cancel).",
            ["TERM_INVALID"] = "Süre veya yenileme takibi alanlarında geçersiz ya da çelişkili bir kombinasyon var; field alanı reddedilen anahtarı gösterir.",
            ["BULK_MAX_10"] =
                "Toplu oluşturmada rows en fazla 10 satır olabilir.",
            // Gönderim ve davet
            ["DISPATCH_NO_PARTIES"] =
                "Sözleşmede imzacı taraf yok; yayına alınmadı ve kredi düşülmedi.",
            ["DISPATCH_TOO_MANY"] =
                "Taraf sayısı 20 sınırını aşıyor. Gönderim ucunda istek yan etkisiz reddedilir (yayına geçmez, kredi düşmez); oluşturma yanıtında davetlerin gönderilmediğini bildirir.",
            ["INVALID_SEND_INVITATIONS"] =
                "send_invitations değeri tanınmıyor.",
            ["RECONCILE_FAILED"] =
                "Kredi mutabakatı beklenmedik bir hatayla düştü; sözleşme yayına geçmedi, istek güvenle tekrarlanabilir.",
            ["INVALID_CHANNELS"] =
                "channels boş ya da email/sms dışında bir değer içeriyor.",
            ["MAX_SMS_REMINDERS_REACHED"] =
                "Sözleşme için SMS hatırlatma üst sınırına ulaşıldı.",
            ["ENVELOPE_NOT_DISPATCHED"] =
                "Çok belgeli zarf henüz gönderilmedi; önce POST /demands/{id}/dispatch çağırın.",
            ["ENVELOPE_CHANGED_DURING_DISPATCH"] =
                "Kredi mutabakatı ile gönderim arasında zarf değiştirildi. Sözleşme yayına geçmedi; istek güvenle tekrarlanabilir.",
            // Zarf belgeleri
            ["ENVELOPE_MULTI_DOC_DISABLED"] =
                "Çok belgeli zarf özelliği bu hesap için henüz açık değil.",
            ["SIGNING_ALREADY_STARTED"] =
                "İmza süreci başlamış zarfın belge listesi değiştirilemez.",
            ["ENVELOPE_ALREADY_DISPATCHED"] =
                "Sözleşme gönderilmiş; gönderilmiş bir zarfın belge listesi değiştirilemez.",
            ["DOCUMENT_LIMIT_EXCEEDED"] =
                "Zarf başına en fazla 20 belge eklenebilir.",
            ["QES_NOT_SUPPORTED_MULTI_DOCUMENT"] =
                "Nitelikli elektronik imza birden çok belge içeren zarflarda henüz desteklenmiyor.",
            ["CANNOT_DELETE_LAST_DOCUMENT"] =
                "Zarftaki son belge silinemez.",
            ["DOCUMENT_HAS_SIGNED_CONTENT"] =
                "İmza veya içerik girilmiş belge silinemez.",
            ["DOCUMENT_HAS_DECISIONS"] =
                "Bu belge için onay veya red kararı verilmiş; silinemez.",
            ["ORDER_SET_MISMATCH"] =
                "Gönderilen belge kimlikleri zarftaki belgelerle birebir eşleşmiyor.",
            ["INVALID_DOC_KIND"] =
                "doc_kind değeri geçersiz.",
            ["CONSENT_CANNOT_BE_REQUIRED"] =
                "KVKK açık rıza belgesi zorunlu olarak işaretlenemez; açık rıza reddedilebilir olmalıdır.",
            ["PREINFO_MUST_BE_REQUIRED"] =
                "Ön bilgilendirme belgesi zorunlu olarak işaretlenmelidir.",
            ["ASSIGNMENT_EMPTY"] =
                "party_ids eksik veya boş.",
            ["ASSIGNMENT_HAS_DECISION"] =
                "Karar vermiş bir imzacının ataması kaldırılamaz.",
            // Alan yerleştirme
            ["INVALID_ITEMS_BODY"] =
                "items bir dizi değil.",
            ["PAGE_ID_REQUIRED"] =
                "Bir öğede tam sayı page_id yok.",
            ["INVALID_PAGE_ID"] =
                "page_id bu sözleşmeye ait değil.",
            ["INVALID_PARTY_ID"] =
                "party_id bu sözleşmeye ait değil.",
            ["INVALID_ITEM_TYPE"] =
                "item_type desteklenmiyor; hata mesajı izinli türleri listeler.",
            ["DUPLICATE_SIGNATURE_FIELD"] =
                "Aynı sayfa, taraf ve konumda ikinci bir imza alanı oluşturulamaz.",
            // Kişiler
            ["CONTACT_DUPLICATE"] =
                "Çalışma alanında aynı e-posta veya telefona sahip aktif bir kişi zaten var.",
            // Dosya ve zaman damgası
            ["FILE_REQUIRED"] =
                "İstekte dosya yok.",
            ["FILE_TOO_LARGE"] =
                "Dosya boyut sınırını aşıyor.",
            ["UNSUPPORTED_FILE_TYPE"] =
                "Dosya türü bu uçta desteklenmiyor.",
            ["UNSUPPORTED_MIME_TYPE"] =
                "Dosyanın MIME türü zaman damgası için izin verilenler arasında değil.",
            ["ENCRYPTED_PDF"] =
                "PDF parola korumalı; şifresiz bir kopya yükleyin.",
            ["IMAGE_DECODE_FAILED"] =
                "Görsel çözümlenemedi.",
            ["TEXT_EXTRACTION_TIMEOUT"] =
                "Belgeden metin çıkarma süre sınırını aştı.",
            ["BAD_BASE64"] =
                "file_base64 geçerli standart Base64 değil.",
            ["STAMP_INVALID"] =
                "Zaman damgası sunucusu geçersiz bir yanıt döndürdü.",
            ["TSA_UNAVAILABLE"] =
                "Zaman damgası servisi geçici olarak erişilemiyor; kısa süre sonra tekrar deneyin.",
            // Genel
            ["VALIDATION_ERROR"] =
                "İstek doğrulamadan geçmedi (ör. konum sınırları, slug biçimi, eksik taraf).",
            ["VALIDATION_FAIL"] =
                "Zorunlu bir alan eksik veya geçersiz (ör. boş başlık).",
            ["INVALID_PAGE"] =
                "page 1 veya daha büyük bir tam sayı değil; limit hata vermez, 1 ile 100 arasına kırpılır.",
            ["INTERNAL_ERROR"] =
                "Sunucu tarafında beklenmeyen bir hata oluştu.",
        });

    /// <summary>Whether <paramref name="code"/> is in the catalogue. <c>null</c> is never known.</summary>
    public static bool IsKnown(string? code) => code != null && Codes.ContainsKey(code);

    /// <summary>The description for <paramref name="code"/>, or <c>null</c> for an unknown or missing code.</summary>
    public static string? Describe(string? code) =>
        code != null && Codes.TryGetValue(code, out var description) ? description : null;
}
