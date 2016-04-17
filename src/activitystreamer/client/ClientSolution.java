package activitystreamer.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

public class ClientSolution extends Thread {
	private static final Logger log = LogManager.getLogger();
	private static ClientSolution clientSolution;
	private TextFrame textFrame;
	
	/*
	 * additional variables
	 */
	
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

		// open the gui
		log.debug("opening the gui");
		textFrame = new TextFrame();
		// start the client's thread
		start();
	}
	
	// called by the gui when the user clicks "send"
	public void sendActivityObject(JSONObject activityObj){
		
	}
	
	// called by the gui when the user clicks disconnect
	public void disconnect(){
		textFrame.setVisible(false);
		/*
		 * other things to do
		 */
	}
	

	// the client's run method, to receive messages
	@Override
	public void run(){
		
	}

	/*
	 * additional methods
	 */
	
}
