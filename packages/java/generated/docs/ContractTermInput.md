

# ContractTermInput

Sözleşme süre/yenileme takibi girdisi. `POST /demands` üzerinde oluştururken ve `PATCH /demands/{id}/term` üzerinde güncellerken AYNI alan adları kullanılır. `PATCH`'te kısmi güncelleme uygulanır: yalnız gövdede GÖNDERİLEN anahtarlar değiştirilir; bir anahtarı `null` göndermek o alanı temizler, hiç göndermemek dokunmaz.  Doğrulama kuralları: - `term_fixed_end_date` ile `term_duration_months` birlikte gönderilemez. - `term_start_mode: FIXED_DATE` ise oluştururken `term_start_date` ya   da `term_fixed_end_date` gerekir; `ON_FIRST_SIGNATURE` /   `ON_COMPLETION` modlarında `term_start_date` gönderilse de yok   sayılır (başlangıç imza anında otomatik yazılır). - `renewal_type: AUTO_RENEW` ise `renewal_period_months` gerekir;   gönderilmezse `term_duration_months`'tan devralınır. - Geçersiz kombinasyon 400 `TERM_INVALID` + hangi alanı işaret eden   `field` ile döner. 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**termStartMode** | [**TermStartModeEnum**](#TermStartModeEnum) |  |  [optional] |
|**termStartDate** | **LocalDate** | Yalnız &#x60;term_start_mode: FIXED_DATE&#x60; iken kullanılır. |  [optional] |
|**termDurationMonths** | **Integer** |  |  [optional] |
|**termFixedEndDate** | **LocalDate** |  |  [optional] |
|**renewalType** | [**RenewalTypeEnum**](#RenewalTypeEnum) |  |  [optional] |
|**renewalPeriodMonths** | **Integer** |  |  [optional] |
|**noticeDays** | **Integer** |  |  [optional] |
|**reminderOffsets** | **List&lt;Integer&gt;** | Boş dizi göndermek şablon/önceki değeri SİLMEZ (yok sayılır); temizlemek için &#x60;null&#x60; gönderin.  |  [optional] |
|**notifyCounterparty** | **Boolean** |  |  [optional] |



## Enum: TermStartModeEnum

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



