package activitystreamer.messages;

public class LoginSuccess extends JsonMessage{
	
	private String info;

    public LoginSuccess(String info) {
        super();
        this.info = info;
        this.command = "LOGIN_SUCCESS";
    }

    public String getInfo() { return info; }
}
