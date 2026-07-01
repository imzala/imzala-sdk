# ApiV1TimestampsPostRequest1


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**file_base64** | **string** | Standart Base64 kodlanmış dosya içeriği (RFC 4648 §4 — A-Za-z0-9+/ alfabesi, \&#39;&#x3D;\&#39; padding). data URL öneki (&#x60;data:...;base64,&#x60;) ve URL-safe alfabe (&#x60;-_&#x60;) kabul edilmez.  | [default to undefined]
**file_name** | **string** | Orijinal dosya adı (uzantısıyla, ör. \&quot;belge.pdf\&quot;) | [default to undefined]
**description** | **string** | Kayıt açıklaması (opsiyonel, max 500 karakter) | [optional] [default to undefined]
**owner_first_name** | **string** | Dosya sahibinin adı (opsiyonel, kullanıcı beyanı) | [optional] [default to undefined]
**owner_last_name** | **string** | Dosya sahibinin soyadı (opsiyonel, kullanıcı beyanı) | [optional] [default to undefined]

## Example

```typescript
import { ApiV1TimestampsPostRequest1 } from '@imzala/server-sdk-node';

const instance: ApiV1TimestampsPostRequest1 = {
    file_base64,
    file_name,
    description,
    owner_first_name,
    owner_last_name,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
