package activitystreamer.messages;

public class Redirect extends JsonMessage{
	
	private String hostname;
	private int port;
	
	public Redirect(String hostname, String port){
		
		this.command = "REDIRECT";
		this.hostname = hostname;
		this.port = Integer.parseInt(port);
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}
	
}
