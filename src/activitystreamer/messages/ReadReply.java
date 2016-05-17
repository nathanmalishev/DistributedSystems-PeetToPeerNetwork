package activitystreamer.messages;

/**
 * Created by Jeames on 17/05/2016.
 */
public class ReadReply extends JsonMessage {

    private String username;
    private String result;
    private String info;

    public ReadReply(String username, String result, String info) {
        this.info = info;
        this.username = username;
        this.command = "READ_REPLY";
        this.result = result;
    }
    public String getInfo() { return info; }
    public String getUsername() { return username; }

    public boolean passed() {
        if (result.equals("SUCCESS")) {
            return true;
        } else {
            return false;
        }
    }

}
