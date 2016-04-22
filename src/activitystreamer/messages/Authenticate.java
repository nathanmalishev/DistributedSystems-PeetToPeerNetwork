package activitystreamer.messages;

public class Authenticate extends JsonMessage {
	
	private String secret;
	private String messageType = "AUTHENTICATE";


	public Authenticate(String secret) {
		this.secret = secret;
	}

	public String getSecret() {
		return secret;
	}

	public boolean respond() {
		return RulesEngine.triggerAuthenticateAttempt(this);
	}
	
}
