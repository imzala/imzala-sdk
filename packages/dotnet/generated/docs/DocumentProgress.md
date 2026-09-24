# ImzalaApiClient.Model.DocumentProgress
Belgeye atanmış tarafların karar ilerlemesi. Mühürden ve rızadan bağımsızdır: `COMPLETED`, belgeye atanmış tüm tarafların bu belge için karar verdiğini gösterir (isteğe bağlı bir belgede ret de karardır). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Status** | **string** | &#x60;PENDING&#x60;: henüz kimse karar vermedi. &#x60;PARTIAL&#x60;: tarafların bir kısmı karar verdi. &#x60;COMPLETED&#x60;: atanmış tüm taraflar karar verdi.  | [optional] 
**ActiveAssignedPartyCount** | **int** | Belgeye atanmış ve doldurulmuş (boş bırakılmamış) taraf sayısı. | [optional] 
**DecidedPartyCount** | **int** | Bunlardan bu belge için karar vermiş olanların sayısı. | [optional] 
**CompletedAt** | **DateTime?** | Belgenin kendi tamamlanma anı. Sistem bu anı kaydetmediyse &#x60;null&#x60; döner; zarfın tamamlanma zamanı bu alana kopyalanmaz.  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

