package activitystreamer.client;


import activitystreamer.util.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

/** Class contains information related to the Clients connection */
public class Connection extends Thread {
	
	private static final Logger log = LogManager.getLogger();
	private DataInputStream in;
	private DataOutputStream out;
	private BufferedReader inreader;
	private PrintWriter outwriter;
	private boolean open = false;
	private Socket socket;
	private boolean term=false;
	
	Connection(Socket socket) throws IOException{
		in = new DataInputStream(socket.getInputStream());
	    out = new DataOutputStream(socket.getOutputStream());
	    inreader = new BufferedReader( new InputStreamReader(in));
	    outwriter = new PrintWriter(out, true);
	    this.socket = socket;
	    open = true;
		start();
	}
	
	/**
	 * Returns true if the message was written, otherwise false
	 */
	public boolean writeMsg(String msg) {
		if(open){
			outwriter.println(msg);
			outwriter.flush();
			return true;	
		}
		return false;
	}
	
	
	public void closeCon(){
		log.info("closing connection "+Settings.socketAddress(socket));
		try {
			term=true;
			open = false;
			inreader.close();
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			// already closed?
			log.error("received exception closing the connection "+Settings.socketAddress(socket)+": "+e);
		}
	}


	public void run() {

		try {
			String data;
			while(!term && (data = inreader.readLine())!=null){
				term = ClientSolution.getInstance().process(this, data);
			}
			log.debug("connection closed to "+Settings.socketAddress(socket));
			closeCon();
		} catch (IOException e) {
			log.error("connection "+Settings.socketAddress(socket)+" closed with exception: "+e);
			closeCon();
		}


	}

	public Socket getSocket() {
		return socket;
	}
	
	public boolean isOpen() {
		return open;
	}
	
	public PrintWriter getOutwriter(){
		return outwriter;
	}
}
