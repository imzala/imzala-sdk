

# ContractTerm

Sözleşme süre/yenileme takibi. Sözleşmede takip hiç tanımlanmadıysa (`start_mode` VE `end_date` boşsa) bu alan `null` döner.  Bu, hukuki bir yenileme veya sona erme beyanı değildir; platformun takip amaçlı kaydıdır. Sözleşmenin fiilen ne zaman sona erdiği veya yenilendiği, tarafların kendi sözleşme hükümlerine ve yürürlükteki hukuka tabidir. Bu alan yalnız hatırlatma ve görünürlük sağlar. 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**startMode** | [**StartModeEnum**](#StartModeEnum) | Takibin ne zaman başladığı. &#x60;FIXED_DATE&#x60;: &#x60;start_date&#x60;&#39;ten. &#x60;ON_FIRST_SIGNATURE&#x60; / &#x60;ON_COMPLETION&#x60;: ilk imza / tüm imzalar tamamlandığı anda otomatik (o ana kadar &#x60;end_date&#x60; bilinmez).  |  |
|**startDate** | **LocalDate** |  |  |
|**durationMonths** | **Integer** |  |  |
|**fixedEndDate** | **LocalDate** |  |  |
|**endDate** | **LocalDate** | Takip edilen bitiş tarihi. &#x60;renewal_type: AUTO_RENEW&#x60; sözleşmelerde her dönemde (günlük olarak) bir sonraki döneme ileri alınır; bkz. &#x60;contract.advanced&#x60; webhook olayı.  |  |
|**endDateSigned** | **LocalDate** | İmzalanan belge metninde bitiş tarihi bir değişken olarak yazılıysa, sözleşme tamamlandığı anda &#x60;end_date&#x60;&#39;in dondurulmuş kopyası. &#x60;end_date&#x60; sonradan ileri alınsa bile bu alan DEĞİŞMEZ. İmzalanan metinle takip değeri arasındaki tarihsel referans budur.  |  |
|**renewalType** | [**RenewalTypeEnum**](#RenewalTypeEnum) |  |  |
|**renewalPeriodMonths** | **Integer** |  |  |
|**noticeDays** | **Integer** | Fesih ihbarı için bitişten önce kaç gün gerektiği. |  |
|**noticeDeadline** | **LocalDate** | Hesaplanır (&#x60;end_date - notice_days&#x60;); &#x60;end_date&#x60; veya &#x60;notice_days&#x60; boşsa &#x60;null&#x60;. |  |
|**reminderOffsets** | **List&lt;Integer&gt;** | Bitişten kaç gün önce hatırlatma gönderileceği (gün sayıları). |  |
|**notifyCounterparty** | **Boolean** | Karşı tarafa da hatırlatma gönderilsin mi. |  |
|**state** | [**StateEnum**](#StateEnum) | &#x60;UNTRACKED&#x60;: takip yok (&#x60;end_date&#x60; bilinmiyor). &#x60;ACTIVE&#x60;: takip sürüyor. &#x60;ENDED&#x60;: &#x60;FIXED_TERM&#x60; sözleşmenin bitiş tarihi geçti. &#x60;STOPPED&#x60;: yenileme takibi kullanıcı tarafından durduruldu.  |  |
|**daysLeft** | **Integer** | Bugünden &#x60;end_date&#x60;&#39;e gün sayısı (negatifse geçmişte). |  |
|**renewalStoppedAt** | **OffsetDateTime** |  |  |



## Enum: StartModeEnum

| Name | Value |
|---- | -----|
| FIXED_DATE | &quot;FIXED_DATE&quot; |
| ON_FIRST_SIGNATURE | &quot;ON_FIRST_SIGNATURE&quot; |
| ON_COMPLETION | &quot;ON_COMPLETION&quot; |



## Enum: RenewalTypeEnum

| Name | Value |
|---- | -----|
| AUTO_RENEW | &quot;AUTO_RENEW&quot; |
| FIXED_TERM | &quot;FIXED_TERM&quot; |



## Enum: StateEnum

| Name | Value |
|---- | -----|
| UNTRACKED | &quot;UNTRACKED&quot; |
| ACTIVE | &quot;ACTIVE&quot; |
| ENDED | &quot;ENDED&quot; |
| STOPPED | &quot;STOPPED&quot; |



