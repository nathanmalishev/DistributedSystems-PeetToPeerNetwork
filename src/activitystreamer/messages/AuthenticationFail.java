package activitystreamer.messages;

public class AuthenticationFail extends JsonMessage {

	private String info;
	public static final String invalidSecretTypeError = "the supplied secret is incorrect: ";

	public AuthenticationFail(String info) {

		this.info = info;
		this.command = "AUTHENTICATION_FAIL";
	}

	public String getInfo() {
		return info;
	}
	
}
