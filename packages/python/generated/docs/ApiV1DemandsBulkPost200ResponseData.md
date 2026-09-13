# ApiV1DemandsBulkPost200ResponseData


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_id** | **UUID** |  | [optional] 
**total** | **int** |  | [optional] 
**created** | **int** |  | [optional] 
**failed** | **int** |  | [optional] 
**results** | [**List[ApiV1DemandsBulkPost200ResponseDataResultsInner]**](ApiV1DemandsBulkPost200ResponseDataResultsInner.md) |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_bulk_post200_response_data import ApiV1DemandsBulkPost200ResponseData

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsBulkPost200ResponseData from a JSON string
api_v1_demands_bulk_post200_response_data_instance = ApiV1DemandsBulkPost200ResponseData.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsBulkPost200ResponseData.to_json())

# convert the object into a dict
api_v1_demands_bulk_post200_response_data_dict = api_v1_demands_bulk_post200_response_data_instance.to_dict()
# create an instance of ApiV1DemandsBulkPost200ResponseData from a dict
api_v1_demands_bulk_post200_response_data_from_dict = ApiV1DemandsBulkPost200ResponseData.from_dict(api_v1_demands_bulk_post200_response_data_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


