# ImzalaApiClient.Model.DemandDocumentStatus
Zarftaki tek bir belgenin durumu.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Id** | **Guid** |  | [optional] 
**Order** | **int** | 1&#39;den başlar. | [optional] 
**Title** | **string** |  | [optional] 
**DocKind** | **string** |  | [optional] 
**IsRequired** | **bool** |  | [optional] 
**SignatureRequired** | **bool** |  | [optional] 
**AssignedPartyIds** | **List&lt;Guid&gt;** |  | [optional] 
**Progress** | [**DocumentProgress**](DocumentProgress.md) |  | [optional] 
**Sealing** | [**DocumentSealing**](DocumentSealing.md) |  | [optional] 
**Decisions** | [**List&lt;DocumentPartyDecision&gt;**](DocumentPartyDecision.md) |  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

