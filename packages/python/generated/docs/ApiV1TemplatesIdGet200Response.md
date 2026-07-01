# ApiV1TemplatesIdGet200Response


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**data** | [**TemplateDetail**](TemplateDetail.md) |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_templates_id_get200_response import ApiV1TemplatesIdGet200Response

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1TemplatesIdGet200Response from a JSON string
api_v1_templates_id_get200_response_instance = ApiV1TemplatesIdGet200Response.from_json(json)
# print the JSON string representation of the object
print(ApiV1TemplatesIdGet200Response.to_json())

# convert the object into a dict
api_v1_templates_id_get200_response_dict = api_v1_templates_id_get200_response_instance.to_dict()
# create an instance of ApiV1TemplatesIdGet200Response from a dict
api_v1_templates_id_get200_response_from_dict = ApiV1TemplatesIdGet200Response.from_dict(api_v1_templates_id_get200_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


