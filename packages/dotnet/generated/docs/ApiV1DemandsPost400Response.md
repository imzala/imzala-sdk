# ImzalaApiClient.Model.ApiV1DemandsPost400Response
Eski üretilmiş istemci türleri için belge seçimi uyumluluk görünümü. Tüm HTTP 400 yanıtlarını sınırlamaz; taşıma zarfı DemandCreationError'dur.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Success** | **bool** |  | [optional]
**Error** | **string** |  | [optional]
**Code** | **string** |  | [optional]
**Field** | **string** | Bazı kodlarda (ör. &#x60;TERM_INVALID&#x60;) hangi alanın reddedildiğini gösterir. Yalnız bu tür kodlarda bulunur; her hata gövdesinde YOKTUR.  | [optional]
**Details** | [**DocumentSelectionErrorDetails**](DocumentSelectionErrorDetails.md) |  | [optional]

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)
