

# CreateDemandRequest


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**templateId** | **UUID** | GET /api/v1/templates listesinden veya dashboard&#39;dan kopyalayın |  |
|**title** | **String** | Sözleşme başlığı (yoksa template adı kullanılır) |  [optional] |
|**description** | **String** |  |  [optional] |
|**partyMapping** | [**List&lt;PartyMappingInput&gt;**](PartyMappingInput.md) |  |  |
|**variables** | [**Map&lt;String, PartyMappingInputVariablesValue&gt;**](PartyMappingInputVariablesValue.md) | **Root scope** — partilerden bağımsız field&#39;lara gönderilen değerler. Item&#39;ın template_party_id&#39;si NULL ise (partisiz) buradan dolar. Multi-party şablonda kira_baslangic_tarihi gibi paylaşılan field&#39;lar.  |  [optional] |
|**hasTimestamp** | **Boolean** | TÜBİTAK zaman damgası |  [optional] |
|**sendSmsNotifications** | **Boolean** |  |  [optional] |
|**sendEmailNotifications** | **Boolean** |  |  [optional] |
|**smsTitle** | **String** | SMS gönderici adı |  [optional] |
|**smsContent** | **String** | Custom SMS gövdesi. **Sadece** çağıran organizasyon **PRO veya ENTERPRISE planda** ise ve aktif &#x60;OrganizationSmsConfig&#x60; (sender_name dolu) varsa kabul edilir; aksi halde 403 &#x60;SMS_CUSTOMIZATION_NOT_ALLOWED&#x60; döner.  FREE/BASIC planda olan veya kendi SMS sağlayıcısı tanımlı olmayan müşterilerin marka itibarını korumak için sistem default sağlayıcısı (Codeck NetGSM) ile gönderim yapılır ve özel metin reddedilir. Kendi sağlayıcınızı tanımlamak için Dashboard → Organizasyon → SMS Ayarları sayfasını kullanın.  Boş string / null gönderirseniz \&quot;clear\&quot; olarak yorumlanır (gating&#39;den geçer).  |  [optional] |
|**emailContent** | **String** | Custom e-posta gövdesi |  [optional] |
|**expiryDate** | **OffsetDateTime** |  |  [optional] |
|**requireTcVerification** | **Boolean** |  |  [optional] |
|**requireBiometricVerification** | **Boolean** |  |  [optional] |
|**reminderSettings** | [**ReminderSettings**](ReminderSettings.md) | Bu sözleşme için hatırlatma ayarlarını **şablon default&#39;unu override** ederek belirtir. Yollanmazsa şablonun &#x60;reminder_*&#x60; alanları kullanılır (PUT /api/templates/:id ile dashboard&#39;dan kaydedilen değerler); şablonda da yoksa &#x60;{enabled:true, intervals_hours:[48], max_reminders:1, channels:[\&quot;email\&quot;]}&#x60; default&#39;u uygulanır. Demand oluşumunda &#x60;ReminderConfig&#x60; satırı yaratılır ve BullMQ kuyruğuna scheduled hatırlatmalar yazılır.  |  [optional] |



