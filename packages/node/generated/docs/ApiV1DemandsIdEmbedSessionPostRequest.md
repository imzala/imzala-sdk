# ApiV1DemandsIdEmbedSessionPostRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**party_id** | **string** | Token üretilecek tarafın ID\&#39;si. &#x60;POST /api/v1/demands&#x60; veya &#x60;GET /api/v1/demands/{id}&#x60; cevabındaki &#x60;signing_urls[].party_id&#x60; alanından alınır.  | [default to undefined]

## Example

```typescript
import { ApiV1DemandsIdEmbedSessionPostRequest } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsIdEmbedSessionPostRequest = {
    party_id,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
