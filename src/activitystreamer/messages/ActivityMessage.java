package activitystreamer.messages;
import org.json.simple.JSONObject;


public class ActivityMessage extends JsonMessage {

    private String username;
    private String secret;
    private JSONObject activity;
    public String getUsername() { return username; }
    public JSONObject getActivity() { return activity; }
    public ActivityMessage(String username, String secret, JSONObject activity) {
        super();
        this.command = "ACTIVITY_MESSAGE";
        this.username = username;
        this.secret = secret;
        this.activity = activity;

        if (secret == null) this.secret = "-";
    }


}
