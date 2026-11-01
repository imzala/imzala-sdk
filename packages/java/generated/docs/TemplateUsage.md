

# TemplateUsage


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**kind** | **String** | Rehberin anlattığı akış. Bu uç yalnız belgeli şablonu çözer, dolayısıyla burada daima &#x60;DOCUMENT&#x60; döner (Alan Şablonu bu uçta 404&#39;tür; kimliği &#x60;GET /api/v1/field-templates&#x60; ile öğrenilir ve &#x60;POST /api/v1/demands/upload&#x60; ile kullanılır).  Bilinen değerler: DOCUMENT, FIELD_LAYOUT. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  [optional] |
|**formFields** | [**List&lt;TemplateUsageFormFieldsInner&gt;**](TemplateUsageFormFieldsInner.md) | &#x60;multipart/form-data&#x60; alanları. Belgeli şablon JSON gövde kullandığı için bu uçta boş dizidir.  |  [optional] |
|**template** | [**ApiV1TemplatesIdPatch200ResponseData**](ApiV1TemplatesIdPatch200ResponseData.md) |  |  [optional] |
|**endpoint** | [**TemplateUsageEndpoint**](TemplateUsageEndpoint.md) |  |  [optional] |
|**requiredHeaders** | **Map&lt;String, String&gt;** |  |  [optional] |
|**parties** | [**List&lt;TemplateUsagePartiesInner&gt;**](TemplateUsagePartiesInner.md) |  |  [optional] |
|**variables** | [**List&lt;TemplateUsageVariablesInner&gt;**](TemplateUsageVariablesInner.md) |  |  [optional] |
|**documents** | [**List&lt;TemplateUsageDocumentsInner&gt;**](TemplateUsageDocumentsInner.md) | Şablonun belgeleri (sıra artan). &#x60;document_variables&#x60; anahtarları buradaki &#x60;template_document_id&#x60; değerleridir.  |  [optional] |
|**exampleRequest** | [**TemplateUsageExampleRequest**](TemplateUsageExampleRequest.md) |  |  [optional] |
