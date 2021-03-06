package activitystreamer.messages;

public class RegisterKey extends JsonMessage {

	private String publicKeyStr;
	private String serverId;

	public RegisterKey(String publicKeyString, String uniqueIdentifier) {
		this.command = "REGISTER_KEY";
		this.publicKeyStr = publicKeyString;
		this.serverId = uniqueIdentifier;
	}

	public String getPublicKeyStr() {
		return publicKeyStr;
	}

	public String getServerId() {
		return serverId;
	}

}
