# ContractNumber

Sözleşmeye panelde tanımlı bir numaratörden verilen sözleşme numarası. Numara sözleşme imzaya gönderilirken verilir ve sonradan değişmez.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**sequence_id** | **UUID** | Numarayı veren numaratörün kimliği. |
**value** | **str** | Biçimlendirilmiş sözleşme numarası (önek, sıra numarası ve sonek). |

## Example

```python
from imzala_client.models.contract_number import ContractNumber

# TODO update the JSON string below
json = "{}"
# create an instance of ContractNumber from a JSON string
contract_number_instance = ContractNumber.from_json(json)
# print the JSON string representation of the object
print(ContractNumber.to_json())

# convert the object into a dict
contract_number_dict = contract_number_instance.to_dict()
# create an instance of ContractNumber from a dict
contract_number_from_dict = ContractNumber.from_dict(contract_number_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
