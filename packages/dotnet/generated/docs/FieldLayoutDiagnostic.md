# ImzalaApiClient.Model.FieldLayoutDiagnostic
Alan yerleşimi tanısı. `code` makine sözleşmesidir, `message` insan metnidir (TR). **Bilinmeyen `code` toleranslı okunmalıdır**: yeni kod eklemek kırıcı değişiklik sayılmaz, istemci tanımadığı kodda `message` alanına düşmelidir.  Belgenizden çıkarılan hiçbir metin (çapa metni, eşleşen satır, sayfa metni, dosya adı) bu yapıya girmez. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Code** | **string** |  | 
**Severity** | **string** |  | 
**ItemId** | **int** | Şablon alanının kimliği. &#x60;-1&#x60; belge/taraf düzeyindeki tanılar için kullanılan sentinel değerdir.  | 
**Slug** | **string** |  | [optional] 
**Label** | **string** |  | [optional] 
**ItemType** | **string** |  | 
**Page** | **int?** | 1 tabanlı sayfa numarası; bilinmiyorsa null. | [optional] 
**Message** | **string** |  | 
**Detail** | **Dictionary&lt;string, Object&gt;** | Koda göre değişen, kapalı bir anahtar kümesi (yalnız sayısal değerler ve alan kimlikleri).  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

