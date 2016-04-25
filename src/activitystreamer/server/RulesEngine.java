package activitystreamer.server;

import activitystreamer.messages.*;
import activitystreamer.util.Settings;
import java.util.*;

import org.apache.logging.log4j.Logger;

public class RulesEngine {

    private Logger log;

    public RulesEngine(Logger log) {
        this.log = log;
    }

    public boolean triggerResponse(JsonMessage msg, Connection con) {
        // If message factory returned null, means message was invalid
        if (msg == null) {
            return triggerInvalidMessage(con, InvalidMessage.invalidMessageTypeError);
        }

        // Process accordingly
        switch(msg.getCommand()){

            case "AUTHENTICATE" :

                return triggerAuthenticateAttempt((Authenticate)msg, con);

            case "AUTHENTICATION_FAIL" :

                return triggerAuthenticationFailRead((AuthenticationFail) msg, con);

            case "SERVER_ANNOUNCE" :

                return triggerServerAnnounceRead((ServerAnnounce)msg, con);

            case "LOGIN" :

                return triggerLoginRead((Login)msg, con);

            case "INVALID_MESSAGE" :

                return triggerInvalidMessageRead((InvalidMessage)msg, con);

            default :
                return triggerInvalidMessage(con, InvalidMessage.invalidMessageTypeError);
        }
    }


    /* Return True if the server is to be shut down */
    public boolean triggerServerAnnounceRead(ServerAnnounce msg, Connection con) {
    	
    	ControlSolution server = ControlSolution.getInstance();
    	
    	// Message received from unauthorized Server
    	if(!server.getAuthServers().contains(con)){
    		return triggerInvalidMessage(con, InvalidMessage.unauthorisedServerError);
    	}
    	
        // Broadcast received message to fellow connections
        for(Connection c : server.getAuthServers()){
        	
        	// Don't send to the received connection
        	if(!c.equals(con)) c.writeMsg(msg.toData());
        }
        
        // Update Servers load in Map
        if(server.getServerLoads().containsKey(con)){
        	server.getServerLoads().replace(con, msg);
        }
        else{
        	server.getServerLoads().put(con, msg);
        }
        
        return false;
    }

    public boolean triggerAuthenticateAttempt(Authenticate msg, Connection con) {

        // Check if secret is valid
        if(!secretMatch(msg.getSecret())){

            // If secret is invalid, send authentication fail message

            String info = AuthenticationFail.invalidSecretTypeError + msg.getSecret();
            JsonMessage response = new AuthenticationFail(info);
            con.writeMsg(response.toData());

            // Close the connection
            return true;
        } else if (ControlSolution.getInstance().getAuthServers().contains(con)) {

            // If this connection has already authorized, send invalid message
            ControlSolution.getInstance().getUnauthConnections().add(con);
            return triggerInvalidMessage(con, InvalidMessage.alreadyAuthenticatedError);

        } else {

            // Add to authorized list
            ControlSolution.getInstance().getAuthServers().add(con);

            // Otherwise, do not close the connection
            return false;
        }
    }


    /* Logs the information and returns true to indicate the connection will be closed */
    public boolean triggerAuthenticationFailRead(AuthenticationFail msg, Connection con) {

        // Display information on failed authentication

        log.info("command : " + msg.getCommand());
        log.info("info : " + msg.getInfo());

        // Remove from Authorized list, add to Unauthorized list
        ControlSolution.getInstance().getAuthServers().remove(con);
        ControlSolution.getInstance().getServerLoads().remove(con);
        ControlSolution.getInstance().getUnauthConnections().add(con);

        return true;

    }

    public boolean triggerLoginRead(Login msg, Connection con) {

        if (isClient(msg) && isCorrectClientSecret(msg)) {

            if (!ControlSolution.getInstance().getAuthClients().contains(con)) {
                ControlSolution.getInstance().getAuthClients().add(con);
            }
            ControlSolution.getInstance().getUnauthConnections().remove(con);
            return false;
        }

        return triggerLoginFailed(msg, con);
    }

    public boolean triggerLoginFailed(Login msg, Connection con) {

        String info;

        if (!isClient(msg)) {
            info = LoginFailed.noMatchingUsernameError;
        } else if (!isCorrectClientSecret(msg)) {
            info = LoginFailed.incorrectSecretError;
        } else {
            info = LoginFailed.genericLoginFailedError;
        }

        log.info(info);
        JsonMessage response = new LoginFailed(info);
        con.writeMsg(response.toData());
        return true;

    }

    public boolean triggerInvalidMessageRead(InvalidMessage msg, Connection con) {
        return true;
    }

    public boolean triggerInvalidMessage(Connection con, String info) {

        log.info(info);
        JsonMessage response = new InvalidMessage(info);
        con.writeMsg(response.toData());

        return true;
    }


    /* --- Helper Methods --- */

    private boolean secretMatch(String secret) {
        return secret.equals(Settings.getSecret());
    }

    // Checks whether the username exists in the db
    private boolean isClient(Login msg) {

        HashMap<String, String> clientDB = ControlSolution.getInstance().getClientDB();

        if (msg.isAnonymous()) {
            return true;
        } else if (clientDB.containsKey(msg.getUsername())){
            return true;
        }
        return false;
    }

    private boolean isCorrectClientSecret(Login msg) {

        HashMap<String, String> clientDB = ControlSolution.getInstance().getClientDB();
        if (msg.isAnonymous()) {
            return true;
        } else if (clientDB.containsKey(msg.getUsername())){

            if (clientDB.get(msg.getUsername()).equals(msg.getSecret())) {
                return true;
            }
        }
        return false;

    }

}