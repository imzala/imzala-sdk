

# CreatedDemand


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **UUID** |  |  [optional] |
|**title** | **String** |  |  [optional] |
|**status** | [**StatusEnum**](#StatusEnum) |  |  [optional] |
|**templateId** | **UUID** |  |  [optional] |
|**signingUrls** | [**List&lt;CreatedDemandSigningUrlsInner&gt;**](CreatedDemandSigningUrlsInner.md) |  |  [optional] |
|**resultUrl** | **URI** |  |  [optional] |
|**variablesApplied** | **List&lt;String&gt;** | Uygulanan TÜM slug&#39;ların unique union&#39;ı (sorted). Geriye dönük uyumluluk için korunur — yeni entegrasyonlar variables_applied_root + variables_applied_by_party kullanmalı.  |  [optional] |
|**variablesAppliedRoot** | **List&lt;String&gt;** | Root variables&#39;tan uygulanan slug listesi (sorted). |  [optional] |
|**variablesAppliedByParty** | **Map&lt;String, List&lt;String&gt;&gt;** | template_party_id → o partiye uygulanan slug listesi (sorted per party).  |  [optional] |
|**variablesIgnored** | **List&lt;String&gt;** | Gönderdiğiniz AMA hiçbir item&#39;a uygulanmayan slug&#39;lar (unique, sorted). Boş olmayınca yazım hatası yapmışsınız demektir — kontrol edin.  |  [optional] |



## Enum: StatusEnum

| Name | Value |
|---- | -----|
| DRAFT | &quot;DRAFT&quot; |
| PENDING | &quot;PENDING&quot; |



