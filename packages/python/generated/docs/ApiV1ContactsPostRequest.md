# ApiV1ContactsPostRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**first_name** | **str** |  | 
**last_name** | **str** |  | 
**email** | **str** |  | [optional] 
**phone** | **str** |  | [optional] 
**job_title** | **str** |  | [optional] 
**company_id** | **UUID** |  | [optional] 
**notes** | **str** |  | [optional] 
**address_country** | **str** |  | [optional] 
**address_city** | **str** |  | [optional] 
**address_district** | **str** |  | [optional] 
**address_line** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_contacts_post_request import ApiV1ContactsPostRequest

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1ContactsPostRequest from a JSON string
api_v1_contacts_post_request_instance = ApiV1ContactsPostRequest.from_json(json)
# print the JSON string representation of the object
print(ApiV1ContactsPostRequest.to_json())

# convert the object into a dict
api_v1_contacts_post_request_dict = api_v1_contacts_post_request_instance.to_dict()
# create an instance of ApiV1ContactsPostRequest from a dict
api_v1_contacts_post_request_from_dict = ApiV1ContactsPostRequest.from_dict(api_v1_contacts_post_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


