

# ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**templatePartyId** | **UUID** |  |  [optional] |
|**firstName** | **String** |  |  [optional] |
|**lastName** | **String** |  |  [optional] |
|**email** | **String** |  |  [optional] |
|**phone** | **String** | E.164 biçiminde telefon (ör. &#x60;+905551112233&#x60;). Ülke kodu ile birlikte gönderin; fazlalık şehirlerarası öneki (TRde baş sıfır, &#x60;+90 0542…&#x60;) sunucuda kaldırılır ve numara kanonik E.164 biçiminde saklanır. Tanınmayan girdi olduğu gibi korunur ama SMS teslim edilemeyebilir. |  [optional] |
|**governmentId** | **String** | T.C. kimlik numarası (11 hane) |  [optional] |
|**birthDate** | **LocalDate** | ISO 8601 (ör. 1990-05-15) |  [optional] |
|**sendSms** | **Boolean** |  |  [optional] |
|**sendEmail** | **Boolean** |  |  [optional] |
|**locale** | **SignerLocale** |  |  [optional] |
|**requirePhoneVerification** | **Boolean** | Telefon doğrulamasını yalnız bu taraf için açar ya da kapatır (bkz. &#x60;PartyMappingInput.require_phone_verification&#x60;). Sözleşmede telefon doğrulaması açıksa imzalayan taraflar için false etkisizdir. Geçersiz değer satırı &#x60;failed&#x60; (&#x60;error: \&quot;INVALID_PHONE_VERIFICATION\&quot;&#x60;), &#x60;true&#x60; iken boş &#x60;phone&#x60; satırı &#x60;failed&#x60; (&#x60;error: \&quot;PHONE_REQUIRED_FOR_VERIFICATION\&quot;&#x60;) yapar. |  [optional] |
|**customMessage** | **String** | Bu tarafa özel davet mesajı |  [optional] |
|**variables** | **Object** | Bu tarafa özel değişkenler |  [optional] |
