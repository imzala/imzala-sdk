

# PatchStampItemResponseData


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**itemId** | **Integer** |  |  [optional] |
|**pageId** | **Integer** |  |  [optional] |
|**documentId** | **String** |  |  [optional] |
|**partyId** | **UUID** |  |  [optional] |
|**source** | **String** | Güncelleme sonrası kaynak (FROM_SAVED kaşe INLINE olur)  Bilinen değerler: INLINE, FILLER_PROVIDES. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  [optional] |
|**isRequired** | **Boolean** |  |  [optional] |
|**stampData** | **Map&lt;String, Object&gt;** | Güncelleme sonrası kaşe verisinin tamamı. |  [optional] |
