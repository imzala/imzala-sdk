# ApiV1DemandsBulkPostRequestRowsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**party_mapping** | [**List[ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner]**](ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner.md) | Bu satırın taraf eşlemesi (POST /demands ile aynı şekil) | 
**variables** | **object** | Satır-kök değişkenler | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_bulk_post_request_rows_inner import ApiV1DemandsBulkPostRequestRowsInner

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsBulkPostRequestRowsInner from a JSON string
api_v1_demands_bulk_post_request_rows_inner_instance = ApiV1DemandsBulkPostRequestRowsInner.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsBulkPostRequestRowsInner.to_json())

# convert the object into a dict
api_v1_demands_bulk_post_request_rows_inner_dict = api_v1_demands_bulk_post_request_rows_inner_instance.to_dict()
# create an instance of ApiV1DemandsBulkPostRequestRowsInner from a dict
api_v1_demands_bulk_post_request_rows_inner_from_dict = ApiV1DemandsBulkPostRequestRowsInner.from_dict(api_v1_demands_bulk_post_request_rows_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


