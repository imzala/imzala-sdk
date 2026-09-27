# DemandStatus


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**title** | **str** |  | [optional] 
**status** | **str** |  | [optional] 
**created_at** | **datetime** |  | [optional] 
**completed_at** | **datetime** |  | [optional] 
**parties** | [**List[DemandStatusPartiesInner]**](DemandStatusPartiesInner.md) |  | [optional] 
**stamp_items** | [**List[DemandStatusStampItemsInner]**](DemandStatusStampItemsInner.md) | Sözleşmedeki kaşe alanları. &#x60;PATCH /api/v1/demands/{id}/items/{itemId}/stamp&#x60; için alan kimliği buradan alınır. Kaşe içeriği bu listede dönmez.  | [optional] 
**result_url** | **str** |  | [optional] 
**pdf_url** | **str** | Sadece status&#x3D;COMPLETED iken dolu | [optional] 
**documents** | [**List[DemandDocumentStatus]**](DemandDocumentStatus.md) | Zarftaki belgelerin her biri için ayrı durum, &#x60;order&#x60; sırasıyla. Zarf genelindeki &#x60;status&#x60; alanı değişmez; bu liste hangi belgenin ne zaman tamamlandığını, hangisinin beklediğini, mühür durumunu ve her tarafın belge başına kararını ayrıca gösterir. Belge kaydı bulunmayan eski sözleşmelerde boş dizi döner.  | [optional] 
**term** | [**ContractTerm**](ContractTerm.md) | Sözleşmede süre/yenileme takibi tanımlı değilse &#x60;null&#x60;. | [optional] 
**archived_at** | **datetime** |  | [optional] 

## Example

```python
from imzala_client.models.demand_status import DemandStatus

# TODO update the JSON string below
json = "{}"
# create an instance of DemandStatus from a JSON string
demand_status_instance = DemandStatus.from_json(json)
# print the JSON string representation of the object
print(DemandStatus.to_json())

# convert the object into a dict
demand_status_dict = demand_status_instance.to_dict()
# create an instance of DemandStatus from a dict
demand_status_from_dict = DemandStatus.from_dict(demand_status_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


