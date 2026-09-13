# ApiV1DemandsDemandIdDispatchPostRequestSendInvitations

Hangi kanaldan davet gideceğini **daraltır** (sözleşmenin kendi bildirim ayarlarını AÇAMAZ, yalnız kapatabilir). **Kapalı:** `false` (boolean), `\"false\"`, `\"0\"`, `\"off\"`, `\"no\"`, `\"hayir\"`, `\"hayır\"` → hiçbir davet gönderilmez, sözleşme yalnız yayına alınır. Tanınmayan değer **400 `INVALID_SEND_INVITATIONS`** döner (fail-closed — davet gitti sanıp gitmemesindense hata görmek yeğdir). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------

## Example

```python
from imzala_client.models.api_v1_demands_demand_id_dispatch_post_request_send_invitations import ApiV1DemandsDemandIdDispatchPostRequestSendInvitations

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsDemandIdDispatchPostRequestSendInvitations from a JSON string
api_v1_demands_demand_id_dispatch_post_request_send_invitations_instance = ApiV1DemandsDemandIdDispatchPostRequestSendInvitations.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsDemandIdDispatchPostRequestSendInvitations.to_json())

# convert the object into a dict
api_v1_demands_demand_id_dispatch_post_request_send_invitations_dict = api_v1_demands_demand_id_dispatch_post_request_send_invitations_instance.to_dict()
# create an instance of ApiV1DemandsDemandIdDispatchPostRequestSendInvitations from a dict
api_v1_demands_demand_id_dispatch_post_request_send_invitations_from_dict = ApiV1DemandsDemandIdDispatchPostRequestSendInvitations.from_dict(api_v1_demands_demand_id_dispatch_post_request_send_invitations_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


