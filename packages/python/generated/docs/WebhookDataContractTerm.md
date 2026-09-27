# WebhookDataContractTerm

`contract.expiring` / `contract.ended` / `contract.advanced` olaylarının ortak verisi. Yalnız takip alanlarını taşır; taraf adı, e-postası veya sözleşme başlığı BU GÖVDEDE YER ALMAZ. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **UUID** |  | 
**term_end_date** | **date** | Olay anında platformda kayıtlı takip bitiş tarihi. | 
**renewal_type** | **str** |  | 
**notice_deadline** | **date** | İhbar için son gün (&#x60;term_end_date - notice_days&#x60;); tanımsızsa &#x60;null&#x60;. | 
**term_state** | **str** | Olay anındaki takip durumu (bkz. &#x60;ContractTerm.state&#x60;). | 
**due_key** | **str** | İşçinin tekillik anahtarı (aynı olayı tekrar göndermemek için kullandığı dahili kayıt). Formatı sabit değildir, opak string olarak ele alın; idempotency için &#x60;id&#x60; alanını kullanın.  | 

## Example

```python
from imzala_client.models.webhook_data_contract_term import WebhookDataContractTerm

# TODO update the JSON string below
json = "{}"
# create an instance of WebhookDataContractTerm from a JSON string
webhook_data_contract_term_instance = WebhookDataContractTerm.from_json(json)
# print the JSON string representation of the object
print(WebhookDataContractTerm.to_json())

# convert the object into a dict
webhook_data_contract_term_dict = webhook_data_contract_term_instance.to_dict()
# create an instance of WebhookDataContractTerm from a dict
webhook_data_contract_term_from_dict = WebhookDataContractTerm.from_dict(webhook_data_contract_term_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


