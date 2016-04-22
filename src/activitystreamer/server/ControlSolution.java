package activitystreamer.server;

import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.*;

// Need to change this so it is importing the one in our library
import com.google.gson.Gson;

import activitystreamer.Authenticate;
import activitystreamer.AuthenticationFail;
import activitystreamer.JsonMessage;
import activitystreamer.ServerAnnounce;
import activitystreamer.util.Settings;



public class ControlSolution extends Control {
	
	private static final Logger log = LogManager.getLogger();
	
	/*
	 * additional variables as needed
	 */
	
	// since control and its subclasses are singleton, we get the singleton this way
	public static ControlSolution getInstance() {
		if(control==null){
			control=new ControlSolution();
		} 
		return (ControlSolution) control;
	}
	
	public ControlSolution() {
		super();
		/*
		 * Do some further initialization here if necessary
		 */

		
		// check if we should initiate a connection and do so if necessary
		initiateConnection();
		
		// start the server's activity loop
		// it will call doActivity every few seconds
		start();
	}
	
	
	/*
	 * a new incoming connection
	 */
	@Override
	public Connection incomingConnection(Socket s) throws IOException{
		Connection con = super.incomingConnection(s);
		/*
		 * do additional things here
		 */
		
		return con;
	}
	
	/*
	 * a new outgoing connection
	 */
	@Override
	public Connection outgoingConnection(Socket s) throws IOException{
		Connection con = super.outgoingConnection(s);
		/*
		 * do additional things here
		 */
		
		
		return con;
	}
	
	
	/*
	 * the connection has been closed
	 */
	@Override
	public void connectionClosed(Connection con){
		super.connectionClosed(con);
		/*
		 * do additional things here
		 */
	}
	
	
	/*
	 * process incoming msg, from connection con
	 * return true if the connection should be closed, false otherwise
	 */
	@Override
	public synchronized boolean process(Connection con,String msg){
		
		/* GSON Parser transforms JSON objects into instance of a class */
		Gson parser = new Gson();
		
		/* Determine what kind of message we need to process */
		JsonMessage messageType = parser.fromJson(msg, JsonMessage.class);
		
		// Process accordingly
		switch(messageType.getCommand()){
			
			case "AUTHENTICATE" :
				
				Authenticate serverRequest = parser.fromJson(msg, Authenticate.class);
				return readAuthenticate(serverRequest, con);
				
			case "AUTHENTICATION_FAIL" :
				
				AuthenticationFail failReply = parser.fromJson(msg, AuthenticationFail.class);
				return readAuthenticationFail(failReply, con);
				
			case "SERVER_ANNOUNCE" :
				
				ServerAnnounce serverLoad = parser.fromJson(msg, ServerAnnounce.class);
				return readServerAnnounce(serverLoad, con);
			
			// --- Will be INVALID_MESSAGE ---
			default :
				break;
			
		}
		

		return false;
	}


	/*
	 * Called once every few seconds
	 * Servers announce their current load to all other servers
	 * Return true if server should shut down, false otherwise
	 */
	@Override
	public boolean doActivity(){
		
		// JSON Object contains info about each servers load
		JSONObject serverAnnounce = new JSONObject();
		serverAnnounce.put("command", "SERVER_ANNOUNCE");
		serverAnnounce.put("id", Settings.getId());
		serverAnnounce.put("load", new Integer(getConnections().size()));	
		serverAnnounce.put("hostname", Settings.getLocalHostname());
		serverAnnounce.put("port", Settings.getLocalPort());
		
		// Sends JSON Object to all its connections
		for(Connection c : getConnections()){
			
			// --- Need to adjust to only send to the servers ---
			if(c.writeMsg(serverAnnounce.toString())){
				log.info("Hostname: " + Settings.getLocalHostname() + " sending load");
			}
			else{
				log.info("Error sending load. Hostname: " + Settings.getLocalHostname());
			}
		}
		
		return false;
	}
	
	/*
	 * Other methods as needed
	 */
	
	/* Return True if the server is to be shut down */
	public boolean readServerAnnounce(ServerAnnounce msg, Connection con){
		
		// ---- DEBUG ----
		log.debug("Command: " + msg.getCommand());
		log.debug("ID: " + msg.getId());
		log.debug("Load: " + msg.getLoad());
		log.debug("Hostname: " + msg.getHostname());
		log.debug("Port: " + msg.getPort());
		
		// --- Need to actually store the loads of each server ---
		
		return false;
	}
	
	/* Return True if the server is to be shut down */
	public boolean readAuthenticate(Authenticate msg, Connection con){
		
		// Send AUTHENTICATION_FAIL
		if(!msg.getSecret().equals(Settings.getSecret())){

			JSONObject authenticationFail = new JSONObject();
			authenticationFail.put("command", "AUTHENTICATION_FAIL");
			authenticationFail.put("info", "the supplied secret is incorrect: "+msg.getSecret());
			
			con.writeMsg(authenticationFail.toString());
			return true;				// Close connection
		}
		
		return false;
	}
	
	/* Logs the information and returns true to indicate the connection will be closed */
	public boolean readAuthenticationFail(AuthenticationFail msg, Connection con){

		// Display JSON Message
		log.info("command : " + msg.getCommand());
		log.info("info : " + msg.getInfo());
		
		setTerm(true);		// Terminate the Control
		
		return true;
	}
}	
