

# DocumentSealing

Belgenin mühür durumu. Karar ilerlemesinden ayrı bir olgudur.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**status** | **String** | Bilinen değerler: NONE, PENDING, SEALED, FAILED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  [optional] |
|**hasTimestamp** | **Boolean** | Yalnız &#x60;status&#x60; &#x60;SEALED&#x60; iken &#x60;true&#x60; olabilir. |  [optional] |
