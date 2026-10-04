# ImzalaApiClient.Model.ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**TemplatePartyId** | **Guid** |  | [optional] 
**FirstName** | **string** |  | [optional] 
**LastName** | **string** |  | [optional] 
**Email** | **string** |  | [optional] 
**Phone** | **string** | E.164 biçiminde telefon (ör. &#x60;+905551112233&#x60;). Ülke kodu ile birlikte gönderin; fazlalık şehirlerarası öneki (TRde baş sıfır, &#x60;+90 0542…&#x60;) sunucuda kaldırılır ve numara kanonik E.164 biçiminde saklanır. Tanınmayan girdi olduğu gibi korunur ama SMS teslim edilemeyebilir. | [optional] 
**GovernmentId** | **string** | T.C. kimlik numarası (11 hane) | [optional] 
**BirthDate** | **DateOnly** | ISO 8601 (ör. 1990-05-15) | [optional] 
**SendSms** | **bool** |  | [optional] [default to true]
**SendEmail** | **bool** |  | [optional] [default to true]
**Locale** | **SignerLocale** |  | [optional] 
**CustomMessage** | **string** | Bu tarafa özel davet mesajı | [optional] 
**Variables** | **Object** | Bu tarafa özel değişkenler | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

