# ImzalaApiClient.Model.ContractTerm
Sözleşme süre/yenileme takibi. Sözleşmede takip hiç tanımlanmadıysa (`start_mode` VE `end_date` boşsa) bu alan `null` döner.  Bu, hukuki bir yenileme veya sona erme beyanı değildir; platformun takip amaçlı kaydıdır. Sözleşmenin fiilen ne zaman sona erdiği veya yenilendiği, tarafların kendi sözleşme hükümlerine ve yürürlükteki hukuka tabidir. Bu alan yalnız hatırlatma ve görünürlük sağlar. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**StartMode** | **string** | Takibin ne zaman başladığı. &#x60;FIXED_DATE&#x60;: &#x60;start_date&#x60;&#39;ten. &#x60;ON_FIRST_SIGNATURE&#x60; / &#x60;ON_COMPLETION&#x60;: ilk imza / tüm imzalar tamamlandığı anda otomatik (o ana kadar &#x60;end_date&#x60; bilinmez).  | 
**StartDate** | **DateOnly?** |  | 
**DurationMonths** | **int?** |  | 
**FixedEndDate** | **DateOnly?** |  | 
**EndDate** | **DateOnly?** | Takip edilen bitiş tarihi. &#x60;renewal_type: AUTO_RENEW&#x60; sözleşmelerde her dönemde (günlük olarak) bir sonraki döneme ileri alınır; bkz. &#x60;contract.advanced&#x60; webhook olayı.  | 
**EndDateSigned** | **DateOnly?** | İmzalanan belge metninde bitiş tarihi bir değişken olarak yazılıysa, sözleşme tamamlandığı anda &#x60;end_date&#x60;&#39;in dondurulmuş kopyası. &#x60;end_date&#x60; sonradan ileri alınsa bile bu alan DEĞİŞMEZ. İmzalanan metinle takip değeri arasındaki tarihsel referans budur.  | 
**RenewalType** | **string** |  | 
**RenewalPeriodMonths** | **int?** |  | 
**NoticeDays** | **int?** | Fesih ihbarı için bitişten önce kaç gün gerektiği. | 
**NoticeDeadline** | **DateOnly?** | Hesaplanır (&#x60;end_date - notice_days&#x60;); &#x60;end_date&#x60; veya &#x60;notice_days&#x60; boşsa &#x60;null&#x60;. | 
**ReminderOffsets** | **List&lt;int&gt;** | Bitişten kaç gün önce hatırlatma gönderileceği (gün sayıları). | 
**NotifyCounterparty** | **bool** | Karşı tarafa da hatırlatma gönderilsin mi. | 
**State** | **string** | &#x60;UNTRACKED&#x60;: takip yok (&#x60;end_date&#x60; bilinmiyor). &#x60;ACTIVE&#x60;: takip sürüyor. &#x60;ENDED&#x60;: &#x60;FIXED_TERM&#x60; sözleşmenin bitiş tarihi geçti. &#x60;STOPPED&#x60;: yenileme takibi kullanıcı tarafından durduruldu.  | 
**DaysLeft** | **int?** | Bugünden &#x60;end_date&#x60;&#39;e gün sayısı (negatifse geçmişte). | 
**RenewalStoppedAt** | **DateTime?** |  | 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

