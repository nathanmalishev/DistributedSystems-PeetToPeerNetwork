package activitystreamer.messages;

/**
 * Created by Jeames on 27/04/2016.
 */
public class ActivityBroadcast extends JsonMessage {

    private String activity;
    public String getActivity() { return activity; }

    public ActivityBroadcast(String activity) {

        this.command = "ACTIVITY_BROADCAST";
        this.activity = activity;
    }
}
