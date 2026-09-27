package org.imzala;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Machine-readable error codes returned by the imzala.org External API, each
 * with a one-line Turkish explanation taken from the public API reference.
 *
 * <p>{@link ImzalaException#getCode()} carries the raw code and {@link
 * ImzalaException#getCodeDescription()} the matching line from here. A code
 * that is not in this catalogue is still thrown unchanged, only without a
 * description, so a newer server never breaks an older SDK.
 *
 * <p>Several different limits answer with HTTP 429, each with its own code
 * ({@code RATE_LIMIT_EXCEEDED}, {@code TOO_MANY_REQUESTS}, {@code
 * RATE_LIMITED}, ...): branch on {@link ImzalaRateLimitException} or on the
 * status code, not on one particular code.
 *
 * <p>Source: spec/openapi.v1.yaml, identical to the Node SDK catalogue
 * (packages/node/src/errorCodes.ts). {@code ErrorCodesTest} fails when the
 * spec gains a code this class does not list, loses one it does, or the two
 * catalogues drift apart.
 */
public final class ErrorCodes {

  /** Code to one-line Turkish description, in catalogue order. Read-only. */
  public static final Map<String, String> CODES;

  static {
    Map<String, String> m = new LinkedHashMap<>();
    // İstek limiti ve tekrar koruması
    m.put("RATE_LIMIT_EXCEEDED",
        "API anahtarının dakikalık istek sınırı aşıldı (genel sınır varsayılan 60, zaman damgası isteklerinde 10). Retry-After kadar bekleyip tekrar deneyin.");
    m.put("TOO_MANY_REQUESTS",
        "Uca özgü istek sınırı aşıldı (ör. belge yüklemede dakikada 30, davet tekrarı ve zarf gönderiminde saatte 300).");
    m.put("RECIPIENT_RESEND_LIMIT",
        "Aynı alıcıya davet tekrarı sınırı aşıldı (saatte 3, günde 10).");
    m.put("RATE_LIMITED",
        "Uca özgü sıklık sınırı aşıldı (ör. aynı sözleşmeye 5 dakika içinde ikinci hatırlatma). Hatırlatmada force: true ile aşılabilir.");
    m.put("RATE_LIMITER_UNAVAILABLE",
        "İstek sınırlayıcı geçici olarak kullanılamıyor; kısa süre sonra tekrar deneyin.");
    m.put("IDEMPOTENCY_KEY_REUSED",
        "Aynı Idempotency-Key daha önce farklı bir içerikle kullanıldı. Yeni sözleşme için yeni bir anahtar gönderin.");
    m.put("IDEMPOTENCY_UNVERIFIABLE",
        "Bu Idempotency-Key daha önce bir sözleşme üretti ama isteğin aynı olduğu doğrulanamadı. Yeni anahtarla körlemesine denemeyin, gövdedeki demand_id ile durumu sorgulayın.");
    m.put("INVALID_IDEMPOTENCY_KEY",
        "Idempotency-Key biçimi geçersiz ya da başlık ile gövdedeki anahtar çelişiyor.");
    m.put("DUPLICATE_SUSPECTED",
        "Anahtarsız istek, son 10 dakikada aynı içerikle gönderilmiş bir sözleşmeyle eşleşti. Bilerek tekrarlamak için force gönderin.");
    m.put("IDEMPOTENT_REPLAY",
        "Aynı idempotency_key ile belge daha önce yüklenmiş; yeni belge oluşturulmadı, gövdede mevcut belge döner.");
    // Yetki, çalışma alanı, kredi
    m.put("UNAUTHORIZED",
        "Kimlik doğrulama gerekli; API anahtarı eksik veya geçersiz.");
    m.put("INSUFFICIENT_SCOPE",
        "API anahtarında bu işlem için gereken yetki (scope) yok.");
    m.put("WORKSPACE_MISMATCH",
        "X-Workspace-Id, API anahtarının bağlı olduğu organizasyonla uyuşmuyor.");
    m.put("SMS_CUSTOMIZATION_NOT_ALLOWED",
        "sms_content yalnız uygun planda ve kendi SMS sağlayıcı ayarı tanımlı organizasyonlarda kullanılabilir.");
    m.put("INSUFFICIENT_CREDITS",
        "Hesabın veya organizasyonun kredisi bu işlem için yetmiyor.");
    m.put("MEMBER_LIMIT_EXCEEDED",
        "Organizasyon üyesinin aylık kredi limiti aşıldı (kredi havuzunda bakiye olsa bile).");
    // Şablon ve alan şablonu
    m.put("TEMPLATE_NOT_FOUND",
        "Şablon bulunamadı, size ait değil ya da bir Alan Şablonu değil.");
    m.put("TEMPLATE_IN_USE",
        "Şablonun aktif (taslak veya imza bekleyen) sözleşmesi olduğu için silinemez.");
    m.put("NOT_A_FIELD_LAYOUT_TEMPLATE",
        "Verilen şablon bir Alan Şablonu değil.");
    m.put("FIELD_LAYOUT_TEMPLATE_NOT_SENDABLE",
        "Verilen kimlik bir Alan Şablonuna ait; Alan Şablonundan doğrudan sözleşme gönderilemez.");
    m.put("FIELD_TEMPLATE_CONFLICT",
        "field_template_id ile template_id birlikte gönderilemez.");
    m.put("FIELD_LAYOUT_UNRESOLVED",
        "Alan yerleşimi bu belgeye uygulanamadı; sözleşme oluşturulmadı ve kredi düşülmedi.");
    m.put("INVALID_ON_ANCHOR_MISS",
        "on_anchor_miss değeri geçersiz.");
    // Şablon belge seçimi
    m.put("SIGNATURE_VARIANTS_PHONE_ONLY", "İmza yöntemi listesi yalnız phone içeremez; draw, type veya upload yöntemlerinden en az biri gereklidir.");
    m.put("SIGNATURE_VARIANTS_PHONE_DRAW_EXCLUSIVE", "phone_draw yalnız tek başına kullanılabilir; başka bir imza yöntemiyle birlikte gönderilemez.");
    m.put("INVALID_VARIABLES", "variables değeri geçersiz; details.reason alanı nedeni verir: object_value_for_non_stamp (kaşe olmayan alana nesne değer), unsupported_endpoint (toplu uçlar nesne değer desteklemez).");
    m.put("INVALID_DOCUMENT_VARIABLES", "document_variables geçersiz; details.reason alanı nedeni verir: shape (biçim hatası), unknown_document (kimlik bu şablonun belgesi değil), unselected_document (belge bu istekte gönderilmiyor), unsupported_endpoint (bu uç desteklemiyor).");
    m.put("INVALID_STAMP_DATA", "Kaşe verisi geçersiz: bilinmeyen alan veya metin olmayan değer içeriyor.");
    m.put("INVALID_ITEM_ID", "Alan kimliği (itemId) pozitif bir tam sayı değil.");
    m.put("ITEM_NOT_FOUND", "Alan bu sözleşmede (veya verilen document_id belgesinde) bulunamadı.");
    m.put("NOT_A_STAMP_ITEM", "Verilen alan bir kaşe alanı değil.");
    m.put("PARTY_NOT_ASSIGNED_TO_DOCUMENT", "Kaşeyi dolduracak taraf, kaşenin bulunduğu belgeye atanmamış.");
    m.put("DEMAND_PARTIALLY_SIGNED", "Taraflardan biri imzaladı veya karar verdi; bu işlem artık yapılamaz.");
    m.put("FIRST_SIGNATURE_OTP_UNSUPPORTED", "İlk imzada SMS kodu doğrulaması bu taraf veya akış için desteklenmiyor (nitelikli e-imza, onaycı ve kendi kendine imza akışları).");
    m.put("INVALID_DOCUMENT_SELECTION",
        "Belge seçimi geçersiz; details.reason alanı nedeni verir: shape (biçim hatası), unknown_document (kimlik bu şablonun belgesi değil), conflict (aynı kimlik iki listede), empty (seçim sonucunda belge kalmadı).");
    m.put("PARTY_WITHOUT_DOCUMENTS",
        "Eşlenen bir tarafa imzalayacak belge düşmüyor. Seçimi değiştirin, o rolü eşlemeden çıkarın ya da dispatch_notifications: false ile oluşturup atamaları düzelttikten sonra gönderin.");
    m.put("TEMPLATE_DOCUMENTS_NOT_READY",
        "Şablonun belge yapısı henüz hazır değil; documents göndermeden deneyin.");
    m.put("DOCUMENT_SOURCE_UNAVAILABLE",
        "Seçim sonrasında kalan ilk belgenin kaynak dosyası yok; ilk belgeyi de gönderin ya da o belgeye dosya yükleyin.");
    // Sözleşme durumu
    m.put("DEMAND_NOT_FOUND",
        "Sözleşme bu çalışma alanında bulunamadı.");
    m.put("DEMAND_NOT_EDITABLE",
        "Sözleşme düzenlemeye açık değil (ör. tamamlanmış, reddedilmiş, iptal edilmiş, süresi geçmiş veya bulunamadı).");
    m.put("DEMAND_COMPLETED",
        "Tamamlanmış sözleşme API üzerinden silinemez.");
    m.put("DEMAND_NOT_DISPATCHED",
        "Sözleşme henüz imzaya gönderilmedi (taslak).");
    m.put("DEMAND_NOT_DISPATCHABLE",
        "Sözleşme tamamlanmış veya iptal edilmiş; tekrar gönderilemez.");
    m.put("DEMAND_EXPIRED",
        "Sözleşmenin imza süresi geçmiş.");
    m.put("ALREADY_COMPLETED",
        "Tüm taraflar imzalamış; hatırlatılacak taraf yok.");
    m.put("INVALID_EXPIRY_DATE",
        "expiry_date çözümlenemiyor veya takvimde olmayan bir gün (ör. 2026-02-30).");
    m.put("INVALID_PADES_LEVEL",
        "options.qes_pades_level satın alınabilir seviyelerden biri değil.");
    m.put("DEMAND_ARCHIVED", "Sözleşme arşivde; değiştirilemez ve silinemez. Önce arşivden çıkarın (unarchive).");
    m.put("DEMAND_NOT_ARCHIVABLE", "Yalnız tamamlanmış, iptal edilmiş veya süresi dolmuş sözleşmeler arşivlenebilir.");
    m.put("DEMAND_REJECTED_CANCEL_FIRST", "Sözleşme reddedilmiş ve imza bekliyor; arşivlemeden önce iptal edin (cancel).");
    m.put("TERM_INVALID", "Süre veya yenileme takibi alanlarında geçersiz ya da çelişkili bir kombinasyon var; field alanı reddedilen anahtarı gösterir.");
    m.put("BULK_MAX_10",
        "Toplu oluşturmada rows en fazla 10 satır olabilir.");
    // Gönderim ve davet
    m.put("DISPATCH_NO_PARTIES",
        "Sözleşmede imzacı taraf yok; yayına alınmadı ve kredi düşülmedi.");
    m.put("DISPATCH_TOO_MANY",
        "Taraf sayısı 20 sınırını aşıyor. Gönderim ucunda istek yan etkisiz reddedilir (yayına geçmez, kredi düşmez); oluşturma yanıtında davetlerin gönderilmediğini bildirir.");
    m.put("INVALID_SEND_INVITATIONS",
        "send_invitations değeri tanınmıyor.");
    m.put("RECONCILE_FAILED",
        "Kredi mutabakatı beklenmedik bir hatayla düştü; sözleşme yayına geçmedi, istek güvenle tekrarlanabilir.");
    m.put("INVALID_CHANNELS",
        "channels boş ya da email/sms dışında bir değer içeriyor.");
    m.put("MAX_SMS_REMINDERS_REACHED",
        "Sözleşme için SMS hatırlatma üst sınırına ulaşıldı.");
    m.put("ENVELOPE_NOT_DISPATCHED",
        "Çok belgeli zarf henüz gönderilmedi; önce POST /demands/{id}/dispatch çağırın.");
    m.put("ENVELOPE_CHANGED_DURING_DISPATCH",
        "Kredi mutabakatı ile gönderim arasında zarf değiştirildi. Sözleşme yayına geçmedi; istek güvenle tekrarlanabilir.");
    // Zarf belgeleri
    m.put("ENVELOPE_MULTI_DOC_DISABLED",
        "Çok belgeli zarf özelliği bu hesap için henüz açık değil.");
    m.put("SIGNING_ALREADY_STARTED",
        "İmza süreci başlamış zarfın belge listesi değiştirilemez.");
    m.put("ENVELOPE_ALREADY_DISPATCHED",
        "Sözleşme gönderilmiş; gönderilmiş bir zarfın belge listesi değiştirilemez.");
    m.put("DOCUMENT_LIMIT_EXCEEDED",
        "Zarf başına en fazla 20 belge eklenebilir.");
    m.put("QES_NOT_SUPPORTED_MULTI_DOCUMENT",
        "Nitelikli elektronik imza birden çok belge içeren zarflarda henüz desteklenmiyor.");
    m.put("CANNOT_DELETE_LAST_DOCUMENT",
        "Zarftaki son belge silinemez.");
    m.put("DOCUMENT_HAS_SIGNED_CONTENT",
        "İmza veya içerik girilmiş belge silinemez.");
    m.put("DOCUMENT_HAS_DECISIONS",
        "Bu belge için onay veya red kararı verilmiş; silinemez.");
    m.put("ORDER_SET_MISMATCH",
        "Gönderilen belge kimlikleri zarftaki belgelerle birebir eşleşmiyor.");
    m.put("INVALID_DOC_KIND",
        "doc_kind değeri geçersiz.");
    m.put("CONSENT_CANNOT_BE_REQUIRED",
        "KVKK açık rıza belgesi zorunlu olarak işaretlenemez; açık rıza reddedilebilir olmalıdır.");
    m.put("PREINFO_MUST_BE_REQUIRED",
        "Ön bilgilendirme belgesi zorunlu olarak işaretlenmelidir.");
    m.put("ASSIGNMENT_EMPTY",
        "party_ids eksik veya boş.");
    m.put("ASSIGNMENT_HAS_DECISION",
        "Karar vermiş bir imzacının ataması kaldırılamaz.");
    // Alan yerleştirme
    m.put("INVALID_ITEMS_BODY",
        "items bir dizi değil.");
    m.put("PAGE_ID_REQUIRED",
        "Bir öğede tam sayı page_id yok.");
    m.put("INVALID_PAGE_ID",
        "page_id bu sözleşmeye ait değil.");
    m.put("INVALID_PARTY_ID",
        "party_id bu sözleşmeye ait değil.");
    m.put("INVALID_ITEM_TYPE",
        "item_type desteklenmiyor; hata mesajı izinli türleri listeler.");
    m.put("DUPLICATE_SIGNATURE_FIELD",
        "Aynı sayfa, taraf ve konumda ikinci bir imza alanı oluşturulamaz.");
    // Kişiler
    m.put("CONTACT_DUPLICATE",
        "Çalışma alanında aynı e-posta veya telefona sahip aktif bir kişi zaten var.");
    // Dosya ve zaman damgası
    m.put("FILE_REQUIRED",
        "İstekte dosya yok.");
    m.put("FILE_TOO_LARGE",
        "Dosya boyut sınırını aşıyor.");
    m.put("UNSUPPORTED_FILE_TYPE",
        "Dosya türü bu uçta desteklenmiyor.");
    m.put("UNSUPPORTED_MIME_TYPE",
        "Dosyanın MIME türü zaman damgası için izin verilenler arasında değil.");
    m.put("ENCRYPTED_PDF",
        "PDF parola korumalı; şifresiz bir kopya yükleyin.");
    m.put("IMAGE_DECODE_FAILED",
        "Görsel çözümlenemedi.");
    m.put("TEXT_EXTRACTION_TIMEOUT",
        "Belgeden metin çıkarma süre sınırını aştı.");
    m.put("BAD_BASE64",
        "file_base64 geçerli standart Base64 değil.");
    m.put("STAMP_INVALID",
        "Zaman damgası sunucusu geçersiz bir yanıt döndürdü.");
    m.put("TSA_UNAVAILABLE",
        "Zaman damgası servisi geçici olarak erişilemiyor; kısa süre sonra tekrar deneyin.");
    // Genel
    m.put("VALIDATION_ERROR",
        "İstek doğrulamadan geçmedi (ör. konum sınırları, slug biçimi, eksik taraf).");
    m.put("VALIDATION_FAIL",
        "Zorunlu bir alan eksik veya geçersiz (ör. boş başlık).");
    m.put("INVALID_PAGE",
        "page 1 veya daha büyük bir tam sayı değil; limit hata vermez, 1 ile 100 arasına kırpılır.");
    m.put("INTERNAL_ERROR",
        "Sunucu tarafında beklenmeyen bir hata oluştu.");
    CODES = Collections.unmodifiableMap(m);
  }

  private ErrorCodes() {
  }

  /** Whether {@code code} is in the catalogue. {@code null} is never known. */
  public static boolean isKnown(String code) {
    return code != null && CODES.containsKey(code);
  }

  /** The description for {@code code}, or {@code null} for an unknown or missing code. */
  public static String describe(String code) {
    return code == null ? null : CODES.get(code);
  }
}
