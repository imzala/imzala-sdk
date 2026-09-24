# CreatedDemandStampsAppliedInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**item_id** | **int** |  | [optional] 
**slug** | **str** |  | [optional] 
**document_id** | **str** | Kaşenin bulunduğu sözleşme belgesi (tek belgeli eski sözleşmede null olabilir). | [optional] 

## Example

```python
from imzala_client.models.created_demand_stamps_applied_inner import CreatedDemandStampsAppliedInner

# TODO update the JSON string below
json = "{}"
# create an instance of CreatedDemandStampsAppliedInner from a JSON string
created_demand_stamps_applied_inner_instance = CreatedDemandStampsAppliedInner.from_json(json)
# print the JSON string representation of the object
print(CreatedDemandStampsAppliedInner.to_json())

# convert the object into a dict
created_demand_stamps_applied_inner_dict = created_demand_stamps_applied_inner_instance.to_dict()
# create an instance of CreatedDemandStampsAppliedInner from a dict
created_demand_stamps_applied_inner_from_dict = CreatedDemandStampsAppliedInner.from_dict(created_demand_stamps_applied_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


