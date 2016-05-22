package activitystreamer.messages;

public class RegisterKey extends JsonMessage {

	private String serverId;	
	
	public RegisterKey(String id){
		
		this.command = "REGISTER_KEY";
		this.serverId = id;
	}
}
