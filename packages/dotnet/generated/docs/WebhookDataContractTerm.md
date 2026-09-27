# ImzalaApiClient.Model.WebhookDataContractTerm
`contract.expiring` / `contract.ended` / `contract.advanced` olaylarının ortak verisi. Yalnız takip alanlarını taşır; taraf adı, e-postası veya sözleşme başlığı BU GÖVDEDE YER ALMAZ. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**DemandId** | **Guid** |  | 
**TermEndDate** | **DateOnly?** | Olay anında platformda kayıtlı takip bitiş tarihi. | 
**RenewalType** | **string** |  | 
**NoticeDeadline** | **DateOnly?** | İhbar için son gün (&#x60;term_end_date - notice_days&#x60;); tanımsızsa &#x60;null&#x60;. | 
**TermState** | **string** | Olay anındaki takip durumu (bkz. &#x60;ContractTerm.state&#x60;). | 
**DueKey** | **string** | İşçinin tekillik anahtarı (aynı olayı tekrar göndermemek için kullandığı dahili kayıt). Formatı sabit değildir, opak string olarak ele alın; idempotency için &#x60;id&#x60; alanını kullanın.  | 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

