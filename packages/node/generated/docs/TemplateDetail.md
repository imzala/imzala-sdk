# TemplateDetail


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional] [default to undefined]
**name** | **string** |  | [optional] [default to undefined]
**description** | **string** |  | [optional] [default to undefined]
**category** | **string** |  | [optional] [default to undefined]
**usage_count** | **number** |  | [optional] [default to undefined]
**parties** | [**Array&lt;TemplatePartySummary&gt;**](TemplatePartySummary.md) |  | [optional] [default to undefined]
**pages_count** | **number** |  | [optional] [default to undefined]
**variables** | [**Array&lt;TemplateVariable&gt;**](TemplateVariable.md) |  | [optional] [default to undefined]

## Example

```typescript
import { TemplateDetail } from '@imzala/server-sdk-node';

const instance: TemplateDetail = {
    id,
    name,
    description,
    category,
    usage_count,
    parties,
    pages_count,
    variables,
};
```

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
