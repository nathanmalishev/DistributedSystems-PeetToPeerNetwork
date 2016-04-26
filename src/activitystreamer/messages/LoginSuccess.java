package activitystreamer.messages;

public class LoginSuccess extends JsonMessage {

    private String info;

    public String getInfo() { return info; }

    public LoginSuccess(String username) {
        this.command = "LOGIN_SUCCESS";
        this.info = "logged in as user " + username;
    }
}
