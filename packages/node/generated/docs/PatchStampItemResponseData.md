# PatchStampItemResponseData


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**item_id** | **number** |  | [optional] [default to undefined]
**page_id** | **number** |  | [optional] [default to undefined]
**document_id** | **string** |  | [optional] [default to undefined]
**party_id** | **string** |  | [optional] [default to undefined]
**source** | **string** | Güncelleme sonrası kaynak (FROM_SAVED kaşe INLINE olur) | [optional] [default to undefined]
**is_required** | **boolean** |  | [optional] [default to undefined]
**stamp_data** | **{ [key: string]: any; }** | Güncelleme sonrası kaşe verisinin tamamı. | [optional] [default to undefined]

## Example

```typescript
import { PatchStampItemResponseData } from '@imzala/server-sdk-node';

const instance: PatchStampItemResponseData = {
    item_id,
    page_id,
    document_id,
    party_id,
    source,
    is_required,
    stamp_data,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
