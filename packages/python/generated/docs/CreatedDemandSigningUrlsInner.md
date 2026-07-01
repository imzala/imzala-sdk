# CreatedDemandSigningUrlsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**party_id** | **UUID** |  | [optional] 
**first_name** | **str** |  | [optional] 
**last_name** | **str** |  | [optional] 
**email** | **str** |  | [optional] 
**phone** | **str** |  | [optional] 
**signing_url** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.created_demand_signing_urls_inner import CreatedDemandSigningUrlsInner

# TODO update the JSON string below
json = "{}"
# create an instance of CreatedDemandSigningUrlsInner from a JSON string
created_demand_signing_urls_inner_instance = CreatedDemandSigningUrlsInner.from_json(json)
# print the JSON string representation of the object
print(CreatedDemandSigningUrlsInner.to_json())

# convert the object into a dict
created_demand_signing_urls_inner_dict = created_demand_signing_urls_inner_instance.to_dict()
# create an instance of CreatedDemandSigningUrlsInner from a dict
created_demand_signing_urls_inner_from_dict = CreatedDemandSigningUrlsInner.from_dict(created_demand_signing_urls_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


