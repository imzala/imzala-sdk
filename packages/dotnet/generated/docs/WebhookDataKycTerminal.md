# ImzalaApiClient.Model.WebhookDataKycTerminal
KYC terminal olay verisi. Yalnız doğrulamanın sonucunu taşır: kimlik numarası, telefon, ad soyad ve belge görüntüleri bu gövdede YER ALMAZ. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Id** | **Guid** | Doğrulama kaydının kimliği. Talep oluşturulurken dönen kimlikle aynıdır; aynı doğrulamaya ait olayları bu değerle ilişkilendirin.  | 
**Verified** | **bool** | Doğrulamanın toplam sonucu. | 
**Methods** | **List&lt;string&gt;** | Talepte istenen yöntemler (örn. &#x60;id_card&#x60;, &#x60;phone_otp&#x60;). | 
**Steps** | [**List&lt;WebhookDataKycTerminalStepsInner&gt;**](WebhookDataKycTerminalStepsInner.md) |  | 
**ExternalRef** | **string** | Talep oluşturulurken verilen dış referans. | 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

