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
    private HashMap<String,String> keyStore;
    private String secret;
    private Listener listener;
    private boolean term=false;
	private int portNumber;

	public int getPortNumber() {
		return portNumber;
	}

	public RegisterSolution(int portNumber){
		this.portNumber = portNumber;
		keyStore = new HashMap<String,String>();
		connections = new ArrayList();
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
		log.info("incomming connection: "+ Settings.socketAddress(s));
        Connection c = new Connection(s, this);
		 connections.add(c);
        return c;
	}


     // UNUSED
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
        	
        	case "GET_KEY" :
        		return triggerGetKey((GetKey) receivedMessage, connection);
        		
        	default :
                return triggerInvalidMessage(connection, InvalidMessage.invalidMessageTypeError);
        }
        
	}
	
	/**
     * Swallows all random messages from other servers
     */
    public boolean triggerInvalidMessage(Connection con, String info) {

//        log.info("Sending Invalid Message Response: " + info);
//        JsonMessage response = new InvalidMessage(info);
//        con.writeMsg(response.toData());

        return false;		// Do we want this connection to close - Change to true if so??
    }
	
	private boolean triggerRegisterKey(RegisterKey msg, Connection con){
		log.info("Attmepting to register a new key");
		String info, result;

		if(keyStore.containsKey(msg.getServerId())){

			// Check to see if key already exists
			if(keyStore.get(msg.getServerId()).equals(msg.getPublicKeyStr())){
				info = KeyRegisterResponse.keyExists;
				log.info("Server already registered with key registry");
				result = "SUCCESS";
			}
			// Else Public Key supplied doesn't match
			else{
				info = KeyRegisterResponse.invalidKey;
				log.info("Invalid key register");
				result = "FAILED";
			}
		}
		else{
			// Store new key in register
			keyStore.put(msg.getServerId(), msg.getPublicKeyStr());
			info = KeyRegisterResponse.keyRegisterSuccess;
			log.info("Successfully registered new public key");
			result = "SUCCESS";
		}

		triggerRegisterKeyResponse(info, msg.getServerId(), result, con);
		return false;

	}
	
	public boolean triggerRegisterKeyResponse(String info, String serverId, String result, Connection con){
		
		log.info("Key Register for: " + serverId + " " + result);
		con.writeMsg(new KeyRegisterResponse(info, result).toData());
		
		return false;
	}
	
	//TODO: Test
	private boolean triggerGetKey(GetKey msg, Connection con){

		log.info("Triggering key retrieval");
		if(keyStore.containsKey(msg.getServerId())){
			log.info("Successful key retrieval");

			// Send key to the Client
			String publicKey = keyStore.get(msg.getServerId());
			GetKeySuccess response = new GetKeySuccess(publicKey, msg.getServerId());

			con.writeMsg(response.toData());
		}
		else{
			log.info("Unsuccessful key retrieval");
			// Send failure message to the client
			GetKeyFailed response = new GetKeyFailed(GetKeyFailed.serverKeyDoesntExist);

			con.writeMsg(response.toData());
		}

		return false;
	}
	
	
	public final void setTerm(boolean t){
        term=t;
    }

}
