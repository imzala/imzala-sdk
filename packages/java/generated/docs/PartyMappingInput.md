

# PartyMappingInput


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**templatePartyId** | **UUID** | GET /api/v1/templates/{id} cevabındaki parties[].id |  |
|**firstName** | **String** |  |  |
|**lastName** | **String** |  |  |
|**email** | **String** | email VEYA phone&#39;dan en az biri zorunlu |  [optional] |
|**phone** | **String** | E.164 format (örn. \&quot;+905551234567\&quot;) |  [optional] |
|**governmentId** | **String** | TC kimlik no (11 hane) |  [optional] |
|**birthDate** | **LocalDate** | ISO 8601 (örn. \&quot;1990-05-15\&quot;) |  [optional] |
|**sendSms** | **Boolean** |  |  [optional] |
|**sendEmail** | **Boolean** |  |  [optional] |
|**variables** | [**Map&lt;String, PartyMappingInputVariablesValue&gt;**](PartyMappingInputVariablesValue.md) | Bu PARTİYE AİT dynamic field&#39;lara gönderilen değerler. Slug bazında eşleşir. Item&#39;ın template_party_id&#39;si bu partiyle aynı olmalı; değilse değişken atlanır ve variables_ignored&#39;a düşürülür. Kaşe alanının slug&#39;ına &#x60;StampData&#x60; nesnesi verilebilir (bkz. &#x60;CreateDemandRequest.variables&#x60;).  |  [optional] |
|**documentVariables** | **Map&lt;String, Map&lt;String, PartyMappingInputVariablesValue&gt;&gt;** | Bu partinin BELGE BAŞINA değerleri. Anahtar şablon belgesinin kimliğidir (&#x60;GET /api/v1/templates/{id}/usage&#x60; cevabındaki &#x60;documents[].template_document_id&#x60;); belge başlığı ya da sırası anahtar olarak kullanılamaz. Aynı slug için en yüksek önceliklidir (bkz. &#x60;CreateDemandRequest.document_variables&#x60;).  |  [optional] |



