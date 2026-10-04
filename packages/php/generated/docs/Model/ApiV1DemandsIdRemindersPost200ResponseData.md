# ApiV1DemandsIdRemindersPost200ResponseData

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **string** |  | [optional]
**reminders_ineligible** | **int** | İmzasını veya onayını tamamlamamış ama bu çağrıda hatırlatma almayan taraf sayısı (reddetmiş, açık düzeltme talebi olan ya da imza ekranı henüz açılmamış). Gönderim sayısı 0 iken sözleşmenin tamamlanıp tamamlanmadığını ayırt etmek için kullanılabilir. | [optional]
**dispatched** | [**\Imzala\Client\Model\ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner[]**](ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner.md) |  | [optional]
**skipped** | [**\Imzala\Client\Model\ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner[]**](ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner.md) | Hatırlatma gönderilmeyen partilerin nedenleriyle birlikte (telefon/email yok, opt-out vs.) | [optional]
**last_reminder_sent_at** | **\DateTime** |  | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
