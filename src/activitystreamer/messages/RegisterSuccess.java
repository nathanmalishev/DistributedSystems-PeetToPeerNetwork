package activitystreamer.messages;

public class RegisterSuccess extends JsonMessage {

    private String info;

    public String getInfo() { return info; }

    public RegisterSuccess(String username) {
        this.command = "REGISTER_SUCCESS";
        this.info = "register success for " + username;
    }
}
