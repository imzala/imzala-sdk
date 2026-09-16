# CreateDemandRequest

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_id** | **string** | GET /api/v1/templates listesinden veya dashboard&#39;dan kopyalayın |
**title** | **string** | Sözleşme başlığı (yoksa template adı kullanılır) | [optional]
**description** | **string** |  | [optional]
**idempotency_key** | **string** | &#x60;Idempotency-Key&#x60; başlığının gövde karşılığı; başlık ekleyemeyen istemciler için. İkisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;. | [optional]
**force** | **bool** | Kopya kapısını bilerek geç. Yalnız idempotency anahtarı GÖNDERİLMEYEN çağrılarda anlamlıdır: aynı şablondan aynı taraflara aynı başlık ve değişkenlerle 10 dakika içinde ikinci bir sözleşmeyi kasten göndermek için. | [optional]
**party_mapping** | [**\Imzala\Client\Model\PartyMappingInput[]**](PartyMappingInput.md) |  |
**documents** | [**\Imzala\Client\Model\DocumentSelectionInput**](DocumentSelectionInput.md) |  | [optional]
**variables** | [**array<string,\Imzala\Client\Model\PartyMappingInputVariablesValue>**](PartyMappingInputVariablesValue.md) | **Root scope** — partilerden bağımsız field&#39;lara gönderilen değerler. Item&#39;ın template_party_id&#39;si NULL ise (partisiz) buradan dolar. Multi-party şablonda kira_baslangic_tarihi gibi paylaşılan field&#39;lar. | [optional]
**has_timestamp** | **bool** | TÜBİTAK zaman damgası | [optional] [default to false]
**dispatch_notifications** | **bool** | &#x60;false&#x60; verilirse sözleşme yalnız oluşturulur, davet gönderilmez; daha sonra &#x60;POST /api/v1/demands/{demandId}/dispatch&#x60; ile gönderilir (çok-belgeli zarf akışında belgeler bu arada eklenir). Varsayılan açık: davetler oluşturma anında gider. | [optional] [default to true]
**send_sms_notifications** | **bool** |  | [optional] [default to true]
**send_email_notifications** | **bool** |  | [optional] [default to true]
**sms_title** | **string** | SMS gönderici adı | [optional] [default to 'CODECK']
**sms_content** | **string** | Custom SMS gövdesi. **Sadece** çağıran organizasyon **PRO veya ENTERPRISE planda** ise ve aktif &#x60;OrganizationSmsConfig&#x60; (sender_name dolu) varsa kabul edilir; aksi halde 403 &#x60;SMS_CUSTOMIZATION_NOT_ALLOWED&#x60; döner.  FREE/BASIC planda olan veya kendi SMS sağlayıcısı tanımlı olmayan müşterilerin marka itibarını korumak için sistem default sağlayıcısı (Codeck NetGSM) ile gönderim yapılır ve özel metin reddedilir. Kendi sağlayıcınızı tanımlamak için Dashboard → Organizasyon → SMS Ayarları sayfasını kullanın.  Boş string / null gönderirseniz \&quot;clear\&quot; olarak yorumlanır (gating&#39;den geçer). | [optional]
**email_content** | **string** | Custom e-posta gövdesi | [optional]
**expiry_date** | **\DateTime** |  | [optional]
**require_tc_verification** | **bool** |  | [optional] [default to false]
**require_biometric_verification** | **bool** |  | [optional] [default to false]
**reminder_settings** | [**\Imzala\Client\Model\ReminderSettings**](ReminderSettings.md) | Bu sözleşme için hatırlatma ayarlarını **şablon default&#39;unu override** ederek belirtir. Yollanmazsa şablonun &#x60;reminder_*&#x60; alanları kullanılır (PUT /api/templates/:id ile dashboard&#39;dan kaydedilen değerler); şablonda da yoksa &#x60;{enabled:true, intervals_hours:[48], max_reminders:1, channels:[\&quot;email\&quot;]}&#x60; default&#39;u uygulanır. Demand oluşumunda &#x60;ReminderConfig&#x60; satırı yaratılır ve BullMQ kuyruğuna scheduled hatırlatmalar yazılır. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
