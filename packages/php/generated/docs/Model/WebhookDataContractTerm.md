# WebhookDataContractTerm

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **string** |  |
**term_end_date** | **\DateTime** | Olay anında platformda kayıtlı takip bitiş tarihi. |
**renewal_type** | **string** |  |
**notice_deadline** | **\DateTime** | İhbar için son gün (&#x60;term_end_date - notice_days&#x60;); tanımsızsa &#x60;null&#x60;. |
**term_state** | **string** | Olay anındaki takip durumu (bkz. &#x60;ContractTerm.state&#x60;). |
**due_key** | **string** | İşçinin tekillik anahtarı (aynı olayı tekrar göndermemek için kullandığı dahili kayıt). Formatı sabit değildir, opak string olarak ele alın; idempotency için &#x60;id&#x60; alanını kullanın. |

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
