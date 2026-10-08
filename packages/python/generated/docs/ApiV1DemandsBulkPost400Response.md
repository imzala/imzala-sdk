# ApiV1DemandsBulkPost400Response

Toplu oluşturma ön kontrolünün açık hata zarfı; alanlar hata türüne göre bulunur.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional]
**error** | **str** |  | [optional]
**code** | **str** |  | [optional]
**message** | **str** |  | [optional]
**var_field** | **str** |  | [optional]
**details** | **Dict[str, object]** |  | [optional]

## Example

```python
from imzala_client.models.api_v1_demands_bulk_post400_response import ApiV1DemandsBulkPost400Response

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsBulkPost400Response from a JSON string
api_v1_demands_bulk_post400_response_instance = ApiV1DemandsBulkPost400Response.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsBulkPost400Response.to_json())

# convert the object into a dict
api_v1_demands_bulk_post400_response_dict = api_v1_demands_bulk_post400_response_instance.to_dict()
# create an instance of ApiV1DemandsBulkPost400Response from a dict
api_v1_demands_bulk_post400_response_from_dict = ApiV1DemandsBulkPost400Response.from_dict(api_v1_demands_bulk_post400_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
