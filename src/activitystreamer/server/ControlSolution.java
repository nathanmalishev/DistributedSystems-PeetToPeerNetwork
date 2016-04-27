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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class ControlSolution extends Control {
	
	private static final Logger log = LogManager.getLogger();

    private ArrayList<Connection> unauthClients;
    private HashMap<String, HashSet<Connection>> lockRequests;
    private HashMap<String, Connection> lockConnections;
	private HashMap<Connection, String> loggedInUsernames;

	public HashMap<Connection, String> getLoggedInUsernames() { return loggedInUsernames; }

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
        lockRequests = new HashMap<>();
        lockConnections = new HashMap<>();
        unauthClients = new ArrayList<>();
		loggedInUsernames = new HashMap<Connection, String>();
		
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

    public boolean userKnownDifferentSecret(String username, String secret) {
        return getClientDB().containsKey(username) && !getClientDB().get(username).equals(secret);
    }

    public boolean hasUser(String username, String secret) {
        return getClientDB().get(username).equals(secret);
    }

    public void removeUser(String username) {
        getClientDB().remove(username);
    }

    public ArrayList<Connection> getUnauthClients() { return unauthClients; }

    public void addUnauthClient(Connection con) {
        getUnauthClients().add(con);
    }

    public void removeUnauthClient(Connection con) {
        getUnauthClients().remove(con);
    }

    public HashSet<Connection> getLockRequest(String combo) { return lockRequests.get(combo); }
    public void addLockRequest(String combo, HashSet<Connection> set) { lockRequests.put(combo, set); }

    public void addConnectionForLock(String combo, Connection con) { lockConnections.put(combo, con); }
    public Connection getConnectionForLock(String combo) { return lockConnections.get(combo); }
    public boolean containsConnectionForLock(String combo) { return lockConnections.containsKey(combo); }

    public void removeLockRequestsAndConnection(String combo) {
        if (lockRequests.containsKey(combo))
            lockRequests.remove(combo);
        if (lockConnections.containsKey(combo))
            lockConnections.remove(combo);
    }

}	
