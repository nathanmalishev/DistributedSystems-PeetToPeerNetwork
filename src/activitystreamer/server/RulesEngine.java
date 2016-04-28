package activitystreamer.server;

import activitystreamer.messages.*;
import activitystreamer.util.Settings;
import java.util.*;
import org.json.simple.JSONObject;
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

                return triggerLogoutRead(con);

            case "ACTIVITY_MESSAGE":
                return triggerActivityMessageRead((ActivityMessage)msg, con);

            case "ACTIVITY_BROADCAST":
                return triggerActivityBroadcastRead((ActivityBroadcast)msg);

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
            return triggerAuthenticationFail(con, info);

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

    public boolean triggerAuthenticationFail(Connection con, String info) {


        JsonMessage response = new AuthenticationFail(info);
        con.writeMsg(response.toData());

        return true;
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

            if (!alreadyLoggedIn(msg.getUsername(), con)) {
            	ControlSolution.getInstance().getUnauthConnections().remove(con);
                login(msg, con);

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

    private void login(Login msg, Connection con) {
        ControlSolution.getInstance().getAuthClients().add(con);
        ControlSolution.getInstance().getUnauthConnections().remove(con);
        ControlSolution.getInstance().getLoggedInUsernames().put(con, msg.getUsername());
    }

    private void logout(Connection con) {
        ControlSolution.getInstance().getLoggedInUsernames().remove(con);
    }

    private boolean alreadyLoggedIn(String username, Connection con) {

        if (ControlSolution.getInstance().getAuthClients().contains(con)) {
            return true;
        }

        if (ControlSolution.getInstance().getLoggedInUsernames().containsValue(username)) {
            return true;
        }
        return false;
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
    		if((currentInstance.getAuthClients().size() - load) > 2){
    			
    			// Send Redirect Message
    			Redirect response = new Redirect(server.getValue().getHostname(), server.getValue().getPort());
    			con.writeMsg(response.toData());
                logout(con);
    			return true;
    		}
    		
    	}
    	return false;
    }

    public boolean triggerLogoutRead(Connection con){
        logout(con);
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

    public boolean triggerActivityMessageRead(ActivityMessage msg, Connection con) {
        ControlSolution server = ControlSolution.getInstance();
        if (!alreadyLoggedIn(msg.getUsername(), con)) {
            return triggerAuthenticationFail(con, ActivityMessage.alreadyAuthenticatedError);
        } else {

            return triggerActivityBroadcast(msg.getActivity());
        }
    }

    public boolean triggerActivityBroadcast(JSONObject activity) {

        ControlSolution server = ControlSolution.getInstance();

        ActivityBroadcast activityBroadcast = new ActivityBroadcast(activity);

        for (Connection connection : server.getConnections()) {
            connection.writeMsg(activityBroadcast.toData());
        }
        return false;

    }

    public boolean triggerActivityBroadcastRead(ActivityBroadcast msg) {

        return triggerActivityBroadcast(msg.getActivity());

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
        String msgUsername = msg.getUsername();
        String msgSecret = msg.getSecret();

        // Check if already logged in on this connection.
        if (alreadyLoggedIn(msgUsername, con)) {
            log.info("User attempting to register while logged in.");
            con.writeMsg(new InvalidMessage(InvalidMessage.alreadyLoggedInError).toData());
            return true;
        }

        // Check if already registered.
        if (server.getClientDB().containsKey(msgUsername)) {
            log.info(msgUsername + " already known.");
            con.writeMsg(new RegisterFailed(msgUsername).toData());
            return true;
        }

        // Get known servers.
        ArrayList<Connection> knownServers = server.getAuthServers();

        // If no other servers, register the user.
        if (knownServers.size() == 0) {
            log.info("Registering user " + msgUsername);
            con.writeMsg(new RegisterSuccess(msgUsername).toData());
            server.addUser(msgUsername, msgSecret);
            return false;
        }

        // Setup Set of servers we are waiting to reply.
        server.addLockRequest(msgUsername, new HashSet<>(knownServers));
        server.addConnectionForLock(msgUsername, con);

        // Add to list on unauth clients.
        server.addUnauthClient(con);

        // Send lock request to all servers.
        log.info("Sending lock requests to all known servers regarding " + msgUsername);
        for (Connection otherServer : knownServers) {
            otherServer.writeMsg(new LockRequest(msgUsername, msgSecret).toData());
        }

        return false;
    }

    public boolean triggerLockRequestRead(LockRequest msg, Connection con) {
        ControlSolution server = ControlSolution.getInstance();
        String msgUsername = msg.getUsername();
        String msgSecret = msg.getSecret();

        // Get known servers.
        ArrayList<Connection> knownServers = server.getAuthServers();

        // Check already registered (same or different name), or already registering
        if (server.userKnown(msgUsername) || server.hasLockRequest(msgUsername)) {
            // Broadcast lock denied.
            log.info(msgUsername + " already known. Sending lock denied.");
            for (Connection otherServer : knownServers)
                otherServer.writeMsg(new LockDenied(msgUsername, msgSecret).toData());
            return false;
        }

        // Check if only other know server is that which sent message.
        if (knownServers.size() == 1) {
            // Register user
            log.info("Registering user " + msgUsername);
            con.writeMsg(new LockAllowed(msgUsername, msgSecret).toData());
            server.addUser(msgUsername, msgSecret);
            return false;
        }

        // Send LR to all but original.
        HashSet<Connection> knownSet = new HashSet<>(knownServers);
        knownSet.remove(con);
        server.addLockRequest(msgUsername, knownSet);
        server.addConnectionForLock(msgUsername, con);
        log.info("Sending lock requests to all known servers regarding " + msgUsername);
        for (Connection otherServer : knownSet) {
            if (otherServer != con)
                otherServer.writeMsg(new LockRequest(msgUsername, msgSecret).toData());
        }

        return false;
    }

    public boolean triggerLockAllowedRead(LockAllowed msg, Connection con) {
        ControlSolution server = ControlSolution.getInstance();

        // Get servers we are waiting for.
        String msgUsername = msg.getUsername();
        String msgSecret = msg.getSecret();
        HashSet<Connection> waiting = server.getLockRequest(msgUsername);

        // Remove received connection.
        waiting.remove(con);

        log.info("Lock allowed received regarding " + msgUsername + ". Waiting for " + waiting.size() + " more.");

        // Check if no longer waiting.
        if (waiting.size() == 0) {
            if (server.hasConnectionForLock(msgUsername)) {
                Connection replyCon = server.getConnectionForLock(msgUsername);

                // Remove lock requests waiting.
                server.removeLockRequestsAndConnection(msgUsername);

                // If connection is client, send register success.
                if (server.getUnauthClients().contains(replyCon)) {
                    log.info("Registering " + msgUsername);
                    replyCon.writeMsg(new RegisterSuccess(msgUsername).toData());

                    // Register the user.
                    server.addUser(msgUsername, msgSecret);
                    server.removeUnauthClient(replyCon);
                    return false;
                }

                // If connection is server, send lock allowed.
                if (server.getAuthServers().contains(replyCon)) {
                    log.info("Lock allowed for " + msgUsername);
                    replyCon.writeMsg(new LockAllowed(msgUsername, msgSecret).toData());
                    
                    // Register the user.
                    server.addUser(msgUsername, msgSecret);

                    return false;
                }
                // Otherwise, send failure.
                triggerInvalidMessage(con, InvalidMessage.userAlreadyRegistered);
                return true;
            }
        }

        return false;
    }

    public boolean triggerLockDeniedRead(LockDenied msg, Connection con) {
        ControlSolution server = ControlSolution.getInstance();

        String msgUsername = msg.getUsername();
        String msgSecret = msg.getSecret();

        log.info("Lock denied received " + msgUsername);

        // Remove from storage.
        if (server.userKnownSameSecret(msgUsername, msgSecret)) {
            server.removeUser(msgUsername);
        }

        // Remove lock requests waiting.
        Connection replyCon = null;
        if (server.hasConnectionForLock(msgUsername))
            replyCon = server.getConnectionForLock(msgUsername);
        server.removeLockRequestsAndConnection(msgUsername);

        // Propagate lock denied.
        ArrayList<Connection> knownServers = server.getAuthServers();
        for (Connection otherServer : knownServers) {
            log.info("Sending lock denied regarding " + msgUsername);
            if (otherServer != con) otherServer.writeMsg(new LockDenied(msgUsername, msgSecret).toData());
        }

        // If connected to client, send failure.
        if (replyCon != null && server.getUnauthClients().contains(replyCon)) {
            log.info("Registration failed for " + msgUsername + " .Notifying.");
            replyCon.writeMsg(new RegisterFailed(msgUsername).toData());
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