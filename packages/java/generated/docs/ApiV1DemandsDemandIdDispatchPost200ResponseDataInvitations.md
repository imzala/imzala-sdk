

# ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitations


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**sent** | **Integer** | En az bir kanaldan başarıyla gönderilen taraf sayısı. |  [optional] |
|**results** | [**List&lt;ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner&gt;**](ApiV1DemandsDemandIdDispatchPost200ResponseDataInvitationsResultsInner.md) |  |  [optional] |
|**error** | **String** | &#x60;DISPATCH_SKIPPED&#x60; — sözleşme satırı okunamadı, körlemesine gönderim yapılmadı. (Taraf sayısı tavanı bu uçta davet fazından ÖNCE denetlenir ve **409 &#x60;DISPATCH_TOO_MANY&#x60;** olarak döner; 200 gövdesinde görünmez.)  Bilinen değerler: DISPATCH_SKIPPED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  [optional] |
