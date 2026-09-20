# ImzalaApiClient.Api.DemandsApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|--------|--------------|-------------|
| [**ApiV1DemandsBulkPost**](DemandsApi.md#apiv1demandsbulkpost) | **POST** /api/v1/demands/bulk | Toplu sözleşme oluştur (tek şablondan N alıcı) |
| [**ApiV1DemandsDemandIdDispatchPost**](DemandsApi.md#apiv1demandsdemandiddispatchpost) | **POST** /api/v1/demands/{demandId}/dispatch | Zarfı imzaya gönder (yayınla + davet) |
| [**ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPut**](DemandsApi.md#apiv1demandsdemandiddocumentsdocidassignmentsput) | **PUT** /api/v1/demands/{demandId}/documents/{docId}/assignments | Belgeye imzacı ata (tam-küme replace) |
| [**ApiV1DemandsDemandIdDocumentsDocIdDelete**](DemandsApi.md#apiv1demandsdemandiddocumentsdociddelete) | **DELETE** /api/v1/demands/{demandId}/documents/{docId} | Belgeyi zarftan sil |
| [**ApiV1DemandsDemandIdDocumentsDocIdPatch**](DemandsApi.md#apiv1demandsdemandiddocumentsdocidpatch) | **PATCH** /api/v1/demands/{demandId}/documents/{docId} | Belge metadata güncelle |
| [**ApiV1DemandsDemandIdDocumentsGet**](DemandsApi.md#apiv1demandsdemandiddocumentsget) | **GET** /api/v1/demands/{demandId}/documents | Zarf belge listesi |
| [**ApiV1DemandsDemandIdDocumentsOrderPut**](DemandsApi.md#apiv1demandsdemandiddocumentsorderput) | **PUT** /api/v1/demands/{demandId}/documents/order | Zarftaki belgelerin sırasını değiştir |
| [**ApiV1DemandsDemandIdDocumentsPost**](DemandsApi.md#apiv1demandsdemandiddocumentspost) | **POST** /api/v1/demands/{demandId}/documents | Zarfa metadata-only belge ekle |
| [**ApiV1DemandsDemandIdDocumentsUploadPost**](DemandsApi.md#apiv1demandsdemandiddocumentsuploadpost) | **POST** /api/v1/demands/{demandId}/documents/upload | Zarfa dosya yükle (belge başına tek dosya) |
| [**ApiV1DemandsGet**](DemandsApi.md#apiv1demandsget) | **GET** /api/v1/demands | Sözleşme listesi (counts-only, PII&#39;siz) |
| [**ApiV1DemandsIdBelgeDocumentIdPdfGet**](DemandsApi.md#apiv1demandsidbelgedocumentidpdfget) | **GET** /api/v1/demands/{id}/belge/{document_id}/pdf | Belge-özgü imzalı PDF (çok-belgeli zarf) |
| [**ApiV1DemandsIdCancelPost**](DemandsApi.md#apiv1demandsidcancelpost) | **POST** /api/v1/demands/{id}/cancel | Sözleşme iptal (void) |
| [**ApiV1DemandsIdCertificateGet**](DemandsApi.md#apiv1demandsidcertificateget) | **GET** /api/v1/demands/{id}/certificate | Tamamlanma sertifikası (PAdES B-T) |
| [**ApiV1DemandsIdDelete**](DemandsApi.md#apiv1demandsiddelete) | **DELETE** /api/v1/demands/{id} | Sözleşme sil (yalnızca tamamlanmamış) |
| [**ApiV1DemandsIdEmbedSessionPost**](DemandsApi.md#apiv1demandsidembedsessionpost) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint) |
| [**ApiV1DemandsIdGet**](DemandsApi.md#apiv1demandsidget) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi |
| [**ApiV1DemandsIdItemsPost**](DemandsApi.md#apiv1demandsiditemspost) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace) |
| [**ApiV1DemandsIdPartiesPartyIdResendPost**](DemandsApi.md#apiv1demandsidpartiespartyidresendpost) | **POST** /api/v1/demands/{id}/parties/{partyId}/resend | Tekil tarafa imza davetini tekrar gönder |
| [**ApiV1DemandsIdPdfGet**](DemandsApi.md#apiv1demandsidpdfget) | **GET** /api/v1/demands/{id}/pdf | İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme) |
| [**ApiV1DemandsIdTimelineGet**](DemandsApi.md#apiv1demandsidtimelineget) | **GET** /api/v1/demands/{id}/timeline | İmza denetim izi (maskeli) |
| [**ApiV1DemandsPost**](DemandsApi.md#apiv1demandspost) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan) |
| [**ApiV1DemandsUploadPost**](DemandsApi.md#apiv1demandsuploadpost) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz) |
| [**ApiV1FieldTemplatesIdPreviewLayoutPost**](DemandsApi.md#apiv1fieldtemplatesidpreviewlayoutpost) | **POST** /api/v1/field-templates/{id}/preview-layout | Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener |

<a id="apiv1demandsbulkpost"></a>
# **ApiV1DemandsBulkPost**
> ApiV1DemandsBulkPost200Response ApiV1DemandsBulkPost (ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest, Guid? xWorkspaceId = null)

Toplu sözleşme oluştur (tek şablondan N alıcı)

Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \"bulk send\" modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Davranış:** - `rows` en fazla 10 (aşarsa 400 `BULK_MAX_10`). Daha büyük listeler   istemci tarafında 10'arlı parçalara bölünür. - Kredi: sabit \"1 satır = 1 kredi\" değildir, her satırın maliyeti   `POST /demands` ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; `eidas_timestamp` seçilirse   satır başına +1 kredi eklenir). Şablon çok-belgeli bir zarf   tanımlıyorsa imzacı başına maliyet o imzacıya ATANMIŞ belge   sayısına göre hesaplanır ve belge sayısına göre azalan birim   fiyatlı bir indirim uygulanır (1 belge %0, 2 belge %30, 3 ve üzeri   %50, imzacı başına ayrı yukarı yuvarlanır; bkz. `POST /demands`   \"Kredi (çok belgeli zarf)\" bölümü). PAdES seviye eki yalnız QES'te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   `ocr_id` ve `liveness` +1 kredi, diğerleri 0'dır. `options`   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır `failed` (`error: \"INSUFFICIENT_CREDITS\"`)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine `failed` döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (`demand_id` response'ta bulunur, davet   gönderilmemiştir); mutabakat kredi-dışı bir hatayla düşerse satır   `failed` (`error: \"RECONCILE_FAILED\"`) döner, sözleşme yine taslağa   düşer. - Atamasız imzacı: çok belgeli şablonda eşlenen bir taraf hiçbir   belgeye atanmamışsa (ve `options.dispatch_notifications` `false`   DEĞİLSE) o satır `failed` (`error: \"PARTY_WITHOUT_DOCUMENTS\"`)   döner; kontrol satır oluşturulmadan ÖNCE (`demand_id` YOK) veya   oluşturulduktan hemen SONRA (sözleşme taslağa düşer, `demand_id` +   `party_ids` döner) çalışabilir. Diğer satırlar etkilenmez. - Belge seçimi satır başınadır: `rows[i].documents` (`POST /demands`   ile aynı şema). `options.documents` gönderilirse 400   `INVALID_DOCUMENT_SELECTION`. Satırın seçim hatası o satırı `failed`   yapar (`error`: `INVALID_DOCUMENT_SELECTION` + `details`, ya da   `PARTY_WITHOUT_DOCUMENTS` + `template_party_ids`); diğer satırlar   etkilenmez. - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace'in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar `failed` olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - `X-Workspace-Id` header'ı ile organizasyon workspace'i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsBulkPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var apiV1DemandsBulkPostRequest = new ApiV1DemandsBulkPostRequest(); // ApiV1DemandsBulkPostRequest | 
            var xWorkspaceId = "xWorkspaceId_example";  // Guid? | Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   `WORKSPACE_MISMATCH` döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (`Not a member of this organization`).  (optional) 

            try
            {
                // Toplu sözleşme oluştur (tek şablondan N alıcı)
                ApiV1DemandsBulkPost200Response result = apiInstance.ApiV1DemandsBulkPost(apiV1DemandsBulkPostRequest, xWorkspaceId);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsBulkPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsBulkPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Toplu sözleşme oluştur (tek şablondan N alıcı)
    ApiResponse<ApiV1DemandsBulkPost200Response> response = apiInstance.ApiV1DemandsBulkPostWithHttpInfo(apiV1DemandsBulkPostRequest, xWorkspaceId);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsBulkPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **apiV1DemandsBulkPostRequest** | [**ApiV1DemandsBulkPostRequest**](ApiV1DemandsBulkPostRequest.md) |  |  |
| **xWorkspaceId** | **Guid?** | Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  | [optional]  |

### Return type

[**ApiV1DemandsBulkPost200Response**](ApiV1DemandsBulkPost200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Batch tamamlandı (kısmi başarı dahil). &#x60;results&#x60; giriş sırasında, her satır &#x60;row_index&#x60; ile eşlenir.  |  -  |
| **400** | Geçersiz istek. Olası kodlar: &#x60;BULK_MAX_10&#x60; (&#x60;rows&#x60; 10&#39;dan fazla), boş &#x60;rows&#x60;, eksik &#x60;template_id&#x60;, &#x60;INVALID_DOCUMENT_SELECTION&#x60; (&#x60;options.documents&#x60; gönderildi; belge seçimi satır başınadır, bkz. &#x60;rows[i].documents&#x60;), &#x60;INVALID_PADES_LEVEL&#x60; (&#x60;options.qes_pades_level&#x60; satılabilir seviyelerden biri değil), &#x60;FIELD_LAYOUT_TEMPLATE_NOT_SENDABLE&#x60; (verilen kimlik bir Alan Şablonuna ait). AB nitelikli zaman damgası hesabınızda kapalıyken &#x60;eidas_timestamp&#x60; gönderilirse de 400 döner.  &#x60;SIGNATURE_VARIANTS_PHONE_ONLY&#x60;: &#x60;options.allowed_signature_variants&#x60; yalnız &#x60;phone&#x60; içeriyor. Batch-seviye kontrol olduğu için hiçbir satır oluşturulmaz.  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **402** | INSUFFICIENT_CREDITS: N satırın toplam tahmini maliyeti için yeterli kredi yok (ön kontrol; hiçbir sözleşme yaratılmadı). Satır bazlı yetersiz kredi durumları burada DEĞİL, 200 yanıtı içindeki &#x60;results[].status: \&quot;failed\&quot;&#x60; altında döner.  |  -  |
| **403** | INSUFFICIENT_SCOPE (demands:write yok) veya SMS_CUSTOMIZATION_NOT_ALLOWED |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | &#x60;QES_NOT_SUPPORTED_MULTI_DOCUMENT&#x60;: nitelikli imza (QES) birden çok belge içeren zarflarda henüz desteklenmiyor.  |  -  |
| **429** | İstek limiti aşıldı (API anahtarı başına 5 toplu istek/dakika) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsdemandiddispatchpost"></a>
# **ApiV1DemandsDemandIdDispatchPost**
> ApiV1DemandsDemandIdDispatchPost200Response ApiV1DemandsDemandIdDispatchPost (Guid demandId, ApiV1DemandsDemandIdDispatchPostRequest? apiV1DemandsDemandIdDispatchPostRequest = null)

Zarfı imzaya gönder (yayınla + davet)

Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, `DRAFT` ise sözleşmeyi `PENDING`'e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  🔴 **Bu uçta yukarıdaki `/documents*` ailesinin bayrak kapısı (`ENVELOPE_DECISION_ENFORCE`) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — `dispatch_notifications: false` ile oluşturulur, ardından `POST .../documents` / `POST .../documents/upload` ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; `send_invitations` gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir. **Bu uç çağrılana kadar** birden çok belgeli bir zarfın oluşturma yanıtındaki `signing_url`'leri ÇALIŞMAZ (imzacı için 410; `POST .../parties/{partyId}/resend` ve `POST .../reminders` için 409 `ENVELOPE_NOT_DISPATCHED` döner); belge uçları (`/documents*`) ise gönderim beklemeden kullanılabilir.  **Kredi:** tek tahsilat noktası burasıdır (`reconcileDemandSigningCost`). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (`dispatched: false` döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. `v1ResendRateLimiter`, `/parties/{partyId}/resend` ile AYNI dakika-bazlı freni paylaşır). 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsDemandIdDispatchPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var demandId = "demandId_example";  // Guid | 
            var apiV1DemandsDemandIdDispatchPostRequest = new ApiV1DemandsDemandIdDispatchPostRequest?(); // ApiV1DemandsDemandIdDispatchPostRequest? |  (optional) 

            try
            {
                // Zarfı imzaya gönder (yayınla + davet)
                ApiV1DemandsDemandIdDispatchPost200Response result = apiInstance.ApiV1DemandsDemandIdDispatchPost(demandId, apiV1DemandsDemandIdDispatchPostRequest);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDispatchPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsDemandIdDispatchPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Zarfı imzaya gönder (yayınla + davet)
    ApiResponse<ApiV1DemandsDemandIdDispatchPost200Response> response = apiInstance.ApiV1DemandsDemandIdDispatchPostWithHttpInfo(demandId, apiV1DemandsDemandIdDispatchPostRequest);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDispatchPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **demandId** | **Guid** |  |  |
| **apiV1DemandsDemandIdDispatchPostRequest** | [**ApiV1DemandsDemandIdDispatchPostRequest?**](ApiV1DemandsDemandIdDispatchPostRequest?.md) |  | [optional]  |

### Return type

[**ApiV1DemandsDemandIdDispatchPost200Response**](ApiV1DemandsDemandIdDispatchPost200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Yayına alındı (veya zaten yayındaydı) + davet sonucu |  -  |
| **400** | &#x60;INVALID_SEND_INVITATIONS&#x60; — tanınmayan &#x60;send_invitations&#x60; değeri. |  -  |
| **402** | Kredi mutabakatı başarısız — sözleşme yayına GEÇMEZ, davet gönderilmez. &#x60;INSUFFICIENT_CREDITS&#x60; — hesabın/organizasyonun kredisi yetmiyor. &#x60;MEMBER_LIMIT_EXCEEDED&#x60; — organizasyon üyesinin aylık kredi limiti aşıldı (kredi havuzunda bakiye olsa bile).  |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | &#x60;DEMAND_EXPIRED&#x60; — sözleşmenin imza süresi geçmiş. &#x60;DEMAND_NOT_DISPATCHABLE&#x60; — tamamlanmış/iptal edilmiş sözleşme tekrar yayınlanamaz. &#x60;DISPATCH_NO_PARTIES&#x60; — sözleşmede hiç imzacı taraf yok. Zarf tamamlanamayacağı için yayına ALINMAZ; kredi dokunulmaz, statü değişmez, &#x60;demand.dispatched&#x60; olayı yayılmaz. Önce &#x60;POST /demands/{demandId}/parties&#x60; ile taraf ekleyin. &#x60;DISPATCH_TOO_MANY&#x60; — taraf sayısı 20&#39;yi aşıyor. İstek hiçbir yan etki bırakmadan reddedilir (kredi düşmez, sözleşme yayına geçmez); sözleşmeyi tarafları azaltarak gönderin veya davetleri &#x60;/parties/{partyId}/resend&#x60; ile tek tek yollayın. &#x60;PARTY_WITHOUT_DOCUMENTS&#x60;: çok belgeli zarfta eşlenen bir taraf hiçbir belgeye atanmamış. Kredi ve iddiadan ÖNCE, yan etkisiz kontrol edilir (kredi dokunulmaz, statü değişmez, davet gitmez); gövdede atamasız taraf id&#39;leri &#x60;party_ids&#x60; içinde döner. &#x60;PUT .../documents/{docId}/assignments&#x60; ile atamayı tamamlayıp tekrar deneyin. &#x60;ENVELOPE_CHANGED_DURING_DISPATCH&#x60;: kredi mutabakatı ile gönderim iddiası arasında zarf değiştirildi (ör. eşzamanlı bir istek belge/atama ekledi). Sunucu bunu kilit altında bir kez kendiliğinden yeniden dener; ikinci denemede de olursa bu kod döner. Sözleşme yayına GEÇMEDİ; istek güvenle tekrarlanabilir.  |  -  |
| **429** | Tekrar-gönderim hız sınırı aşıldı — bu uç &#x60;/parties/{partyId}/resend&#x60; ile AYNI anahtar-bazlı freni paylaşır.  &#x60;TOO_MANY_REQUESTS&#x60;: API anahtarı başına saatte 300 istek. Gövdede &#x60;retry_after_seconds&#x60; döner.  |  -  |
| **500** | &#x60;RECONCILE_FAILED&#x60; — kredi mutabakatı beklenmedik bir hatayla düştü. Sözleşme yayına GEÇMEZ ve davet gönderilmez; istek güvenle tekrarlanabilir (mutabakat idempotenttir).  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsdemandiddocumentsdocidassignmentsput"></a>
# **ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPut**
> ApiV1DemandsDemandIdDocumentsPost201Response ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPut (Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest)

Belgeye imzacı ata (tam-küme replace)

Belgeye atanmış imzacı kümesini **tam olarak** `party_ids` ile değiştirir (eski atamalar silinir, yenileri yazılır). `party_ids` boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 `INVALID_PARTY_ID` — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 `ASSIGNMENT_HAS_DECISION`). 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var demandId = "demandId_example";  // Guid | 
            var docId = "docId_example";  // Guid | 
            var apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest = new ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest(); // ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest | 

            try
            {
                // Belgeye imzacı ata (tam-küme replace)
                ApiV1DemandsDemandIdDocumentsPost201Response result = apiInstance.ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPut(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPut: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Belgeye imzacı ata (tam-küme replace)
    ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> response = apiInstance.ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **demandId** | **Guid** |  |  |
| **docId** | **Guid** |  |  |
| **apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest** | [**ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest**](ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest.md) |  |  |

### Return type

[**ApiV1DemandsDemandIdDocumentsPost201Response**](ApiV1DemandsDemandIdDocumentsPost201Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Atama güncellendi |  -  |
| **400** | &#x60;ASSIGNMENT_EMPTY&#x60; — &#x60;party_ids&#x60; eksik/boş. &#x60;INVALID_PARTY_ID&#x60; — bu sözleşmeye ait olmayan taraf id&#39;si. &#x60;ASSIGNMENT_HAS_DECISION&#x60; — karar vermiş bir imzacının ataması kaldırılamaz.  |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60; / &#x60;DEMAND_NOT_EDITABLE&#x60; / &#x60;SIGNING_ALREADY_STARTED&#x60; / &#x60;ENVELOPE_ALREADY_DISPATCHED&#x60;. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsdemandiddocumentsdociddelete"></a>
# **ApiV1DemandsDemandIdDocumentsDocIdDelete**
> ApiV1TemplatesIdDelete200Response ApiV1DemandsDemandIdDocumentsDocIdDelete (Guid demandId, Guid docId)

Belgeyi zarftan sil

Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N'e yeniden sıralanır. **Zarftaki son belge silinemez** (400 `CANNOT_DELETE_LAST_DOCUMENT`) — zarf hiç boş kalamaz. 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsDemandIdDocumentsDocIdDeleteExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var demandId = "demandId_example";  // Guid | 
            var docId = "docId_example";  // Guid | 

            try
            {
                // Belgeyi zarftan sil
                ApiV1TemplatesIdDelete200Response result = apiInstance.ApiV1DemandsDemandIdDocumentsDocIdDelete(demandId, docId);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDocumentsDocIdDelete: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Belgeyi zarftan sil
    ApiResponse<ApiV1TemplatesIdDelete200Response> response = apiInstance.ApiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo(demandId, docId);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **demandId** | **Guid** |  |  |
| **docId** | **Guid** |  |  |

### Return type

[**ApiV1TemplatesIdDelete200Response**](ApiV1TemplatesIdDelete200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Silindi |  -  |
| **400** | &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60; — zarftaki son belge. &#x60;DOCUMENT_HAS_SIGNED_CONTENT&#x60; — imza/içerik girilmiş belge silinemez. &#x60;DOCUMENT_HAS_DECISIONS&#x60; — bu belge için onay/red kararı verilmiş.  |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60; / &#x60;DEMAND_NOT_EDITABLE&#x60; / &#x60;SIGNING_ALREADY_STARTED&#x60; / &#x60;ENVELOPE_ALREADY_DISPATCHED&#x60;. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsdemandiddocumentsdocidpatch"></a>
# **ApiV1DemandsDemandIdDocumentsDocIdPatch**
> ApiV1DemandsDemandIdDocumentsPost201Response ApiV1DemandsDemandIdDocumentsDocIdPatch (Guid demandId, Guid docId, ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest)

Belge metadata güncelle

Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (`PUT .../assignments`). 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsDemandIdDocumentsDocIdPatchExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var demandId = "demandId_example";  // Guid | 
            var docId = "docId_example";  // Guid | 
            var apiV1DemandsDemandIdDocumentsDocIdPatchRequest = new ApiV1DemandsDemandIdDocumentsDocIdPatchRequest(); // ApiV1DemandsDemandIdDocumentsDocIdPatchRequest | 

            try
            {
                // Belge metadata güncelle
                ApiV1DemandsDemandIdDocumentsPost201Response result = apiInstance.ApiV1DemandsDemandIdDocumentsDocIdPatch(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdPatchRequest);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDocumentsDocIdPatch: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Belge metadata güncelle
    ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> response = apiInstance.ApiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdPatchRequest);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **demandId** | **Guid** |  |  |
| **docId** | **Guid** |  |  |
| **apiV1DemandsDemandIdDocumentsDocIdPatchRequest** | [**ApiV1DemandsDemandIdDocumentsDocIdPatchRequest**](ApiV1DemandsDemandIdDocumentsDocIdPatchRequest.md) |  |  |

### Return type

[**ApiV1DemandsDemandIdDocumentsPost201Response**](ApiV1DemandsDemandIdDocumentsPost201Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Güncellendi |  -  |
| **400** | &#x60;VALIDATION_FAIL&#x60; (başlık boş) · &#x60;INVALID_DOC_KIND&#x60; · &#x60;CONSENT_CANNOT_BE_REQUIRED&#x60; / &#x60;PREINFO_MUST_BE_REQUIRED&#x60; — yamadan SONRA etkin &#x60;doc_kind&#x60;+&#x60;is_required&#x60; çiftine göre kontrol edilir (tek alanı yamalayıp kuralı atlatmak mümkün değildir).  |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60; / &#x60;DEMAND_NOT_EDITABLE&#x60; / &#x60;SIGNING_ALREADY_STARTED&#x60;. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsdemandiddocumentsget"></a>
# **ApiV1DemandsDemandIdDocumentsGet**
> ApiV1DemandsDemandIdDocumentsGet200Response ApiV1DemandsDemandIdDocumentsGet (Guid demandId, string? view = null)

Zarf belge listesi

Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm `/documents*` + `/documents/{docId}*` + `/documents/{docId}/assignments` uçları `ENVELOPE_DECISION_ENFORCE` bayrağı **arkasındadır**; bayrak kapalıyken **409 `ENVELOPE_MULTI_DOC_DISABLED`** döner. Bu, dashboard'un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca `POST /demands/{demandId}/dispatch` çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. `?view=wizard` verilirse tam şekil döner (`EnvelopeDocument` şemasındaki alan notlarına bakın). 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsDemandIdDocumentsGetExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var demandId = "demandId_example";  // Guid | 
            var view = "wizard";  // string? | `wizard` → tam DTO (`assigned_party_ids` + `decision_count` dahil). (optional) 

            try
            {
                // Zarf belge listesi
                ApiV1DemandsDemandIdDocumentsGet200Response result = apiInstance.ApiV1DemandsDemandIdDocumentsGet(demandId, view);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDocumentsGet: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsDemandIdDocumentsGetWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Zarf belge listesi
    ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> response = apiInstance.ApiV1DemandsDemandIdDocumentsGetWithHttpInfo(demandId, view);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDocumentsGetWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **demandId** | **Guid** |  |  |
| **view** | **string?** | &#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). | [optional]  |

### Return type

[**ApiV1DemandsDemandIdDocumentsGet200Response**](ApiV1DemandsDemandIdDocumentsGet200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Belge listesi |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60; — çok-belgeli zarf yüzeyi bu hesap için henüz kapalı. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsdemandiddocumentsorderput"></a>
# **ApiV1DemandsDemandIdDocumentsOrderPut**
> ApiV1DemandsDemandIdDocumentsGet200Response ApiV1DemandsDemandIdDocumentsOrderPut (Guid demandId, ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest)

Zarftaki belgelerin sırasını değiştir

Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. `document_ids` zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 `ORDER_SET_MISMATCH`** döner. 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsDemandIdDocumentsOrderPutExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var demandId = "demandId_example";  // Guid | 
            var apiV1DemandsDemandIdDocumentsOrderPutRequest = new ApiV1DemandsDemandIdDocumentsOrderPutRequest(); // ApiV1DemandsDemandIdDocumentsOrderPutRequest | 

            try
            {
                // Zarftaki belgelerin sırasını değiştir
                ApiV1DemandsDemandIdDocumentsGet200Response result = apiInstance.ApiV1DemandsDemandIdDocumentsOrderPut(demandId, apiV1DemandsDemandIdDocumentsOrderPutRequest);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDocumentsOrderPut: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Zarftaki belgelerin sırasını değiştir
    ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> response = apiInstance.ApiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo(demandId, apiV1DemandsDemandIdDocumentsOrderPutRequest);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **demandId** | **Guid** |  |  |
| **apiV1DemandsDemandIdDocumentsOrderPutRequest** | [**ApiV1DemandsDemandIdDocumentsOrderPutRequest**](ApiV1DemandsDemandIdDocumentsOrderPutRequest.md) |  |  |

### Return type

[**ApiV1DemandsDemandIdDocumentsGet200Response**](ApiV1DemandsDemandIdDocumentsGet200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Yeni sıra uygulandı |  -  |
| **400** | &#x60;ORDER_SET_MISMATCH&#x60; — gönderilen id kümesi zarftaki belgelerle eşleşmiyor. |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60; / &#x60;DEMAND_NOT_EDITABLE&#x60; / &#x60;SIGNING_ALREADY_STARTED&#x60; / &#x60;ENVELOPE_ALREADY_DISPATCHED&#x60;. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsdemandiddocumentspost"></a>
# **ApiV1DemandsDemandIdDocumentsPost**
> ApiV1DemandsDemandIdDocumentsPost201Response ApiV1DemandsDemandIdDocumentsPost (Guid demandId, ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest)

Zarfa metadata-only belge ekle

Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: `POST /demands/{demandId}/documents/upload`.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`'te yapılır. 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsDemandIdDocumentsPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var demandId = "demandId_example";  // Guid | 
            var apiV1DemandsDemandIdDocumentsPostRequest = new ApiV1DemandsDemandIdDocumentsPostRequest(); // ApiV1DemandsDemandIdDocumentsPostRequest | 

            try
            {
                // Zarfa metadata-only belge ekle
                ApiV1DemandsDemandIdDocumentsPost201Response result = apiInstance.ApiV1DemandsDemandIdDocumentsPost(demandId, apiV1DemandsDemandIdDocumentsPostRequest);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDocumentsPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsDemandIdDocumentsPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Zarfa metadata-only belge ekle
    ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> response = apiInstance.ApiV1DemandsDemandIdDocumentsPostWithHttpInfo(demandId, apiV1DemandsDemandIdDocumentsPostRequest);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDocumentsPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **demandId** | **Guid** |  |  |
| **apiV1DemandsDemandIdDocumentsPostRequest** | [**ApiV1DemandsDemandIdDocumentsPostRequest**](ApiV1DemandsDemandIdDocumentsPostRequest.md) |  |  |

### Return type

[**ApiV1DemandsDemandIdDocumentsPost201Response**](ApiV1DemandsDemandIdDocumentsPost201Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Belge oluşturuldu |  -  |
| **400** | &#x60;VALIDATION_FAIL&#x60; — başlık boş. &#x60;INVALID_DOC_KIND&#x60; — geçersiz &#x60;doc_kind&#x60;. &#x60;CONSENT_CANNOT_BE_REQUIRED&#x60; / &#x60;PREINFO_MUST_BE_REQUIRED&#x60; — yukarı bakın. &#x60;DOCUMENT_LIMIT_EXCEEDED&#x60; — zarf başına en fazla 20 belge. &#x60;QES_NOT_SUPPORTED_MULTI_DOCUMENT&#x60; — NES/Mobil İmza açık (&#x60;enable_qes&#x60;) bir sözleşmede ikinci belge eklenemez.  |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60; — bayrak kapalı. &#x60;DEMAND_NOT_EDITABLE&#x60; — sözleşme düzenlemeye açık değil (iptal/ süresi geçmiş/bulunamadı). &#x60;SIGNING_ALREADY_STARTED&#x60; — imza süreci başlamış zarfın belge listesi değiştirilemez (biri görüntülemiş/imzalamış/karar vermiş). &#x60;ENVELOPE_ALREADY_DISPATCHED&#x60;: sözleşme gönderilmiş (yayına alınmış veya en az bir tarafa davet gitmiş); gönderilmiş bir zarfa belge eklenemez.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsdemandiddocumentsuploadpost"></a>
# **ApiV1DemandsDemandIdDocumentsUploadPost**
> ApiV1DemandsDemandIdDocumentsPost201Response ApiV1DemandsDemandIdDocumentsUploadPost (Guid demandId, FileParameter file, string idempotencyKey, string title, string? docKind = null, string? isRequired = null)

Zarfa dosya yükle (belge başına tek dosya)

Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  `idempotency_key` **zorunludur** (gövde alanı; `Idempotency-Key` HEADER'ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı `(demandId, idempotency_key)` çifti ile tekrar çağrı **409 `IDEMPOTENT_REPLAY`** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO'su döner.  Belge uçları kredi düşmez; tahsilat `POST /dispatch`'te yapılır. 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsDemandIdDocumentsUploadPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var demandId = "demandId_example";  // Guid | 
            var file = new System.IO.MemoryStream(System.IO.File.ReadAllBytes("/path/to/file.txt"));  // FileParameter | PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.
            var idempotencyKey = "idempotencyKey_example";  // string | Zorunlu tekrar-koruma anahtarı.
            var title = "title_example";  // string | 
            var docKind = "CONTRACT";  // string? |  (optional)  (default to OTHER)
            var isRequired = "true";  // string? | Multipart alanı — string olarak gönderilir. (optional)  (default to true)

            try
            {
                // Zarfa dosya yükle (belge başına tek dosya)
                ApiV1DemandsDemandIdDocumentsPost201Response result = apiInstance.ApiV1DemandsDemandIdDocumentsUploadPost(demandId, file, idempotencyKey, title, docKind, isRequired);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDocumentsUploadPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Zarfa dosya yükle (belge başına tek dosya)
    ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> response = apiInstance.ApiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo(demandId, file, idempotencyKey, title, docKind, isRequired);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **demandId** | **Guid** |  |  |
| **file** | **FileParameter****FileParameter** | PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya. |  |
| **idempotencyKey** | **string** | Zorunlu tekrar-koruma anahtarı. |  |
| **title** | **string** |  |  |
| **docKind** | **string?** |  | [optional] [default to OTHER] |
| **isRequired** | **string?** | Multipart alanı — string olarak gönderilir. | [optional] [default to true] |

### Return type

[**ApiV1DemandsDemandIdDocumentsPost201Response**](ApiV1DemandsDemandIdDocumentsPost201Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Belge yüklendi |  -  |
| **400** | &#x60;VALIDATION_FAIL&#x60; (dosya/idempotency_key/başlık eksik) veya &#x60;INVALID_DOC_KIND&#x60;. |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60; — bayrak kapalı. &#x60;IDEMPOTENT_REPLAY&#x60; — aynı &#x60;idempotency_key&#x60; daha önce kullanıldı, yeni belge yaratılmaz (gövdede mevcut belgenin &#x60;document&#x60;&#39;ı döner). &#x60;DEMAND_NOT_EDITABLE&#x60; / &#x60;SIGNING_ALREADY_STARTED&#x60; / &#x60;ENVELOPE_ALREADY_DISPATCHED&#x60;: bkz. yukarıdaki uç.  |  -  |
| **413** | &#x60;FILE_TOO_LARGE&#x60; — 20 MB sınırı. |  -  |
| **415** | Desteklenmeyen dosya türü. |  -  |
| **422** | &#x60;IMAGE_DECODE_FAILED&#x60; — görsel çözümlenemedi. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsget"></a>
# **ApiV1DemandsGet**
> ApiV1DemandsGet200Response ApiV1DemandsGet (string? status = null, string? q = null, DateOnly? from = null, DateOnly? to = null, Guid? templateId = null, int? page = null, int? limit = null, string? sort = null)

Sözleşme listesi (counts-only, PII'siz)

Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (`parties_total`, `parties_signed`). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için `GET /demands/{id}`. 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsGetExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var status = "DRAFT";  // string? |  (optional) 
            var q = "q_example";  // string? | Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez. (optional) 
            var from = DateOnly.Parse("2013-10-20");  // DateOnly? |  (optional) 
            var to = DateOnly.Parse("2013-10-20");  // DateOnly? |  (optional) 
            var templateId = "templateId_example";  // Guid? |  (optional) 
            var page = 1;  // int? |  (optional)  (default to 1)
            var limit = 20;  // int? | Sayfa boyutu (page_size ile aynı) (optional)  (default to 20)
            var sort = "sort_example";  // string? | alan:yön (ör. createdAt:desc) (optional) 

            try
            {
                // Sözleşme listesi (counts-only, PII'siz)
                ApiV1DemandsGet200Response result = apiInstance.ApiV1DemandsGet(status, q, from, to, templateId, page, limit, sort);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsGet: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsGetWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Sözleşme listesi (counts-only, PII'siz)
    ApiResponse<ApiV1DemandsGet200Response> response = apiInstance.ApiV1DemandsGetWithHttpInfo(status, q, from, to, templateId, page, limit, sort);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsGetWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **status** | **string?** |  | [optional]  |
| **q** | **string?** | Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez. | [optional]  |
| **from** | **DateOnly?** |  | [optional]  |
| **to** | **DateOnly?** |  | [optional]  |
| **templateId** | **Guid?** |  | [optional]  |
| **page** | **int?** |  | [optional] [default to 1] |
| **limit** | **int?** | Sayfa boyutu (page_size ile aynı) | [optional] [default to 20] |
| **sort** | **string?** | alan:yön (ör. createdAt:desc) | [optional]  |

### Return type

[**ApiV1DemandsGet200Response**](ApiV1DemandsGet200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Başarılı |  -  |
| **401** | API key geçersiz veya eksik |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsidbelgedocumentidpdfget"></a>
# **ApiV1DemandsIdBelgeDocumentIdPdfGet**
> FileParameter ApiV1DemandsIdBelgeDocumentIdPdfGet (Guid id, Guid documentId)

Belge-özgü imzalı PDF (çok-belgeli zarf)

Çok-belgeli zarfta TEK bir belgenin imzalı PDF'ini indirir. Zarf-geneli `/demands/{id}/pdf` ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND'lenir (başka zarfın belgesi istenirse 404). 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsIdBelgeDocumentIdPdfGetExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | 
            var documentId = "documentId_example";  // Guid | Zarftaki belgenin kimliği.

            try
            {
                // Belge-özgü imzalı PDF (çok-belgeli zarf)
                FileParameter result = apiInstance.ApiV1DemandsIdBelgeDocumentIdPdfGet(id, documentId);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdBelgeDocumentIdPdfGet: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Belge-özgü imzalı PDF (çok-belgeli zarf)
    ApiResponse<FileParameter> response = apiInstance.ApiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo(id, documentId);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** |  |  |
| **documentId** | **Guid** | Zarftaki belgenin kimliği. |  |

### Return type

[**FileParameter**](FileParameter.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/pdf, application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Belgenin imzalı PDF&#39;i |  -  |
| **404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsidcancelpost"></a>
# **ApiV1DemandsIdCancelPost**
> ApiV1DemandsIdCancelPost200Response ApiV1DemandsIdCancelPost (Guid id, ApiV1DemandsIdCancelPostRequest? apiV1DemandsIdCancelPostRequest = null)

Sözleşme iptal (void)

Bekleyen bir sözleşmeyi iptal eder (status=CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsIdCancelPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | 
            var apiV1DemandsIdCancelPostRequest = new ApiV1DemandsIdCancelPostRequest?(); // ApiV1DemandsIdCancelPostRequest? |  (optional) 

            try
            {
                // Sözleşme iptal (void)
                ApiV1DemandsIdCancelPost200Response result = apiInstance.ApiV1DemandsIdCancelPost(id, apiV1DemandsIdCancelPostRequest);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdCancelPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsIdCancelPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Sözleşme iptal (void)
    ApiResponse<ApiV1DemandsIdCancelPost200Response> response = apiInstance.ApiV1DemandsIdCancelPostWithHttpInfo(id, apiV1DemandsIdCancelPostRequest);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdCancelPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** |  |  |
| **apiV1DemandsIdCancelPostRequest** | [**ApiV1DemandsIdCancelPostRequest?**](ApiV1DemandsIdCancelPostRequest?.md) |  | [optional]  |

### Return type

[**ApiV1DemandsIdCancelPost200Response**](ApiV1DemandsIdCancelPost200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | İptal edildi |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | Tamamlanmış/iptal edilmiş sözleşme |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsidcertificateget"></a>
# **ApiV1DemandsIdCertificateGet**
> FileParameter ApiV1DemandsIdCertificateGet (Guid id, string? lang = null)

Tamamlanma sertifikası (PAdES B-T)

Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsIdCertificateGetExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | 
            var lang = "lang_example";  // string? | tr | en (optional) 

            try
            {
                // Tamamlanma sertifikası (PAdES B-T)
                FileParameter result = apiInstance.ApiV1DemandsIdCertificateGet(id, lang);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdCertificateGet: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsIdCertificateGetWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Tamamlanma sertifikası (PAdES B-T)
    ApiResponse<FileParameter> response = apiInstance.ApiV1DemandsIdCertificateGetWithHttpInfo(id, lang);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdCertificateGetWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** |  |  |
| **lang** | **string?** | tr | en | [optional]  |

### Return type

[**FileParameter**](FileParameter.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/pdf, application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Sertifika PDF |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | Sözleşme henüz tamamlanmadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsiddelete"></a>
# **ApiV1DemandsIdDelete**
> ApiV1TemplatesIdDelete200Response ApiV1DemandsIdDelete (Guid id)

Sözleşme sil (yalnızca tamamlanmamış)

Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API'den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 `DEMAND_COMPLETED`. 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsIdDeleteExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | 

            try
            {
                // Sözleşme sil (yalnızca tamamlanmamış)
                ApiV1TemplatesIdDelete200Response result = apiInstance.ApiV1DemandsIdDelete(id);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdDelete: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsIdDeleteWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Sözleşme sil (yalnızca tamamlanmamış)
    ApiResponse<ApiV1TemplatesIdDelete200Response> response = apiInstance.ApiV1DemandsIdDeleteWithHttpInfo(id);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdDeleteWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** |  |  |

### Return type

[**ApiV1TemplatesIdDelete200Response**](ApiV1TemplatesIdDelete200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Silindi |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | Tamamlanmış sözleşme silinemez |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsidembedsessionpost"></a>
# **ApiV1DemandsIdEmbedSessionPost**
> ApiV1DemandsIdEmbedSessionPost200Response ApiV1DemandsIdEmbedSessionPost (Guid id, ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest)

Gömülü imza oturumu başlat (embed token mint)

Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token'ı üretir. Dönen `embed_url` bir `<iframe>` içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri) yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış nitelikli elektronik imza (QES) üretmez — \"güvenli\" veya \"nitelikli\" sınıf için ayrı QES akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: `expires_at` alanında belirtilen sürede geçersiz olur. - `embed_allowed_origins` kısıtı: API anahtarına tanımlanmış   izin verilen origin'ler dışından `<iframe>` açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** `X-Workspace-Id` header'ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace'in sözleşmesi için 404 döner (IDOR koruması). 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsIdEmbedSessionPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | Sözleşme (demand) ID
            var apiV1DemandsIdEmbedSessionPostRequest = new ApiV1DemandsIdEmbedSessionPostRequest(); // ApiV1DemandsIdEmbedSessionPostRequest | 

            try
            {
                // Gömülü imza oturumu başlat (embed token mint)
                ApiV1DemandsIdEmbedSessionPost200Response result = apiInstance.ApiV1DemandsIdEmbedSessionPost(id, apiV1DemandsIdEmbedSessionPostRequest);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdEmbedSessionPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsIdEmbedSessionPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Gömülü imza oturumu başlat (embed token mint)
    ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response> response = apiInstance.ApiV1DemandsIdEmbedSessionPostWithHttpInfo(id, apiV1DemandsIdEmbedSessionPostRequest);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdEmbedSessionPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** | Sözleşme (demand) ID |  |
| **apiV1DemandsIdEmbedSessionPostRequest** | [**ApiV1DemandsIdEmbedSessionPostRequest**](ApiV1DemandsIdEmbedSessionPostRequest.md) |  |  |

### Return type

[**ApiV1DemandsIdEmbedSessionPost200Response**](ApiV1DemandsIdEmbedSessionPost200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Token üretildi |  -  |
| **400** | &#x60;party_id&#x60; eksik veya geçersiz format.  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **403** | **INSUFFICIENT_SCOPE** — API key&#39;in &#x60;demands&#x60; scope&#39;u yok veya gömülü imza özelliği bu API anahtarı için devre dışı.  |  -  |
| **404** | Sözleşme veya taraf bulunamadı. İki durum ayrıştırılmaz (IDOR koruması): - &#x60;Sözleşme bulunamadı&#x60; — demand bu workspace&#39;te yok - &#x60;Taraf bulunamadı&#x60; — party_id bu demand&#39;e ait değil  |  -  |
| **409** | Token üretilemez. Olası nedenler: - &#x60;Bu taraf zaten imzaladı&#x60; — taraf imzalamış - &#x60;Bu taraf imzayı reddetti&#x60; — taraf reddetmiş - &#x60;embed_allowed_origins tanımlı değil&#x60; — API anahtarında izin verilen   origin listesi boş; dashboard&#39;dan API anahtarı düzenleyerek ekleyin.  |  -  |
| **429** | Rate limit aşıldı (per API key + demand + party kombinasyonu) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsidget"></a>
# **ApiV1DemandsIdGet**
> ApiV1DemandsIdGet200Response ApiV1DemandsIdGet (Guid id)

Sözleşme durumu + imza ilerlemesi

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsIdGetExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | 

            try
            {
                // Sözleşme durumu + imza ilerlemesi
                ApiV1DemandsIdGet200Response result = apiInstance.ApiV1DemandsIdGet(id);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdGet: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsIdGetWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Sözleşme durumu + imza ilerlemesi
    ApiResponse<ApiV1DemandsIdGet200Response> response = apiInstance.ApiV1DemandsIdGetWithHttpInfo(id);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdGetWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** |  |  |

### Return type

[**ApiV1DemandsIdGet200Response**](ApiV1DemandsIdGet200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Başarılı |  -  |
| **404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsiditemspost"></a>
# **ApiV1DemandsIdItemsPost**
> UpsertItemsResponse ApiV1DemandsIdItemsPost (Guid id, UpsertItemsRequest upsertItemsRequest)

Sözleşmeye alan yerleştir (replace)

Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: `POST /api/v1/demands/upload` ile demand yarat (`dispatch_notifications=false` ile auto-dispatch'i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da `POST /api/v1/demands/{id}/reminders` ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - `page_ids` **omitted** → demand'in TÜM mevcut item'ları silinir,   body'dekiler yaratılır (full replace). - `page_ids: [N, M, ...]` → sadece bu sayfaların item'ları silinir,   diğer sayfalardaki item'lar korunur. Body'deki `items[].page_id`   değerleri `page_ids` listesinde olmalıdır.  ### Item type'ları  | `item_type` | `party_id` zorunlu? | `config` örneği | |- -- -- -- -- -- --|- -- -- -- -- -- -- -- -- -- --|- -- -- -- -- -- -- -- --| | `signature` | ✅ | (yok) | | `text` | ❌ | `{ default_content }` | | `dynamic_text` | ✅ | `{ defaultSource: \"{{signer.full_name}}\" }` | | `cells` | ✅ | `{ cellCount: 11, defaultSource: \"{{signer.government_id}}\" }` | | `date` | ✅ | `{ defaultSource, defaultValue }` | | `dropdown` | ✅ | `{ options: [{label,value}], defaultValue }` | | `checkbox` | ✅ | `{ checkedByDefault: false }` | | `radio` | ✅ | `{ options: [{label,value}], defaultValue }` | | `stamp` | ❌ | `{ stampData: \"data:image/png;base64,...\" }` |  ### Sistem değişkenleri (dynamic_text/cells/date `config.defaultSource`)  `{{signer.first_name}}`, `{{signer.last_name}}`, `{{signer.full_name}}`, `{{signer.email}}`, `{{signer.phone}}`, `{{signer.government_id}}`, `{{signer.birth_date}}`, `{{signer.sign_date}}`, `{{contract.title}}`, `{{sender.full_name}}`, `{{current.date}}`, `{{current.datetime}}`.  ### Workspace izolasyonu  X-API-Key middleware demand'i workspace'e göre filtreler; başka workspace'in demand'ine item ekleyemezsiniz (404 döner).  ### Status kontrolü  Sadece `PENDING` demand edit edilebilir. `COMPLETED`, `EXPIRED`, `REJECTED` için 403.  ### Örnek  ```bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \"X-API-Key: imz_...\" \\   -H \"Content-Type: application/json\" \\   -d '{     \"items\": [       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"signature\",         \"position_x\": 0.5, \"position_y\": 0.85,         \"width\": 0.2, \"height\": 0.05,         \"is_required\": true       },       {         \"page_id\": 12345,         \"party_id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",         \"item_type\": \"cells\",         \"position_x\": 0.1, \"position_y\": 0.5,         \"width\": 0.4, \"height\": 0.04,         \"slug\": \"tc\",         \"config\": { \"cellCount\": 11, \"defaultSource\": \"{{signer.government_id}}\" }       }     ]   }' ``` 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsIdItemsPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | 
            var upsertItemsRequest = new UpsertItemsRequest(); // UpsertItemsRequest | 

            try
            {
                // Sözleşmeye alan yerleştir (replace)
                UpsertItemsResponse result = apiInstance.ApiV1DemandsIdItemsPost(id, upsertItemsRequest);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdItemsPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsIdItemsPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Sözleşmeye alan yerleştir (replace)
    ApiResponse<UpsertItemsResponse> response = apiInstance.ApiV1DemandsIdItemsPostWithHttpInfo(id, upsertItemsRequest);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdItemsPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** |  |  |
| **upsertItemsRequest** | [**UpsertItemsRequest**](UpsertItemsRequest.md) |  |  |

### Return type

[**UpsertItemsResponse**](UpsertItemsResponse.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Alanlar yerleştirildi |  -  |
| **400** | Validation hatası. Olası &#x60;error&#x60; değerleri: - &#x60;INVALID_ITEMS_BODY&#x60; — items array değil - &#x60;VALIDATION_ERROR&#x60; — position bounds, slug regex, party-required - &#x60;INVALID_PAGE_ID&#x60; — page_id demand&#39;e ait değil veya page_ids&#39;te yok - &#x60;INVALID_PARTY_ID&#x60; — party_id demand&#39;e ait değil - &#x60;PAGE_ID_REQUIRED&#x60;: bir öğede tam sayı &#x60;page_id&#x60; yok - &#x60;INVALID_ITEM_TYPE&#x60;: desteklenmeyen &#x60;item_type&#x60; (mesaj izinli türleri listeler)  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **403** | &#x60;DEMAND_NOT_EDITABLE&#x60; — demand status ≠ &#x60;PENDING&#x60; (COMPLETED, EXPIRED, REJECTED edit edilemez).  |  -  |
| **404** | &#x60;DEMAND_NOT_FOUND&#x60; — demand bu workspace&#39;te yok (cross-workspace IDOR koruması).  |  -  |
| **409** | &#x60;DUPLICATE_SIGNATURE_FIELD&#x60; — aynı &#x60;(page_id, party_id, position_x, position_y)&#x60; tuple&#39;ında ikinci &#x60;signature&#x60; alanı yaratıldı. DB-level partial unique constraint engelledi. Pozisyonu değiştirip tekrar deneyin.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsidpartiespartyidresendpost"></a>
# **ApiV1DemandsIdPartiesPartyIdResendPost**
> ApiV1DemandsIdPartiesPartyIdResendPost200Response ApiV1DemandsIdPartiesPartyIdResendPost (Guid id, Guid partyId)

Tekil tarafa imza davetini tekrar gönder

Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsIdPartiesPartyIdResendPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | 
            var partyId = "partyId_example";  // Guid | 

            try
            {
                // Tekil tarafa imza davetini tekrar gönder
                ApiV1DemandsIdPartiesPartyIdResendPost200Response result = apiInstance.ApiV1DemandsIdPartiesPartyIdResendPost(id, partyId);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdPartiesPartyIdResendPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Tekil tarafa imza davetini tekrar gönder
    ApiResponse<ApiV1DemandsIdPartiesPartyIdResendPost200Response> response = apiInstance.ApiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo(id, partyId);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** |  |  |
| **partyId** | **Guid** |  |  |

### Return type

[**ApiV1DemandsIdPartiesPartyIdResendPost200Response**](ApiV1DemandsIdPartiesPartyIdResendPost200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Gönderildi |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | Tekrar gönderim yapılamaz. &#x60;error&#x60; insan-okur mesajı taşır. - İmzalamış, reddetmiş veya sıralı imzada sırası gelmemiş taraf   (&#x60;code&#x60; alanı yok) - &#x60;DEMAND_NOT_DISPATCHED&#x60;: sözleşme henüz imzaya gönderilmedi (taslak) - &#x60;DEMAND_NOT_DISPATCHABLE&#x60;: sözleşme tamamlanmış veya iptal edilmiş - &#x60;DEMAND_EXPIRED&#x60;: sözleşmenin imza süresi geçmiş - &#x60;ENVELOPE_NOT_DISPATCHED&#x60;: çok belgeli zarf henüz gönderilmedi.   Önce &#x60;POST /demands/{id}/dispatch&#x60; çağırın; tek belgeli ve   gönderilmiş sözleşmelerde bu kod hiç dönmez.  |  -  |
| **429** | İki ayrı sınır vardır.  &#x60;TOO_MANY_REQUESTS&#x60;: API anahtarı başına saatte 300 istek (dispatch ucuyla ortak sınır). Gövdede &#x60;retry_after_seconds&#x60; döner.  &#x60;RECIPIENT_RESEND_LIMIT&#x60;: aynı alıcıya saatte en fazla 3, günde en fazla 10 davet tekrarı gönderilebilir.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsidpdfget"></a>
# **ApiV1DemandsIdPdfGet**
> FileParameter ApiV1DemandsIdPdfGet (Guid id)

İmzalı sözleşme PDF'i (auth'lu indirme)

Tamamlanmış sözleşmenin imzalı PDF'ini indirir. Public `/sonuc/{id}/pdf`'in aksine API key ownership'i zorunludur. 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsIdPdfGetExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | 

            try
            {
                // İmzalı sözleşme PDF'i (auth'lu indirme)
                FileParameter result = apiInstance.ApiV1DemandsIdPdfGet(id);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdPdfGet: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsIdPdfGetWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // İmzalı sözleşme PDF'i (auth'lu indirme)
    ApiResponse<FileParameter> response = apiInstance.ApiV1DemandsIdPdfGetWithHttpInfo(id);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdPdfGetWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** |  |  |

### Return type

[**FileParameter**](FileParameter.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/pdf, application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | PDF |  -  |
| **404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsidtimelineget"></a>
# **ApiV1DemandsIdTimelineGet**
> ApiV1DemandsIdTimelineGet200Response ApiV1DemandsIdTimelineGet (Guid id)

İmza denetim izi (maskeli)

Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP `ip_masked` (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez. 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsIdTimelineGetExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | 

            try
            {
                // İmza denetim izi (maskeli)
                ApiV1DemandsIdTimelineGet200Response result = apiInstance.ApiV1DemandsIdTimelineGet(id);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdTimelineGet: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsIdTimelineGetWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // İmza denetim izi (maskeli)
    ApiResponse<ApiV1DemandsIdTimelineGet200Response> response = apiInstance.ApiV1DemandsIdTimelineGetWithHttpInfo(id);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsIdTimelineGetWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** |  |  |

### Return type

[**ApiV1DemandsIdTimelineGet200Response**](ApiV1DemandsIdTimelineGet200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Başarılı |  -  |
| **404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandspost"></a>
# **ApiV1DemandsPost**
> ApiV1DemandsPost201Response ApiV1DemandsPost (CreateDemandRequest createDemandRequest, string? idempotencyKey = null)

Sözleşme oluştur (şablondan)

Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field'ları `variables` payload'undan doldurur ve imzalama URL'lerini döner.  **Variable resolution:** - Item'ın `template_party_id` non-null → `party_mapping[i].variables`'ta   o slug var ise oradan uygulanır - Yoksa root `variables`'tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   `editable: true` ise)  **Validation:** - `party_mapping[i].variables` ve root `variables` object olmalı - Variable value'ları `string | number | boolean | null` olmalı   (object/array reject) - `template_party_id` party_mapping içinde unique olmalı  **Kredi (çok belgeli zarf):** Şablon çok-belgeli bir zarf tanımlıyorsa (her belge farklı imzacı(lar)a atanabilir), imzacı başına kredi o imzacıya ATANMIŞ belge sayısına göre hesaplanır ve belge sayısına göre azalan birim fiyatlı bir indirim uygulanır: 1 belge %0, 2 belge %30, 3 ve üzeri %50 (indirim imzacı başına ayrı hesaplanır ve yukarı yuvarlanır). AB nitelikli zaman damgası (`eidas_timestamp`) seçiliyse aynı indirim zarftaki farklı belge sayısına göre toplam üzerinden uygulanır. Tek belgeli şablonlarda tutar eski formülle bayt-aynıdır.  **Atamasız imzacı:** `dispatch_notifications` `false` gönderilmediği sürece, eşlenen bir taraf şablonun hiçbir belgesine atanmamışsa sözleşme HİÇ oluşturulmaz (409 `PARTY_WITHOUT_DOCUMENTS`). Bu kontrolü atlamak için `dispatch_notifications: false` gönderip belge atamalarını `PUT .../documents/{docId}/assignments` ile düzelttikten sonra `POST .../dispatch` ile gönderin (o uç aynı kapıyı yeniden uygular). 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var createDemandRequest = new CreateDemandRequest(); // CreateDemandRequest | 
            var idempotencyKey = "idempotencyKey_example";  // string? | Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional) 

            try
            {
                // Sözleşme oluştur (şablondan)
                ApiV1DemandsPost201Response result = apiInstance.ApiV1DemandsPost(createDemandRequest, idempotencyKey);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Sözleşme oluştur (şablondan)
    ApiResponse<ApiV1DemandsPost201Response> response = apiInstance.ApiV1DemandsPostWithHttpInfo(createDemandRequest, idempotencyKey);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **createDemandRequest** | [**CreateDemandRequest**](CreateDemandRequest.md) |  |  |
| **idempotencyKey** | **string?** | Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  | [optional]  |

### Return type

[**ApiV1DemandsPost201Response**](ApiV1DemandsPost201Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Sözleşme oluşturuldu |  -  |
| **400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate&#39;i bulundu: &lt;id&gt;\&quot;  &#x60;INVALID_EXPIRY_DATE&#x60;: &#x60;expiry_date&#x60; çözümlenemiyor veya takvimde olmayan bir gün (ör. &#x60;2026-02-30&#x60;). Bu hatada &#x60;error&#x60; insan-okur mesajı, &#x60;code&#x60; alanı makinece okunur kodu taşır.  &#x60;INVALID_DOCUMENT_SELECTION&#x60;: belge seçimi hatası; &#x60;details.reason&#x60; şu değerlerden biridir: &#x60;shape&#x60; (biçim: nesne değil, bilinmeyen alan, kimlik metin değil, 20&#39;den fazla kimlik), &#x60;unknown_document&#x60; (kimlik bu şablonun belgesi değil), &#x60;conflict&#x60; (aynı kimlik iki listede), &#x60;empty&#x60; (seçim sonucunda belge kalmadı). Biçim hataları kredi kontrolünden önce, şablona bağlı hatalar şablon erişim kontrolünden sonra ve kredi kontrolünden önce döner.  &#x60;SIGNATURE_VARIANTS_PHONE_ONLY&#x60;: &#x60;allowed_signature_variants&#x60; yalnız &#x60;phone&#x60; içeriyor. Telefonda çizim tek başına seçilemez; listede en az bir &#x60;phone&#x60; olmayan yöntem bırakın.  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **402** | Kredi mutabakatı başarısız: &#x60;INSUFFICIENT_CREDITS&#x60; (bakiye yetersiz) veya &#x60;MEMBER_LIMIT_EXCEEDED&#x60; (organizasyon üyesinin aylık kredi limiti aşıldı, havuzda bakiye olsa bile).  Sözleşme bu noktada ZATEN oluşturulmuştur ve davet gitmemiştir: gövdede &#x60;data.id&#x60; ile aynı sözleşmenin kimliği ve &#x60;data.status: \&quot;DRAFT_UNDISPATCHED\&quot;&#x60; döner. Kredi yükleyip aynı sözleşmeyi &#x60;POST /demands/{id}/dispatch&#x60; ile gönderin; sözleşme silinmez.  |  -  |
| **403** | **SMS_CUSTOMIZATION_NOT_ALLOWED** — Body&#39;de &#x60;sms_content&#x60; alanı dolu gönderildi ama çağıran organizasyon PRO/ENTERPRISE planda değil veya kendi SMS sağlayıcı config&#39;i (sender_name dolu) yok. &#x60;sms_content&#x60; alanını çıkarın veya planınızı yükseltip kendi SMS sağlayıcınızı tanımlayın.  |  -  |
| **409** | Altı ayrı kod döner.  &#x60;IDEMPOTENCY_KEY_REUSED&#x60; — aynı &#x60;Idempotency-Key&#x60; daha önce FARKLI bir içerikle kullanıldı. Sessizce eski sözleşmeyi döndürmek \&quot;gönderdim sandım\&quot; kazası üretirdi; yeni sözleşme için yeni anahtar gönderin. Gövdede eski sözleşmenin &#x60;demand_id&#x60; alanı döner.  &#x60;IDEMPOTENCY_UNVERIFIABLE&#x60;: bu &#x60;Idempotency-Key&#x60; daha önce bir sözleşme üretti, ancak bu isteğin aynı içerikte olduğu doğrulanamadı. Eski sözleşme sessizce döndürülmez. Gövdedeki &#x60;demand_id&#x60; ile durumu sorgulayın; yeni bir anahtarla körlemesine tekrar denemek ikinci bir sözleşme oluşturur.  &#x60;DUPLICATE_SUSPECTED&#x60; — idempotency anahtarı GÖNDERİLMEDİ ve aynı API anahtarı son 10 dakika içinde aynı içeriği (aynı belge/şablon + aynı taraf kümesi) zaten gönderdi. Gövdede mevcut sözleşmenin &#x60;demand_id&#x60; alanı döner. Kasten tekrarlamak için &#x60;force&#x60; gönderin.  Bu kapı yalnız anahtarsız çağrılarda çalışır: &#x60;Idempotency-Key&#x60; gönderen istemci zaten tekrar-korumalıdır.  &#x60;PARTY_WITHOUT_DOCUMENTS&#x60;: eşlediğiniz bir tarafa imzalayacak belge düşmüyor. Üç biçimde görünebilir: (a) şablon düzeyinde ön-kontrol (rol hiçbir şablon belgesine atanmamış ve davet gönderilecek) sözleşmeyi HİÇ yaratmadan reddeder, gövdede &#x60;template_party_ids&#x60; (şablon rol kimlikleri) döner; (b) &#x60;documents&#x60; seçiminiz (ya da &#x60;documents&#x60; göndermediyseniz şablonun varsayılan belge kümesi) bir role seçili belge bırakmıyorsa &#x60;dispatch_notifications&#x60; değerinden bağımsız yine yaratmadan reddedilir, gövdede &#x60;template_party_ids&#x60; döner (seçimi değiştirin ya da o rolü eşlemeden çıkarın); (c) oluşturma sonrası aynı kontrol atamasız tarafı bulursa sözleşme TASLAĞA düşürülür ve gövdede &#x60;party_ids&#x60; (imzacı kimlikleri) + &#x60;data.id&#x60; / &#x60;data.status: \&quot;DRAFT_UNDISPATCHED\&quot;&#x60; döner. Hiçbir biçimde kredi düşülmez, davet gönderilmez.  &#x60;TEMPLATE_DOCUMENTS_NOT_READY&#x60;: şablonun belge yapısı henüz hazır değil; &#x60;documents&#x60; göndermeden deneyin ya da destekle iletişime geçin.  &#x60;DOCUMENT_SOURCE_UNAVAILABLE&#x60;: şablonun ilk belgesini çıkardınız ve kalan ilk belgenin kaynak dosyası yok; ilk belgeyi de gönderin ya da o belgeye dosya yükleyin.  |  -  |
| **500** | &#x60;RECONCILE_FAILED&#x60;: kredi mutabakatı beklenmedik (kredi-dışı) bir hatayla düştü. Sözleşme bu noktada ZATEN oluşturulmuştur ve TASLAĞA düşürülür; gövdede &#x60;data.id&#x60; / &#x60;data.status: \&quot;DRAFT_UNDISPATCHED\&quot;&#x60; döner. Davet gönderilmez. İstek güvenle tekrarlanabilir (mutabakat idempotenttir).  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1demandsuploadpost"></a>
# **ApiV1DemandsUploadPost**
> ApiV1DemandsUploadPost201Response ApiV1DemandsUploadPost (List<FileParameter> files, string parties, string? idempotencyKey = null, string? order = null, string? title = null, string? description = null, Guid? fieldTemplateId = null, string? force = null, string? sendInvitations = null, string? onAnchorMiss = null)

Dosya upload ile sözleşme oluştur (şablonsuz)

Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF'e birleştirilir, office formatları LibreOffice ile PDF'e çevrilir. 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1DemandsUploadPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var files = new List<FileParameter>(); // List<FileParameter> | 1 belge VEYA 1-20 görsel
            var parties = "parties_example";  // string | JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  `field_template_id` gönderildiğinde her party AYRICA `template_party_id` (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir. 
            var idempotencyKey = "idempotencyKey_example";  // string? | Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional) 
            var order = "order_example";  // string? | Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\") (optional) 
            var title = "title_example";  // string? |  (optional) 
            var description = "description_example";  // string? |  (optional) 
            var fieldTemplateId = "fieldTemplateId_example";  // Guid? | Alan Şablonu (`kind: FIELD_LAYOUT`) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - `template_id` ile **birlikte gönderilemez**   (400 `FIELD_TEMPLATE_CONFLICT`). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için `POST /api/v1/field-templates/{id}/preview-layout`.  (optional) 
            var force = "true";  // string? | Kopya kapısını bilerek geç. Yalnız `Idempotency-Key` GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  (optional) 
            var sendInvitations = "true";  // string? | `\\\"true\\\"` (veya `\\\"1\\\"`) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca `POST /api/v1/demands/{id}/parties/{partyId}/resend` çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 `INVALID_SEND_INVITATIONS`   döner (fail-closed). - **Açık değerler:** `\\\"true\\\"`, `\\\"1\\\"`, `\\\"all\\\"`, `\\\"email\\\"`, `\\\"sms\\\"`. - **Kapalı değerler:** `\\\"false\\\"`, `\\\"0\\\"`, `\\\"off\\\"`, `\\\"no\\\"`,   `\\\"hayir\\\"`, `\\\"hayır\\\"`. - Ad bilerek `dispatch_notifications` **değildir**: o   parametre `POST /api/v1/demands` ve `/demands/bulk`   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: `\\\"true\\\"`, `\\\"1\\\"` ve `\\\"all\\\"` tüm kanalları kullanır;   `\\\"email\\\"` yalnız e-posta, `\\\"sms\\\"` telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   `send_sms`/`send_email` bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (`dispatch.error = DISPATCH_TOO_MANY`), tarafları   `resend` ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği `Idempotency-Key`   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı `DUPLICATE_SUSPECTED` yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte `Idempotency-Key` gönderin;   anahtar yoksa yeniden göndermek yerine   `GET /api/v1/demands?...` ile sonucu doğrulayın.  (optional) 
            var onAnchorMiss = "block";  // string? | Çapa bulunamadığında ne yapılacağı. Yalnız `field_template_id` ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, `block` uygulanır. - `drop`, yalnız şablonun İLGİLİ TÜM alanları zaten `DROP`   ise uygulanır; aksi halde yok sayılır, `block` uygulanır   ve yanıtta `ON_ANCHOR_MISS_NOT_RELAXED` uyarısı döner. - İmza alanlarında `drop` hiçbir koşulda uygulanmaz. - `fallback` bu API'de **yoktur** (400 `INVALID_ON_ANCHOR_MISS`).  Yanıttaki `data.field_layout.on_anchor_miss` istenen değil, **uygulanan** değeri taşır.  (optional) 

            try
            {
                // Dosya upload ile sözleşme oluştur (şablonsuz)
                ApiV1DemandsUploadPost201Response result = apiInstance.ApiV1DemandsUploadPost(files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1DemandsUploadPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1DemandsUploadPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Dosya upload ile sözleşme oluştur (şablonsuz)
    ApiResponse<ApiV1DemandsUploadPost201Response> response = apiInstance.ApiV1DemandsUploadPostWithHttpInfo(files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1DemandsUploadPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **files** | **List&lt;FileParameter&gt;** | 1 belge VEYA 1-20 görsel |  |
| **parties** | **string** | JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  |  |
| **idempotencyKey** | **string?** | Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  | [optional]  |
| **order** | **string?** | Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) | [optional]  |
| **title** | **string?** |  | [optional]  |
| **description** | **string?** |  | [optional]  |
| **fieldTemplateId** | **Guid?** | Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  | [optional]  |
| **force** | **string?** | Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  | [optional]  |
| **sendInvitations** | **string?** | &#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60; ve &#x60;\\\&quot;all\\\&quot;&#x60; tüm kanalları kullanır;   &#x60;\\\&quot;email\\\&quot;&#x60; yalnız e-posta, &#x60;\\\&quot;sms\\\&quot;&#x60; telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği &#x60;Idempotency-Key&#x60;   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı &#x60;DUPLICATE_SUSPECTED&#x60; yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte &#x60;Idempotency-Key&#x60; gönderin;   anahtar yoksa yeniden göndermek yerine   &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  | [optional]  |
| **onAnchorMiss** | **string?** | Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  | [optional]  |

### Return type

[**ApiV1DemandsUploadPost201Response**](ApiV1DemandsUploadPost201Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Sözleşme oluşturuldu |  -  |
| **400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate&#39;i bulundu: &lt;id&gt;\&quot;  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **402** | Yetersiz kredi: &#x60;INSUFFICIENT_CREDITS&#x60; veya &#x60;MEMBER_LIMIT_EXCEEDED&#x60;. Sözleşme bu noktada ZATEN oluşturulmuştur ve davet gitmemiştir: gövdedeki &#x60;demand_id&#x60; ile aynı sözleşmeyi kredi yükledikten sonra &#x60;POST /demands/{id}/dispatch&#x60; ile gönderebilirsiniz; sözleşme silinmez.  |  -  |
| **403** | Organizasyona bağlı API anahtarı ile uyuşmayan &#x60;X-Workspace-Id&#x60; (&#x60;WORKSPACE_MISMATCH&#x60;). Yalnız &#x60;field_template_id&#x60; yolunda.  |  -  |
| **404** | Alan Şablonu bulunamadı (&#x60;TEMPLATE_NOT_FOUND&#x60;) |  -  |
| **409** | Üç ayrı kod döner.  &#x60;IDEMPOTENCY_KEY_REUSED&#x60; — aynı &#x60;Idempotency-Key&#x60; daha önce FARKLI bir içerikle kullanıldı. Sessizce eski sözleşmeyi döndürmek \&quot;gönderdim sandım\&quot; kazası üretirdi; yeni sözleşme için yeni anahtar gönderin. Gövdede eski sözleşmenin &#x60;demand_id&#x60; alanı döner.  &#x60;IDEMPOTENCY_UNVERIFIABLE&#x60;: bu &#x60;Idempotency-Key&#x60; daha önce bir sözleşme üretti, ancak bu isteğin aynı içerikte olduğu doğrulanamadı. Eski sözleşme sessizce döndürülmez. Gövdedeki &#x60;demand_id&#x60; ile durumu sorgulayın; yeni bir anahtarla körlemesine tekrar denemek ikinci bir sözleşme oluşturur.  &#x60;DUPLICATE_SUSPECTED&#x60; — idempotency anahtarı GÖNDERİLMEDİ ve aynı API anahtarı son 10 dakika içinde aynı içeriği (aynı belge/şablon + aynı taraf kümesi) zaten gönderdi. Gövdede mevcut sözleşmenin &#x60;demand_id&#x60; alanı döner. Kasten tekrarlamak için &#x60;force&#x60; gönderin.  Bu kapı yalnız anahtarsız çağrılarda çalışır: &#x60;Idempotency-Key&#x60; gönderen istemci zaten tekrar-korumalıdır.  |  -  |
| **413** | Dosya boyut limiti aşıldı (FILE_TOO_LARGE) |  -  |
| **415** | &#x60;field_template_id&#x60; yolunda PDF olmayan dosya (&#x60;UNSUPPORTED_FILE_TYPE&#x60;).  |  -  |
| **422** | Görsel okunamadı (&#x60;IMAGE_DECODE_FAILED&#x60;) **veya** alan yerleşimi bu belgeye uygulanamadı (&#x60;FIELD_LAYOUT_UNRESOLVED&#x60;). İkinci durumda sözleşme oluşturulmaz ve kredi düşülmez.  |  -  |
| **429** | &#x60;TOO_MANY_REQUESTS&#x60; — API anahtarı başına dakikada 30 istek sınırı aşıldı. Bu uç yükleme + PDF ayrıştırma yapar ve &#x60;send_invitations&#x60; ile gerçek SMS/e-posta tetikleyebilir; sınır kötüye kullanımın hızını kırmak içindir.  |  -  |
| **500** | &#x60;RECONCILE_FAILED&#x60;: kredi mutabakatı beklenmedik (kredi-dışı) bir hatayla düştü. Sözleşme bu noktada ZATEN oluşturulmuştur ve davet gitmemiştir; gövdedeki &#x60;demand_id&#x60; ile durumu sorgulayın. İstek güvenle tekrarlanabilir (mutabakat idempotenttir).  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

<a id="apiv1fieldtemplatesidpreviewlayoutpost"></a>
# **ApiV1FieldTemplatesIdPreviewLayoutPost**
> ApiV1FieldTemplatesIdPreviewLayoutPost200Response ApiV1FieldTemplatesIdPreviewLayoutPost (Guid id, List<FileParameter> files, string? onAnchorMiss = null)

Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener

Bir Alan Şablonunun (`kind: FIELD_LAYOUT`) yüklediğiniz PDF'e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  `POST /api/v1/demands/upload` + `field_template_id` çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API'de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (`data.resolvable: false`) — kuru koşumun cevabı \"uygulanamaz\"dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 `RATE_LIMITED`). 

### Example
```csharp
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Http;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;

namespace Example
{
    public class ApiV1FieldTemplatesIdPreviewLayoutPostExample
    {
        public static void Main()
        {
            Configuration config = new Configuration();
            config.BasePath = "https://api-prd.imzala.org";
            // Configure API key authorization: ApiKeyAuth
            config.AddApiKey("X-API-Key", "YOUR_API_KEY");
            // Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
            // config.AddApiKeyPrefix("X-API-Key", "Bearer");

            // create instances of HttpClient, HttpClientHandler to be reused later with different Api classes
            HttpClient httpClient = new HttpClient();
            HttpClientHandler httpClientHandler = new HttpClientHandler();
            var apiInstance = new DemandsApi(httpClient, config, httpClientHandler);
            var id = "id_example";  // Guid | Alan Şablonu (FIELD_LAYOUT) kimliği
            var files = new List<FileParameter>(); // List<FileParameter> | Tek PDF belge
            var onAnchorMiss = "block";  // string? | `POST /api/v1/demands/upload` ile aynı semantik (yalnız sıkılaştırır, gönderilmezse `block`).  (optional) 

            try
            {
                // Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
                ApiV1FieldTemplatesIdPreviewLayoutPost200Response result = apiInstance.ApiV1FieldTemplatesIdPreviewLayoutPost(id, files, onAnchorMiss);
                Debug.WriteLine(result);
            }
            catch (ApiException  e)
            {
                Debug.Print("Exception when calling DemandsApi.ApiV1FieldTemplatesIdPreviewLayoutPost: " + e.Message);
                Debug.Print("Status Code: " + e.ErrorCode);
                Debug.Print(e.StackTrace);
            }
        }
    }
}
```

#### Using the ApiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo variant
This returns an ApiResponse object which contains the response data, status code and headers.

```csharp
try
{
    // Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
    ApiResponse<ApiV1FieldTemplatesIdPreviewLayoutPost200Response> response = apiInstance.ApiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo(id, files, onAnchorMiss);
    Debug.Write("Status Code: " + response.StatusCode);
    Debug.Write("Response Headers: " + response.Headers);
    Debug.Write("Response Body: " + response.Data);
}
catch (ApiException e)
{
    Debug.Print("Exception when calling DemandsApi.ApiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo: " + e.Message);
    Debug.Print("Status Code: " + e.ErrorCode);
    Debug.Print(e.StackTrace);
}
```

### Parameters

| Name | Type | Description | Notes |
|------|------|-------------|-------|
| **id** | **Guid** | Alan Şablonu (FIELD_LAYOUT) kimliği |  |
| **files** | **List&lt;FileParameter&gt;** | Tek PDF belge |  |
| **onAnchorMiss** | **string?** | &#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  | [optional]  |

### Return type

[**ApiV1FieldTemplatesIdPreviewLayoutPost200Response**](ApiV1FieldTemplatesIdPreviewLayoutPost200Response.md)

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Kuru koşum sonucu |  -  |
| **400** | Dosya gönderilmedi (&#x60;FILE_REQUIRED&#x60;) veya &#x60;on_anchor_miss&#x60; geçersiz (&#x60;INVALID_ON_ANCHOR_MISS&#x60;) veya şablon bir Alan Şablonu değil (&#x60;NOT_A_FIELD_LAYOUT_TEMPLATE&#x60;).  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **403** | Yetersiz scope (&#x60;INSUFFICIENT_SCOPE&#x60;) veya organizasyona bağlı API anahtarı ile uyuşmayan &#x60;X-Workspace-Id&#x60; (&#x60;WORKSPACE_MISMATCH&#x60;).  |  -  |
| **404** | Alan Şablonu bulunamadı (&#x60;TEMPLATE_NOT_FOUND&#x60;) |  -  |
| **413** | Dosya boyut limiti aşıldı |  -  |
| **415** | PDF olmayan dosya (&#x60;UNSUPPORTED_FILE_TYPE&#x60;) |  -  |
| **422** | Belge okunamadı: parola korumalı PDF (&#x60;ENCRYPTED_PDF&#x60;) veya metin çıkarma süre bütçesi aşıldı (&#x60;TEXT_EXTRACTION_TIMEOUT&#x60;).  |  -  |
| **429** | Dakikalık önizleme sınırı aşıldı (&#x60;RATE_LIMITED&#x60;) |  -  |
| **503** | Sınırlayıcı geçici olarak kullanılamıyor (&#x60;RATE_LIMITER_UNAVAILABLE&#x60;) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

