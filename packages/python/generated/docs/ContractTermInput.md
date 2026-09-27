# ContractTermInput

Sözleşme süre/yenileme takibi girdisi. `POST /demands` üzerinde oluştururken ve `PATCH /demands/{id}/term` üzerinde güncellerken AYNI alan adları kullanılır. `PATCH`'te kısmi güncelleme uygulanır: yalnız gövdede GÖNDERİLEN anahtarlar değiştirilir; bir anahtarı `null` göndermek o alanı temizler, hiç göndermemek dokunmaz.  Doğrulama kuralları: - `term_fixed_end_date` ile `term_duration_months` birlikte gönderilemez. - `term_start_mode: FIXED_DATE` ise oluştururken `term_start_date` ya   da `term_fixed_end_date` gerekir; `ON_FIRST_SIGNATURE` /   `ON_COMPLETION` modlarında `term_start_date` gönderilse de yok   sayılır (başlangıç imza anında otomatik yazılır). - `renewal_type: AUTO_RENEW` ise `renewal_period_months` gerekir;   gönderilmezse `term_duration_months`'tan devralınır. - Geçersiz kombinasyon 400 `TERM_INVALID` + hangi alanı işaret eden   `field` ile döner. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**term_start_mode** | **str** |  | [optional] 
**term_start_date** | **date** | Yalnız &#x60;term_start_mode: FIXED_DATE&#x60; iken kullanılır. | [optional] 
**term_duration_months** | **int** |  | [optional] 
**term_fixed_end_date** | **date** |  | [optional] 
**renewal_type** | **str** |  | [optional] 
**renewal_period_months** | **int** |  | [optional] 
**notice_days** | **int** |  | [optional] 
**reminder_offsets** | **List[int]** | Boş dizi göndermek şablon/önceki değeri SİLMEZ (yok sayılır); temizlemek için &#x60;null&#x60; gönderin.  | [optional] 
**notify_counterparty** | **bool** |  | [optional] 

## Example

```python
from imzala_client.models.contract_term_input import ContractTermInput

# TODO update the JSON string below
json = "{}"
# create an instance of ContractTermInput from a JSON string
contract_term_input_instance = ContractTermInput.from_json(json)
# print the JSON string representation of the object
print(ContractTermInput.to_json())

# convert the object into a dict
contract_term_input_dict = contract_term_input_instance.to_dict()
# create an instance of ContractTermInput from a dict
contract_term_input_from_dict = ContractTermInput.from_dict(contract_term_input_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


