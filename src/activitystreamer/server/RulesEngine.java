package activitystreamer.server;

import activitystreamer.messages.*;
import activitystreamer.util.Settings;
import java.util.*;
import org.json.simple.JSONObject;
import org.apache.logging.log4j.Logger;

/**
 * Class controls the processing of incoming messages from the server side
 * and determines what action to perform according to the type of message. 
 */
public class RulesEngine {

    private Logger log;

    public RulesEngine(Logger log) {
        this.log = log;
    }

    /**
     * Decides what kind of action to take depending on the type of 
     * message which is passed in.
     * 
     * @param msg	Incoming Message
     * @param con	Connection which sent the message
     * @return		True if connection is to be closed, false otherwise
     */
    public boolean triggerResponse(JsonMessage msg, Connection con) {
       System.out.println("Receiving something: " + msg);

    	// If message factory returned null, means message was invalid
        if (msg == null) {
            return triggerInvalidMessage(con, InvalidMessage.invalidMessageTypeError);
        }
        System.out.println(msg.getCommand());
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

            case "READ_REPLY":
                return triggerDBReadReply((ReadReply) msg);

            case "WRITE_REPLY":
                return triggerDBWriteReply((WriteReply) msg);

            case "ACTIVITY_MESSAGE":
                return triggerActivityMessageRead((ActivityMessage)msg, con);

            case "ACTIVITY_BROADCAST":
                return triggerActivityBroadcastRead((ActivityBroadcast)msg, con);

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


    /**
     *  Return True if the server is to be shut down 
     */
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

    /**
     * Processing the incoming Authentication attempt, and adds the server to 
     * the list of authorised connections if valid.
     */
    public boolean triggerAuthenticateAttempt(Authenticate msg, Connection con) {

        log.info("Authentication request received with secret: " + msg.getSecret());

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
            log.info("Authentication Successful");
            // Otherwise, do not close the connection
            return false;
        }
    }

    /**
     * Sends an Authentication Fail to the connection and closes the connection.
     */
    public boolean triggerAuthenticationFail(Connection con, String info) {
        log.info("Failing Authentication Request: " + info);

        JsonMessage response = new AuthenticationFail(info);
        con.writeMsg(response.toData());

        return true;
    }

    /**
     *  Logs the information and returns true to indicate the connection will be closed 
     */
    public boolean triggerAuthenticationFailRead(AuthenticationFail msg, Connection con) {
        
    	 // Display information on failed authentication
    	log.info("Authentication Request Failed: " + msg.getInfo());
        log.info("command : " + msg.getCommand());
        log.info("info : " + msg.getInfo());

        // Remove from Authorized list, add to Unauthorized list
        ControlSolution.getInstance().getAuthServers().remove(con);
        ControlSolution.getInstance().getServerLoads().remove(con);
        ControlSolution.getInstance().getUnauthConnections().add(con);

        return true;

    }
    
    /**
     * Processes the incoming Login attempt, and adds the user to the logged in list
     * if valid.
     */
    public boolean triggerLoginRead(Login msg, Connection con) {

        log.info("Login Attempt Received: " + msg.getUsername());

        triggerDBRead(msg.getUsername(), msg.getSecret(), con);

        /*if (isClient(msg.getUsername()) && isCorrectClientSecret(msg)) {

            if (!alreadyLoggedIn(msg.getUsername(), con)) {
            	ControlSolution.getInstance().getUnauthConnections().remove(con);

            	// Send Login Success Message
                return triggerLoginSuccess(msg.getUsername(), con);

            }
        }

        return triggerLoginFailed(msg, con);*/
        return false;
    }


    public boolean triggerDBRead(String username, String secret, Connection con) {

        System.out.println("triggering db read");
        Connection dbCon = ControlSolution.getInstance().map(username);
        if (dbCon== null) {
            con.writeMsg(new InvalidMessage(InvalidMessage.invalidUsernameError).toData());
            return true;
        }
        System.out.println("writing to con db");
        ControlSolution.getInstance().getLoginWaiting().put(username, con);
        dbCon.writeMsg(new ReadRequest(username, secret).toData());

        return false;

    }

    public boolean triggerDBReadReply(ReadReply msg) {

        String username = msg.getUsername();
        System.out.println("reading login reply!");
        Connection replyCon = ControlSolution.getInstance().getLoginWaiting().get(username);
        ControlSolution.getInstance().getLoginWaiting().remove(username);

        if (msg.passed()) {
            System.out.println("login succcess");
            triggerLoginSuccess(username, replyCon);

        } else {
            System.out.println("login failed :( ");
            triggerLoginFailed(username, msg.getInfo(), replyCon);
        }
        return false;

    }

    public boolean triggerLoginSuccess(String username, Connection con) {
        login(username, con);
        log.info("Login Success: " + username);
        con.writeMsg((new LoginSuccess(LoginSuccess.loginSuccess + username)).toData());

        // Determine if we need to redirect
        return triggerRedirect(con);
    }


    /** Sends REDIRECT message if there is a server with a load with 2 or more less than
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
                log.info("Redirecting client to: " + response.getHostname());

                con.writeMsg(response.toData());
                logout(con);
    			con.closeCon();
                ControlSolution.getInstance().connectionClosed(con);
    		}
    		
    	}
    	return false;
    }
    
    /**
     * Closes the connection.
     */
    public boolean triggerLogoutRead(Connection con){
        log.info("Received Logout");
        logout(con);
        con.closeCon();
        return true;
    }

    /**
     * Writes login_failed message to the connection and closes the connection after
     */
    public boolean triggerLoginFailed(String username, String info, Connection con) {

        log.info("Login Failed: " + info);
        JsonMessage response = new LoginFailed(info);
        con.writeMsg(response.toData());
        con.closeCon();
        ControlSolution.getInstance().connectionClosed(con);
        return true;
    }
    
    /**
     * Processes incoming activity message and broadcasts to all other connections
     */
    public boolean triggerActivityMessageRead(ActivityMessage msg, Connection con) {
        
    	ControlSolution server = ControlSolution.getInstance();
    	
        if (!alreadyLoggedIn(msg.getUsername(), con)) {
            return triggerAuthenticationFail(con, ActivityMessage.alreadyAuthenticatedError);
        } else {
            // Add user field, broadcast.
            log.info("Activity Message Received From: " + msg.getUsername());

            JSONObject msgActivity = msg.getActivity();
            msgActivity.put("authenticated_user", msg.getUsername());

            // Resend the message back to the original client so they display it
            ActivityBroadcast activityBroadcast = new ActivityBroadcast(msgActivity);
            con.writeMsg(activityBroadcast.toData());

            return triggerActivityBroadcast(msgActivity, con);
        }
    }
    
    /**
     * Processes incoming Activity_Broadcast message and broadcasts it to all other 
     * connections it knows about.
     */
    public boolean triggerActivityBroadcast(JSONObject activity, Connection con) {
        
    	log.info("Broadcasting Activity Message: " + activity);
        ControlSolution server = ControlSolution.getInstance();

        ActivityBroadcast activityBroadcast = new ActivityBroadcast(activity);
        
        // Send to every connection, but the one you received from
        for (Connection connection : server.getConnections()) {
            if (!connection.equals(con)) {
                connection.writeMsg(activityBroadcast.toData());
            }
        }
        return false;

    }
    
    /**
     * Checks the incoming Activity_Broadcast was sent from an Authorized Server
     * and proceeds to broadcast
     */
    public boolean triggerActivityBroadcastRead(ActivityBroadcast msg, Connection con) {
        
    	ControlSolution server = ControlSolution.getInstance();
    	
        // Check if connection authenticated.
        if (!server.getAuthServers().contains(con))
            return triggerInvalidMessage(con, InvalidMessage.unauthorisedServerError);

        return triggerActivityBroadcast(msg.getActivity(), con);

    }

    /**
     * Closes the connection
     */
    public boolean triggerInvalidMessageRead(InvalidMessage msg, Connection con) {
        log.info("Received Invalid Message Response");
        return true;
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

    public boolean triggerDBWrite(Register msg, Connection con) {
        System.out.println("triggering db write");
        Connection dbCon = ControlSolution.getInstance().map(msg.getUsername());
        if (dbCon== null) {
            con.writeMsg(new InvalidMessage(InvalidMessage.invalidUsernameError).toData());
            return true;
        }
        System.out.println("writing to con db");
        ControlSolution.getInstance().getRegisterWaiting().put(msg.getUsername(), con);
        dbCon.writeMsg(new WriteRequest(msg.getUsername(), msg.getSecret()).toData());

        return false;
    }


    public boolean triggerDBWriteReply(WriteReply msg) {
        System.out.println("got a reply!");
        String username = msg.getUsername();

        Connection replyCon = ControlSolution.getInstance().getRegisterWaiting().get(username);
        ControlSolution.getInstance().getRegisterWaiting().remove(username);

        if (msg.passed()) {
            System.out.println("it passed!");
            replyCon.writeMsg(new RegisterSuccess(username).toData());
        } else {
            System.out.println("it didnt pass:*");
            replyCon.writeMsg(new RegisterFailed(username).toData());
            replyCon.closeCon();
            ControlSolution.getInstance().connectionClosed(replyCon);
        }
        return false;
    }


    /**
     * Processes incoming Register request. Closes the connection if invalid.
     */
    public boolean triggerRegisterRead(Register msg, Connection con) {


        return triggerDBWrite(msg, con);

    	/*ControlSolution server = ControlSolution.getInstance();
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
        }*/

    }
    
    /**
     * Processes Incoming Lock_Request
     */
    public boolean triggerLockRequestRead(LockRequest msg, Connection con) {
        
    	ControlSolution server = ControlSolution.getInstance();
        String msgUsername = msg.getUsername();
        String msgSecret = msg.getSecret();

        // Get known servers.
        ArrayList<Connection> knownServers = server.getAuthServers();

        // Check if server authenticated.
        if (!knownServers.contains(con))
            return triggerInvalidMessage(con, InvalidMessage.unauthorisedServerError);

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
    
    /**
     * Process incoming Lock_Allowed message
     */
    public boolean triggerLockAllowedRead(LockAllowed msg, Connection con) {
        ControlSolution server = ControlSolution.getInstance();

        // Check if server authenticated.
        if (!server.getAuthServers().contains(con))
            return triggerInvalidMessage(con, InvalidMessage.unauthorisedServerError);

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
    
    /**
     * Process incoming Lock_denied message
     */
    public boolean triggerLockDeniedRead(LockDenied msg, Connection con) {
        ControlSolution server = ControlSolution.getInstance();

        // Check if server authenticated.
        if (!server.getAuthServers().contains(con))
            return triggerInvalidMessage(con, InvalidMessage.unauthorisedServerError);

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
    private boolean isClient(String username) {

        HashMap<String, String> clientDB = ControlSolution.getInstance().getClientDB();

        if (username == "anonymous") {
            return true;
        } else if (clientDB.containsKey(username)){
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


    /**
     * Controls the handling of adding the user to the required lists
     */
    private void login(String username, Connection con) {
        ControlSolution.getInstance().getAuthClients().add(con);
        ControlSolution.getInstance().getUnauthConnections().remove(con);
        ControlSolution.getInstance().getLoggedInUsernames().put(con, username);
    }

    /**
     * Removes client from the logged in list
     */
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

}