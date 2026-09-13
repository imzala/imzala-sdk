# WebhookDataKycTerminal

KYC terminal olay verisi. Yalnız doğrulamanın sonucunu taşır: kimlik numarası, telefon, ad soyad ve belge görüntüleri bu gövdede YER ALMAZ. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** | Doğrulama kaydının kimliği. Talep oluşturulurken dönen kimlikle aynıdır; aynı doğrulamaya ait olayları bu değerle ilişkilendirin.  | [default to undefined]
**verified** | **boolean** | Doğrulamanın toplam sonucu. | [default to undefined]
**methods** | **Array&lt;string&gt;** | Talepte istenen yöntemler (örn. &#x60;id_card&#x60;, &#x60;phone_otp&#x60;). | [default to undefined]
**steps** | [**Array&lt;WebhookDataKycTerminalStepsInner&gt;**](WebhookDataKycTerminalStepsInner.md) |  | [default to undefined]
**external_ref** | **string** | Talep oluşturulurken verilen dış referans. | [default to undefined]

## Example

```typescript
import { WebhookDataKycTerminal } from '@imzala/server-sdk-node';

const instance: WebhookDataKycTerminal = {
    id,
    verified,
    methods,
    steps,
    external_ref,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
