package activitystreamer.client;

import activitystreamer.messages.*;
import activitystreamer.client.Connection;
import activitystreamer.util.Helper;
import activitystreamer.util.Settings;

import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.*;
import com.google.gson.stream.MalformedJsonException;

import java.util.HashMap;

/**
 * Class controls the processing of incoming messages from the client side
 *  and determines what action to perform according to the type of message. 
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

            case "LOGIN_FAILED" :
                return triggerLoginFailedRead((LoginFailed)msg, con);
                
            case "LOGIN_SUCCESS" :
            	return triggerLoginSuccess((LoginSuccess)msg, con);

            case "ACTIVITY_BROADCAST" :
                return triggerActivityBroadcast((ActivityBroadcast) msg, con);

            case "INVALID_MESSAGE" :
                return triggerInvalidMessageRead((InvalidMessage)msg, con);
                
            case "REDIRECT" :
            	return triggerRedirectMessage((Redirect)msg, con);

            case "REGISTER_FAILED" :
                return triggerRegisterFailed((RegisterFailed) msg, con);

            case "REGISTER_SUCCESS" :
                return triggerRegisterSuccess((RegisterSuccess) msg, con);
                
            case "GET_KEY_SUCCESS" :
            	return triggerGetKeySuccess((GetKeySuccess) msg, con);
            	
            case "GET_KEY_FAILED" :
            	return triggerGetKeyFailed((GetKeyFailed) msg, con);

            default :
                return triggerInvalidMessage(con, InvalidMessage.invalidMessageTypeError);
        }
    }
    
    public boolean triggerGetKeyMessage(Connection con){
    	
    	String uniqueIdentifier = Helper.createUniqueServerIdentifier(Settings.getRemoteHostname(), Integer.toString(Settings.getRemotePort()));
    	GetKey msg = new GetKey(uniqueIdentifier);
    	
    	con.writeMsg(msg.toData());
    	log.info("GETKEY Sent");
    	return false;
    }
    
    // TODO: Test
    public boolean triggerGetKeySuccess(GetKeySuccess msg, Connection con){
    	
    	ClientSolution.decodePublicKey(msg.getServerKey());
    	
    	return false;
    }
    
    // TODO: Complete
    public boolean triggerGetKeyFailed(GetKeyFailed msg, Connection con){
    	
    	// What do we do when this occurs???
    	
    	
    	return false;
    }
    
    /**
     *  Always ensures connection is closed 
     */
    public boolean triggerRedirectMessage(Redirect msg, Connection con){
    	// Simply close the connection
    	log.info("Being Redirected to, Hostname: " + msg.getHostname() + " Port: " + msg.getPort());
        ClientSolution.getInstance().resetServer(msg.getHostname(), msg.getPort());
        ClientSolution.getInstance().setRedirect(true);
    	return true;
    }
    
    /**
     *  Always ensures connection is held open 
     */
    public boolean triggerLoginSuccess(LoginSuccess msg, Connection con){

    	log.info("Login successful: " + msg.getInfo());
    	return false;
    }
    
    /**
     * Ensures connection is held open and sends a Login Message back to the 
     * connection.
     */
    public boolean triggerRegisterSuccess(RegisterSuccess msg, Connection con) {
        log.info("Register successful: " + msg.getInfo());

        // Once registration has succeeded, attempt to login
        Login loginMsg = new Login(Settings.getUsername(), Settings.getSecret());
        con.writeMsg(loginMsg.toData());

        return false;
    }
    
    /** 
     * Ensures the connection is closed.
     */
    public boolean triggerRegisterFailed(RegisterFailed msg, Connection con) {
        log.info("Register failed: " + msg.getInfo());
        return true;
    }
    
    /**
     * Displays the broadcasted message to the Output text frame on the GUI.
     * Connection is maintained.
     */
    public boolean triggerActivityBroadcast(ActivityBroadcast msg, Connection con) {
        
    	try{
			ClientSolution.getInstance().getTextFrame().setOutputText(msg.getActivity());
		}catch(Exception e){
			log.error(e);
		}
        return false;
    }

    /**
     * Ensures the connection is always closed.
     */
    public boolean triggerLoginFailedRead(LoginFailed msg, Connection con) {
        log.info("Login Failed: " + msg.getInfo());
        return true;

    }

    /**
     * Ensures the connection is always closed.
     */
    public boolean triggerInvalidMessageRead(InvalidMessage msg, Connection con) {
        log.info("Invalid Message Received: " + msg.getInfo());
        return true;
    }

    /**
     * Sends a logout message to the server, and closes the connection.
     */
    public boolean triggerLogout(Connection con) {
        Logout logoutMsg = new Logout(Logout.disconnectLogout);
        log.info("Logging out");
        con.writeMsg(logoutMsg.toData());
        return true;
    }
    
    /**
     * Sends an Invalid_Message to the connection and closes the connection.
     */
    public boolean triggerInvalidMessage(Connection con, String info) {

        JsonMessage response = new InvalidMessage(info);
        con.writeMsg(response.toData());
        log.info("Closing connection due to: " + info);
        return true;
    }

    /**
     * Sends a login message to the connection.
     */
    public boolean triggerLogin(Connection con) {

        Login loginMsg = new Login(Settings.getUsername(), Settings.getSecret());
        log.info("Logging in with: " + Settings.getUsername() + " " + Settings.getSecret());
        con.writeMsg(loginMsg.toData());
        return false;
    }

    /**
     * Sends a Register message to the connection.
     */
    public boolean triggerRegister(Connection con) {

        Register registerMsg = new Register(Settings.getUsername(), Settings.getSecret());
        log.info("Registering with secret: " + Settings.getSecret());
        con.writeMsg(registerMsg.toData());
        return false;
    }

}
