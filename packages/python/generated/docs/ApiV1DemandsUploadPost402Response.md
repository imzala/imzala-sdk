# ApiV1DemandsUploadPost402Response


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**error** | **str** |  | [optional] 
**code** | **str** |  | [optional] 
**demand_id** | **UUID** |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_upload_post402_response import ApiV1DemandsUploadPost402Response

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsUploadPost402Response from a JSON string
api_v1_demands_upload_post402_response_instance = ApiV1DemandsUploadPost402Response.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsUploadPost402Response.to_json())

# convert the object into a dict
api_v1_demands_upload_post402_response_dict = api_v1_demands_upload_post402_response_instance.to_dict()
# create an instance of ApiV1DemandsUploadPost402Response from a dict
api_v1_demands_upload_post402_response_from_dict = ApiV1DemandsUploadPost402Response.from_dict(api_v1_demands_upload_post402_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


