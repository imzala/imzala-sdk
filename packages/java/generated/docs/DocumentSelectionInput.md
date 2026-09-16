

# DocumentSelectionInput

Bu istekte gönderilecek belgeleri şablon varsayılanına göre değiştirir. Alan hiç gönderilmezse `default_included: true` olan belgeler gider. `include` varsayılanı kapalı belgeyi ekler, `exclude` varsayılanı açık belgeyi (zorunlu olsa bile) çıkarır. Çıkarılan belge bu imza sürecine (zarfa) eklenmez: imzacıya gösterilmez, imzalı PDF'te ve tamamlanma sertifikasında yer almaz, kredi hesaplamasına dahil edilmez. Sonuçta en az bir belge kalmalıdır ve eşlediğiniz her role en az bir seçili belge düşmelidir. Mevzuat gereği verilmesi gereken belgeleri çıkarmadan önce `is_required` açıklamasına bakın; bu belgelerin verilmesi yükümlülüğü sözleşmeyi gönderene aittir. 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**include** | **List&lt;UUID&gt;** | Şablon belge kimlikleri (&#x60;GET /api/v1/templates/{id}&#x60; → &#x60;documents[].id&#x60;). |  [optional] |
|**exclude** | **List&lt;UUID&gt;** |  |  [optional] |



