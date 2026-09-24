# DocumentProgress

Belgeye atanmış tarafların karar ilerlemesi. Mühürden ve rızadan bağımsızdır: `COMPLETED`, belgeye atanmış tüm tarafların bu belge için karar verdiğini gösterir (isteğe bağlı bir belgede ret de karardır). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**status** | **str** | &#x60;PENDING&#x60;: henüz kimse karar vermedi. &#x60;PARTIAL&#x60;: tarafların bir kısmı karar verdi. &#x60;COMPLETED&#x60;: atanmış tüm taraflar karar verdi.  | [optional] 
**active_assigned_party_count** | **int** | Belgeye atanmış ve doldurulmuş (boş bırakılmamış) taraf sayısı. | [optional] 
**decided_party_count** | **int** | Bunlardan bu belge için karar vermiş olanların sayısı. | [optional] 
**completed_at** | **datetime** | Belgenin kendi tamamlanma anı. Sistem bu anı kaydetmediyse &#x60;null&#x60; döner; zarfın tamamlanma zamanı bu alana kopyalanmaz.  | [optional] 

## Example

```python
from imzala_client.models.document_progress import DocumentProgress

# TODO update the JSON string below
json = "{}"
# create an instance of DocumentProgress from a JSON string
document_progress_instance = DocumentProgress.from_json(json)
# print the JSON string representation of the object
print(DocumentProgress.to_json())

# convert the object into a dict
document_progress_dict = document_progress_instance.to_dict()
# create an instance of DocumentProgress from a dict
document_progress_from_dict = DocumentProgress.from_dict(document_progress_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


