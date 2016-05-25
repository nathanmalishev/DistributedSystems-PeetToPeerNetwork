package activitystreamer.messages;


public class SecretKeyMessage extends JsonMessage{

	private byte[] key;
	
	public SecretKeyMessage(byte[] key){
		
		this.command = "SECRET_KEY_MESSAGE";
		this.key = key;
	}
	
	public byte[] getKey() {return key;}
}

