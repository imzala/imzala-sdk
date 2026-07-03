# DemandStatusPartiesInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**party_id** | **UUID** |  | [optional] 
**name** | **str** | Kısaltılmış görünen ad (Ahmet Y.) — KVKK maskeleme | [optional] 
**email_masked** | **str** | Maskeli e-posta (ah***@x.com) | [optional] 
**signed** | **bool** |  | [optional] 
**signed_at** | **datetime** |  | [optional] 
**rejected** | **bool** |  | [optional] 
**rejected_at** | **datetime** |  | [optional] 
**signing_url** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.demand_status_parties_inner import DemandStatusPartiesInner

# TODO update the JSON string below
json = "{}"
# create an instance of DemandStatusPartiesInner from a JSON string
demand_status_parties_inner_instance = DemandStatusPartiesInner.from_json(json)
# print the JSON string representation of the object
print(DemandStatusPartiesInner.to_json())

# convert the object into a dict
demand_status_parties_inner_dict = demand_status_parties_inner_instance.to_dict()
# create an instance of DemandStatusPartiesInner from a dict
demand_status_parties_inner_from_dict = DemandStatusPartiesInner.from_dict(demand_status_parties_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


