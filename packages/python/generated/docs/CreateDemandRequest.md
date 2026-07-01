# CreateDemandRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_id** | **UUID** | GET /api/v1/templates listesinden veya dashboard&#39;dan kopyalayın | 
**title** | **str** | Sözleşme başlığı (yoksa template adı kullanılır) | [optional] 
**description** | **str** |  | [optional] 
**party_mapping** | [**List[PartyMappingInput]**](PartyMappingInput.md) |  | 
**variables** | [**Dict[str, PartyMappingInputVariablesValue]**](PartyMappingInputVariablesValue.md) | **Root scope** — partilerden bağımsız field&#39;lara gönderilen değerler. Item&#39;ın template_party_id&#39;si NULL ise (partisiz) buradan dolar. Multi-party şablonda kira_baslangic_tarihi gibi paylaşılan field&#39;lar.  | [optional] 
**has_timestamp** | **bool** | TÜBİTAK zaman damgası | [optional] [default to False]
**send_sms_notifications** | **bool** |  | [optional] [default to True]
**send_email_notifications** | **bool** |  | [optional] [default to True]
**sms_title** | **str** | SMS gönderici adı | [optional] [default to 'CODECK']
**sms_content** | **str** | Custom SMS gövdesi. **Sadece** çağıran organizasyon **PRO veya ENTERPRISE planda** ise ve aktif &#x60;OrganizationSmsConfig&#x60; (sender_name dolu) varsa kabul edilir; aksi halde 403 &#x60;SMS_CUSTOMIZATION_NOT_ALLOWED&#x60; döner.  FREE/BASIC planda olan veya kendi SMS sağlayıcısı tanımlı olmayan müşterilerin marka itibarını korumak için sistem default sağlayıcısı (Codeck NetGSM) ile gönderim yapılır ve özel metin reddedilir. Kendi sağlayıcınızı tanımlamak için Dashboard → Organizasyon → SMS Ayarları sayfasını kullanın.  Boş string / null gönderirseniz \&quot;clear\&quot; olarak yorumlanır (gating&#39;den geçer).  | [optional] 
**email_content** | **str** | Custom e-posta gövdesi | [optional] 
**expiry_date** | **datetime** |  | [optional] 
**require_tc_verification** | **bool** |  | [optional] [default to False]
**require_biometric_verification** | **bool** |  | [optional] [default to False]
**reminder_settings** | [**ReminderSettings**](ReminderSettings.md) | Bu sözleşme için hatırlatma ayarlarını **şablon default&#39;unu override** ederek belirtir. Yollanmazsa şablonun &#x60;reminder_*&#x60; alanları kullanılır (PUT /api/templates/:id ile dashboard&#39;dan kaydedilen değerler); şablonda da yoksa &#x60;{enabled:true, intervals_hours:[48], max_reminders:1, channels:[\&quot;email\&quot;]}&#x60; default&#39;u uygulanır. Demand oluşumunda &#x60;ReminderConfig&#x60; satırı yaratılır ve BullMQ kuyruğuna scheduled hatırlatmalar yazılır.  | [optional] 

## Example

```python
from imzala_client.models.create_demand_request import CreateDemandRequest

# TODO update the JSON string below
json = "{}"
# create an instance of CreateDemandRequest from a JSON string
create_demand_request_instance = CreateDemandRequest.from_json(json)
# print the JSON string representation of the object
print(CreateDemandRequest.to_json())

# convert the object into a dict
create_demand_request_dict = create_demand_request_instance.to_dict()
# create an instance of CreateDemandRequest from a dict
create_demand_request_from_dict = CreateDemandRequest.from_dict(create_demand_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


