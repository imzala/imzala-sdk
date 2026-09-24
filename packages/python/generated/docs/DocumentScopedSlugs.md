# DocumentScopedSlugs


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**common** | **List[str]** |  | [optional] 
**by_party** | **Dict[str, List[str]]** |  | [optional] 

## Example

```python
from imzala_client.models.document_scoped_slugs import DocumentScopedSlugs

# TODO update the JSON string below
json = "{}"
# create an instance of DocumentScopedSlugs from a JSON string
document_scoped_slugs_instance = DocumentScopedSlugs.from_json(json)
# print the JSON string representation of the object
print(DocumentScopedSlugs.to_json())

# convert the object into a dict
document_scoped_slugs_dict = document_scoped_slugs_instance.to_dict()
# create an instance of DocumentScopedSlugs from a dict
document_scoped_slugs_from_dict = DocumentScopedSlugs.from_dict(document_scoped_slugs_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


