

# FieldLayoutDiagnostic

Alan yerleşimi tanısı. `code` makine sözleşmesidir, `message` insan metnidir (TR). **Bilinmeyen `code` toleranslı okunmalıdır**: yeni kod eklemek kırıcı değişiklik sayılmaz, istemci tanımadığı kodda `message` alanına düşmelidir.  Belgenizden çıkarılan hiçbir metin (çapa metni, eşleşen satır, sayfa metni, dosya adı) bu yapıya girmez. 

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**code** | **String** |  |  |
|**severity** | [**SeverityEnum**](#SeverityEnum) |  |  |
|**itemId** | **Integer** | Şablon alanının kimliği. &#x60;-1&#x60; belge/taraf düzeyindeki tanılar için kullanılan sentinel değerdir.  |  |
|**slug** | **String** |  |  [optional] |
|**label** | **String** |  |  [optional] |
|**itemType** | **String** |  |  |
|**page** | **Integer** | 1 tabanlı sayfa numarası; bilinmiyorsa null. |  [optional] |
|**message** | **String** |  |  |
|**detail** | **Map&lt;String, Object&gt;** | Koda göre değişen, kapalı bir anahtar kümesi (yalnız sayısal değerler ve alan kimlikleri).  |  [optional] |



## Enum: SeverityEnum

| Name | Value |
|---- | -----|
| ERROR | &quot;ERROR&quot; |
| WARNING | &quot;WARNING&quot; |



