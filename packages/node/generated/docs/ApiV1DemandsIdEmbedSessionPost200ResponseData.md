# ApiV1DemandsIdEmbedSessionPost200ResponseData


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**embed_token** | **string** | Tek kullanımlık, kısa ömürlü gömülü imza token\&#39;ı | [optional] [default to undefined]
**expires_at** | **string** | Token geçerlilik bitiş zamanı (ISO 8601 UTC) | [optional] [default to undefined]
**embed_url** | **string** | &#x60;&lt;iframe src&#x3D;\&quot;\&quot;&gt;&#x60; alanına yerleştirilecek tam URL. &#x60;https://e.imzala.org/embed/sign?token&#x3D;&lt;embed_token&gt;&#x60; formatında.  | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsIdEmbedSessionPost200ResponseData } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsIdEmbedSessionPost200ResponseData = {
    embed_token,
    expires_at,
    embed_url,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
