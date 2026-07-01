# CreateDemandRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_id** | **string** | GET /api/v1/templates listesinden veya dashboard\&#39;dan kopyalayın | [default to undefined]
**title** | **string** | Sözleşme başlığı (yoksa template adı kullanılır) | [optional] [default to undefined]
**description** | **string** |  | [optional] [default to undefined]
**party_mapping** | [**Array&lt;PartyMappingInput&gt;**](PartyMappingInput.md) |  | [default to undefined]
**variables** | [**{ [key: string]: PartyMappingInputVariablesValue; }**](PartyMappingInputVariablesValue.md) | **Root scope** — partilerden bağımsız field\&#39;lara gönderilen değerler. Item\&#39;ın template_party_id\&#39;si NULL ise (partisiz) buradan dolar. Multi-party şablonda kira_baslangic_tarihi gibi paylaşılan field\&#39;lar.  | [optional] [default to undefined]
**has_timestamp** | **boolean** | TÜBİTAK zaman damgası | [optional] [default to false]
**send_sms_notifications** | **boolean** |  | [optional] [default to true]
**send_email_notifications** | **boolean** |  | [optional] [default to true]
**sms_title** | **string** | SMS gönderici adı | [optional] [default to 'CODECK']
**sms_content** | **string** | Custom SMS gövdesi. **Sadece** çağıran organizasyon **PRO veya ENTERPRISE planda** ise ve aktif &#x60;OrganizationSmsConfig&#x60; (sender_name dolu) varsa kabul edilir; aksi halde 403 &#x60;SMS_CUSTOMIZATION_NOT_ALLOWED&#x60; döner.  FREE/BASIC planda olan veya kendi SMS sağlayıcısı tanımlı olmayan müşterilerin marka itibarını korumak için sistem default sağlayıcısı (Codeck NetGSM) ile gönderim yapılır ve özel metin reddedilir. Kendi sağlayıcınızı tanımlamak için Dashboard → Organizasyon → SMS Ayarları sayfasını kullanın.  Boş string / null gönderirseniz \&quot;clear\&quot; olarak yorumlanır (gating\&#39;den geçer).  | [optional] [default to undefined]
**email_content** | **string** | Custom e-posta gövdesi | [optional] [default to undefined]
**expiry_date** | **string** |  | [optional] [default to undefined]
**require_tc_verification** | **boolean** |  | [optional] [default to false]
**require_biometric_verification** | **boolean** |  | [optional] [default to false]
**reminder_settings** | [**ReminderSettings**](ReminderSettings.md) | Bu sözleşme için hatırlatma ayarlarını **şablon default\&#39;unu override** ederek belirtir. Yollanmazsa şablonun &#x60;reminder_*&#x60; alanları kullanılır (PUT /api/templates/:id ile dashboard\&#39;dan kaydedilen değerler); şablonda da yoksa &#x60;{enabled:true, intervals_hours:[48], max_reminders:1, channels:[\&quot;email\&quot;]}&#x60; default\&#39;u uygulanır. Demand oluşumunda &#x60;ReminderConfig&#x60; satırı yaratılır ve BullMQ kuyruğuna scheduled hatırlatmalar yazılır.  | [optional] [default to undefined]

## Example

```typescript
import { CreateDemandRequest } from '@imzala/server-sdk-node';

const instance: CreateDemandRequest = {
    template_id,
    title,
    description,
    party_mapping,
    variables,
    has_timestamp,
    send_sms_notifications,
    send_email_notifications,
    sms_title,
    sms_content,
    email_content,
    expiry_date,
    require_tc_verification,
    require_biometric_verification,
    reminder_settings,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
