# CreatedDemand


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**title** | **str** |  | [optional] 
**status** | **str** |  | [optional] 
**template_id** | **UUID** |  | [optional] 
**signing_urls** | [**List[CreatedDemandSigningUrlsInner]**](CreatedDemandSigningUrlsInner.md) |  | [optional] 
**result_url** | **str** |  | [optional] 
**variables_applied** | **List[str]** | Uygulanan TÜM slug&#39;ların unique union&#39;ı (sorted). Geriye dönük uyumluluk için korunur — yeni entegrasyonlar variables_applied_root + variables_applied_by_party kullanmalı.  | [optional] 
**variables_applied_root** | **List[str]** | Root variables&#39;tan uygulanan slug listesi (sorted). | [optional] 
**variables_applied_by_party** | **Dict[str, List[str]]** | template_party_id → o partiye uygulanan slug listesi (sorted per party).  | [optional] 
**variables_ignored** | **List[str]** | Gönderdiğiniz AMA hiçbir item&#39;a uygulanmayan slug&#39;lar (unique, sorted). Boş olmayınca yazım hatası yapmışsınız demektir — kontrol edin.  | [optional] 

## Example

```python
from imzala_client.models.created_demand import CreatedDemand

# TODO update the JSON string below
json = "{}"
# create an instance of CreatedDemand from a JSON string
created_demand_instance = CreatedDemand.from_json(json)
# print the JSON string representation of the object
print(CreatedDemand.to_json())

# convert the object into a dict
created_demand_dict = created_demand_instance.to_dict()
# create an instance of CreatedDemand from a dict
created_demand_from_dict = CreatedDemand.from_dict(created_demand_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


