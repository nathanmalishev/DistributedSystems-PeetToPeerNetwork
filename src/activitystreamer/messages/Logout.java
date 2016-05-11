package activitystreamer.messages;

/**
 * Created by nathan on 26/04/2016.
 */
public class Logout extends JsonMessage{

    public Logout(String info) {
        this.command = "LOGOUT";
    }
}