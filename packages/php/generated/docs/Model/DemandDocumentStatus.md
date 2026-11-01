# DemandDocumentStatus

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional]
**order** | **int** | 1&#39;den başlar. | [optional]
**title** | **string** |  | [optional]
**doc_kind** | **string** | Bilinen değerler: CONTRACT, KVKK_NOTICE, KVKK_CONSENT, PREINFO, PRICE_LIST, OTHER. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. | [optional]
**is_required** | **bool** |  | [optional]
**signature_required** | **bool** |  | [optional]
**assigned_party_ids** | **string[]** |  | [optional]
**progress** | [**\Imzala\Client\Model\DocumentProgress**](DocumentProgress.md) |  | [optional]
**sealing** | [**\Imzala\Client\Model\DocumentSealing**](DocumentSealing.md) |  | [optional]
**decisions** | [**\Imzala\Client\Model\DocumentPartyDecision[]**](DocumentPartyDecision.md) |  | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
