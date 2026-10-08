

# ApiV1DemandsBulkPostRequestOptions

Tüm satırlara uygulanan batch-seviye seçenekler (opsiyonel)

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**documentCollectionEnabled** | **Boolean** | Belge Toplama açılsın mı? Gönderilmezse veya null ise sözleşme şablonunun ayarı devralınır. Etkin değer true ise bir Belge Toplama şablonu gerekir. false devralınan toplamayı kapatır; açıkça verilen document_collection_template_id yine doğrulanır.  |  [optional] |
|**documentCollectionTemplateId** | **UUID** | Mevcut Belge Toplama şablonunun kimliği. Gönderilmezse, null veya boş dize ise etkin toplama açıkken sözleşme şablonundaki seçim devralınır. Açıkça verilen dolu kimlik, toplama false olsa da doğrulanır. Seçim aktif ve aynı çalışma alanına ait olmalıdır; bulunamazsa, arşivliyse, kapsam dışıysa veya kimlik metin değilse 404 DOCUMENT_COLLECTION_TEMPLATE_NOT_FOUND döner.  |  [optional] |
|**documentCollectionTiming** | [**DocumentCollectionTimingEnum**](#DocumentCollectionTimingEnum) | Toplama zamanı; gönderilmezse veya null ise sözleşme şablonunun ayarı devralınır. Geçersiz değer 400 INVALID_DOCUMENT_COLLECTION_TIMING döner.  |  [optional] |
|**documentCollectionBlockSigning** | **Boolean** | Zorunlu belgeler tamamlanana kadar imzalamayı engelleme ayarı. Gönderilmezse veya null ise sözleşme şablonunun ayarı devralınır. Etkin AFTER_SIGNING zamanında true false olarak normalleştirilir. Boolean alanlara boolean veya null dışında değer verilirse 400 INVALID_DOCUMENT_COLLECTION_FLAG döner.  |  [optional] |
|**dispatchNotifications** | **Boolean** | false ise davet gönderilmez (yalnızca oluşturulur) |  [optional] |
|**ordered** | **Boolean** | true ise taraflar sırayla imzalar |  [optional] |
|**smsContent** | **String** | SMS davet metni özelleştirmesi (yalnızca kendi SMS sağlayıcısı olan org&#39;lar) |  [optional] |
|**language** | **String** |  |  [optional] |
|**enableQes** | **Boolean** | 🔴 Faturayı etkiler. Güvenli elektronik imza sınıfı; imzacı başına taban ücret 1 yerine 2 kredi. |  [optional] |
|**qesPadesLevel** | [**QesPadesLevelEnum**](#QesPadesLevelEnum) | 🔴 Faturayı etkiler. Yalnız enable_qes ile anlamlıdır: B-T +0, B-LT +0, B-LTA +1 kredi (imzacı başına). |  [optional] |
|**eidasTimestamp** | **Boolean** | 🔴 Faturayı etkiler. AB nitelikli zaman damgası; satır (sözleşme) başına +1 kredi. |  [optional] |
|**allowedSignatureVariants** | [**List&lt;AllowedSignatureVariantsEnum&gt;**](#List&lt;AllowedSignatureVariantsEnum&gt;) | Batch-seviye: bu listedeki dijital imza yöntemleri ve sıra TÜM satırlara uygulanır (satır başına ayarlanamaz). Davranışı &#x60;POST /api/v1/demands&#x60; ile aynıdır: sıra sekme sırasıdır, gönderilmezse şablon, sonra organizasyon ayarı devralınır, boş dizi veya tanınmayan değer yok sayılır. Yalnız &#x60;phone&#x60; içeren liste tüm batch&#39;i 400 &#x60;SIGNATURE_VARIANTS_PHONE_ONLY&#x60; ile, &#x60;phone_draw&#x60;&#39;ı başka bir yöntemle birleştiren liste 400 &#x60;SIGNATURE_VARIANTS_PHONE_DRAW_EXCLUSIVE&#x60; ile reddeder (hiçbir sözleşme oluşturulmaz).  |  [optional] |
|**requireIdPhoto** | **Boolean** | 🔴 Faturayı etkiler. Kimlik fotoğrafı doğrulaması; imzacı başına +1 kredi. |  [optional] |
|**requireLivenessCheck** | **Boolean** | 🔴 Faturayı etkiler. Canlılık doğrulaması; imzacı başına +1 kredi. |  [optional] |



## Enum: DocumentCollectionTimingEnum

| Name | Value |
|---- | -----|
| BEFORE_SIGNING | &quot;BEFORE_SIGNING&quot; |
| AFTER_SIGNING | &quot;AFTER_SIGNING&quot; |



## Enum: QesPadesLevelEnum

| Name | Value |
|---- | -----|
| PAD_ES_BASELINE_T | &quot;PAdES_BASELINE_T&quot; |
| PAD_ES_BASELINE_LT | &quot;PAdES_BASELINE_LT&quot; |
| PAD_ES_BASELINE_LTA | &quot;PAdES_BASELINE_LTA&quot; |



## Enum: List&lt;AllowedSignatureVariantsEnum&gt;

| Name | Value |
|---- | -----|
| DRAW | &quot;draw&quot; |
| TYPE | &quot;type&quot; |
| UPLOAD | &quot;upload&quot; |
| PHONE | &quot;phone&quot; |
| PHONE_DRAW | &quot;phone_draw&quot; |



