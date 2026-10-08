# ApiV1DemandsBulkPostRequestOptions

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**document_collection_enabled** | **bool** | Belge Toplama açılsın mı? Gönderilmezse veya null ise sözleşme şablonunun ayarı devralınır. Etkin değer true ise bir Belge Toplama şablonu gerekir. false devralınan toplamayı kapatır; açıkça verilen document_collection_template_id yine doğrulanır. | [optional]
**document_collection_template_id** | **string** | Mevcut Belge Toplama şablonunun kimliği. Gönderilmezse, null veya boş dize ise etkin toplama açıkken sözleşme şablonundaki seçim devralınır. Açıkça verilen dolu kimlik, toplama false olsa da doğrulanır. Seçim aktif ve aynı çalışma alanına ait olmalıdır; bulunamazsa, arşivliyse, kapsam dışıysa veya kimlik metin değilse 404 DOCUMENT_COLLECTION_TEMPLATE_NOT_FOUND döner. | [optional]
**document_collection_timing** | **string** | Toplama zamanı; gönderilmezse veya null ise sözleşme şablonunun ayarı devralınır. Geçersiz değer 400 INVALID_DOCUMENT_COLLECTION_TIMING döner. | [optional]
**document_collection_block_signing** | **bool** | Zorunlu belgeler tamamlanana kadar imzalamayı engelleme ayarı. Gönderilmezse veya null ise sözleşme şablonunun ayarı devralınır. Etkin AFTER_SIGNING zamanında true false olarak normalleştirilir. Boolean alanlara boolean veya null dışında değer verilirse 400 INVALID_DOCUMENT_COLLECTION_FLAG döner. | [optional]
**dispatch_notifications** | **bool** | false ise davet gönderilmez (yalnızca oluşturulur) | [optional] [default to true]
**ordered** | **bool** | true ise taraflar sırayla imzalar | [optional]
**sms_content** | **string** | SMS davet metni özelleştirmesi (yalnızca kendi SMS sağlayıcısı olan org&#39;lar) | [optional]
**language** | **string** |  | [optional]
**enable_qes** | **bool** | 🔴 Faturayı etkiler. Güvenli elektronik imza sınıfı; imzacı başına taban ücret 1 yerine 2 kredi. | [optional]
**qes_pades_level** | **string** | 🔴 Faturayı etkiler. Yalnız enable_qes ile anlamlıdır: B-T +0, B-LT +0, B-LTA +1 kredi (imzacı başına). | [optional]
**eidas_timestamp** | **bool** | 🔴 Faturayı etkiler. AB nitelikli zaman damgası; satır (sözleşme) başına +1 kredi. | [optional]
**allowed_signature_variants** | **string[]** | Batch-seviye: bu listedeki dijital imza yöntemleri ve sıra TÜM satırlara uygulanır (satır başına ayarlanamaz). Davranışı &#x60;POST /api/v1/demands&#x60; ile aynıdır: sıra sekme sırasıdır, gönderilmezse şablon, sonra organizasyon ayarı devralınır, boş dizi veya tanınmayan değer yok sayılır. Yalnız &#x60;phone&#x60; içeren liste tüm batch&#39;i 400 &#x60;SIGNATURE_VARIANTS_PHONE_ONLY&#x60; ile, &#x60;phone_draw&#x60;&#39;ı başka bir yöntemle birleştiren liste 400 &#x60;SIGNATURE_VARIANTS_PHONE_DRAW_EXCLUSIVE&#x60; ile reddeder (hiçbir sözleşme oluşturulmaz). | [optional]
**require_id_photo** | **bool** | 🔴 Faturayı etkiler. Kimlik fotoğrafı doğrulaması; imzacı başına +1 kredi. | [optional]
**require_liveness_check** | **bool** | 🔴 Faturayı etkiler. Canlılık doğrulaması; imzacı başına +1 kredi. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
