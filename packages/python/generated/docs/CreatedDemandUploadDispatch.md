# CreatedDemandUploadDispatch

YALNIZ istekte `send_invitations` gönderildiğinde döner. Davet gönderimi 201'i düşürmez (sözleşme yaratılmış ve kredi düşülmüştür), bu yüzden hatalar burada taraf bazında raporlanır. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**requested** | **bool** |  | [optional] 
**sent** | **int** | En az bir kanaldan (SMS/e-posta/WhatsApp) davet **gönderilen taraf** sayısı — kanal sayısı DEĞİL. (&#x60;POST /api/v1/demands&#x60; yanıtındaki &#x60;dispatched&#x60; alanı kanal sayar; bu alan saymaz.)  | [optional] 
**results** | [**List[CreatedDemandUploadDispatchResultsInner]**](CreatedDemandUploadDispatchResultsInner.md) |  | [optional] 
**error** | **str** | &#x60;DISPATCH_SKIPPED&#x60; — sözleşme satırı okunamadı, bildirim ayarları bilinmediği için hiç davet gönderilmedi. &#x60;DISPATCH_TOO_MANY&#x60; — taraf sayısı 20 sınırını aşıyor.  | [optional] 

## Example

```python
from imzala_client.models.created_demand_upload_dispatch import CreatedDemandUploadDispatch

# TODO update the JSON string below
json = "{}"
# create an instance of CreatedDemandUploadDispatch from a JSON string
created_demand_upload_dispatch_instance = CreatedDemandUploadDispatch.from_json(json)
# print the JSON string representation of the object
print(CreatedDemandUploadDispatch.to_json())

# convert the object into a dict
created_demand_upload_dispatch_dict = created_demand_upload_dispatch_instance.to_dict()
# create an instance of CreatedDemandUploadDispatch from a dict
created_demand_upload_dispatch_from_dict = CreatedDemandUploadDispatch.from_dict(created_demand_upload_dispatch_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


