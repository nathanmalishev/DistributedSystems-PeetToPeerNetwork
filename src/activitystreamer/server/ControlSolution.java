package activitystreamer.server;

import java.io.IOException;
import java.net.Socket;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class ControlSolution extends Control {
	private static final Logger log = LogManager.getLogger();
	
	/*
	 * additional variables as needed
	 */
	
	// since control and its subclasses are singleton, we get the singleton this way
	public static ControlSolution getInstance() {
		if(control==null){
			control=new ControlSolution();
		} 
		return (ControlSolution) control;
	}
	
	public ControlSolution() {
		super();
		/*
		 * Do some further initialization here if necessary
		 */

		
		// check if we should initiate a connection and do so if necessary
		initiateConnection();
		// start the server's activity loop
		// it will call doActivity every few seconds
		start();
	}
	
	
	/*
	 * a new incoming connection
	 */
	@Override
	public Connection incomingConnection(Socket s) throws IOException{
		Connection con = super.incomingConnection(s);
		/*
		 * do additional things here
		 */
		
		return con;
	}
	
	/*
	 * a new outgoing connection
	 */
	@Override
	public Connection outgoingConnection(Socket s) throws IOException{
		Connection con = super.outgoingConnection(s);
		/*
		 * do additional things here
		 */
		
		
		return con;
	}
	
	
	/*
	 * the connection has been closed
	 */
	@Override
	public void connectionClosed(Connection con){
		super.connectionClosed(con);
		/*
		 * do additional things here
		 */
	}
	
	
	/*
	 * process incoming msg, from connection con
	 * return true if the connection should be closed, false otherwise
	 */
	@Override
	public synchronized boolean process(Connection con,String msg){
		/*
		 * do additional work here
		 * return true/false as appropriate
		 */
		
		return false;
	}

	
	
	/*
	 * Called once every few seconds
	 * Return true if server should shut down, false otherwise
	 */
	@Override
	public boolean doActivity(){
		/*
		 * do additional work here
		 * return true/false as appropriate
		 */
		
		return false;
	}
	
	/*
	 * Other methods as needed
	 */
	
	
}
