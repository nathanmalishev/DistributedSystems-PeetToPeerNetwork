package activitystreamer.messages;


/**
 * Created by Jeames on 22/04/2016.
 */
public class Login extends JsonMessage {

    private String username;
    private String secret;
    public Login(String username, String secret) {

        this.command = "LOGIN";
        this.username = username;
        // Going to use null values for when username is anonymous
        if (!username.equals("anonymous")) {
            this.secret = secret;
        } else {
            this.secret = null;
        }
    }

}

