# ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**party_id** | **UUID** |  | [optional] 
**sms** | **bool** |  | [optional] 
**email** | **bool** |  | [optional] 
**whatsapp** | **bool** |  | [optional] 
**error** | **str** | Yalnız o taraf için dolar. &#x60;DISPATCH_FAILED&#x60; — gönderim patladı. &#x60;RECIPIENT_QUOTA_EXCEEDED&#x60; — alıcı ekseni freni (telefon başına 3/sa + 10/gün); yalnız bu taraf ATLANDI, diğerleri gönderilmeye devam etti.  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_demand_id_dispatch_post200_response_data_invitations_results_inner import ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner from a JSON string
api_v1_demands_demand_id_dispatch_post200_response_data_invitations_results_inner_instance = ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner.to_json())

# convert the object into a dict
api_v1_demands_demand_id_dispatch_post200_response_data_invitations_results_inner_dict = api_v1_demands_demand_id_dispatch_post200_response_data_invitations_results_inner_instance.to_dict()
# create an instance of ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner from a dict
api_v1_demands_demand_id_dispatch_post200_response_data_invitations_results_inner_from_dict = ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner.from_dict(api_v1_demands_demand_id_dispatch_post200_response_data_invitations_results_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


