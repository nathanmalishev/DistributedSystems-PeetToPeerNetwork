package activitystreamer.messages;

import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.server.ControlSolution;
import activitystreamer.util.Settings;

import org.apache.logging.log4j.Logger;

public class RulesEngine {

    private Logger log;

    public RulesEngine(Logger log) {
        this.log = log;
    }

    public boolean triggerResponse(JsonMessage msg, Connection con) {

        // If message factory returned null, means message was invalid
        if (msg == null) {
            return triggerInvalidMessage(con);
        }

        // Process accordingly
        switch(msg.getCommand()){

            case "AUTHENTICATE" :

                return triggerAuthenticateAttempt((Authenticate)msg, con);

            case "AUTHENTICATION_FAIL" :

                return triggerAuthenticationFailRead((AuthenticationFail) msg, con);

            case "SERVER_ANNOUNCE" :

                return triggerServerAnnounceRead((ServerAnnounce)msg, con);

            case "INVALID_MESSAGE" :
                return triggerInvalidMessageRead((InvalidMessage)msg, con);

            default :
                return triggerInvalidMessage(con);
        }


    }


    /* Return True if the server is to be shut down */
    public boolean triggerServerAnnounceRead(ServerAnnounce msg, Connection con) {

        // ---- DEBUG ----
        log.debug("Command: " + msg.getCommand());
        log.debug("ID: " + msg.getId());
        log.debug("Load: " + msg.getLoad());
        log.debug("Hostname: " + msg.getHostname());
        log.debug("Port: " + msg.getPort());

        return false;
    }

    public boolean triggerAuthenticateAttempt(Authenticate msg, Connection con) {

        // Check if secret is valid
        if(!msg.getSecret().equals(Settings.getSecret())){

            // If secret is invalid, send authentication fail message

            String info = AuthenticationFail.invalidSecretTypeError + msg.getSecret();
            JsonMessage response = new AuthenticationFail(info);
            con.writeMsg(response.toData());
            
            // Add to unauthorized list
            ControlSolution.getInstance().getUnauthServers().add(con);
            
            // Close the connection
            return true;
        }
        
        // Add to authorized list
        ControlSolution.getInstance().getAuthServers().add(con);
        
        // Otherwise, do not close the connection
        return false;
    }


    /* Logs the information and returns true to indicate the connection will be closed */
    public boolean triggerAuthenticationFailRead(AuthenticationFail msg, Connection con) {

        // Display information on failed authentication

        log.info("command : " + msg.getCommand());
        log.info("info : " + msg.getInfo());
        
        // Remove from Authorized list, add to Unauthorized list
        ControlSolution.getInstance().getAuthServers().remove(con);
        ControlSolution.getInstance().getUnauthServers().add(con);

        ControlSolution.getInstance().setTerm(true);

        return true;

    }

    public boolean triggerInvalidMessageRead(InvalidMessage msg, Connection con) {
        return false;
    }

    public boolean triggerInvalidMessage(Connection con) {

        String info = InvalidMessage.invalidMessageTypeError;

        log.info(info);
        JsonMessage response = new InvalidMessage(info);
        con.writeMsg(response.toData());

        return false;
    }

}
