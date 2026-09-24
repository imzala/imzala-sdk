# DocumentProgress

Belgeye atanmış tarafların karar ilerlemesi. Mühürden ve rızadan bağımsızdır: `COMPLETED`, belgeye atanmış tüm tarafların bu belge için karar verdiğini gösterir (isteğe bağlı bir belgede ret de karardır). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**status** | **string** | &#x60;PENDING&#x60;: henüz kimse karar vermedi. &#x60;PARTIAL&#x60;: tarafların bir kısmı karar verdi. &#x60;COMPLETED&#x60;: atanmış tüm taraflar karar verdi.  | [optional] [default to undefined]
**active_assigned_party_count** | **number** | Belgeye atanmış ve doldurulmuş (boş bırakılmamış) taraf sayısı. | [optional] [default to undefined]
**decided_party_count** | **number** | Bunlardan bu belge için karar vermiş olanların sayısı. | [optional] [default to undefined]
**completed_at** | **string** | Belgenin kendi tamamlanma anı. Sistem bu anı kaydetmediyse &#x60;null&#x60; döner; zarfın tamamlanma zamanı bu alana kopyalanmaz.  | [optional] [default to undefined]

## Example

```typescript
import { DocumentProgress } from '@imzala/server-sdk-node';

const instance: DocumentProgress = {
    status,
    active_assigned_party_count,
    decided_party_count,
    completed_at,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
