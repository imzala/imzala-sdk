# ApiV1DemandsIdEmbedSessionPost200Response


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**data** | [**ApiV1DemandsIdEmbedSessionPost200ResponseData**](ApiV1DemandsIdEmbedSessionPost200ResponseData.md) |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_id_embed_session_post200_response import ApiV1DemandsIdEmbedSessionPost200Response

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsIdEmbedSessionPost200Response from a JSON string
api_v1_demands_id_embed_session_post200_response_instance = ApiV1DemandsIdEmbedSessionPost200Response.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsIdEmbedSessionPost200Response.to_json())

# convert the object into a dict
api_v1_demands_id_embed_session_post200_response_dict = api_v1_demands_id_embed_session_post200_response_instance.to_dict()
# create an instance of ApiV1DemandsIdEmbedSessionPost200Response from a dict
api_v1_demands_id_embed_session_post200_response_from_dict = ApiV1DemandsIdEmbedSessionPost200Response.from_dict(api_v1_demands_id_embed_session_post200_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


