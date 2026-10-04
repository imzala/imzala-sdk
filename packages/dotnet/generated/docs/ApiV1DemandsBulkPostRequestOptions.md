# ImzalaApiClient.Model.ApiV1DemandsBulkPostRequestOptions
Tüm satırlara uygulanan batch-seviye seçenekler (opsiyonel)

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**DispatchNotifications** | **bool** | false ise davet gönderilmez (yalnızca oluşturulur) | [optional] [default to true]
**Ordered** | **bool** | true ise taraflar sırayla imzalar | [optional] 
**SmsContent** | **string** | SMS davet metni özelleştirmesi (yalnızca kendi SMS sağlayıcısı olan org&#39;lar) | [optional] 
**Language** | **string** |  | [optional] 
**EnableQes** | **bool** | 🔴 Faturayı etkiler. Güvenli elektronik imza sınıfı; imzacı başına taban ücret 1 yerine 2 kredi. | [optional] 
**QesPadesLevel** | **string** | 🔴 Faturayı etkiler. Yalnız enable_qes ile anlamlıdır: B-T +0, B-LT +0, B-LTA +1 kredi (imzacı başına). | [optional] 
**EidasTimestamp** | **bool** | 🔴 Faturayı etkiler. AB nitelikli zaman damgası; satır (sözleşme) başına +1 kredi. | [optional] 
**AllowedSignatureVariants** | **List&lt;ApiV1DemandsBulkPostRequestOptions.AllowedSignatureVariantsEnum&gt;** | Batch-seviye: bu listedeki dijital imza yöntemleri ve sıra TÜM satırlara uygulanır (satır başına ayarlanamaz). Davranışı &#x60;POST /api/v1/demands&#x60; ile aynıdır: sıra sekme sırasıdır, gönderilmezse şablon, sonra organizasyon ayarı devralınır, boş dizi veya tanınmayan değer yok sayılır. Yalnız &#x60;phone&#x60; içeren liste tüm batch&#39;i 400 &#x60;SIGNATURE_VARIANTS_PHONE_ONLY&#x60; ile, &#x60;phone_draw&#x60;&#39;ı başka bir yöntemle birleştiren liste 400 &#x60;SIGNATURE_VARIANTS_PHONE_DRAW_EXCLUSIVE&#x60; ile reddeder (hiçbir sözleşme oluşturulmaz).  | [optional] 
**RequireIdPhoto** | **bool** | 🔴 Faturayı etkiler. Kimlik fotoğrafı doğrulaması; imzacı başına +1 kredi. | [optional] 
**RequireLivenessCheck** | **bool** | 🔴 Faturayı etkiler. Canlılık doğrulaması; imzacı başına +1 kredi. | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

