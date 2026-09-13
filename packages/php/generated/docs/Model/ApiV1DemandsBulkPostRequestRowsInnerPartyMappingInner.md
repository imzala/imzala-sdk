# ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_party_id** | **string** |  | [optional]
**first_name** | **string** |  | [optional]
**last_name** | **string** |  | [optional]
**email** | **string** |  | [optional]
**phone** | **string** | E.164 biçiminde telefon (ör. &#x60;+905551234567&#x60;). Ülke kodu ile birlikte gönderin; fazlalık şehirlerarası öneki (TRde baş sıfır, &#x60;+90 0542…&#x60;) sunucuda kaldırılır ve numara kanonik E.164 biçiminde saklanır. Tanınmayan girdi olduğu gibi korunur ama SMS teslim edilemeyebilir. | [optional]
**government_id** | **string** | T.C. kimlik numarası (11 hane) | [optional]
**birth_date** | **\DateTime** | ISO 8601 (ör. 1990-05-15) | [optional]
**send_sms** | **bool** |  | [optional] [default to true]
**send_email** | **bool** |  | [optional] [default to true]
**custom_message** | **string** | Bu tarafa özel davet mesajı | [optional]
**variables** | **object** | Bu tarafa özel değişkenler | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
