package activitystreamer.messages;


public class InvalidMessage extends JsonMessage {

    private String messageType = "INVALID_MESSAGE";
    private String info;

    public InvalidMessage(String info) {
        this.info = info;
    }

    public String getInfo() { return info; }

    public boolean respond() {
        return RulesEngine.triggerInvalidMessageRead(this);
    }

}
