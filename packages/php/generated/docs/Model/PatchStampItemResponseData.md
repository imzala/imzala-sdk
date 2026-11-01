# PatchStampItemResponseData

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**item_id** | **int** |  | [optional]
**page_id** | **int** |  | [optional]
**document_id** | **string** |  | [optional]
**party_id** | **string** |  | [optional]
**source** | **string** | Güncelleme sonrası kaynak (FROM_SAVED kaşe INLINE olur)  Bilinen değerler: INLINE, FILLER_PROVIDES. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. | [optional]
**is_required** | **bool** |  | [optional]
**stamp_data** | **array<string,mixed>** | Güncelleme sonrası kaşe verisinin tamamı. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
