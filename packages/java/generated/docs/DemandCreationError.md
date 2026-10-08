

# DemandCreationError

Sözleşme oluşturma hatalarının açık taşıma zarfı. Alanlar hata türüne göre bulunur; yeni makine kodları istemci tarafından reddedilmez. Somut Belge Toplama zarfları DocumentCollectionPolicyError ve DocumentVerificationDisabledError şemaları ile yanıt örneklerinde açıklanır.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**success** | **Boolean** |  |  [optional] |
|**error** | **String** |  |  [optional] |
|**code** | **String** |  |  [optional] |
|**message** | **String** |  |  [optional] |
|**field** | **String** |  |  [optional] |
|**details** | **Map&lt;String, Object&gt;** |  |  [optional] |
