

# CodedError

`error` alanında insan-okur mesaj, `code` alanında makinece okunur hata kodu taşıyan hata gövdesi. Hangi kodların döndüğü ilgili yanıtın açıklamasında listelenir. 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**success** | **Boolean** |  |  [optional] |
|**error** | **String** |  |  [optional] |
|**code** | **String** |  |  [optional] |
|**field** | **String** | Bazı kodlarda (ör. &#x60;TERM_INVALID&#x60;) hangi alanın reddedildiğini gösterir. Yalnız bu tür kodlarda bulunur; her hata gövdesinde YOKTUR.  |  [optional] |



