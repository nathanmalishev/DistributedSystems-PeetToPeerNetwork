package activitystreamer.messages;

public class LoginSuccess extends JsonMessage{
	
	private String info;
	public static String loginSuccess = "logged in as user ";

    public LoginSuccess(String info) {

        this.info = info;
        this.command = "LOGIN_SUCCESS";
    }

    public String getInfo() { return info; }
}
