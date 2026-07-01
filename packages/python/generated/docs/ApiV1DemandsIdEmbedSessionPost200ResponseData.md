# ApiV1DemandsIdEmbedSessionPost200ResponseData


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**embed_token** | **str** | Tek kullanımlık, kısa ömürlü gömülü imza token&#39;ı | [optional] 
**expires_at** | **datetime** | Token geçerlilik bitiş zamanı (ISO 8601 UTC) | [optional] 
**embed_url** | **str** | &#x60;&lt;iframe src&#x3D;\&quot;\&quot;&gt;&#x60; alanına yerleştirilecek tam URL. &#x60;https://e.imzala.org/embed/sign?token&#x3D;&lt;embed_token&gt;&#x60; formatında.  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_id_embed_session_post200_response_data import ApiV1DemandsIdEmbedSessionPost200ResponseData

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsIdEmbedSessionPost200ResponseData from a JSON string
api_v1_demands_id_embed_session_post200_response_data_instance = ApiV1DemandsIdEmbedSessionPost200ResponseData.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsIdEmbedSessionPost200ResponseData.to_json())

# convert the object into a dict
api_v1_demands_id_embed_session_post200_response_data_dict = api_v1_demands_id_embed_session_post200_response_data_instance.to_dict()
# create an instance of ApiV1DemandsIdEmbedSessionPost200ResponseData from a dict
api_v1_demands_id_embed_session_post200_response_data_from_dict = ApiV1DemandsIdEmbedSessionPost200ResponseData.from_dict(api_v1_demands_id_embed_session_post200_response_data_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


