# ReportsApi

All URIs are relative to *https://api-prd.imzala.org*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**apiV1ReportsGet**](#apiv1reportsget) | **GET** /api/v1/reports | Rapor / özet (agrege sözleşme durum sayıları)|

# **apiV1ReportsGet**
> ApiV1ReportsGet200Response apiV1ReportsGet()

API key sahibinin workspace\'indeki sözleşmelerin agrege durum sayılarını döner (bekleyen / tamamlanan / iptal / süresi-dolmuş + bu ay oluşturulan). Yalnızca sayı; PII içermez. Kişisel workspace yalnızca sahibinin, org workspace org\'un sözleşmelerini sayar (IDOR-safe, GET /demands ile aynı SSOT). 

### Example

```typescript
import {
    ReportsApi,
    Configuration
} from '@imzala/server-sdk-node';

const configuration = new Configuration();
const apiInstance = new ReportsApi(configuration);

const { status, data } = await apiInstance.apiV1ReportsGet();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**ApiV1ReportsGet200Response**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | Özet sayılar |  -  |
|**403** | Yetersiz kapsam (demands:read gerekli) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

