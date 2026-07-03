# ApiV1DemandsIdDelete409Response


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**error** | **str** |  | [optional] 
**code** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_id_delete409_response import ApiV1DemandsIdDelete409Response

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsIdDelete409Response from a JSON string
api_v1_demands_id_delete409_response_instance = ApiV1DemandsIdDelete409Response.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsIdDelete409Response.to_json())

# convert the object into a dict
api_v1_demands_id_delete409_response_dict = api_v1_demands_id_delete409_response_instance.to_dict()
# create an instance of ApiV1DemandsIdDelete409Response from a dict
api_v1_demands_id_delete409_response_from_dict = ApiV1DemandsIdDelete409Response.from_dict(api_v1_demands_id_delete409_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


