

# DemandDocumentStatus

Zarftaki tek bir belgenin durumu.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **UUID** |  |  [optional] |
|**order** | **Integer** | 1&#39;den başlar. |  [optional] |
|**title** | **String** |  |  [optional] |
|**docKind** | **String** | Bilinen değerler: CONTRACT, KVKK_NOTICE, KVKK_CONSENT, PREINFO, PRICE_LIST, OTHER. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  [optional] |
|**isRequired** | **Boolean** |  |  [optional] |
|**signatureRequired** | **Boolean** |  |  [optional] |
|**assignedPartyIds** | **List&lt;UUID&gt;** |  |  [optional] |
|**progress** | [**DocumentProgress**](DocumentProgress.md) |  |  [optional] |
|**sealing** | [**DocumentSealing**](DocumentSealing.md) |  |  [optional] |
|**decisions** | [**List&lt;DocumentPartyDecision&gt;**](DocumentPartyDecision.md) |  |  [optional] |
