

# EnvelopeDocument

Çok-belgeli imza zarfındaki tek bir belge. `assigned_party_ids` ve `decision_count` yalnız `GET .../documents?view=wizard` yanıtında bulunur; `has_timestamp` yalnız varsayılan (özet) görünümde bulunur — ikisi AYNI ANDA gelmez (dashboard'un iki ayrı iç DTO'sunun [`EnvelopeDocumentDTO` / `EnvelopeDocumentSummaryDTO`] tek şema altında birleştirilmiş hâli).

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **UUID** |  |  [optional] |
|**order** | **Integer** | 1&#39;den başlar. |  [optional] |
|**title** | **String** |  |  [optional] |
|**docKind** | **String** | Bilinen değerler: CONTRACT, KVKK_NOTICE, KVKK_CONSENT, PREINFO, PRICE_LIST, OTHER. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  [optional] |
|**isRequired** | **Boolean** |  |  [optional] |
|**signatureRequired** | **Boolean** |  |  [optional] |
|**assignedPartyIds** | **List&lt;UUID&gt;** | Yalnız &#x60;?view&#x3D;wizard&#x60; yanıtında bulunur. |  [optional] |
|**decisionCount** | **Integer** | Yalnız &#x60;?view&#x3D;wizard&#x60; yanıtında bulunur. Bu belge için verilmiş onay/red kararı sayısı.  |  [optional] |
|**sealingStatus** | **String** | Bilinen değerler: NONE, PENDING, SEALED, FAILED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  [optional] |
|**hasTimestamp** | **Boolean** | Yalnız varsayılan (özet) görünümde bulunur. &#x60;sealing_status&#x60; &#x60;SEALED&#x60; değilken daima &#x60;false&#x60; döner.  |  [optional] |
|**pageCount** | **Integer** |  |  [optional] |
|**completedAt** | **OffsetDateTime** |  |  [optional] |
|**progress** | [**DocumentProgress**](DocumentProgress.md) | Yalnız &#x60;GET .../documents&#x60; liste yanıtında bulunur. |  [optional] |
|**sealing** | [**DocumentSealing**](DocumentSealing.md) | Yalnız &#x60;GET .../documents&#x60; liste yanıtında bulunur. |  [optional] |
|**decisions** | [**List&lt;DocumentPartyDecision&gt;**](DocumentPartyDecision.md) | Yalnız &#x60;GET .../documents&#x60; liste yanıtında bulunur. Belge başına taraf kararları; &#x60;GET /api/v1/demands/{id}&#x60; yanıtındaki &#x60;documents[].decisions&#x60; ile aynıdır.  |  [optional] |
