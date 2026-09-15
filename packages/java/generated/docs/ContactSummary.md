

# ContactSummary

Kişi (Contact) public görünümü. İç eşleşme alanları (matched_user_id / match_via / matched_at) ve şifreleme sütunları (*_enc / *_hash) DÂHİL DEĞİLDİR. 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **UUID** |  |  [optional] |
|**firstName** | **String** |  |  [optional] |
|**lastName** | **String** |  |  [optional] |
|**email** | **String** |  |  [optional] |
|**phone** | **String** |  |  [optional] |
|**governmentId** | **String** |  |  [optional] |
|**governmentIdSerial** | **String** |  |  [optional] |
|**jobTitle** | **String** |  |  [optional] |
|**companyId** | **UUID** |  |  [optional] |
|**company** | [**ContactSummaryCompany**](ContactSummaryCompany.md) |  |  [optional] |
|**notes** | **String** |  |  [optional] |
|**addressCountry** | **String** |  |  [optional] |
|**addressCity** | **String** |  |  [optional] |
|**addressDistrict** | **String** |  |  [optional] |
|**addressLine** | **String** |  |  [optional] |
|**archivedAt** | **OffsetDateTime** |  |  [optional] |
|**createdAt** | **OffsetDateTime** |  |  [optional] |
|**updatedAt** | **OffsetDateTime** |  |  [optional] |



