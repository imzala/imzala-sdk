# ImzalaApiClient.Model.PatchStampItemResponseData

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**ItemId** | **int** |  | [optional]
**PageId** | **int** |  | [optional]
**DocumentId** | **string** |  | [optional]
**PartyId** | **Guid?** |  | [optional]
**Source** | **string** | Güncelleme sonrası kaynak (FROM_SAVED kaşe INLINE olur)  Bilinen değerler: INLINE, FILLER_PROVIDES. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. | [optional]
**IsRequired** | **bool** |  | [optional]
**StampData** | **Dictionary&lt;string, Object&gt;** | Güncelleme sonrası kaşe verisinin tamamı. | [optional]

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
