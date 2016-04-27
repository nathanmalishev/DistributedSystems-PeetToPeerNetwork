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

                return triggerServerAnnounceRead((ServerAnnounce) msg, con);

            case "LOGIN" :

                return triggerLoginRead((Login) msg, con);

            case "LOGOUT":

                return triggerLogout(con);

            case "INVALID_MESSAGE" :

                return triggerInvalidMessageRead((InvalidMessage) msg, con);

            case "REGISTER" :
                return triggerRegisterRead((Register) msg, con);

            case "LOCK_REQUEST" :
                return triggerLockRequestRead((LockRequest) msg, con);

            case "LOCK_DENIED" :
                return triggerLockDeniedRead((LockDenied)msg, con);

            case "LOCK_ALLOWED" :
                return triggerLockAllowedRead((LockAllowed)msg, con);

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
        server.getServerLoads().put(con, msg);
        
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
            	ControlSolution.getInstance().getUnauthConnections().remove(con);
            
            	// Send Login Success Message
            	String info = LoginSuccess.loginSuccess + msg.getUsername();
            	LoginSuccess response = new LoginSuccess(info);
            	con.writeMsg(response.toData());
            
            	// Determine if we need to redirect
            	return triggerRedirect(con);
            }
        }

        return triggerLoginFailed(msg, con);
    }
    
    /* Sends REDIRECT message if there is a server with a load with 2 or more less than
     * the current server.
     * Returns true if connection is to be closed.
     */
    public boolean triggerRedirect(Connection con){
    	
    	ControlSolution currentInstance = ControlSolution.getInstance();
    	
    	// Check all Server Loads
    	for(Map.Entry<Connection, ServerAnnounce> server : currentInstance.getServerLoads().entrySet()){
    		
    		// Close connection of load load difference > 2
    		int load = server.getValue().getLoad();
    		if((currentInstance.getAuthClients().size() - load) >= 2){
    			
    			// Send Redirect Message
    			Redirect response = new Redirect(server.getValue().getHostname(), server.getValue().getPort());
    			con.writeMsg(response.toData());
    			
    			// Remove from Authorised list and disconnect
    			currentInstance.getAuthClients().remove(con);
    			return true;
    		}
    		
    	}
    	return false;
    }

    public boolean triggerLogout(Connection con){
        con.closeCon();
        return true;
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

    public boolean triggerRegisterRead(Register msg, Connection con) {
        ControlSolution server = ControlSolution.getInstance();

        // Check if already logged in on this connection.

        // Check if already registered.
        if (server.getClientDB().containsKey(msg.getUsername())) {
            log.info(msg.getUsername() + " already know.");
            con.writeMsg(new RegisterFailed(msg.getUsername()).toData());
            return true;
        }

        // Get known servers.
        ArrayList<Connection> knownServers = server.getAuthServers();

        // If no other servers, register the user.
        if (knownServers.size() == 0) {
            con.writeMsg(new RegisterSuccess(msg.getUsername()).toData());
            server.addUser(msg.getUsername(), msg.getSecret());
            return false;
        }

        // Setup Set of servers we are waiting to reply.
        server.addLockRequest(msg.getUsername()+msg.getSecret(), new HashSet<>(knownServers));
        server.addConnectionForLock(msg.getUsername() + msg.getSecret(), con);

        // Add to list on unauth clients.
        server.addUnauthClient(con);

        // Send lock request to all servers.
        for (Connection otherServer : knownServers) {
            otherServer.writeMsg(new LockRequest(msg.getUsername(), msg.getSecret()).toData());
        }

        return false;
    }

    public boolean triggerLockRequestRead(LockRequest msg, Connection con) {
        ControlSolution server = ControlSolution.getInstance();

        // Get known servers.
        ArrayList<Connection> knownServers = server.getAuthServers();
        // Check already registered.
        if (server.userKnownDifferentSecret(msg.getUsername(), msg.getSecret())) {
            // Broadcast lock denied.
            log.info("sending lock denied from already reg");
            for (Connection otherServer : knownServers)
                otherServer.writeMsg(new LockDenied(msg.getUsername(), msg.getSecret()).toData());
            return false;
        }

        // Check if only other know server is that which sent message.
        if (knownServers.size() == 1) {
            con.writeMsg(new LockAllowed(msg.getUsername(), msg.getSecret()).toData());

            // Register user
            server.addUser(msg.getUsername(), msg.getSecret());

            return false;
        }

        // Send LR to all but original.
        HashSet<Connection> knownSet = new HashSet<>(knownServers);
        knownSet.remove(con);
        server.addLockRequest(msg.getUsername() + msg.getSecret(), knownSet);
        server.addConnectionForLock(msg.getUsername() + msg.getSecret(), con);
        for (Connection otherServer : knownSet) {
            if (otherServer != con) {
                otherServer.writeMsg(new LockRequest(msg.getUsername(), msg.getSecret()).toData());
            }
        }

        return false;
    }

    public boolean triggerLockAllowedRead(LockAllowed msg, Connection con) {
        ControlSolution server = ControlSolution.getInstance();

        // Get servers we are waiting for.
        HashSet<Connection> waiting = server.getLockRequest(msg.getUsername() + msg.getSecret());

        // Remove received connection.
        waiting.remove(con);

        // Check if no longer waiting.
        if (waiting.size() == 0) {
            if (server.containsConnectionForLock(msg.getUsername()+msg.getSecret())) {
                Connection replyCon = server.getConnectionForLock(msg.getUsername()+msg.getSecret());

                // Remove lock requests waiting.
                server.removeLockRequestsAndConnection(msg.getUsername()+msg.getSecret());

                // If connection is client, send register success.
                if (server.getUnauthClients().contains(replyCon)) {
                    log.info("Successful register for " + msg.getUsername());
                    replyCon.writeMsg(new RegisterSuccess(msg.getUsername()).toData());

                    // Register the user.
                    server.addUser(msg.getUsername(), msg.getSecret());
                    server.removeUnauthClient(replyCon);
                    return false;
                }
                // If connection is server, send lock allowed.
                if (server.getAuthServers().contains(replyCon)) {
                    log.info("Lock allowed for " + msg.getUsername());
                    replyCon.writeMsg(new LockAllowed(msg.getUsername(), msg.getSecret()).toData());
                    
                    // Register the user.
                    server.addUser(msg.getUsername(), msg.getSecret());

                    return false;
                }
                // Otherwise, send failure.
                triggerInvalidMessage(con, InvalidMessage.invalidMessageTypeError);
                return true;

            }
        }

        return false;
    }

    public boolean triggerLockDeniedRead(LockDenied msg, Connection con) {
        ControlSolution server = ControlSolution.getInstance();

        // Remove from storage.
        if (server.hasUser(msg.getUsername(), msg.getSecret())) {
            server.removeUser(msg.getUsername());
        }

        // Remove lock requests waiting.
        server.removeLockRequestsAndConnection(msg.getUsername()+msg.getSecret());

        // Propagate lock denied.
        ArrayList<Connection> knownServers = server.getAuthServers();
        for (Connection otherServer : knownServers) {
            log.info("sending lock denied");
            if (otherServer != con) otherServer.writeMsg(new LockDenied(msg.getUsername(), msg.getSecret()).toData());
        }

        // If connected to client, send failure.
        if (server.containsConnectionForLock(msg.getUsername() + msg.getSecret())) {
            server.getConnectionForLock(msg.getUsername() + msg.getSecret()).writeMsg(new RegisterFailed(msg.getUsername()).toData());
            return false;
        }

        return false;
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