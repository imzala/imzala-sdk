# ContactSummary

Kişi (Contact) public görünümü. İç eşleşme alanları (matched_user_id / match_via / matched_at) ve şifreleme sütunları (*_enc / *_hash) DÂHİL DEĞİLDİR. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional] [default to undefined]
**first_name** | **string** |  | [optional] [default to undefined]
**last_name** | **string** |  | [optional] [default to undefined]
**email** | **string** |  | [optional] [default to undefined]
**phone** | **string** |  | [optional] [default to undefined]
**government_id** | **string** |  | [optional] [default to undefined]
**government_id_serial** | **string** |  | [optional] [default to undefined]
**job_title** | **string** |  | [optional] [default to undefined]
**company_id** | **string** |  | [optional] [default to undefined]
**company** | [**ContactSummaryCompany**](ContactSummaryCompany.md) |  | [optional] [default to undefined]
**notes** | **string** |  | [optional] [default to undefined]
**address_country** | **string** |  | [optional] [default to undefined]
**address_city** | **string** |  | [optional] [default to undefined]
**address_district** | **string** |  | [optional] [default to undefined]
**address_line** | **string** |  | [optional] [default to undefined]
**archived_at** | **string** |  | [optional] [default to undefined]
**created_at** | **string** |  | [optional] [default to undefined]
**updated_at** | **string** |  | [optional] [default to undefined]

## Example

```typescript
import { ContactSummary } from '@imzala/server-sdk-node';

const instance: ContactSummary = {
    id,
    first_name,
    last_name,
    email,
    phone,
    government_id,
    government_id_serial,
    job_title,
    company_id,
    company,
    notes,
    address_country,
    address_city,
    address_district,
    address_line,
    archived_at,
    created_at,
    updated_at,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
