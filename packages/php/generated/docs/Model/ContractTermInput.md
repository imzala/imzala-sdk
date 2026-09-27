# ContractTermInput

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**term_start_mode** | **string** |  | [optional]
**term_start_date** | **\DateTime** | Yalnız &#x60;term_start_mode: FIXED_DATE&#x60; iken kullanılır. | [optional]
**term_duration_months** | **int** |  | [optional]
**term_fixed_end_date** | **\DateTime** |  | [optional]
**renewal_type** | **string** |  | [optional]
**renewal_period_months** | **int** |  | [optional]
**notice_days** | **int** |  | [optional]
**reminder_offsets** | **int[]** | Boş dizi göndermek şablon/önceki değeri SİLMEZ (yok sayılır); temizlemek için &#x60;null&#x60; gönderin. | [optional]
**notify_counterparty** | **bool** |  | [optional] [default to false]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
