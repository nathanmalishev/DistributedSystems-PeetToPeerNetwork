package activitystreamer.server;

/**
 * Created by Jeames on 15/05/2016.
 */

import activitystreamer.database.DBShard;
import java.net.Socket;

import java.util.*;
import java.io.*;
import activitystreamer.util.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBManager {

    private ArrayList<DBShard> dbShards;
    private static final int numShards = 4;
    private static final Logger log = LogManager.getLogger();

    public DBManager() {

    }

    public HashMap<Integer, Connection> initialiseDBConnections(ControlSolution server) {

        // If any of the hostnames are null, setup the databases
        if (Settings.getShardAHostname() == null || Settings.getShardBHostname() == null || Settings.getShardCHostname() == null || Settings.getShardDHostname() == null) {
            setupDB();
        }
        HashMap<Integer, Connection> dbLookup = new HashMap();
        // Either way, initialise connections with them
        try {
            System.out.println("trying outgoing connetions");
            System.out.println("still trying outgoing connetions");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
            Connection a = server.outgoingConnection(new Socket(Settings.getShardAHostname(), Settings.getShardAPort()));
            System.out.println("think one got created");
            Connection b = server.outgoingConnection(new Socket(Settings.getShardBHostname(), Settings.getShardBPort()));

            Connection c = server.outgoingConnection(new Socket(Settings.getShardCHostname(), Settings.getShardCPort()));
            Connection d = server.outgoingConnection(new Socket(Settings.getShardDHostname(), Settings.getShardDPort()));
            dbLookup.put(0, a);
            dbLookup.put(1, b);
            dbLookup.put(2, c);
            dbLookup.put(3, d);
            System.out.println("finished outgoing connections");
        } catch (IOException e) {
            log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
            System.exit(-1);
        }
        return dbLookup;
    }

    // Override to generate all databases on same location as registry server
    // Not sure if this will actually work because need to take at least a port? maybe just have static ports
    public ArrayList<activitystreamer.server.Connection> setupDB() {
        System.out.println("uh oh shouldn't have gotten here yet");
        // Needs to set up 4 db chard instances, and set them in settings

        for (int i = 0; i < numShards; i ++) {

            DBShard newDB = new DBShard(i);
            newDB.start();
            dbShards.add(newDB);

        }
        return null;
    }


}
