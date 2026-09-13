# ApiV1DemandsDemandIdDocumentsGet200Response


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**data** | [**ApiV1DemandsDemandIdDocumentsGet200ResponseData**](ApiV1DemandsDemandIdDocumentsGet200ResponseData.md) |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_demand_id_documents_get200_response import ApiV1DemandsDemandIdDocumentsGet200Response

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsDemandIdDocumentsGet200Response from a JSON string
api_v1_demands_demand_id_documents_get200_response_instance = ApiV1DemandsDemandIdDocumentsGet200Response.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsDemandIdDocumentsGet200Response.to_json())

# convert the object into a dict
api_v1_demands_demand_id_documents_get200_response_dict = api_v1_demands_demand_id_documents_get200_response_instance.to_dict()
# create an instance of ApiV1DemandsDemandIdDocumentsGet200Response from a dict
api_v1_demands_demand_id_documents_get200_response_from_dict = ApiV1DemandsDemandIdDocumentsGet200Response.from_dict(api_v1_demands_demand_id_documents_get200_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


