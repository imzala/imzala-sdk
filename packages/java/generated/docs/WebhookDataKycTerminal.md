

# WebhookDataKycTerminal

KYC terminal olay verisi. Yalnız doğrulamanın sonucunu taşır: kimlik numarası, telefon, ad soyad ve belge görüntüleri bu gövdede YER ALMAZ. 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **UUID** | Doğrulama kaydının kimliği. Talep oluşturulurken dönen kimlikle aynıdır; aynı doğrulamaya ait olayları bu değerle ilişkilendirin.  |  |
|**verified** | **Boolean** | Doğrulamanın toplam sonucu. |  |
|**methods** | **List&lt;String&gt;** | Talepte istenen yöntemler (örn. &#x60;id_card&#x60;, &#x60;phone_otp&#x60;). |  |
|**steps** | [**List&lt;WebhookDataKycTerminalStepsInner&gt;**](WebhookDataKycTerminalStepsInner.md) |  |  |
|**externalRef** | **String** | Talep oluşturulurken verilen dış referans. |  |



