

# PartyMappingInputVariablesValue

## oneOf schemas
* [BigDecimal](BigDecimal.md)
* [Boolean](Boolean.md)
* [String](String.md)

NOTE: this class is nullable.

## Example
```java
// Import classes:
import org.imzala.client.generated.model.PartyMappingInputVariablesValue;
import org.imzala.client.generated.model.BigDecimal;
import org.imzala.client.generated.model.Boolean;
import org.imzala.client.generated.model.String;

public class Example {
    public static void main(String[] args) {
        PartyMappingInputVariablesValue examplePartyMappingInputVariablesValue = new PartyMappingInputVariablesValue();

        // create a new BigDecimal
        BigDecimal exampleBigDecimal = new BigDecimal();
        // set PartyMappingInputVariablesValue to BigDecimal
        examplePartyMappingInputVariablesValue.setActualInstance(exampleBigDecimal);
        // to get back the BigDecimal set earlier
        BigDecimal testBigDecimal = (BigDecimal) examplePartyMappingInputVariablesValue.getActualInstance();

        // create a new Boolean
        Boolean exampleBoolean = new Boolean();
        // set PartyMappingInputVariablesValue to Boolean
        examplePartyMappingInputVariablesValue.setActualInstance(exampleBoolean);
        // to get back the Boolean set earlier
        Boolean testBoolean = (Boolean) examplePartyMappingInputVariablesValue.getActualInstance();

        // create a new String
        String exampleString = new String();
        // set PartyMappingInputVariablesValue to String
        examplePartyMappingInputVariablesValue.setActualInstance(exampleString);
        // to get back the String set earlier
        String testString = (String) examplePartyMappingInputVariablesValue.getActualInstance();
    }
}
```


