# WebhookDataKycTerminal

KYC terminal olay verisi. Yalnız doğrulamanın sonucunu taşır: kimlik numarası, telefon, ad soyad ve belge görüntüleri bu gövdede YER ALMAZ. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** | Doğrulama kaydının kimliği. Talep oluşturulurken dönen kimlikle aynıdır; aynı doğrulamaya ait olayları bu değerle ilişkilendirin.  | 
**verified** | **bool** | Doğrulamanın toplam sonucu. | 
**methods** | **List[str]** | Talepte istenen yöntemler (örn. &#x60;id_card&#x60;, &#x60;phone_otp&#x60;). | 
**steps** | [**List[WebhookDataKycTerminalStepsInner]**](WebhookDataKycTerminalStepsInner.md) |  | 
**external_ref** | **str** | Talep oluşturulurken verilen dış referans. | 

## Example

```python
from imzala_client.models.webhook_data_kyc_terminal import WebhookDataKycTerminal

# TODO update the JSON string below
json = "{}"
# create an instance of WebhookDataKycTerminal from a JSON string
webhook_data_kyc_terminal_instance = WebhookDataKycTerminal.from_json(json)
# print the JSON string representation of the object
print(WebhookDataKycTerminal.to_json())

# convert the object into a dict
webhook_data_kyc_terminal_dict = webhook_data_kyc_terminal_instance.to_dict()
# create an instance of WebhookDataKycTerminal from a dict
webhook_data_kyc_terminal_from_dict = WebhookDataKycTerminal.from_dict(webhook_data_kyc_terminal_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


