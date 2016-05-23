package activitystreamer.messages;

/**
 * Created by Jeames on 17/05/2016.
 */
public class WriteReply extends JsonMessage {


    private String username;
    private String secret;
    private String result;
    private String info;

    public WriteReply(String username, String result, String info, String secret) {
        super();
        this.info = info;
        this.username = username;
        this.command = "WRITE_REPLY";
        this.result = result;
        this.secret = secret;
    }

    public String getInfo() { return info; }
    public String getUsername() { return username; }
    public String getSecret() { return secret; }

    public boolean passed() {
        if (result.equals("SUCCESS")) {
            return true;
        } else {
            return false;
        }
    }

}
