package activitystreamer.messages;

public class AuthenticationFail extends JsonMessage {

	private String info;
	private String messageType = "AUTHENTICATION_FAIL";
	public String invalidSecretError = "the supplied secret is incorrect: ";

	public AuthenticationFail(String info) {
		this.info = info;
	}

	public String getInfo() {
		return info;
	}

	public boolean respond() {

		return RulesEngine.triggerAuthenticationFailRead(this);
	}
	
}
