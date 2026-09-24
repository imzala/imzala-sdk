# DocumentProgress

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**status** | **string** | &#x60;PENDING&#x60;: henüz kimse karar vermedi. &#x60;PARTIAL&#x60;: tarafların bir kısmı karar verdi. &#x60;COMPLETED&#x60;: atanmış tüm taraflar karar verdi. | [optional]
**active_assigned_party_count** | **int** | Belgeye atanmış ve doldurulmuş (boş bırakılmamış) taraf sayısı. | [optional]
**decided_party_count** | **int** | Bunlardan bu belge için karar vermiş olanların sayısı. | [optional]
**completed_at** | **\DateTime** | Belgenin kendi tamamlanma anı. Sistem bu anı kaydetmediyse &#x60;null&#x60; döner; zarfın tamamlanma zamanı bu alana kopyalanmaz. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
