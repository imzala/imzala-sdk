# DemandPage


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **int** |  | 
**order** | **int** | 1-based sayfa sırası | 

## Example

```python
from imzala_client.models.demand_page import DemandPage

# TODO update the JSON string below
json = "{}"
# create an instance of DemandPage from a JSON string
demand_page_instance = DemandPage.from_json(json)
# print the JSON string representation of the object
print(DemandPage.to_json())

# convert the object into a dict
demand_page_dict = demand_page_instance.to_dict()
# create an instance of DemandPage from a dict
demand_page_from_dict = DemandPage.from_dict(demand_page_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


