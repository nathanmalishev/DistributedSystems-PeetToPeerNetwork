package activitystreamer.server;

import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.*;

// Need to change this so it is importing the one in our library
import com.google.gson.Gson;

import activitystreamer.messages.*;
import activitystreamer.util.Settings;
import java.util.HashMap;
import java.util.HashSet;


public class ControlSolution extends Control {
	
	private static final Logger log = LogManager.getLogger();

    private HashMap<Connection, HashSet<Connection>> lockRequests = new HashMap<>();

    public HashMap<Connection, HashSet<Connection>> getLockRequests() { return lockRequests; }

    public void addLockRequest(Connection con, HashSet<Connection> knownServers) {
        lockRequests.put(con, knownServers);
    }
	
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
		
		// Remove from list
		if(getAuthServers().contains(con)) getAuthServers().remove(con);
	}
	
	
	/*
	 * process incoming msg, from connection con
	 * return true if the connection should be closed, false otherwise
	 */
	@Override
	public synchronized boolean process(Connection con,String msg){

		MessageFactory msgFactory = new MessageFactory();
		RulesEngine rulesEngine = new RulesEngine(log);

		JsonMessage receivedMessage = msgFactory.buildMessage(msg, log);
		return rulesEngine.triggerResponse(receivedMessage, con);

	}


	/*
	 * Called once every few seconds
	 * Servers announce their current load to all other servers
	 * Return true if server should shut down, false otherwise
	 */
	@Override
	public boolean doActivity(){

		ServerAnnounce serverAnnounce = new ServerAnnounce(Settings.getId(), getAuthClients().size(), Settings.getLocalHostname(), String.valueOf(Settings.getLocalPort()));

		// Sends JSON Object to Authorized Servers only
		for(Connection c : getAuthServers()){

			if(c.writeMsg(serverAnnounce.toData())){
				log.info("Hostname: " + Settings.getLocalHostname() + " sending load");
			}
			else{
				log.info("Error sending load. Hostname: " + Settings.getLocalHostname());
			}
		}
		
		return false;
	}

    public void addUser(String username, String secret) {
        getClientDB().put(username, secret);
    }

}	
