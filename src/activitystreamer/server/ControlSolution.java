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

/** 
 * Class handles the Server Functionality. */
public class ControlSolution extends Control {
	
	private static final Logger log = LogManager.getLogger();

    private ArrayList<Connection> unauthClients;				// Unauthorized Connections
    private HashMap<String, HashSet<Connection>> lockRequests;	// Outstanding Lock Requests
    private HashMap<String, Connection> lockConnections;		
	private HashMap<Connection, String> loggedInUsernames;		// Current active users
	private HashMap<String, Connection> registerWaiting;
	private HashMap<String, Connection> loginWaiting;

	public HashMap<String, Connection> getRegisterWaiting() { return registerWaiting; }
	public HashMap<Connection, String> getLoggedInUsernames() { return loggedInUsernames; }
	public HashMap<String, Connection> getLoginWaiting() { return loginWaiting; }
	private static final char[] startBoundaries = {'a', 'h', 'n', 'u'};
	private static final char[] endBoundaries = {'g', 'm', 't', 'z'};

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
		loggedInUsernames = new HashMap<>();
		registerWaiting = new HashMap<>();
		loginWaiting = new HashMap<>();
		// check if we should initiate a connection and do so if necessary
		initiateConnection();
		
		// start the server's activity loop
		// it will call doActivity every few seconds
		start();
	}

	/**
	 * Called when starting a server and connects to the provided remote host
	 * if it is supplied.
	 * To successfully connect the secret must match that of the given remote host.
	 */
	public void initiateConnection(){
		// make a connection to another server if remote hostname is supplied
		if(Settings.getRemoteHostname()!=null){

			try {
				// Establish a connection
				Connection c = outgoingConnection(new Socket(Settings.getRemoteHostname(),Settings.getRemotePort()));

				// Send JSON Authenticate message
				Authenticate authenticateMsg = new Authenticate(Settings.getSecret());
				log.info("Sending Authentication Request to: " + Settings.getRemoteHostname() + ", with Secret: " + authenticateMsg.getSecret());
				c.writeMsg(authenticateMsg.toData());

				// Will need to then receive a message with db info

				// Add to authorized connections
				getAuthServers().add(c);

				// Remove from unauthorized connections
				getUnauthConnections().remove(c);

			} catch (IOException e) {
				log.error("failed to make connection to "+Settings.getRemoteHostname()+":"+Settings.getRemotePort()+" :"+e);
				System.exit(-1);
			}
		}
		/* DB needs to be setup (either take arguments or generate yourself) */
		initialiseDBConnections();


	}

	public void initialiseDBConnections() {

		dbLookup = new HashMap();
		// Either way, initialise connections with them
		try {
			Connection a = outgoingConnection(new Socket(Settings.getShardAHostname(), Settings.getShardAPort()));
			Connection b = outgoingConnection(new Socket(Settings.getShardBHostname(), Settings.getShardBPort()));
			Connection c = outgoingConnection(new Socket(Settings.getShardCHostname(), Settings.getShardCPort()));
			Connection d = outgoingConnection(new Socket(Settings.getShardDHostname(), Settings.getShardDPort()));
			dbLookup.put(0, a);
			dbLookup.put(1, b);
			dbLookup.put(2, c);
			dbLookup.put(3, d);
		} catch (IOException e) {
			log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
			System.exit(-1);
		}
	}


	/* Takes a username and returns the connection to the correct db shard */
	public Connection map(String username) {

		char[] usernameArray = username.toCharArray();

		char firstLetter = usernameArray[0];
		int dbNum = -1;
		for (int i = 0; i < 4; i ++) {
			if (firstLetter >= startBoundaries[i] && firstLetter <= endBoundaries[i]) {
				dbNum = i;
				break;
			}
		}
		if (dbNum!= -1) {
			return dbLookup.get(dbNum);
		}

		return null;
	}


	/**
	 * Process a new incoming connection
	 */
	@Override
	public Connection incomingConnection(Socket s) throws IOException{
		
		Connection con = super.incomingConnection(s);
		
		return con;
	}
	
	/**
	 * Process a new outgoing connection
	 */
	@Override
	public Connection outgoingConnection(Socket s) throws IOException{
		
		Connection con = super.outgoingConnection(s);

		return con;
	}
	
	
	/**
	 * A connection has been closed
	 */
	@Override
	public void connectionClosed(Connection con){
		super.connectionClosed(con);
		if (unauthClients.contains(con)) unauthClients.remove(con);
		if (loggedInUsernames.containsKey(con)) loggedInUsernames.remove(con);

	}
	
	
	/**
	 *  Process incoming msg, from connection con.
	 *  
	 *  @param con 	Incomming connection
	 *  @param msg	Incoming Message
	 *  @return		True if the connection is to be closed, false otherwise
	 */
	@Override
	public synchronized boolean process(Connection con,String msg){

		MessageFactory msgFactory = new MessageFactory();
		RulesEngine rulesEngine = new RulesEngine(log);

		JsonMessage receivedMessage = msgFactory.buildMessage(msg, log);
		return rulesEngine.triggerResponse(receivedMessage, con);

	}


	/**
	 * Called once every few seconds
	 * Servers announce their current load to all other servers
	 * Return true if server should shut down, false otherwise
	 */
	@Override
	public boolean doActivity(){

		ServerAnnounce serverAnnounce = new ServerAnnounce(Settings.getId(), getAuthClients().size(), Settings.getLocalHostname(), Settings.getLocalPort());

		// Sends Activity Boradcast to Authorized Servers only
		for(Connection c : getAuthServers()){
			c.writeMsg(serverAnnounce.toData());
		}

		return false;
	}
	
	/**
	 * Adds a user to the local storage
	 */
    public void addUser(String username, String secret) {
        getClientDB().put(username, secret);
    }

    /**
     * Tests if the user name and secret are not a match
     */
    public boolean userKnownDifferentSecret(String username, String secret) {
        return getClientDB().containsKey(username) && !getClientDB().get(username).equals(secret);
    }

    /**
     * Tests if the user name and secret are a match
     */
    public boolean userKnownSameSecret(String username, String secret) {
        return userKnown(username) && getClientDB().get(username).equals(secret);
    }
    
    /**
     * Tests if the user name if contained in the local storage
     */
    public boolean userKnown(String username) {
        return getClientDB().containsKey(username);
    }
    
    /**
     * Removes user from the local storage
     */
    public void removeUser(String username) {
        getClientDB().remove(username);
    }

    public void removeLockRequestsAndConnection(String username) {
        if (lockRequests.containsKey(username))
            lockRequests.remove(username);
        if (lockConnections.containsKey(username))
            lockConnections.remove(username);
    }
    
    // Getters & Setters
    public ArrayList<Connection> getUnauthClients() { return unauthClients; }
    public void addUnauthClient(Connection con) {getUnauthClients().add(con);}
    public void removeUnauthClient(Connection con) {getUnauthClients().remove(con);}
    public HashSet<Connection> getLockRequest(String username) { return lockRequests.get(username); }
    public boolean hasLockRequest(String username) { return lockRequests.containsKey(username); }
    public void addLockRequest(String username, HashSet<Connection> set) { lockRequests.put(username, set); }
    public void addConnectionForLock(String username, Connection con) { lockConnections.put(username, con); }
    public Connection getConnectionForLock(String username) { return lockConnections.get(username); }
    public boolean hasConnectionForLock(String username) { return lockConnections.containsKey(username); }


}	
