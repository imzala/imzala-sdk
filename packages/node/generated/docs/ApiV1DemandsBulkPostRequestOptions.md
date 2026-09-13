# ApiV1DemandsBulkPostRequestOptions

Tüm satırlara uygulanan batch-seviye seçenekler (opsiyonel)

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**dispatch_notifications** | **boolean** | false ise davet gönderilmez (yalnızca oluşturulur) | [optional] [default to true]
**ordered** | **boolean** | true ise taraflar sırayla imzalar | [optional] [default to undefined]
**sms_content** | **string** | SMS davet metni özelleştirmesi (yalnızca kendi SMS sağlayıcısı olan org\&#39;lar) | [optional] [default to undefined]
**language** | **string** |  | [optional] [default to undefined]
**enable_qes** | **boolean** | 🔴 Faturayı etkiler. Nitelikli imza (QES) sınıfı; imzacı başına taban ücret 1 yerine 2 kredi. | [optional] [default to undefined]
**qes_pades_level** | **string** | 🔴 Faturayı etkiler. Yalnız enable_qes ile anlamlıdır: B-T +0, B-LT +0, B-LTA +1 kredi (imzacı başına). | [optional] [default to undefined]
**eidas_timestamp** | **boolean** | 🔴 Faturayı etkiler. AB nitelikli zaman damgası; satır (sözleşme) başına +1 kredi. | [optional] [default to undefined]
**require_id_photo** | **boolean** | 🔴 Faturayı etkiler. Kimlik fotoğrafı doğrulaması; imzacı başına +1 kredi. | [optional] [default to undefined]
**require_liveness_check** | **boolean** | 🔴 Faturayı etkiler. Canlılık doğrulaması; imzacı başına +1 kredi. | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsBulkPostRequestOptions } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsBulkPostRequestOptions = {
    dispatch_notifications,
    ordered,
    sms_content,
    language,
    enable_qes,
    qes_pades_level,
    eidas_timestamp,
    require_id_photo,
    require_liveness_check,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
