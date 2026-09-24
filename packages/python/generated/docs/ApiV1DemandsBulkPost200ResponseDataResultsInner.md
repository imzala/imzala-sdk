# ApiV1DemandsBulkPost200ResponseDataResultsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**row_index** | **int** |  | [optional] 
**status** | **str** |  | [optional] 
**demand_id** | **UUID** | status&#x3D;created ise; veya status&#x3D;failed olup sözleşme oluşturulduktan SONRA kredi mutabakatı reddettiyse ya da atamasız imzacı bulunduysa (taslak, davet gönderilmedi) | [optional] 
**party_ids** | **List[UUID]** | Yalnız error&#x3D;PARTY_WITHOUT_DOCUMENTS ve sözleşme oluşturulduktan SONRA tespit edildiyse: hiçbir belgeye atanmamış taraf id&#39;leri. | [optional] 
**template_party_ids** | **List[UUID]** | Yalnız error&#x3D;PARTY_WITHOUT_DOCUMENTS ve satır oluşturulmadan ÖNCE tespit edildiyse: seçim sonucunda hiçbir belgeye atanmamış kalan şablon rol kimlikleri. | [optional] 
**details** | [**ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails**](ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails.md) |  | [optional] 
**signing_urls** | [**List[ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner]**](ApiV1DemandsBulkPost200ResponseDataResultsInnerSigningUrlsInner.md) |  | [optional] 
**result_url** | **str** |  | [optional] 
**dispatched** | **int** | Gönderilen davet (SMS+e-posta) sayısı | [optional] 
**error** | **str** | status&#x3D;failed ise makinece okunabilir kod (VALIDATION, INSUFFICIENT_CREDITS, MEMBER_LIMIT_EXCEEDED, PARTY_WITHOUT_DOCUMENTS, INVALID_DOCUMENT_SELECTION, INVALID_DOCUMENT_VARIABLES, TEMPLATE_DOCUMENTS_NOT_READY, DOCUMENT_SOURCE_UNAVAILABLE, RECONCILE_FAILED, CREATE_FAILED vb.) | [optional] 
**message** | **str** | status&#x3D;failed ise açıklama | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_bulk_post200_response_data_results_inner import ApiV1DemandsBulkPost200ResponseDataResultsInner

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsBulkPost200ResponseDataResultsInner from a JSON string
api_v1_demands_bulk_post200_response_data_results_inner_instance = ApiV1DemandsBulkPost200ResponseDataResultsInner.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsBulkPost200ResponseDataResultsInner.to_json())

# convert the object into a dict
api_v1_demands_bulk_post200_response_data_results_inner_dict = api_v1_demands_bulk_post200_response_data_results_inner_instance.to_dict()
# create an instance of ApiV1DemandsBulkPost200ResponseDataResultsInner from a dict
api_v1_demands_bulk_post200_response_data_results_inner_from_dict = ApiV1DemandsBulkPost200ResponseDataResultsInner.from_dict(api_v1_demands_bulk_post200_response_data_results_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


