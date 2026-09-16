

# ApiV1DemandsPost400Response

## anyOf schemas
* [CodedError](CodedError.md)
* [DocumentSelectionError](DocumentSelectionError.md)

## Example
```java
// Import classes:
import org.imzala.client.generated.model.ApiV1DemandsPost400Response;
import org.imzala.client.generated.model.CodedError;
import org.imzala.client.generated.model.DocumentSelectionError;

public class Example {
    public static void main(String[] args) {
        ApiV1DemandsPost400Response exampleApiV1DemandsPost400Response = new ApiV1DemandsPost400Response();

        // create a new CodedError
        CodedError exampleCodedError = new CodedError();
        // set ApiV1DemandsPost400Response to CodedError
        exampleApiV1DemandsPost400Response.setActualInstance(exampleCodedError);
        // to get back the CodedError set earlier
        CodedError testCodedError = (CodedError) exampleApiV1DemandsPost400Response.getActualInstance();

        // create a new DocumentSelectionError
        DocumentSelectionError exampleDocumentSelectionError = new DocumentSelectionError();
        // set ApiV1DemandsPost400Response to DocumentSelectionError
        exampleApiV1DemandsPost400Response.setActualInstance(exampleDocumentSelectionError);
        // to get back the DocumentSelectionError set earlier
        DocumentSelectionError testDocumentSelectionError = (DocumentSelectionError) exampleApiV1DemandsPost400Response.getActualInstance();
    }
}
```


