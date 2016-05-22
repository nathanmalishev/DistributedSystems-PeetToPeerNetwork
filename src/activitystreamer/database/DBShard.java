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
    private int portnum;
    private boolean term=false;
    private static final char[] startBoundaries = {'a', 'h', 'n', 'u'};
    private static final char[] endBoundaries = {'g', 'm', 't', 'z'};
    public final void setTerm(boolean t){
        term=t;
    }

    public int getPortnum() { return portnum; }

    public DBShard(int dbnum, int portnum) {
        this.start = startBoundaries[dbnum];
        this.end = endBoundaries[dbnum];
        this.portnum = portnum;
        connections = new ArrayList();
        unauthConnections = new ArrayList();
        authConnections = new ArrayList();
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
            if (authConnections.contains(con)) { authConnections.remove(con); }
            if (unauthConnections.contains(con)) unauthConnections.remove(con);
        }
    }

    public synchronized boolean process(Connection con, String msg) {
        System.out.println("PROCESSING: " + msg);

        MessageFactory msgFactory = new MessageFactory();

        JsonMessage receivedMessage = msgFactory.buildMessage(msg, log);

        switch (receivedMessage.getCommand()) {

            case "WRITE_REQUEST" :
                return triggerWriteRequest((WriteRequest) receivedMessage, con);

            case "READ_REQUEST" :
                return triggerReadRequest((ReadRequest) receivedMessage, con);
        }

        return false;
    }

    private boolean triggerReadRequest(ReadRequest msg, Connection con) {

        String result, info;
        if (clientShard.containsKey(msg.getUsername())) {

            if (clientShard.get(msg.getUsername()).equals(msg.getSecret())) {
                result = "SUCCESS";
                info = LoginSuccess.loginSuccess;
            }
            else {
                result = "FAILED";
                info = LoginFailed.incorrectSecretError;
            }
        } else {
            result = "FAILED";
            info = LoginFailed.genericLoginFailedError;
        }
        triggerReadReply(msg.getUsername(), con, result, info);
        return false;

    }

    private boolean triggerReadReply(String username, Connection con, String result, String info) {

        log.info("Read " + result + " for " + username);
        con.writeMsg(new ReadReply(username, result, info).toData());
        return false;

    }


    private boolean triggerWriteRequest(WriteRequest msg, Connection con) {
        System.out.println("trying to write");
        String username = msg.getUsername();
        if (!checkBounds(username.charAt(0))) {
            return triggerInvalidMessage(con, InvalidMessage.invalidShardBoundaryError);
        }

        System.out.println("Got the register for " + username);
        String result, info;
        if (clientShard.containsKey(username)) {
            result = "FAILED";
            info = RegisterFailed.userAlreadyRegistered;
        } else {
            clientShard.put(username, msg.getSecret());
            result = "SUCCESS";
            info = RegisterSuccess.registerSuccessMsg;
        }
        return triggerWriteReply(username, con, result, info);
    }


    private boolean triggerWriteReply(String username, Connection con, String result, String info) {

        log.info("Registration " + result + " for " + username + " .Notifying.");
        con.writeMsg(new WriteReply(username, result, info).toData());
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
