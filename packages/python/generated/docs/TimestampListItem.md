# TimestampListItem

Zaman damgası liste/detay öğesi. `timestamp_file_url` doğrudan indirilebilir bir bağlantı DEĞİLDİR (dahili depolama anahtarı olabilir). 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**original_file_name** | **str** |  | [optional] 
**original_file_size** | **int** |  | [optional] 
**timestamp_date** | **datetime** |  | [optional] 
**status** | **str** |  | [optional] 
**description** | **str** |  | [optional] 
**created_at** | **datetime** |  | [optional] 
**timestamp_file_url** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.timestamp_list_item import TimestampListItem

# TODO update the JSON string below
json = "{}"
# create an instance of TimestampListItem from a JSON string
timestamp_list_item_instance = TimestampListItem.from_json(json)
# print the JSON string representation of the object
print(TimestampListItem.to_json())

# convert the object into a dict
timestamp_list_item_dict = timestamp_list_item_instance.to_dict()
# create an instance of TimestampListItem from a dict
timestamp_list_item_from_dict = TimestampListItem.from_dict(timestamp_list_item_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


