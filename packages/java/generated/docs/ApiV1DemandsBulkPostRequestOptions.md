

# ApiV1DemandsBulkPostRequestOptions

Tüm satırlara uygulanan batch-seviye seçenekler (opsiyonel)

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**dispatchNotifications** | **Boolean** | false ise davet gönderilmez (yalnızca oluşturulur) |  [optional] |
|**ordered** | **Boolean** | true ise taraflar sırayla imzalar |  [optional] |
|**smsContent** | **String** | SMS davet metni özelleştirmesi (yalnızca kendi SMS sağlayıcısı olan org&#39;lar) |  [optional] |
|**language** | **String** |  |  [optional] |
|**enableQes** | **Boolean** | 🔴 Faturayı etkiler. Nitelikli imza (QES) sınıfı; imzacı başına taban ücret 1 yerine 2 kredi. |  [optional] |
|**qesPadesLevel** | [**QesPadesLevelEnum**](#QesPadesLevelEnum) | 🔴 Faturayı etkiler. Yalnız enable_qes ile anlamlıdır: B-T +0, B-LT +0, B-LTA +1 kredi (imzacı başına). |  [optional] |
|**eidasTimestamp** | **Boolean** | 🔴 Faturayı etkiler. AB nitelikli zaman damgası; satır (sözleşme) başına +1 kredi. |  [optional] |
|**requireIdPhoto** | **Boolean** | 🔴 Faturayı etkiler. Kimlik fotoğrafı doğrulaması; imzacı başına +1 kredi. |  [optional] |
|**requireLivenessCheck** | **Boolean** | 🔴 Faturayı etkiler. Canlılık doğrulaması; imzacı başına +1 kredi. |  [optional] |



## Enum: QesPadesLevelEnum

| Name | Value |
|---- | -----|
| PAD_ES_BASELINE_T | &quot;PAdES_BASELINE_T&quot; |
| PAD_ES_BASELINE_LT | &quot;PAdES_BASELINE_LT&quot; |
| PAD_ES_BASELINE_LTA | &quot;PAdES_BASELINE_LTA&quot; |



