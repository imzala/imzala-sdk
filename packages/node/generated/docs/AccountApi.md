# AccountApi

All URIs are relative to *https://api-prd.imzala.org*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiV1MeGet**](#apiv1meget) | **GET** /api/v1/me | API key sahibi bilgisi|

# **apiV1MeGet**
> ApiV1MeGet200Response apiV1MeGet()

Çağrıyı yapan API key\'in sahibi hakkında temel bilgileri döner: kullanıcı kimliği, e-posta, isim, aktif workspace ve kalan kredi. `timestamps` scope\'u gerektirir; scope yoksa 403 `INSUFFICIENT_SCOPE` döner. 

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
|**403** | **INSUFFICIENT_SCOPE** — API key\&#39;in &#x60;timestamps&#x60; scope\&#39;u yok. Dashboard → Geliştirici → API Anahtarları sayfasından scope\&#39;u güncelleyin.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

