package activitystreamer.client;

import activitystreamer.client.RulesEngine;
import activitystreamer.messages.JsonMessage;
import activitystreamer.messages.MessageFactory;
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
	/*
	 * additional variables
	 */
	public Connection myConnection;
	private JSONParser parser = new JSONParser();
	private RulesEngine rulesEngine;
	private Socket s;
	// this is a singleton object
	public static ClientSolution getInstance(){
		if(clientSolution==null){
			clientSolution = new ClientSolution();
		}
		return clientSolution;
	}
	
	public ClientSolution(){
		/*
		 * some additional initialization
		 */
		try {
			s = new Socket(Settings.getLocalHostname(), Settings.getRemotePort());
			myConnection =new Connection(s);

			System.out.print("connection started to server ");
		}catch(Exception e){
			System.out.print(e);
		}

		// open the gui
		log.debug("opening the gui");
		textFrame = new TextFrame();
		// start the client's thread
		start();
	}
	
	// called by the gui when the user clicks "send"
	public void sendActivityObject(JSONObject activityObj){
		try{
			//FIXME: Server will not recieve messages from gui, unless
			//new lines etc have been removed. Though this is in the lectures
			//implementation of reading in the data.
			myConnection.writeMsg(textFrame.getInputText().replaceAll("(\\r|\\n|\\t)", ""));
			log.debug("Message successfully sent: " + textFrame.getInputText().replaceAll("(\\r|\\n|\\t)", ""));

		}catch(Exception e){
			System.out.print(e);
		}
	}
	
	// called by the gui when the user clicks disconnect
	public void disconnect(){
		textFrame.setVisible(false);
		/*
		 * other things to do
		 */
		myConnection.closeCon();
	}
	

	// the client's run method, to receive messages
	@Override
	public void run(){

		try{
			myConnection.run();

		}catch(Exception e){
			log.error("connection "+Settings.socketAddress(s)+ "" +
					"closed with exception: "+e );

		}
	}

	/*
	 * additional methods
	 */

	public boolean process(Connection con, String msg){

		try{
			textFrame.setOutputText( (JSONObject) parser.parse(msg) );
		}catch(Exception e){

		}

		MessageFactory msgFactory = new MessageFactory();
		rulesEngine = new RulesEngine(log);

		JsonMessage receivedMessage = msgFactory.buildMessage(msg, log);
		return rulesEngine.triggerResponse(receivedMessage, con);

	}

}
