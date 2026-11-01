

# ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**partyId** | **UUID** |  |  [optional] |
|**sms** | **Boolean** |  |  [optional] |
|**email** | **Boolean** |  |  [optional] |
|**whatsapp** | **Boolean** |  |  [optional] |
|**error** | **String** | Yalnız o taraf için dolar. &#x60;DISPATCH_FAILED&#x60; — gönderim patladı. &#x60;RECIPIENT_QUOTA_EXCEEDED&#x60; — alıcı ekseni freni (telefon başına 3/sa + 10/gün); yalnız bu taraf ATLANDI, diğerleri gönderilmeye devam etti.  Bilinen değerler: DISPATCH_FAILED, RECIPIENT_QUOTA_EXCEEDED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  [optional] |
