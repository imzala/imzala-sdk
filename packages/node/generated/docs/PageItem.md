# PageItem

Sözleşme sayfasına yerleştirilen alan tanımı. Tüm koordinatlar sayfa boyutuna göre normalize edilmiş [0,1] aralığında. Origin top-left (PDF/canvas standardı). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**page_id** | **number** | AgreementPage.id (&#x60;/upload&#x60; response\&#39;undaki &#x60;pages[].id&#x60;) | [default to undefined]
**party_id** | **string** | &#x60;signature&#x60; ve doldurulabilir alanlar (&#x60;dynamic_text&#x60;, &#x60;cells&#x60;, &#x60;date&#x60;, &#x60;dropdown&#x60;, &#x60;checkbox&#x60;, &#x60;radio&#x60;) için **zorunlu** — alanı dolduracak/imzalayacak partinin id\&#39;si (&#x60;signing_urls[].party_id&#x60;). &#x60;text&#x60; ve &#x60;stamp&#x60; için null.  | [optional] [default to undefined]
**item_type** | **string** |  | [default to undefined]
**position_x** | **number** | Sayfa genişliğine göre x koordinatı (sol&#x3D;0) | [default to undefined]
**position_y** | **number** | Sayfa yüksekliğine göre y koordinatı (üst&#x3D;0) | [default to undefined]
**width** | **number** |  | [default to undefined]
**height** | **number** |  | [default to undefined]
**is_required** | **boolean** | İmza/alan zorunlu mu — tarafın bu alanı doldurmadan imzalayamadığı | [optional] [default to false]
**slug** | **string** | Alan tanımlayıcı (snake_case, 2-50 karakter). Doldurulabilir alanlar için **önerilir**. &#x60;dynamic_text&#x60;/&#x60;cells&#x60; gibi değişken alanlarda &#x60;config.defaultSource&#x60; ile system değişkenleri (&#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.government_id}}&#x60; vb.) bağlanır.  | [optional] [default to undefined]
**label** | **string** | Kullanıcıya gösterilecek etiket | [optional] [default to undefined]
**config** | **object** | Item type\&#39;a özgü konfigürasyon: - &#x60;dynamic_text&#x60;: &#x60;{ defaultSource, defaultValue }&#x60; - &#x60;cells&#x60;: &#x60;{ cellCount, defaultSource }&#x60; - &#x60;date&#x60;: &#x60;{ defaultSource, defaultValue }&#x60; - &#x60;dropdown&#x60;/&#x60;radio&#x60;: &#x60;{ options: [{label, value}], defaultValue }&#x60; - &#x60;checkbox&#x60;: &#x60;{ checkedByDefault }&#x60; - &#x60;stamp&#x60;: &#x60;{ stampData }&#x60; (base64 data URL)  | [optional] [default to undefined]

## Example

```typescript
import { PageItem } from '@imzala/server-sdk-node';

const instance: PageItem = {
    page_id,
    party_id,
    item_type,
    position_x,
    position_y,
    width,
    height,
    is_required,
    slug,
    label,
    config,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
