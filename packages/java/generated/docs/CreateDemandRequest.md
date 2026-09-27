

# CreateDemandRequest


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**templateId** | **UUID** | GET /api/v1/templates listesinden veya dashboard&#39;dan kopyalayın |  |
|**title** | **String** | Sözleşme başlığı (yoksa template adı kullanılır). Başlık, imza davet ve hatırlatma SMS&#39;lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın.  |  [optional] |
|**description** | **String** |  |  [optional] |
|**idempotencyKey** | **String** | &#x60;Idempotency-Key&#x60; başlığının gövde karşılığı; başlık ekleyemeyen istemciler için. İkisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  |  [optional] |
|**force** | **Boolean** | Kopya kapısını bilerek geç. Yalnız idempotency anahtarı GÖNDERİLMEYEN çağrılarda anlamlıdır: aynı şablondan aynı taraflara aynı başlık ve değişkenlerle 10 dakika içinde ikinci bir sözleşmeyi kasten göndermek için.  |  [optional] |
|**partyMapping** | [**List&lt;PartyMappingInput&gt;**](PartyMappingInput.md) |  |  |
|**documents** | [**DocumentSelectionInput**](DocumentSelectionInput.md) |  |  [optional] |
|**variables** | [**Map&lt;String, PartyMappingInputVariablesValue&gt;**](PartyMappingInputVariablesValue.md) | **Root scope** — partilerden bağımsız field&#39;lara gönderilen değerler. Item&#39;ın template_party_id&#39;si NULL ise (partisiz) buradan dolar. Multi-party şablonda kira_baslangic_tarihi gibi paylaşılan field&#39;lar.  **Kaşe alanları:** Değer bir &#x60;StampData&#x60; NESNESİ ise ve slug bir kaşe alanına aitse, sözleşme oluşturulurken o kaşe &#x60;PATCH /api/v1/demands/{id}/items/{itemId}/stamp&#x60; ile aynı kurallarla doldurulur (gönderenin kaşesi yapılandırılmış veriyle; imzalayanın dolduracağı kaşeye düzenlenebilir ön değer). Aynı slug birden fazla belgede geçiyorsa hepsine yazılır; öncelik kuralları &#x60;variables&#x60; ile aynıdır (bkz. &#x60;document_variables&#x60;). Nesne değer yalnız kaşe slug&#39;ına verilebilir: kaşe olmayan ya da gönderilecek belgelerde bulunmayan slug 400 &#x60;INVALID_VARIABLES&#x60; (&#x60;details.reason: object_value_for_non_stamp&#x60;), geçersiz kaşe verisi 400 &#x60;INVALID_STAMP_DATA&#x60; döner; ikisinde de sözleşme oluşturulmaz, kredi düşülmez. Kaşe slug&#39;ına düz metin gönderilirse kaşeye yazılmaz ve &#x60;variables_ignored&#x60; içinde görünür. Toplu uçlar nesne değeri desteklemez (400 &#x60;INVALID_VARIABLES&#x60;, &#x60;details.reason: unsupported_endpoint&#x60;).  |  [optional] |
|**documentVariables** | **Map&lt;String, Map&lt;String, PartyMappingInputVariablesValue&gt;&gt;** | Çok belgeli şablonda BELGE BAŞINA ortak değerler (o belgedeki tüm taraflar ve partisiz alanlar için). Opsiyoneldir; göndermezseniz &#x60;variables&#x60; ve &#x60;party_mapping[].variables&#x60; eskisi gibi çalışır.  Öncelik (alan bazında; bir seviyede anahtar yoksa bir alttakine düşer, açıkça gönderilen boş metin ya da null o seviyede kalır): 1. &#x60;party_mapping[i].document_variables[belge][slug]&#x60; 2. &#x60;document_variables[belge][slug]&#x60; 3. &#x60;party_mapping[i].variables[slug]&#x60; 4. &#x60;variables[slug]&#x60;  Şablona ait olmayan ya da bu istekte gönderilmeyen belge kimliği 400 &#x60;INVALID_DOCUMENT_VARIABLES&#x60; ile reddedilir; sözleşme oluşturulmaz, kredi düşülmez, davet gönderilmez.  |  [optional] |
|**hasTimestamp** | **Boolean** | TÜBİTAK zaman damgası |  [optional] |
|**allowedSignatureVariants** | [**List&lt;AllowedSignatureVariantsEnum&gt;**](#List&lt;AllowedSignatureVariantsEnum&gt;) | İmzacının kullanabileceği dijital imza yöntemleri ve görünme sırası. Dizinin SIRASI imza ekranındaki sekme sırasıdır; listede olmayan yöntem o sözleşmede kapalıdır. Değerler: &#x60;draw&#x60; (parmakla veya fareyle çizerek), &#x60;type&#x60; (adını yazarak), &#x60;upload&#x60; (imza görselini dosya olarak yükleyerek), &#x60;phone&#x60; (telefonda çizerek).  Gönderilmezse (veya &#x60;null&#x60; gönderilirse) şablonun ayarı, şablonda da tanımlı değilse organizasyonun ayarı geçerli olur; hiçbiri tanımlı değilse dört yöntem varsayılan sırayla açıktır.  Bu alan toleranslı okunur: boş dizi, dizi olmayan bir değer veya tanınmayan bir değer gönderilirse alan YOK SAYILIR ve üst katmandan devralınır. Yani boş dizi göndermek \&quot;tüm yöntemleri kapat\&quot; anlamına GELMEZ; en az bir yöntem her zaman açık kalır. Büyük harfli değerler (&#x60;DRAW&#x60;) kabul edilir, tekrar eden değerler teke indirilir.  Yalnız &#x60;phone&#x60; içeren bir liste reddedilir (400 &#x60;SIGNATURE_VARIANTS_PHONE_ONLY&#x60;): telefonda çizim mobil cihazda kullanılamadığı için tek başına bırakılırsa imzacı yöntemsiz kalır. En az bir &#x60;phone&#x60; olmayan yöntem bırakın.  &#x60;phone_draw&#x60; (\&quot;Yalnız telefonda çizerek\&quot;) ayrı bir moddur ve YALNIZ tek başına gönderilir: &#x60;[\&quot;phone_draw\&quot;]&#x60;. Bilgisayardan açan imzacı yalnız QR kod ile telefona yönlendirilir ve imzasını telefonda çizer; telefondan açan imzacı yalnız çizerek imzalar. Başka bir yöntemle birlikte gönderilirse 400 &#x60;SIGNATURE_VARIANTS_PHONE_DRAW_EXCLUSIVE&#x60; döner.  Ayar sözleşme düzeyindedir; imzacı bazında farklılaştırılamaz. &#x60;party_mapping&#x60; girdilerine yazılan böyle bir alan yok sayılır.  |  [optional] |
|**dispatchNotifications** | **Boolean** | &#x60;false&#x60; verilirse sözleşme yalnız oluşturulur, davet gönderilmez; daha sonra &#x60;POST /api/v1/demands/{demandId}/dispatch&#x60; ile gönderilir (çok-belgeli zarf akışında belgeler bu arada eklenir). Varsayılan açık: davetler oluşturma anında gider.  |  [optional] |
|**sendSmsNotifications** | **Boolean** |  |  [optional] |
|**sendEmailNotifications** | **Boolean** |  |  [optional] |
|**smsTitle** | **String** | SMS gönderici adı |  [optional] |
|**smsContent** | **String** | Custom SMS gövdesi. **Sadece** çağıran organizasyon **PRO veya ENTERPRISE planda** ise ve aktif &#x60;OrganizationSmsConfig&#x60; (sender_name dolu) varsa kabul edilir; aksi halde 403 &#x60;SMS_CUSTOMIZATION_NOT_ALLOWED&#x60; döner.  FREE/BASIC planda olan veya kendi SMS sağlayıcısı tanımlı olmayan müşterilerin marka itibarını korumak için sistem default sağlayıcısı (Codeck NetGSM) ile gönderim yapılır ve özel metin reddedilir. Kendi sağlayıcınızı tanımlamak için Dashboard → Organizasyon → SMS Ayarları sayfasını kullanın.  Boş string / null gönderirseniz \&quot;clear\&quot; olarak yorumlanır (gating&#39;den geçer).  |  [optional] |
|**emailContent** | **String** | Custom e-posta gövdesi |  [optional] |
|**expiryDate** | **OffsetDateTime** |  |  [optional] |
|**requireTcVerification** | **Boolean** |  |  [optional] |
|**requireBiometricVerification** | **Boolean** |  |  [optional] |
|**reminderSettings** | [**ReminderSettings**](ReminderSettings.md) | Bu sözleşme için hatırlatma ayarlarını **şablon default&#39;unu override** ederek belirtir. Yollanmazsa şablonun &#x60;reminder_*&#x60; alanları kullanılır (PUT /api/templates/:id ile dashboard&#39;dan kaydedilen değerler); şablonda da yoksa &#x60;{enabled:true, intervals_hours:[48], max_reminders:1, channels:[\&quot;email\&quot;]}&#x60; default&#39;u uygulanır. Demand oluşumunda &#x60;ReminderConfig&#x60; satırı yaratılır ve BullMQ kuyruğuna scheduled hatırlatmalar yazılır.  |  [optional] |
|**termStartMode** | [**TermStartModeEnum**](#TermStartModeEnum) | Sözleşme süre/yenileme takibi. Gönderilmezse şablonun takip politikası kullanılır. Tam alan kümesi ve doğrulama kuralları için &#x60;ContractTermInput&#x60; şemasına bakın (&#x60;PATCH /demands/{id}/term&#x60; ile AYNI alan adları).  |  [optional] |
|**termStartDate** | **LocalDate** | Yalnız term_start_mode: FIXED_DATE iken kullanılır. |  [optional] |
|**termDurationMonths** | **Integer** |  |  [optional] |
|**termFixedEndDate** | **LocalDate** |  |  [optional] |
|**renewalType** | [**RenewalTypeEnum**](#RenewalTypeEnum) |  |  [optional] |
|**renewalPeriodMonths** | **Integer** | AUTO_RENEW gerektirir; verilmezse term_duration_months&#39;tan devralınır. |  [optional] |
|**noticeDays** | **Integer** |  |  [optional] |
|**reminderOffsets** | **List&lt;Integer&gt;** |  |  [optional] |
|**notifyCounterparty** | **Boolean** |  |  [optional] |



## Enum: List&lt;AllowedSignatureVariantsEnum&gt;

| Name | Value |
|---- | -----|
| DRAW | &quot;draw&quot; |
| TYPE | &quot;type&quot; |
| UPLOAD | &quot;upload&quot; |
| PHONE | &quot;phone&quot; |
| PHONE_DRAW | &quot;phone_draw&quot; |



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



