package activitystreamer.messages;
import org.json.simple.JSONObject;


public class ActivityBroadcast extends JsonMessage {

    private JSONObject activity;
    public JSONObject getActivity() { return activity; }

    public ActivityBroadcast(JSONObject activity) {

        this.command = "ACTIVITY_BROADCAST";
        this.activity = activity;
    }
}
