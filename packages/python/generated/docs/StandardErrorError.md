# StandardErrorError


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**code** | **str** |  | 
**message** | **str** |  | 
**retry_after_seconds** | **int** | Sadece RATE_LIMITED&#39;de doludur | [optional] 

## Example

```python
from imzala_client.models.standard_error_error import StandardErrorError

# TODO update the JSON string below
json = "{}"
# create an instance of StandardErrorError from a JSON string
standard_error_error_instance = StandardErrorError.from_json(json)
# print the JSON string representation of the object
print(StandardErrorError.to_json())

# convert the object into a dict
standard_error_error_dict = standard_error_error_instance.to_dict()
# create an instance of StandardErrorError from a dict
standard_error_error_from_dict = StandardErrorError.from_dict(standard_error_error_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


