package activitystreamer.messages;

public class SecretKeyFailed extends JsonMessage{
	
	public SecretKeyFailed(){
		
		this.command = "SECRET_KEY_FAILED";
	}
}
