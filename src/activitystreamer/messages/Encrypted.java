package activitystreamer.messages;

public class Encrypted extends JsonMessage{
	
	private byte[] content;
	
	public Encrypted(byte[] content){
		
		this.command = "ENCRYPTED";
		this.content = content;
	}
	
	public byte[] getContent(){ return content;}
}
