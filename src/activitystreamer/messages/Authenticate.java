package activitystreamer.messages;

public class Authenticate extends JsonMessage {
	
	private String secret;
	private String command = "AUTHENTICATE";


	public Authenticate(String secret) {
		this.secret = secret;
	}

	public String getSecret() {
		return secret;
	}


}
