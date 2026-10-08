# DocumentCollectionPolicyError

Seçili Belge Toplama şablonunun politikası geçersiz; error kaynak makine kodudur.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  |
**code** | **str** |  |
**error** | **str** |  |

## Example

```python
from imzala_client.models.document_collection_policy_error import DocumentCollectionPolicyError

# TODO update the JSON string below
json = "{}"
# create an instance of DocumentCollectionPolicyError from a JSON string
document_collection_policy_error_instance = DocumentCollectionPolicyError.from_json(json)
# print the JSON string representation of the object
print(DocumentCollectionPolicyError.to_json())

# convert the object into a dict
document_collection_policy_error_dict = document_collection_policy_error_instance.to_dict()
# create an instance of DocumentCollectionPolicyError from a dict
document_collection_policy_error_from_dict = DocumentCollectionPolicyError.from_dict(document_collection_policy_error_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


