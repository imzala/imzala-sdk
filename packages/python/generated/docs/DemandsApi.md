# imzala_client.DemandsApi

All URIs are relative to *https://api-prd.imzala.org*

Method | HTTP request | Description
------------- | ------------- | -------------
[**api_v1_demands_bulk_post**](DemandsApi.md#api_v1_demands_bulk_post) | **POST** /api/v1/demands/bulk | Toplu sözleşme oluştur (tek şablondan N alıcı)
[**api_v1_demands_demand_id_dispatch_post**](DemandsApi.md#api_v1_demands_demand_id_dispatch_post) | **POST** /api/v1/demands/{demandId}/dispatch | Zarfı imzaya gönder (yayınla + davet)
[**api_v1_demands_demand_id_documents_doc_id_assignments_put**](DemandsApi.md#api_v1_demands_demand_id_documents_doc_id_assignments_put) | **PUT** /api/v1/demands/{demandId}/documents/{docId}/assignments | Belgeye imzacı ata (tam-küme replace)
[**api_v1_demands_demand_id_documents_doc_id_delete**](DemandsApi.md#api_v1_demands_demand_id_documents_doc_id_delete) | **DELETE** /api/v1/demands/{demandId}/documents/{docId} | Belgeyi zarftan sil
[**api_v1_demands_demand_id_documents_doc_id_patch**](DemandsApi.md#api_v1_demands_demand_id_documents_doc_id_patch) | **PATCH** /api/v1/demands/{demandId}/documents/{docId} | Belge metadata güncelle
[**api_v1_demands_demand_id_documents_get**](DemandsApi.md#api_v1_demands_demand_id_documents_get) | **GET** /api/v1/demands/{demandId}/documents | Zarf belge listesi
[**api_v1_demands_demand_id_documents_order_put**](DemandsApi.md#api_v1_demands_demand_id_documents_order_put) | **PUT** /api/v1/demands/{demandId}/documents/order | Zarftaki belgelerin sırasını değiştir
[**api_v1_demands_demand_id_documents_post**](DemandsApi.md#api_v1_demands_demand_id_documents_post) | **POST** /api/v1/demands/{demandId}/documents | Zarfa metadata-only belge ekle
[**api_v1_demands_demand_id_documents_upload_post**](DemandsApi.md#api_v1_demands_demand_id_documents_upload_post) | **POST** /api/v1/demands/{demandId}/documents/upload | Zarfa dosya yükle (belge başına tek dosya)
[**api_v1_demands_get**](DemandsApi.md#api_v1_demands_get) | **GET** /api/v1/demands | Sözleşme listesi (counts-only, PII&#39;siz)
[**api_v1_demands_id_belge_document_id_pdf_get**](DemandsApi.md#api_v1_demands_id_belge_document_id_pdf_get) | **GET** /api/v1/demands/{id}/belge/{document_id}/pdf | Belge-özgü imzalı PDF (çok-belgeli zarf)
[**api_v1_demands_id_cancel_post**](DemandsApi.md#api_v1_demands_id_cancel_post) | **POST** /api/v1/demands/{id}/cancel | Sözleşme iptal (void)
[**api_v1_demands_id_certificate_get**](DemandsApi.md#api_v1_demands_id_certificate_get) | **GET** /api/v1/demands/{id}/certificate | Tamamlanma sertifikası (PAdES B-T)
[**api_v1_demands_id_delete**](DemandsApi.md#api_v1_demands_id_delete) | **DELETE** /api/v1/demands/{id} | Sözleşme sil (yalnızca tamamlanmamış)
[**api_v1_demands_id_embed_session_post**](DemandsApi.md#api_v1_demands_id_embed_session_post) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint)
[**api_v1_demands_id_get**](DemandsApi.md#api_v1_demands_id_get) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi
[**api_v1_demands_id_items_post**](DemandsApi.md#api_v1_demands_id_items_post) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace)
[**api_v1_demands_id_parties_party_id_resend_post**](DemandsApi.md#api_v1_demands_id_parties_party_id_resend_post) | **POST** /api/v1/demands/{id}/parties/{partyId}/resend | Tekil tarafa imza davetini tekrar gönder
[**api_v1_demands_id_pdf_get**](DemandsApi.md#api_v1_demands_id_pdf_get) | **GET** /api/v1/demands/{id}/pdf | İmzalı sözleşme PDF&#39;i (auth&#39;lu indirme)
[**api_v1_demands_id_timeline_get**](DemandsApi.md#api_v1_demands_id_timeline_get) | **GET** /api/v1/demands/{id}/timeline | İmza denetim izi (maskeli)
[**api_v1_demands_post**](DemandsApi.md#api_v1_demands_post) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan)
[**api_v1_demands_upload_post**](DemandsApi.md#api_v1_demands_upload_post) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz)
[**api_v1_field_templates_id_preview_layout_post**](DemandsApi.md#api_v1_field_templates_id_preview_layout_post) | **POST** /api/v1/field-templates/{id}/preview-layout | Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener


# **api_v1_demands_bulk_post**
> ApiV1DemandsBulkPost200Response api_v1_demands_bulk_post(api_v1_demands_bulk_post_request, x_workspace_id=x_workspace_id)

Toplu sözleşme oluştur (tek şablondan N alıcı)

Tek şablondan en fazla 10 alıcıya AYRI AYRI sözleşme oluşturur ve
her birine imza daveti gönderir (DocuSign "bulk send" modeli). Her
satır bağımsız bir sözleşmedir; satır-başı kısmi başarı raporlanır.

**Davranış:**
- `rows` en fazla 10 (aşarsa 400 `BULK_MAX_10`). Daha büyük listeler
  istemci tarafında 10'arlı parçalara bölünür.
- Kredi: sabit "1 satır = 1 kredi" değildir, her satırın maliyeti
  `POST /demands` ile AYNI formülle hesaplanır (imza sınıfı taban
  ücreti + doğrulama yöntemi ek maliyetleri + PAdES seviye ek
  maliyeti, imzacı sayısıyla çarpılır; `eidas_timestamp` seçilirse
  satır başına +1 kredi eklenir). PAdES seviye eki yalnız QES'te
  uygulanır: B-T +0, B-LT +0, **B-LTA +1**. Doğrulama yöntemlerinden
  `ocr_id` ve `liveness` +1 kredi, diğerleri 0'dır. `options`
  batch-seviye geçerlidir ve şablon varsayılanını ezer. İstek başında N satırın
  toplam maliyeti için yeterlilik kontrol edilir (yetersizse 402,
  hiçbir sözleşme yaratılmaz); ayrıca her satır kendi maliyeti için
  tekrar kontrol edilir; kredi satır oluşturma sırasında
  tükenirse o satır `failed` (`error: "INSUFFICIENT_CREDITS"`)
  olarak işaretlenir, batch devam eder. Nadir bir yarışta (satır
  oluşturulduktan hemen sonra kredi mutabakatı reddederse) o satır
  yine `failed` döner ama sözleşme zaten oluşturulmuş taslak
  olarak kalır (`demand_id` response'ta bulunur, davet
  gönderilmemiştir).
- Şablon sahipliği istek başında bir kez doğrulanır (workspace-scoped;
  başka workspace'in şablonu 404).
- Kısmi başarı normaldir: batch tamamlanınca HTTP 200 döner (bazı
  satırlar `failed` olsa bile). Yalnızca ön-kontrol redleri
  (cap/kredi/sahiplik) 400/402/404 döner.
- `X-Workspace-Id` header'ı ile organizasyon workspace'i seçilebilir.

Rate limit: 5 istek/dakika (API key başına).


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_bulk_post200_response import ApiV1DemandsBulkPost200Response
from imzala_client.models.api_v1_demands_bulk_post_request import ApiV1DemandsBulkPostRequest
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    api_v1_demands_bulk_post_request = imzala_client.ApiV1DemandsBulkPostRequest() # ApiV1DemandsBulkPostRequest | 
    x_workspace_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   `WORKSPACE_MISMATCH` döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (`Not a member of this organization`).  (optional)

    try:
        # Toplu sözleşme oluştur (tek şablondan N alıcı)
        api_response = api_instance.api_v1_demands_bulk_post(api_v1_demands_bulk_post_request, x_workspace_id=x_workspace_id)
        print("The response of DemandsApi->api_v1_demands_bulk_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_bulk_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **api_v1_demands_bulk_post_request** | [**ApiV1DemandsBulkPostRequest**](ApiV1DemandsBulkPostRequest.md)|  | 
 **x_workspace_id** | **UUID**| Organizasyon (workspace) kimliği.  - **Organizasyon anahtarı:** zorunlu değildir; gönderilmezse anahtarın   bağlı olduğu organizasyon otomatik uygulanır. Anahtarın   organizasyonundan farklı bir kimlik gönderilirse 403   &#x60;WORKSPACE_MISMATCH&#x60; döner. - **Kişisel anahtar:** gerekmez. Üyesi olmadığınız bir organizasyon   kimliği gönderilirse 403 döner   (&#x60;Not a member of this organization&#x60;).  | [optional] 

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
**200** | Batch tamamlandı (kısmi başarı dahil). &#x60;results&#x60; giriş sırasında, her satır &#x60;row_index&#x60; ile eşlenir.  |  -  |
**400** | Geçersiz istek. Olası kodlar: &#x60;BULK_MAX_10&#x60; (&#x60;rows&#x60; 10&#39;dan fazla), boş &#x60;rows&#x60;, eksik &#x60;template_id&#x60;, &#x60;INVALID_PADES_LEVEL&#x60; (&#x60;options.qes_pades_level&#x60; satılabilir seviyelerden biri değil), &#x60;FIELD_LAYOUT_TEMPLATE_NOT_SENDABLE&#x60; (verilen kimlik bir Alan Şablonuna ait). AB nitelikli zaman damgası hesabınızda kapalıyken &#x60;eidas_timestamp&#x60; gönderilirse de 400 döner.  |  -  |
**401** | API key geçersiz veya eksik |  -  |
**402** | INSUFFICIENT_CREDITS: N satırın toplam tahmini maliyeti için yeterli kredi yok (ön kontrol; hiçbir sözleşme yaratılmadı). Satır bazlı yetersiz kredi durumları burada DEĞİL, 200 yanıtı içindeki &#x60;results[].status: \&quot;failed\&quot;&#x60; altında döner.  |  -  |
**403** | INSUFFICIENT_SCOPE (demands:write yok) veya SMS_CUSTOMIZATION_NOT_ALLOWED |  -  |
**404** | Kayıt bulunamadı |  -  |
**409** | &#x60;QES_NOT_SUPPORTED_MULTI_DOCUMENT&#x60;: nitelikli imza (QES) birden çok belge içeren zarflarda henüz desteklenmiyor.  |  -  |
**429** | İstek limiti aşıldı (API anahtarı başına 5 toplu istek/dakika) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_demand_id_dispatch_post**
> ApiV1DemandsDemandIdDispatchPost200Response api_v1_demands_demand_id_dispatch_post(demand_id, api_v1_demands_demand_id_dispatch_post_request=api_v1_demands_demand_id_dispatch_post_request)

Zarfı imzaya gönder (yayınla + davet)

Sözleşmeyi imzaya gönderir: kredi mutabakatı yapar, `DRAFT` ise
sözleşmeyi `PENDING`'e alır ve tarafları imza daveti (SMS/e-posta/
WhatsApp) ile bilgilendirir.

🔴 **Bu uçta yukarıdaki `/documents*` ailesinin bayrak kapısı
(`ENVELOPE_DECISION_ENFORCE`) YOKTUR** (bilinçli): gönderim
tek-belgeli zarflarda da anlamlıdır; o bayrak yalnız çok-belgeli zarf
YARATIMINI kapatan bir anahtardır.

**İki aşamalı akış örneği:** bir sözleşme önce sessizce hazırlanabilir
— `dispatch_notifications: false` ile oluşturulur, ardından
`POST .../documents` / `POST .../documents/upload` ile belgeler
eklenir (bu iki uç kimseye bildirim GÖNDERMEZ; `send_invitations`
gibi bir parametreleri bile yoktur) — çağıran hazır olduğunda TEK bu
uçla yayına alıp davetleri gönderir.

**Kredi:** tek tahsilat noktası burasıdır (`reconcileDemandSigningCost`).
Belge CRUD/yükleme uçları kredi düşmez. Mutabakat **idempotent**tir:
sözleşme zaten yayınlanmışsa (`dispatched: false` döner) fark 0
olduğu için tekrar tahsilat YAPILMAZ — ama davet fazı yine çalışır
(bkz. `v1ResendRateLimiter`, `/parties/{partyId}/resend` ile AYNI
dakika-bazlı freni paylaşır).


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_demand_id_dispatch_post200_response import ApiV1DemandsDemandIdDispatchPost200Response
from imzala_client.models.api_v1_demands_demand_id_dispatch_post_request import ApiV1DemandsDemandIdDispatchPostRequest
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    demand_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    api_v1_demands_demand_id_dispatch_post_request = imzala_client.ApiV1DemandsDemandIdDispatchPostRequest() # ApiV1DemandsDemandIdDispatchPostRequest |  (optional)

    try:
        # Zarfı imzaya gönder (yayınla + davet)
        api_response = api_instance.api_v1_demands_demand_id_dispatch_post(demand_id, api_v1_demands_demand_id_dispatch_post_request=api_v1_demands_demand_id_dispatch_post_request)
        print("The response of DemandsApi->api_v1_demands_demand_id_dispatch_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_demand_id_dispatch_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **demand_id** | **UUID**|  | 
 **api_v1_demands_demand_id_dispatch_post_request** | [**ApiV1DemandsDemandIdDispatchPostRequest**](ApiV1DemandsDemandIdDispatchPostRequest.md)|  | [optional] 

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
**200** | Yayına alındı (veya zaten yayındaydı) + davet sonucu |  -  |
**400** | &#x60;INVALID_SEND_INVITATIONS&#x60; — tanınmayan &#x60;send_invitations&#x60; değeri. |  -  |
**402** | Kredi mutabakatı başarısız — sözleşme yayına GEÇMEZ, davet gönderilmez. &#x60;INSUFFICIENT_CREDITS&#x60; — hesabın/organizasyonun kredisi yetmiyor. &#x60;MEMBER_LIMIT_EXCEEDED&#x60; — organizasyon üyesinin aylık kredi limiti aşıldı (kredi havuzunda bakiye olsa bile).  |  -  |
**404** | Kayıt bulunamadı |  -  |
**409** | &#x60;DEMAND_EXPIRED&#x60; — sözleşmenin imza süresi geçmiş. &#x60;DEMAND_NOT_DISPATCHABLE&#x60; — tamamlanmış/iptal edilmiş sözleşme tekrar yayınlanamaz. &#x60;DISPATCH_NO_PARTIES&#x60; — sözleşmede hiç imzacı taraf yok. Zarf tamamlanamayacağı için yayına ALINMAZ; kredi dokunulmaz, statü değişmez, &#x60;demand.dispatched&#x60; olayı yayılmaz. Önce &#x60;POST /demands/{demandId}/parties&#x60; ile taraf ekleyin. &#x60;DISPATCH_TOO_MANY&#x60; — taraf sayısı 20&#39;yi aşıyor. İstek hiçbir yan etki bırakmadan reddedilir (kredi düşmez, sözleşme yayına geçmez); sözleşmeyi tarafları azaltarak gönderin veya davetleri &#x60;/parties/{partyId}/resend&#x60; ile tek tek yollayın.  |  -  |
**429** | Tekrar-gönderim hız sınırı aşıldı — bu uç &#x60;/parties/{partyId}/resend&#x60; ile AYNI anahtar-bazlı freni paylaşır.  |  -  |
**500** | &#x60;RECONCILE_FAILED&#x60; — kredi mutabakatı beklenmedik bir hatayla düştü. Sözleşme yayına GEÇMEZ ve davet gönderilmez; istek güvenle tekrarlanabilir (mutabakat idempotenttir).  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_demand_id_documents_doc_id_assignments_put**
> ApiV1DemandsDemandIdDocumentsPost201Response api_v1_demands_demand_id_documents_doc_id_assignments_put(demand_id, doc_id, api_v1_demands_demand_id_documents_doc_id_assignments_put_request)

Belgeye imzacı ata (tam-küme replace)

Belgeye atanmış imzacı kümesini **tam olarak** `party_ids` ile
değiştirir (eski atamalar silinir, yenileri yazılır). `party_ids`
boş olamaz ve sözleşmenin kendi taraflarına ait olmak zorundadır
(400 `INVALID_PARTY_ID` — çapraz-sözleşme id kabul edilmez).

Zaten karar vermiş (onaylamış/reddetmiş) bir imzacının ataması
kaldırılamaz (400 `ASSIGNMENT_HAS_DECISION`).


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_demand_id_documents_doc_id_assignments_put_request import ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest
from imzala_client.models.api_v1_demands_demand_id_documents_post201_response import ApiV1DemandsDemandIdDocumentsPost201Response
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    demand_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    doc_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    api_v1_demands_demand_id_documents_doc_id_assignments_put_request = imzala_client.ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest() # ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest | 

    try:
        # Belgeye imzacı ata (tam-küme replace)
        api_response = api_instance.api_v1_demands_demand_id_documents_doc_id_assignments_put(demand_id, doc_id, api_v1_demands_demand_id_documents_doc_id_assignments_put_request)
        print("The response of DemandsApi->api_v1_demands_demand_id_documents_doc_id_assignments_put:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_demand_id_documents_doc_id_assignments_put: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **demand_id** | **UUID**|  | 
 **doc_id** | **UUID**|  | 
 **api_v1_demands_demand_id_documents_doc_id_assignments_put_request** | [**ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest**](ApiV1DemandsDemandIdDocumentsDocIdAssignmentsPutRequest.md)|  | 

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
**200** | Atama güncellendi |  -  |
**400** | &#x60;ASSIGNMENT_EMPTY&#x60; — &#x60;party_ids&#x60; eksik/boş. &#x60;INVALID_PARTY_ID&#x60; — bu sözleşmeye ait olmayan taraf id&#39;si. &#x60;ASSIGNMENT_HAS_DECISION&#x60; — karar vermiş bir imzacının ataması kaldırılamaz.  |  -  |
**404** | Kayıt bulunamadı |  -  |
**409** | &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60; / &#x60;DEMAND_NOT_EDITABLE&#x60; / &#x60;SIGNING_ALREADY_STARTED&#x60;. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_demand_id_documents_doc_id_delete**
> ApiV1TemplatesIdDelete200Response api_v1_demands_demand_id_documents_doc_id_delete(demand_id, doc_id)

Belgeyi zarftan sil

Belgeyi (sayfaları + içerikleriyle birlikte) siler; kalan belgeler
1..N'e yeniden sıralanır. **Zarftaki son belge silinemez** (400
`CANNOT_DELETE_LAST_DOCUMENT`) — zarf hiç boş kalamaz.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_templates_id_delete200_response import ApiV1TemplatesIdDelete200Response
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    demand_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    doc_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 

    try:
        # Belgeyi zarftan sil
        api_response = api_instance.api_v1_demands_demand_id_documents_doc_id_delete(demand_id, doc_id)
        print("The response of DemandsApi->api_v1_demands_demand_id_documents_doc_id_delete:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_demand_id_documents_doc_id_delete: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **demand_id** | **UUID**|  | 
 **doc_id** | **UUID**|  | 

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
**200** | Silindi |  -  |
**400** | &#x60;CANNOT_DELETE_LAST_DOCUMENT&#x60; — zarftaki son belge. &#x60;DOCUMENT_HAS_SIGNED_CONTENT&#x60; — imza/içerik girilmiş belge silinemez. &#x60;DOCUMENT_HAS_DECISIONS&#x60; — bu belge için onay/red kararı verilmiş.  |  -  |
**404** | Kayıt bulunamadı |  -  |
**409** | &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60; / &#x60;DEMAND_NOT_EDITABLE&#x60; / &#x60;SIGNING_ALREADY_STARTED&#x60;. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_demand_id_documents_doc_id_patch**
> ApiV1DemandsDemandIdDocumentsPost201Response api_v1_demands_demand_id_documents_doc_id_patch(demand_id, doc_id, api_v1_demands_demand_id_documents_doc_id_patch_request)

Belge metadata güncelle

Belgenin yalnız metadata alanlarını (title/doc_kind/is_required/
signature_required) günceller — **kısmi güncelleme**: gövdede
gönderilmeyen alanlar değişmez. İmzacı ataması AYRI bir uçtur
(`PUT .../assignments`).


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_demand_id_documents_doc_id_patch_request import ApiV1DemandsDemandIdDocumentsDocIdPatchRequest
from imzala_client.models.api_v1_demands_demand_id_documents_post201_response import ApiV1DemandsDemandIdDocumentsPost201Response
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    demand_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    doc_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    api_v1_demands_demand_id_documents_doc_id_patch_request = imzala_client.ApiV1DemandsDemandIdDocumentsDocIdPatchRequest() # ApiV1DemandsDemandIdDocumentsDocIdPatchRequest | 

    try:
        # Belge metadata güncelle
        api_response = api_instance.api_v1_demands_demand_id_documents_doc_id_patch(demand_id, doc_id, api_v1_demands_demand_id_documents_doc_id_patch_request)
        print("The response of DemandsApi->api_v1_demands_demand_id_documents_doc_id_patch:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_demand_id_documents_doc_id_patch: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **demand_id** | **UUID**|  | 
 **doc_id** | **UUID**|  | 
 **api_v1_demands_demand_id_documents_doc_id_patch_request** | [**ApiV1DemandsDemandIdDocumentsDocIdPatchRequest**](ApiV1DemandsDemandIdDocumentsDocIdPatchRequest.md)|  | 

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
**200** | Güncellendi |  -  |
**400** | &#x60;VALIDATION_FAIL&#x60; (başlık boş) · &#x60;INVALID_DOC_KIND&#x60; · &#x60;CONSENT_CANNOT_BE_REQUIRED&#x60; / &#x60;PREINFO_MUST_BE_REQUIRED&#x60; — yamadan SONRA etkin &#x60;doc_kind&#x60;+&#x60;is_required&#x60; çiftine göre kontrol edilir (tek alanı yamalayıp kuralı atlatmak mümkün değildir).  |  -  |
**404** | Kayıt bulunamadı |  -  |
**409** | &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60; / &#x60;DEMAND_NOT_EDITABLE&#x60; / &#x60;SIGNING_ALREADY_STARTED&#x60;. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_demand_id_documents_get**
> ApiV1DemandsDemandIdDocumentsGet200Response api_v1_demands_demand_id_documents_get(demand_id, view=view)

Zarf belge listesi

Çok-belgeli imza zarfının belge listesini döner.

🔴 Bu uç ve aşağıdaki tüm `/documents*` + `/documents/{docId}*` +
`/documents/{docId}/assignments` uçları `ENVELOPE_DECISION_ENFORCE`
bayrağı **arkasındadır**; bayrak kapalıyken **409
`ENVELOPE_MULTI_DOC_DISABLED`** döner. Bu, dashboard'un davranışından
BİLEREK daha katıdır: dashboard yalnız yazma uçlarını kapatır, burada
okuma da kapalıdır — kademeli açılış tamamlanmadan dış API çağıranına
kararsız/tek-belgeye-indirgenmiş bir zarf durumu hiç gösterilmez.

Belge uçları **kredi düşmez**; tahsilat yalnızca
`POST /demands/{demandId}/dispatch` çağrıldığında yapılır.

Varsayılan yanıt daraltılmış özet şeklidir. `?view=wizard` verilirse
tam şekil döner (`EnvelopeDocument` şemasındaki alan notlarına bakın).


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_demand_id_documents_get200_response import ApiV1DemandsDemandIdDocumentsGet200Response
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    demand_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    view = 'view_example' # str | `wizard` → tam DTO (`assigned_party_ids` + `decision_count` dahil). (optional)

    try:
        # Zarf belge listesi
        api_response = api_instance.api_v1_demands_demand_id_documents_get(demand_id, view=view)
        print("The response of DemandsApi->api_v1_demands_demand_id_documents_get:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_demand_id_documents_get: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **demand_id** | **UUID**|  | 
 **view** | **str**| &#x60;wizard&#x60; → tam DTO (&#x60;assigned_party_ids&#x60; + &#x60;decision_count&#x60; dahil). | [optional] 

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
**200** | Belge listesi |  -  |
**404** | Kayıt bulunamadı |  -  |
**409** | &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60; — çok-belgeli zarf yüzeyi bu hesap için henüz kapalı. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_demand_id_documents_order_put**
> ApiV1DemandsDemandIdDocumentsGet200Response api_v1_demands_demand_id_documents_order_put(demand_id, api_v1_demands_demand_id_documents_order_put_request)

Zarftaki belgelerin sırasını değiştir

Zarftaki TÜM belgelerin sırasını tek istekte yeniden atar.
`document_ids` zarftaki **mevcut belge kümesiyle birebir aynı** (aynı
eleman sayısı, farklı sıra) olmak zorundadır — eksik/fazla/yabancı id
**400 `ORDER_SET_MISMATCH`** döner.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_demand_id_documents_get200_response import ApiV1DemandsDemandIdDocumentsGet200Response
from imzala_client.models.api_v1_demands_demand_id_documents_order_put_request import ApiV1DemandsDemandIdDocumentsOrderPutRequest
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    demand_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    api_v1_demands_demand_id_documents_order_put_request = imzala_client.ApiV1DemandsDemandIdDocumentsOrderPutRequest() # ApiV1DemandsDemandIdDocumentsOrderPutRequest | 

    try:
        # Zarftaki belgelerin sırasını değiştir
        api_response = api_instance.api_v1_demands_demand_id_documents_order_put(demand_id, api_v1_demands_demand_id_documents_order_put_request)
        print("The response of DemandsApi->api_v1_demands_demand_id_documents_order_put:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_demand_id_documents_order_put: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **demand_id** | **UUID**|  | 
 **api_v1_demands_demand_id_documents_order_put_request** | [**ApiV1DemandsDemandIdDocumentsOrderPutRequest**](ApiV1DemandsDemandIdDocumentsOrderPutRequest.md)|  | 

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
**200** | Yeni sıra uygulandı |  -  |
**400** | &#x60;ORDER_SET_MISMATCH&#x60; — gönderilen id kümesi zarftaki belgelerle eşleşmiyor. |  -  |
**404** | Kayıt bulunamadı |  -  |
**409** | &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60; / &#x60;DEMAND_NOT_EDITABLE&#x60; / &#x60;SIGNING_ALREADY_STARTED&#x60;. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_demand_id_documents_post**
> ApiV1DemandsDemandIdDocumentsPost201Response api_v1_demands_demand_id_documents_post(demand_id, api_v1_demands_demand_id_documents_post_request)

Zarfa metadata-only belge ekle

Zarfa dosyasız (yalnız metadata) yeni bir belge satırı ekler; sıra
numarası otomatik atanır (mevcut belge sayısı + 1). Dosyalı yükleme
AYRI bir uçtur: `POST /demands/{demandId}/documents/upload`.

Belge uçları kredi düşmez; tahsilat `POST /dispatch`'te yapılır.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_demand_id_documents_post201_response import ApiV1DemandsDemandIdDocumentsPost201Response
from imzala_client.models.api_v1_demands_demand_id_documents_post_request import ApiV1DemandsDemandIdDocumentsPostRequest
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    demand_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    api_v1_demands_demand_id_documents_post_request = imzala_client.ApiV1DemandsDemandIdDocumentsPostRequest() # ApiV1DemandsDemandIdDocumentsPostRequest | 

    try:
        # Zarfa metadata-only belge ekle
        api_response = api_instance.api_v1_demands_demand_id_documents_post(demand_id, api_v1_demands_demand_id_documents_post_request)
        print("The response of DemandsApi->api_v1_demands_demand_id_documents_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_demand_id_documents_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **demand_id** | **UUID**|  | 
 **api_v1_demands_demand_id_documents_post_request** | [**ApiV1DemandsDemandIdDocumentsPostRequest**](ApiV1DemandsDemandIdDocumentsPostRequest.md)|  | 

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
**201** | Belge oluşturuldu |  -  |
**400** | &#x60;VALIDATION_FAIL&#x60; — başlık boş. &#x60;INVALID_DOC_KIND&#x60; — geçersiz &#x60;doc_kind&#x60;. &#x60;CONSENT_CANNOT_BE_REQUIRED&#x60; / &#x60;PREINFO_MUST_BE_REQUIRED&#x60; — yukarı bakın. &#x60;DOCUMENT_LIMIT_EXCEEDED&#x60; — zarf başına en fazla 20 belge. &#x60;QES_NOT_SUPPORTED_MULTI_DOCUMENT&#x60; — NES/Mobil İmza açık (&#x60;enable_qes&#x60;) bir sözleşmede ikinci belge eklenemez.  |  -  |
**404** | Kayıt bulunamadı |  -  |
**409** | &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60; — bayrak kapalı. &#x60;DEMAND_NOT_EDITABLE&#x60; — sözleşme düzenlemeye açık değil (iptal/ süresi geçmiş/bulunamadı). &#x60;SIGNING_ALREADY_STARTED&#x60; — imza süreci başlamış zarfın belge listesi değiştirilemez (biri görüntülemiş/imzalamış/karar vermiş).  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_demand_id_documents_upload_post**
> ApiV1DemandsDemandIdDocumentsPost201Response api_v1_demands_demand_id_documents_upload_post(demand_id, file, idempotency_key, title, doc_kind=doc_kind, is_required=is_required)

Zarfa dosya yükle (belge başına tek dosya)

Zarfa **belge başına tek dosya** yükler. Aynı sözleşmeye birden çok
belge eklemek için bu uç birden çok kez çağrılır — tek istekte N
belge YASAKTIR.

`idempotency_key` **zorunludur** (gövde alanı; `Idempotency-Key`
HEADER'ı ile KARIŞTIRILMAZ, ayrı bir mekanizmadır). Aynı
`(demandId, idempotency_key)` çifti ile tekrar çağrı **409
`IDEMPOTENT_REPLAY`** döner ve **yeni belge YARATILMAZ**; yanıt
gövdesinde daha önce yüklenen belgenin DTO'su döner.

Belge uçları kredi düşmez; tahsilat `POST /dispatch`'te yapılır.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_demand_id_documents_post201_response import ApiV1DemandsDemandIdDocumentsPost201Response
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    demand_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    file = None # bytes | PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya.
    idempotency_key = 'idempotency_key_example' # str | Zorunlu tekrar-koruma anahtarı.
    title = 'title_example' # str | 
    doc_kind = OTHER # str |  (optional) (default to OTHER)
    is_required = true # str | Multipart alanı — string olarak gönderilir. (optional) (default to true)

    try:
        # Zarfa dosya yükle (belge başına tek dosya)
        api_response = api_instance.api_v1_demands_demand_id_documents_upload_post(demand_id, file, idempotency_key, title, doc_kind=doc_kind, is_required=is_required)
        print("The response of DemandsApi->api_v1_demands_demand_id_documents_upload_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_demand_id_documents_upload_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **demand_id** | **UUID**|  | 
 **file** | **bytes**| PDF/DOC/DOCX/ODT/RTF/TXT veya görsel. Tek dosya. | 
 **idempotency_key** | **str**| Zorunlu tekrar-koruma anahtarı. | 
 **title** | **str**|  | 
 **doc_kind** | **str**|  | [optional] [default to OTHER]
 **is_required** | **str**| Multipart alanı — string olarak gönderilir. | [optional] [default to true]

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
**200** | Belge yüklendi |  -  |
**400** | &#x60;VALIDATION_FAIL&#x60; (dosya/idempotency_key/başlık eksik) veya &#x60;INVALID_DOC_KIND&#x60;. |  -  |
**404** | Kayıt bulunamadı |  -  |
**409** | &#x60;ENVELOPE_MULTI_DOC_DISABLED&#x60; — bayrak kapalı. &#x60;IDEMPOTENT_REPLAY&#x60; — aynı &#x60;idempotency_key&#x60; daha önce kullanıldı, yeni belge yaratılmaz (gövdede mevcut belgenin &#x60;document&#x60;&#39;ı döner). &#x60;DEMAND_NOT_EDITABLE&#x60; / &#x60;SIGNING_ALREADY_STARTED&#x60; — bkz. yukarıdaki uç.  |  -  |
**413** | &#x60;FILE_TOO_LARGE&#x60; — 20 MB sınırı. |  -  |
**415** | Desteklenmeyen dosya türü. |  -  |
**422** | &#x60;IMAGE_DECODE_FAILED&#x60; — görsel çözümlenemedi. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_get**
> ApiV1DemandsGet200Response api_v1_demands_get(status=status, q=q, var_from=var_from, to=to, template_id=template_id, page=page, limit=limit, sort=sort)

Sözleşme listesi (counts-only, PII'siz)

Workspace + rol farkındalıklı sözleşme listesi. KVKK veri minimizasyonu:
yalnızca sözleşme başlığı/durumu + imzacı SAYILARI döner (`parties_total`,
`parties_signed`). Taraf adı/e-posta/telefon ve ham IP/cihaz/TC/konum HİÇ
döndürülmez — taraf detayı için `GET /demands/{id}`.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_get200_response import ApiV1DemandsGet200Response
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    status = 'status_example' # str |  (optional)
    q = 'q_example' # str | Başlık araması (optional)
    var_from = '2013-10-20' # date |  (optional)
    to = '2013-10-20' # date |  (optional)
    template_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID |  (optional)
    page = 1 # int |  (optional) (default to 1)
    limit = 20 # int | Sayfa boyutu (page_size ile aynı) (optional) (default to 20)
    sort = 'sort_example' # str | alan:yön (ör. createdAt:desc) (optional)

    try:
        # Sözleşme listesi (counts-only, PII'siz)
        api_response = api_instance.api_v1_demands_get(status=status, q=q, var_from=var_from, to=to, template_id=template_id, page=page, limit=limit, sort=sort)
        print("The response of DemandsApi->api_v1_demands_get:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_get: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **status** | **str**|  | [optional] 
 **q** | **str**| Başlık araması | [optional] 
 **var_from** | **date**|  | [optional] 
 **to** | **date**|  | [optional] 
 **template_id** | **UUID**|  | [optional] 
 **page** | **int**|  | [optional] [default to 1]
 **limit** | **int**| Sayfa boyutu (page_size ile aynı) | [optional] [default to 20]
 **sort** | **str**| alan:yön (ör. createdAt:desc) | [optional] 

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
**200** | Başarılı |  -  |
**401** | API key geçersiz veya eksik |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_id_belge_document_id_pdf_get**
> bytes api_v1_demands_id_belge_document_id_pdf_get(id, document_id)

Belge-özgü imzalı PDF (çok-belgeli zarf)

Çok-belgeli zarfta TEK bir belgenin imzalı PDF'ini indirir. Zarf-geneli
`/demands/{id}/pdf` ucunun belge-kırılımlı ikizidir; scope ve ownership
kapıları birebir aynıdır, belge aidiyeti ayrıca sözleşmeye AND'lenir
(başka zarfın belgesi istenirse 404).


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    document_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | Zarftaki belgenin kimliği.

    try:
        # Belge-özgü imzalı PDF (çok-belgeli zarf)
        api_response = api_instance.api_v1_demands_id_belge_document_id_pdf_get(id, document_id)
        print("The response of DemandsApi->api_v1_demands_id_belge_document_id_pdf_get:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_id_belge_document_id_pdf_get: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  | 
 **document_id** | **UUID**| Zarftaki belgenin kimliği. | 

### Return type

**bytes**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/pdf, application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | Belgenin imzalı PDF&#39;i |  -  |
**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_id_cancel_post**
> ApiV1DemandsIdCancelPost200Response api_v1_demands_id_cancel_post(id, api_v1_demands_id_cancel_post_request=api_v1_demands_id_cancel_post_request)

Sözleşme iptal (void)

Bekleyen bir sözleşmeyi iptal eder (status=CANCELLED). Tamamlanmış
(409) veya zaten iptal edilmiş (409) sözleşme iptal edilemez. Bekleyen
hatırlatmalar iptal edilir.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_id_cancel_post200_response import ApiV1DemandsIdCancelPost200Response
from imzala_client.models.api_v1_demands_id_cancel_post_request import ApiV1DemandsIdCancelPostRequest
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    api_v1_demands_id_cancel_post_request = imzala_client.ApiV1DemandsIdCancelPostRequest() # ApiV1DemandsIdCancelPostRequest |  (optional)

    try:
        # Sözleşme iptal (void)
        api_response = api_instance.api_v1_demands_id_cancel_post(id, api_v1_demands_id_cancel_post_request=api_v1_demands_id_cancel_post_request)
        print("The response of DemandsApi->api_v1_demands_id_cancel_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_id_cancel_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  | 
 **api_v1_demands_id_cancel_post_request** | [**ApiV1DemandsIdCancelPostRequest**](ApiV1DemandsIdCancelPostRequest.md)|  | [optional] 

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
**200** | İptal edildi |  -  |
**404** | Kayıt bulunamadı |  -  |
**409** | Tamamlanmış/iptal edilmiş sözleşme |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_id_certificate_get**
> bytes api_v1_demands_id_certificate_get(id, lang=lang)

Tamamlanma sertifikası (PAdES B-T)

Sözleşmenin tamamlanma/denetim sertifikasını (imza denetim izi + zaman
damgası özeti, PAdES B-T mühürlü) PDF olarak döner. Yalnızca COMPLETED
sözleşmeler için üretilir (aksi 409).


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    lang = 'lang_example' # str | tr | en (optional)

    try:
        # Tamamlanma sertifikası (PAdES B-T)
        api_response = api_instance.api_v1_demands_id_certificate_get(id, lang=lang)
        print("The response of DemandsApi->api_v1_demands_id_certificate_get:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_id_certificate_get: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  | 
 **lang** | **str**| tr | en | [optional] 

### Return type

**bytes**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/pdf, application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | Sertifika PDF |  -  |
**404** | Kayıt bulunamadı |  -  |
**409** | Sözleşme henüz tamamlanmadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_id_delete**
> ApiV1TemplatesIdDelete200Response api_v1_demands_id_delete(id)

Sözleşme sil (yalnızca tamamlanmamış)

Tamamlanmamış sözleşmeyi ve ilişkili tüm verilerini siler. 🔴
Tamamlanmış (COMPLETED) sözleşme API'den SİLİNEMEZ (imzalı belge +
denetim izi kaybı geri alınamaz) → 409 `DEMAND_COMPLETED`.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_templates_id_delete200_response import ApiV1TemplatesIdDelete200Response
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 

    try:
        # Sözleşme sil (yalnızca tamamlanmamış)
        api_response = api_instance.api_v1_demands_id_delete(id)
        print("The response of DemandsApi->api_v1_demands_id_delete:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_id_delete: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  | 

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
**200** | Silindi |  -  |
**404** | Kayıt bulunamadı |  -  |
**409** | Tamamlanmış sözleşme silinemez |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_id_embed_session_post**
> ApiV1DemandsIdEmbedSessionPost200Response api_v1_demands_id_embed_session_post(id, api_v1_demands_id_embed_session_post_request)

Gömülü imza oturumu başlat (embed token mint)

Belirtilen sözleşmedeki bir taraf için kısa ömürlü, tek kullanımlık
gömülü imza token'ı üretir. Dönen `embed_url` bir `<iframe>` içine
yerleştirilerek tarafın kendi uygulamanız içinden imzalaması sağlanır.

**İmza sınıfı:** Bu akışla elde edilen imzalar **SES** (Basit Elektronik
İmza) sınıfında değerlendirilir; doğrulama (TC kimlik veya biyometri)
yapılmışsa **AES** (Gelişmiş Elektronik İmza) olabilir. Bu akış
nitelikli elektronik imza (QES) üretmez — "güvenli" veya "nitelikli"
sınıf için ayrı QES akışını kullanın.

**Token özellikleri:**
- Tek kullanımlık: imza sayfası açıldığında token tüketilir.
- Kısa ömürlü: `expires_at` alanında belirtilen sürede geçersiz olur.
- `embed_allowed_origins` kısıtı: API anahtarına tanımlanmış
  izin verilen origin'ler dışından `<iframe>` açılamaz (409 döner).

**Güvenlik katmanları:**
- B1: Sözleşme sahiplik kontrolü (workspace-aware IDOR koruması)
- B3: Çapraz sözleşme taraf IDOR koruması (party.demand_id doğrulaması)
- K:  Taraf-eylem kapısı (zaten imzalamış veya reddetmiş tarafa token üretilmez)

**Workspace izolasyonu:** `X-Workspace-Id` header'ıyla yalnızca çağıran
organizasyonun sözleşmelerine erişilebilir; başka workspace'in sözleşmesi
için 404 döner (IDOR koruması).


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_id_embed_session_post200_response import ApiV1DemandsIdEmbedSessionPost200Response
from imzala_client.models.api_v1_demands_id_embed_session_post_request import ApiV1DemandsIdEmbedSessionPostRequest
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | Sözleşme (demand) ID
    api_v1_demands_id_embed_session_post_request = {"party_id":"f47ac10b-58cc-4372-a567-0e02b2c3d479"} # ApiV1DemandsIdEmbedSessionPostRequest | 

    try:
        # Gömülü imza oturumu başlat (embed token mint)
        api_response = api_instance.api_v1_demands_id_embed_session_post(id, api_v1_demands_id_embed_session_post_request)
        print("The response of DemandsApi->api_v1_demands_id_embed_session_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_id_embed_session_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**| Sözleşme (demand) ID | 
 **api_v1_demands_id_embed_session_post_request** | [**ApiV1DemandsIdEmbedSessionPostRequest**](ApiV1DemandsIdEmbedSessionPostRequest.md)|  | 

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
**200** | Token üretildi |  -  |
**400** | &#x60;party_id&#x60; eksik veya geçersiz format.  |  -  |
**401** | API key geçersiz veya eksik |  -  |
**403** | **INSUFFICIENT_SCOPE** — API key&#39;in &#x60;demands&#x60; scope&#39;u yok veya gömülü imza özelliği bu API anahtarı için devre dışı.  |  -  |
**404** | Sözleşme veya taraf bulunamadı. İki durum ayrıştırılmaz (IDOR koruması): - &#x60;Sözleşme bulunamadı&#x60; — demand bu workspace&#39;te yok - &#x60;Taraf bulunamadı&#x60; — party_id bu demand&#39;e ait değil  |  -  |
**409** | Token üretilemez. Olası nedenler: - &#x60;Bu taraf zaten imzaladı&#x60; — taraf imzalamış - &#x60;Bu taraf imzayı reddetti&#x60; — taraf reddetmiş - &#x60;embed_allowed_origins tanımlı değil&#x60; — API anahtarında izin verilen   origin listesi boş; dashboard&#39;dan API anahtarı düzenleyerek ekleyin.  |  -  |
**429** | Rate limit aşıldı (per API key + demand + party kombinasyonu) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_id_get**
> ApiV1DemandsIdGet200Response api_v1_demands_id_get(id)

Sözleşme durumu + imza ilerlemesi

### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_id_get200_response import ApiV1DemandsIdGet200Response
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 

    try:
        # Sözleşme durumu + imza ilerlemesi
        api_response = api_instance.api_v1_demands_id_get(id)
        print("The response of DemandsApi->api_v1_demands_id_get:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_id_get: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  | 

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
**200** | Başarılı |  -  |
**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_id_items_post**
> UpsertItemsResponse api_v1_demands_id_items_post(id, upsert_items_request)

Sözleşmeye alan yerleştir (replace)

Sözleşmenin sayfalarına imza ve form alanlarını koordinatlarıyla
yerleştirir. Tipik kullanım: `POST /api/v1/demands/upload` ile
demand yarat (`dispatch_notifications=false` ile auto-dispatch'i
ertele) → bu endpoint ile alanları yerleştir → dashboard üzerinden
ya da `POST /api/v1/demands/{id}/reminders` ile gönderim başlat.

### Replace mode

Endpoint **replace** semantiği taşır:
- `page_ids` **omitted** → demand'in TÜM mevcut item'ları silinir,
  body'dekiler yaratılır (full replace).
- `page_ids: [N, M, ...]` → sadece bu sayfaların item'ları silinir,
  diğer sayfalardaki item'lar korunur. Body'deki `items[].page_id`
  değerleri `page_ids` listesinde olmalıdır.

### Item type'ları

| `item_type` | `party_id` zorunlu? | `config` örneği |
|-------------|---------------------|-----------------|
| `signature` | ✅ | (yok) |
| `text` | ❌ | `{ default_content }` |
| `dynamic_text` | ✅ | `{ defaultSource: "{{signer.full_name}}" }` |
| `cells` | ✅ | `{ cellCount: 11, defaultSource: "{{signer.government_id}}" }` |
| `date` | ✅ | `{ defaultSource, defaultValue }` |
| `dropdown` | ✅ | `{ options: [{label,value}], defaultValue }` |
| `checkbox` | ✅ | `{ checkedByDefault: false }` |
| `radio` | ✅ | `{ options: [{label,value}], defaultValue }` |
| `stamp` | ❌ | `{ stampData: "data:image/png;base64,..." }` |

### Sistem değişkenleri (dynamic_text/cells/date `config.defaultSource`)

`{{signer.first_name}}`, `{{signer.last_name}}`, `{{signer.full_name}}`,
`{{signer.email}}`, `{{signer.phone}}`, `{{signer.government_id}}`,
`{{signer.birth_date}}`, `{{signer.sign_date}}`, `{{contract.title}}`,
`{{sender.full_name}}`, `{{current.date}}`, `{{current.datetime}}`.

### Workspace izolasyonu

X-API-Key middleware demand'i workspace'e göre filtreler;
başka workspace'in demand'ine item ekleyemezsiniz (404 döner).

### Status kontrolü

Sadece `PENDING` demand edit edilebilir. `COMPLETED`, `EXPIRED`,
`REJECTED` için 403.

### Örnek

```bash
curl -X POST https://api-prd.imzala.org/api/v1/demands/$DEMAND_ID/items \
  -H "X-API-Key: imz_..." \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "page_id": 12345,
        "party_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
        "item_type": "signature",
        "position_x": 0.5, "position_y": 0.85,
        "width": 0.2, "height": 0.05,
        "is_required": true
      },
      {
        "page_id": 12345,
        "party_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
        "item_type": "cells",
        "position_x": 0.1, "position_y": 0.5,
        "width": 0.4, "height": 0.04,
        "slug": "tc",
        "config": { "cellCount": 11, "defaultSource": "{{signer.government_id}}" }
      }
    ]
  }'
```


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.upsert_items_request import UpsertItemsRequest
from imzala_client.models.upsert_items_response import UpsertItemsResponse
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    upsert_items_request = imzala_client.UpsertItemsRequest() # UpsertItemsRequest | 

    try:
        # Sözleşmeye alan yerleştir (replace)
        api_response = api_instance.api_v1_demands_id_items_post(id, upsert_items_request)
        print("The response of DemandsApi->api_v1_demands_id_items_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_id_items_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  | 
 **upsert_items_request** | [**UpsertItemsRequest**](UpsertItemsRequest.md)|  | 

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
**200** | Alanlar yerleştirildi |  -  |
**400** | Validation hatası. Olası &#x60;error&#x60; değerleri: - &#x60;INVALID_ITEMS_BODY&#x60; — items array değil - &#x60;VALIDATION_ERROR&#x60; — position bounds, slug regex, party-required - &#x60;INVALID_PAGE_ID&#x60; — page_id demand&#39;e ait değil veya page_ids&#39;te yok - &#x60;INVALID_PARTY_ID&#x60; — party_id demand&#39;e ait değil - &#x60;PAGE_ID_REQUIRED&#x60;: bir öğede tam sayı &#x60;page_id&#x60; yok - &#x60;INVALID_ITEM_TYPE&#x60;: desteklenmeyen &#x60;item_type&#x60; (mesaj izinli türleri listeler)  |  -  |
**401** | API key geçersiz veya eksik |  -  |
**403** | &#x60;DEMAND_NOT_EDITABLE&#x60; — demand status ≠ &#x60;PENDING&#x60; (COMPLETED, EXPIRED, REJECTED edit edilemez).  |  -  |
**404** | &#x60;DEMAND_NOT_FOUND&#x60; — demand bu workspace&#39;te yok (cross-workspace IDOR koruması).  |  -  |
**409** | &#x60;DUPLICATE_SIGNATURE_FIELD&#x60; — aynı &#x60;(page_id, party_id, position_x, position_y)&#x60; tuple&#39;ında ikinci &#x60;signature&#x60; alanı yaratıldı. DB-level partial unique constraint engelledi. Pozisyonu değiştirip tekrar deneyin.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_id_parties_party_id_resend_post**
> ApiV1DemandsIdPartiesPartyIdResendPost200Response api_v1_demands_id_parties_party_id_resend_post(id, party_id)

Tekil tarafa imza davetini tekrar gönder

Belirtilen tarafa imza davetini (SMS/e-posta/WhatsApp, sözleşme
ayarına göre) tekrar gönderir. İmzalamış/reddetmiş tarafa veya sıralı
imzada sırası gelmemiş tarafa gönderilemez (409).


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_id_parties_party_id_resend_post200_response import ApiV1DemandsIdPartiesPartyIdResendPost200Response
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 
    party_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 

    try:
        # Tekil tarafa imza davetini tekrar gönder
        api_response = api_instance.api_v1_demands_id_parties_party_id_resend_post(id, party_id)
        print("The response of DemandsApi->api_v1_demands_id_parties_party_id_resend_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_id_parties_party_id_resend_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  | 
 **party_id** | **UUID**|  | 

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
**200** | Gönderildi |  -  |
**404** | Kayıt bulunamadı |  -  |
**409** | Tekrar gönderim yapılamaz. &#x60;error&#x60; insan-okur mesajı taşır. - İmzalamış, reddetmiş veya sıralı imzada sırası gelmemiş taraf   (&#x60;code&#x60; alanı yok) - &#x60;DEMAND_NOT_DISPATCHED&#x60;: sözleşme henüz imzaya gönderilmedi (taslak) - &#x60;DEMAND_NOT_DISPATCHABLE&#x60;: sözleşme tamamlanmış veya iptal edilmiş - &#x60;DEMAND_EXPIRED&#x60;: sözleşmenin imza süresi geçmiş  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_id_pdf_get**
> bytes api_v1_demands_id_pdf_get(id)

İmzalı sözleşme PDF'i (auth'lu indirme)

Tamamlanmış sözleşmenin imzalı PDF'ini indirir. Public
`/sonuc/{id}/pdf`'in aksine API key ownership'i zorunludur.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 

    try:
        # İmzalı sözleşme PDF'i (auth'lu indirme)
        api_response = api_instance.api_v1_demands_id_pdf_get(id)
        print("The response of DemandsApi->api_v1_demands_id_pdf_get:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_id_pdf_get: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  | 

### Return type

**bytes**

### Authorization

[ApiKeyAuth](../README.md#ApiKeyAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/pdf, application/json

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | PDF |  -  |
**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_id_timeline_get**
> ApiV1DemandsIdTimelineGet200Response api_v1_demands_id_timeline_get(id)

İmza denetim izi (maskeli)

Sözleşmenin imza denetim izini (görüntüleme/imza/red olayları) döner.
KVKK: IP `ip_masked` (son oktet maskeli), actor e-postası maskeli; ham
IP/cihaz asla döndürülmez.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_id_timeline_get200_response import ApiV1DemandsIdTimelineGet200Response
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | 

    try:
        # İmza denetim izi (maskeli)
        api_response = api_instance.api_v1_demands_id_timeline_get(id)
        print("The response of DemandsApi->api_v1_demands_id_timeline_get:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_id_timeline_get: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**|  | 

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
**200** | Başarılı |  -  |
**404** | Kayıt bulunamadı |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_post**
> ApiV1DemandsPost201Response api_v1_demands_post(create_demand_request, idempotency_key=idempotency_key)

Sözleşme oluştur (şablondan)

Belirtilen şablondan yeni bir sözleşme oluşturur, taraf bilgilerini
kaydeder, dynamic field'ları `variables` payload'undan doldurur ve
imzalama URL'lerini döner.

**Variable resolution:**
- Item'ın `template_party_id` non-null → `party_mapping[i].variables`'ta
  o slug var ise oradan uygulanır
- Yoksa root `variables`'tan fallback
- Hiçbiri yoksa item boş kalır (signer manuel doldurabilir,
  `editable: true` ise)

**Validation:**
- `party_mapping[i].variables` ve root `variables` object olmalı
- Variable value'ları `string | number | boolean | null` olmalı
  (object/array reject)
- `template_party_id` party_mapping içinde unique olmalı


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_post201_response import ApiV1DemandsPost201Response
from imzala_client.models.create_demand_request import CreateDemandRequest
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    create_demand_request = {"template_id":"7ec4b653-e84a-47f1-9e0b-7671e1aae2a1","title":"Vize Danışmanlığı - Ayşe Yılmaz","party_mapping":[{"template_party_id":"e5b4e0cb-c2d5-473f-9f62-44d51c76f56e","first_name":"Ayşe","last_name":"Yılmaz","email":"ayse@example.com","phone":"+905551112233","government_id":"36747474747","variables":{"adres":"Atatürk Cad. No: 12, Çankaya/Ankara","danismanlik_ucreti":"5.000 TL","danismanlik_notlar":"Schengen vize başvuru danışmanlığı","randevu_takibi_ucreti":"1.500 TL","randevu_takibi_notlar":"Konsolosluk randevu takibi 60 gün","genel_toplam":"6.500 TL"}}]} # CreateDemandRequest | 
    idempotency_key = 'idempotency_key_example' # str | Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)

    try:
        # Sözleşme oluştur (şablondan)
        api_response = api_instance.api_v1_demands_post(create_demand_request, idempotency_key=idempotency_key)
        print("The response of DemandsApi->api_v1_demands_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **create_demand_request** | [**CreateDemandRequest**](CreateDemandRequest.md)|  | 
 **idempotency_key** | **str**| Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  | [optional] 

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
**201** | Sözleşme oluşturuldu |  -  |
**400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate&#39;i bulundu: &lt;id&gt;\&quot;  &#x60;INVALID_EXPIRY_DATE&#x60;: &#x60;expiry_date&#x60; çözümlenemiyor veya takvimde olmayan bir gün (ör. &#x60;2026-02-30&#x60;). Bu hatada &#x60;error&#x60; insan-okur mesajı, &#x60;code&#x60; alanı makinece okunur kodu taşır.  |  -  |
**401** | API key geçersiz veya eksik |  -  |
**402** | Yetersiz kredi (INSUFFICIENT_CREDITS) |  -  |
**403** | **SMS_CUSTOMIZATION_NOT_ALLOWED** — Body&#39;de &#x60;sms_content&#x60; alanı dolu gönderildi ama çağıran organizasyon PRO/ENTERPRISE planda değil veya kendi SMS sağlayıcı config&#39;i (sender_name dolu) yok. &#x60;sms_content&#x60; alanını çıkarın veya planınızı yükseltip kendi SMS sağlayıcınızı tanımlayın.  |  -  |
**409** | Üç ayrı kod döner.  &#x60;IDEMPOTENCY_KEY_REUSED&#x60; — aynı &#x60;Idempotency-Key&#x60; daha önce FARKLI bir içerikle kullanıldı. Sessizce eski sözleşmeyi döndürmek \&quot;gönderdim sandım\&quot; kazası üretirdi; yeni sözleşme için yeni anahtar gönderin. Gövdede eski sözleşmenin &#x60;demand_id&#x60; alanı döner.  &#x60;IDEMPOTENCY_UNVERIFIABLE&#x60;: bu &#x60;Idempotency-Key&#x60; daha önce bir sözleşme üretti, ancak bu isteğin aynı içerikte olduğu doğrulanamadı. Eski sözleşme sessizce döndürülmez. Gövdedeki &#x60;demand_id&#x60; ile durumu sorgulayın; yeni bir anahtarla körlemesine tekrar denemek ikinci bir sözleşme oluşturur.  &#x60;DUPLICATE_SUSPECTED&#x60; — idempotency anahtarı GÖNDERİLMEDİ ve aynı API anahtarı son 10 dakika içinde aynı içeriği (aynı belge/şablon + aynı taraf kümesi) zaten gönderdi. Gövdede mevcut sözleşmenin &#x60;demand_id&#x60; alanı döner. Kasten tekrarlamak için &#x60;force&#x60; gönderin.  Bu kapı yalnız anahtarsız çağrılarda çalışır: &#x60;Idempotency-Key&#x60; gönderen istemci zaten tekrar-korumalıdır.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_demands_upload_post**
> ApiV1DemandsUploadPost201Response api_v1_demands_upload_post(files, parties, idempotency_key=idempotency_key, order=order, title=title, description=description, field_template_id=field_template_id, force=force, send_invitations=send_invitations, on_anchor_miss=on_anchor_miss)

Dosya upload ile sözleşme oluştur (şablonsuz)

Multipart/form-data ile doğrudan dosya yükleyerek sözleşme oluşturur
(şablon kullanmadan). Tek PDF/DOC/DOCX/ODT/RTF/TXT veya 1-20 görsel
(JPG/PNG/HEIC/TIFF/WEBP) kabul eder; görseller sırayla tek PDF'e
birleştirilir, office formatları LibreOffice ile PDF'e çevrilir.


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_demands_upload_post201_response import ApiV1DemandsUploadPost201Response
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    files = None # List[bytes] | 1 belge VEYA 1-20 görsel
    parties = 'parties_example' # str | JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  `field_template_id` gönderildiğinde her party AYRICA `template_party_id` (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir. 
    idempotency_key = 'idempotency_key_example' # str | Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme `reused: true` + `created_at` ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse `409 IDEMPOTENCY_KEY_REUSED` döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; `dispatch`, `variables_applied`, `field_layout` gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki `idempotency_key` alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE `400 INVALID_IDEMPOTENCY_KEY`.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  (optional)
    order = 'order_example' # str | Çoklu görsel sırası (JSON array of indices, örnek \\\"[0,2,1]\\\") (optional)
    title = 'title_example' # str |  (optional)
    description = 'description_example' # str |  (optional)
    field_template_id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | Alan Şablonu (`kind: FIELD_LAYOUT`) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - `template_id` ile **birlikte gönderilemez**   (400 `FIELD_TEMPLATE_CONFLICT`). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için `POST /api/v1/field-templates/{id}/preview-layout`.  (optional)
    force = 'force_example' # str | Kopya kapısını bilerek geç. Yalnız `Idempotency-Key` GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  (optional)
    send_invitations = 'send_invitations_example' # str | `\\\"true\\\"` (veya `\\\"1\\\"`) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca `POST /api/v1/demands/{id}/parties/{partyId}/resend` çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 `INVALID_SEND_INVITATIONS`   döner (fail-closed). - **Açık değerler:** `\\\"true\\\"`, `\\\"1\\\"`, `\\\"all\\\"`, `\\\"email\\\"`, `\\\"sms\\\"`. - **Kapalı değerler:** `\\\"false\\\"`, `\\\"0\\\"`, `\\\"off\\\"`, `\\\"no\\\"`,   `\\\"hayir\\\"`, `\\\"hayır\\\"`. - Ad bilerek `dispatch_notifications` **değildir**: o   parametre `POST /api/v1/demands` ve `/demands/bulk`   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Hangi kanalın gideceğini sözleşmenin bildirim ayarları   ve tarafın `send_sms`/`send_email` bayrakları belirler.   Bu uçtan yaratılan sözleşmelerde ikisi de açık doğar:   pratikte **hem SMS hem e-posta** gider. Kanal seçimi   henüz yoktur. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (`dispatch.error = DISPATCH_TOO_MANY`), tarafları   `resend` ile çağırın. - 🔴 **Tekrar denemeye dikkat:** bu ucun idempotency   anahtarı yoktur. İstemci zaman aşımında isteği körlemesine   tekrarlarsanız **yeni bir sözleşme, yeni kredi ve ikinci   bir davet seti** oluşur. Zaman aşımında yeniden göndermek   yerine `GET /api/v1/demands?...` ile sonucu doğrulayın.  (optional)
    on_anchor_miss = 'on_anchor_miss_example' # str | Çapa bulunamadığında ne yapılacağı. Yalnız `field_template_id` ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, `block` uygulanır. - `drop`, yalnız şablonun İLGİLİ TÜM alanları zaten `DROP`   ise uygulanır; aksi halde yok sayılır, `block` uygulanır   ve yanıtta `ON_ANCHOR_MISS_NOT_RELAXED` uyarısı döner. - İmza alanlarında `drop` hiçbir koşulda uygulanmaz. - `fallback` bu API'de **yoktur** (400 `INVALID_ON_ANCHOR_MISS`).  Yanıttaki `data.field_layout.on_anchor_miss` istenen değil, **uygulanan** değeri taşır.  (optional)

    try:
        # Dosya upload ile sözleşme oluştur (şablonsuz)
        api_response = api_instance.api_v1_demands_upload_post(files, parties, idempotency_key=idempotency_key, order=order, title=title, description=description, field_template_id=field_template_id, force=force, send_invitations=send_invitations, on_anchor_miss=on_anchor_miss)
        print("The response of DemandsApi->api_v1_demands_upload_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_demands_upload_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **files** | **List[bytes]**| 1 belge VEYA 1-20 görsel | 
 **parties** | **str**| JSON array of party objects. Her party: first_name, last_name (zorunlu), email VEYA phone (zorunlu).  &#x60;field_template_id&#x60; gönderildiğinde her party AYRICA &#x60;template_party_id&#x60; (Alan Şablonu rolü) taşımak zorundadır ve şablondaki her rol tam olarak bir kez eşlenmelidir.  | 
 **idempotency_key** | **str**| Çağıranın kendi referansı (sipariş / dosya numarası olabilir). Aynı API anahtarı + aynı anahtarla gelen İKİNCİ istek yeni sözleşme YARATMAZ: ilk sözleşme &#x60;reused: true&#x60; + &#x60;created_at&#x60; ile döner, kredi düşülmez, davet gönderilmez.  🔴 Anahtar KALICIDIR (süre sınırı yoktur). Numaralandırmanızı yıl döngüsünde tekrar kullanıyorsanız yıl/ön ek ekleyin. Aynı anahtar FARKLI içerikle gelirse &#x60;409 IDEMPOTENCY_KEY_REUSED&#x60; döner.  Tekrar yanıtı ilk yanıtın birebir kopyası DEĞİLDİR: yalnız kalıcı alanlar (kimlik, durum, sayfalar, imza bağlantıları) döner; &#x60;dispatch&#x60;, &#x60;variables_applied&#x60;, &#x60;field_layout&#x60; gibi o isteğin çalışma zamanı çıktıları YOKTUR. Anahtara kişisel veri yazmayın (alan düz metin saklanır).  Multipart uçlarda başlık yerine gövdedeki &#x60;idempotency_key&#x60; alanı da kullanılabilir; ikisi birden gönderilip ÇELİŞİRSE &#x60;400 INVALID_IDEMPOTENCY_KEY&#x60;.  Biçim: 1-255 karakter, boşluksuz yazdırılabilir ASCII.  | [optional] 
 **order** | **str**| Çoklu görsel sırası (JSON array of indices, örnek \\\&quot;[0,2,1]\\\&quot;) | [optional] 
 **title** | **str**|  | [optional] 
 **description** | **str**|  | [optional] 
 **field_template_id** | **UUID**| Alan Şablonu (&#x60;kind: FIELD_LAYOUT&#x60;) kimliği. Verilirse yüklenen belgeye şablonun alan yerleşimi uygulanır.  - Yüklenen dosya **PDF olmak zorundadır** (sihirli bayt   doğrulaması; DOCX/ODT/RTF bu yolda kabul edilmez) ve tek   dosya olmalıdır. - &#x60;template_id&#x60; ile **birlikte gönderilemez**   (400 &#x60;FIELD_TEMPLATE_CONFLICT&#x60;). - Çözümleme sözleşme yaratımından ve kredi düşümünden   ÖNCE koşar: 422 dönen bir istek sözleşme yaratmaz, kredi   düşmez. - Ön kontrol için &#x60;POST /api/v1/field-templates/{id}/preview-layout&#x60;.  | [optional] 
 **force** | **str**| Kopya kapısını bilerek geç. Yalnız &#x60;Idempotency-Key&#x60; GÖNDERİLMEYEN çağrılarda anlamlıdır; aynı belgeyi aynı taraflara kasten ikinci kez göndermek için.  | [optional] 
 **send_invitations** | **str**| &#x60;\\\&quot;true\\\&quot;&#x60; (veya &#x60;\\\&quot;1\\\&quot;&#x60;) verilirse imza davetleri **aynı istekte** gönderilir; taraf başına ayrıca &#x60;POST /api/v1/demands/{id}/parties/{partyId}/resend&#x60; çağırmanız gerekmez.  - **Varsayılan kapalıdır.** Bu uç tarihsel olarak davet   göndermiyordu; mevcut entegrasyonların davranışı   değişmemelidir. Tanınmayan değer 400 &#x60;INVALID_SEND_INVITATIONS&#x60;   döner (fail-closed). - **Açık değerler:** &#x60;\\\&quot;true\\\&quot;&#x60;, &#x60;\\\&quot;1\\\&quot;&#x60;, &#x60;\\\&quot;all\\\&quot;&#x60;, &#x60;\\\&quot;email\\\&quot;&#x60;, &#x60;\\\&quot;sms\\\&quot;&#x60;. - **Kapalı değerler:** &#x60;\\\&quot;false\\\&quot;&#x60;, &#x60;\\\&quot;0\\\&quot;&#x60;, &#x60;\\\&quot;off\\\&quot;&#x60;, &#x60;\\\&quot;no\\\&quot;&#x60;,   &#x60;\\\&quot;hayir\\\&quot;&#x60;, &#x60;\\\&quot;hayır\\\&quot;&#x60;. - Ad bilerek &#x60;dispatch_notifications&#x60; **değildir**: o   parametre &#x60;POST /api/v1/demands&#x60; ve &#x60;/demands/bulk&#x60;   uçlarında vardır ve orada varsayılanı **açıktır**.   Karıştırılırsa sessizce ters anlam üretir. - Hangi kanalın gideceğini sözleşmenin bildirim ayarları   ve tarafın &#x60;send_sms&#x60;/&#x60;send_email&#x60; bayrakları belirler.   Bu uçtan yaratılan sözleşmelerde ikisi de açık doğar:   pratikte **hem SMS hem e-posta** gider. Kanal seçimi   henüz yoktur. - En çok **20 tarafa** davet gönderilir; üstünde sözleşme   yine oluşur ama davet gönderilmez   (&#x60;dispatch.error &#x3D; DISPATCH_TOO_MANY&#x60;), tarafları   &#x60;resend&#x60; ile çağırın. - 🔴 **Tekrar denemeye dikkat:** bu ucun idempotency   anahtarı yoktur. İstemci zaman aşımında isteği körlemesine   tekrarlarsanız **yeni bir sözleşme, yeni kredi ve ikinci   bir davet seti** oluşur. Zaman aşımında yeniden göndermek   yerine &#x60;GET /api/v1/demands?...&#x60; ile sonucu doğrulayın.  | [optional] 
 **on_anchor_miss** | **str**| Çapa bulunamadığında ne yapılacağı. Yalnız &#x60;field_template_id&#x60; ile anlamlıdır.  Bu parametre şablon ayarını yalnızca **sıkılaştırabilir**: - Gönderilmezse şablon ayarı geçerli değildir, &#x60;block&#x60; uygulanır. - &#x60;drop&#x60;, yalnız şablonun İLGİLİ TÜM alanları zaten &#x60;DROP&#x60;   ise uygulanır; aksi halde yok sayılır, &#x60;block&#x60; uygulanır   ve yanıtta &#x60;ON_ANCHOR_MISS_NOT_RELAXED&#x60; uyarısı döner. - İmza alanlarında &#x60;drop&#x60; hiçbir koşulda uygulanmaz. - &#x60;fallback&#x60; bu API&#39;de **yoktur** (400 &#x60;INVALID_ON_ANCHOR_MISS&#x60;).  Yanıttaki &#x60;data.field_layout.on_anchor_miss&#x60; istenen değil, **uygulanan** değeri taşır.  | [optional] 

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
**201** | Sözleşme oluşturuldu |  -  |
**400** | Geçersiz istek. Örnek hatalar: - \&quot;template_id gerekli\&quot; - \&quot;party_mapping gerekli (en az 1 taraf)\&quot; - \&quot;party_mapping[0].first_name ve last_name gerekli\&quot; - \&quot;party_mapping[0].email veya phone gerekli\&quot; - \&quot;party_mapping[0].variables object olmalı\&quot; - \&quot;party_mapping[0].variables.adres value&#39;su string|number|boolean|null olmali\&quot; - \&quot;variables object olmalı\&quot; - \&quot;template_party_id duplicate&#39;i bulundu: &lt;id&gt;\&quot;  |  -  |
**401** | API key geçersiz veya eksik |  -  |
**402** | Yetersiz kredi |  -  |
**403** | Organizasyona bağlı API anahtarı ile uyuşmayan &#x60;X-Workspace-Id&#x60; (&#x60;WORKSPACE_MISMATCH&#x60;). Yalnız &#x60;field_template_id&#x60; yolunda.  |  -  |
**404** | Alan Şablonu bulunamadı (&#x60;TEMPLATE_NOT_FOUND&#x60;) |  -  |
**409** | Üç ayrı kod döner.  &#x60;IDEMPOTENCY_KEY_REUSED&#x60; — aynı &#x60;Idempotency-Key&#x60; daha önce FARKLI bir içerikle kullanıldı. Sessizce eski sözleşmeyi döndürmek \&quot;gönderdim sandım\&quot; kazası üretirdi; yeni sözleşme için yeni anahtar gönderin. Gövdede eski sözleşmenin &#x60;demand_id&#x60; alanı döner.  &#x60;IDEMPOTENCY_UNVERIFIABLE&#x60;: bu &#x60;Idempotency-Key&#x60; daha önce bir sözleşme üretti, ancak bu isteğin aynı içerikte olduğu doğrulanamadı. Eski sözleşme sessizce döndürülmez. Gövdedeki &#x60;demand_id&#x60; ile durumu sorgulayın; yeni bir anahtarla körlemesine tekrar denemek ikinci bir sözleşme oluşturur.  &#x60;DUPLICATE_SUSPECTED&#x60; — idempotency anahtarı GÖNDERİLMEDİ ve aynı API anahtarı son 10 dakika içinde aynı içeriği (aynı belge/şablon + aynı taraf kümesi) zaten gönderdi. Gövdede mevcut sözleşmenin &#x60;demand_id&#x60; alanı döner. Kasten tekrarlamak için &#x60;force&#x60; gönderin.  Bu kapı yalnız anahtarsız çağrılarda çalışır: &#x60;Idempotency-Key&#x60; gönderen istemci zaten tekrar-korumalıdır.  |  -  |
**413** | Dosya boyut limiti aşıldı (FILE_TOO_LARGE) |  -  |
**415** | &#x60;field_template_id&#x60; yolunda PDF olmayan dosya (&#x60;UNSUPPORTED_FILE_TYPE&#x60;).  |  -  |
**422** | Görsel okunamadı (&#x60;IMAGE_DECODE_FAILED&#x60;) **veya** alan yerleşimi bu belgeye uygulanamadı (&#x60;FIELD_LAYOUT_UNRESOLVED&#x60;). İkinci durumda sözleşme oluşturulmaz ve kredi düşülmez.  |  -  |
**429** | &#x60;TOO_MANY_REQUESTS&#x60; — API anahtarı başına dakikada 30 istek sınırı aşıldı. Bu uç yükleme + PDF ayrıştırma yapar ve &#x60;send_invitations&#x60; ile gerçek SMS/e-posta tetikleyebilir; sınır kötüye kullanımın hızını kırmak içindir.  |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **api_v1_field_templates_id_preview_layout_post**
> ApiV1FieldTemplatesIdPreviewLayoutPost200Response api_v1_field_templates_id_preview_layout_post(id, files, on_anchor_miss=on_anchor_miss)

Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener

Bir Alan Şablonunun (`kind: FIELD_LAYOUT`) yüklediğiniz PDF'e nasıl
uygulanacağını, **hiçbir yan etki üretmeden** döner.

🔴 Sözleşme oluşturmaz, kredi düşmez, dosyanızı saklamaz.

### Neden var

`POST /api/v1/demands/upload` + `field_template_id` çağrısı, alan
yerleşimi çözülemezse 422 döner ve hiçbir şey yaratmaz. Bu uç, o
çağrıyı yapmadan önce sonucu görmenizi sağlar: hangi alanların nereye
yerleşeceğini, hangi çapaların tutmadığını ve belgenin gönderime uygun
olup olmadığını.

API'de insan önizleme ekranı olmadığından, yerleşimin doğruluğunu
gönderimden önce kontrol etme imkânı bu uçla sunulur; entegrasyonunuzda
bu adımı çalıştırmanız önerilir.

### Durum kodu semantiği

Çözümlenemeyen bir belge de **200** döner (`data.resolvable: false`) —
kuru koşumun cevabı "uygulanamaz"dır, isteğin kendisi başarısız
değildir. Belgenin okunamaması (parola korumalı PDF, sayfa tavanı)
gerçek bir girdi hatasıdır ve kendi 4xx kodunu döner.

### Sınırlar

Yalnızca PDF (sihirli bayt doğrulaması), tek dosya, en fazla 20 MB.
Bu uç kredi tüketmediği için kullanıcı başına dakikada 5 istekle
sınırlıdır (aşımda 429 `RATE_LIMITED`).


### Example

* Api Key Authentication (ApiKeyAuth):

```python
import imzala_client
from imzala_client.models.api_v1_field_templates_id_preview_layout_post200_response import ApiV1FieldTemplatesIdPreviewLayoutPost200Response
from imzala_client.rest import ApiException
from pprint import pprint

# Defining the host is optional and defaults to https://api-prd.imzala.org
# See configuration.py for a list of all supported configuration parameters.
configuration = imzala_client.Configuration(
    host = "https://api-prd.imzala.org"
)

# The client must configure the authentication and authorization parameters
# in accordance with the API server security policy.
# Examples for each auth method are provided below, use the example that
# satisfies your auth use case.

# Configure API key authorization: ApiKeyAuth
configuration.api_key['ApiKeyAuth'] = os.environ["API_KEY"]

# Uncomment below to setup prefix (e.g. Bearer) for API key, if needed
# configuration.api_key_prefix['ApiKeyAuth'] = 'Bearer'

# Enter a context with an instance of the API client
with imzala_client.ApiClient(configuration) as api_client:
    # Create an instance of the API class
    api_instance = imzala_client.DemandsApi(api_client)
    id = UUID('38400000-8cf0-11bd-b23e-10b96e4ef00d') # UUID | Alan Şablonu (FIELD_LAYOUT) kimliği
    files = None # List[bytes] | Tek PDF belge
    on_anchor_miss = 'on_anchor_miss_example' # str | `POST /api/v1/demands/upload` ile aynı semantik (yalnız sıkılaştırır, gönderilmezse `block`).  (optional)

    try:
        # Alan Şablonu yerleşimini bir PDF üzerinde KURU KOŞUM ile dener
        api_response = api_instance.api_v1_field_templates_id_preview_layout_post(id, files, on_anchor_miss=on_anchor_miss)
        print("The response of DemandsApi->api_v1_field_templates_id_preview_layout_post:\n")
        pprint(api_response)
    except Exception as e:
        print("Exception when calling DemandsApi->api_v1_field_templates_id_preview_layout_post: %s\n" % e)
```



### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **UUID**| Alan Şablonu (FIELD_LAYOUT) kimliği | 
 **files** | **List[bytes]**| Tek PDF belge | 
 **on_anchor_miss** | **str**| &#x60;POST /api/v1/demands/upload&#x60; ile aynı semantik (yalnız sıkılaştırır, gönderilmezse &#x60;block&#x60;).  | [optional] 

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
**200** | Kuru koşum sonucu |  -  |
**400** | Dosya gönderilmedi (&#x60;FILE_REQUIRED&#x60;) veya &#x60;on_anchor_miss&#x60; geçersiz (&#x60;INVALID_ON_ANCHOR_MISS&#x60;) veya şablon bir Alan Şablonu değil (&#x60;NOT_A_FIELD_LAYOUT_TEMPLATE&#x60;).  |  -  |
**401** | API key geçersiz veya eksik |  -  |
**403** | Yetersiz scope (&#x60;INSUFFICIENT_SCOPE&#x60;) veya organizasyona bağlı API anahtarı ile uyuşmayan &#x60;X-Workspace-Id&#x60; (&#x60;WORKSPACE_MISMATCH&#x60;).  |  -  |
**404** | Alan Şablonu bulunamadı (&#x60;TEMPLATE_NOT_FOUND&#x60;) |  -  |
**413** | Dosya boyut limiti aşıldı |  -  |
**415** | PDF olmayan dosya (&#x60;UNSUPPORTED_FILE_TYPE&#x60;) |  -  |
**422** | Belge okunamadı: parola korumalı PDF (&#x60;ENCRYPTED_PDF&#x60;) veya metin çıkarma süre bütçesi aşıldı (&#x60;TEXT_EXTRACTION_TIMEOUT&#x60;).  |  -  |
**429** | Dakikalık önizleme sınırı aşıldı (&#x60;RATE_LIMITED&#x60;) |  -  |
**503** | Sınırlayıcı geçici olarak kullanılamıyor (&#x60;RATE_LIMITER_UNAVAILABLE&#x60;) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

