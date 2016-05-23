package activitystreamer.messages;


public class InvalidMessage extends JsonMessage {

    private String info;

    public InvalidMessage(String info) {
        super();
        this.info = info;
        this.command = "INVALID_MESSAGE";
    }

    public String getInfo() { return info; }

}
