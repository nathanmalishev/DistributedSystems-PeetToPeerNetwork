package activitystreamer.client;

import activitystreamer.client.RulesEngine;
import activitystreamer.messages.*;
import activitystreamer.util.Helper;
import activitystreamer.util.Settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.Socket;
import java.security.PublicKey;

import javax.crypto.SecretKey;


/** Class handles the Client Functionality. Including connecting to Server,
 * sending activity messages and incoming message handling */
public class ClientSolution extends Thread {
	
	private static ClientSolution clientSolution;		// Singleton Object
	private TextFrame textFrame;						// GUI Frame
	
	private boolean open = false;						// Connection open flag
	private boolean redirect = false;					// Redirect needed flag

	public Connection myConnection;						// Connection to Server
	private JSONParser parser = new JSONParser();		
	private RulesEngine rulesEngine;					// Handles message processing
	private Socket s;									// Connection socket
	private static final Logger log = LogManager.getLogger();
	
	private static PublicKey serverPubKey;
	private SecretKey secretKey;
	
	/* Getters and Setters */
	public void setOpen(boolean open) { this.open = open; }
	public void setRedirect(boolean redirect) { this.redirect = redirect; }
	public TextFrame getTextFrame() { return textFrame; }

	// this is a singleton object
	public static ClientSolution getInstance(){
		if(clientSolution==null){
			clientSolution = new ClientSolution();
		}
		return clientSolution;
	}
	
	public ClientSolution(){
		open = true;
		rulesEngine = new RulesEngine(log);
		initialiseConnection();

		// open the gui
		log.debug("opening the gui");
		textFrame = new TextFrame();
		// start the client's thread
		start();
	}

	/**
	 * Initializes connection to the Server.
	 */
	private void initialiseConnection() {

		connectToServer();
		createSecretKey();

		// If secret is null, attempt to register
		if (Settings.getSecret() == null && !Settings.getUsername().equals("anonymous")) {
			Settings.setSecret(Settings.nextSecret());
			rulesEngine.triggerRegister(myConnection);
		}
		// Otherwise attempt to login
		else {
			rulesEngine.triggerLogin(myConnection);
		}

	}
	
	// TODO: Test
	public static void decodePublicKey(String serverKey){
		
		serverPubKey = Helper.stringToPublicKey(serverKey);
	}
	
	// TODO: Complete
	public void createSecretKey(){
		
		// check if publicKey != null
		// Create SecretKeyObject
		// Encode SecretKeyObject using public Key
		// Send encoded secret key to server when sending first message
	}

	/**
	 * Creates a socket to connect to and instantiates a Connection object with that Socket 
	 */
	public void connectToServer() {
		
		try {
			s = new Socket(Settings.getRemoteHostname(), Settings.getRemotePort());
			myConnection = new Connection(s);
			
			// TODO: Test
			log.info("Setting up connection with key register");
			Connection krCon = new Connection(new Socket(Settings.getKeyRegisterHostname(), Settings.getKeyRegisterPort()));
			log.info("Sending GETKEY message");
			rulesEngine.triggerGetKeyMessage(krCon);
			
		} catch(Exception e) {
			System.out.print(e);
		}
	}
	
	/** 
	 * Called when redirecting, to update server details.
	 * 
	 * @param hostname	New Host connection
	 * @param port		New Port connection
	 */
	public void resetServer(String hostname, int port) {
		Settings.setRemoteHostname(hostname);
		Settings.setRemotePort(port);
	}
	
	/**
	 * Connects to the newly given hostname and port, and attempts to login.
	 */
	public void redirectConnection() {
		connectToServer();
		rulesEngine.triggerLogin(myConnection);
	}

	/** called by the GUI when the user clicks "send"
	 * 
	 * @param activityObj
	 */
	public void sendActivityObject(JSONObject activityObj){
		
		try{
			ActivityMessage activityMessage = new ActivityMessage(Settings.getUsername(), Settings.getSecret(), activityObj);
			myConnection.writeMsg(activityMessage.toData());

			log.debug("Message successfully sent: " + activityObj.toString());

		}catch(Exception e){
			System.out.print(e);
		}
	}
	
	/** 
	 * Called by the GUI when the user clicks disconnect 
	 */
	public void disconnect(){
		
		textFrame.setVisible(false);
		rulesEngine.triggerLogout(myConnection);
		myConnection.closeCon();
		System.exit(1);
	}
	

	/**
	 *  The client's run method, to receive messages
	 */
	@Override
	public void run(){
		
		// Continues until the connection is closed with the client
		while (open) {
			
			// Redirect if required
			if (!myConnection.isOpen()) {
				if (redirect) {
					redirectConnection();
					this.redirect = false;
				}
				else {
					this.open = false;
				}
			}
			else {
				try{
					myConnection.listen();
				}catch(Exception e){
					log.error("connection "+Settings.socketAddress(s)+ "" +
							"closed with exception: "+e );
				}
			}
		}
	}
	
	/**
	 * Method handles processing of incoming messages.
	 * 
	 * @param con Connection of incoming message
	 * @param msg Message received
	 * @return True if the connection is to be closed, false otherwise
	 */
	public boolean process(Connection con, String msg){

		MessageFactory msgFactory = new MessageFactory();
		JsonMessage receivedMessage = msgFactory.buildMessage(msg, log);
		return rulesEngine.triggerResponse(receivedMessage, con);
	}

}
