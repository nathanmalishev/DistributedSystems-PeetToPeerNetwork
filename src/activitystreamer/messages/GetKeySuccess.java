package activitystreamer.messages;

public class GetKeySuccess extends JsonMessage{
	
	private String serverKey;
	
	public GetKeySuccess(String serverKey){
		
		this.command = "GET_KEY_SUCCESS";
		this.serverKey = serverKey;
	}
	
	public String getServerKey(){ return serverKey;}
}
