package activitystreamer.messages;

public class LockAllowed extends JsonMessage {
	
    private String username;
    private String secret;

    public String getUsername() { return username; }
    public String getSecret() { return secret; }

    public LockAllowed(String username, String secret) {
        super();
        this.command = "LOCK_ALLOWED";
        this.username = username;
        this.secret = secret;
    }
}
