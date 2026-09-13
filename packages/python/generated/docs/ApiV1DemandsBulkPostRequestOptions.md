# ApiV1DemandsBulkPostRequestOptions

Tüm satırlara uygulanan batch-seviye seçenekler (opsiyonel)

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**dispatch_notifications** | **bool** | false ise davet gönderilmez (yalnızca oluşturulur) | [optional] [default to True]
**ordered** | **bool** | true ise taraflar sırayla imzalar | [optional] 
**sms_content** | **str** | SMS davet metni özelleştirmesi (yalnızca kendi SMS sağlayıcısı olan org&#39;lar) | [optional] 
**language** | **str** |  | [optional] 
**enable_qes** | **bool** | 🔴 Faturayı etkiler. Nitelikli imza (QES) sınıfı; imzacı başına taban ücret 1 yerine 2 kredi. | [optional] 
**qes_pades_level** | **str** | 🔴 Faturayı etkiler. Yalnız enable_qes ile anlamlıdır: B-T +0, B-LT +0, B-LTA +1 kredi (imzacı başına). | [optional] 
**eidas_timestamp** | **bool** | 🔴 Faturayı etkiler. AB nitelikli zaman damgası; satır (sözleşme) başına +1 kredi. | [optional] 
**require_id_photo** | **bool** | 🔴 Faturayı etkiler. Kimlik fotoğrafı doğrulaması; imzacı başına +1 kredi. | [optional] 
**require_liveness_check** | **bool** | 🔴 Faturayı etkiler. Canlılık doğrulaması; imzacı başına +1 kredi. | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_bulk_post_request_options import ApiV1DemandsBulkPostRequestOptions

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsBulkPostRequestOptions from a JSON string
api_v1_demands_bulk_post_request_options_instance = ApiV1DemandsBulkPostRequestOptions.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsBulkPostRequestOptions.to_json())

# convert the object into a dict
api_v1_demands_bulk_post_request_options_dict = api_v1_demands_bulk_post_request_options_instance.to_dict()
# create an instance of ApiV1DemandsBulkPostRequestOptions from a dict
api_v1_demands_bulk_post_request_options_from_dict = ApiV1DemandsBulkPostRequestOptions.from_dict(api_v1_demands_bulk_post_request_options_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


