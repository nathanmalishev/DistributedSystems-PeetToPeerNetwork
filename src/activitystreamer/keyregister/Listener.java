package activitystreamer.keyregister;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import activitystreamer.util.Settings;


public class Listener extends Thread{
	
	private static final Logger log = LogManager.getLogger();
	private ServerSocket serverSocket=null;
	private boolean term = false;
	private int portnum;
	private RegisterSolution keyStore;
	
	public Listener(RegisterSolution keyStore) throws IOException{
		portnum = keyStore.getPortNumber(); // keep our own copy in case it changes later
		serverSocket = new ServerSocket(portnum);
		this.keyStore = keyStore;
		start();
	}
	
	@Override
	public void run() {
		log.info("listening for new connections on "+portnum);
		while(!term){
			Socket clientSocket;
			try {
				clientSocket = serverSocket.accept();
				keyStore.incomingConnection(clientSocket);
			} catch (IOException e) {
				log.info("received exception, shutting down");
				term=true;
			}
		}
	}
	
	public void setTerm(boolean term) {
		this.term = term;
		if (term) {
			try {
				serverSocket.close();

			} catch (IOException io) {
				log.error("Server Socket close Error");
			}

		}
	}
	
}
