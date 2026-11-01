

# WebhookDataContractTerm

`contract.expiring` / `contract.ended` / `contract.advanced` olaylarının ortak verisi. Yalnız takip alanlarını taşır; taraf adı, e-postası veya sözleşme başlığı BU GÖVDEDE YER ALMAZ.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**demandId** | **UUID** |  |  |
|**termEndDate** | **LocalDate** | Olay anında platformda kayıtlı takip bitiş tarihi. |  |
|**renewalType** | **String** | Bilinen değerler: AUTO_RENEW, FIXED_TERM. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  |
|**noticeDeadline** | **LocalDate** | İhbar için son gün (&#x60;term_end_date - notice_days&#x60;); tanımsızsa &#x60;null&#x60;. |  |
|**termState** | **String** | Olay anındaki takip durumu (bkz. &#x60;ContractTerm.state&#x60;).  Bilinen değerler: UNTRACKED, ACTIVE, ENDED, STOPPED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  |
|**dueKey** | **String** | İşçinin tekillik anahtarı (aynı olayı tekrar göndermemek için kullandığı dahili kayıt). Formatı sabit değildir, opak string olarak ele alın; idempotency için &#x60;id&#x60; alanını kullanın.  |  |
