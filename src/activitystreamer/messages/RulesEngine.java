package activitystreamer.messages;

import activitystreamer.server.Connection;
import activitystreamer.util.Settings;

public class RulesEngine {


    public static JsonMessage triggerResponse(JsonMessage msg, Connection con) {

        // Process accordingly
        switch(msg.getMessageType()){

            case "AUTHENTICATE" :

                triggerAuthenticateAttempt((Authenticate)msg, con);

            case "AUTHENTICATION_FAIL" :

                triggerAuthenticationFailRead((AuthenticationFail) msg, con);

            case "SERVER_ANNOUNCE" :

                triggerServerAnnounceRead((ServerAnnounce)msg, con);

            // --- Will be INVALID_MESSAGE ---

            case "INVALID_MESSAGE" :
                triggerInvalidMessageRead((InvalidMessage)msg, con);

            default :

                triggerInvalidMessage(con);

        }


    }

    public static boolean triggerServerAnnounceRead(ServerAnnounce msg, Connection con) {



    }

    public static boolean triggerAuthenticateAttempt(Authenticate msg, Connection con) {

        // Check if secret is valid



        if(!msg.getSecret().equals(Settings.getSecret())){

            // If secret is invalid, send authentication fail message

            String info = AuthenticationFail.invalidMessageTypeError + msg.getSecret();

            JsonMessage response = new AuthenticationFail(info);

            con.writeMsg(response.toData());

            // Close the connection

            return true;
        }

        // Otherwise, do not close the connection

        return false;

    }

    public static boolean triggerAuthenticationFailRead(AuthenticationFail msg, Connection con) {

    }

    public static boolean triggerInvalidMessageRead(InvalidMessage msg, Connection con) {

    }

    public static boolean triggerInvalidMessage(Connection con) {
        
    }

}
