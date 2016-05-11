package activitystreamer.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import activitystreamer.util.Settings;
import activitystreamer.messages.*;

public class Control extends Thread {
	
	private static final Logger log = LogManager.getLogger();
	private ArrayList<Connection> connections; 				// A list of all connections
	private ArrayList<Connection> authServers; 				// A list of authorized servers
	private ArrayList<Connection> authClients; 				// A list of logged in clients
	private ArrayList<Connection> unauthConnections; 		/* A list of unauthorized connections
															 (may be servers that havn't authorized or 
															 clients that havn't logged in) */
	
	private HashMap<String, String> clientDB;				// Map of Registered Users
	private HashMap<Connection, ServerAnnounce> serverLoads;// Map of current server loads

	private boolean term=false;
	private Listener listener;
	protected static Control control = null;				// Singleton Object

	// Getters and Setters
	public final ArrayList<Connection> getConnections() {return connections;}
	public final ArrayList<Connection> getAuthServers() {return authServers;}
	public final ArrayList<Connection> getUnauthConnections() {return unauthConnections;}
	public final ArrayList<Connection> getAuthClients() {return authClients;}
	public final HashMap<String, String> getClientDB() { return clientDB; }
	public final HashMap<Connection, ServerAnnounce> getServerLoads() { return serverLoads; }

	
	public static Control getInstance() {
		if(control==null){
			control=new Control();
		} 
		return control;
	}
	
	public Control() {
		
		// initialize the connections arrays
		authServers = new ArrayList<Connection>();
		unauthConnections = new ArrayList<Connection>();
		authClients = new ArrayList<Connection>();
		connections = new ArrayList<Connection>();
		clientDB = new HashMap<String, String>();
		serverLoads = new HashMap<Connection, ServerAnnounce>();
		
		// start a listener
		try {
			listener = new Listener();
		} catch (IOException e1) {
			log.fatal("failed to startup a listening thread: "+e1);
			System.exit(-1);
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
			
			try {
				// Establish a connection
				Connection c = outgoingConnection(new Socket(Settings.getRemoteHostname(),Settings.getRemotePort()));
				
				// Send JSON Authenticate message
				Authenticate authenticateMsg = new Authenticate(Settings.getSecret());
				log.info("Sending Authentication Request to: " + Settings.getRemoteHostname() + ", with Secret: " + authenticateMsg.getSecret());
				c.writeMsg(authenticateMsg.toData());
				
				// Add to authorized connections
				authServers.add(c);
				
			} catch (IOException e) {
				log.error("failed to make connection to "+Settings.getRemoteHostname()+":"+Settings.getRemotePort()+" :"+e);
				System.exit(-1);
			}
		}
	}
	
	/**
	 * Processing incoming messages from the connection.
	 * Return true if the connection should close.
	 */
	public synchronized boolean process(Connection con,String msg){
		
		return true;
	}
	
	/*
	 * The connection has been closed by the other party.
	 */
	public synchronized void connectionClosed(Connection con){
		if(!term) {
			connections.remove(con);
			if (authClients.contains(con)) authClients.remove(con);
			if (authServers.contains(con)) { authServers.remove(con); }
			if (serverLoads.containsKey(con)) { serverLoads.remove(con); }
			if (unauthConnections.contains(con)) unauthConnections.remove(con);
			if (ControlSolution.getInstance().getUnauthClients().contains(con))
				ControlSolution.getInstance().removeUnauthClient(con);
		}
	}
	
	/*
	 * A new incoming connection has been established, and a reference is returned to it
	 */
	public synchronized Connection incomingConnection(Socket s) throws IOException{
		log.debug("incomming connection: "+Settings.socketAddress(s));
		Connection c = new Connection(s);
		connections.add(c);
		unauthConnections.add(c);
		return c;
		
	}
	
	/*
	 * A new outgoing connection has been established, and a reference is returned to it
	 */
	public synchronized Connection outgoingConnection(Socket s) throws IOException{
		log.debug("outgoing connection: "+Settings.socketAddress(s));
		Connection c = new Connection(s);
		connections.add(c);
		unauthConnections.add(c);
		return c;
		
	}
	
	@Override
	public void run(){
		while(!term){
			// do something with 5 second intervals in between
			try {
				Thread.sleep(Settings.getActivityInterval());
			} catch (InterruptedException e) {
				log.info("received an interrupt, system is shutting down");
				break;
			}
			if(!term){
				term=doActivity();
			}
			
		}
		log.info("closing "+connections.size()+" connections");
		// clean up
		for(Connection connection : connections){
			connection.closeCon();
		}
		listener.setTerm(true);
		
	}
	
	public boolean doActivity(){
		return false;
	}
	
	public final void setTerm(boolean t){
		term=t;
	}

	
}
