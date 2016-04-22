package activitystreamer.messages;

public class Authenticate extends JsonMessage {
	
	private String secret;

	public Authenticate(String secret) {

		this.secret = secret;
		this.command = "AUTHENTICATE";
	}

	public String getSecret() {
		return secret;
	}


}
