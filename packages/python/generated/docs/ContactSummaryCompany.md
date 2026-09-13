# ContactSummaryCompany


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**name** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.contact_summary_company import ContactSummaryCompany

# TODO update the JSON string below
json = "{}"
# create an instance of ContactSummaryCompany from a JSON string
contact_summary_company_instance = ContactSummaryCompany.from_json(json)
# print the JSON string representation of the object
print(ContactSummaryCompany.to_json())

# convert the object into a dict
contact_summary_company_dict = contact_summary_company_instance.to_dict()
# create an instance of ContactSummaryCompany from a dict
contact_summary_company_from_dict = ContactSummaryCompany.from_dict(contact_summary_company_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


