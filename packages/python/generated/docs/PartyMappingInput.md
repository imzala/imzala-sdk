# PartyMappingInput


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**template_party_id** | **UUID** | GET /api/v1/templates/{id} cevabındaki parties[].id | 
**first_name** | **str** |  | 
**last_name** | **str** |  | 
**email** | **str** | email VEYA phone&#39;dan en az biri zorunlu | [optional] 
**phone** | **str** | E.164 format (örn. \&quot;+905551234567\&quot;) | [optional] 
**government_id** | **str** | TC kimlik no (11 hane) | [optional] 
**birth_date** | **date** | ISO 8601 (örn. \&quot;1990-05-15\&quot;) | [optional] 
**send_sms** | **bool** |  | [optional] [default to True]
**send_email** | **bool** |  | [optional] [default to True]
**variables** | [**Dict[str, PartyMappingInputVariablesValue]**](PartyMappingInputVariablesValue.md) | Bu PARTİYE AİT dynamic field&#39;lara gönderilen değerler. Slug bazında eşleşir. Item&#39;ın template_party_id&#39;si bu partiyle aynı olmalı; değilse değişken atlanır ve variables_ignored&#39;a düşürülür.  | [optional] 

## Example

```python
from imzala_client.models.party_mapping_input import PartyMappingInput

# TODO update the JSON string below
json = "{}"
# create an instance of PartyMappingInput from a JSON string
party_mapping_input_instance = PartyMappingInput.from_json(json)
# print the JSON string representation of the object
print(PartyMappingInput.to_json())

# convert the object into a dict
party_mapping_input_dict = party_mapping_input_instance.to_dict()
# create an instance of PartyMappingInput from a dict
party_mapping_input_from_dict = PartyMappingInput.from_dict(party_mapping_input_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


