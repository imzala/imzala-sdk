# ApiV1DemandsPost400Response


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**error** | **str** |  | [optional] 
**code** | **str** |  | [optional] 
**var_field** | **str** | Bazı kodlarda (ör. &#x60;TERM_INVALID&#x60;) hangi alanın reddedildiğini gösterir. Yalnız bu tür kodlarda bulunur; her hata gövdesinde YOKTUR.  | [optional] 
**details** | [**DocumentSelectionErrorDetails**](DocumentSelectionErrorDetails.md) |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_post400_response import ApiV1DemandsPost400Response

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsPost400Response from a JSON string
api_v1_demands_post400_response_instance = ApiV1DemandsPost400Response.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsPost400Response.to_json())

# convert the object into a dict
api_v1_demands_post400_response_dict = api_v1_demands_post400_response_instance.to_dict()
# create an instance of ApiV1DemandsPost400Response from a dict
api_v1_demands_post400_response_from_dict = ApiV1DemandsPost400Response.from_dict(api_v1_demands_post400_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


