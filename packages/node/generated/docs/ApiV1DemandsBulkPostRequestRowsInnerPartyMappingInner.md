# ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_party_id** | **string** |  | [optional] [default to undefined]
**first_name** | **string** |  | [optional] [default to undefined]
**last_name** | **string** |  | [optional] [default to undefined]
**email** | **string** |  | [optional] [default to undefined]
**phone** | **string** | E.164 biçiminde telefon (ör. &#x60;+905551112233&#x60;). Ülke kodu ile birlikte gönderin; fazlalık şehirlerarası öneki (TRde baş sıfır, &#x60;+90 0542…&#x60;) sunucuda kaldırılır ve numara kanonik E.164 biçiminde saklanır. Tanınmayan girdi olduğu gibi korunur ama SMS teslim edilemeyebilir. | [optional] [default to undefined]
**government_id** | **string** | T.C. kimlik numarası (11 hane) | [optional] [default to undefined]
**birth_date** | **string** | ISO 8601 (ör. 1990-05-15) | [optional] [default to undefined]
**send_sms** | **boolean** |  | [optional] [default to true]
**send_email** | **boolean** |  | [optional] [default to true]
**locale** | [**SignerLocale**](SignerLocale.md) |  | [optional] [default to undefined]
**custom_message** | **string** | Bu tarafa özel davet mesajı | [optional] [default to undefined]
**variables** | **object** | Bu tarafa özel değişkenler | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner = {
    template_party_id,
    first_name,
    last_name,
    email,
    phone,
    government_id,
    birth_date,
    send_sms,
    send_email,
    locale,
    custom_message,
    variables,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
