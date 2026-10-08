# DocumentVerificationDisabledError

Seçili Belge Toplama şablonunun doğrulama politikası kullanılamıyor.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  |
**code** | **str** |  |
**error** | **str** |  |

## Example

```python
from imzala_client.models.document_verification_disabled_error import DocumentVerificationDisabledError

# TODO update the JSON string below
json = "{}"
# create an instance of DocumentVerificationDisabledError from a JSON string
document_verification_disabled_error_instance = DocumentVerificationDisabledError.from_json(json)
# print the JSON string representation of the object
print(DocumentVerificationDisabledError.to_json())

# convert the object into a dict
document_verification_disabled_error_dict = document_verification_disabled_error_instance.to_dict()
# create an instance of DocumentVerificationDisabledError from a dict
document_verification_disabled_error_from_dict = DocumentVerificationDisabledError.from_dict(document_verification_disabled_error_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
