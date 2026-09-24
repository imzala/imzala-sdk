# DocumentPartyDecision

Bir tarafın bu belge için kararı. Kişisel veri (ad, e-posta, telefon) içermez; tarafı `party_id` ile `parties` listesiyle eşleyin. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**party_id** | **string** |  | [optional] [default to undefined]
**decision** | **string** | Yürürlükteki karar. &#x60;PENDING&#x60; karar verilmediğini gösterir. &#x60;WITHDRAWN&#x60;, önce verilmiş bir onayın sonradan geri çekildiğini gösterir; geri çekme tamamlanmış belgeyi yeniden açmaz.  | [optional] [default to undefined]
**decided_at** | **string** | Kararın verildiği an. &#x60;WITHDRAWN&#x60; için geri çekilen onayın anıdır. &#x60;recorded&#x60; &#x60;false&#x60; iken &#x60;null&#x60; döner.  | [optional] [default to undefined]
**withdrawn_at** | **string** | Yalnız &#x60;WITHDRAWN&#x60; için dolu; geri çekme anı. | [optional] [default to undefined]
**signature_collected** | **boolean** | Bu kararla birlikte bu belgede dijital imza toplandı mı. | [optional] [default to undefined]
**recorded** | **boolean** | &#x60;false&#x60; ise karar belge bazında kayıtlı değildir; tek belgeli eski sözleşmelerde tarafın imzasından türetilmiştir ve karar delili olarak kullanılamaz.  | [optional] [default to undefined]
**consent_granted** | **boolean** | Yalnız &#x60;KVKK_CONSENT&#x60; belgesinde dolu. Taraf bu belgeyi onaylamış ve onayını geri çekmemişse &#x60;true&#x60;; reddetmiş, geri çekmiş veya karar vermemişse &#x60;false&#x60;. Bu alan yalnızca platformdaki onay kaydını yansıtır; açık rızanın KVKK anlamında hukuken geçerli olup olmadığını (belirli konuya ilişkin olması, bilgilendirmeye dayanması, özgür iradeyle verilmesi) imzala.org değerlendirmez. Reddedilen isteğe bağlı bir rıza hizmetin koşulu yapılmamalıdır. Diğer belge türlerinde &#x60;null&#x60;.  | [optional] [default to undefined]

## Example

```typescript
import { DocumentPartyDecision } from '@imzala/server-sdk-node';

const instance: DocumentPartyDecision = {
    party_id,
    decision,
    decided_at,
    withdrawn_at,
    signature_collected,
    recorded,
    consent_granted,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
