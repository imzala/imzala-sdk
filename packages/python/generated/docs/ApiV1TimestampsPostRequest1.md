# ApiV1TimestampsPostRequest1


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**file_base64** | **str** | Standart Base64 kodlanmış dosya içeriği (RFC 4648 §4 — A-Za-z0-9+/ alfabesi, &#39;&#x3D;&#39; padding). data URL öneki (&#x60;data:...;base64,&#x60;) ve URL-safe alfabe (&#x60;-_&#x60;) kabul edilmez.  | 
**file_name** | **str** | Orijinal dosya adı (uzantısıyla, ör. \&quot;belge.pdf\&quot;) | 
**description** | **str** | Kayıt açıklaması (opsiyonel, max 500 karakter) | [optional] 
**owner_first_name** | **str** | Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) | [optional] 
**owner_last_name** | **str** | Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) | [optional] 

## Example

```python
from imzala_client.models.api_v1_timestamps_post_request1 import ApiV1TimestampsPostRequest1

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1TimestampsPostRequest1 from a JSON string
api_v1_timestamps_post_request1_instance = ApiV1TimestampsPostRequest1.from_json(json)
# print the JSON string representation of the object
print(ApiV1TimestampsPostRequest1.to_json())

# convert the object into a dict
api_v1_timestamps_post_request1_dict = api_v1_timestamps_post_request1_instance.to_dict()
# create an instance of ApiV1TimestampsPostRequest1 from a dict
api_v1_timestamps_post_request1_from_dict = ApiV1TimestampsPostRequest1.from_dict(api_v1_timestamps_post_request1_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


