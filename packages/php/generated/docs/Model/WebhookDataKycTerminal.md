# WebhookDataKycTerminal

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** | Doğrulama kaydının kimliği. Talep oluşturulurken dönen kimlikle aynıdır; aynı doğrulamaya ait olayları bu değerle ilişkilendirin. |
**verified** | **bool** | Doğrulamanın toplam sonucu. |
**methods** | **string[]** | Talepte istenen yöntemler (örn. &#x60;id_card&#x60;, &#x60;phone_otp&#x60;). |
**steps** | [**\Imzala\Client\Model\WebhookDataKycTerminalStepsInner[]**](WebhookDataKycTerminalStepsInner.md) |  |
**external_ref** | **string** | Talep oluşturulurken verilen dış referans. |

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
