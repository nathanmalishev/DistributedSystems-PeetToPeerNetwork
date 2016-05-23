package activitystreamer.messages;

public class GetKeyFailed extends JsonMessage{
	
	private String info;
	
	public GetKeyFailed(String info){
		
		this.command = "GET_KEY_FAILED";
		this.info = info;
	}
	
	public String getInfo(){ return info;}
}
