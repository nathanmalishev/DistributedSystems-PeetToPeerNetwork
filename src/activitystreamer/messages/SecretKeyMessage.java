package activitystreamer.messages;


public class SecretKeyMessage extends JsonMessage{

	private String key;
	
	public SecretKeyMessage(String key){
		
		this.command = "SECRET_KEY_MESSAGE";
		this.key = key;
	}
	
	public String getKey() {return key;}
}

