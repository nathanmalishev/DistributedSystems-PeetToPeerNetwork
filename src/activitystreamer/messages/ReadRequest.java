package activitystreamer.messages;

/**
 * Created by Jeames on 17/05/2016.
 */
public class ReadRequest extends JsonMessage {

    private String username;
    private String secret;

    public ReadRequest(String username, String secret) {
        super();
        this.username = username;
        this.secret = secret;
        this.command = "READ_REQUEST";
    }

    public String getUsername() { return username; }
    public String getSecret() { return secret; }

}
