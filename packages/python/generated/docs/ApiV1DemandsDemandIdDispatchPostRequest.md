# ApiV1DemandsDemandIdDispatchPostRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**send_invitations** | [**ApiV1DemandsDemandIdDispatchPostRequestSendInvitations**](ApiV1DemandsDemandIdDispatchPostRequestSendInvitations.md) |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_demand_id_dispatch_post_request import ApiV1DemandsDemandIdDispatchPostRequest

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsDemandIdDispatchPostRequest from a JSON string
api_v1_demands_demand_id_dispatch_post_request_instance = ApiV1DemandsDemandIdDispatchPostRequest.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsDemandIdDispatchPostRequest.to_json())

# convert the object into a dict
api_v1_demands_demand_id_dispatch_post_request_dict = api_v1_demands_demand_id_dispatch_post_request_instance.to_dict()
# create an instance of ApiV1DemandsDemandIdDispatchPostRequest from a dict
api_v1_demands_demand_id_dispatch_post_request_from_dict = ApiV1DemandsDemandIdDispatchPostRequest.from_dict(api_v1_demands_demand_id_dispatch_post_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


