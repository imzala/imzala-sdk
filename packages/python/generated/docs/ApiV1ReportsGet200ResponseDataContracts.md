# ApiV1ReportsGet200ResponseDataContracts


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**total** | **int** |  | [optional] 
**pending** | **int** |  | [optional] 
**completed** | **int** |  | [optional] 
**cancelled** | **int** |  | [optional] 
**expired** | **int** |  | [optional] 
**this_month** | **int** |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_reports_get200_response_data_contracts import ApiV1ReportsGet200ResponseDataContracts

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1ReportsGet200ResponseDataContracts from a JSON string
api_v1_reports_get200_response_data_contracts_instance = ApiV1ReportsGet200ResponseDataContracts.from_json(json)
# print the JSON string representation of the object
print(ApiV1ReportsGet200ResponseDataContracts.to_json())

# convert the object into a dict
api_v1_reports_get200_response_data_contracts_dict = api_v1_reports_get200_response_data_contracts_instance.to_dict()
# create an instance of ApiV1ReportsGet200ResponseDataContracts from a dict
api_v1_reports_get200_response_data_contracts_from_dict = ApiV1ReportsGet200ResponseDataContracts.from_dict(api_v1_reports_get200_response_data_contracts_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


