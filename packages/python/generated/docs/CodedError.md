# CodedError

`error` alanında insan-okur mesaj, `code` alanında makinece okunur hata kodu taşıyan hata gövdesi. Hangi kodların döndüğü ilgili yanıtın açıklamasında listelenir. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**error** | **str** |  | [optional] 
**code** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.coded_error import CodedError

# TODO update the JSON string below
json = "{}"
# create an instance of CodedError from a JSON string
coded_error_instance = CodedError.from_json(json)
# print the JSON string representation of the object
print(CodedError.to_json())

# convert the object into a dict
coded_error_dict = coded_error_instance.to_dict()
# create an instance of CodedError from a dict
coded_error_from_dict = CodedError.from_dict(coded_error_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


