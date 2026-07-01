# TimestampRecord


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**timestamp_time** | **datetime** | TÜBİTAK KAMU SM&#39;nin onayladığı damga zamanı (UTC) | [optional] 
**tsa_authority** | **str** | Zaman damgası sağlayıcısı | [optional] 
**file_sha256** | **str** | Damgalanan dosyanın SHA-256 hash değeri (hex, 64 karakter) | [optional] 
**verify_url** | **str** | Damgayı doğrulamak için URL | [optional] 
**certificate_url** | **str** | Damga sertifikası URL&#39;i | [optional] 
**credits_used** | **int** | Bu damga için harcanan kredi miktarı | [optional] 
**credits_remaining** | **int** | İşlem sonrası kalan kredi bakiyesi | [optional] 

## Example

```python
from imzala_client.models.timestamp_record import TimestampRecord

# TODO update the JSON string below
json = "{}"
# create an instance of TimestampRecord from a JSON string
timestamp_record_instance = TimestampRecord.from_json(json)
# print the JSON string representation of the object
print(TimestampRecord.to_json())

# convert the object into a dict
timestamp_record_dict = timestamp_record_instance.to_dict()
# create an instance of TimestampRecord from a dict
timestamp_record_from_dict = TimestampRecord.from_dict(timestamp_record_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


