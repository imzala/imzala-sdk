# ApiV1DemandsIdExternalRefPatch200Response


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional]
**data** | [**ApiV1DemandsIdExternalRefPatch200ResponseData**](ApiV1DemandsIdExternalRefPatch200ResponseData.md) |  | [optional]

## Example

```python
from imzala_client.models.api_v1_demands_id_external_ref_patch200_response import ApiV1DemandsIdExternalRefPatch200Response

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsIdExternalRefPatch200Response from a JSON string
api_v1_demands_id_external_ref_patch200_response_instance = ApiV1DemandsIdExternalRefPatch200Response.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsIdExternalRefPatch200Response.to_json())

# convert the object into a dict
api_v1_demands_id_external_ref_patch200_response_dict = api_v1_demands_id_external_ref_patch200_response_instance.to_dict()
# create an instance of ApiV1DemandsIdExternalRefPatch200Response from a dict
api_v1_demands_id_external_ref_patch200_response_from_dict = ApiV1DemandsIdExternalRefPatch200Response.from_dict(api_v1_demands_id_external_ref_patch200_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
