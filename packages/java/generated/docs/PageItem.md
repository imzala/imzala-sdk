

# PageItem

Sözleşme sayfasına yerleştirilen alan tanımı. Tüm koordinatlar sayfa boyutuna göre normalize edilmiş [0,1] aralığında. Origin top-left (PDF/canvas standardı). 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**pageId** | **Integer** | AgreementPage.id (&#x60;/upload&#x60; response&#39;undaki &#x60;pages[].id&#x60;) |  |
|**partyId** | **UUID** | &#x60;signature&#x60; ve doldurulabilir alanlar (&#x60;dynamic_text&#x60;, &#x60;cells&#x60;, &#x60;date&#x60;, &#x60;dropdown&#x60;, &#x60;checkbox&#x60;, &#x60;radio&#x60;) için **zorunlu** — alanı dolduracak/imzalayacak partinin id&#39;si (&#x60;signing_urls[].party_id&#x60;). &#x60;text&#x60; ve &#x60;stamp&#x60; için null.  |  [optional] |
|**itemType** | [**ItemTypeEnum**](#ItemTypeEnum) |  |  |
|**positionX** | **BigDecimal** | Sayfa genişliğine göre x koordinatı (sol&#x3D;0) |  |
|**positionY** | **BigDecimal** | Sayfa yüksekliğine göre y koordinatı (üst&#x3D;0) |  |
|**width** | **BigDecimal** |  |  |
|**height** | **BigDecimal** |  |  |
|**isRequired** | **Boolean** | İmza/alan zorunlu mu — tarafın bu alanı doldurmadan imzalayamadığı |  [optional] |
|**slug** | **String** | Alan tanımlayıcı (snake_case, 2-50 karakter). Doldurulabilir alanlar için **önerilir**. &#x60;dynamic_text&#x60;/&#x60;cells&#x60; gibi değişken alanlarda &#x60;config.defaultSource&#x60; ile system değişkenleri (&#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.government_id}}&#x60; vb.) bağlanır.  |  [optional] |
|**label** | **String** | Kullanıcıya gösterilecek etiket |  [optional] |
|**config** | **Object** | Item type&#39;a özgü konfigürasyon: - &#x60;dynamic_text&#x60;: &#x60;{ defaultSource, defaultValue }&#x60; - &#x60;cells&#x60;: &#x60;{ cellCount, defaultSource }&#x60; - &#x60;date&#x60;: &#x60;{ defaultSource, defaultValue }&#x60; - &#x60;dropdown&#x60;/&#x60;radio&#x60;: &#x60;{ options: [{label, value}], defaultValue }&#x60; - &#x60;checkbox&#x60;: &#x60;{ checkedByDefault }&#x60; - &#x60;stamp&#x60;: &#x60;{ stampData }&#x60; (base64 data URL)  |  [optional] |



## Enum: ItemTypeEnum

| Name | Value |
|---- | -----|
| SIGNATURE | &quot;signature&quot; |
| TEXT | &quot;text&quot; |
| DYNAMIC_TEXT | &quot;dynamic_text&quot; |
| CELLS | &quot;cells&quot; |
| DATE | &quot;date&quot; |
| DROPDOWN | &quot;dropdown&quot; |
| CHECKBOX | &quot;checkbox&quot; |
| RADIO | &quot;radio&quot; |
| STAMP | &quot;stamp&quot; |



