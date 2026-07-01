# ApiV1TemplatesGet401Response


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**error** | **str** |  | [optional] 
**message** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_templates_get401_response import ApiV1TemplatesGet401Response

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1TemplatesGet401Response from a JSON string
api_v1_templates_get401_response_instance = ApiV1TemplatesGet401Response.from_json(json)
# print the JSON string representation of the object
print(ApiV1TemplatesGet401Response.to_json())

# convert the object into a dict
api_v1_templates_get401_response_dict = api_v1_templates_get401_response_instance.to_dict()
# create an instance of ApiV1TemplatesGet401Response from a dict
api_v1_templates_get401_response_from_dict = ApiV1TemplatesGet401Response.from_dict(api_v1_templates_get401_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


