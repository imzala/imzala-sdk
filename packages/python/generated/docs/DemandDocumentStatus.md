# DemandDocumentStatus

Zarftaki tek bir belgenin durumu.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**order** | **int** | 1&#39;den başlar. | [optional] 
**title** | **str** |  | [optional] 
**doc_kind** | **str** |  | [optional] 
**is_required** | **bool** |  | [optional] 
**signature_required** | **bool** |  | [optional] 
**assigned_party_ids** | **List[UUID]** |  | [optional] 
**progress** | [**DocumentProgress**](DocumentProgress.md) |  | [optional] 
**sealing** | [**DocumentSealing**](DocumentSealing.md) |  | [optional] 
**decisions** | [**List[DocumentPartyDecision]**](DocumentPartyDecision.md) |  | [optional] 

## Example

```python
from imzala_client.models.demand_document_status import DemandDocumentStatus

# TODO update the JSON string below
json = "{}"
# create an instance of DemandDocumentStatus from a JSON string
demand_document_status_instance = DemandDocumentStatus.from_json(json)
# print the JSON string representation of the object
print(DemandDocumentStatus.to_json())

# convert the object into a dict
demand_document_status_dict = demand_document_status_instance.to_dict()
# create an instance of DemandDocumentStatus from a dict
demand_document_status_from_dict = DemandDocumentStatus.from_dict(demand_document_status_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


