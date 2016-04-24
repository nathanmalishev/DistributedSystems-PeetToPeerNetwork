package activitystreamer.messages;

/**
 * Created by Jeames on 24/04/2016.
 */
public class LoginFailed extends JsonMessage {

    public static String noMatchingUsernameError = "there was no client matching this username";
    public static String incorrectSecretError = "the secret was incorrect for this username";
    public static String genericLoginFailedError = "failed to login: incorrect details";

    private String info;

    public LoginFailed(String info) {

        this.info = info;
        this.command = "LOGIN_FAILED";
    }

    public String getInfo() { return info; }
}
