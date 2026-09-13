# ApiV1DemandsBulkPostRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_id** | **UUID** | Sözleşmelerin oluşturulacağı şablon | 
**options** | [**ApiV1DemandsBulkPostRequestOptions**](ApiV1DemandsBulkPostRequestOptions.md) |  | [optional] 
**rows** | [**List[ApiV1DemandsBulkPostRequestRowsInner]**](ApiV1DemandsBulkPostRequestRowsInner.md) | Her biri ayrı bir sözleşme olacak satırlar (en fazla 10) | 

## Example

```python
from imzala_client.models.api_v1_demands_bulk_post_request import ApiV1DemandsBulkPostRequest

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsBulkPostRequest from a JSON string
api_v1_demands_bulk_post_request_instance = ApiV1DemandsBulkPostRequest.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsBulkPostRequest.to_json())

# convert the object into a dict
api_v1_demands_bulk_post_request_dict = api_v1_demands_bulk_post_request_instance.to_dict()
# create an instance of ApiV1DemandsBulkPostRequest from a dict
api_v1_demands_bulk_post_request_from_dict = ApiV1DemandsBulkPostRequest.from_dict(api_v1_demands_bulk_post_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


