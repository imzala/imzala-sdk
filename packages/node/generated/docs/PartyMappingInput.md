# PartyMappingInput


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_party_id** | **string** | GET /api/v1/templates/{id} cevabındaki parties[].id | [default to undefined]
**first_name** | **string** |  | [default to undefined]
**last_name** | **string** |  | [default to undefined]
**email** | **string** | email VEYA phone\&#39;dan en az biri zorunlu | [optional] [default to undefined]
**phone** | **string** | E.164 format (örn. \&quot;+905551234567\&quot;) | [optional] [default to undefined]
**government_id** | **string** | TC kimlik no (11 hane) | [optional] [default to undefined]
**birth_date** | **string** | ISO 8601 (örn. \&quot;1990-05-15\&quot;) | [optional] [default to undefined]
**send_sms** | **boolean** |  | [optional] [default to true]
**send_email** | **boolean** |  | [optional] [default to true]
**variables** | [**{ [key: string]: PartyMappingInputVariablesValue; }**](PartyMappingInputVariablesValue.md) | Bu PARTİYE AİT dynamic field\&#39;lara gönderilen değerler. Slug bazında eşleşir. Item\&#39;ın template_party_id\&#39;si bu partiyle aynı olmalı; değilse değişken atlanır ve variables_ignored\&#39;a düşürülür.  | [optional] [default to undefined]

## Example

```typescript
import { PartyMappingInput } from '@imzala/server-sdk-node';

const instance: PartyMappingInput = {
    template_party_id,
    first_name,
    last_name,
    email,
    phone,
    government_id,
    birth_date,
    send_sms,
    send_email,
    variables,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
