# ContractNumber

Sözleşmeye panelde tanımlı bir numaratörden verilen sözleşme numarası. Numara sözleşme imzaya gönderilirken verilir ve sonradan değişmez.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**sequence_id** | **string** | Numarayı veren numaratörün kimliği. | [default to undefined]
**value** | **string** | Biçimlendirilmiş sözleşme numarası (önek, sıra numarası ve sonek). | [default to undefined]

## Example

```typescript
import { ContractNumber } from '@imzala/server-sdk-node';

const instance: ContractNumber = {
    sequence_id,
    value,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
