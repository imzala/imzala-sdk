# ImzalaApiClient.Model.EnvelopeDocument
Çok-belgeli imza zarfındaki tek bir belge. `assigned_party_ids` ve `decision_count` yalnız `GET .../documents?view=wizard` yanıtında bulunur; `has_timestamp` yalnız varsayılan (özet) görünümde bulunur — ikisi AYNI ANDA gelmez (dashboard'un iki ayrı iç DTO'sunun [`EnvelopeDocumentDTO` / `EnvelopeDocumentSummaryDTO`] tek şema altında birleştirilmiş hâli). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Id** | **Guid** |  | [optional] 
**Order** | **int** | 1&#39;den başlar. | [optional] 
**Title** | **string** |  | [optional] 
**DocKind** | **string** |  | [optional] 
**IsRequired** | **bool** |  | [optional] 
**SignatureRequired** | **bool** |  | [optional] 
**AssignedPartyIds** | **List&lt;Guid&gt;** | Yalnız &#x60;?view&#x3D;wizard&#x60; yanıtında bulunur. | [optional] 
**DecisionCount** | **int** | Yalnız &#x60;?view&#x3D;wizard&#x60; yanıtında bulunur. Bu belge için verilmiş onay/red kararı sayısı.  | [optional] 
**SealingStatus** | **string** |  | [optional] 
**HasTimestamp** | **bool** | Yalnız varsayılan (özet) görünümde bulunur. &#x60;sealing_status&#x60; &#x60;SEALED&#x60; değilken daima &#x60;false&#x60; döner.  | [optional] 
**PageCount** | **int** |  | [optional] 
**CompletedAt** | **DateTime?** |  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

