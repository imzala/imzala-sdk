

# ApiV1DemandsDemandIdDispatchPostRequestSendInvitations

Hangi kanaldan davet gideceğini **daraltır** (sözleşmenin kendi bildirim ayarlarını AÇAMAZ, yalnız kapatabilir). **Kapalı:** `false` (boolean), `\"false\"`, `\"0\"`, `\"off\"`, `\"no\"`, `\"hayir\"`, `\"hayır\"` → hiçbir davet gönderilmez, sözleşme yalnız yayına alınır. Tanınmayan değer **400 `INVALID_SEND_INVITATIONS`** döner (fail-closed — davet gitti sanıp gitmemesindense hata görmek yeğdir). 

## oneOf schemas
* [Boolean](Boolean.md)
* [String](String.md)

## Example
```java
// Import classes:
import org.imzala.client.generated.model.ApiV1DemandsDemandIdDispatchPostRequestSendInvitations;
import org.imzala.client.generated.model.Boolean;
import org.imzala.client.generated.model.String;

public class Example {
    public static void main(String[] args) {
        ApiV1DemandsDemandIdDispatchPostRequestSendInvitations exampleApiV1DemandsDemandIdDispatchPostRequestSendInvitations = new ApiV1DemandsDemandIdDispatchPostRequestSendInvitations();

        // create a new Boolean
        Boolean exampleBoolean = new Boolean();
        // set ApiV1DemandsDemandIdDispatchPostRequestSendInvitations to Boolean
        exampleApiV1DemandsDemandIdDispatchPostRequestSendInvitations.setActualInstance(exampleBoolean);
        // to get back the Boolean set earlier
        Boolean testBoolean = (Boolean) exampleApiV1DemandsDemandIdDispatchPostRequestSendInvitations.getActualInstance();

        // create a new String
        String exampleString = new String();
        // set ApiV1DemandsDemandIdDispatchPostRequestSendInvitations to String
        exampleApiV1DemandsDemandIdDispatchPostRequestSendInvitations.setActualInstance(exampleString);
        // to get back the String set earlier
        String testString = (String) exampleApiV1DemandsDemandIdDispatchPostRequestSendInvitations.getActualInstance();
    }
}
```


