# ApiV1DemandsGet200ResponseDataDemandsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**title** | **str** |  | [optional] 
**status** | **str** |  | [optional] 
**created_at** | **datetime** |  | [optional] 
**completed_at** | **datetime** |  | [optional] 
**parties_total** | **int** |  | [optional] 
**parties_signed** | **int** |  | [optional] 
**pdf_url** | **str** | COMPLETED ise imzalı PDF public URL&#39;i | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_get200_response_data_demands_inner import ApiV1DemandsGet200ResponseDataDemandsInner

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsGet200ResponseDataDemandsInner from a JSON string
api_v1_demands_get200_response_data_demands_inner_instance = ApiV1DemandsGet200ResponseDataDemandsInner.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsGet200ResponseDataDemandsInner.to_json())

# convert the object into a dict
api_v1_demands_get200_response_data_demands_inner_dict = api_v1_demands_get200_response_data_demands_inner_instance.to_dict()
# create an instance of ApiV1DemandsGet200ResponseDataDemandsInner from a dict
api_v1_demands_get200_response_data_demands_inner_from_dict = ApiV1DemandsGet200ResponseDataDemandsInner.from_dict(api_v1_demands_get200_response_data_demands_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


