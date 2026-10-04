# ApiV1DemandsIdRemindersPost200ResponseData


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **UUID** |  | [optional] 
**reminders_ineligible** | **int** | İmzasını veya onayını tamamlamamış ama bu çağrıda hatırlatma almayan taraf sayısı (reddetmiş, açık düzeltme talebi olan ya da imza ekranı henüz açılmamış). Gönderim sayısı 0 iken sözleşmenin tamamlanıp tamamlanmadığını ayırt etmek için kullanılabilir.  | [optional] 
**dispatched** | [**List[ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner]**](ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner.md) |  | [optional] 
**skipped** | [**List[ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner]**](ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner.md) | Hatırlatma gönderilmeyen partilerin nedenleriyle birlikte (telefon/email yok, opt-out vs.) | [optional] 
**last_reminder_sent_at** | **datetime** |  | [optional] 

## Example

```python
from imzala_client.models.api_v1_demands_id_reminders_post200_response_data import ApiV1DemandsIdRemindersPost200ResponseData

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsIdRemindersPost200ResponseData from a JSON string
api_v1_demands_id_reminders_post200_response_data_instance = ApiV1DemandsIdRemindersPost200ResponseData.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsIdRemindersPost200ResponseData.to_json())

# convert the object into a dict
api_v1_demands_id_reminders_post200_response_data_dict = api_v1_demands_id_reminders_post200_response_data_instance.to_dict()
# create an instance of ApiV1DemandsIdRemindersPost200ResponseData from a dict
api_v1_demands_id_reminders_post200_response_data_from_dict = ApiV1DemandsIdRemindersPost200ResponseData.from_dict(api_v1_demands_id_reminders_post200_response_data_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


