# ContractTerm

Sözleşme süre/yenileme takibi. Sözleşmede takip hiç tanımlanmadıysa (`start_mode` VE `end_date` boşsa) bu alan `null` döner.  Bu, hukuki bir yenileme veya sona erme beyanı değildir; platformun takip amaçlı kaydıdır. Sözleşmenin fiilen ne zaman sona erdiği veya yenilendiği, tarafların kendi sözleşme hükümlerine ve yürürlükteki hukuka tabidir. Bu alan yalnız hatırlatma ve görünürlük sağlar. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**start_mode** | **str** | Takibin ne zaman başladığı. &#x60;FIXED_DATE&#x60;: &#x60;start_date&#x60;&#39;ten. &#x60;ON_FIRST_SIGNATURE&#x60; / &#x60;ON_COMPLETION&#x60;: ilk imza / tüm imzalar tamamlandığı anda otomatik (o ana kadar &#x60;end_date&#x60; bilinmez).  | 
**start_date** | **date** |  | 
**duration_months** | **int** |  | 
**fixed_end_date** | **date** |  | 
**end_date** | **date** | Takip edilen bitiş tarihi. &#x60;renewal_type: AUTO_RENEW&#x60; sözleşmelerde her dönemde (günlük olarak) bir sonraki döneme ileri alınır; bkz. &#x60;contract.advanced&#x60; webhook olayı.  | 
**end_date_signed** | **date** | İmzalanan belge metninde bitiş tarihi bir değişken olarak yazılıysa, sözleşme tamamlandığı anda &#x60;end_date&#x60;&#39;in dondurulmuş kopyası. &#x60;end_date&#x60; sonradan ileri alınsa bile bu alan DEĞİŞMEZ. İmzalanan metinle takip değeri arasındaki tarihsel referans budur.  | 
**renewal_type** | **str** |  | 
**renewal_period_months** | **int** |  | 
**notice_days** | **int** | Fesih ihbarı için bitişten önce kaç gün gerektiği. | 
**notice_deadline** | **date** | Hesaplanır (&#x60;end_date - notice_days&#x60;); &#x60;end_date&#x60; veya &#x60;notice_days&#x60; boşsa &#x60;null&#x60;. | 
**reminder_offsets** | **List[int]** | Bitişten kaç gün önce hatırlatma gönderileceği (gün sayıları). | 
**notify_counterparty** | **bool** | Karşı tarafa da hatırlatma gönderilsin mi. | 
**state** | **str** | &#x60;UNTRACKED&#x60;: takip yok (&#x60;end_date&#x60; bilinmiyor). &#x60;ACTIVE&#x60;: takip sürüyor. &#x60;ENDED&#x60;: &#x60;FIXED_TERM&#x60; sözleşmenin bitiş tarihi geçti. &#x60;STOPPED&#x60;: yenileme takibi kullanıcı tarafından durduruldu.  | 
**days_left** | **int** | Bugünden &#x60;end_date&#x60;&#39;e gün sayısı (negatifse geçmişte). | 
**renewal_stopped_at** | **datetime** |  | 

## Example

```python
from imzala_client.models.contract_term import ContractTerm

# TODO update the JSON string below
json = "{}"
# create an instance of ContractTerm from a JSON string
contract_term_instance = ContractTerm.from_json(json)
# print the JSON string representation of the object
print(ContractTerm.to_json())

# convert the object into a dict
contract_term_dict = contract_term_instance.to_dict()
# create an instance of ContractTerm from a dict
contract_term_from_dict = ContractTerm.from_dict(contract_term_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


