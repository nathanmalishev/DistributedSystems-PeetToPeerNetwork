package activitystreamer.client;

import activitystreamer.util.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.xml.soap.Text;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;

public class ClientSolution extends Thread {
	private static final Logger log = LogManager.getLogger();
	private static ClientSolution clientSolution;
	private TextFrame textFrame;
	private PrintWriter outwriter;
	/*
	 * additional variables
	 */
    private Socket connection;
	private BufferedReader inreader;
	private DataInputStream in;
	private JSONParser parser = new JSONParser();
	
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
			connection = new Socket(Settings.getLocalHostname(), Settings.getRemotePort());
			/* for reading messages */
			in = new DataInputStream(connection.getInputStream());
			inreader = new BufferedReader(new InputStreamReader(in));

			/* For writting messages */
			DataOutputStream out = new DataOutputStream(connection.
					getOutputStream());
			outwriter = new PrintWriter(out, true);

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
			outwriter.println(textFrame.getInputText().replaceAll("(\\r|\\n|\\t)", ""));
			outwriter.flush();
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
	}
	

	// the client's run method, to receive messages
	@Override
	public void run(){

		try{
			String msg = (inreader.readLine());
			textFrame.setOutputText( (JSONObject) parser.parse(msg) );
		}catch(Exception e){
			log.error("connection "+Settings.socketAddress(connection)+ "" +
					"closed with exception: "+e );

		}

		
		
	}

	/*
	 * additional methods
	 */
	
}
