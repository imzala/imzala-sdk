# TemplateUsage


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
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
    template,
    endpoint,
    required_headers,
    parties,
    variables,
    example_request,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
