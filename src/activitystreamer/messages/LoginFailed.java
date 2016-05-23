package activitystreamer.messages;

/**
 * Created by Jeames on 24/04/2016.
 */
public class LoginFailed extends JsonMessage {

    private String info;

    public LoginFailed(String info) {
        super();
        this.info = info;
        this.command = "LOGIN_FAILED";
    }

    public String getInfo() { return info; }
}
