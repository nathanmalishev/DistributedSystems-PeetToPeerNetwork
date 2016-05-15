package activitystreamer.database;

import activitystreamer.server.Listener;
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


    public DBShard(char start, char end) {
        this.start = start;
        this.end = end;
        connections = new ArrayList();
        clientShard = new HashMap();

        // start a listener
        try {
            listener = new Listener();
        } catch (IOException e1) {
            log.fatal("failed to startup a listening thread: "+e1);
            System.exit(-1);
        }
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
        // process request for data
        return false;
    }


}
