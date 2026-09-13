# FieldLayoutDiagnostic

Alan yerleşimi tanısı. `code` makine sözleşmesidir, `message` insan metnidir (TR). **Bilinmeyen `code` toleranslı okunmalıdır**: yeni kod eklemek kırıcı değişiklik sayılmaz, istemci tanımadığı kodda `message` alanına düşmelidir.  Belgenizden çıkarılan hiçbir metin (çapa metni, eşleşen satır, sayfa metni, dosya adı) bu yapıya girmez. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**code** | **string** |  | [default to undefined]
**severity** | **string** |  | [default to undefined]
**item_id** | **number** | Şablon alanının kimliği. &#x60;-1&#x60; belge/taraf düzeyindeki tanılar için kullanılan sentinel değerdir.  | [default to undefined]
**slug** | **string** |  | [optional] [default to undefined]
**label** | **string** |  | [optional] [default to undefined]
**item_type** | **string** |  | [default to undefined]
**page** | **number** | 1 tabanlı sayfa numarası; bilinmiyorsa null. | [optional] [default to undefined]
**message** | **string** |  | [default to undefined]
**detail** | **{ [key: string]: any; }** | Koda göre değişen, kapalı bir anahtar kümesi (yalnız sayısal değerler ve alan kimlikleri).  | [optional] [default to undefined]

## Example

```typescript
import { FieldLayoutDiagnostic } from '@imzala/server-sdk-node';

const instance: FieldLayoutDiagnostic = {
    code,
    severity,
    item_id,
    slug,
    label,
    item_type,
    page,
    message,
    detail,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
