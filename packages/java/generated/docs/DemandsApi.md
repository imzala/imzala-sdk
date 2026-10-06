# DemandsApi

All URIs are relative to *https://api-prd.imzala.org*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**apiV1DemandsBulkPost**](DemandsApi.md#apiV1DemandsBulkPost) | **POST** /api/v1/demands/bulk | Toplu sözleşme oluştur (tek şablondan N alıcı) |
| [**apiV1DemandsBulkPostWithHttpInfo**](DemandsApi.md#apiV1DemandsBulkPostWithHttpInfo) | **POST** /api/v1/demands/bulk | Toplu sözleşme oluştur (tek şablondan N alıcı) |
| [**apiV1DemandsDemandIdDispatchPost**](DemandsApi.md#apiV1DemandsDemandIdDispatchPost) | **POST** /api/v1/demands/{demandId}/dispatch | Zarfı imzaya gönder (yayınla + davet) |
| [**apiV1DemandsDemandIdDispatchPostWithHttpInfo**](DemandsApi.md#apiV1DemandsDemandIdDispatchPostWithHttpInfo) | **POST** /api/v1/demands/{demandId}/dispatch | Zarfı imzaya gönder (yayınla + davet) |
| [**apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut**](DemandsApi.md#apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut) | **PUT** /api/v1/demands/{demandId}/documents/{docId}/assignments | Belgeye imzacı ata (tam-küme replace) |
| [**apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo**](DemandsApi.md#apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo) | **PUT** /api/v1/demands/{demandId}/documents/{docId}/assignments | Belgeye imzacı ata (tam-küme replace) |
| [**apiV1DemandsDemandIdDocumentsDocIdDelete**](DemandsApi.md#apiV1DemandsDemandIdDocumentsDocIdDelete) | **DELETE** /api/v1/demands/{demandId}/documents/{docId} | Belgeyi zarftan sil |
| [**apiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo**](DemandsApi.md#apiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo) | **DELETE** /api/v1/demands/{demandId}/documents/{docId} | Belgeyi zarftan sil |
| [**apiV1DemandsDemandIdDocumentsDocIdPatch**](DemandsApi.md#apiV1DemandsDemandIdDocumentsDocIdPatch) | **PATCH** /api/v1/demands/{demandId}/documents/{docId} | Belge metadata güncelle |
| [**apiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo**](DemandsApi.md#apiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo) | **PATCH** /api/v1/demands/{demandId}/documents/{docId} | Belge metadata güncelle |
| [**apiV1DemandsDemandIdDocumentsGet**](DemandsApi.md#apiV1DemandsDemandIdDocumentsGet) | **GET** /api/v1/demands/{demandId}/documents | Zarf belge listesi |
| [**apiV1DemandsDemandIdDocumentsGetWithHttpInfo**](DemandsApi.md#apiV1DemandsDemandIdDocumentsGetWithHttpInfo) | **GET** /api/v1/demands/{demandId}/documents | Zarf belge listesi |
| [**apiV1DemandsDemandIdDocumentsOrderPut**](DemandsApi.md#apiV1DemandsDemandIdDocumentsOrderPut) | **PUT** /api/v1/demands/{demandId}/documents/order | Zarftaki belgelerin sırasını değiştir |
| [**apiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo**](DemandsApi.md#apiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo) | **PUT** /api/v1/demands/{demandId}/documents/order | Zarftaki belgelerin sırasını değiştir |
| [**apiV1DemandsDemandIdDocumentsPost**](DemandsApi.md#apiV1DemandsDemandIdDocumentsPost) | **POST** /api/v1/demands/{demandId}/documents | Zarfa metadata-only belge ekle |
| [**apiV1DemandsDemandIdDocumentsPostWithHttpInfo**](DemandsApi.md#apiV1DemandsDemandIdDocumentsPostWithHttpInfo) | **POST** /api/v1/demands/{demandId}/documents | Zarfa metadata-only belge ekle |
| [**apiV1DemandsDemandIdDocumentsUploadPost**](DemandsApi.md#apiV1DemandsDemandIdDocumentsUploadPost) | **POST** /api/v1/demands/{demandId}/documents/upload | Zarfa dosya yükle (belge başına tek dosya) |
| [**apiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo**](DemandsApi.md#apiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo) | **POST** /api/v1/demands/{demandId}/documents/upload | Zarfa dosya yükle (belge başına tek dosya) |
| [**apiV1DemandsGet**](DemandsApi.md#apiV1DemandsGet) | **GET** /api/v1/demands | Sözleşme listesi (counts-only, PII&#39;siz) |
| [**apiV1DemandsGetWithHttpInfo**](DemandsApi.md#apiV1DemandsGetWithHttpInfo) | **GET** /api/v1/demands | Sözleşme listesi (counts-only, PII&#39;siz) |
| [**apiV1DemandsIdArchivePost**](DemandsApi.md#apiV1DemandsIdArchivePost) | **POST** /api/v1/demands/{id}/archive | Sözleşmeyi arşivle |
| [**apiV1DemandsIdArchivePostWithHttpInfo**](DemandsApi.md#apiV1DemandsIdArchivePostWithHttpInfo) | **POST** /api/v1/demands/{id}/archive | Sözleşmeyi arşivle |
| [**apiV1DemandsIdBelgeDocumentIdPdfGet**](DemandsApi.md#apiV1DemandsIdBelgeDocumentIdPdfGet) | **GET** /api/v1/demands/{id}/belge/{document_id}/pdf | Belge-özgü imzalı PDF (çok-belgeli zarf) |
| [**apiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo**](DemandsApi.md#apiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo) | **GET** /api/v1/demands/{id}/belge/{document_id}/pdf | Belge-özgü imzalı PDF (çok-belgeli zarf) |
| [**apiV1DemandsIdCancelPost**](DemandsApi.md#apiV1DemandsIdCancelPost) | **POST** /api/v1/demands/{id}/cancel | Sözleşme iptal (void) |
| [**apiV1DemandsIdCancelPostWithHttpInfo**](DemandsApi.md#apiV1DemandsIdCancelPostWithHttpInfo) | **POST** /api/v1/demands/{id}/cancel | Sözleşme iptal (void) |
| [**apiV1DemandsIdCertificateGet**](DemandsApi.md#apiV1DemandsIdCertificateGet) | **GET** /api/v1/demands/{id}/certificate | Tamamlanma sertifikası (PAdES B-T) |
| [**apiV1DemandsIdCertificateGetWithHttpInfo**](DemandsApi.md#apiV1DemandsIdCertificateGetWithHttpInfo) | **GET** /api/v1/demands/{id}/certificate | Tamamlanma sertifikası (PAdES B-T) |
| [**apiV1DemandsIdDelete**](DemandsApi.md#apiV1DemandsIdDelete) | **DELETE** /api/v1/demands/{id} | Sözleşme sil (yalnızca tamamlanmamış) |
| [**apiV1DemandsIdDeleteWithHttpInfo**](DemandsApi.md#apiV1DemandsIdDeleteWithHttpInfo) | **DELETE** /api/v1/demands/{id} | Sözleşme sil (yalnızca tamamlanmamış) |
| [**apiV1DemandsIdEmbedSessionPost**](DemandsApi.md#apiV1DemandsIdEmbedSessionPost) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint) |
| [**apiV1DemandsIdEmbedSessionPostWithHttpInfo**](DemandsApi.md#apiV1DemandsIdEmbedSessionPostWithHttpInfo) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint) |
| [**apiV1DemandsIdGet**](DemandsApi.md#apiV1DemandsIdGet) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi |
| [**apiV1DemandsIdGetWithHttpInfo**](DemandsApi.md#apiV1DemandsIdGetWithHttpInfo) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi |
| [**apiV1DemandsIdItemsItemIdStampPatch**](DemandsApi.md#apiV1DemandsIdItemsItemIdStampPatch) | **PATCH** /api/v1/demands/{id}/items/{itemId}/stamp | Kaşe alanını doldur (kısmi güncelleme) |
| [**apiV1DemandsIdItemsItemIdStampPatchWithHttpInfo**](DemandsApi.md#apiV1DemandsIdItemsItemIdStampPatchWithHttpInfo) | **PATCH** /api/v1/demands/{id}/items/{itemId}/stamp | Kaşe alanını doldur (kısmi güncelleme) |
| [**apiV1DemandsIdItemsPost**](DemandsApi.md#apiV1DemandsIdItemsPost) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace) |
| [**apiV1DemandsIdItemsPostWithHttpInfo**](DemandsApi.md#apiV1DemandsIdItemsPostWithHttpInfo) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace) |
| [**apiV1DemandsIdPartiesPartyIdResendPost**](DemandsApi.md#apiV1DemandsIdPartiesPartyIdResendPost) | **POST** /api/v1/demands/{id}/parties/{partyId}/resend | Tekil tarafa imza davetini tekrar gönder |
| [**apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo**](DemandsApi.md#apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo) | **POST** /api/v1/demands/{id}/parties/{partyId}/resend | Tekil tarafa imza davetini tekrar gönder |
| [**apiV1DemandsIdPdfGet**](DemandsApi.md#apiV1DemandsIdPdfGet) | **GET** /api/v1/demands/{id}/pdf | İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme) |
| [**apiV1DemandsIdPdfGetWithHttpInfo**](DemandsApi.md#apiV1DemandsIdPdfGetWithHttpInfo) | **GET** /api/v1/demands/{id}/pdf | İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme) |
| [**apiV1DemandsIdTermPatch**](DemandsApi.md#apiV1DemandsIdTermPatch) | **PATCH** /api/v1/demands/{id}/term | Sözleşme süre/yenileme takibini güncelle |
| [**apiV1DemandsIdTermPatchWithHttpInfo**](DemandsApi.md#apiV1DemandsIdTermPatchWithHttpInfo) | **PATCH** /api/v1/demands/{id}/term | Sözleşme süre/yenileme takibini güncelle |
| [**apiV1DemandsIdTimelineGet**](DemandsApi.md#apiV1DemandsIdTimelineGet) | **GET** /api/v1/demands/{id}/timeline | İmza denetim izi (maskeli) |
| [**apiV1DemandsIdTimelineGetWithHttpInfo**](DemandsApi.md#apiV1DemandsIdTimelineGetWithHttpInfo) | **GET** /api/v1/demands/{id}/timeline | İmza denetim izi (maskeli) |
| [**apiV1DemandsIdUnarchivePost**](DemandsApi.md#apiV1DemandsIdUnarchivePost) | **POST** /api/v1/demands/{id}/unarchive | Sözleşmeyi arşivden çıkar |
| [**apiV1DemandsIdUnarchivePostWithHttpInfo**](DemandsApi.md#apiV1DemandsIdUnarchivePostWithHttpInfo) | **POST** /api/v1/demands/{id}/unarchive | Sözleşmeyi arşivden çıkar |
| [**apiV1DemandsPost**](DemandsApi.md#apiV1DemandsPost) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan) |
| [**apiV1DemandsPostWithHttpInfo**](DemandsApi.md#apiV1DemandsPostWithHttpInfo) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan) |
| [**apiV1DemandsUploadPost**](DemandsApi.md#apiV1DemandsUploadPost) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz) |
| [**apiV1DemandsUploadPostWithHttpInfo**](DemandsApi.md#apiV1DemandsUploadPostWithHttpInfo) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz) |
| [**apiV1FieldTemplatesIdPreviewLayoutPost**](DemandsApi.md#apiV1FieldTemplatesIdPreviewLayoutPost) | **POST** /api/v1/field-templates/{id}/preview-layout | Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener |
| [**apiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo**](DemandsApi.md#apiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo) | **POST** /api/v1/field-templates/{id}/preview-layout | Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener |



## apiV1DemandsBulkPost

> ApiV1DemandsBulkPost200Response apiV1DemandsBulkPost(apiV1DemandsBulkPostRequest, xWorkspaceId)

Toplu sözleşme oluştur (tek şablondan N alıcı)

Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \&quot;bulk send\&quot; modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa her satırda önce yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez.  **Davranış:** - &#x60;rows&#x60; en fazla 10 (aşarsa 400 &#x60;BULK_MAX_10&#x60;). Daha büyük listeler   istemci tarafında 10&#39;arlı parçalara bölünür. - Kredi: sabit \&quot;1 satır &#x3D; 1 kredi\&quot; değildir, her satırın maliyeti   &#x60;POST /demands&#x60; ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; &#x60;eidas_timestamp&#x60; seçilirse   satır başına +1 kredi eklenir). Şablon çok-belgeli bir zarf   tanımlıyorsa imzacı başına maliyet o imzacıya ATANMIŞ belge   sayısına göre hesaplanır ve belge sayısına göre azalan birim   fiyatlı bir indirim uygulanır (1 belge %0, 2 belge %30, 3 ve üzeri   %50, imzacı başına ayrı yukarı yuvarlanır; bkz. &#x60;POST /demands&#x60;   \&quot;Kredi (çok belgeli zarf)\&quot; bölümü). PAdES seviye eki yalnız QES&#39;te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   &#x60;ocr_id&#x60; ve &#x60;liveness&#x60; +1 kredi, diğerleri 0&#39;dır. &#x60;options&#x60;   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır &#x60;failed&#x60; (&#x60;error: \&quot;INSUFFICIENT_CREDITS\&quot;&#x60;)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine &#x60;failed&#x60; döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (&#x60;demand_id&#x60; response&#39;ta bulunur, davet   gönderilmemiştir); mutabakat kredi-dışı bir hatayla düşerse satır   &#x60;failed&#x60; (&#x60;error: \&quot;RECONCILE_FAILED\&quot;&#x60;) döner, sözleşme yine taslağa   düşer. - Atamasız imzacı: çok belgeli şablonda eşlenen bir taraf hiçbir   belgeye atanmamışsa (ve &#x60;options.dispatch_notifications&#x60; &#x60;false&#x60;   DEĞİLSE) o satır &#x60;failed&#x60; (&#x60;error: \&quot;PARTY_WITHOUT_DOCUMENTS\&quot;&#x60;)   döner; kontrol satır oluşturulmadan ÖNCE (&#x60;demand_id&#x60; YOK) veya   oluşturulduktan hemen SONRA (sözleşme taslağa düşer, &#x60;demand_id&#x60; +   &#x60;party_ids&#x60; döner) çalışabilir. Diğer satırlar etkilenmez. - Belge seçimi satır başınadır: &#x60;rows[i].documents&#x60; (&#x60;POST /demands&#x60;   ile aynı şema). &#x60;options.documents&#x60; gönderilirse 400   &#x60;INVALID_DOCUMENT_SELECTION&#x60;. Satırın seçim hatası o satırı &#x60;failed&#x60;   yapar (&#x60;error&#x60;: &#x60;INVALID_DOCUMENT_SELECTION&#x60; + &#x60;details&#x60;, ya da   &#x60;PARTY_WITHOUT_DOCUMENTS&#x60; + &#x60;template_party_ids&#x60;); diğer satırlar   etkilenmez. - Belge başına değişkenler (&#x60;document_variables&#x60;) bu uçta   desteklenmez; yalnız &#x60;POST /demands&#x60; ile gönderilir. Satırda (kökte   ya da &#x60;party_mapping[i]&#x60; içinde) gönderilirse o satır &#x60;failed&#x60;   (&#x60;error: \&quot;INVALID_DOCUMENT_VARIABLES\&quot;&#x60;, &#x60;details.reason:   \&quot;unsupported_endpoint\&quot;&#x60;, &#x60;details.path&#x60;) döner ve oluşturulmaz;   &#x60;options.document_variables&#x60; gönderilirse 400   &#x60;INVALID_DOCUMENT_VARIABLES&#x60; döner, hiçbir satır oluşturulmaz. - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace&#39;in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar &#x60;failed&#x60; olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - &#x60;X-Workspace-Id&#x60; header&#39;ı ile organizasyon workspace&#39;i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest = new ApiV1DemandsBulkPostRequest(); // ApiV1DemandsBulkPostRequest | 
        UUID xWorkspaceId = UUID.randomUUID(); // UUID | Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   `WORKSPACE_MISMATCH` döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (`Not a member of this organization`). 
        try {
            ApiV1DemandsBulkPost200Response result = apiInstance.apiV1DemandsBulkPost(apiV1DemandsBulkPostRequest, xWorkspaceId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsBulkPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **apiV1DemandsBulkPostRequest** | [**ApiV1DemandsBulkPostRequest**](ApiV1DemandsBulkPostRequest.md)|  | |
| **xWorkspaceId** | **UUID**| Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  | [optional] |

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
| **400** | Geçersiz istek. Olası kodlar: &#x60;BULK_MAX_10&#x60; (&#x60;rows&#x60; 10&#39;dan fazla), boş &#x60;rows&#x60;, eksik &#x60;template_id&#x60;, &#x60;INVALID_DOCUMENT_SELECTION&#x60; (&#x60;options.documents&#x60; gönderildi; belge seçimi satır başınadır, bkz. &#x60;rows[i].documents&#x60;), &#x60;INVALID_DOCUMENT_VARIABLES&#x60; (&#x60;options.document_variables&#x60; gönderildi; bu uçta desteklenmez, &#x60;details.reason: \&quot;unsupported_endpoint\&quot;&#x60;), &#x60;INVALID_PADES_LEVEL&#x60; (&#x60;options.qes_pades_level&#x60; satılabilir seviyelerden biri değil), &#x60;FIELD_LAYOUT_TEMPLATE_NOT_SENDABLE&#x60; (verilen kimlik bir Alan Şablonuna ait). AB nitelikli zaman damgası hesabınızda kapalıyken &#x60;eidas_timestamp&#x60; gönderilirse de 400 döner.  &#x60;SIGNATURE_VARIANTS_PHONE_ONLY&#x60;: &#x60;options.allowed_signature_variants&#x60; yalnız &#x60;phone&#x60; içeriyor. &#x60;SIGNATURE_VARIANTS_PHONE_DRAW_EXCLUSIVE&#x60;: &#x60;phone_draw&#x60; başka bir yöntemle birlikte gönderildi. Batch-seviye kontrol olduğu için hiçbir satır oluşturulmaz.  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **402** | INSUFFICIENT_CREDITS: N satırın toplam tahmini maliyeti için yeterli kredi yok (ön kontrol; hiçbir sözleşme yaratılmadı). Satır bazlı yetersiz kredi durumları burada DEĞİL, 200 yanıtı içindeki &#x60;results[].status: \&quot;failed\&quot;&#x60; altında döner.  |  -  |
| **403** | INSUFFICIENT_SCOPE (demands:write yok) veya SMS_CUSTOMIZATION_NOT_ALLOWED |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | &#x60;QES_NOT_SUPPORTED_MULTI_DOCUMENT&#x60;: güvenli elektronik imza birden çok belge içeren zarflarda henüz desteklenmiyor.  |  -  |
| **429** | İstek limiti aşıldı (API anahtarı başına 5 toplu istek/dakika) |  -  |

## apiV1DemandsBulkPostWithHttpInfo

> ApiResponse<ApiV1DemandsBulkPost200Response> apiV1DemandsBulkPostWithHttpInfo(apiV1DemandsBulkPostRequest, xWorkspaceId)

Toplu sözleşme oluştur (tek şablondan N alıcı)

Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve her birine imza daveti gönderir (DocuSign \&quot;bulk send\&quot; modeli). Her satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa her satırda önce yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez.  **Davranış:** - &#x60;rows&#x60; en fazla 10 (aşarsa 400 &#x60;BULK_MAX_10&#x60;). Daha büyük listeler   istemci tarafında 10&#39;arlı parçalara bölünür. - Kredi: sabit \&quot;1 satır &#x3D; 1 kredi\&quot; değildir, her satırın maliyeti   &#x60;POST /demands&#x60; ile AYNI formülle hesaplanır (imza sınıfı taban   ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek   maliyeti, imzacı sayısıyla çarpılır; &#x60;eidas_timestamp&#x60; seçilirse   satır başına +1 kredi eklenir). Şablon çok-belgeli bir zarf   tanımlıyorsa imzacı başına maliyet o imzacıya ATANMIŞ belge   sayısına göre hesaplanır ve belge sayısına göre azalan birim   fiyatlı bir indirim uygulanır (1 belge %0, 2 belge %30, 3 ve üzeri   %50, imzacı başına ayrı yukarı yuvarlanır; bkz. &#x60;POST /demands&#x60;   \&quot;Kredi (çok belgeli zarf)\&quot; bölümü). PAdES seviye eki yalnız QES&#39;te   uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden   &#x60;ocr_id&#x60; ve &#x60;liveness&#x60; +1 kredi, diğerleri 0&#39;dır. &#x60;options&#x60;   batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın   toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,   hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için   tekrar kontrol edilir; kredi satır oluşturma sırasında   tükenirse o satır &#x60;failed&#x60; (&#x60;error: \&quot;INSUFFICIENT_CREDITS\&quot;&#x60;)   olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır   oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır   yine &#x60;failed&#x60; döner ama sözleşme zaten oluşturulmuş taslak   olarak kalır (&#x60;demand_id&#x60; response&#39;ta bulunur, davet   gönderilmemiştir); mutabakat kredi-dışı bir hatayla düşerse satır   &#x60;failed&#x60; (&#x60;error: \&quot;RECONCILE_FAILED\&quot;&#x60;) döner, sözleşme yine taslağa   düşer. - Atamasız imzacı: çok belgeli şablonda eşlenen bir taraf hiçbir   belgeye atanmamışsa (ve &#x60;options.dispatch_notifications&#x60; &#x60;false&#x60;   DEĞİLSE) o satır &#x60;failed&#x60; (&#x60;error: \&quot;PARTY_WITHOUT_DOCUMENTS\&quot;&#x60;)   döner; kontrol satır oluşturulmadan ÖNCE (&#x60;demand_id&#x60; YOK) veya   oluşturulduktan hemen SONRA (sözleşme taslağa düşer, &#x60;demand_id&#x60; +   &#x60;party_ids&#x60; döner) çalışabilir. Diğer satırlar etkilenmez. - Belge seçimi satır başınadır: &#x60;rows[i].documents&#x60; (&#x60;POST /demands&#x60;   ile aynı şema). &#x60;options.documents&#x60; gönderilirse 400   &#x60;INVALID_DOCUMENT_SELECTION&#x60;. Satırın seçim hatası o satırı &#x60;failed&#x60;   yapar (&#x60;error&#x60;: &#x60;INVALID_DOCUMENT_SELECTION&#x60; + &#x60;details&#x60;, ya da   &#x60;PARTY_WITHOUT_DOCUMENTS&#x60; + &#x60;template_party_ids&#x60;); diğer satırlar   etkilenmez. - Belge başına değişkenler (&#x60;document_variables&#x60;) bu uçta   desteklenmez; yalnız &#x60;POST /demands&#x60; ile gönderilir. Satırda (kökte   ya da &#x60;party_mapping[i]&#x60; içinde) gönderilirse o satır &#x60;failed&#x60;   (&#x60;error: \&quot;INVALID_DOCUMENT_VARIABLES\&quot;&#x60;, &#x60;details.reason:   \&quot;unsupported_endpoint\&quot;&#x60;, &#x60;details.path&#x60;) döner ve oluşturulmaz;   &#x60;options.document_variables&#x60; gönderilirse 400   &#x60;INVALID_DOCUMENT_VARIABLES&#x60; döner, hiçbir satır oluşturulmaz. - Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;   başka workspace&#39;in şablonu 404). - Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı   satırlar &#x60;failed&#x60; olsa bile). Yalnızca ön-kontrol redleri   (cap/kredi/sahiplik) 400/402/404 döner. - &#x60;X-Workspace-Id&#x60; header&#39;ı ile organizasyon workspace&#39;i seçilebilir.  Rate limit: 5 istek/dakika (API key başına). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        ApiV1DemandsBulkPostRequest apiV1DemandsBulkPostRequest = new ApiV1DemandsBulkPostRequest(); // ApiV1DemandsBulkPostRequest | 
        UUID xWorkspaceId = UUID.randomUUID(); // UUID | Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   `WORKSPACE_MISMATCH` döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (`Not a member of this organization`). 
        try {
            ApiResponse<ApiV1DemandsBulkPost200Response> response = apiInstance.apiV1DemandsBulkPostWithHttpInfo(apiV1DemandsBulkPostRequest, xWorkspaceId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsBulkPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **apiV1DemandsBulkPostRequest** | [**ApiV1DemandsBulkPostRequest**](ApiV1DemandsBulkPostRequest.md)|  | |
| **xWorkspaceId** | **UUID**| Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  | [optional] |

### Return type

ApiResponse<[**ApiV1DemandsBulkPost200Response**](ApiV1DemandsBulkPost200Response.md)>


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Batch tamamlandı (kısmi başarı dahil). &#x60;results&#x60; giriş sırasında, her satır &#x60;row_index&#x60; ile eşlenir.  |  -  |
| **400** | Geçersiz istek. Olası kodlar: &#x60;BULK_MAX_10&#x60; (&#x60;rows&#x60; 10&#39;dan fazla), boş &#x60;rows&#x60;, eksik &#x60;template_id&#x60;, &#x60;INVALID_DOCUMENT_SELECTION&#x60; (&#x60;options.documents&#x60; gönderildi; belge seçimi satır başınadır, bkz. &#x60;rows[i].documents&#x60;), &#x60;INVALID_DOCUMENT_VARIABLES&#x60; (&#x60;options.document_variables&#x60; gönderildi; bu uçta desteklenmez, &#x60;details.reason: \&quot;unsupported_endpoint\&quot;&#x60;), &#x60;INVALID_PADES_LEVEL&#x60; (&#x60;options.qes_pades_level&#x60; satılabilir seviyelerden biri değil), &#x60;FIELD_LAYOUT_TEMPLATE_NOT_SENDABLE&#x60; (verilen kimlik bir Alan Şablonuna ait). AB nitelikli zaman damgası hesabınızda kapalıyken &#x60;eidas_timestamp&#x60; gönderilirse de 400 döner.  &#x60;SIGNATURE_VARIANTS_PHONE_ONLY&#x60;: &#x60;options.allowed_signature_variants&#x60; yalnız &#x60;phone&#x60; içeriyor. &#x60;SIGNATURE_VARIANTS_PHONE_DRAW_EXCLUSIVE&#x60;: &#x60;phone_draw&#x60; başka bir yöntemle birlikte gönderildi. Batch-seviye kontrol olduğu için hiçbir satır oluşturulmaz.  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **402** | INSUFFICIENT_CREDITS: N satırın toplam tahmini maliyeti için yeterli kredi yok (ön kontrol; hiçbir sözleşme yaratılmadı). Satır bazlı yetersiz kredi durumları burada DEĞİL, 200 yanıtı içindeki &#x60;results[].status: \&quot;failed\&quot;&#x60; altında döner.  |  -  |
| **403** | INSUFFICIENT_SCOPE (demands:write yok) veya SMS_CUSTOMIZATION_NOT_ALLOWED |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | &#x60;QES_NOT_SUPPORTED_MULTI_DOCUMENT&#x60;: güvenli elektronik imza birden çok belge içeren zarflarda henüz desteklenmiyor.  |  -  |
| **429** | İstek limiti aşıldı (API anahtarı başına 5 toplu istek/dakika) |  -  |


## apiV1DemandsDemandIdDispatchPost

> ApiV1DemandsDemandIdDispatchPost200Response apiV1DemandsDemandIdDispatchPost(demandId, apiV1DemandsDemandIdDispatchPostRequest)

Zarfı imzaya gönder (yayınla + davet)

Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, &#x60;DRAFT&#x60; ise sözleşmeyi &#x60;PENDING&#x60;&#39;e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  **Onaylayan:** Sözleşmede Onaylayan varsa bu uç önce yalnızca onay bekleyen onaylayanları davet eder; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir. Bir onaylayan reddettiyse bu uç kimseye davet göndermez; onay vermiş onaylayan yeniden davet edilmez.  🔴 **Bu uçta yukarıdaki &#x60;/documents*&#x60; ailesinin bayrak kapısı (&#x60;ENVELOPE_DECISION_ENFORCE&#x60;) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — &#x60;dispatch_notifications: false&#x60; ile oluşturulur, ardından &#x60;POST .../documents&#x60; / &#x60;POST .../documents/upload&#x60; ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; &#x60;send_invitations&#x60; gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir. **Bu uç çağrılana kadar** birden çok belgeli bir zarfın oluşturma yanıtındaki &#x60;signing_url&#x60;&#39;leri ÇALIŞMAZ (imzacı için 410; &#x60;POST .../parties/{partyId}/resend&#x60; ve &#x60;POST .../reminders&#x60; için 409 &#x60;ENVELOPE_NOT_DISPATCHED&#x60; döner); belge uçları (&#x60;/documents*&#x60;) ise gönderim beklemeden kullanılabilir.  **Kredi:** tek tahsilat noktası burasıdır (&#x60;reconcileDemandSigningCost&#x60;). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (&#x60;dispatched: false&#x60; döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. &#x60;v1ResendRateLimiter&#x60;, &#x60;/parties/{partyId}/resend&#x60; ile AYNI dakika-bazlı freni paylaşır). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        ApiV1DemandsDemandIdDispatchPostRequest apiV1DemandsDemandIdDispatchPostRequest = new ApiV1DemandsDemandIdDispatchPostRequest(); // ApiV1DemandsDemandIdDispatchPostRequest | 
        try {
            ApiV1DemandsDemandIdDispatchPost200Response result = apiInstance.apiV1DemandsDemandIdDispatchPost(demandId, apiV1DemandsDemandIdDispatchPostRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDispatchPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **apiV1DemandsDemandIdDispatchPostRequest** | [**ApiV1DemandsDemandIdDispatchPostRequest**](ApiV1DemandsDemandIdDispatchPostRequest.md)|  | [optional] |

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

## apiV1DemandsDemandIdDispatchPostWithHttpInfo

> ApiResponse<ApiV1DemandsDemandIdDispatchPost200Response> apiV1DemandsDemandIdDispatchPostWithHttpInfo(demandId, apiV1DemandsDemandIdDispatchPostRequest)

Zarfı imzaya gönder (yayınla + davet)

Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, &#x60;DRAFT&#x60; ise sözleşmeyi &#x60;PENDING&#x60;&#39;e alır ve tarafları imza daveti (SMS/e-posta/ WhatsApp) ile bilgilendirir.  **Onaylayan:** Sözleşmede Onaylayan varsa bu uç önce yalnızca onay bekleyen onaylayanları davet eder; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir. Bir onaylayan reddettiyse bu uç kimseye davet göndermez; onay vermiş onaylayan yeniden davet edilmez.  🔴 **Bu uçta yukarıdaki &#x60;/documents*&#x60; ailesinin bayrak kapısı (&#x60;ENVELOPE_DECISION_ENFORCE&#x60;) YOKTUR** (bilinçli): gönderim tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf YARATIMINI kapatan bir anahtardır.  **İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir — &#x60;dispatch_notifications: false&#x60; ile oluşturulur, ardından &#x60;POST .../documents&#x60; / &#x60;POST .../documents/upload&#x60; ile belgeler eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; &#x60;send_invitations&#x60; gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu uçla yayına alıp davetleri gönderir. **Bu uç çağrılana kadar** birden çok belgeli bir zarfın oluşturma yanıtındaki &#x60;signing_url&#x60;&#39;leri ÇALIŞMAZ (imzacı için 410; &#x60;POST .../parties/{partyId}/resend&#x60; ve &#x60;POST .../reminders&#x60; için 409 &#x60;ENVELOPE_NOT_DISPATCHED&#x60; döner); belge uçları (&#x60;/documents*&#x60;) ise gönderim beklemeden kullanılabilir.  **Kredi:** tek tahsilat noktası burasıdır (&#x60;reconcileDemandSigningCost&#x60;). Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir: sözleşme zaten yayınlanmışsa (&#x60;dispatched: false&#x60; döner) fark 0 olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır (bkz. &#x60;v1ResendRateLimiter&#x60;, &#x60;/parties/{partyId}/resend&#x60; ile AYNI dakika-bazlı freni paylaşır). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        ApiV1DemandsDemandIdDispatchPostRequest apiV1DemandsDemandIdDispatchPostRequest = new ApiV1DemandsDemandIdDispatchPostRequest(); // ApiV1DemandsDemandIdDispatchPostRequest | 
        try {
            ApiResponse<ApiV1DemandsDemandIdDispatchPost200Response> response = apiInstance.apiV1DemandsDemandIdDispatchPostWithHttpInfo(demandId, apiV1DemandsDemandIdDispatchPostRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDispatchPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **apiV1DemandsDemandIdDispatchPostRequest** | [**ApiV1DemandsDemandIdDispatchPostRequest**](ApiV1DemandsDemandIdDispatchPostRequest.md)|  | [optional] |

### Return type

ApiResponse<[**ApiV1DemandsDemandIdDispatchPost200Response**](ApiV1DemandsDemandIdDispatchPost200Response.md)>


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


## apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut

> ApiV1DemandsDemandIdDocumentsPost201Response apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest)

Belgeye imzacı ata (tam-küme replace)

Belgeye atanmış imzacı kümesini **tam olarak** &#x60;party_ids&#x60; ile değiştirir (eski atamalar silinir, yenileri yazılır). &#x60;party_ids&#x60; boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 &#x60;INVALID_PARTY_ID&#x60; — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 &#x60;ASSIGNMENT_HAS_DECISION&#x60;). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        UUID docId = UUID.randomUUID(); // UUID | 
        ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest = new ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest(); // ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest | 
        try {
            ApiV1DemandsDemandIdDocumentsPost201Response result = apiInstance.apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **docId** | **UUID**|  | |
| **apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest** | [**ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest**](ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest.md)|  | |

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

## apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo

> ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest)

Belgeye imzacı ata (tam-küme replace)

Belgeye atanmış imzacı kümesini **tam olarak** &#x60;party_ids&#x60; ile değiştirir (eski atamalar silinir, yenileri yazılır). &#x60;party_ids&#x60; boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır (400 &#x60;INVALID_PARTY_ID&#x60; — çapraz-sözleşme id kabul edilmez).  Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması kaldırılamaz (400 &#x60;ASSIGNMENT_HAS_DECISION&#x60;). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        UUID docId = UUID.randomUUID(); // UUID | 
        ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest = new ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest(); // ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest | 
        try {
            ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> response = apiInstance.apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutWithHttpInfo(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDocumentsDocIdAssignmentsPut");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **docId** | **UUID**|  | |
| **apiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest** | [**ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest**](ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest.md)|  | |

### Return type

ApiResponse<[**ApiV1DemandsDemandIdDocumentsPost201Response**](ApiV1DemandsDemandIdDocumentsPost201Response.md)>


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


## apiV1DemandsDemandIdDocumentsDocIdDelete

> ApiV1TemplatesIdDelete200Response apiV1DemandsDemandIdDocumentsDocIdDelete(demandId, docId)

Belgeyi zarftan sil

Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N&#39;e yeniden sıralanır. **Zarftaki son belge silinemez** (400 &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60;) — zarf hiç boş kalamaz. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        UUID docId = UUID.randomUUID(); // UUID | 
        try {
            ApiV1TemplatesIdDelete200Response result = apiInstance.apiV1DemandsDemandIdDocumentsDocIdDelete(demandId, docId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDocumentsDocIdDelete");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **docId** | **UUID**|  | |

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

## apiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo

> ApiResponse<ApiV1TemplatesIdDelete200Response> apiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo(demandId, docId)

Belgeyi zarftan sil

Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler 1..N&#39;e yeniden sıralanır. **Zarftaki son belge silinemez** (400 &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60;) — zarf hiç boş kalamaz. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        UUID docId = UUID.randomUUID(); // UUID | 
        try {
            ApiResponse<ApiV1TemplatesIdDelete200Response> response = apiInstance.apiV1DemandsDemandIdDocumentsDocIdDeleteWithHttpInfo(demandId, docId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDocumentsDocIdDelete");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **docId** | **UUID**|  | |

### Return type

ApiResponse<[**ApiV1TemplatesIdDelete200Response**](ApiV1TemplatesIdDelete200Response.md)>


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


## apiV1DemandsDemandIdDocumentsDocIdPatch

> ApiV1DemandsDemandIdDocumentsPost201Response apiV1DemandsDemandIdDocumentsDocIdPatch(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdPatchRequest)

Belge metadata güncelle

Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (&#x60;PUT .../assignments&#x60;). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        UUID docId = UUID.randomUUID(); // UUID | 
        ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest = new ApiV1DemandsDemandIdDocumentsDocIdPatchRequest(); // ApiV1DemandsDemandIdDocumentsDocIdPatchRequest | 
        try {
            ApiV1DemandsDemandIdDocumentsPost201Response result = apiInstance.apiV1DemandsDemandIdDocumentsDocIdPatch(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdPatchRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDocumentsDocIdPatch");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **docId** | **UUID**|  | |
| **apiV1DemandsDemandIdDocumentsDocIdPatchRequest** | [**ApiV1DemandsDemandIdDocumentsDocIdPatchRequest**](ApiV1DemandsDemandIdDocumentsDocIdPatchRequest.md)|  | |

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

## apiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo

> ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> apiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdPatchRequest)

Belge metadata güncelle

Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/ signature_required) günceller — **kısmi güncelleme**: gövdede gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur (&#x60;PUT .../assignments&#x60;). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        UUID docId = UUID.randomUUID(); // UUID | 
        ApiV1DemandsDemandIdDocumentsDocIdPatchRequest apiV1DemandsDemandIdDocumentsDocIdPatchRequest = new ApiV1DemandsDemandIdDocumentsDocIdPatchRequest(); // ApiV1DemandsDemandIdDocumentsDocIdPatchRequest | 
        try {
            ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> response = apiInstance.apiV1DemandsDemandIdDocumentsDocIdPatchWithHttpInfo(demandId, docId, apiV1DemandsDemandIdDocumentsDocIdPatchRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDocumentsDocIdPatch");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **docId** | **UUID**|  | |
| **apiV1DemandsDemandIdDocumentsDocIdPatchRequest** | [**ApiV1DemandsDemandIdDocumentsDocIdPatchRequest**](ApiV1DemandsDemandIdDocumentsDocIdPatchRequest.md)|  | |

### Return type

ApiResponse<[**ApiV1DemandsDemandIdDocumentsPost201Response**](ApiV1DemandsDemandIdDocumentsPost201Response.md)>


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


## apiV1DemandsDemandIdDocumentsGet

> ApiV1DemandsDemandIdDocumentsGet200Response apiV1DemandsDemandIdDocumentsGet(demandId, view)

Zarf belge listesi

Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm &#x60;/documents*&#x60; + &#x60;/documents/{docId}*&#x60; + &#x60;/documents/{docId}/assignments&#x60; uçları &#x60;ENVELOPE_DECISION_ENFORCE&#x60; bayrağı **arkasındadır**; bayrak kapalıyken **409 &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60;** döner. Bu, dashboard&#39;un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca &#x60;POST /demands/{demandId}/dispatch&#x60; çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. &#x60;?view&#x3D;wizard&#x60; verilirse tam şekil döner (&#x60;EnvelopeDocument&#x60; şemasındaki alan notlarına bakın). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        String view = "wizard"; // String | `wizard` → tam DTO (`assigned_party_ids` + `decision_count` dahil).
        try {
            ApiV1DemandsDemandIdDocumentsGet200Response result = apiInstance.apiV1DemandsDemandIdDocumentsGet(demandId, view);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDocumentsGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **view** | **String**| &#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). | [optional] [enum: wizard] |

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

## apiV1DemandsDemandIdDocumentsGetWithHttpInfo

> ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> apiV1DemandsDemandIdDocumentsGetWithHttpInfo(demandId, view)

Zarf belge listesi

Çok-belgeli imza zarfının belge listesini döner.  🔴 Bu uç ve aşağıdaki tüm &#x60;/documents*&#x60; + &#x60;/documents/{docId}*&#x60; + &#x60;/documents/{docId}/assignments&#x60; uçları &#x60;ENVELOPE_DECISION_ENFORCE&#x60; bayrağı **arkasındadır**; bayrak kapalıyken **409 &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60;** döner. Bu, dashboard&#39;un davranışından BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.  Belge uçları **kredi düşmez**; tahsilat yalnızca &#x60;POST /demands/{demandId}/dispatch&#x60; çağrıldığında yapılır.  Varsayılan yanıt daraltılmış özet şeklidir. &#x60;?view&#x3D;wizard&#x60; verilirse tam şekil döner (&#x60;EnvelopeDocument&#x60; şemasındaki alan notlarına bakın). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        String view = "wizard"; // String | `wizard` → tam DTO (`assigned_party_ids` + `decision_count` dahil).
        try {
            ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> response = apiInstance.apiV1DemandsDemandIdDocumentsGetWithHttpInfo(demandId, view);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDocumentsGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **view** | **String**| &#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). | [optional] [enum: wizard] |

### Return type

ApiResponse<[**ApiV1DemandsDemandIdDocumentsGet200Response**](ApiV1DemandsDemandIdDocumentsGet200Response.md)>


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


## apiV1DemandsDemandIdDocumentsOrderPut

> ApiV1DemandsDemandIdDocumentsGet200Response apiV1DemandsDemandIdDocumentsOrderPut(demandId, apiV1DemandsDemandIdDocumentsOrderPutRequest)

Zarftaki belgelerin sırasını değiştir

Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. &#x60;document_ids&#x60; zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 &#x60;ORDER_SET_MISMATCH&#x60;** döner. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest = new ApiV1DemandsDemandIdDocumentsOrderPutRequest(); // ApiV1DemandsDemandIdDocumentsOrderPutRequest | 
        try {
            ApiV1DemandsDemandIdDocumentsGet200Response result = apiInstance.apiV1DemandsDemandIdDocumentsOrderPut(demandId, apiV1DemandsDemandIdDocumentsOrderPutRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDocumentsOrderPut");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **apiV1DemandsDemandIdDocumentsOrderPutRequest** | [**ApiV1DemandsDemandIdDocumentsOrderPutRequest**](ApiV1DemandsDemandIdDocumentsOrderPutRequest.md)|  | |

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

## apiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo

> ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> apiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo(demandId, apiV1DemandsDemandIdDocumentsOrderPutRequest)

Zarftaki belgelerin sırasını değiştir

Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar. &#x60;document_ids&#x60; zarftaki **mevcut belge kümesiyle birebir aynı** (aynı eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id **400 &#x60;ORDER_SET_MISMATCH&#x60;** döner. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        ApiV1DemandsDemandIdDocumentsOrderPutRequest apiV1DemandsDemandIdDocumentsOrderPutRequest = new ApiV1DemandsDemandIdDocumentsOrderPutRequest(); // ApiV1DemandsDemandIdDocumentsOrderPutRequest | 
        try {
            ApiResponse<ApiV1DemandsDemandIdDocumentsGet200Response> response = apiInstance.apiV1DemandsDemandIdDocumentsOrderPutWithHttpInfo(demandId, apiV1DemandsDemandIdDocumentsOrderPutRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDocumentsOrderPut");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **apiV1DemandsDemandIdDocumentsOrderPutRequest** | [**ApiV1DemandsDemandIdDocumentsOrderPutRequest**](ApiV1DemandsDemandIdDocumentsOrderPutRequest.md)|  | |

### Return type

ApiResponse<[**ApiV1DemandsDemandIdDocumentsGet200Response**](ApiV1DemandsDemandIdDocumentsGet200Response.md)>


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


## apiV1DemandsDemandIdDocumentsPost

> ApiV1DemandsDemandIdDocumentsPost201Response apiV1DemandsDemandIdDocumentsPost(demandId, apiV1DemandsDemandIdDocumentsPostRequest)

Zarfa metadata-only belge ekle

Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: &#x60;POST /demands/{demandId}/documents/upload&#x60;.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest = new ApiV1DemandsDemandIdDocumentsPostRequest(); // ApiV1DemandsDemandIdDocumentsPostRequest | 
        try {
            ApiV1DemandsDemandIdDocumentsPost201Response result = apiInstance.apiV1DemandsDemandIdDocumentsPost(demandId, apiV1DemandsDemandIdDocumentsPostRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDocumentsPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **apiV1DemandsDemandIdDocumentsPostRequest** | [**ApiV1DemandsDemandIdDocumentsPostRequest**](ApiV1DemandsDemandIdDocumentsPostRequest.md)|  | |

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

## apiV1DemandsDemandIdDocumentsPostWithHttpInfo

> ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> apiV1DemandsDemandIdDocumentsPostWithHttpInfo(demandId, apiV1DemandsDemandIdDocumentsPostRequest)

Zarfa metadata-only belge ekle

Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme AYRI bir uçtur: &#x60;POST /demands/{demandId}/documents/upload&#x60;.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        ApiV1DemandsDemandIdDocumentsPostRequest apiV1DemandsDemandIdDocumentsPostRequest = new ApiV1DemandsDemandIdDocumentsPostRequest(); // ApiV1DemandsDemandIdDocumentsPostRequest | 
        try {
            ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> response = apiInstance.apiV1DemandsDemandIdDocumentsPostWithHttpInfo(demandId, apiV1DemandsDemandIdDocumentsPostRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDocumentsPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **apiV1DemandsDemandIdDocumentsPostRequest** | [**ApiV1DemandsDemandIdDocumentsPostRequest**](ApiV1DemandsDemandIdDocumentsPostRequest.md)|  | |

### Return type

ApiResponse<[**ApiV1DemandsDemandIdDocumentsPost201Response**](ApiV1DemandsDemandIdDocumentsPost201Response.md)>


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


## apiV1DemandsDemandIdDocumentsUploadPost

> ApiV1DemandsDemandIdDocumentsPost201Response apiV1DemandsDemandIdDocumentsUploadPost(demandId, _file, idempotencyKey, title, docKind, isRequired)

Zarfa dosya yükle (belge başına tek dosya)

Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  &#x60;idempotency_key&#x60; **zorunludur** (gövde alanı; &#x60;Idempotency-Key&#x60; HEADER&#39;ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı &#x60;(demandId, idempotency_key)&#x60; çifti ile tekrar çağrı **409 &#x60;IDEMPOTENT_REPLAY&#x60;** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO&#39;su döner.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        File _file = new File("/path/to/file"); // File | PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.
        String idempotencyKey = "idempotencyKey_example"; // String | Zorunlu tekrar-koruma anahtarı.
        String title = "title_example"; // String | 
        String docKind = "CONTRACT"; // String | 
        String isRequired = "true"; // String | Multipart alanı — string olarak gönderilir.
        try {
            ApiV1DemandsDemandIdDocumentsPost201Response result = apiInstance.apiV1DemandsDemandIdDocumentsUploadPost(demandId, _file, idempotencyKey, title, docKind, isRequired);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDocumentsUploadPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **_file** | **File**| PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya. | |
| **idempotencyKey** | **String**| Zorunlu tekrar-koruma anahtarı. | |
| **title** | **String**|  | |
| **docKind** | **String**|  | [optional] [default to OTHER] [enum: CONTRACT, KVKK_NOTICE, KVKK_CONSENT, PREINFO, PRICE_LIST, OTHER] |
| **isRequired** | **String**| Multipart alanı — string olarak gönderilir. | [optional] [default to true] [enum: true, false] |

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

## apiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo

> ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> apiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo(demandId, _file, idempotencyKey, title, docKind, isRequired)

Zarfa dosya yükle (belge başına tek dosya)

Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok belge eklemek için bu uç birden çok kez çağrılır — tek istekte N belge YASAKTIR.  &#x60;idempotency_key&#x60; **zorunludur** (gövde alanı; &#x60;Idempotency-Key&#x60; HEADER&#39;ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı &#x60;(demandId, idempotency_key)&#x60; çifti ile tekrar çağrı **409 &#x60;IDEMPOTENT_REPLAY&#x60;** döner ve **yeni belge YARATILMAZ**; yanıt gövdesinde daha önce yüklenen belgenin DTO&#39;su döner.  Belge uçları kredi düşmez; tahsilat &#x60;POST /dispatch&#x60;&#39;te yapılır. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID demandId = UUID.randomUUID(); // UUID | 
        File _file = new File("/path/to/file"); // File | PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.
        String idempotencyKey = "idempotencyKey_example"; // String | Zorunlu tekrar-koruma anahtarı.
        String title = "title_example"; // String | 
        String docKind = "CONTRACT"; // String | 
        String isRequired = "true"; // String | Multipart alanı — string olarak gönderilir.
        try {
            ApiResponse<ApiV1DemandsDemandIdDocumentsPost201Response> response = apiInstance.apiV1DemandsDemandIdDocumentsUploadPostWithHttpInfo(demandId, _file, idempotencyKey, title, docKind, isRequired);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsDemandIdDocumentsUploadPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **demandId** | **UUID**|  | |
| **_file** | **File**| PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya. | |
| **idempotencyKey** | **String**| Zorunlu tekrar-koruma anahtarı. | |
| **title** | **String**|  | |
| **docKind** | **String**|  | [optional] [default to OTHER] [enum: CONTRACT, KVKK_NOTICE, KVKK_CONSENT, PREINFO, PRICE_LIST, OTHER] |
| **isRequired** | **String**| Multipart alanı — string olarak gönderilir. | [optional] [default to true] [enum: true, false] |

### Return type

ApiResponse<[**ApiV1DemandsDemandIdDocumentsPost201Response**](ApiV1DemandsDemandIdDocumentsPost201Response.md)>


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


## apiV1DemandsGet

> ApiV1DemandsGet200Response apiV1DemandsGet(status, q, from, to, templateId, page, limit, sort, archived)

Sözleşme listesi (counts-only, PII&#39;siz)

Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        String status = "DRAFT"; // String | 
        String q = "q_example"; // String | Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez.
        LocalDate from = LocalDate.now(); // LocalDate | 
        LocalDate to = LocalDate.now(); // LocalDate | 
        UUID templateId = UUID.randomUUID(); // UUID | 
        Integer page = 1; // Integer | 
        Integer limit = 20; // Integer | Sayfa boyutu (page_size ile aynı)
        String sort = "sort_example"; // String | alan:yön (ör. createdAt:desc)
        String archived = "exclude"; // String | Arşiv durumu filtresi. `exclude`: yalnız arşivsiz sözleşmeler. `only`: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan `include`: tüm sözleşmeler (arşivli ve arşivsiz). 
        try {
            ApiV1DemandsGet200Response result = apiInstance.apiV1DemandsGet(status, q, from, to, templateId, page, limit, sort, archived);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **status** | **String**|  | [optional] [enum: DRAFT, PENDING, COMPLETED, CANCELLED, EXPIRED] |
| **q** | **String**| Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez. | [optional] |
| **from** | **LocalDate**|  | [optional] |
| **to** | **LocalDate**|  | [optional] |
| **templateId** | **UUID**|  | [optional] |
| **page** | **Integer**|  | [optional] [default to 1] |
| **limit** | **Integer**| Sayfa boyutu (page_size ile aynı) | [optional] [default to 20] |
| **sort** | **String**| alan:yön (ör. createdAt:desc) | [optional] |
| **archived** | **String**| Arşiv durumu filtresi. &#x60;exclude&#x60;: yalnız arşivsiz sözleşmeler. &#x60;only&#x60;: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan &#x60;include&#x60;: tüm sözleşmeler (arşivli ve arşivsiz).  | [optional] [enum: exclude, only, include] |

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

## apiV1DemandsGetWithHttpInfo

> ApiResponse<ApiV1DemandsGet200Response> apiV1DemandsGetWithHttpInfo(status, q, from, to, templateId, page, limit, sort, archived)

Sözleşme listesi (counts-only, PII&#39;siz)

Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu: yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (&#x60;parties_total&#x60;, &#x60;parties_signed&#x60;). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ döndürülmez — taraf detayı için &#x60;GET /demands/{id}&#x60;. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        String status = "DRAFT"; // String | 
        String q = "q_example"; // String | Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez.
        LocalDate from = LocalDate.now(); // LocalDate | 
        LocalDate to = LocalDate.now(); // LocalDate | 
        UUID templateId = UUID.randomUUID(); // UUID | 
        Integer page = 1; // Integer | 
        Integer limit = 20; // Integer | Sayfa boyutu (page_size ile aynı)
        String sort = "sort_example"; // String | alan:yön (ör. createdAt:desc)
        String archived = "exclude"; // String | Arşiv durumu filtresi. `exclude`: yalnız arşivsiz sözleşmeler. `only`: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan `include`: tüm sözleşmeler (arşivli ve arşivsiz). 
        try {
            ApiResponse<ApiV1DemandsGet200Response> response = apiInstance.apiV1DemandsGetWithHttpInfo(status, q, from, to, templateId, page, limit, sort, archived);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **status** | **String**|  | [optional] [enum: DRAFT, PENDING, COMPLETED, CANCELLED, EXPIRED] |
| **q** | **String**| Sözleşme kimliği, başlığı ve açıklamasında arama. Taraf ad, e-posta ve telefon alanlarında ARAMAZ; taraf bilgisi bu uçtan dönmez. | [optional] |
| **from** | **LocalDate**|  | [optional] |
| **to** | **LocalDate**|  | [optional] |
| **templateId** | **UUID**|  | [optional] |
| **page** | **Integer**|  | [optional] [default to 1] |
| **limit** | **Integer**| Sayfa boyutu (page_size ile aynı) | [optional] [default to 20] |
| **sort** | **String**| alan:yön (ör. createdAt:desc) | [optional] |
| **archived** | **String**| Arşiv durumu filtresi. &#x60;exclude&#x60;: yalnız arşivsiz sözleşmeler. &#x60;only&#x60;: yalnız arşivli sözleşmeler. Parametre gönderilmezse varsayılan &#x60;include&#x60;: tüm sözleşmeler (arşivli ve arşivsiz).  | [optional] [enum: exclude, only, include] |

### Return type

ApiResponse<[**ApiV1DemandsGet200Response**](ApiV1DemandsGet200Response.md)>


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


## apiV1DemandsIdArchivePost

> ApiV1DemandsIdArchivePost200Response apiV1DemandsIdArchivePost(id)

Sözleşmeyi arşivle

Arşiv sözleşmenin &#x60;status&#x60;&#39;unu DEĞİŞTİRMEZ; yalnız &#x60;archived_at&#x60; damgası ekler ve arşivlenen sözleşmeyi salt-okunur yapar (&#x60;PATCH .../term&#x60; gibi mutasyonlar 409 &#x60;DEMAND_ARCHIVED&#x60; ile reddedilir; &#x60;GET&#x60; uçları etkilenmez). Yalnız tamamlanmış, iptal edilmiş veya süresi dolmuş sözleşmeler arşivlenebilir. Zaten arşivliyse idempotent (200, mevcut &#x60;archived_at&#x60; ile). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiV1DemandsIdArchivePost200Response result = apiInstance.apiV1DemandsIdArchivePost(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdArchivePost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |

### Return type

[**ApiV1DemandsIdArchivePost200Response**](ApiV1DemandsIdArchivePost200Response.md)


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Arşivlendi (veya zaten arşivliydi) |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | &#x60;DEMAND_NOT_ARCHIVABLE&#x60;: sözleşme henüz tamamlanmadı/iptal edilmedi/süresi dolmadı. &#x60;DEMAND_REJECTED_CANCEL_FIRST&#x60;: sözleşme reddedilmiş ve imza bekliyor; önce &#x60;POST .../cancel&#x60; ile iptal edin.  |  -  |

## apiV1DemandsIdArchivePostWithHttpInfo

> ApiResponse<ApiV1DemandsIdArchivePost200Response> apiV1DemandsIdArchivePostWithHttpInfo(id)

Sözleşmeyi arşivle

Arşiv sözleşmenin &#x60;status&#x60;&#39;unu DEĞİŞTİRMEZ; yalnız &#x60;archived_at&#x60; damgası ekler ve arşivlenen sözleşmeyi salt-okunur yapar (&#x60;PATCH .../term&#x60; gibi mutasyonlar 409 &#x60;DEMAND_ARCHIVED&#x60; ile reddedilir; &#x60;GET&#x60; uçları etkilenmez). Yalnız tamamlanmış, iptal edilmiş veya süresi dolmuş sözleşmeler arşivlenebilir. Zaten arşivliyse idempotent (200, mevcut &#x60;archived_at&#x60; ile). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiResponse<ApiV1DemandsIdArchivePost200Response> response = apiInstance.apiV1DemandsIdArchivePostWithHttpInfo(id);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdArchivePost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |

### Return type

ApiResponse<[**ApiV1DemandsIdArchivePost200Response**](ApiV1DemandsIdArchivePost200Response.md)>


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Arşivlendi (veya zaten arşivliydi) |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | &#x60;DEMAND_NOT_ARCHIVABLE&#x60;: sözleşme henüz tamamlanmadı/iptal edilmedi/süresi dolmadı. &#x60;DEMAND_REJECTED_CANCEL_FIRST&#x60;: sözleşme reddedilmiş ve imza bekliyor; önce &#x60;POST .../cancel&#x60; ile iptal edin.  |  -  |


## apiV1DemandsIdBelgeDocumentIdPdfGet

> File apiV1DemandsIdBelgeDocumentIdPdfGet(id, documentId)

Belge-özgü imzalı PDF (çok-belgeli zarf)

Çok-belgeli zarfta TEK bir belgenin imzalı PDF&#39;ini indirir. Zarf-geneli &#x60;/demands/{id}/pdf&#x60; ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND&#39;lenir (başka zarfın belgesi istenirse 404). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        UUID documentId = UUID.randomUUID(); // UUID | Zarftaki belgenin kimliği.
        try {
            File result = apiInstance.apiV1DemandsIdBelgeDocumentIdPdfGet(id, documentId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdBelgeDocumentIdPdfGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **documentId** | **UUID**| Zarftaki belgenin kimliği. | |

### Return type

[**File**](File.md)


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

## apiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo

> ApiResponse<File> apiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo(id, documentId)

Belge-özgü imzalı PDF (çok-belgeli zarf)

Çok-belgeli zarfta TEK bir belgenin imzalı PDF&#39;ini indirir. Zarf-geneli &#x60;/demands/{id}/pdf&#x60; ucunun belge-kırılımlı ikizidir; scope ve ownership kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND&#39;lenir (başka zarfın belgesi istenirse 404). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        UUID documentId = UUID.randomUUID(); // UUID | Zarftaki belgenin kimliği.
        try {
            ApiResponse<File> response = apiInstance.apiV1DemandsIdBelgeDocumentIdPdfGetWithHttpInfo(id, documentId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdBelgeDocumentIdPdfGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **documentId** | **UUID**| Zarftaki belgenin kimliği. | |

### Return type

ApiResponse<[**File**](File.md)>


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


## apiV1DemandsIdCancelPost

> ApiV1DemandsIdCancelPost200Response apiV1DemandsIdCancelPost(id, apiV1DemandsIdCancelPostRequest)

Sözleşme iptal (void)

Bekleyen bir sözleşmeyi iptal eder (status&#x3D;CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        ApiV1DemandsIdCancelPostRequest apiV1DemandsIdCancelPostRequest = new ApiV1DemandsIdCancelPostRequest(); // ApiV1DemandsIdCancelPostRequest | 
        try {
            ApiV1DemandsIdCancelPost200Response result = apiInstance.apiV1DemandsIdCancelPost(id, apiV1DemandsIdCancelPostRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdCancelPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **apiV1DemandsIdCancelPostRequest** | [**ApiV1DemandsIdCancelPostRequest**](ApiV1DemandsIdCancelPostRequest.md)|  | [optional] |

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

## apiV1DemandsIdCancelPostWithHttpInfo

> ApiResponse<ApiV1DemandsIdCancelPost200Response> apiV1DemandsIdCancelPostWithHttpInfo(id, apiV1DemandsIdCancelPostRequest)

Sözleşme iptal (void)

Bekleyen bir sözleşmeyi iptal eder (status&#x3D;CANCELLED). Tamamlanmış (409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen hatırlatmalar iptal edilir. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        ApiV1DemandsIdCancelPostRequest apiV1DemandsIdCancelPostRequest = new ApiV1DemandsIdCancelPostRequest(); // ApiV1DemandsIdCancelPostRequest | 
        try {
            ApiResponse<ApiV1DemandsIdCancelPost200Response> response = apiInstance.apiV1DemandsIdCancelPostWithHttpInfo(id, apiV1DemandsIdCancelPostRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdCancelPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **apiV1DemandsIdCancelPostRequest** | [**ApiV1DemandsIdCancelPostRequest**](ApiV1DemandsIdCancelPostRequest.md)|  | [optional] |

### Return type

ApiResponse<[**ApiV1DemandsIdCancelPost200Response**](ApiV1DemandsIdCancelPost200Response.md)>


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


## apiV1DemandsIdCertificateGet

> File apiV1DemandsIdCertificateGet(id, lang)

Tamamlanma sertifikası (PAdES B-T)

Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        String lang = "lang_example"; // String | tr | en
        try {
            File result = apiInstance.apiV1DemandsIdCertificateGet(id, lang);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdCertificateGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **lang** | **String**| tr | en | [optional] |

### Return type

[**File**](File.md)


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

## apiV1DemandsIdCertificateGetWithHttpInfo

> ApiResponse<File> apiV1DemandsIdCertificateGetWithHttpInfo(id, lang)

Tamamlanma sertifikası (PAdES B-T)

Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED sözleşmeler için üretilir (aksi 409). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        String lang = "lang_example"; // String | tr | en
        try {
            ApiResponse<File> response = apiInstance.apiV1DemandsIdCertificateGetWithHttpInfo(id, lang);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdCertificateGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **lang** | **String**| tr | en | [optional] |

### Return type

ApiResponse<[**File**](File.md)>


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


## apiV1DemandsIdDelete

> ApiV1TemplatesIdDelete200Response apiV1DemandsIdDelete(id)

Sözleşme sil (yalnızca tamamlanmamış)

Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. Arşivlenmiş sözleşme de silinemez → 409 &#x60;DEMAND_ARCHIVED&#x60;; önce &#x60;POST /api/v1/demands/{id}/unarchive&#x60; ile arşivden çıkarın. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiV1TemplatesIdDelete200Response result = apiInstance.apiV1DemandsIdDelete(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdDelete");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |

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
| **409** | Tamamlanmış (&#x60;DEMAND_COMPLETED&#x60;) veya arşivlenmiş (&#x60;DEMAND_ARCHIVED&#x60;) sözleşme silinemez |  -  |

## apiV1DemandsIdDeleteWithHttpInfo

> ApiResponse<ApiV1TemplatesIdDelete200Response> apiV1DemandsIdDeleteWithHttpInfo(id)

Sözleşme sil (yalnızca tamamlanmamış)

Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴 Tamamlanmış (COMPLETED) sözleşme API&#39;den SİLİNEMEZ (imzalı belge + denetim izi kaybı geri alınamaz) → 409 &#x60;DEMAND_COMPLETED&#x60;. Arşivlenmiş sözleşme de silinemez → 409 &#x60;DEMAND_ARCHIVED&#x60;; önce &#x60;POST /api/v1/demands/{id}/unarchive&#x60; ile arşivden çıkarın. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiResponse<ApiV1TemplatesIdDelete200Response> response = apiInstance.apiV1DemandsIdDeleteWithHttpInfo(id);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdDelete");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |

### Return type

ApiResponse<[**ApiV1TemplatesIdDelete200Response**](ApiV1TemplatesIdDelete200Response.md)>


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
| **409** | Tamamlanmış (&#x60;DEMAND_COMPLETED&#x60;) veya arşivlenmiş (&#x60;DEMAND_ARCHIVED&#x60;) sözleşme silinemez |  -  |


## apiV1DemandsIdEmbedSessionPost

> ApiV1DemandsIdEmbedSessionPost200Response apiV1DemandsIdEmbedSessionPost(id, apiV1DemandsIdEmbedSessionPostRequest)

Gömülü imza oturumu başlat (embed token mint)

Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla atılan dijital imza, 5070 sayılı Elektronik İmza Kanunu m.3 anlamında elektronik imzadır; güvenli elektronik imza değildir ve AB hukuku bakımından gelişmiş veya nitelikli imza olarak sunulmaz. Telefon doğrulaması, T.C. kimlik numarası kontrolü veya biyometrik adımlar gibi ek doğrulamalar imzanın delil değerini güçlendirir; imzayı gelişmiş ya da güvenli elektronik imzaya dönüştürmez. Güvenli elektronik imza gerekiyorsa bu akış yerine nitelikli elektronik sertifika ile imzalama akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). Kurum çalışma alanında sözleşme düzenleme ile aynı rol kuralı geçerlidir: OWNER ve ADMIN kurumun tüm sözleşmeleri için, MEMBER yalnız kendi oluşturduğu sözleşmeler için oturum alır. Erişim yoksa 404 döner. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | Sözleşme (demand) ID
        ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest = new ApiV1DemandsIdEmbedSessionPostRequest(); // ApiV1DemandsIdEmbedSessionPostRequest | 
        try {
            ApiV1DemandsIdEmbedSessionPost200Response result = apiInstance.apiV1DemandsIdEmbedSessionPost(id, apiV1DemandsIdEmbedSessionPostRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdEmbedSessionPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**| Sözleşme (demand) ID | |
| **apiV1DemandsIdEmbedSessionPostRequest** | [**ApiV1DemandsIdEmbedSessionPostRequest**](ApiV1DemandsIdEmbedSessionPostRequest.md)|  | |

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
| **400** | &#x60;party_id&#x60; eksik veya geçersiz format. &#x60;INVALID_LOCALE&#x60; (&#x60;code&#x60; alanında): &#x60;locale&#x60; &#x60;tr&#x60;, &#x60;en&#x60; veya &#x60;null&#x60; değil.  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **403** | **INSUFFICIENT_SCOPE** — API key&#39;in &#x60;demands&#x60; scope&#39;u yok veya gömülü imza özelliği bu API anahtarı için devre dışı.  |  -  |
| **404** | Sözleşme veya taraf bulunamadı. İki durum ayrıştırılmaz (IDOR koruması): - &#x60;Sözleşme bulunamadı&#x60; — demand bu workspace&#39;te yok - &#x60;Taraf bulunamadı&#x60; — party_id bu demand&#39;e ait değil  |  -  |
| **409** | Token üretilemez. Olası nedenler: - &#x60;Bu taraf zaten imzaladı&#x60; — taraf imzalamış - &#x60;Bu taraf imzayı reddetti&#x60; — taraf reddetmiş - &#x60;embed_allowed_origins tanımlı değil&#x60; — API anahtarında izin verilen   origin listesi boş; dashboard&#39;dan API anahtarı düzenleyerek ekleyin.  |  -  |
| **429** | Rate limit aşıldı (per API key + demand + party kombinasyonu) |  -  |

## apiV1DemandsIdEmbedSessionPostWithHttpInfo

> ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response> apiV1DemandsIdEmbedSessionPostWithHttpInfo(id, apiV1DemandsIdEmbedSessionPostRequest)

Gömülü imza oturumu başlat (embed token mint)

Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık gömülü imza token&#39;ı üretir. Dönen &#x60;embed_url&#x60; bir &#x60;&lt;iframe&gt;&#x60; içine yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.  **İmza sınıfı:** Bu akışla atılan dijital imza, 5070 sayılı Elektronik İmza Kanunu m.3 anlamında elektronik imzadır; güvenli elektronik imza değildir ve AB hukuku bakımından gelişmiş veya nitelikli imza olarak sunulmaz. Telefon doğrulaması, T.C. kimlik numarası kontrolü veya biyometrik adımlar gibi ek doğrulamalar imzanın delil değerini güçlendirir; imzayı gelişmiş ya da güvenli elektronik imzaya dönüştürmez. Güvenli elektronik imza gerekiyorsa bu akış yerine nitelikli elektronik sertifika ile imzalama akışını kullanın.  **Token özellikleri:** - Tek kullanımlık: imza sayfası açıldığında token tüketilir. - Kısa ömürlü: &#x60;expires_at&#x60; alanında belirtilen sürede geçersiz olur. - &#x60;embed_allowed_origins&#x60; kısıtı: API anahtarına tanımlanmış   izin verilen origin&#39;ler dışından &#x60;&lt;iframe&gt;&#x60; açılamaz (409 döner).  **Güvenlik katmanları:** - B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması) - B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması) - K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)  **Workspace izolasyonu:** &#x60;X-Workspace-Id&#x60; header&#39;ıyla yalnızca çağıran organizasyonun sözleşmelerine erişilebilir; başka workspace&#39;in sözleşmesi için 404 döner (IDOR koruması). Kurum çalışma alanında sözleşme düzenleme ile aynı rol kuralı geçerlidir: OWNER ve ADMIN kurumun tüm sözleşmeleri için, MEMBER yalnız kendi oluşturduğu sözleşmeler için oturum alır. Erişim yoksa 404 döner. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | Sözleşme (demand) ID
        ApiV1DemandsIdEmbedSessionPostRequest apiV1DemandsIdEmbedSessionPostRequest = new ApiV1DemandsIdEmbedSessionPostRequest(); // ApiV1DemandsIdEmbedSessionPostRequest | 
        try {
            ApiResponse<ApiV1DemandsIdEmbedSessionPost200Response> response = apiInstance.apiV1DemandsIdEmbedSessionPostWithHttpInfo(id, apiV1DemandsIdEmbedSessionPostRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdEmbedSessionPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**| Sözleşme (demand) ID | |
| **apiV1DemandsIdEmbedSessionPostRequest** | [**ApiV1DemandsIdEmbedSessionPostRequest**](ApiV1DemandsIdEmbedSessionPostRequest.md)|  | |

### Return type

ApiResponse<[**ApiV1DemandsIdEmbedSessionPost200Response**](ApiV1DemandsIdEmbedSessionPost200Response.md)>


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Token üretildi |  -  |
| **400** | &#x60;party_id&#x60; eksik veya geçersiz format. &#x60;INVALID_LOCALE&#x60; (&#x60;code&#x60; alanında): &#x60;locale&#x60; &#x60;tr&#x60;, &#x60;en&#x60; veya &#x60;null&#x60; değil.  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **403** | **INSUFFICIENT_SCOPE** — API key&#39;in &#x60;demands&#x60; scope&#39;u yok veya gömülü imza özelliği bu API anahtarı için devre dışı.  |  -  |
| **404** | Sözleşme veya taraf bulunamadı. İki durum ayrıştırılmaz (IDOR koruması): - &#x60;Sözleşme bulunamadı&#x60; — demand bu workspace&#39;te yok - &#x60;Taraf bulunamadı&#x60; — party_id bu demand&#39;e ait değil  |  -  |
| **409** | Token üretilemez. Olası nedenler: - &#x60;Bu taraf zaten imzaladı&#x60; — taraf imzalamış - &#x60;Bu taraf imzayı reddetti&#x60; — taraf reddetmiş - &#x60;embed_allowed_origins tanımlı değil&#x60; — API anahtarında izin verilen   origin listesi boş; dashboard&#39;dan API anahtarı düzenleyerek ekleyin.  |  -  |
| **429** | Rate limit aşıldı (per API key + demand + party kombinasyonu) |  -  |


## apiV1DemandsIdGet

> ApiV1DemandsIdGet200Response apiV1DemandsIdGet(id)

Sözleşme durumu + imza ilerlemesi

Kurum çalışma alanında erişim, sözleşme listesi ile aynı rol kuralını izler: OWNER ve ADMIN kurumun tüm sözleşmelerini, MEMBER yalnız kendi oluşturduğu sözleşmeleri görür. Erişim yoksa 404 döner. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiV1DemandsIdGet200Response result = apiInstance.apiV1DemandsIdGet(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |

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

## apiV1DemandsIdGetWithHttpInfo

> ApiResponse<ApiV1DemandsIdGet200Response> apiV1DemandsIdGetWithHttpInfo(id)

Sözleşme durumu + imza ilerlemesi

Kurum çalışma alanında erişim, sözleşme listesi ile aynı rol kuralını izler: OWNER ve ADMIN kurumun tüm sözleşmelerini, MEMBER yalnız kendi oluşturduğu sözleşmeleri görür. Erişim yoksa 404 döner. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiResponse<ApiV1DemandsIdGet200Response> response = apiInstance.apiV1DemandsIdGetWithHttpInfo(id);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |

### Return type

ApiResponse<[**ApiV1DemandsIdGet200Response**](ApiV1DemandsIdGet200Response.md)>


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


## apiV1DemandsIdItemsItemIdStampPatch

> PatchStampItemResponse apiV1DemandsIdItemsItemIdStampPatch(id, itemId, patchStampItemRequest)

Kaşe alanını doldur (kısmi güncelleme)

Sözleşmedeki var olan TEK bir kaşe alanını yapılandırılmış veriyle doldurur. &#x60;POST /api/v1/demands/{id}/items&#x60; tüm alanları yeniden yazar; bu uç ise yalnız belirtilen kaşeye dokunur, diğer alanlar değişmez.  Kaşe alanının kimliği (&#x60;itemId&#x60;) &#x60;GET /api/v1/demands/{id}&#x60; yanıtındaki &#x60;stamp_items[].item_id&#x60; alanından alınır.  ### Kısmi güncelleme kuralları  - &#x60;stamp_data&#x60; içinde gönderilen alan yazılır (baştaki/sondaki boşluk kırpılır). - &#x60;null&#x60; veya boş string gönderilen alan kaldırılır. - Gönderilmeyen alan olduğu gibi korunur. - Bilinmeyen alan veya string olmayan değer &#x60;400 INVALID_STAMP_DATA&#x60; döner.  ### Kaşe kaynağına göre davranış  - &#x60;FILLER_PROVIDES&#x60;: veri imzalayanın kaşesine önceden doldurulur;   zorunlu kaşe bu veriyle karşılanmış sayılır. İmzalayan imza   sayfasında bu değeri görür ve imzalamadan önce düzenleyebilir;   düzenlerse onun verisi kaydedilir. Kayıtta verinin gönderen   tarafından mı imzalayan tarafından mı girildiği ayrıca tutulur. - &#x60;INLINE&#x60;: kaşe verisi güncellenir. - &#x60;FROM_SAVED&#x60;: veri artık kayıtlı kaşeyle aynı olmadığı için kaşe   &#x60;INLINE&#x60; olur. Görsel kaşe (logo) varsa korunur ve belgede logo gösterilir.  ### Durum kontrolü  Yalnız &#x60;DRAFT&#x60; ve &#x60;PENDING&#x60; sözleşmeler güncellenebilir. Taraflardan biri imzaladıysa veya bir belge için karar verdiyse kaşe değiştirilemez.  ### Erişim  Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerinde, MEMBER yalnız kendi oluşturduğu sözleşmelerde kaşe doldurabilir. Erişim yoksa &#x60;404 DEMAND_NOT_FOUND&#x60; döner.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X PATCH https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items/$STAMP_ITEM_ID/stamp \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;stamp_data\&quot;: {       \&quot;companyName\&quot;: \&quot;Örnek Ltd.\&quot;,       \&quot;taxNumber\&quot;: \&quot;1234567890\&quot;,       \&quot;taxOffice\&quot;: \&quot;Kadıköy\&quot;,       \&quot;companyPhone\&quot;: \&quot;+905551112233\&quot;     }   }&#39; &#x60;&#x60;&#x60; 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        Integer itemId = 56; // Integer | Kaşe alanının kimliği (`stamp_items[].item_id`)
        PatchStampItemRequest patchStampItemRequest = new PatchStampItemRequest(); // PatchStampItemRequest | 
        try {
            PatchStampItemResponse result = apiInstance.apiV1DemandsIdItemsItemIdStampPatch(id, itemId, patchStampItemRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdItemsItemIdStampPatch");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **itemId** | **Integer**| Kaşe alanının kimliği (&#x60;stamp_items[].item_id&#x60;) | |
| **patchStampItemRequest** | [**PatchStampItemRequest**](PatchStampItemRequest.md)|  | |

### Return type

[**PatchStampItemResponse**](PatchStampItemResponse.md)


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Kaşe alanı güncellendi |  -  |
| **400** | Olası &#x60;error&#x60; değerleri: - &#x60;INVALID_ITEM_ID&#x60;: &#x60;itemId&#x60; pozitif tam sayı değil - &#x60;VALIDATION_ERROR&#x60;: gövde nesne değil, bilinmeyen gövde alanı veya geçersiz &#x60;document_id&#x60; - &#x60;INVALID_STAMP_DATA&#x60;: &#x60;stamp_data&#x60; boş, bilinmeyen alan veya string/null olmayan değer içeriyor - &#x60;NOT_A_STAMP_ITEM&#x60;: alan kaşe değil  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **403** | &#x60;DEMAND_NOT_EDITABLE&#x60;: sözleşme &#x60;DRAFT&#x60;/&#x60;PENDING&#x60; değil. &#x60;INSUFFICIENT_SCOPE&#x60;: API anahtarında &#x60;demands:write&#x60; yok.  |  -  |
| **404** | &#x60;DEMAND_NOT_FOUND&#x60;: sözleşme bu workspace&#39;te yok. &#x60;ITEM_NOT_FOUND&#x60;: alan bu sözleşmede (veya verilen &#x60;document_id&#x60; belgesinde) yok.  |  -  |
| **409** | &#x60;DEMAND_PARTIALLY_SIGNED&#x60;: taraflardan biri imzaladı veya karar verdi.  |  -  |

## apiV1DemandsIdItemsItemIdStampPatchWithHttpInfo

> ApiResponse<PatchStampItemResponse> apiV1DemandsIdItemsItemIdStampPatchWithHttpInfo(id, itemId, patchStampItemRequest)

Kaşe alanını doldur (kısmi güncelleme)

Sözleşmedeki var olan TEK bir kaşe alanını yapılandırılmış veriyle doldurur. &#x60;POST /api/v1/demands/{id}/items&#x60; tüm alanları yeniden yazar; bu uç ise yalnız belirtilen kaşeye dokunur, diğer alanlar değişmez.  Kaşe alanının kimliği (&#x60;itemId&#x60;) &#x60;GET /api/v1/demands/{id}&#x60; yanıtındaki &#x60;stamp_items[].item_id&#x60; alanından alınır.  ### Kısmi güncelleme kuralları  - &#x60;stamp_data&#x60; içinde gönderilen alan yazılır (baştaki/sondaki boşluk kırpılır). - &#x60;null&#x60; veya boş string gönderilen alan kaldırılır. - Gönderilmeyen alan olduğu gibi korunur. - Bilinmeyen alan veya string olmayan değer &#x60;400 INVALID_STAMP_DATA&#x60; döner.  ### Kaşe kaynağına göre davranış  - &#x60;FILLER_PROVIDES&#x60;: veri imzalayanın kaşesine önceden doldurulur;   zorunlu kaşe bu veriyle karşılanmış sayılır. İmzalayan imza   sayfasında bu değeri görür ve imzalamadan önce düzenleyebilir;   düzenlerse onun verisi kaydedilir. Kayıtta verinin gönderen   tarafından mı imzalayan tarafından mı girildiği ayrıca tutulur. - &#x60;INLINE&#x60;: kaşe verisi güncellenir. - &#x60;FROM_SAVED&#x60;: veri artık kayıtlı kaşeyle aynı olmadığı için kaşe   &#x60;INLINE&#x60; olur. Görsel kaşe (logo) varsa korunur ve belgede logo gösterilir.  ### Durum kontrolü  Yalnız &#x60;DRAFT&#x60; ve &#x60;PENDING&#x60; sözleşmeler güncellenebilir. Taraflardan biri imzaladıysa veya bir belge için karar verdiyse kaşe değiştirilemez.  ### Erişim  Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerinde, MEMBER yalnız kendi oluşturduğu sözleşmelerde kaşe doldurabilir. Erişim yoksa &#x60;404 DEMAND_NOT_FOUND&#x60; döner.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X PATCH https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items/$STAMP_ITEM_ID/stamp \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;stamp_data\&quot;: {       \&quot;companyName\&quot;: \&quot;Örnek Ltd.\&quot;,       \&quot;taxNumber\&quot;: \&quot;1234567890\&quot;,       \&quot;taxOffice\&quot;: \&quot;Kadıköy\&quot;,       \&quot;companyPhone\&quot;: \&quot;+905551112233\&quot;     }   }&#39; &#x60;&#x60;&#x60; 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        Integer itemId = 56; // Integer | Kaşe alanının kimliği (`stamp_items[].item_id`)
        PatchStampItemRequest patchStampItemRequest = new PatchStampItemRequest(); // PatchStampItemRequest | 
        try {
            ApiResponse<PatchStampItemResponse> response = apiInstance.apiV1DemandsIdItemsItemIdStampPatchWithHttpInfo(id, itemId, patchStampItemRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdItemsItemIdStampPatch");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **itemId** | **Integer**| Kaşe alanının kimliği (&#x60;stamp_items[].item_id&#x60;) | |
| **patchStampItemRequest** | [**PatchStampItemRequest**](PatchStampItemRequest.md)|  | |

### Return type

ApiResponse<[**PatchStampItemResponse**](PatchStampItemResponse.md)>


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Kaşe alanı güncellendi |  -  |
| **400** | Olası &#x60;error&#x60; değerleri: - &#x60;INVALID_ITEM_ID&#x60;: &#x60;itemId&#x60; pozitif tam sayı değil - &#x60;VALIDATION_ERROR&#x60;: gövde nesne değil, bilinmeyen gövde alanı veya geçersiz &#x60;document_id&#x60; - &#x60;INVALID_STAMP_DATA&#x60;: &#x60;stamp_data&#x60; boş, bilinmeyen alan veya string/null olmayan değer içeriyor - &#x60;NOT_A_STAMP_ITEM&#x60;: alan kaşe değil  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **403** | &#x60;DEMAND_NOT_EDITABLE&#x60;: sözleşme &#x60;DRAFT&#x60;/&#x60;PENDING&#x60; değil. &#x60;INSUFFICIENT_SCOPE&#x60;: API anahtarında &#x60;demands:write&#x60; yok.  |  -  |
| **404** | &#x60;DEMAND_NOT_FOUND&#x60;: sözleşme bu workspace&#39;te yok. &#x60;ITEM_NOT_FOUND&#x60;: alan bu sözleşmede (veya verilen &#x60;document_id&#x60; belgesinde) yok.  |  -  |
| **409** | &#x60;DEMAND_PARTIALLY_SIGNED&#x60;: taraflardan biri imzaladı veya karar verdi.  |  -  |


## apiV1DemandsIdItemsPost

> UpsertItemsResponse apiV1DemandsIdItemsPost(id, upsertItemsRequest)

Sözleşmeye alan yerleştir (replace)

Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |-------------|---------------------|-----------------| | &#x60;signature&#x60; | ✅ | (yok) veya &#x60;{ requireFirstSignatureOtp: true }&#x60; | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | Yalnız &#x60;source: FILLER_PROVIDES&#x60; ise (zorunlu kaşede şart) | &#x60;{ source: \&quot;INLINE\&quot;, stampData: { companyName: \&quot;Örnek Ltd.\&quot;, taxNumber: \&quot;1234567890\&quot; } }&#x60; |  ### Kaşe (&#x60;stamp&#x60;) alanı  - &#x60;config.source&#x60;: &#x60;INLINE&#x60; (varsayılan, kaşe verisi gönderenden gelir),   &#x60;FROM_SAVED&#x60; (kayıtlı kaşeden kopya) veya &#x60;FILLER_PROVIDES&#x60; (kaşeyi   &#x60;party_id&#x60; ile belirtilen imzalayan doldurur). - &#x60;config.stampData&#x60;: yapılandırılmış kaşe verisi. İzinli alanlar:   &#x60;companyName&#x60;, &#x60;personalName&#x60;, &#x60;address&#x60;, &#x60;companyAddress&#x60;,   &#x60;personalAddress&#x60;, &#x60;taxNumber&#x60;, &#x60;taxOffice&#x60;, &#x60;idNumber&#x60;, &#x60;phone&#x60;,   &#x60;companyPhone&#x60;, &#x60;personalPhone&#x60;, &#x60;email&#x60; (hepsi string). Bilinmeyen alan   veya string olmayan değer &#x60;400 INVALID_STAMP_DATA&#x60; döner. Eski   dokümandaki data URL string biçimi geriye dönük uyum için hâlâ kabul   edilir, ancak belgede kaşe olarak çizilmez; yapılandırılmış nesneyi kullanın. - &#x60;config.isRequired: true&#x60; yalnız &#x60;FILLER_PROVIDES&#x60; kaşede anlamlıdır:   imzalayan kaşeyi doldurmadan imzalayamaz. Zorunlu kaşenin &#x60;party_id&#x60;&#39;si   olmak zorundadır; çok belgeli zarfta taraf, kaşenin bulunduğu belgeye   atanmış olmalıdır. - Var olan tek bir kaşeyi diğer alanlara dokunmadan doldurmak için bu   uç yerine &#x60;PATCH /api/v1/demands/{id}/items/{itemId}/stamp&#x60; kullanın.  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner). Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerine, MEMBER yalnız kendi oluşturduğu sözleşmelere alan yerleştirebilir. Erişim yoksa 404 döner.  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        UpsertItemsRequest upsertItemsRequest = new UpsertItemsRequest(); // UpsertItemsRequest | 
        try {
            UpsertItemsResponse result = apiInstance.apiV1DemandsIdItemsPost(id, upsertItemsRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdItemsPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **upsertItemsRequest** | [**UpsertItemsRequest**](UpsertItemsRequest.md)|  | |

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
| **400** | Validation hatası. Olası &#x60;error&#x60; değerleri: - &#x60;INVALID_ITEMS_BODY&#x60; — items array değil - &#x60;VALIDATION_ERROR&#x60; — position bounds, slug regex, party-required - &#x60;INVALID_PAGE_ID&#x60; — page_id demand&#39;e ait değil veya page_ids&#39;te yok - &#x60;INVALID_PARTY_ID&#x60; — party_id demand&#39;e ait değil - &#x60;PARTY_NOT_ASSIGNED_TO_DOCUMENT&#x60;: kaşeyi dolduracak taraf, kaşenin belgesine atanmamış - &#x60;INVALID_STAMP_DATA&#x60;: &#x60;config.stampData&#x60; bilinmeyen alan veya string olmayan değer içeriyor - &#x60;PAGE_ID_REQUIRED&#x60;: bir öğede tam sayı &#x60;page_id&#x60; yok - &#x60;INVALID_ITEM_TYPE&#x60;: desteklenmeyen &#x60;item_type&#x60; (mesaj izinli türleri listeler)  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **403** | &#x60;DEMAND_NOT_EDITABLE&#x60; — demand status ≠ &#x60;PENDING&#x60; (COMPLETED, EXPIRED, REJECTED edit edilemez).  |  -  |
| **404** | &#x60;DEMAND_NOT_FOUND&#x60; — demand bu workspace&#39;te yok (cross-workspace IDOR koruması).  |  -  |
| **409** | &#x60;DUPLICATE_SIGNATURE_FIELD&#x60; — aynı &#x60;(page_id, party_id, position_x, position_y)&#x60; tuple&#39;ında ikinci &#x60;signature&#x60; alanı yaratıldı. DB-level partial unique constraint engelledi. Pozisyonu değiştirip tekrar deneyin.  |  -  |

## apiV1DemandsIdItemsPostWithHttpInfo

> ApiResponse<UpsertItemsResponse> apiV1DemandsIdItemsPostWithHttpInfo(id, upsertItemsRequest)

Sözleşmeye alan yerleştir (replace)

Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla yerleştirir. Tipik kullanım: &#x60;POST /api/v1/demands/upload&#x60; ile demand yarat (&#x60;dispatch_notifications&#x3D;false&#x60; ile auto-dispatch&#39;i ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden ya da &#x60;POST /api/v1/demands/{id}/reminders&#x60; ile gönderim başlat.  ### Replace mode  Endpoint **replace** semantiği taşır: - &#x60;page_ids&#x60; **omitted** → demand&#39;in TÜM mevcut item&#39;ları silinir,   body&#39;dekiler yaratılır (full replace). - &#x60;page_ids: [N, M, ...]&#x60; → sadece bu sayfaların item&#39;ları silinir,   diğer sayfalardaki item&#39;lar korunur. Body&#39;deki &#x60;items[].page_id&#x60;   değerleri &#x60;page_ids&#x60; listesinde olmalıdır.  ### Item type&#39;ları  | &#x60;item_type&#x60; | &#x60;party_id&#x60; zorunlu? | &#x60;config&#x60; örneği | |-------------|---------------------|-----------------| | &#x60;signature&#x60; | ✅ | (yok) veya &#x60;{ requireFirstSignatureOtp: true }&#x60; | | &#x60;text&#x60; | ❌ | &#x60;{ default_content }&#x60; | | &#x60;dynamic_text&#x60; | ✅ | &#x60;{ defaultSource: \&quot;{{signer.full_name}}\&quot; }&#x60; | | &#x60;cells&#x60; | ✅ | &#x60;{ cellCount: 11, defaultSource: \&quot;{{signer.government_id}}\&quot; }&#x60; | | &#x60;date&#x60; | ✅ | &#x60;{ defaultSource, defaultValue }&#x60; | | &#x60;dropdown&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;checkbox&#x60; | ✅ | &#x60;{ checkedByDefault: false }&#x60; | | &#x60;radio&#x60; | ✅ | &#x60;{ options: [{label,value}], defaultValue }&#x60; | | &#x60;stamp&#x60; | Yalnız &#x60;source: FILLER_PROVIDES&#x60; ise (zorunlu kaşede şart) | &#x60;{ source: \&quot;INLINE\&quot;, stampData: { companyName: \&quot;Örnek Ltd.\&quot;, taxNumber: \&quot;1234567890\&quot; } }&#x60; |  ### Kaşe (&#x60;stamp&#x60;) alanı  - &#x60;config.source&#x60;: &#x60;INLINE&#x60; (varsayılan, kaşe verisi gönderenden gelir),   &#x60;FROM_SAVED&#x60; (kayıtlı kaşeden kopya) veya &#x60;FILLER_PROVIDES&#x60; (kaşeyi   &#x60;party_id&#x60; ile belirtilen imzalayan doldurur). - &#x60;config.stampData&#x60;: yapılandırılmış kaşe verisi. İzinli alanlar:   &#x60;companyName&#x60;, &#x60;personalName&#x60;, &#x60;address&#x60;, &#x60;companyAddress&#x60;,   &#x60;personalAddress&#x60;, &#x60;taxNumber&#x60;, &#x60;taxOffice&#x60;, &#x60;idNumber&#x60;, &#x60;phone&#x60;,   &#x60;companyPhone&#x60;, &#x60;personalPhone&#x60;, &#x60;email&#x60; (hepsi string). Bilinmeyen alan   veya string olmayan değer &#x60;400 INVALID_STAMP_DATA&#x60; döner. Eski   dokümandaki data URL string biçimi geriye dönük uyum için hâlâ kabul   edilir, ancak belgede kaşe olarak çizilmez; yapılandırılmış nesneyi kullanın. - &#x60;config.isRequired: true&#x60; yalnız &#x60;FILLER_PROVIDES&#x60; kaşede anlamlıdır:   imzalayan kaşeyi doldurmadan imzalayamaz. Zorunlu kaşenin &#x60;party_id&#x60;&#39;si   olmak zorundadır; çok belgeli zarfta taraf, kaşenin bulunduğu belgeye   atanmış olmalıdır. - Var olan tek bir kaşeyi diğer alanlara dokunmadan doldurmak için bu   uç yerine &#x60;PATCH /api/v1/demands/{id}/items/{itemId}/stamp&#x60; kullanın.  ### Sistem değişkenleri (dynamic_text/cells/date &#x60;config.defaultSource&#x60;)  &#x60;{{signer.first_name}}&#x60;, &#x60;{{signer.last_name}}&#x60;, &#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.email}}&#x60;, &#x60;{{signer.phone}}&#x60;, &#x60;{{signer.government_id}}&#x60;, &#x60;{{signer.birth_date}}&#x60;, &#x60;{{signer.sign_date}}&#x60;, &#x60;{{contract.title}}&#x60;, &#x60;{{sender.full_name}}&#x60;, &#x60;{{current.date}}&#x60;, &#x60;{{current.datetime}}&#x60;.  ### Workspace izolasyonu  X-API-Key middleware demand&#39;i workspace&#39;e göre filtreler; başka workspace&#39;in demand&#39;ine item ekleyemezsiniz (404 döner). Kurum çalışma alanında OWNER ve ADMIN kurumun tüm sözleşmelerine, MEMBER yalnız kendi oluşturduğu sözleşmelere alan yerleştirebilir. Erişim yoksa 404 döner.  ### Status kontrolü  Sadece &#x60;PENDING&#x60; demand edit edilebilir. &#x60;COMPLETED&#x60;, &#x60;EXPIRED&#x60;, &#x60;REJECTED&#x60; için 403.  ### Örnek  &#x60;&#x60;&#x60;bash curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \\   -H \&quot;X-API-Key: imz_...\&quot; \\   -H \&quot;Content-Type: application/json\&quot; \\   -d &#39;{     \&quot;items\&quot;: [       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;signature\&quot;,         \&quot;position_x\&quot;: 0.5, \&quot;position_y\&quot;: 0.85,         \&quot;width\&quot;: 0.2, \&quot;height\&quot;: 0.05,         \&quot;is_required\&quot;: true       },       {         \&quot;page_id\&quot;: 12345,         \&quot;party_id\&quot;: \&quot;f47ac10b-58cc-4372-a567-0e02b2c3d479\&quot;,         \&quot;item_type\&quot;: \&quot;cells\&quot;,         \&quot;position_x\&quot;: 0.1, \&quot;position_y\&quot;: 0.5,         \&quot;width\&quot;: 0.4, \&quot;height\&quot;: 0.04,         \&quot;slug\&quot;: \&quot;tc\&quot;,         \&quot;config\&quot;: { \&quot;cellCount\&quot;: 11, \&quot;defaultSource\&quot;: \&quot;{{signer.government_id}}\&quot; }       }     ]   }&#39; &#x60;&#x60;&#x60; 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        UpsertItemsRequest upsertItemsRequest = new UpsertItemsRequest(); // UpsertItemsRequest | 
        try {
            ApiResponse<UpsertItemsResponse> response = apiInstance.apiV1DemandsIdItemsPostWithHttpInfo(id, upsertItemsRequest);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdItemsPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **upsertItemsRequest** | [**UpsertItemsRequest**](UpsertItemsRequest.md)|  | |

### Return type

ApiResponse<[**UpsertItemsResponse**](UpsertItemsResponse.md)>


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Alanlar yerleştirildi |  -  |
| **400** | Validation hatası. Olası &#x60;error&#x60; değerleri: - &#x60;INVALID_ITEMS_BODY&#x60; — items array değil - &#x60;VALIDATION_ERROR&#x60; — position bounds, slug regex, party-required - &#x60;INVALID_PAGE_ID&#x60; — page_id demand&#39;e ait değil veya page_ids&#39;te yok - &#x60;INVALID_PARTY_ID&#x60; — party_id demand&#39;e ait değil - &#x60;PARTY_NOT_ASSIGNED_TO_DOCUMENT&#x60;: kaşeyi dolduracak taraf, kaşenin belgesine atanmamış - &#x60;INVALID_STAMP_DATA&#x60;: &#x60;config.stampData&#x60; bilinmeyen alan veya string olmayan değer içeriyor - &#x60;PAGE_ID_REQUIRED&#x60;: bir öğede tam sayı &#x60;page_id&#x60; yok - &#x60;INVALID_ITEM_TYPE&#x60;: desteklenmeyen &#x60;item_type&#x60; (mesaj izinli türleri listeler)  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **403** | &#x60;DEMAND_NOT_EDITABLE&#x60; — demand status ≠ &#x60;PENDING&#x60; (COMPLETED, EXPIRED, REJECTED edit edilemez).  |  -  |
| **404** | &#x60;DEMAND_NOT_FOUND&#x60; — demand bu workspace&#39;te yok (cross-workspace IDOR koruması).  |  -  |
| **409** | &#x60;DUPLICATE_SIGNATURE_FIELD&#x60; — aynı &#x60;(page_id, party_id, position_x, position_y)&#x60; tuple&#39;ında ikinci &#x60;signature&#x60; alanı yaratıldı. DB-level partial unique constraint engelledi. Pozisyonu değiştirip tekrar deneyin.  |  -  |


## apiV1DemandsIdPartiesPartyIdResendPost

> ApiV1DemandsIdPartiesPartyIdResendPost200Response apiV1DemandsIdPartiesPartyIdResendPost(id, partyId)

Tekil tarafa imza davetini tekrar gönder

Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa, onayını vermiş onaylayana, açık düzeltme talebi olan tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). Sıralı imzada onaylayanın onayı o adımı tamamlar; sıra bir sonraki tarafa geçer. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        UUID partyId = UUID.randomUUID(); // UUID | 
        try {
            ApiV1DemandsIdPartiesPartyIdResendPost200Response result = apiInstance.apiV1DemandsIdPartiesPartyIdResendPost(id, partyId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdPartiesPartyIdResendPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **partyId** | **UUID**|  | |

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
| **409** | Tekrar gönderim yapılamaz. &#x60;error&#x60; insan-okur mesajı taşır. - İmzalamış veya reddetmiş taraf (&#x60;code&#x60; alanı yok) - &#x60;CHANGE_REQUEST_PENDING&#x60;: tarafın açık bir düzeltme talebi var;   talep çözüldükten sonra tekrar gönderilebilir - &#x60;PARTY_NOT_ELIGIBLE&#x60;: tarafa şu an davet gönderilemez. Şu   durumlarda döner: taraf zaten onay vermiş bir onaylayan   olduğunda, sözleşme bir onaylayanın reddiyle durdurulduğunda,   onaylayan katmanının onayı tamamlanmadan imzacıya gönderim   denendiğinde veya sıralı imzada henüz bu tarafın sırası   gelmediğinde. - &#x60;DEMAND_NOT_DISPATCHED&#x60;: sözleşme henüz imzaya gönderilmedi (taslak) - &#x60;DEMAND_NOT_DISPATCHABLE&#x60;: sözleşme tamamlanmış veya iptal edilmiş - &#x60;DEMAND_EXPIRED&#x60;: sözleşmenin imza süresi geçmiş - &#x60;ENVELOPE_NOT_DISPATCHED&#x60;: çok belgeli zarf henüz gönderilmedi.   Önce &#x60;POST /demands/{id}/dispatch&#x60; çağırın; tek belgeli ve   gönderilmiş sözleşmelerde bu kod hiç dönmez.  |  -  |
| **429** | İki ayrı sınır vardır.  &#x60;TOO_MANY_REQUESTS&#x60;: API anahtarı başına saatte 300 istek (dispatch ucuyla ortak sınır). Gövdede &#x60;retry_after_seconds&#x60; döner.  &#x60;RECIPIENT_RESEND_LIMIT&#x60;: aynı alıcıya saatte en fazla 3, günde en fazla 10 davet tekrarı gönderilebilir.  |  -  |

## apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo

> ApiResponse<ApiV1DemandsIdPartiesPartyIdResendPost200Response> apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo(id, partyId)

Tekil tarafa imza davetini tekrar gönder

Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa, onayını vermiş onaylayana, açık düzeltme talebi olan tarafa veya sıralı imzada sırası gelmemiş tarafa gönderilemez (409). Sıralı imzada onaylayanın onayı o adımı tamamlar; sıra bir sonraki tarafa geçer. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        UUID partyId = UUID.randomUUID(); // UUID | 
        try {
            ApiResponse<ApiV1DemandsIdPartiesPartyIdResendPost200Response> response = apiInstance.apiV1DemandsIdPartiesPartyIdResendPostWithHttpInfo(id, partyId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdPartiesPartyIdResendPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **partyId** | **UUID**|  | |

### Return type

ApiResponse<[**ApiV1DemandsIdPartiesPartyIdResendPost200Response**](ApiV1DemandsIdPartiesPartyIdResendPost200Response.md)>


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
| **409** | Tekrar gönderim yapılamaz. &#x60;error&#x60; insan-okur mesajı taşır. - İmzalamış veya reddetmiş taraf (&#x60;code&#x60; alanı yok) - &#x60;CHANGE_REQUEST_PENDING&#x60;: tarafın açık bir düzeltme talebi var;   talep çözüldükten sonra tekrar gönderilebilir - &#x60;PARTY_NOT_ELIGIBLE&#x60;: tarafa şu an davet gönderilemez. Şu   durumlarda döner: taraf zaten onay vermiş bir onaylayan   olduğunda, sözleşme bir onaylayanın reddiyle durdurulduğunda,   onaylayan katmanının onayı tamamlanmadan imzacıya gönderim   denendiğinde veya sıralı imzada henüz bu tarafın sırası   gelmediğinde. - &#x60;DEMAND_NOT_DISPATCHED&#x60;: sözleşme henüz imzaya gönderilmedi (taslak) - &#x60;DEMAND_NOT_DISPATCHABLE&#x60;: sözleşme tamamlanmış veya iptal edilmiş - &#x60;DEMAND_EXPIRED&#x60;: sözleşmenin imza süresi geçmiş - &#x60;ENVELOPE_NOT_DISPATCHED&#x60;: çok belgeli zarf henüz gönderilmedi.   Önce &#x60;POST /demands/{id}/dispatch&#x60; çağırın; tek belgeli ve   gönderilmiş sözleşmelerde bu kod hiç dönmez.  |  -  |
| **429** | İki ayrı sınır vardır.  &#x60;TOO_MANY_REQUESTS&#x60;: API anahtarı başına saatte 300 istek (dispatch ucuyla ortak sınır). Gövdede &#x60;retry_after_seconds&#x60; döner.  &#x60;RECIPIENT_RESEND_LIMIT&#x60;: aynı alıcıya saatte en fazla 3, günde en fazla 10 davet tekrarı gönderilebilir.  |  -  |


## apiV1DemandsIdPdfGet

> File apiV1DemandsIdPdfGet(id)

İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)

Tamamlanmış sözleşmenin imzalı PDF&#39;ini indirir. Public &#x60;/sonuc/{id}/pdf&#x60;&#39;in aksine API key ownership&#39;i zorunludur. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            File result = apiInstance.apiV1DemandsIdPdfGet(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdPdfGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |

### Return type

[**File**](File.md)


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

## apiV1DemandsIdPdfGetWithHttpInfo

> ApiResponse<File> apiV1DemandsIdPdfGetWithHttpInfo(id)

İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)

Tamamlanmış sözleşmenin imzalı PDF&#39;ini indirir. Public &#x60;/sonuc/{id}/pdf&#x60;&#39;in aksine API key ownership&#39;i zorunludur. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiResponse<File> response = apiInstance.apiV1DemandsIdPdfGetWithHttpInfo(id);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdPdfGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |

### Return type

ApiResponse<[**File**](File.md)>


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


## apiV1DemandsIdTermPatch

> ApiV1DemandsIdTermPatch200Response apiV1DemandsIdTermPatch(id, contractTermInput)

Sözleşme süre/yenileme takibini güncelle

Kısmi güncelleme: yalnız gövdede gönderilen &#x60;ContractTermInput&#x60; anahtarları değiştirilir; bir anahtarı &#x60;null&#x60; göndermek o alanı temizler. Dashboard&#39;daki sözleşme detay sayfasıyla AYNI çekirdek kuralı uygular.  Bitişi etkileyen bir alan (&#x60;term_start_mode&#x60;, &#x60;term_start_date&#x60;, &#x60;term_duration_months&#x60;, &#x60;term_fixed_end_date&#x60;) kayıttakinden farklı bir DEĞERLE gönderilirse &#x60;term_end_date&#x60; bilinen başlangıçtan yeniden hesaplanır; aynı değerle gelen alan no-op&#39;tur (otomatik olarak ileri alınmış bitiş korunur). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        ContractTermInput contractTermInput = new ContractTermInput(); // ContractTermInput | 
        try {
            ApiV1DemandsIdTermPatch200Response result = apiInstance.apiV1DemandsIdTermPatch(id, contractTermInput);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdTermPatch");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **contractTermInput** | [**ContractTermInput**](ContractTermInput.md)|  | |

### Return type

[**ApiV1DemandsIdTermPatch200Response**](ApiV1DemandsIdTermPatch200Response.md)


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Güncellendi |  -  |
| **400** | &#x60;TERM_INVALID&#x60;: geçersiz veya çelişkili alan kombinasyonu, ya da gövdede hiç term alanı yok. &#x60;field&#x60; hangi anahtarın reddedildiğini gösterir (gövde boşsa &#x60;null&#x60;).  |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | Arşivlenmiş sözleşme değiştirilemez (&#x60;DEMAND_ARCHIVED&#x60;); önce &#x60;POST .../unarchive&#x60; çağırın. |  -  |

## apiV1DemandsIdTermPatchWithHttpInfo

> ApiResponse<ApiV1DemandsIdTermPatch200Response> apiV1DemandsIdTermPatchWithHttpInfo(id, contractTermInput)

Sözleşme süre/yenileme takibini güncelle

Kısmi güncelleme: yalnız gövdede gönderilen &#x60;ContractTermInput&#x60; anahtarları değiştirilir; bir anahtarı &#x60;null&#x60; göndermek o alanı temizler. Dashboard&#39;daki sözleşme detay sayfasıyla AYNI çekirdek kuralı uygular.  Bitişi etkileyen bir alan (&#x60;term_start_mode&#x60;, &#x60;term_start_date&#x60;, &#x60;term_duration_months&#x60;, &#x60;term_fixed_end_date&#x60;) kayıttakinden farklı bir DEĞERLE gönderilirse &#x60;term_end_date&#x60; bilinen başlangıçtan yeniden hesaplanır; aynı değerle gelen alan no-op&#39;tur (otomatik olarak ileri alınmış bitiş korunur). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        ContractTermInput contractTermInput = new ContractTermInput(); // ContractTermInput | 
        try {
            ApiResponse<ApiV1DemandsIdTermPatch200Response> response = apiInstance.apiV1DemandsIdTermPatchWithHttpInfo(id, contractTermInput);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdTermPatch");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |
| **contractTermInput** | [**ContractTermInput**](ContractTermInput.md)|  | |

### Return type

ApiResponse<[**ApiV1DemandsIdTermPatch200Response**](ApiV1DemandsIdTermPatch200Response.md)>


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Güncellendi |  -  |
| **400** | &#x60;TERM_INVALID&#x60;: geçersiz veya çelişkili alan kombinasyonu, ya da gövdede hiç term alanı yok. &#x60;field&#x60; hangi anahtarın reddedildiğini gösterir (gövde boşsa &#x60;null&#x60;).  |  -  |
| **404** | Kayıt bulunamadı |  -  |
| **409** | Arşivlenmiş sözleşme değiştirilemez (&#x60;DEMAND_ARCHIVED&#x60;); önce &#x60;POST .../unarchive&#x60; çağırın. |  -  |


## apiV1DemandsIdTimelineGet

> ApiV1DemandsIdTimelineGet200Response apiV1DemandsIdTimelineGet(id)

İmza denetim izi (maskeli)

Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez.  &#x60;event_type&#x60; değerleri: &#x60;CREATED&#x60;, &#x60;SENT&#x60;, &#x60;VIEWED&#x60;, &#x60;FIELDS_FILLED&#x60;, &#x60;COMMENT_ADDED&#x60;, &#x60;SIGNED&#x60;, &#x60;APPROVED&#x60;, &#x60;REJECTED&#x60;, &#x60;TIMESTAMPED&#x60;, &#x60;COMPLETED&#x60;, &#x60;OTP_SENT&#x60; (SMS doğrulama kodu gönderildi), &#x60;OTP_VERIFIED&#x60; (SMS doğrulama kodu doğrulandı), &#x60;OTP_LOCKED&#x60; (deneme sınırı doldu), &#x60;MOBILE_SIGNATURE_CAPTURED&#x60; (imza QR kod ile telefonda çizildi; bu olayın &#x60;ip_masked&#x60; ve &#x60;device_label&#x60; alanları telefona aittir), &#x60;REAPPROVAL_REQUIRED&#x60; (gönderen açık bir değişiklik talebi sırasında sözleşme içeriğini güncelledi, önceki onaylar sıfırlandı ve onaylayanların yeniden onay vermesi gerekiyor). Liste ileride genişleyebilir; tanımadığınız değeri yok saymanız önerilir. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiV1DemandsIdTimelineGet200Response result = apiInstance.apiV1DemandsIdTimelineGet(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdTimelineGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |

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

## apiV1DemandsIdTimelineGetWithHttpInfo

> ApiResponse<ApiV1DemandsIdTimelineGet200Response> apiV1DemandsIdTimelineGetWithHttpInfo(id)

İmza denetim izi (maskeli)

Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner. KVKK: IP &#x60;ip_masked&#x60; (son oktet maskeli), actor e-postası maskeli; ham IP/cihaz asla döndürülmez.  &#x60;event_type&#x60; değerleri: &#x60;CREATED&#x60;, &#x60;SENT&#x60;, &#x60;VIEWED&#x60;, &#x60;FIELDS_FILLED&#x60;, &#x60;COMMENT_ADDED&#x60;, &#x60;SIGNED&#x60;, &#x60;APPROVED&#x60;, &#x60;REJECTED&#x60;, &#x60;TIMESTAMPED&#x60;, &#x60;COMPLETED&#x60;, &#x60;OTP_SENT&#x60; (SMS doğrulama kodu gönderildi), &#x60;OTP_VERIFIED&#x60; (SMS doğrulama kodu doğrulandı), &#x60;OTP_LOCKED&#x60; (deneme sınırı doldu), &#x60;MOBILE_SIGNATURE_CAPTURED&#x60; (imza QR kod ile telefonda çizildi; bu olayın &#x60;ip_masked&#x60; ve &#x60;device_label&#x60; alanları telefona aittir), &#x60;REAPPROVAL_REQUIRED&#x60; (gönderen açık bir değişiklik talebi sırasında sözleşme içeriğini güncelledi, önceki onaylar sıfırlandı ve onaylayanların yeniden onay vermesi gerekiyor). Liste ileride genişleyebilir; tanımadığınız değeri yok saymanız önerilir. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiResponse<ApiV1DemandsIdTimelineGet200Response> response = apiInstance.apiV1DemandsIdTimelineGetWithHttpInfo(id);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdTimelineGet");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |

### Return type

ApiResponse<[**ApiV1DemandsIdTimelineGet200Response**](ApiV1DemandsIdTimelineGet200Response.md)>


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


## apiV1DemandsIdUnarchivePost

> ApiV1DemandsIdUnarchivePost200Response apiV1DemandsIdUnarchivePost(id)

Sözleşmeyi arşivden çıkar

&#x60;archived_at&#x60;&#39;i temizler; sözleşme yeniden mutasyona açılır. Zaten arşivsizse idempotent (200, &#x60;archived_at: null&#x60;). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiV1DemandsIdUnarchivePost200Response result = apiInstance.apiV1DemandsIdUnarchivePost(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdUnarchivePost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |

### Return type

[**ApiV1DemandsIdUnarchivePost200Response**](ApiV1DemandsIdUnarchivePost200Response.md)


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Arşivden çıkarıldı (veya zaten arşivsizdi) |  -  |
| **404** | Kayıt bulunamadı |  -  |

## apiV1DemandsIdUnarchivePostWithHttpInfo

> ApiResponse<ApiV1DemandsIdUnarchivePost200Response> apiV1DemandsIdUnarchivePostWithHttpInfo(id)

Sözleşmeyi arşivden çıkar

&#x60;archived_at&#x60;&#39;i temizler; sözleşme yeniden mutasyona açılır. Zaten arşivsizse idempotent (200, &#x60;archived_at: null&#x60;). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | 
        try {
            ApiResponse<ApiV1DemandsIdUnarchivePost200Response> response = apiInstance.apiV1DemandsIdUnarchivePostWithHttpInfo(id);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsIdUnarchivePost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**|  | |

### Return type

ApiResponse<[**ApiV1DemandsIdUnarchivePost200Response**](ApiV1DemandsIdUnarchivePost200Response.md)>


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Arşivden çıkarıldı (veya zaten arşivsizdi) |  -  |
| **404** | Kayıt bulunamadı |  -  |


## apiV1DemandsPost

> ApiV1DemandsPost201Response apiV1DemandsPost(createDemandRequest, idempotencyKey)

Sözleşme oluştur (şablondan)

Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa oluşturma anında yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez. Ayrıntı: açıklamadaki \&quot;Onaylayan (onay adımı)\&quot; bölümü.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı  **Kredi (çok belgeli zarf):** Şablon çok-belgeli bir zarf tanımlıyorsa (her belge farklı imzacı(lar)a atanabilir), imzacı başına kredi o imzacıya ATANMIŞ belge sayısına göre hesaplanır ve belge sayısına göre azalan birim fiyatlı bir indirim uygulanır: 1 belge %0, 2 belge %30, 3 ve üzeri %50 (indirim imzacı başına ayrı hesaplanır ve yukarı yuvarlanır). AB nitelikli zaman damgası (&#x60;eidas_timestamp&#x60;) seçiliyse aynı indirim zarftaki farklı belge sayısına göre toplam üzerinden uygulanır. Tek belgeli şablonlarda tutar eski formülle bayt-aynıdır.  **Atamasız imzacı:** &#x60;dispatch_notifications&#x60; &#x60;false&#x60; gönderilmediği sürece, eşlenen bir taraf şablonun hiçbir belgesine atanmamışsa sözleşme HİÇ oluşturulmaz (409 &#x60;PARTY_WITHOUT_DOCUMENTS&#x60;). Bu kontrolü atlamak için &#x60;dispatch_notifications: false&#x60; gönderip belge atamalarını &#x60;PUT .../documents/{docId}/assignments&#x60; ile düzelttikten sonra &#x60;POST .../dispatch&#x60; ile gönderin (o uç aynı kapıyı yeniden uygular). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        CreateDemandRequest createDemandRequest = new CreateDemandRequest(); // CreateDemandRequest | 
        String idempotencyKey = "idempotencyKey_example"; // String | Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
        try {
            ApiV1DemandsPost201Response result = apiInstance.apiV1DemandsPost(createDemandRequest, idempotencyKey);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **createDemandRequest** | [**CreateDemandRequest**](CreateDemandRequest.md)|  | |
| **idempotencyKey** | **String**| Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  | [optional] |

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
| **400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate&#39;i bulundu: &lt;id&gt;\&quot;  &#x60;INVALID_EXPIRY_DATE&#x60;: &#x60;expiry_date&#x60; çözümlenemiyor veya takvimde olmayan bir gün (ör. &#x60;2026-02-30&#x60;). Bu hatada &#x60;error&#x60; insan-okur mesajı, &#x60;code&#x60; alanı makinece okunur kodu taşır.  &#x60;INVALID_LOCALE&#x60;: &#x60;party_mapping[i].locale&#x60; &#x60;tr&#x60;, &#x60;en&#x60; veya &#x60;null&#x60; değil. &#x60;error&#x60; hatalı alanın yolunu taşır; sözleşme oluşturulmaz, kredi düşülmez.  &#x60;INVALID_DOCUMENT_SELECTION&#x60;: belge seçimi hatası; &#x60;details.reason&#x60; şu değerlerden biridir: &#x60;shape&#x60; (biçim: nesne değil, bilinmeyen alan, kimlik metin değil, 20&#39;den fazla kimlik), &#x60;unknown_document&#x60; (kimlik bu şablonun belgesi değil), &#x60;conflict&#x60; (aynı kimlik iki listede), &#x60;empty&#x60; (seçim sonucunda belge kalmadı). Biçim hataları kredi kontrolünden önce, şablona bağlı hatalar şablon erişim kontrolünden sonra ve kredi kontrolünden önce döner.  &#x60;INVALID_DOCUMENT_VARIABLES&#x60;: &#x60;document_variables&#x60; hatası; &#x60;details.reason&#x60; şu değerlerden biridir: &#x60;shape&#x60; (biçim: nesne değil, değer string|number|boolean|null değil, boş ya da ayrılmış anahtar, 20&#39;den fazla belge), &#x60;unknown_document&#x60; (kimlik bu şablonun belgesi değil), &#x60;unselected_document&#x60; (belge bu istekte gönderilmiyor; &#x60;documents.include&#x60; ile ekleyin). &#x60;details.path&#x60; hatalı alanı (&#x60;document_variables&#x60; ya da &#x60;party_mapping[i].document_variables&#x60;), &#x60;details.document_ids&#x60; yalnız gönderdiğiniz kimlikleri taşır. Biçim hataları kredi kontrolünden önce, şablona bağlı hatalar şablon erişim kontrolünden sonra döner; hiçbirinde sözleşme oluşturulmaz.  &#x60;SIGNATURE_VARIANTS_PHONE_ONLY&#x60;: &#x60;allowed_signature_variants&#x60; yalnız &#x60;phone&#x60; içeriyor. Telefonda çizim tek başına seçilemez; listede en az bir &#x60;phone&#x60; olmayan yöntem bırakın.  &#x60;SIGNATURE_VARIANTS_PHONE_DRAW_EXCLUSIVE&#x60;: &#x60;allowed_signature_variants&#x60; içinde &#x60;phone_draw&#x60; başka bir yöntemle birlikte gönderildi. &#x60;phone_draw&#x60; yalnız tek başına kullanılabilir (&#x60;[\&quot;phone_draw\&quot;]&#x60;).  &#x60;TERM_INVALID&#x60;: sözleşme süre/yenileme (&#x60;ContractTermInput&#x60;) alanlarında geçersiz veya çelişkili bir kombinasyon (ör. &#x60;term_fixed_end_date&#x60; ile &#x60;term_duration_months&#x60; birlikte, veya &#x60;renewal_type: AUTO_RENEW&#x60; periyotsuz). &#x60;field&#x60; hangi anahtarın reddedildiğini gösterir.  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **402** | Kredi mutabakatı başarısız: &#x60;INSUFFICIENT_CREDITS&#x60; (bakiye yetersiz) veya &#x60;MEMBER_LIMIT_EXCEEDED&#x60; (organizasyon üyesinin aylık kredi limiti aşıldı, havuzda bakiye olsa bile).  Sözleşme bu noktada ZATEN oluşturulmuştur ve davet gitmemiştir: gövdede &#x60;data.id&#x60; ile aynı sözleşmenin kimliği ve &#x60;data.status: \&quot;DRAFT_UNDISPATCHED\&quot;&#x60; döner. Kredi yükleyip aynı sözleşmeyi &#x60;POST /demands/{id}/dispatch&#x60; ile gönderin; sözleşme silinmez.  |  -  |
| **403** | **SMS_CUSTOMIZATION_NOT_ALLOWED** — Body&#39;de &#x60;sms_content&#x60; alanı dolu gönderildi ama çağıran organizasyon PRO/ENTERPRISE planda değil veya kendi SMS sağlayıcı config&#39;i (sender_name dolu) yok. &#x60;sms_content&#x60; alanını çıkarın veya planınızı yükseltip kendi SMS sağlayıcınızı tanımlayın.  |  -  |
| **409** | Altı ayrı kod döner.  &#x60;IDEMPOTENCY_KEY_REUSED&#x60; — aynı &#x60;Idempotency-Key&#x60; daha önce FARKLI bir içerikle kullanıldı. Sessizce eski sözleşmeyi döndürmek \&quot;gönderdim sandım\&quot; kazası üretirdi; yeni sözleşme için yeni anahtar gönderin. Gövdede eski sözleşmenin &#x60;demand_id&#x60; alanı döner.  &#x60;IDEMPOTENCY_UNVERIFIABLE&#x60;: bu &#x60;Idempotency-Key&#x60; daha önce bir sözleşme üretti, ancak bu isteğin aynı içerikte olduğu doğrulanamadı. Eski sözleşme sessizce döndürülmez. Gövdedeki &#x60;demand_id&#x60; ile durumu sorgulayın; yeni bir anahtarla körlemesine tekrar denemek ikinci bir sözleşme oluşturur.  &#x60;DUPLICATE_SUSPECTED&#x60; — idempotency anahtarı GÖNDERİLMEDİ ve aynı API anahtarı son 10 dakika içinde aynı içeriği (aynı belge/şablon + aynı taraf kümesi) zaten gönderdi. Gövdede mevcut sözleşmenin &#x60;demand_id&#x60; alanı döner. Kasten tekrarlamak için &#x60;force&#x60; gönderin.  Bu kapı yalnız anahtarsız çağrılarda çalışır: &#x60;Idempotency-Key&#x60; gönderen istemci zaten tekrar-korumalıdır.  &#x60;PARTY_WITHOUT_DOCUMENTS&#x60;: eşlediğiniz bir tarafa imzalayacak belge düşmüyor. Üç biçimde görünebilir: (a) şablon düzeyinde ön-kontrol (rol hiçbir şablon belgesine atanmamış ve davet gönderilecek) sözleşmeyi HİÇ yaratmadan reddeder, gövdede &#x60;template_party_ids&#x60; (şablon rol kimlikleri) döner; (b) &#x60;documents&#x60; seçiminiz (ya da &#x60;documents&#x60; göndermediyseniz şablonun varsayılan belge kümesi) bir role seçili belge bırakmıyorsa &#x60;dispatch_notifications&#x60; değerinden bağımsız yine yaratmadan reddedilir, gövdede &#x60;template_party_ids&#x60; döner (seçimi değiştirin ya da o rolü eşlemeden çıkarın); (c) oluşturma sonrası aynı kontrol atamasız tarafı bulursa sözleşme TASLAĞA düşürülür ve gövdede &#x60;party_ids&#x60; (imzacı kimlikleri) + &#x60;data.id&#x60; / &#x60;data.status: \&quot;DRAFT_UNDISPATCHED\&quot;&#x60; döner. Hiçbir biçimde kredi düşülmez, davet gönderilmez.  &#x60;TEMPLATE_DOCUMENTS_NOT_READY&#x60;: şablonun belge yapısı henüz hazır değil; &#x60;documents&#x60; ve &#x60;document_variables&#x60; göndermeden deneyin ya da destekle iletişime geçin.  &#x60;DOCUMENT_SOURCE_UNAVAILABLE&#x60;: şablonun ilk belgesini çıkardınız ve kalan ilk belgenin kaynak dosyası yok; ilk belgeyi de gönderin ya da o belgeye dosya yükleyin.  |  -  |
| **500** | &#x60;RECONCILE_FAILED&#x60;: kredi mutabakatı beklenmedik (kredi-dışı) bir hatayla düştü. Sözleşme bu noktada ZATEN oluşturulmuştur ve TASLAĞA düşürülür; gövdede &#x60;data.id&#x60; / &#x60;data.status: \&quot;DRAFT_UNDISPATCHED\&quot;&#x60; döner. Davet gönderilmez. İstek güvenle tekrarlanabilir (mutabakat idempotenttir).  |  -  |

## apiV1DemandsPostWithHttpInfo

> ApiResponse<ApiV1DemandsPost201Response> apiV1DemandsPostWithHttpInfo(createDemandRequest, idempotencyKey)

Sözleşme oluştur (şablondan)

Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini kaydeder, dynamic field&#39;ları &#x60;variables&#x60; payload&#39;undan doldurur ve imzalama URL&#39;lerini döner.  **Onaylayan:** Şablonda Onaylayan olarak işaretlenmiş taraf varsa oluşturma anında yalnızca onay bekleyen onaylayanlar davet edilir; imzacılar tüm onaylayanlar onayladıktan sonra davet edilir, bir onaylayan reddederse imzacılar davet edilmez. Ayrıntı: açıklamadaki \&quot;Onaylayan (onay adımı)\&quot; bölümü.  **Variable resolution:** - Item&#39;ın &#x60;template_party_id&#x60; non-null → &#x60;party_mapping[i].variables&#x60;&#39;ta   o slug var ise oradan uygulanır - Yoksa root &#x60;variables&#x60;&#39;tan fallback - Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,   &#x60;editable: true&#x60; ise)  **Validation:** - &#x60;party_mapping[i].variables&#x60; ve root &#x60;variables&#x60; object olmalı - Variable value&#39;ları &#x60;string | number | boolean | null&#x60; olmalı   (object/array reject) - &#x60;template_party_id&#x60; party_mapping içinde unique olmalı  **Kredi (çok belgeli zarf):** Şablon çok-belgeli bir zarf tanımlıyorsa (her belge farklı imzacı(lar)a atanabilir), imzacı başına kredi o imzacıya ATANMIŞ belge sayısına göre hesaplanır ve belge sayısına göre azalan birim fiyatlı bir indirim uygulanır: 1 belge %0, 2 belge %30, 3 ve üzeri %50 (indirim imzacı başına ayrı hesaplanır ve yukarı yuvarlanır). AB nitelikli zaman damgası (&#x60;eidas_timestamp&#x60;) seçiliyse aynı indirim zarftaki farklı belge sayısına göre toplam üzerinden uygulanır. Tek belgeli şablonlarda tutar eski formülle bayt-aynıdır.  **Atamasız imzacı:** &#x60;dispatch_notifications&#x60; &#x60;false&#x60; gönderilmediği sürece, eşlenen bir taraf şablonun hiçbir belgesine atanmamışsa sözleşme HİÇ oluşturulmaz (409 &#x60;PARTY_WITHOUT_DOCUMENTS&#x60;). Bu kontrolü atlamak için &#x60;dispatch_notifications: false&#x60; gönderip belge atamalarını &#x60;PUT .../documents/{docId}/assignments&#x60; ile düzelttikten sonra &#x60;POST .../dispatch&#x60; ile gönderin (o uç aynı kapıyı yeniden uygular). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        CreateDemandRequest createDemandRequest = new CreateDemandRequest(); // CreateDemandRequest | 
        String idempotencyKey = "idempotencyKey_example"; // String | Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
        try {
            ApiResponse<ApiV1DemandsPost201Response> response = apiInstance.apiV1DemandsPostWithHttpInfo(createDemandRequest, idempotencyKey);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **createDemandRequest** | [**CreateDemandRequest**](CreateDemandRequest.md)|  | |
| **idempotencyKey** | **String**| Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  | [optional] |

### Return type

ApiResponse<[**ApiV1DemandsPost201Response**](ApiV1DemandsPost201Response.md)>


### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Sözleşme oluşturuldu |  -  |
| **400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate&#39;i bulundu: &lt;id&gt;\&quot;  &#x60;INVALID_EXPIRY_DATE&#x60;: &#x60;expiry_date&#x60; çözümlenemiyor veya takvimde olmayan bir gün (ör. &#x60;2026-02-30&#x60;). Bu hatada &#x60;error&#x60; insan-okur mesajı, &#x60;code&#x60; alanı makinece okunur kodu taşır.  &#x60;INVALID_LOCALE&#x60;: &#x60;party_mapping[i].locale&#x60; &#x60;tr&#x60;, &#x60;en&#x60; veya &#x60;null&#x60; değil. &#x60;error&#x60; hatalı alanın yolunu taşır; sözleşme oluşturulmaz, kredi düşülmez.  &#x60;INVALID_DOCUMENT_SELECTION&#x60;: belge seçimi hatası; &#x60;details.reason&#x60; şu değerlerden biridir: &#x60;shape&#x60; (biçim: nesne değil, bilinmeyen alan, kimlik metin değil, 20&#39;den fazla kimlik), &#x60;unknown_document&#x60; (kimlik bu şablonun belgesi değil), &#x60;conflict&#x60; (aynı kimlik iki listede), &#x60;empty&#x60; (seçim sonucunda belge kalmadı). Biçim hataları kredi kontrolünden önce, şablona bağlı hatalar şablon erişim kontrolünden sonra ve kredi kontrolünden önce döner.  &#x60;INVALID_DOCUMENT_VARIABLES&#x60;: &#x60;document_variables&#x60; hatası; &#x60;details.reason&#x60; şu değerlerden biridir: &#x60;shape&#x60; (biçim: nesne değil, değer string|number|boolean|null değil, boş ya da ayrılmış anahtar, 20&#39;den fazla belge), &#x60;unknown_document&#x60; (kimlik bu şablonun belgesi değil), &#x60;unselected_document&#x60; (belge bu istekte gönderilmiyor; &#x60;documents.include&#x60; ile ekleyin). &#x60;details.path&#x60; hatalı alanı (&#x60;document_variables&#x60; ya da &#x60;party_mapping[i].document_variables&#x60;), &#x60;details.document_ids&#x60; yalnız gönderdiğiniz kimlikleri taşır. Biçim hataları kredi kontrolünden önce, şablona bağlı hatalar şablon erişim kontrolünden sonra döner; hiçbirinde sözleşme oluşturulmaz.  &#x60;SIGNATURE_VARIANTS_PHONE_ONLY&#x60;: &#x60;allowed_signature_variants&#x60; yalnız &#x60;phone&#x60; içeriyor. Telefonda çizim tek başına seçilemez; listede en az bir &#x60;phone&#x60; olmayan yöntem bırakın.  &#x60;SIGNATURE_VARIANTS_PHONE_DRAW_EXCLUSIVE&#x60;: &#x60;allowed_signature_variants&#x60; içinde &#x60;phone_draw&#x60; başka bir yöntemle birlikte gönderildi. &#x60;phone_draw&#x60; yalnız tek başına kullanılabilir (&#x60;[\&quot;phone_draw\&quot;]&#x60;).  &#x60;TERM_INVALID&#x60;: sözleşme süre/yenileme (&#x60;ContractTermInput&#x60;) alanlarında geçersiz veya çelişkili bir kombinasyon (ör. &#x60;term_fixed_end_date&#x60; ile &#x60;term_duration_months&#x60; birlikte, veya &#x60;renewal_type: AUTO_RENEW&#x60; periyotsuz). &#x60;field&#x60; hangi anahtarın reddedildiğini gösterir.  |  -  |
| **401** | API key geçersiz veya eksik |  -  |
| **402** | Kredi mutabakatı başarısız: &#x60;INSUFFICIENT_CREDITS&#x60; (bakiye yetersiz) veya &#x60;MEMBER_LIMIT_EXCEEDED&#x60; (organizasyon üyesinin aylık kredi limiti aşıldı, havuzda bakiye olsa bile).  Sözleşme bu noktada ZATEN oluşturulmuştur ve davet gitmemiştir: gövdede &#x60;data.id&#x60; ile aynı sözleşmenin kimliği ve &#x60;data.status: \&quot;DRAFT_UNDISPATCHED\&quot;&#x60; döner. Kredi yükleyip aynı sözleşmeyi &#x60;POST /demands/{id}/dispatch&#x60; ile gönderin; sözleşme silinmez.  |  -  |
| **403** | **SMS_CUSTOMIZATION_NOT_ALLOWED** — Body&#39;de &#x60;sms_content&#x60; alanı dolu gönderildi ama çağıran organizasyon PRO/ENTERPRISE planda değil veya kendi SMS sağlayıcı config&#39;i (sender_name dolu) yok. &#x60;sms_content&#x60; alanını çıkarın veya planınızı yükseltip kendi SMS sağlayıcınızı tanımlayın.  |  -  |
| **409** | Altı ayrı kod döner.  &#x60;IDEMPOTENCY_KEY_REUSED&#x60; — aynı &#x60;Idempotency-Key&#x60; daha önce FARKLI bir içerikle kullanıldı. Sessizce eski sözleşmeyi döndürmek \&quot;gönderdim sandım\&quot; kazası üretirdi; yeni sözleşme için yeni anahtar gönderin. Gövdede eski sözleşmenin &#x60;demand_id&#x60; alanı döner.  &#x60;IDEMPOTENCY_UNVERIFIABLE&#x60;: bu &#x60;Idempotency-Key&#x60; daha önce bir sözleşme üretti, ancak bu isteğin aynı içerikte olduğu doğrulanamadı. Eski sözleşme sessizce döndürülmez. Gövdedeki &#x60;demand_id&#x60; ile durumu sorgulayın; yeni bir anahtarla körlemesine tekrar denemek ikinci bir sözleşme oluşturur.  &#x60;DUPLICATE_SUSPECTED&#x60; — idempotency anahtarı GÖNDERİLMEDİ ve aynı API anahtarı son 10 dakika içinde aynı içeriği (aynı belge/şablon + aynı taraf kümesi) zaten gönderdi. Gövdede mevcut sözleşmenin &#x60;demand_id&#x60; alanı döner. Kasten tekrarlamak için &#x60;force&#x60; gönderin.  Bu kapı yalnız anahtarsız çağrılarda çalışır: &#x60;Idempotency-Key&#x60; gönderen istemci zaten tekrar-korumalıdır.  &#x60;PARTY_WITHOUT_DOCUMENTS&#x60;: eşlediğiniz bir tarafa imzalayacak belge düşmüyor. Üç biçimde görünebilir: (a) şablon düzeyinde ön-kontrol (rol hiçbir şablon belgesine atanmamış ve davet gönderilecek) sözleşmeyi HİÇ yaratmadan reddeder, gövdede &#x60;template_party_ids&#x60; (şablon rol kimlikleri) döner; (b) &#x60;documents&#x60; seçiminiz (ya da &#x60;documents&#x60; göndermediyseniz şablonun varsayılan belge kümesi) bir role seçili belge bırakmıyorsa &#x60;dispatch_notifications&#x60; değerinden bağımsız yine yaratmadan reddedilir, gövdede &#x60;template_party_ids&#x60; döner (seçimi değiştirin ya da o rolü eşlemeden çıkarın); (c) oluşturma sonrası aynı kontrol atamasız tarafı bulursa sözleşme TASLAĞA düşürülür ve gövdede &#x60;party_ids&#x60; (imzacı kimlikleri) + &#x60;data.id&#x60; / &#x60;data.status: \&quot;DRAFT_UNDISPATCHED\&quot;&#x60; döner. Hiçbir biçimde kredi düşülmez, davet gönderilmez.  &#x60;TEMPLATE_DOCUMENTS_NOT_READY&#x60;: şablonun belge yapısı henüz hazır değil; &#x60;documents&#x60; ve &#x60;document_variables&#x60; göndermeden deneyin ya da destekle iletişime geçin.  &#x60;DOCUMENT_SOURCE_UNAVAILABLE&#x60;: şablonun ilk belgesini çıkardınız ve kalan ilk belgenin kaynak dosyası yok; ilk belgeyi de gönderin ya da o belgeye dosya yükleyin.  |  -  |
| **500** | &#x60;RECONCILE_FAILED&#x60;: kredi mutabakatı beklenmedik (kredi-dışı) bir hatayla düştü. Sözleşme bu noktada ZATEN oluşturulmuştur ve TASLAĞA düşürülür; gövdede &#x60;data.id&#x60; / &#x60;data.status: \&quot;DRAFT_UNDISPATCHED\&quot;&#x60; döner. Davet gönderilmez. İstek güvenle tekrarlanabilir (mutabakat idempotenttir).  |  -  |


## apiV1DemandsUploadPost

> ApiV1DemandsUploadPost201Response apiV1DemandsUploadPost(files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss)

Dosya upload ile sözleşme oluştur (şablonsuz)

Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        List<File> files = Arrays.asList(); // List<File> | 1 belge VEYA 1-20 görsel
        String parties = "parties_example"; // String | JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  `field_template_id` gönderildiğinde her party AYRICA `template_party_id` (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel `locale` (`tr`, `en` veya `null`): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. `PartyMappingInput.locale`). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer `400 INVALID_LOCALE` döner, sözleşme oluşturulmaz. 
        String idempotencyKey = "idempotencyKey_example"; // String | Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
        String order = "order_example"; // String | Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\")
        String title = "title_example"; // String | Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS'lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın. 
        String description = "description_example"; // String | 
        UUID fieldTemplateId = UUID.randomUUID(); // UUID | Alan Şablonu (`kind: FIELD_LAYOUT`) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - `template_id` ile **birlikte gönderilemez**   (400 `FIELD_TEMPLATE_CONFLICT`). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için `POST /api/v1/field-templates/{id}/preview-layout`. 
        String force = "true"; // String | Kopya kapısını bilerek geç. Yalnız `Idempotency-Key` GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için. 
        String sendInvitations = "true"; // String | `\\\"true\\\"` (veya `\\\"1\\\"`) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca `POST /api/v1/demands/{id}/parties/{partyId}/resend` çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 `INVALID_SEND_INVITATIONS`   döner (fail-closed). - **Açık değerler:** `\\\"true\\\"`, `\\\"1\\\"`, `\\\"all\\\"`, `\\\"email\\\"`, `\\\"sms\\\"`. - **Kapalı değerler:** `\\\"false\\\"`, `\\\"0\\\"`, `\\\"off\\\"`, `\\\"no\\\"`,   `\\\"hayir\\\"`, `\\\"hayır\\\"`. - Ad bilerek `dispatch_notifications` **değildir**: o   parametre `POST /api/v1/demands` ve `/demands/bulk`   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: `\\\"true\\\"`, `\\\"1\\\"` ve `\\\"all\\\"` tüm kanalları kullanır;   `\\\"email\\\"` yalnız e-posta, `\\\"sms\\\"` telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   `send_sms`/`send_email` bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (`dispatch.error = DISPATCH_TOO_MANY`), tarafları   `resend` ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği `Idempotency-Key`   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı `DUPLICATE_SUSPECTED` yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte `Idempotency-Key` gönderin;   anahtar yoksa yeniden göndermek yerine   `GET /api/v1/demands?...` ile sonucu doğrulayın. 
        String onAnchorMiss = "block"; // String | Çapa bulunamadığında ne yapılacağı. Yalnız `field_template_id` ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, `block` uygulanır. - `drop`, yalnız şablonun İLGİLİ TÜM alanları zaten `DROP`   ise uygulanır; aksi halde yok sayılır, `block` uygulanır   ve yanıtta `ON_ANCHOR_MISS_NOT_RELAXED` uyarısı döner. - İmza alanlarında `drop` hiçbir koşulda uygulanmaz. - `fallback` bu API'de **yoktur** (400 `INVALID_ON_ANCHOR_MISS`).  Yanıttaki `data.field_layout.on_anchor_miss` istenen değil, **uygulanan** değeri taşır. 
        try {
            ApiV1DemandsUploadPost201Response result = apiInstance.apiV1DemandsUploadPost(files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsUploadPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **files** | **List&lt;File&gt;**| 1 belge VEYA 1-20 görsel | |
| **parties** | **String**| JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel &#x60;locale&#x60; (&#x60;tr&#x60;, &#x60;en&#x60; veya &#x60;null&#x60;): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. &#x60;PartyMappingInput.locale&#x60;). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer &#x60;400 INVALID_LOCALE&#x60; döner, sözleşme oluşturulmaz.  | |
| **idempotencyKey** | **String**| Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  | [optional] |
| **order** | **String**| Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) | [optional] |
| **title** | **String**| Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS&#39;lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın.  | [optional] |
| **description** | **String**|  | [optional] |
| **fieldTemplateId** | **UUID**| Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  | [optional] |
| **force** | **String**| Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  | [optional] [enum: true, 1] |
| **sendInvitations** | **String**| &#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60; ve &#x60;\\\&quot;all\\\&quot;&#x60; tüm kanalları kullanır;   &#x60;\\\&quot;email\\\&quot;&#x60; yalnız e-posta, &#x60;\\\&quot;sms\\\&quot;&#x60; telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği &#x60;Idempotency-Key&#x60;   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı &#x60;DUPLICATE_SUSPECTED&#x60; yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte &#x60;Idempotency-Key&#x60; gönderin;   anahtar yoksa yeniden göndermek yerine   &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  | [optional] [enum: true, 1, all, email, sms, false, 0, off, no, hayir, hayır] |
| **onAnchorMiss** | **String**| Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  | [optional] [enum: block, drop] |

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

## apiV1DemandsUploadPostWithHttpInfo

> ApiResponse<ApiV1DemandsUploadPost201Response> apiV1DemandsUploadPostWithHttpInfo(files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss)

Dosya upload ile sözleşme oluştur (şablonsuz)

Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur (şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel (JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF&#39;e birleştirilir, office formatları LibreOffice ile PDF&#39;e çevrilir. 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        List<File> files = Arrays.asList(); // List<File> | 1 belge VEYA 1-20 görsel
        String parties = "parties_example"; // String | JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  `field_template_id` gönderildiğinde her party AYRICA `template_party_id` (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel `locale` (`tr`, `en` veya `null`): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. `PartyMappingInput.locale`). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer `400 INVALID_LOCALE` döner, sözleşme oluşturulmaz. 
        String idempotencyKey = "idempotencyKey_example"; // String | Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII. 
        String order = "order_example"; // String | Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\")
        String title = "title_example"; // String | Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS'lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın. 
        String description = "description_example"; // String | 
        UUID fieldTemplateId = UUID.randomUUID(); // UUID | Alan Şablonu (`kind: FIELD_LAYOUT`) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - `template_id` ile **birlikte gönderilemez**   (400 `FIELD_TEMPLATE_CONFLICT`). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için `POST /api/v1/field-templates/{id}/preview-layout`. 
        String force = "true"; // String | Kopya kapısını bilerek geç. Yalnız `Idempotency-Key` GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için. 
        String sendInvitations = "true"; // String | `\\\"true\\\"` (veya `\\\"1\\\"`) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca `POST /api/v1/demands/{id}/parties/{partyId}/resend` çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 `INVALID_SEND_INVITATIONS`   döner (fail-closed). - **Açık değerler:** `\\\"true\\\"`, `\\\"1\\\"`, `\\\"all\\\"`, `\\\"email\\\"`, `\\\"sms\\\"`. - **Kapalı değerler:** `\\\"false\\\"`, `\\\"0\\\"`, `\\\"off\\\"`, `\\\"no\\\"`,   `\\\"hayir\\\"`, `\\\"hayır\\\"`. - Ad bilerek `dispatch_notifications` **değildir**: o   parametre `POST /api/v1/demands` ve `/demands/bulk`   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: `\\\"true\\\"`, `\\\"1\\\"` ve `\\\"all\\\"` tüm kanalları kullanır;   `\\\"email\\\"` yalnız e-posta, `\\\"sms\\\"` telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   `send_sms`/`send_email` bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (`dispatch.error = DISPATCH_TOO_MANY`), tarafları   `resend` ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği `Idempotency-Key`   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı `DUPLICATE_SUSPECTED` yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte `Idempotency-Key` gönderin;   anahtar yoksa yeniden göndermek yerine   `GET /api/v1/demands?...` ile sonucu doğrulayın. 
        String onAnchorMiss = "block"; // String | Çapa bulunamadığında ne yapılacağı. Yalnız `field_template_id` ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, `block` uygulanır. - `drop`, yalnız şablonun İLGİLİ TÜM alanları zaten `DROP`   ise uygulanır; aksi halde yok sayılır, `block` uygulanır   ve yanıtta `ON_ANCHOR_MISS_NOT_RELAXED` uyarısı döner. - İmza alanlarında `drop` hiçbir koşulda uygulanmaz. - `fallback` bu API'de **yoktur** (400 `INVALID_ON_ANCHOR_MISS`).  Yanıttaki `data.field_layout.on_anchor_miss` istenen değil, **uygulanan** değeri taşır. 
        try {
            ApiResponse<ApiV1DemandsUploadPost201Response> response = apiInstance.apiV1DemandsUploadPostWithHttpInfo(files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1DemandsUploadPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **files** | **List&lt;File&gt;**| 1 belge VEYA 1-20 görsel | |
| **parties** | **String**| JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  Opsiyonel &#x60;locale&#x60; (&#x60;tr&#x60;, &#x60;en&#x60; veya &#x60;null&#x60;): imzacının dijital imza sayfasını ve bildirimlerini göreceği dil (bkz. &#x60;PartyMappingInput.locale&#x60;). Alan Şablonu kullanılırken gönderilmezse rolün varsayılan dili uygulanır. Geçersiz değer &#x60;400 INVALID_LOCALE&#x60; döner, sözleşme oluşturulmaz.  | |
| **idempotencyKey** | **String**| Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  | [optional] |
| **order** | **String**| Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) | [optional] |
| **title** | **String**| Sözleşme başlığı (boşsa varsayılan bir başlık kullanılır). Başlık, imza davet ve hatırlatma SMS&#39;lerinde imzacının telefon ekranında görünebilir; kişisel veya gizli bilgi yazmayın.  | [optional] |
| **description** | **String**|  | [optional] |
| **fieldTemplateId** | **UUID**| Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  | [optional] |
| **force** | **String**| Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  | [optional] [enum: true, 1] |
| **sendInvitations** | **String**| &#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Kanal: &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60; ve &#x60;\\\&quot;all\\\&quot;&#x60; tüm kanalları kullanır;   &#x60;\\\&quot;email\\\&quot;&#x60; yalnız e-posta, &#x60;\\\&quot;sms\\\&quot;&#x60; telefon kanalları (SMS   ve WhatsApp) gönderir. Değer yalnız **daraltır**:   sözleşmenin bildirim ayarlarında veya tarafın   &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayraklarında kapalı olan bir   kanalı açamaz. Bu uçtan yaratılan sözleşmelerde bu   ayarlar açık doğar. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** isteği &#x60;Idempotency-Key&#x60;   olmadan zaman aşımında körlemesine tekrarlarsanız **yeni   bir sözleşme, yeni kredi ve ikinci bir davet seti**   oluşabilir (kopya kapısı &#x60;DUPLICATE_SUSPECTED&#x60; yalnız aynı   içerikte ve 10 dakika içinde devreye girer). Tekrar   edebileceğiniz her istekte &#x60;Idempotency-Key&#x60; gönderin;   anahtar yoksa yeniden göndermek yerine   &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  | [optional] [enum: true, 1, all, email, sms, false, 0, off, no, hayir, hayır] |
| **onAnchorMiss** | **String**| Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  | [optional] [enum: block, drop] |

### Return type

ApiResponse<[**ApiV1DemandsUploadPost201Response**](ApiV1DemandsUploadPost201Response.md)>


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


## apiV1FieldTemplatesIdPreviewLayoutPost

> ApiV1FieldTemplatesIdPreviewLayoutPost200Response apiV1FieldTemplatesIdPreviewLayoutPost(id, files, onAnchorMiss)

Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener

Bir Alan Şablonunun (&#x60;kind: FIELD_LAYOUT&#x60;) yüklediğiniz PDF&#39;e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  &#x60;POST /api/v1/demands/upload&#x60; + &#x60;field_template_id&#x60; çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API&#39;de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (&#x60;data.resolvable: false&#x60;) — kuru koşumun cevabı \&quot;uygulanamaz\&quot;dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 &#x60;RATE_LIMITED&#x60;). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | Alan Şablonu (FIELD_LAYOUT) kimliği
        List<File> files = Arrays.asList(); // List<File> | Tek PDF belge
        String onAnchorMiss = "block"; // String | `POST /api/v1/demands/upload` ile aynı semantik (yalnız sıkılaştırır, gönderilmezse `block`). 
        try {
            ApiV1FieldTemplatesIdPreviewLayoutPost200Response result = apiInstance.apiV1FieldTemplatesIdPreviewLayoutPost(id, files, onAnchorMiss);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1FieldTemplatesIdPreviewLayoutPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**| Alan Şablonu (FIELD_LAYOUT) kimliği | |
| **files** | **List&lt;File&gt;**| Tek PDF belge | |
| **onAnchorMiss** | **String**| &#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  | [optional] [enum: block, drop] |

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

## apiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo

> ApiResponse<ApiV1FieldTemplatesIdPreviewLayoutPost200Response> apiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo(id, files, onAnchorMiss)

Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener

Bir Alan Şablonunun (&#x60;kind: FIELD_LAYOUT&#x60;) yüklediğiniz PDF&#39;e nasıl uygulanacağını, **hiçbir yan etki üretmeden** döner.  🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.  ### Neden var  &#x60;POST /api/v1/demands/upload&#x60; + &#x60;field_template_id&#x60; çağrısı, alan yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun olup olmadığını.  API&#39;de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda bu adımı çalıştırmanız önerilir.  ### Durum kodu semantiği  Çözümlenemeyen bir belge de **200** döner (&#x60;data.resolvable: false&#x60;) — kuru koşumun cevabı \&quot;uygulanamaz\&quot;dır, isteğin kendisi başarısız değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı) gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.  ### Sınırlar  Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB. Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle sınırlıdır (aşımda 429 &#x60;RATE_LIMITED&#x60;). 

### Example

```java
// Import classes:
import org.imzala.client.generated.ApiClient;
import org.imzala.client.generated.ApiException;
import org.imzala.client.generated.ApiResponse;
import org.imzala.client.generated.Configuration;
import org.imzala.client.generated.auth.*;
import org.imzala.client.generated.models.*;
import org.imzala.client.generated.api.DemandsApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://api-prd.imzala.org");
        
        // Configure API key authorization: ApiKeyAuth
        ApiKeyAuth ApiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("ApiKeyAuth");
        ApiKeyAuth.setApiKey("YOUR API KEY");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //ApiKeyAuth.setApiKeyPrefix("Token");

        DemandsApi apiInstance = new DemandsApi(defaultClient);
        UUID id = UUID.randomUUID(); // UUID | Alan Şablonu (FIELD_LAYOUT) kimliği
        List<File> files = Arrays.asList(); // List<File> | Tek PDF belge
        String onAnchorMiss = "block"; // String | `POST /api/v1/demands/upload` ile aynı semantik (yalnız sıkılaştırır, gönderilmezse `block`). 
        try {
            ApiResponse<ApiV1FieldTemplatesIdPreviewLayoutPost200Response> response = apiInstance.apiV1FieldTemplatesIdPreviewLayoutPostWithHttpInfo(id, files, onAnchorMiss);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling DemandsApi#apiV1FieldTemplatesIdPreviewLayoutPost");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **UUID**| Alan Şablonu (FIELD_LAYOUT) kimliği | |
| **files** | **List&lt;File&gt;**| Tek PDF belge | |
| **onAnchorMiss** | **String**| &#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  | [optional] [enum: block, drop] |

### Return type

ApiResponse<[**ApiV1FieldTemplatesIdPreviewLayoutPost200Response**](ApiV1FieldTemplatesIdPreviewLayoutPost200Response.md)>


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

