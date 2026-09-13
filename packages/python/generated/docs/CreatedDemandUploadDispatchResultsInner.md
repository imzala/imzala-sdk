# CreatedDemandUploadDispatchResultsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**party_id** | **UUID** |  | [optional] 
**sms** | **bool** |  | [optional] 
**email** | **bool** |  | [optional] 
**whatsapp** | **bool** |  | [optional] 
**error** | **str** | Yalnız o taraf için gönderim patladığında döner. | [optional] 

## Example

```python
from imzala_client.models.created_demand_upload_dispatch_results_inner import CreatedDemandUploadDispatchResultsInner

# TODO update the JSON string below
json = "{}"
# create an instance of CreatedDemandUploadDispatchResultsInner from a JSON string
created_demand_upload_dispatch_results_inner_instance = CreatedDemandUploadDispatchResultsInner.from_json(json)
# print the JSON string representation of the object
print(CreatedDemandUploadDispatchResultsInner.to_json())

# convert the object into a dict
created_demand_upload_dispatch_results_inner_dict = created_demand_upload_dispatch_results_inner_instance.to_dict()
# create an instance of CreatedDemandUploadDispatchResultsInner from a dict
created_demand_upload_dispatch_results_inner_from_dict = CreatedDemandUploadDispatchResultsInner.from_dict(created_demand_upload_dispatch_results_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


