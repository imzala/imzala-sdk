# ApiV1DemandsDemandIdDocumentsOrderPutRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**document_ids** | **List[UUID]** | Zarftaki tüm belge id&#39;leri, istenen yeni sırada. | 

## Example

```python
from imzala_client.models.api_v1_demands_demand_id_documents_order_put_request import ApiV1DemandsDemandIdDocumentsOrderPutRequest

# TODO update the JSON string below
json = "{}"
# create an instance of ApiV1DemandsDemandIdDocumentsOrderPutRequest from a JSON string
api_v1_demands_demand_id_documents_order_put_request_instance = ApiV1DemandsDemandIdDocumentsOrderPutRequest.from_json(json)
# print the JSON string representation of the object
print(ApiV1DemandsDemandIdDocumentsOrderPutRequest.to_json())

# convert the object into a dict
api_v1_demands_demand_id_documents_order_put_request_dict = api_v1_demands_demand_id_documents_order_put_request_instance.to_dict()
# create an instance of ApiV1DemandsDemandIdDocumentsOrderPutRequest from a dict
api_v1_demands_demand_id_documents_order_put_request_from_dict = ApiV1DemandsDemandIdDocumentsOrderPutRequest.from_dict(api_v1_demands_demand_id_documents_order_put_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


