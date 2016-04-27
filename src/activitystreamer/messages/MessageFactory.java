package activitystreamer.messages;

// Need to change this so it is importing the one in our library
import activitystreamer.server.Control;
import com.google.gson.*;
import com.google.gson.stream.MalformedJsonException;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.*;

public class MessageFactory {

    public JsonMessage buildMessage(String msg, Logger log) {
//        log.info(msg);
        JsonMessage message;
        /* GSON Parser transforms JSON objects into instance of a class */
        Gson parser = new Gson();
		/* Determine what kind of message we need to process */
        try {
            message = parser.fromJson(msg, JsonMessage.class);
        }catch(Exception e){
            /* catches any malformed json strings */
            log.error(e);
            return null;
        }
        if(message.getCommand() == null){
            log.error("message was null");
            return null;
        }

        log.info("received: " + msg);
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

                case "ACTIVITY_MESSAGE":
                    Gson activityMsgGson =  new GsonBuilder().registerTypeAdapter(ActivityMessage.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    ActivityMessage activityMessage = activityMsgGson.fromJson(msg, ActivityMessage.class);
                    return activityMessage;

                case "ACTIVITY_BROADCAST":
                    Gson activityBroadcastGson =  new GsonBuilder().registerTypeAdapter(ActivityBroadcast.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    ActivityBroadcast activityBroadcast = activityBroadcastGson.fromJson(msg, ActivityBroadcast.class);
                    return activityBroadcast;

                case "REGISTER":
                    Gson registerGson = new GsonBuilder().registerTypeAdapter(Register.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return registerGson.fromJson(msg, Register.class);

                case "REGISTER_FAILED":
                    Gson registerFailedGson = new GsonBuilder().registerTypeAdapter(RegisterFailed.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return registerFailedGson.fromJson(msg, RegisterFailed.class);

                case "REGISTER_SUCCESS":
                    Gson registerSuccessGson = new GsonBuilder().registerTypeAdapter(RegisterSuccess.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return registerSuccessGson.fromJson(msg, RegisterSuccess.class);

                case "LOGIN_SUCCESS":
                    Gson loginSuccessGson =  new GsonBuilder().registerTypeAdapter(LoginSuccess.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return loginSuccessGson.fromJson(msg, LoginSuccess.class);

                case "LOGOUT":
                    Gson logoutGson =  new GsonBuilder().registerTypeAdapter(Logout.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    System.out.print("tyring to log out");
                    return logoutGson.fromJson(msg, Logout.class);

                case "LOCK_REQUEST":
                    Gson lockRequestGson =  new GsonBuilder().registerTypeAdapter(LockRequest.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return lockRequestGson.fromJson(msg, LockRequest.class);

                case "LOCK_DENIED":
                    Gson lockDenied =  new GsonBuilder().registerTypeAdapter(LockDenied.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return lockDenied.fromJson(msg, LockDenied.class);

                case "LOCK_ALLOWED":
                    Gson lockAllowed =  new GsonBuilder().registerTypeAdapter(LockAllowed.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return lockAllowed.fromJson(msg, LockAllowed.class);

                default:
                    return null;

            }
        } catch (JsonParseException e ) {
            log.error(e);
            return null;
        }

    }

}
