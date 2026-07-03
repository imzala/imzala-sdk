## @imzala/server-sdk-node@1.7.0

This generator creates TypeScript/JavaScript client that utilizes [axios](https://github.com/axios/axios). The generated Node module can be used in the following environments:

Environment
* Node.js
* Webpack
* Browserify

Language level
* ES5 - you must have a Promises/A+ library installed
* ES6

Module system
* CommonJS
* ES6 module system

It can be used in both TypeScript and JavaScript. In TypeScript, the definition will be automatically resolved via `package.json`. ([Reference](https://www.typescriptlang.org/docs/handbook/declaration-files/consumption.html))

### Building

To build and compile the typescript sources to javascript use:
```
npm install
npm run build
```

### Publishing

First build the package then run `npm publish`

### Consuming

navigate to the folder of your consuming project and run one of the following commands.

_published:_

```
npm install @imzala/server-sdk-node@1.7.0 --save
```

_unPublished (not recommended):_

```
npm install PATH_TO_GENERATED_PACKAGE --save
```

### Documentation for API Endpoints

All URIs are relative to *https://api-prd.imzala.org*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AccountApi* | [**apiV1MeGet**](docs/AccountApi.md#apiv1meget) | **GET** /api/v1/me | API key sahibi bilgisi
*DemandsApi* | [**apiV1DemandsGet**](docs/DemandsApi.md#apiv1demandsget) | **GET** /api/v1/demands | Sözleşme listesi (counts-only, PII\&#39;siz)
*DemandsApi* | [**apiV1DemandsIdCancelPost**](docs/DemandsApi.md#apiv1demandsidcancelpost) | **POST** /api/v1/demands/{id}/cancel | Sözleşme iptal (void)
*DemandsApi* | [**apiV1DemandsIdCertificateGet**](docs/DemandsApi.md#apiv1demandsidcertificateget) | **GET** /api/v1/demands/{id}/certificate | Tamamlanma sertifikası (PAdES B-T)
*DemandsApi* | [**apiV1DemandsIdDelete**](docs/DemandsApi.md#apiv1demandsiddelete) | **DELETE** /api/v1/demands/{id} | Sözleşme sil (yalnızca tamamlanmamış)
*DemandsApi* | [**apiV1DemandsIdEmbedSessionPost**](docs/DemandsApi.md#apiv1demandsidembedsessionpost) | **POST** /api/v1/demands/{id}/embed-session | Gömülü imza oturumu başlat (embed token mint)
*DemandsApi* | [**apiV1DemandsIdGet**](docs/DemandsApi.md#apiv1demandsidget) | **GET** /api/v1/demands/{id} | Sözleşme durumu + imza ilerlemesi
*DemandsApi* | [**apiV1DemandsIdItemsPost**](docs/DemandsApi.md#apiv1demandsiditemspost) | **POST** /api/v1/demands/{id}/items | Sözleşmeye alan yerleştir (replace)
*DemandsApi* | [**apiV1DemandsIdPartiesPartyIdResendPost**](docs/DemandsApi.md#apiv1demandsidpartiespartyidresendpost) | **POST** /api/v1/demands/{id}/parties/{partyId}/resend | Tekil tarafa imza davetini tekrar gönder
*DemandsApi* | [**apiV1DemandsIdPdfGet**](docs/DemandsApi.md#apiv1demandsidpdfget) | **GET** /api/v1/demands/{id}/pdf | İmzalı sözleşme PDF\&#39;i (auth\&#39;lu indirme)
*DemandsApi* | [**apiV1DemandsIdTimelineGet**](docs/DemandsApi.md#apiv1demandsidtimelineget) | **GET** /api/v1/demands/{id}/timeline | İmza denetim izi (maskeli)
*DemandsApi* | [**apiV1DemandsPost**](docs/DemandsApi.md#apiv1demandspost) | **POST** /api/v1/demands | Sözleşme oluştur (şablondan)
*DemandsApi* | [**apiV1DemandsUploadPost**](docs/DemandsApi.md#apiv1demandsuploadpost) | **POST** /api/v1/demands/upload | Dosya upload ile sözleşme oluştur (şablonsuz)
*RemindersApi* | [**apiV1DemandsIdRemindersPost**](docs/RemindersApi.md#apiv1demandsidreminderspost) | **POST** /api/v1/demands/{id}/reminders | Anlık hatırlatma tetikle (imzalanmamış taraflara)
*TemplatesApi* | [**apiV1TemplatesGet**](docs/TemplatesApi.md#apiv1templatesget) | **GET** /api/v1/templates | Şablon listesi
*TemplatesApi* | [**apiV1TemplatesIdDelete**](docs/TemplatesApi.md#apiv1templatesiddelete) | **DELETE** /api/v1/templates/{id} | Şablon sil
*TemplatesApi* | [**apiV1TemplatesIdGet**](docs/TemplatesApi.md#apiv1templatesidget) | **GET** /api/v1/templates/{id} | Şablon detay
*TemplatesApi* | [**apiV1TemplatesIdPatch**](docs/TemplatesApi.md#apiv1templatesidpatch) | **PATCH** /api/v1/templates/{id} | Şablon metadata güncelle
*TemplatesApi* | [**apiV1TemplatesIdUsageGet**](docs/TemplatesApi.md#apiv1templatesidusageget) | **GET** /api/v1/templates/{id}/usage | Şablon kullanım kılavuzu (curl + JSON örnek)
*TimestampsApi* | [**apiV1TimestampsPost**](docs/TimestampsApi.md#apiv1timestampspost) | **POST** /api/v1/timestamps | Zaman damgası oluştur (eser tescil)


### Documentation For Models

 - [ApiError](docs/ApiError.md)
 - [ApiV1DemandsGet200Response](docs/ApiV1DemandsGet200Response.md)
 - [ApiV1DemandsGet200ResponseData](docs/ApiV1DemandsGet200ResponseData.md)
 - [ApiV1DemandsGet200ResponseDataDemandsInner](docs/ApiV1DemandsGet200ResponseDataDemandsInner.md)
 - [ApiV1DemandsIdCancelPost200Response](docs/ApiV1DemandsIdCancelPost200Response.md)
 - [ApiV1DemandsIdCancelPost200ResponseData](docs/ApiV1DemandsIdCancelPost200ResponseData.md)
 - [ApiV1DemandsIdCancelPostRequest](docs/ApiV1DemandsIdCancelPostRequest.md)
 - [ApiV1DemandsIdDelete409Response](docs/ApiV1DemandsIdDelete409Response.md)
 - [ApiV1DemandsIdEmbedSessionPost200Response](docs/ApiV1DemandsIdEmbedSessionPost200Response.md)
 - [ApiV1DemandsIdEmbedSessionPost200ResponseData](docs/ApiV1DemandsIdEmbedSessionPost200ResponseData.md)
 - [ApiV1DemandsIdEmbedSessionPostRequest](docs/ApiV1DemandsIdEmbedSessionPostRequest.md)
 - [ApiV1DemandsIdGet200Response](docs/ApiV1DemandsIdGet200Response.md)
 - [ApiV1DemandsIdPartiesPartyIdResendPost200Response](docs/ApiV1DemandsIdPartiesPartyIdResendPost200Response.md)
 - [ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData](docs/ApiV1DemandsIdPartiesPartyIdResendPost200ResponseData.md)
 - [ApiV1DemandsIdRemindersPost200Response](docs/ApiV1DemandsIdRemindersPost200Response.md)
 - [ApiV1DemandsIdRemindersPost200ResponseData](docs/ApiV1DemandsIdRemindersPost200ResponseData.md)
 - [ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner](docs/ApiV1DemandsIdRemindersPost200ResponseDataDispatchedInner.md)
 - [ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner](docs/ApiV1DemandsIdRemindersPost200ResponseDataSkippedInner.md)
 - [ApiV1DemandsIdRemindersPost429Response](docs/ApiV1DemandsIdRemindersPost429Response.md)
 - [ApiV1DemandsIdRemindersPost429ResponseError](docs/ApiV1DemandsIdRemindersPost429ResponseError.md)
 - [ApiV1DemandsIdTimelineGet200Response](docs/ApiV1DemandsIdTimelineGet200Response.md)
 - [ApiV1DemandsIdTimelineGet200ResponseData](docs/ApiV1DemandsIdTimelineGet200ResponseData.md)
 - [ApiV1DemandsIdTimelineGet200ResponseDataEventsInner](docs/ApiV1DemandsIdTimelineGet200ResponseDataEventsInner.md)
 - [ApiV1DemandsPost201Response](docs/ApiV1DemandsPost201Response.md)
 - [ApiV1DemandsUploadPost201Response](docs/ApiV1DemandsUploadPost201Response.md)
 - [ApiV1MeGet200Response](docs/ApiV1MeGet200Response.md)
 - [ApiV1MeGet200ResponseData](docs/ApiV1MeGet200ResponseData.md)
 - [ApiV1MeGet200ResponseDataCredits](docs/ApiV1MeGet200ResponseDataCredits.md)
 - [ApiV1MeGet200ResponseDataWorkspace](docs/ApiV1MeGet200ResponseDataWorkspace.md)
 - [ApiV1TemplatesGet200Response](docs/ApiV1TemplatesGet200Response.md)
 - [ApiV1TemplatesGet200ResponseData](docs/ApiV1TemplatesGet200ResponseData.md)
 - [ApiV1TemplatesGet401Response](docs/ApiV1TemplatesGet401Response.md)
 - [ApiV1TemplatesIdDelete200Response](docs/ApiV1TemplatesIdDelete200Response.md)
 - [ApiV1TemplatesIdDelete200ResponseData](docs/ApiV1TemplatesIdDelete200ResponseData.md)
 - [ApiV1TemplatesIdGet200Response](docs/ApiV1TemplatesIdGet200Response.md)
 - [ApiV1TemplatesIdGet404Response](docs/ApiV1TemplatesIdGet404Response.md)
 - [ApiV1TemplatesIdPatch200Response](docs/ApiV1TemplatesIdPatch200Response.md)
 - [ApiV1TemplatesIdPatch200ResponseData](docs/ApiV1TemplatesIdPatch200ResponseData.md)
 - [ApiV1TemplatesIdPatchRequest](docs/ApiV1TemplatesIdPatchRequest.md)
 - [ApiV1TemplatesIdUsageGet200Response](docs/ApiV1TemplatesIdUsageGet200Response.md)
 - [ApiV1TimestampsPost201Response](docs/ApiV1TimestampsPost201Response.md)
 - [ApiV1TimestampsPostRequest1](docs/ApiV1TimestampsPostRequest1.md)
 - [CreateDemandRequest](docs/CreateDemandRequest.md)
 - [CreatedDemand](docs/CreatedDemand.md)
 - [CreatedDemandSigningUrlsInner](docs/CreatedDemandSigningUrlsInner.md)
 - [CreatedDemandUpload](docs/CreatedDemandUpload.md)
 - [DemandPage](docs/DemandPage.md)
 - [DemandStatus](docs/DemandStatus.md)
 - [DemandStatusPartiesInner](docs/DemandStatusPartiesInner.md)
 - [PageItem](docs/PageItem.md)
 - [PartyMappingInput](docs/PartyMappingInput.md)
 - [PartyMappingInputVariablesValue](docs/PartyMappingInputVariablesValue.md)
 - [ReminderSettings](docs/ReminderSettings.md)
 - [StandardError](docs/StandardError.md)
 - [StandardErrorError](docs/StandardErrorError.md)
 - [TemplateDetail](docs/TemplateDetail.md)
 - [TemplatePartySummary](docs/TemplatePartySummary.md)
 - [TemplateSummary](docs/TemplateSummary.md)
 - [TemplateSummaryPartiesInner](docs/TemplateSummaryPartiesInner.md)
 - [TemplateUsage](docs/TemplateUsage.md)
 - [TemplateUsageEndpoint](docs/TemplateUsageEndpoint.md)
 - [TemplateUsageExampleRequest](docs/TemplateUsageExampleRequest.md)
 - [TemplateUsagePartiesInner](docs/TemplateUsagePartiesInner.md)
 - [TemplateUsagePartiesInnerSupportedFieldsInner](docs/TemplateUsagePartiesInnerSupportedFieldsInner.md)
 - [TemplateUsageVariablesInner](docs/TemplateUsageVariablesInner.md)
 - [TemplateVariable](docs/TemplateVariable.md)
 - [TimestampRecord](docs/TimestampRecord.md)
 - [TriggerReminderRequest](docs/TriggerReminderRequest.md)
 - [UpsertItemsRequest](docs/UpsertItemsRequest.md)
 - [UpsertItemsResponse](docs/UpsertItemsResponse.md)
 - [UpsertItemsResponseData](docs/UpsertItemsResponseData.md)
 - [UpsertItemsResponseDataItemsInner](docs/UpsertItemsResponseDataItemsInner.md)
 - [WebhookDataDemandCompleted](docs/WebhookDataDemandCompleted.md)
 - [WebhookDataDemandCompletedPartiesInner](docs/WebhookDataDemandCompletedPartiesInner.md)
 - [WebhookDataDemandCreated](docs/WebhookDataDemandCreated.md)
 - [WebhookDataDemandExpired](docs/WebhookDataDemandExpired.md)
 - [WebhookDataDemandExpiredPartiesInner](docs/WebhookDataDemandExpiredPartiesInner.md)
 - [WebhookDataPartyRejected](docs/WebhookDataPartyRejected.md)
 - [WebhookDataPartyRejectedParty](docs/WebhookDataPartyRejectedParty.md)
 - [WebhookDataPartySigned](docs/WebhookDataPartySigned.md)
 - [WebhookDataPartyViewed](docs/WebhookDataPartyViewed.md)
 - [WebhookEnvelope](docs/WebhookEnvelope.md)


<a id="documentation-for-authorization"></a>
## Documentation For Authorization


Authentication schemes defined for the API:
<a id="ApiKeyAuth"></a>
### ApiKeyAuth

- **Type**: API key
- **API key parameter name**: X-API-Key
- **Location**: HTTP header

