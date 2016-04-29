package activitystreamer.messages;


public class Authenticate extends JsonMessage {
	
	private String secret;

	public Authenticate(String secret) {
		this.command = "AUTHENTICATE";
		this.secret = secret;
	}

	public String getSecret() {
		return secret;
	}


}
