# DocumentSealing

Belgenin mühür durumu. Karar ilerlemesinden ayrı bir olgudur.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**status** | **str** |  | [optional] 
**has_timestamp** | **bool** | Yalnız &#x60;status&#x60; &#x60;SEALED&#x60; iken &#x60;true&#x60; olabilir. | [optional] 

## Example

```python
from imzala_client.models.document_sealing import DocumentSealing

# TODO update the JSON string below
json = "{}"
# create an instance of DocumentSealing from a JSON string
document_sealing_instance = DocumentSealing.from_json(json)
# print the JSON string representation of the object
print(DocumentSealing.to_json())

# convert the object into a dict
document_sealing_dict = document_sealing_instance.to_dict()
# create an instance of DocumentSealing from a dict
document_sealing_from_dict = DocumentSealing.from_dict(document_sealing_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


