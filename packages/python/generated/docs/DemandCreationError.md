# DemandCreationError

Sözleşme oluşturma hatalarının açık taşıma zarfı. Alanlar hata türüne göre bulunur; yeni makine kodları istemci tarafından reddedilmez. Somut Belge Toplama zarfları DocumentCollectionPolicyError ve DocumentVerificationDisabledError şemaları ile yanıt örneklerinde açıklanır.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**success** | **bool** |  | [optional]
**error** | **str** |  | [optional]
**code** | **str** |  | [optional]
**message** | **str** |  | [optional]
**var_field** | **str** |  | [optional]
**details** | **Dict[str, object]** |  | [optional]

## Example

```python
from imzala_client.models.demand_creation_error import DemandCreationError

# TODO update the JSON string below
json = "{}"
# create an instance of DemandCreationError from a JSON string
demand_creation_error_instance = DemandCreationError.from_json(json)
# print the JSON string representation of the object
print(DemandCreationError.to_json())

# convert the object into a dict
demand_creation_error_dict = demand_creation_error_instance.to_dict()
# create an instance of DemandCreationError from a dict
demand_creation_error_from_dict = DemandCreationError.from_dict(demand_creation_error_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
