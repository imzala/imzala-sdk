# ImzalaApiClient.Model.TemplateUsage

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Kind** | **string** | Rehberin anlattığı akış. Bu uç yalnız belgeli şablonu çözer, dolayısıyla burada daima &#x60;DOCUMENT&#x60; döner (Alan Şablonu bu uçta 404&#39;tür; kimliği &#x60;GET /api/v1/field-templates&#x60; ile öğrenilir ve &#x60;POST /api/v1/demands/upload&#x60; ile kullanılır).  | [optional] 
**FormFields** | [**List&lt;TemplateUsageFormFieldsInner&gt;**](TemplateUsageFormFieldsInner.md) | &#x60;multipart/form-data&#x60; alanları. Belgeli şablon JSON gövde kullandığı için bu uçta boş dizidir.  | [optional] 
**Template** | [**ApiV1TemplatesIdPatch200ResponseData**](ApiV1TemplatesIdPatch200ResponseData.md) |  | [optional] 
**Endpoint** | [**TemplateUsageEndpoint**](TemplateUsageEndpoint.md) |  | [optional] 
**RequiredHeaders** | **Dictionary&lt;string, string&gt;** |  | [optional] 
**Parties** | [**List&lt;TemplateUsagePartiesInner&gt;**](TemplateUsagePartiesInner.md) |  | [optional] 
**Variables** | [**List&lt;TemplateUsageVariablesInner&gt;**](TemplateUsageVariablesInner.md) |  | [optional] 
**Documents** | [**List&lt;TemplateUsageDocumentsInner&gt;**](TemplateUsageDocumentsInner.md) | Şablonun belgeleri (sıra artan). &#x60;document_variables&#x60; anahtarları buradaki &#x60;template_document_id&#x60; değerleridir.  | [optional] 
**ExampleRequest** | [**TemplateUsageExampleRequest**](TemplateUsageExampleRequest.md) |  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

