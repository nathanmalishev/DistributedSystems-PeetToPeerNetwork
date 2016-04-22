package activitystreamer.messages;


public class InvalidMessage extends JsonMessage {

    private String command = "INVALID_MESSAGE";
    private String info;

    public InvalidMessage(String info) {
        this.info = info;
    }

    public String getInfo() { return info; }

}
