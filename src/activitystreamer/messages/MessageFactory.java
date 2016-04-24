package activitystreamer.messages;

// Need to change this so it is importing the one in our library
import activitystreamer.server.Control;
import com.google.gson.*;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.*;

public class MessageFactory {

    public JsonMessage buildMessage(String msg, Logger log) {
        log.info(msg);
        /* GSON Parser transforms JSON objects into instance of a class */
        Gson parser = new Gson();
		/* Determine what kind of message we need to process */
        JsonMessage message = parser.fromJson(msg, JsonMessage.class);
        log.info(msg);
        try {
            // Process accordingly
            switch (message.getCommand()) {

                case "AUTHENTICATE":
                    Gson authGson = new GsonBuilder().registerTypeAdapter(Authenticate.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    Authenticate authMessage = authGson.fromJson(msg, Authenticate.class);
                    return authMessage;

                case "AUTHENTICATION_FAIL":
                    Gson authFailGson = new GsonBuilder().registerTypeAdapter(AuthenticationFail.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    AuthenticationFail authFailMessage = authFailGson.fromJson(msg, AuthenticationFail.class);
                    return authFailMessage;

                case "SERVER_ANNOUNCE":
                    Gson serverAnnounceGson =  new GsonBuilder().registerTypeAdapter(ServerAnnounce.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    ServerAnnounce serverAnnounceMessage = serverAnnounceGson.fromJson(msg, ServerAnnounce.class);
                    return serverAnnounceMessage;

                case "LOGIN":
                    Gson loginGson =  new GsonBuilder().registerTypeAdapter(Login.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    Login loginMessage = loginGson.fromJson(msg, Login.class);
                    return loginMessage;

                case "LOGIN_FAILED" :

                    Gson loginFailedGson =  new GsonBuilder().registerTypeAdapter(LoginFailed.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    LoginFailed loginFailedMessage = loginFailedGson.fromJson(msg, LoginFailed.class);
                    return loginFailedMessage;

                case "INVALID_MESSAGE":
                    InvalidMessage invalidMessage = parser.fromJson(msg, InvalidMessage.class);
                    return invalidMessage;

                default:
                    return null;

            }
        } catch (JsonParseException e ) {
            log.error(e);
            return null;
        }

    }

}
