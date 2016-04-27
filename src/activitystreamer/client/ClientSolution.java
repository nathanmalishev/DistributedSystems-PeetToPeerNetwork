package activitystreamer.client;

import activitystreamer.client.RulesEngine;
import activitystreamer.messages.*;
import activitystreamer.util.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.Socket;



public class ClientSolution extends Thread {
	private static final Logger log = LogManager.getLogger();
	private static ClientSolution clientSolution;
	private TextFrame textFrame;
	private boolean open = false;
	private boolean redirect = false;
	/*
	 * additional variables
	 */
	public Connection myConnection;
	private JSONParser parser = new JSONParser();
	private RulesEngine rulesEngine;
	private Socket s;
	public TextFrame getTextFrame() { return textFrame; }
	public void setOpen(boolean open) { this.open = open; }

	public void setRedirect(boolean redirect) { this.redirect = redirect; }

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

	private void initialiseConnection() {

		connectToServer();

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

	public void connectToServer() {
		try {
			s = new Socket(Settings.getRemoteHostname(), Settings.getRemotePort());
			myConnection = new Connection(s);
		} catch(Exception e) {
			System.out.print(e);
		}
	}

	public void resetServer(String hostname, int port) {
		Settings.setRemoteHostname(hostname);
		Settings.setRemotePort(port);
	}

	public void redirectConnection() {
		connectToServer();
		rulesEngine.triggerLogin(myConnection);
	}

	// called by the gui when the user clicks "send"
	public void sendActivityObject(JSONObject activityObj){
		try{
			ActivityMessage activityMessage = new ActivityMessage(Settings.getUsername(), Settings.getSecret(), activityObj.toJSONString());

			myConnection.writeMsg(activityMessage.toData());

			log.debug("Message successfully sent: " + activityObj.toString());

		}catch(Exception e){
			System.out.print(e);
		}
	}
	
	// called by the gui when the user clicks disconnect
	public void disconnect(){
		textFrame.setVisible(false);

		rulesEngine.triggerLogout(myConnection);

		myConnection.closeCon();
	}
	

	// the client's run method, to receive messages
	@Override
	public void run(){
		while (open) {
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

	public boolean process(Connection con, String msg){

		MessageFactory msgFactory = new MessageFactory();
		JsonMessage receivedMessage = msgFactory.buildMessage(msg, log);
		return rulesEngine.triggerResponse(receivedMessage, con);
	}

}

/* 		try{
			textFrame.setOutputText( (JSONObject) parser.parse(msg) );
		}catch(Exception e){

		}*/