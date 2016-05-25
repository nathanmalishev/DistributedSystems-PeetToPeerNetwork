package activitystreamer.messages;


public class AuthenticationFail extends JsonMessage {

	private String info;

	public AuthenticationFail(String info) {
		super();
		this.info = info;
		this.command = "AUTHENTICATION_FAIL";
	}

	public String getInfo() {
		return info;
	}
	
}
