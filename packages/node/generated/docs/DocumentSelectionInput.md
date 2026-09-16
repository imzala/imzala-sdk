# DocumentSelectionInput

Bu istekte gönderilecek belgeleri şablon varsayılanına göre değiştirir. Alan hiç gönderilmezse `default_included: true` olan belgeler gider. `include` varsayılanı kapalı belgeyi ekler, `exclude` varsayılanı açık belgeyi (zorunlu olsa bile) çıkarır. Çıkarılan belge bu imza sürecine (zarfa) eklenmez: imzacıya gösterilmez, imzalı PDF\'te ve tamamlanma sertifikasında yer almaz, kredi hesaplamasına dahil edilmez. Sonuçta en az bir belge kalmalıdır ve eşlediğiniz her role en az bir seçili belge düşmelidir. Mevzuat gereği verilmesi gereken belgeleri çıkarmadan önce `is_required` açıklamasına bakın; bu belgelerin verilmesi yükümlülüğü sözleşmeyi gönderene aittir. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**include** | **Array&lt;string&gt;** | Şablon belge kimlikleri (&#x60;GET /api/v1/templates/{id}&#x60; → &#x60;documents[].id&#x60;). | [optional] [default to undefined]
**exclude** | **Array&lt;string&gt;** |  | [optional] [default to undefined]

## Example

```typescript
import { DocumentSelectionInput } from '@imzala/server-sdk-node';

const instance: DocumentSelectionInput = {
    include,
    exclude,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
