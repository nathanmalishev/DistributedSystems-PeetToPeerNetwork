package activitystreamer.client;

import activitystreamer.messages.*;
import activitystreamer.client.Connection;
import activitystreamer.util.Settings;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

/**
 * Created by Jeames on 24/04/2016.
 */
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

            case "LOGIN_FAILED" :
                return triggerLoginFailedRead((LoginFailed)msg, con);
                
            case "LOGIN_SUCCESS" :
            	return triggerLoginSuccess((LoginSuccess)msg, con);

            case "INVALID_MESSAGE" :
                return triggerInvalidMessageRead((InvalidMessage)msg, con);
                
            case "REDIRECT" :
            	return triggerRedirectMessage((Redirect)msg, con);

            case "REGISTER_SUCCESS" :
                return triggerRegisterSuccess((RegisterSuccess)msg, con);

            default :
                return triggerInvalidMessage(con, InvalidMessage.invalidMessageTypeError);
        }
    }
    
    /* Always ensures connection is closed */
    public boolean triggerRedirectMessage(Redirect msg, Connection con){
    	// Simply close the connection
    	log.info("Being Redirected to, Hostname: " + msg.getHostname() + " Port: " + msg.getPort());
        ClientSolution.getInstance().resetServer(msg.getHostname(), msg.getPort());
        ClientSolution.getInstance().setRedirect(true);
    	return true;
    }
    
    /* Always ensures connection is held open */
    public boolean triggerLoginSuccess(LoginSuccess msg, Connection con){
    	
    	log.info(msg.getInfo());
    	return false;
    }

    public boolean triggerRegisterSuccess(RegisterSuccess msg, Connection con) {
        log.info(msg.getInfo());

        // Once registration has succeeded, attempt to login
        Login loginMsg = new Login(Settings.getUsername(), Settings.getSecret());
        con.writeMsg(loginMsg.toData());

        return false;
    }

    public boolean triggerLoginFailedRead(LoginFailed msg, Connection con) {

        log.info("Login Failed: " + msg.getInfo());
        return true;

    }

    public boolean triggerInvalidMessageRead(InvalidMessage msg, Connection con) {

        log.info("Invalid Message: " + msg.getInfo());
        return true;
    }

    public boolean triggerInvalidMessage(Connection con, String info) {

        log.info(info);
        JsonMessage response = new InvalidMessage(info);
        con.writeMsg(response.toData());
        log.info("Closing connection");
        return true;
    }

    public boolean triggerLogin(Connection con) {

        Login loginMsg = new Login(Settings.getUsername(), Settings.getSecret());
        log.info("Logging in with: " + Settings.getUsername() + " " + Settings.getSecret());
        con.writeMsg(loginMsg.toData());
        return false;
    }

    public boolean triggerRegister(Connection con) {

        Register registerMsg = new Register(Settings.getUsername(), Settings.getSecret());
        log.info("Registering with secret: " + Settings.getSecret());
        con.writeMsg(registerMsg.toData());
        return false;
    }

}
