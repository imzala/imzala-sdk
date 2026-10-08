# ImzalaApiClient.Model.DemandCreationError
Sözleşme oluşturma hatalarının açık taşıma zarfı. Alanlar hata türüne göre bulunur; yeni makine kodları istemci tarafından reddedilmez. Somut Belge Toplama zarfları DocumentCollectionPolicyError ve DocumentVerificationDisabledError şemaları ile yanıt örneklerinde açıklanır.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Success** | **bool** |  | [optional]
**Error** | **string** |  | [optional]
**Code** | **string** |  | [optional]
**Message** | **string** |  | [optional]
**Field** | **string** |  | [optional]
**Details** | **Dictionary&lt;string, Object&gt;** |  | [optional]

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
