# ContactSummary

Kişi (Contact) public görünümü. İç eşleşme alanları (matched_user_id / match_via / matched_at) ve şifreleme sütunları (*_enc / *_hash) DÂHİL DEĞİLDİR. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**first_name** | **str** |  | [optional] 
**last_name** | **str** |  | [optional] 
**email** | **str** |  | [optional] 
**phone** | **str** |  | [optional] 
**government_id** | **str** |  | [optional] 
**government_id_serial** | **str** |  | [optional] 
**job_title** | **str** |  | [optional] 
**company_id** | **UUID** |  | [optional] 
**company** | [**ContactSummaryCompany**](ContactSummaryCompany.md) |  | [optional] 
**notes** | **str** |  | [optional] 
**address_country** | **str** |  | [optional] 
**address_city** | **str** |  | [optional] 
**address_district** | **str** |  | [optional] 
**address_line** | **str** |  | [optional] 
**archived_at** | **datetime** |  | [optional] 
**created_at** | **datetime** |  | [optional] 
**updated_at** | **datetime** |  | [optional] 

## Example

```python
from imzala_client.models.contact_summary import ContactSummary

# TODO update the JSON string below
json = "{}"
# create an instance of ContactSummary from a JSON string
contact_summary_instance = ContactSummary.from_json(json)
# print the JSON string representation of the object
print(ContactSummary.to_json())

# convert the object into a dict
contact_summary_dict = contact_summary_instance.to_dict()
# create an instance of ContactSummary from a dict
contact_summary_from_dict = ContactSummary.from_dict(contact_summary_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


