# ApiV1DemandsIdPartiesPartyIdResendPost200Response


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**data** | [**ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData**](ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData.md) |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_id_parties_party_id_resend_post200_response import ApiV1DemandsIdPartiesPartyIdResendPost200Response

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsIdPartiesPartyIdResendPost200Response from a JSON string
api_v1_demands_id_parties_party_id_resend_post200_response_instance = ApiV1DemandsIdPartiesPartyIdResendPost200Response.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsIdPartiesPartyIdResendPost200Response.to_json())

# convert the object into a dict
api_v1_demands_id_parties_party_id_resend_post200_response_dict = api_v1_demands_id_parties_party_id_resend_post200_response_instance.to_dict()
# create an instance of ApiV1DemandsIdPartiesPartyIdResendPost200Response from a dict
api_v1_demands_id_parties_party_id_resend_post200_response_from_dict = ApiV1DemandsIdPartiesPartyIdResendPost200Response.from_dict(api_v1_demands_id_parties_party_id_resend_post200_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


