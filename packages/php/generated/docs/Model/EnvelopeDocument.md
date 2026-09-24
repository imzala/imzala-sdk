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
**progress** | [**\Imzala\Client\Model\DocumentProgress**](DocumentProgress.md) | Yalnız &#x60;GET .../documents&#x60; liste yanıtında bulunur. | [optional]
**sealing** | [**\Imzala\Client\Model\DocumentSealing**](DocumentSealing.md) | Yalnız &#x60;GET .../documents&#x60; liste yanıtında bulunur. | [optional]
**decisions** | [**\Imzala\Client\Model\DocumentPartyDecision[]**](DocumentPartyDecision.md) | Yalnız &#x60;GET .../documents&#x60; liste yanıtında bulunur. Belge başına taraf kararları; &#x60;GET /api/v1/demands/{id}&#x60; yanıtındaki &#x60;documents[].decisions&#x60; ile aynıdır. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
