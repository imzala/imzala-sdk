# ApiV1DemandsIdRemindersPost200ResponseData


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**demand_id** | **string** |  | [optional] [default to undefined]
**reminders_ineligible** | **number** | İmzasını veya onayını tamamlamamış ama bu çağrıda hatırlatma almayan taraf sayısı (reddetmiş, açık düzeltme talebi olan ya da imza ekranı henüz açılmamış). Gönderim sayısı 0 iken sözleşmenin tamamlanıp tamamlanmadığını ayırt etmek için kullanılabilir.  | [optional] [default to undefined]
**dispatched** | [**Array&lt;ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner&gt;**](ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner.md) |  | [optional] [default to undefined]
**skipped** | [**Array&lt;ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner&gt;**](ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner.md) | Hatırlatma gönderilmeyen partilerin nedenleriyle birlikte (telefon/email yok, opt-out vs.) | [optional] [default to undefined]
**last_reminder_sent_at** | **string** |  | [optional] [default to undefined]

## Example

```typescript
import { ApiV1DemandsIdRemindersPost200ResponseData } from '@imzala/server-sdk-node';

const instance: ApiV1DemandsIdRemindersPost200ResponseData = {
    demand_id,
    reminders_ineligible,
    dispatched,
    skipped,
    last_reminder_sent_at,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
