package activitystreamer.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private ArrayList<Connection> secureServers;		    // A list of servers using secure protocol

	public HashMap<Integer, Connection> dbLookup;
	private HashMap<Connection, ServerAnnounce> serverLoads;// Map of current server loads

	private boolean term=false;
	private Listener listener;
	protected static Control control = null;				// Singleton Object

	// Getters and Setters
	public final ArrayList<Connection> getConnections() {return connections;}
	public final ArrayList<Connection> getAuthServers() {return authServers;}
	public final ArrayList<Connection> getUnauthConnections() {return unauthConnections;}
	public final ArrayList<Connection> getAuthClients() {return authClients;}
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


		/* asks key register for connections public key and updates
		the hash map in control solution when it recieves its response
		//TODO: this is a bit of a hack
		// isSecure.. sends of request to key register to find key
		// then server process that like any other request and updates
		// the hash in the control solution accordingly
		// but if something happens like the response takes a  long time
		// or never, this connection will not be secure ( or until response returns)
		 */
		ControlSolution.getInstance().isSecureConnectionAndUpdate(c);

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
