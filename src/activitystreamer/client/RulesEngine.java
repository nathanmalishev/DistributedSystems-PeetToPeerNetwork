package activitystreamer.client;

import activitystreamer.messages.*;
import activitystreamer.server.Connection;
import activitystreamer.server.ControlSolution;
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

            case "INVALID_MESSAGE" :

                return triggerInvalidMessageRead((InvalidMessage)msg, con);

            default :
                return triggerInvalidMessage(con, InvalidMessage.invalidMessageTypeError);
        }
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

        return true;
    }

}
