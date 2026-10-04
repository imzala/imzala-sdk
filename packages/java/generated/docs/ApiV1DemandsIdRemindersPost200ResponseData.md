

# ApiV1DemandsIdRemindersPost200ResponseData


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**demandId** | **UUID** |  |  [optional] |
|**remindersIneligible** | **Integer** | İmzasını veya onayını tamamlamamış ama bu çağrıda hatırlatma almayan taraf sayısı (reddetmiş, açık düzeltme talebi olan ya da imza ekranı henüz açılmamış). Gönderim sayısı 0 iken sözleşmenin tamamlanıp tamamlanmadığını ayırt etmek için kullanılabilir.  |  [optional] |
|**dispatched** | [**List&lt;ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner&gt;**](ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner.md) |  |  [optional] |
|**skipped** | [**List&lt;ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner&gt;**](ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner.md) | Hatırlatma gönderilmeyen partilerin nedenleriyle birlikte (telefon/email yok, opt-out vs.) |  [optional] |
|**lastReminderSentAt** | **OffsetDateTime** |  |  [optional] |



