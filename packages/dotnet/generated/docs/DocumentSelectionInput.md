# ImzalaApiClient.Model.DocumentSelectionInput
Bu istekte gönderilecek belgeleri şablon varsayılanına göre değiştirir. Alan hiç gönderilmezse `default_included: true` olan belgeler gider. `include` varsayılanı kapalı belgeyi ekler, `exclude` varsayılanı açık belgeyi (zorunlu olsa bile) çıkarır. Çıkarılan belge bu imza sürecine (zarfa) eklenmez: imzacıya gösterilmez, imzalı PDF'te ve tamamlanma sertifikasında yer almaz, kredi hesaplamasına dahil edilmez. Sonuçta en az bir belge kalmalıdır ve eşlediğiniz her role en az bir seçili belge düşmelidir. Mevzuat gereği verilmesi gereken belgeleri çıkarmadan önce `is_required` açıklamasına bakın; bu belgelerin verilmesi yükümlülüğü sözleşmeyi gönderene aittir. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Include** | **List&lt;Guid&gt;** | Şablon belge kimlikleri (&#x60;GET /api/v1/templates/{id}&#x60; → &#x60;documents[].id&#x60;). | [optional] 
**Exclude** | **List&lt;Guid&gt;** |  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

