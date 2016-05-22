package activitystreamer.messages;

public class KeyRegisterResponse extends JsonMessage{

	String info;
	String result;
	
	public KeyRegisterResponse(String info, String result){
		
		this.command = "KEY_REGISTER_RESPONSE";
		this.info = info;
		this.result = result;
	}
	
	public String getInfo() { return info;}
	public String getResult() {return result;}
}
