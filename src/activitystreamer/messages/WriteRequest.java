package activitystreamer.messages;


/**
 * Created by Jeames on 17/05/2016.
 */
public class WriteRequest extends JsonMessage {

    private String username;
    private String secret;

    public WriteRequest(String username, String secret) {

        this.username = username;
        this.secret = secret;
        this.command = "WRITE_REQUEST";
    }

    public String getUsername() { return username; }
    public String getSecret() { return secret; }

}
