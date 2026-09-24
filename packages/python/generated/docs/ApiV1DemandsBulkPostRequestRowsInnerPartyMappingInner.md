# ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_party_id** | **UUID** |  | [optional] 
**first_name** | **str** |  | [optional] 
**last_name** | **str** |  | [optional] 
**email** | **str** |  | [optional] 
**phone** | **str** | E.164 biçiminde telefon (ör. &#x60;+905551112233&#x60;). Ülke kodu ile birlikte gönderin; fazlalık şehirlerarası öneki (TRde baş sıfır, &#x60;+90 0542…&#x60;) sunucuda kaldırılır ve numara kanonik E.164 biçiminde saklanır. Tanınmayan girdi olduğu gibi korunur ama SMS teslim edilemeyebilir. | [optional] 
**government_id** | **str** | T.C. kimlik numarası (11 hane) | [optional] 
**birth_date** | **date** | ISO 8601 (ör. 1990-05-15) | [optional] 
**send_sms** | **bool** |  | [optional] [default to True]
**send_email** | **bool** |  | [optional] [default to True]
**custom_message** | **str** | Bu tarafa özel davet mesajı | [optional] 
**variables** | **object** | Bu tarafa özel değişkenler | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_bulk_post_request_rows_inner_party_mapping_inner import ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner from a JSON string
api_v1_demands_bulk_post_request_rows_inner_party_mapping_inner_instance = ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.to_json())

# convert the object into a dict
api_v1_demands_bulk_post_request_rows_inner_party_mapping_inner_dict = api_v1_demands_bulk_post_request_rows_inner_party_mapping_inner_instance.to_dict()
# create an instance of ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner from a dict
api_v1_demands_bulk_post_request_rows_inner_party_mapping_inner_from_dict = ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.from_dict(api_v1_demands_bulk_post_request_rows_inner_party_mapping_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


