package activitystreamer.messages;

public class RegisterFailed extends JsonMessage {

    private String info;

    public String getInfo() { return info; }

    public RegisterFailed(String username) {
        this.command = "REGISTER_FAILED";
        this.info = username + " is already registered with the system";
    }
}
