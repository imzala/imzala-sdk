# StampData

Yapılandırılmış kaşe verisi. Tüm alanlar isteğe bağlıdır. PATCH isteğinde `null` veya boş string alanı kaldırır.  Belgeye yalnız gerekli kişisel veriyi yazın; bu verilerin hukuka uygunluğu ve aydınlatması sizin sorumluluğunuzdadır. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**companyName** | **string** |  | [optional] [default to undefined]
**personalName** | **string** |  | [optional] [default to undefined]
**address** | **string** |  | [optional] [default to undefined]
**companyAddress** | **string** |  | [optional] [default to undefined]
**personalAddress** | **string** |  | [optional] [default to undefined]
**taxNumber** | **string** |  | [optional] [default to undefined]
**taxOffice** | **string** |  | [optional] [default to undefined]
**idNumber** | **string** |  | [optional] [default to undefined]
**phone** | **string** |  | [optional] [default to undefined]
**companyPhone** | **string** |  | [optional] [default to undefined]
**personalPhone** | **string** |  | [optional] [default to undefined]
**email** | **string** |  | [optional] [default to undefined]

## Example

```typescript
import { StampData } from '@imzala/server-sdk-node';

const instance: StampData = {
    companyName,
    personalName,
    address,
    companyAddress,
    personalAddress,
    taxNumber,
    taxOffice,
    idNumber,
    phone,
    companyPhone,
    personalPhone,
    email,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
