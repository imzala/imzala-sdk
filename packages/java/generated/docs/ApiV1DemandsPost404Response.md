

# ApiV1DemandsPost404Response

## anyOf schemas
* [ApiError](ApiError.md)
* [CodedError](CodedError.md)

## Example
```java
// Import classes:
import org.imzala.client.generated.model.ApiV1DemandsPost404Response;
import org.imzala.client.generated.model.ApiError;
import org.imzala.client.generated.model.CodedError;

public class Example {
    public static void main(String[] args) {
        ApiV1DemandsPost404Response exampleApiV1DemandsPost404Response = new ApiV1DemandsPost404Response();

        // create a new ApiError
        ApiError exampleApiError = new ApiError();
        // set ApiV1DemandsPost404Response to ApiError
        exampleApiV1DemandsPost404Response.setActualInstance(exampleApiError);
        // to get back the ApiError set earlier
        ApiError testApiError = (ApiError) exampleApiV1DemandsPost404Response.getActualInstance();

        // create a new CodedError
        CodedError exampleCodedError = new CodedError();
        // set ApiV1DemandsPost404Response to CodedError
        exampleApiV1DemandsPost404Response.setActualInstance(exampleCodedError);
        // to get back the CodedError set earlier
        CodedError testCodedError = (CodedError) exampleApiV1DemandsPost404Response.getActualInstance();
    }
}
```


