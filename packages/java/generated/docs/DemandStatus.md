

# DemandStatus


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **UUID** |  |  [optional] |
|**title** | **String** |  |  [optional] |
|**status** | [**StatusEnum**](#StatusEnum) |  |  [optional] |
|**createdAt** | **OffsetDateTime** |  |  [optional] |
|**completedAt** | **OffsetDateTime** |  |  [optional] |
|**parties** | [**List&lt;DemandStatusPartiesInner&gt;**](DemandStatusPartiesInner.md) |  |  [optional] |
|**resultUrl** | **URI** |  |  [optional] |
|**pdfUrl** | **URI** | Sadece status&#x3D;COMPLETED iken dolu |  [optional] |



## Enum: StatusEnum

| Name | Value |
|---- | -----|
| DRAFT | &quot;DRAFT&quot; |
| PENDING | &quot;PENDING&quot; |
| COMPLETED | &quot;COMPLETED&quot; |
| EXPIRED | &quot;EXPIRED&quot; |
| CANCELLED | &quot;CANCELLED&quot; |



