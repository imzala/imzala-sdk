# DocumentSelectionError

`400 INVALID_DOCUMENT_SELECTION` gövdesi. `details.reason` hatanın sınıfını, `details.document_ids` (varsa) yalnız isteğinizde gönderdiğiniz ve soruna yol açan kimlikleri taşır. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**error** | **str** |  | [optional] 
**code** | **str** |  | [optional] 
**details** | [**DocumentSelectionErrorDetails**](DocumentSelectionErrorDetails.md) |  | [optional] 

## Example

```python
from imzala_client.models.document_selection_error import DocumentSelectionError

# TODO update the JSON string below
json = "{}"
# create an instance of DocumentSelectionError from a JSON string
document_selection_error_instance = DocumentSelectionError.from_json(json)
# print the JSON string representation of the object
print(DocumentSelectionError.to_json())

# convert the object into a dict
document_selection_error_dict = document_selection_error_instance.to_dict()
# create an instance of DocumentSelectionError from a dict
document_selection_error_from_dict = DocumentSelectionError.from_dict(document_selection_error_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


