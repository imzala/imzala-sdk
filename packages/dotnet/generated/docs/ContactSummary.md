# ImzalaApiClient.Model.ContactSummary
Kişi (Contact) public görünümü. İç eşleşme alanları (matched_user_id / match_via / matched_at) ve şifreleme sütunları (*_enc / *_hash) DÂHİL DEĞİLDİR. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Id** | **Guid** |  | [optional] 
**FirstName** | **string** |  | [optional] 
**LastName** | **string** |  | [optional] 
**Email** | **string** |  | [optional] 
**Phone** | **string** |  | [optional] 
**GovernmentId** | **string** |  | [optional] 
**GovernmentIdSerial** | **string** |  | [optional] 
**JobTitle** | **string** |  | [optional] 
**CompanyId** | **Guid?** |  | [optional] 
**Company** | [**ContactSummaryCompany**](ContactSummaryCompany.md) |  | [optional] 
**Notes** | **string** |  | [optional] 
**AddressCountry** | **string** |  | [optional] 
**AddressCity** | **string** |  | [optional] 
**AddressDistrict** | **string** |  | [optional] 
**AddressLine** | **string** |  | [optional] 
**ArchivedAt** | **DateTime?** |  | [optional] 
**CreatedAt** | **DateTime** |  | [optional] 
**UpdatedAt** | **DateTime** |  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

