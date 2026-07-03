# ApiV1TemplatesIdPatchRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** |  | [optional] 
**description** | **str** |  | [optional] 
**category** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_templates_id_patch_request import ApiV1TemplatesIdPatchRequest

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1TemplatesIdPatchRequest from a JSON string
api_v1_templates_id_patch_request_instance = ApiV1TemplatesIdPatchRequest.from_json(json)
# print the JSON string representation of the object
print(ApiV1TemplatesIdPatchRequest.to_json())

# convert the object into a dict
api_v1_templates_id_patch_request_dict = api_v1_templates_id_patch_request_instance.to_dict()
# create an instance of ApiV1TemplatesIdPatchRequest from a dict
api_v1_templates_id_patch_request_from_dict = ApiV1TemplatesIdPatchRequest.from_dict(api_v1_templates_id_patch_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


