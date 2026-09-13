# AccountApi

All URIs are relative to *https://api-prd.imzala.org*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiV1MeGet**](#apiv1meget) | **GET** /api/v1/me | API key sahibi bilgisi|

# **apiV1MeGet**
> ApiV1MeGet200Response apiV1MeGet()

Çağrıyı yapan API key\'in sahibi hakkında temel bilgileri döner: kullanıcı kimliği, e-posta, isim, aktif workspace ve kalan kredi. Scope gerektirmez — geçerli herhangi bir API key (hangi scope\'a sahip olursa olsun, ör. sadece `demands:read`) bu endpoint\'i çağırabilir. 

### Example

```typescript
import {
    AccountApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new AccountApi(configuration);

const { status, data } = await apiInstance.apiV1MeGet();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**ApiV1MeGet200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Başarılı |  -  |
|**401** | API key geçersiz veya eksik |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

