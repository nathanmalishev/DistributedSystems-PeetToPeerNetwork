package activitystreamer.messages;

public class RegisterKey extends JsonMessage {

	private String serverId;
	private String publicKey;
	
	public RegisterKey(String id, String pk){
		
		this.command = "REGISTER_KEY";
		this.serverId = id;
		this.publicKey = pk;
	}
	
	public String getServerId(){
		return serverId;
	}
	
	public String getPublicKey(){
		return publicKey;
	}
}
