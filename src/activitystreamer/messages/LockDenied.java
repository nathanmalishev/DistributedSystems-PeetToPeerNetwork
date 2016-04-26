package activitystreamer.messages;

public class LockDenied extends JsonMessage {
    private String username;
    private String secret;

    public String getUsername() { return username; }
    public String getSecret() { return secret; }

    public LockDenied(String username, String secret) {
        this.command = "LOCK_DENIED";
        this.username = username;
        this.secret = secret;
    }
}
