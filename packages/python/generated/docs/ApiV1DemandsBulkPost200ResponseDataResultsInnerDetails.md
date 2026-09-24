# ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails

Yalnız error=INVALID_DOCUMENT_SELECTION ise: reason (shape | unknown_document | conflict | empty) ve varsa document_ids. error=INVALID_DOCUMENT_VARIABLES ise: reason (unsupported_endpoint) ve path.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**reason** | **str** |  | [optional] 
**document_ids** | **List[str]** |  | [optional] 
**path** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_bulk_post200_response_data_results_inner_details import ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails from a JSON string
api_v1_demands_bulk_post200_response_data_results_inner_details_instance = ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails.to_json())

# convert the object into a dict
api_v1_demands_bulk_post200_response_data_results_inner_details_dict = api_v1_demands_bulk_post200_response_data_results_inner_details_instance.to_dict()
# create an instance of ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails from a dict
api_v1_demands_bulk_post200_response_data_results_inner_details_from_dict = ApiV1DemandsBulkPost200ResponseDataResultsInnerDetails.from_dict(api_v1_demands_bulk_post200_response_data_results_inner_details_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


