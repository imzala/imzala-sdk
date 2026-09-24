# ImzalaApiClient.Model.DocumentPartyDecision
Bir tarafın bu belge için kararı. Kişisel veri (ad, e-posta, telefon) içermez; tarafı `party_id` ile `parties` listesiyle eşleyin. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**PartyId** | **Guid** |  | [optional] 
**Decision** | **string** | Yürürlükteki karar. &#x60;PENDING&#x60; karar verilmediğini gösterir. &#x60;WITHDRAWN&#x60;, önce verilmiş bir onayın sonradan geri çekildiğini gösterir; geri çekme tamamlanmış belgeyi yeniden açmaz.  | [optional] 
**DecidedAt** | **DateTime?** | Kararın verildiği an. &#x60;WITHDRAWN&#x60; için geri çekilen onayın anıdır. &#x60;recorded&#x60; &#x60;false&#x60; iken &#x60;null&#x60; döner.  | [optional] 
**WithdrawnAt** | **DateTime?** | Yalnız &#x60;WITHDRAWN&#x60; için dolu; geri çekme anı. | [optional] 
**SignatureCollected** | **bool** | Bu kararla birlikte bu belgede dijital imza toplandı mı. | [optional] 
**Recorded** | **bool** | &#x60;false&#x60; ise karar belge bazında kayıtlı değildir; tek belgeli eski sözleşmelerde tarafın imzasından türetilmiştir ve karar delili olarak kullanılamaz.  | [optional] 
**ConsentGranted** | **bool?** | Yalnız &#x60;KVKK_CONSENT&#x60; belgesinde dolu. Taraf bu belgeyi onaylamış ve onayını geri çekmemişse &#x60;true&#x60;; reddetmiş, geri çekmiş veya karar vermemişse &#x60;false&#x60;. Bu alan yalnızca platformdaki onay kaydını yansıtır; açık rızanın KVKK anlamında hukuken geçerli olup olmadığını (belirli konuya ilişkin olması, bilgilendirmeye dayanması, özgür iradeyle verilmesi) imzala.org değerlendirmez. Reddedilen isteğe bağlı bir rıza hizmetin koşulu yapılmamalıdır. Diğer belge türlerinde &#x60;null&#x60;.  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

