# ApiV1DemandsIdEmbedSessionPostRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**party_id** | **UUID** | Token üretilecek tarafın ID&#39;si. &#x60;POST /api/v1/demands&#x60; veya &#x60;GET /api/v1/demands/{id}&#x60; cevabındaki &#x60;signing_urls[].party_id&#x60; alanından alınır.  | 

## Example

```python
from imzala_client.models.api_v1_demands_id_embed_session_post_request import ApiV1DemandsIdEmbedSessionPostRequest

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsIdEmbedSessionPostRequest from a JSON string
api_v1_demands_id_embed_session_post_request_instance = ApiV1DemandsIdEmbedSessionPostRequest.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsIdEmbedSessionPostRequest.to_json())

# convert the object into a dict
api_v1_demands_id_embed_session_post_request_dict = api_v1_demands_id_embed_session_post_request_instance.to_dict()
# create an instance of ApiV1DemandsIdEmbedSessionPostRequest from a dict
api_v1_demands_id_embed_session_post_request_from_dict = ApiV1DemandsIdEmbedSessionPostRequest.from_dict(api_v1_demands_id_embed_session_post_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


