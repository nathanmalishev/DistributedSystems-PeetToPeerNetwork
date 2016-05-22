package activitystreamer.messages;

public class Redirect extends JsonMessage{
	
	private String hostname;
	private int port;
	
	public Redirect(String hostname, int port){
		
		this.command = "REDIRECT";
		this.hostname = hostname;
		this.port = port;
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}
	
}
