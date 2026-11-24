# WebhookDataDemandExpired


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **UUID** |  |
**title** | **str** |  |
**expiry_date** | **datetime** |  |
**parties** | [**List[WebhookDataDemandExpiredPartiesInner]**](WebhookDataDemandExpiredPartiesInner.md) |  |
**contract_numbers** | [**List[ContractNumber]**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları; numara yoksa boş dizi. Numaralar okunamadığında bildirim bu alan olmadan gönderilir.  | [optional]
**external_ref** | **str** | Sözleşmenin bildirim anındaki takip numarası; tanımlı değilse &#x60;null&#x60;. Takip numarası tekrar edebilir: alıcı tarafta tekrar işlemeyi zarftaki olay &#x60;id&#x60;&#39;si ile önleyin, &#x60;external_ref&#x60; ile değil. Sözleşme bilgisi okunamadığında bildirim bu alan olmadan gönderilir.  | [optional]

## Example

```python
from imzala_client.models.webhook_data_demand_expired import WebhookDataDemandExpired

# TODO update the JSON string below
json = "{}"
# create an instance of WebhookDataDemandExpired from a JSON string
webhook_data_demand_expired_instance = WebhookDataDemandExpired.from_json(json)
# print the JSON string representation of the object
print(WebhookDataDemandExpired.to_json())

# convert the object into a dict
webhook_data_demand_expired_dict = webhook_data_demand_expired_instance.to_dict()
# create an instance of WebhookDataDemandExpired from a dict
webhook_data_demand_expired_from_dict = WebhookDataDemandExpired.from_dict(webhook_data_demand_expired_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
