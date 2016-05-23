package activitystreamer.messages;

public class LockRequest extends JsonMessage {
    private String username;
    private String secret;

    public String getUsername() { return username; }
    public String getSecret() { return secret; }

    public LockRequest(String username, String secret) {
        super();
        this.command = "LOCK_REQUEST";
        this.username = username;
        this.secret = secret;
    }
}
