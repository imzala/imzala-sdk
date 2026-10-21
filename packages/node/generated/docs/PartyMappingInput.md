# PartyMappingInput


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_party_id** | **string** | GET /api/v1/templates/{id} cevabındaki parties[].id | [default to undefined]
**first_name** | **string** |  | [default to undefined]
**last_name** | **string** |  | [default to undefined]
**email** | **string** | email VEYA phone\&#39;dan en az biri zorunlu | [optional] [default to undefined]
**phone** | **string** | E.164 format (örn. \&quot;+905551112233\&quot;) | [optional] [default to undefined]
**government_id** | **string** | TC kimlik no (11 hane) | [optional] [default to undefined]
**birth_date** | **string** | ISO 8601 (örn. \&quot;1990-05-15\&quot;) | [optional] [default to undefined]
**send_sms** | **boolean** |  | [optional] [default to true]
**send_email** | **boolean** |  | [optional] [default to true]
**locale** | [**SignerLocale**](SignerLocale.md) |  | [optional] [default to undefined]
**require_phone_verification** | **boolean** | Telefon doğrulamasını (SMS ile gelen kod) yalnız bu taraf için açar (&#x60;true&#x60;) ya da kapatır (&#x60;false&#x60;). Gönderilmezse veya &#x60;null&#x60; gönderilirse sözleşmenin telefon doğrulaması ayarı geçerlidir; davranış alan olmadan önceki ile aynıdır. Sözleşmede telefon doğrulaması açıksa imzalayan taraflar için false etkisizdir. Sözleşme gönderildikten sonra bu ayar değiştirilemez. Geçersiz değer &#x60;400 INVALID_PHONE_VERIFICATION&#x60; döner; &#x60;true&#x60; iken &#x60;phone&#x60; boşsa &#x60;400 PHONE_REQUIRED_FOR_VERIFICATION&#x60; döner. | [optional] [default to undefined]
**variables** | [**{ [key: string]: PartyMappingInputVariablesValue; }**](PartyMappingInputVariablesValue.md) | Bu PARTİYE AİT dynamic field\&#39;lara gönderilen değerler. Slug bazında eşleşir. Item\&#39;ın template_party_id\&#39;si bu partiyle aynı olmalı; değilse değişken atlanır ve variables_ignored\&#39;a düşürülür. Kaşe alanının slug\&#39;ına &#x60;StampData&#x60; nesnesi verilebilir (bkz. &#x60;CreateDemandRequest.variables&#x60;).  | [optional] [default to undefined]
**document_variables** | **{ [key: string]: { [key: string]: PartyMappingInputVariablesValue; }; }** | Bu partinin BELGE BAŞINA değerleri. Anahtar şablon belgesinin kimliğidir (&#x60;GET /api/v1/templates/{id}/usage&#x60; cevabındaki &#x60;documents[].template_document_id&#x60;); belge başlığı ya da sırası anahtar olarak kullanılamaz. Aynı slug için en yüksek önceliklidir (bkz. &#x60;CreateDemandRequest.document_variables&#x60;).  | [optional] [default to undefined]

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
    locale,
    require_phone_verification,
    variables,
    document_variables,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
