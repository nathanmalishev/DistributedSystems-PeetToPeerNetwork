package activitystreamer.server;

import activitystreamer.messages.*;
import activitystreamer.util.Helper;
import activitystreamer.util.Settings;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import org.json.simple.JSONObject;
import org.apache.logging.log4j.Logger;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

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

            case "KEY_REGISTER_RESPONSE" :
            	return triggerKeyRegisterResponse((KeyRegisterResponse) msg, con);

            case "GET_KEY_FAILED":
                return triggerGetKeyFailed((GetKeyFailed)msg, con);

            case "GET_KEY_SUCCESS":
                return triggerGetKeySuccess((GetKeySuccess) msg, con);

            default :
                return triggerInvalidMessage(con, InvalidMessage.invalidMessageTypeError);
        }
    }

    //TODO: delete prints & tidy? Refactor
    public boolean triggerGetKeySuccess(GetKeySuccess msg, Connection con){
        System.out.println("Get key success");
        System.out.println("The key we got back was "+msg.getServerKey());
        BASE64Decoder decoder = new BASE64Decoder();
        try{
            byte decoded[] = decoder.decodeBuffer(msg.getServerKey());
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFact = KeyFactory.getInstance("RSA");
            PublicKey pubKey2 = keyFact.generatePublic(x509KeySpec);

            System.out.println("After unstringing "+pubKey2);

            //Use public key to create secret key
            KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
            SecretKey secretKey = keyGenerator.generateKey();

            //turn secret key to bytes
            byte secretKeyByte[] = secretKey.getEncoded();

            //encrypt secret key with public key
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey2);
            byte secretKeyEncrypted[] = cipher.doFinal(secretKeyByte);

            //send secret key to the specific server
           System.out.println(msg.getServerId());


            ControlSolution.getInstance().getSecureServerHash().put(msg.getServerId(), secretKey);
        }catch(Exception e){
            System.out.println("err "+e);
        }

        return false;
    }

    public boolean triggerGetKeyFailed(GetKeyFailed msg, Connection con){
        System.out.println("Get key failed");
        return false;
    }

    public boolean triggerKeyRegisterResponse(KeyRegisterResponse msg, Connection con){
    	
    	log.info("Response from KeyRegister: " + msg.getResult());
    	
    	return false;
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
        currentVersion(msg);
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
        currentVersion(msg);
        log.info("Login Attempt Received: " + msg.getUsername());

        triggerDBRead(msg.getUsername(), msg.getSecret(), con);

        return false;
    }


    public boolean triggerDBRead(String username, String secret, Connection con) {

        Connection dbCon = ControlSolution.getInstance().map(username);
        if (dbCon== null) {
            con.writeMsg(new InvalidMessage(InvalidMessage.invalidUsernameError).toData());
            return true;
        }
        ControlSolution.getInstance().getLoginWaiting().put(username, con);
        dbCon.writeMsg(new ReadRequest(username, secret).toData());

        return false;

    }

    public boolean triggerDBReadReply(ReadReply msg) {

        String username = msg.getUsername();
        Connection replyCon = ControlSolution.getInstance().getLoginWaiting().get(username);
        ControlSolution.getInstance().getLoginWaiting().remove(username);

        if (msg.passed()) {
            triggerLoginSuccess(username, replyCon);

        } else {
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
    		if((currentInstance.getAuthClients().size() - load) >= 2){
    			
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

        log.info("Sending Invalid Message Response from server: " + info);
        JsonMessage response = new InvalidMessage(info);
        con.writeMsg(response.toData());

        return true;
    }

    public boolean triggerDBWrite(String username, String secret, Connection con) {
        Connection dbCon = ControlSolution.getInstance().map(username);
        if (dbCon== null) {
            con.writeMsg(new InvalidMessage(InvalidMessage.invalidUsernameError).toData());
            return true;
        }
        ControlSolution.getInstance().getRegisterWaiting().put(username, con);
        dbCon.writeMsg(new WriteRequest(username, secret).toData());

        return false;
    }


    public boolean triggerDBWriteReply(WriteReply msg) {
        String username = msg.getUsername();

        Connection replyCon = ControlSolution.getInstance().getRegisterWaiting().get(username);
        ControlSolution.getInstance().getRegisterWaiting().remove(username);

        if (ControlSolution.getInstance().getLockRequestWaiting().get(username)!=null) {
            // Responding to an old server

            ControlSolution.getInstance().getLockRequestWaiting().remove(username);
            if (msg.passed()) {
                triggerLockAllowed(msg.getUsername(), msg.getSecret(), replyCon);
            } else {
                triggerLockDenied(msg.getUsername(), msg.getSecret(), replyCon);
            }

        } else {
            // Responding directly to client

            if (msg.passed()) {
                replyCon.writeMsg(new RegisterSuccess(username).toData());
            } else {
                triggerRegisterFailed(username, replyCon);
            }

        }

        return false;
    }


    public boolean triggerRegisterFailed(String username, Connection con) {
        con.writeMsg(new RegisterFailed(username).toData());
        con.closeCon();
        ControlSolution.getInstance().connectionClosed(con);
        return false;
    }

    /**
     * Processes incoming Register request. Closes the connection if invalid.
     */
    public boolean triggerRegisterRead(Register msg, Connection con) {

        if (ControlSolution.getInstance().getRegisterWaiting().containsKey(msg.getUsername())) {
            return triggerRegisterFailed(msg.getUsername(), con);
        }
        else {
            return triggerDBWrite(msg.getUsername(), msg.getSecret(), con);
        }

    }


    public boolean triggerLockDenied(String username, String secret, Connection con) {

        LockDenied msg = new LockDenied(username, secret);
        con.writeMsg(msg.toData());
        return false;
    }

    public boolean triggerLockAllowed(String username, String secret, Connection con) {

        LockAllowed msg = new LockAllowed(username, secret);
        con.writeMsg(msg.toData());
        return false;
    }

    /**
     * Processes Incoming Lock_Request, has come from an old server
     */
    public boolean triggerLockRequestRead(LockRequest msg, Connection con) {

        if (ControlSolution.getInstance().getRegisterWaiting().containsKey(msg.getUsername())) {
            return triggerLockDenied(msg.getUsername(), msg.getSecret(), con);
        }
        else {
            ControlSolution.getInstance().getLockRequestWaiting().put(msg.getUsername(), con);
            return triggerDBWrite(msg.getUsername(), msg.getSecret(), con);
        }
    }
    




    /* --- Helper Methods --- */

    private boolean secretMatch(String secret) {
        return secret.equals(Settings.getSecret());
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

    private boolean currentVersion(JsonMessage msg) {

        if (msg.getVersion() == 2) {
            return true;
        }

        return false;

    }

}