package activitystreamer.messages;


public class InvalidMessage extends JsonMessage {

    public static final String invalidMessageTypeError = "the message type was not recognised";

    private String info;

    public InvalidMessage(String info) {

        this.info = info;
        this.command = "INVALID_MESSAGE";
    }

    public String getInfo() { return info; }

}
