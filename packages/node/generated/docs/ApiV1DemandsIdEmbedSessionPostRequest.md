# ApiV1DemandsIdEmbedSessionPostRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**party_id** | **string** | Token üretilecek tarafın ID\&#39;si. &#x60;POST /api/v1/demands&#x60; veya &#x60;GET /api/v1/demands/{id}&#x60; cevabındaki &#x60;signing_urls[].party_id&#x60; alanından alınır.  | [default to undefined]
**locale** | **string** | İmzacı dili. &#x60;tr&#x60; (Türkçe), &#x60;en&#x60; (İngilizce) veya &#x60;null&#x60; (atanmadı).  Bilinen değerler: tr, en. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsIdEmbedSessionPostRequest } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsIdEmbedSessionPostRequest = {
    party_id,
    locale,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
