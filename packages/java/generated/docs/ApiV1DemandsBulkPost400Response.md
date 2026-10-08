

# ApiV1DemandsBulkPost400Response

## anyOf schemas
* [ApiError](ApiError.md)
* [CodedError](CodedError.md)
* [DocumentCollectionPolicyError](DocumentCollectionPolicyError.md)

## Example
```java
// Import classes:
import org.imzala.client.generated.model.ApiV1DemandsBulkPost400Response;
import org.imzala.client.generated.model.ApiError;
import org.imzala.client.generated.model.CodedError;
import org.imzala.client.generated.model.DocumentCollectionPolicyError;

public class Example {
    public static void main(String[] args) {
        ApiV1DemandsBulkPost400Response exampleApiV1DemandsBulkPost400Response = new ApiV1DemandsBulkPost400Response();

        // create a new ApiError
        ApiError exampleApiError = new ApiError();
        // set ApiV1DemandsBulkPost400Response to ApiError
        exampleApiV1DemandsBulkPost400Response.setActualInstance(exampleApiError);
        // to get back the ApiError set earlier
        ApiError testApiError = (ApiError) exampleApiV1DemandsBulkPost400Response.getActualInstance();

        // create a new CodedError
        CodedError exampleCodedError = new CodedError();
        // set ApiV1DemandsBulkPost400Response to CodedError
        exampleApiV1DemandsBulkPost400Response.setActualInstance(exampleCodedError);
        // to get back the CodedError set earlier
        CodedError testCodedError = (CodedError) exampleApiV1DemandsBulkPost400Response.getActualInstance();

        // create a new DocumentCollectionPolicyError
        DocumentCollectionPolicyError exampleDocumentCollectionPolicyError = new DocumentCollectionPolicyError();
        // set ApiV1DemandsBulkPost400Response to DocumentCollectionPolicyError
        exampleApiV1DemandsBulkPost400Response.setActualInstance(exampleDocumentCollectionPolicyError);
        // to get back the DocumentCollectionPolicyError set earlier
        DocumentCollectionPolicyError testDocumentCollectionPolicyError = (DocumentCollectionPolicyError) exampleApiV1DemandsBulkPost400Response.getActualInstance();
    }
}
```


