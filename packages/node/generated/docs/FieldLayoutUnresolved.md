# FieldLayoutUnresolved

`field_template_id` ile gönderilen bir istekte alan yerleşimi çözülemediğinde dönen gövde. **Sözleşme oluşturulmaz, kredi düşülmez.** 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **boolean** |  | [optional] [default to undefined]
**error** | **string** |  | [optional] [default to undefined]
**message** | **string** |  | [optional] [default to undefined]
**diagnostics** | [**Array&lt;FieldLayoutDiagnostic&gt;**](FieldLayoutDiagnostic.md) |  | [optional] [default to undefined]
**warnings** | [**Array&lt;FieldLayoutWarning&gt;**](FieldLayoutWarning.md) |  | [optional] [default to undefined]

## Example

```typescript
import { FieldLayoutUnresolved } from '@imzala/server-sdk-node';

const instance: FieldLayoutUnresolved = {
    success,
    error,
    message,
    diagnostics,
    warnings,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
