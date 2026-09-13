# ApiV1DemandsDemandIdDispatchPost200ResponseData


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **UUID** |  | [optional] 
**status** | **str** |  | [optional] 
**dispatched** | **bool** | &#x60;false&#x60; ise sözleşme zaten yayınlanmıştı (&#x60;ALREADY&#x60;); yalnız davet fazı tekrar çalıştı, &#x60;demand.dispatched&#x60; webhook olayı TEKRAR yayılmadı.  | [optional] 
**credits** | [**ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits**](ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits.md) |  | [optional] 
**invitations** | [**ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations**](ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations.md) |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_demand_id_dispatch_post200_response_data import ApiV1DemandsDemandIdDispatchPost200ResponseData

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsDemandIdDispatchPost200ResponseData from a JSON string
api_v1_demands_demand_id_dispatch_post200_response_data_instance = ApiV1DemandsDemandIdDispatchPost200ResponseData.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsDemandIdDispatchPost200ResponseData.to_json())

# convert the object into a dict
api_v1_demands_demand_id_dispatch_post200_response_data_dict = api_v1_demands_demand_id_dispatch_post200_response_data_instance.to_dict()
# create an instance of ApiV1DemandsDemandIdDispatchPost200ResponseData from a dict
api_v1_demands_demand_id_dispatch_post200_response_data_from_dict = ApiV1DemandsDemandIdDispatchPost200ResponseData.from_dict(api_v1_demands_demand_id_dispatch_post200_response_data_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


