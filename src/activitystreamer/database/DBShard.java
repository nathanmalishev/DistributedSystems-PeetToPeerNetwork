package activitystreamer.database;

import activitystreamer.messages.*;
import activitystreamer.util.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class DBShard extends Thread {

    private static final Logger log = LogManager.getLogger();
    private char start;
    private char end;
    private ArrayList<Connection> connections; 				// A list of all connections
    private ArrayList<Connection> unauthConnections;
    private ArrayList<Connection> authConnections;
    private String secret;
    private HashMap<String, String> clientShard;				// Map of Registered Users
    private Listener listener;
    private boolean term=false;
    private static final char[] startBoundaries = {'a', 'h', 'n', 'u'};
    private static final char[] endBoundaries = {'g', 'm', 't', 'z'};
    public final void setTerm(boolean t){
        term=t;
    }

    public DBShard(int num) {
        this.start = startBoundaries[num];
        this.end = endBoundaries[num];
        connections = new ArrayList();
        unauthConnections = new ArrayList();
        clientShard = new HashMap();

        // start a listener
        try {
            listener = new Listener(this);
        } catch (IOException e1) {
            log.fatal("failed to startup a listening thread: "+e1);
            System.exit(-1);
        }

        start();
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
 * The connection has been closed by the other party.
 */
    public synchronized void connectionClosed(Connection con){
        if(!term) {
            connections.remove(con);
            if (authConnections.contains(con)) authConnections.remove(con);
            if (unauthConnections.contains(con)) unauthConnections.remove(con);
        }
    }

    public synchronized boolean process(Connection con, String msg) {
        System.out.println("PROCESSING: " + msg);

        MessageFactory msgFactory = new MessageFactory();

        JsonMessage receivedMessage = msgFactory.buildMessage(msg, log);

        switch (receivedMessage.getCommand()) {

            case "REGISTER" :
                return triggerRegisterRead((Register) receivedMessage, con);

            case "LOGIN" :
        }

        return false;
    }

    private boolean triggerRegisterRead(Register msg, Connection con) {

        String username = msg.getUsername();
        if (!checkBounds(username.charAt(0))) {
            return triggerInvalidMessage(con, InvalidMessage.invalidShardBoundaryError);
        }

        System.out.println("Got the register for " + username);

        return false;
    }

    /**
     * Sends an Invalid_Message to the connection
     */
    public boolean triggerInvalidMessage(Connection con, String info) {

        log.info("Sending Invalid Message Response: " + info);
        JsonMessage response = new InvalidMessage(info);
        con.writeMsg(response.toData());

        return true;
    }

    private boolean checkBounds(char firstLetter) {
        if (firstLetter >= start && firstLetter <= end) {
            return true;
        }
        return false;
    }

}
