# ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**charged** | **int** | Bu istekte düşülen kredi. | [optional] 
**refunded** | **int** | Bu istekte iade edilen kredi. | [optional] 
**expected** | **int** | Sözleşmenin toplam beklenen maliyeti. | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_demand_id_dispatch_post200_response_data_credits import ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits from a JSON string
api_v1_demands_demand_id_dispatch_post200_response_data_credits_instance = ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits.to_json())

# convert the object into a dict
api_v1_demands_demand_id_dispatch_post200_response_data_credits_dict = api_v1_demands_demand_id_dispatch_post200_response_data_credits_instance.to_dict()
# create an instance of ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits from a dict
api_v1_demands_demand_id_dispatch_post200_response_data_credits_from_dict = ApiV1DemandsDemandIdDispatchPost200ResponseDataCredits.from_dict(api_v1_demands_demand_id_dispatch_post200_response_data_credits_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


