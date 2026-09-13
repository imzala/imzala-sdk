# TemplateUsage


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**kind** | **string** | Rehberin anlattığı akış. Bu uç yalnız belgeli şablonu çözer, dolayısıyla burada daima &#x60;DOCUMENT&#x60; döner (Alan Şablonu bu uçta 404\&#39;tür; kimliği &#x60;GET /api/v1/field-templates&#x60; ile öğrenilir ve &#x60;POST /api/v1/demands/upload&#x60; ile kullanılır).  | [optional] [default to undefined]
**form_fields** | [**Array&lt;TemplateUsageFormFieldsInner&gt;**](TemplateUsageFormFieldsInner.md) | &#x60;multipart/form-data&#x60; alanları. Belgeli şablon JSON gövde kullandığı için bu uçta boş dizidir.  | [optional] [default to undefined]
**template** | [**ApiV1TemplatesIdPatch200ResponseData**](ApiV1TemplatesIdPatch200ResponseData.md) |  | [optional] [default to undefined]
**endpoint** | [**TemplateUsageEndpoint**](TemplateUsageEndpoint.md) |  | [optional] [default to undefined]
**required_headers** | **{ [key: string]: string; }** |  | [optional] [default to undefined]
**parties** | [**Array&lt;TemplateUsagePartiesInner&gt;**](TemplateUsagePartiesInner.md) |  | [optional] [default to undefined]
**variables** | [**Array&lt;TemplateUsageVariablesInner&gt;**](TemplateUsageVariablesInner.md) |  | [optional] [default to undefined]
**example_request** | [**TemplateUsageExampleRequest**](TemplateUsageExampleRequest.md) |  | [optional] [default to undefined]

## Example

```typescript
import { TemplateUsage } from '@imzala/server-sdk-node';

const instance: TemplateUsage = {
    kind,
    form_fields,
    template,
    endpoint,
    required_headers,
    parties,
    variables,
    example_request,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
