# EnvelopeDocument

Çok-belgeli imza zarfındaki tek bir belge. `assigned_party_ids` ve `decision_count` yalnız `GET .../documents?view=wizard` yanıtında bulunur; `has_timestamp` yalnız varsayılan (özet) görünümde bulunur — ikisi AYNI ANDA gelmez (dashboard'un iki ayrı iç DTO'sunun [`EnvelopeDocumentDTO` / `EnvelopeDocumentSummaryDTO`] tek şema altında birleştirilmiş hâli). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**order** | **int** | 1&#39;den başlar. | [optional] 
**title** | **str** |  | [optional] 
**doc_kind** | **str** |  | [optional] 
**is_required** | **bool** |  | [optional] 
**signature_required** | **bool** |  | [optional] 
**assigned_party_ids** | **List[UUID]** | Yalnız &#x60;?view&#x3D;wizard&#x60; yanıtında bulunur. | [optional] 
**decision_count** | **int** | Yalnız &#x60;?view&#x3D;wizard&#x60; yanıtında bulunur. Bu belge için verilmiş onay/red kararı sayısı.  | [optional] 
**sealing_status** | **str** |  | [optional] 
**has_timestamp** | **bool** | Yalnız varsayılan (özet) görünümde bulunur. &#x60;sealing_status&#x60; &#x60;SEALED&#x60; değilken daima &#x60;false&#x60; döner.  | [optional] 
**page_count** | **int** |  | [optional] 
**completed_at** | **datetime** |  | [optional] 
**progress** | [**DocumentProgress**](DocumentProgress.md) | Yalnız &#x60;GET .../documents&#x60; liste yanıtında bulunur. | [optional] 
**sealing** | [**DocumentSealing**](DocumentSealing.md) | Yalnız &#x60;GET .../documents&#x60; liste yanıtında bulunur. | [optional] 
**decisions** | [**List[DocumentPartyDecision]**](DocumentPartyDecision.md) | Yalnız &#x60;GET .../documents&#x60; liste yanıtında bulunur. Belge başına taraf kararları; &#x60;GET /api/v1/demands/{id}&#x60; yanıtındaki &#x60;documents[].decisions&#x60; ile aynıdır.  | [optional] 

## Example

```python
from imzala_client.models.envelope_document import EnvelopeDocument

# TODO update the JSON string below
json = "{}"
# create an instance of EnvelopeDocument from a JSON string
envelope_document_instance = EnvelopeDocument.from_json(json)
# print the JSON string representation of the object
print(EnvelopeDocument.to_json())

# convert the object into a dict
envelope_document_dict = envelope_document_instance.to_dict()
# create an instance of EnvelopeDocument from a dict
envelope_document_from_dict = EnvelopeDocument.from_dict(envelope_document_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


