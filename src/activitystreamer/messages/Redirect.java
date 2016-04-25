package activitystreamer.messages;

public class Redirect extends JsonMessage{
	
	private String hostname;
	private String port;
	
	public Redirect(String hostname, String port){
		
		this.command = "REDIRECT";
		this.hostname = hostname;
		this.port = port;
	}

	public String getHostname() {
		return hostname;
	}

	public String getPort() {
		return port;
	}
	
}
