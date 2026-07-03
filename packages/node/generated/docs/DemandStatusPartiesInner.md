# DemandStatusPartiesInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**party_id** | **string** |  | [optional] [default to undefined]
**name** | **string** | Kısaltılmış görünen ad (Ahmet Y.) — KVKK maskeleme | [optional] [default to undefined]
**email_masked** | **string** | Maskeli e-posta (ah***@x.com) | [optional] [default to undefined]
**signed** | **boolean** |  | [optional] [default to undefined]
**signed_at** | **string** |  | [optional] [default to undefined]
**rejected** | **boolean** |  | [optional] [default to undefined]
**rejected_at** | **string** |  | [optional] [default to undefined]
**signing_url** | **string** |  | [optional] [default to undefined]

## Example

```typescript
import { DemandStatusPartiesInner } from '@imzala/server-sdk-node';

const instance: DemandStatusPartiesInner = {
    party_id,
    name,
    email_masked,
    signed,
    signed_at,
    rejected,
    rejected_at,
    signing_url,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
