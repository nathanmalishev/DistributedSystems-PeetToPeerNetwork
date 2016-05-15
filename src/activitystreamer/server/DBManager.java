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
    private static final char[] startBoundaries = {'a', 'h', 'n', 'u'};
    private static final char[] endBoundaries = {'g', 'm', 't', 'z'};
    private static final Logger log = LogManager.getLogger();

    public DBManager() {

    }

    public void initialiseDBConnections() {

        // If any of the hostnames are null, setup the databases
        if (Settings.getShardAHostname() == null || Settings.getShardBHostname() == null || Settings.getShardCHostname() == null || Settings.getShardDHostname() == null) {
            setupDB();
        }

        // Either way, initialise connections with them
        try {
            Connection a = Control.getInstance().outgoingConnection(new Socket(Settings.getShardAHostname(), Settings.getShardAPort()));
            Connection b = Control.getInstance().outgoingConnection(new Socket(Settings.getShardBHostname(), Settings.getShardBPort()));
            Connection c = Control.getInstance().outgoingConnection(new Socket(Settings.getShardCHostname(), Settings.getShardCPort()));
            Connection d = Control.getInstance().outgoingConnection(new Socket(Settings.getShardDHostname(), Settings.getShardDPort()));
        } catch (IOException e) {
            log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
            System.exit(-1);
        }
    }

    // Override to generate all databases on same location as registry server
    public ArrayList<activitystreamer.server.Connection> setupDB() {

        // Needs to set up 4 db chard instances, and return an arraylist of connections to them

        for (int i = 0; i < numShards; i ++) {

            DBShard newDB = new DBShard(startBoundaries[i], endBoundaries[i]);
            newDB.start();
            dbShards.add(newDB);

        }
        return null;
    }
    

}
