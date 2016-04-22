package activitystreamer.messages;

// Need to change this so it is importing the one in our library
import com.google.gson.Gson;



public class MessageFactory {

    public JsonMessage buildMessage(String msg) {

        /* GSON Parser transforms JSON objects into instance of a class */
        Gson parser = new Gson();

		/* Determine what kind of message we need to process */
        JsonMessage message = parser.fromJson(msg, JsonMessage.class);

        // Process accordingly
        switch(message.getMessageType()){

            case "AUTHENTICATE" :

                Authenticate authMessage = parser.fromJson(msg, Authenticate.class);
                return authMessage;

            case "AUTHENTICATION_FAIL" :

                AuthenticationFail authFailMessage = parser.fromJson(msg, AuthenticationFail.class);
                return authFailMessage;

            case "SERVER_ANNOUNCE" :

                ServerAnnounce serverAnnounceMessage = parser.fromJson(msg, ServerAnnounce.class);
                return serverAnnounceMessage;

            // --- Will be INVALID_MESSAGE ---
            default :
                break;

        }

        return null;

    }

}
