# ContractTerm

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**start_mode** | **string** | Takibin ne zaman başladığı. &#x60;FIXED_DATE&#x60;: &#x60;start_date&#x60;&#39;ten. &#x60;ON_FIRST_SIGNATURE&#x60; / &#x60;ON_COMPLETION&#x60;: ilk imza / tüm imzalar tamamlandığı anda otomatik (o ana kadar &#x60;end_date&#x60; bilinmez). |
**start_date** | **\DateTime** |  |
**duration_months** | **int** |  |
**fixed_end_date** | **\DateTime** |  |
**end_date** | **\DateTime** | Takip edilen bitiş tarihi. &#x60;renewal_type: AUTO_RENEW&#x60; sözleşmelerde her dönemde (günlük olarak) bir sonraki döneme ileri alınır; bkz. &#x60;contract.advanced&#x60; webhook olayı. |
**end_date_signed** | **\DateTime** | İmzalanan belge metninde bitiş tarihi bir değişken olarak yazılıysa, sözleşme tamamlandığı anda &#x60;end_date&#x60;&#39;in dondurulmuş kopyası. &#x60;end_date&#x60; sonradan ileri alınsa bile bu alan DEĞİŞMEZ. İmzalanan metinle takip değeri arasındaki tarihsel referans budur. |
**renewal_type** | **string** |  |
**renewal_period_months** | **int** |  |
**notice_days** | **int** | Fesih ihbarı için bitişten önce kaç gün gerektiği. |
**notice_deadline** | **\DateTime** | Hesaplanır (&#x60;end_date - notice_days&#x60;); &#x60;end_date&#x60; veya &#x60;notice_days&#x60; boşsa &#x60;null&#x60;. |
**reminder_offsets** | **int[]** | Bitişten kaç gün önce hatırlatma gönderileceği (gün sayıları). |
**notify_counterparty** | **bool** | Karşı tarafa da hatırlatma gönderilsin mi. |
**state** | **string** | &#x60;UNTRACKED&#x60;: takip yok (&#x60;end_date&#x60; bilinmiyor). &#x60;ACTIVE&#x60;: takip sürüyor. &#x60;ENDED&#x60;: &#x60;FIXED_TERM&#x60; sözleşmenin bitiş tarihi geçti. &#x60;STOPPED&#x60;: yenileme takibi kullanıcı tarafından durduruldu. |
**days_left** | **int** | Bugünden &#x60;end_date&#x60;&#39;e gün sayısı (negatifse geçmişte). |
**renewal_stopped_at** | **\DateTime** |  |

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
