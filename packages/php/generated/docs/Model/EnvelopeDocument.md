# EnvelopeDocument

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional]
**order** | **int** | 1&#39;den başlar. | [optional]
**title** | **string** |  | [optional]
**doc_kind** | **string** |  | [optional]
**is_required** | **bool** |  | [optional]
**signature_required** | **bool** |  | [optional]
**assigned_party_ids** | **string[]** | Yalnız &#x60;?view&#x3D;wizard&#x60; yanıtında bulunur. | [optional]
**decision_count** | **int** | Yalnız &#x60;?view&#x3D;wizard&#x60; yanıtında bulunur. Bu belge için verilmiş onay/red kararı sayısı. | [optional]
**sealing_status** | **string** |  | [optional]
**has_timestamp** | **bool** | Yalnız varsayılan (özet) görünümde bulunur. &#x60;sealing_status&#x60; &#x60;SEALED&#x60; değilken daima &#x60;false&#x60; döner. | [optional]
**page_count** | **int** |  | [optional]
**completed_at** | **\DateTime** |  | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
