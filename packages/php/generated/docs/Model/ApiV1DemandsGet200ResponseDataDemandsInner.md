# ApiV1DemandsGet200ResponseDataDemandsInner

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional]
**title** | **string** |  | [optional]
**status** | **string** |  | [optional]
**created_at** | **\DateTime** |  | [optional]
**completed_at** | **\DateTime** |  | [optional]
**term_end_date** | **\DateTime** | Takip edilen bitiş tarihi; takip yoksa &#x60;null&#x60; (bkz. &#x60;GET /demands/{id}&#x60; → &#x60;term&#x60;). | [optional]
**archived_at** | **\DateTime** |  | [optional]
**parties_total** | **int** |  | [optional]
**parties_signed** | **int** |  | [optional]
**pdf_url** | **string** | COMPLETED ise imzalı PDF public URL&#39;i | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
