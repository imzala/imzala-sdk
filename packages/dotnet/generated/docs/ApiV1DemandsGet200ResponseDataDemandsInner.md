# ImzalaApiClient.Model.ApiV1DemandsGet200ResponseDataDemandsInner

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Id** | **Guid** |  | [optional] 
**Title** | **string** |  | [optional] 
**Status** | **string** |  | [optional] 
**CreatedAt** | **DateTime** |  | [optional] 
**CompletedAt** | **DateTime?** |  | [optional] 
**TermEndDate** | **DateOnly?** | Takip edilen bitiş tarihi; takip yoksa &#x60;null&#x60; (bkz. &#x60;GET /demands/{id}&#x60; → &#x60;term&#x60;). | [optional] 
**ArchivedAt** | **DateTime?** |  | [optional] 
**PartiesTotal** | **int** |  | [optional] 
**PartiesSigned** | **int** |  | [optional] 
**PdfUrl** | **string** | COMPLETED ise imzalı PDF public URL&#39;i | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

