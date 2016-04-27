package activitystreamer.messages;
import org.json.simple.JSONObject;

/**
 * Created by Jeames on 27/04/2016.
 */
public class ActivityBroadcast extends JsonMessage {

    private JSONObject activity;
    public JSONObject getActivity() { return activity; }

    public ActivityBroadcast(JSONObject activity) {

        this.command = "ACTIVITY_BROADCAST";
        this.activity = activity;
    }
}
