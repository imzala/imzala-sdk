# StandardError

`code`/`message` formatında standart hata gövdesi. Reminder trigger (`POST /api/v1/demands/{id}/reminders`) bu formatı kullanır. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional] 
**error** | [**StandardErrorError**](StandardErrorError.md) |  | [optional] 

## Example

```python
from imzala_client.models.standard_error import StandardError

# TODO update the JSON string below
json = "{}"
# create an instance of StandardError from a JSON string
standard_error_instance = StandardError.from_json(json)
# print the JSON string representation of the object
print(StandardError.to_json())

# convert the object into a dict
standard_error_dict = standard_error_instance.to_dict()
# create an instance of StandardError from a dict
standard_error_from_dict = StandardError.from_dict(standard_error_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


