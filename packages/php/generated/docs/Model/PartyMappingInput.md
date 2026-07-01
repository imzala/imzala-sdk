# PartyMappingInput

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_party_id** | **string** | GET /api/v1/templates/{id} cevabındaki parties[].id |
**first_name** | **string** |  |
**last_name** | **string** |  |
**email** | **string** | email VEYA phone&#39;dan en az biri zorunlu | [optional]
**phone** | **string** | E.164 format (örn. \&quot;+905551234567\&quot;) | [optional]
**government_id** | **string** | TC kimlik no (11 hane) | [optional]
**birth_date** | **\DateTime** | ISO 8601 (örn. \&quot;1990-05-15\&quot;) | [optional]
**send_sms** | **bool** |  | [optional] [default to true]
**send_email** | **bool** |  | [optional] [default to true]
**variables** | [**array<string,\Imzala\Client\Model\PartyMappingInputVariablesValue>**](PartyMappingInputVariablesValue.md) | Bu PARTİYE AİT dynamic field&#39;lara gönderilen değerler. Slug bazında eşleşir. Item&#39;ın template_party_id&#39;si bu partiyle aynı olmalı; değilse değişken atlanır ve variables_ignored&#39;a düşürülür. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
