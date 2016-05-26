package activitystreamer.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import activitystreamer.keyregister.RegisterSolution;
import activitystreamer.util.Helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Need to change this so it is importing the one in our library

import activitystreamer.messages.*;
import activitystreamer.util.Settings;

import activitystreamer.database.DBShard;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.crypto.SecretKey;

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
	private Connection KRCon;
	private HashMap<Connection, SecretKey> keyMap;
	
	private PublicKey publicKey;
	private PrivateKey privateKey;
	private HashMap<String, Connection> lockRequestWaiting;

	private HashMap<String, SecretKey> secureServerHash = new HashMap<String, SecretKey>();

	public HashMap<String, Connection> getLockRequestWaiting() { return lockRequestWaiting; }
	public HashMap<Connection, SecretKey> getKeyMap() {return keyMap;}
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
		keyMap = new HashMap<>();
		
		// Create Public and Private Key
		generateKeyPair();
		
		lockRequestWaiting = new HashMap<>();

		start();
	}
	
	public void generateKeyPair(){
		
		KeyPairGenerator keyGen;
		
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
			
			// A key pair generator needs to be initialized before it can generate keys.
		    keyGen.initialize(1024);
		    KeyPair keyPair = keyGen.generateKeyPair();
		    this.publicKey = keyPair.getPublic();
		    this.privateKey = keyPair.getPrivate();
		    
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Called when starting a server and connects to the provided remote host
	 * if it is supplied.
	 * To successfully connect the secret must match that of the given remote host.
	 */
	public void initiateConnection(){


		// make a connection to another server if remote hostname is supplied
		if(Settings.getRemoteHostname()!=null){

			setDBSettings();
			setKRSettings();

			initialiseKRConnections();

			// We want to try and get public key from key register
			publicKeyRead(Settings.getRemoteHostname(), String.valueOf(Settings.getRemotePort()));

		} else {
			setupDB();
			setupKR();

			initialiseKRConnections();


		}
		initialiseDBConnections();

		publicKeyWrite(Settings.getLocalHostname(), Settings.getLocalPort());

	}


	public void publicKeyRead(String hostname, String port) {

		try{
			String query = Helper.createUniqueServerIdentifier(hostname,port);
			GetKey getKeyMsg = new GetKey(query);
			KRCon.writeMsg(getKeyMsg.toData());
		}catch( Exception e){
			System.out.println(e);
		}


	}

	public void publicKeyWrite(String hostname, int port) {

		String publicKeyString = Helper.publicKeyToString(this.publicKey);
		String uniqueId = Helper.createUniqueServerIdentifier(hostname, Integer.toString(port));

		RegisterKey keyRegisterMsg = new RegisterKey(publicKeyString, uniqueId);

		KRCon.writeMsg(keyRegisterMsg.toData());

	}



	public void setKRSettings() {
		log.info("Setting key register settings");
		if (Settings.getKeyRegisterHostname() == null) {
			Settings.setKeyRegisterHostname(Settings.defaultKRHostname());
			Settings.setKeyRegisterPort(Settings.defaultKRPort());
		}
	}

	public void setupKR() {
		log.info("Starting key register");
		if (Settings.getKeyRegisterHostname() == null) {
			Settings.setKeyRegisterHostname(Settings.defaultKRHostname());
			Settings.setKeyRegisterPort(Settings.defaultKRPort());
			final RegisterSolution KeyRegistry = new RegisterSolution(Settings.getKeyRegisterPort());
		}
	}

	public void setDBSettings() {
		log.info("Setting DB Settings");
		if (Settings.getShardAHostname() == null) {
			Settings.setShardAPort(Settings.defaultShardAPort());
			Settings.setShardAHostname(Settings.defaultShardHostname());
		}
		if (Settings.getShardBHostname() == null) {
			Settings.setShardBPort(Settings.defaultShardBPort());
			Settings.setShardBHostname(Settings.defaultShardHostname());
		}
		if (Settings.getShardCHostname() == null) {
			Settings.setShardCPort(Settings.defaultShardCPort());
			Settings.setShardCHostname(Settings.defaultShardHostname());
		}
		if (Settings.getShardDHostname() == null) {
			Settings.setShardDPort(Settings.defaultShardDPort());
			Settings.setShardDHostname(Settings.defaultShardHostname());
		}
	}

	public void setupDB() {
		log.info("Starting database shards");
		if (Settings.getShardAHostname() == null) {
			final DBShard db1 = new DBShard(0, Settings.defaultShardAPort());
			Settings.setShardAPort(Settings.defaultShardAPort());
			Settings.setShardAHostname(Settings.defaultShardHostname());
		}
		if (Settings.getShardBHostname() == null) {
			final DBShard db2 = new DBShard(1, Settings.defaultShardBPort());
			Settings.setShardBPort(Settings.defaultShardBPort());
			Settings.setShardBHostname(Settings.defaultShardHostname());
		}
		if (Settings.getShardCHostname() == null) {
			final DBShard db3 = new DBShard(2, Settings.defaultShardCPort());
			Settings.setShardCPort(Settings.defaultShardCPort());
			Settings.setShardCHostname(Settings.defaultShardHostname());
		}
		if (Settings.getShardDHostname() == null) {
			final DBShard db4 = new DBShard(3, Settings.defaultShardDPort());
			Settings.setShardDPort(Settings.defaultShardDPort());
			Settings.setShardDHostname(Settings.defaultShardHostname());
		}

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

	public void initialiseKRConnections() {
		log.info("Initialising connection to key registry");
		try {
			Connection c = outgoingConnection(new Socket(Settings.getKeyRegisterHostname(), Settings.getKeyRegisterPort()));
			this.KRCon = c;
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
		HashMap<Connection, SecretKey> secureConnections = ControlSolution.getInstance().getKeyMap();
		// Sends Activity Boradcast to Authorized Servers only
		Iterator itr = getAuthServers().iterator();
		while(itr.hasNext()) {
			Connection c = (Connection)itr.next();
				c.writeMsg(serverAnnounce, c, secureConnections);
		}

		return false;
	}

	public void run(){
		initiateConnection();
		while(!getTerm()){
			// do something with 5 second intervals in between
			try {
				Thread.sleep(Settings.getActivityInterval());
			} catch (InterruptedException e) {
				log.info("received an interrupt, system is shutting down");
				break;
			}
			if(!getTerm()){
				setTerm(doActivity());
			}

		}
		log.info("closing "+getConnections().size()+" connections");
		// clean up
		for(Connection connection : getConnections()){
			connection.closeCon();
		}
		getListener().setTerm(true);

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

	public HashMap<String, SecretKey> getSecureServerHash() {
		return secureServerHash;
	}
    public PrivateKey getPrivateKey() {return privateKey;}

}	
