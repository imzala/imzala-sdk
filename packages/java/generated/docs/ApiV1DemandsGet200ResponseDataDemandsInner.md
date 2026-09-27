

# ApiV1DemandsGet200ResponseDataDemandsInner


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **UUID** |  |  [optional] |
|**title** | **String** |  |  [optional] |
|**status** | **String** |  |  [optional] |
|**createdAt** | **OffsetDateTime** |  |  [optional] |
|**completedAt** | **OffsetDateTime** |  |  [optional] |
|**termEndDate** | **LocalDate** | Takip edilen bitiş tarihi; takip yoksa &#x60;null&#x60; (bkz. &#x60;GET /demands/{id}&#x60; → &#x60;term&#x60;). |  [optional] |
|**archivedAt** | **OffsetDateTime** |  |  [optional] |
|**partiesTotal** | **Integer** |  |  [optional] |
|**partiesSigned** | **Integer** |  |  [optional] |
|**pdfUrl** | **URI** | COMPLETED ise imzalı PDF public URL&#39;i |  [optional] |



