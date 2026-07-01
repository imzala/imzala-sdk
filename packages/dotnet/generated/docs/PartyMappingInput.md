# ImzalaApiClient.Model.PartyMappingInput

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**TemplatePartyId** | **Guid** | GET /api/v1/templates/{id} cevabındaki parties[].id | 
**FirstName** | **string** |  | 
**LastName** | **string** |  | 
**Email** | **string** | email VEYA phone&#39;dan en az biri zorunlu | [optional] 
**Phone** | **string** | E.164 format (örn. \&quot;+905551234567\&quot;) | [optional] 
**GovernmentId** | **string** | TC kimlik no (11 hane) | [optional] 
**BirthDate** | **DateOnly** | ISO 8601 (örn. \&quot;1990-05-15\&quot;) | [optional] 
**SendSms** | **bool** |  | [optional] [default to true]
**SendEmail** | **bool** |  | [optional] [default to true]
**Variables** | [**Dictionary&lt;string, PartyMappingInputVariablesValue&gt;**](PartyMappingInputVariablesValue.md) | Bu PARTİYE AİT dynamic field&#39;lara gönderilen değerler. Slug bazında eşleşir. Item&#39;ın template_party_id&#39;si bu partiyle aynı olmalı; değilse değişken atlanır ve variables_ignored&#39;a düşürülür.  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

