# DocumentSelectionErrorDetails


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**reason** | **str** | &#x60;shape&#x60;: &#x60;documents&#x60; nesne değil, bilinmeyen alan, kimlikler metin değil, listede 20&#39;den fazla kimlik ya da toplu uçta &#x60;options.documents&#x60;. &#x60;unknown_document&#x60;: kimlik bu şablonun belgesi değil. &#x60;conflict&#x60;: aynı kimlik hem &#x60;include&#x60; hem &#x60;exclude&#x60; listesinde. &#x60;empty&#x60;: seçim sonucunda ya da &#x60;documents&#x60; göndermediyseniz şablonun varsayılan belge kümesinde gönderilecek belge kalmadı.  | [optional] 
**document_ids** | **List[str]** |  | [optional] 

## Example

```python
from imzala_client.models.document_selection_error_details import DocumentSelectionErrorDetails

# TODO update the JSON string below
json = "{}"
# create an instance of DocumentSelectionErrorDetails from a JSON string
document_selection_error_details_instance = DocumentSelectionErrorDetails.from_json(json)
# print the JSON string representation of the object
print(DocumentSelectionErrorDetails.to_json())

# convert the object into a dict
document_selection_error_details_dict = document_selection_error_details_instance.to_dict()
# create an instance of DocumentSelectionErrorDetails from a dict
document_selection_error_details_from_dict = DocumentSelectionErrorDetails.from_dict(document_selection_error_details_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


