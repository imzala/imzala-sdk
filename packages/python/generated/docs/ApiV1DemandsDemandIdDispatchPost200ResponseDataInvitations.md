# ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**sent** | **int** | En az bir kanaldan başarıyla gönderilen taraf sayısı. | [optional] 
**results** | [**List[ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner]**](ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner.md) |  | [optional] 
**error** | **str** | &#x60;DISPATCH_SKIPPED&#x60; — sözleşme satırı okunamadı, körlemesine gönderim yapılmadı. (Taraf sayısı tavanı bu uçta davet fazından ÖNCE denetlenir ve **409 &#x60;DISPATCH_TOO_MANY&#x60;** olarak döner; 200 gövdesinde görünmez.)  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_demand_id_dispatch_post200_response_data_invitations import ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations from a JSON string
api_v1_demands_demand_id_dispatch_post200_response_data_invitations_instance = ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations.to_json())

# convert the object into a dict
api_v1_demands_demand_id_dispatch_post200_response_data_invitations_dict = api_v1_demands_demand_id_dispatch_post200_response_data_invitations_instance.to_dict()
# create an instance of ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations from a dict
api_v1_demands_demand_id_dispatch_post200_response_data_invitations_from_dict = ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations.from_dict(api_v1_demands_demand_id_dispatch_post200_response_data_invitations_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


