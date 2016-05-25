package activitystreamer.messages;

public class GetKeySuccess extends JsonMessage{
	
	private String serverKey;
	private String serverId;

	public GetKeySuccess(String serverKey, String serverId){
		
		this.command = "GET_KEY_SUCCESS";
		this.serverKey = serverKey;
		this.serverId = serverId;
	}
	
	public String getServerKey(){ return serverKey;}

	public String getServerId() {
		return serverId;
	}
}
