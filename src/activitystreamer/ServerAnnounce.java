package activitystreamer;

public class ServerAnnounce extends JsonMessage{

	private String id;
	private int load;
	private String hostname;
	private String port;
	
	public String getId() {
		return id;
	}
	public int getLoad() {
		return load;
	}
	public String getHostname() {
		return hostname;
	}
	public String getPort() {
		return port;
	}
	
}
