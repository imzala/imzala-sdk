# CreatedDemandUpload


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **UUID** |  | [optional] 
**title** | **str** |  | [optional] 
**status** | **str** |  | [optional] 
**pages** | [**List[DemandPage]**](DemandPage.md) | Oluşturulan sözleşmedeki her sayfanın &#x60;id&#x60; ve &#x60;order&#x60; bilgisi. &#x60;POST /api/v1/demands/{id}/items&#x60; endpoint&#39;ine alan yerleştirmek için &#x60;page_id&#x60; parametresi olarak kullanın.  | [optional] 
**signing_urls** | [**List[CreatedDemandSigningUrlsInner]**](CreatedDemandSigningUrlsInner.md) |  | [optional] 
**result_url** | **str** |  | [optional] 

## Example

```python
from imzala_client.models.created_demand_upload import CreatedDemandUpload

# TODO update the JSON string below
json = "{}"
# create an instance of CreatedDemandUpload from a JSON string
created_demand_upload_instance = CreatedDemandUpload.from_json(json)
# print the JSON string representation of the object
print(CreatedDemandUpload.to_json())

# convert the object into a dict
created_demand_upload_dict = created_demand_upload_instance.to_dict()
# create an instance of CreatedDemandUpload from a dict
created_demand_upload_from_dict = CreatedDemandUpload.from_dict(created_demand_upload_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


