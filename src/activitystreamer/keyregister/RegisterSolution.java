package activitystreamer.keyregister;

import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import activitystreamer.keyregister.Listener;
import activitystreamer.keyregister.Connection;
import activitystreamer.messages.*;
import activitystreamer.util.Settings;

public class RegisterSolution extends Thread{

	private static final Logger log = LogManager.getLogger();
    private ArrayList<Connection> connections; 				// A list of all connections
    private ArrayList<Connection> unauthConnections;
    private ArrayList<Connection> authConnections;
    private HashMap<String,PublicKey> keyStore;
    private String secret;
    private Listener listener;
    private boolean term=false;
	
	
	public RegisterSolution(){
		
		keyStore = new HashMap<String,PublicKey>();
		
		// start a listener
        try {
            listener = new Listener(this);
        } catch (IOException e1) {
            log.fatal("failed to startup a listening thread: "+e1);
            System.exit(-1);
        }
		
		start();
	}
	
	 public void run() {
        while (!term) {
            // do something with 5 second intervals in between
            try {
                Thread.sleep(Settings.getActivityInterval());
            } catch (InterruptedException e) {
                log.info("received an interrupt, system is shutting down");
                break;
            }
        }

        // Safely close all connections
        log.info("closing "+connections.size()+" connections");

        for(Connection connection : connections){
            connection.closeCon();
        }
        listener.setTerm(true);
    }
	
    /*
     * A new incoming connection has been established, and a reference is returned to it
     */
     public synchronized Connection incomingConnection(Socket s) throws IOException{
		
		log.debug("incomming connection: "+ Settings.socketAddress(s));
        Connection c = new Connection(s, this);
        connections.add(c);
        unauthConnections.add(c);
        return c;
	}



    public synchronized void connectionClosed(Connection con){
         if(!term) {
             connections.remove(con);
             if (authConnections.contains(con)) authConnections.remove(con);
             if (unauthConnections.contains(con)) unauthConnections.remove(con);
         }
     }
    
	
	public synchronized boolean process(Connection connection, String msg) {
		
		MessageFactory msgFactory = new MessageFactory();

        JsonMessage receivedMessage = msgFactory.buildMessage(msg, log);

        switch (receivedMessage.getCommand()) {

        	case "REGISTER_KEY" : 
        		return triggerRegisterKey((RegisterKey) receivedMessage, connection);
        	
        }
        
		return false;
	}
	
	private boolean triggerRegisterKey(RegisterKey msg, Connection con){
		
		return false;
		
	}
	
	
	public final void setTerm(boolean t){
        term=t;
    }

}
