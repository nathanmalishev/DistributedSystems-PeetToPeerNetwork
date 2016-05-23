package activitystreamer.messages;


import activitystreamer.server.Control;
import com.google.gson.*;
import com.google.gson.stream.MalformedJsonException;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.*;

/**
 * Class controls the creation of the particular JSON Message which
 * is required for sending. Each message will be an instance of its own class
 * containing information relevant to the protocol.
 */
public class MessageFactory {

    public JsonMessage buildMessage(String msg, Logger log) {
        JsonMessage message;
        /* GSON Parser transforms JSON objects into instance of a class */
        Gson parser = new Gson();

		/* Determine what kind of message we need to process */
        try {
            message = parser.fromJson(msg, JsonMessage.class);
        }catch(Exception e){
            /* catches any malformed json strings */
            return null;
        }
        if(message.getCommand() == null){
            return null;
        }

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

                case "REDIRECT":
                    Gson redirectGson =  new GsonBuilder().registerTypeAdapter(Redirect.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    Redirect redirect = redirectGson.fromJson(msg, Redirect.class);
                    return redirect;

                case "ACTIVITY_BROADCAST":
                    Gson activityBroadcastGson =  new GsonBuilder().registerTypeAdapter(ActivityBroadcast.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    ActivityBroadcast activityBroadcast = activityBroadcastGson.fromJson(msg, ActivityBroadcast.class);
                    return activityBroadcast;

                case "REGISTER":
                    Gson registerGson = new GsonBuilder().registerTypeAdapter(Register.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    Register registerMsg = registerGson.fromJson(msg, Register.class);
                    return registerMsg;

                case "REGISTER_FAILED":
                    Gson registerFailedGson = new GsonBuilder().registerTypeAdapter(RegisterFailed.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    RegisterFailed registerFailMsg = registerFailedGson.fromJson(msg, RegisterFailed.class);
                    return registerFailMsg;

                case "REGISTER_SUCCESS":
                    Gson registerSuccessGson = new GsonBuilder().registerTypeAdapter(RegisterSuccess.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    RegisterSuccess regSuccessMsg = registerSuccessGson.fromJson(msg, RegisterSuccess.class);
                    return regSuccessMsg;

                case "LOGIN_SUCCESS":
                    Gson loginSuccessGson =  new GsonBuilder().registerTypeAdapter(LoginSuccess.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return loginSuccessGson.fromJson(msg, LoginSuccess.class);

                case "LOGOUT":
                    Gson logoutGson =  new GsonBuilder().registerTypeAdapter(Logout.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return logoutGson.fromJson(msg, Logout.class);

                case "LOCK_REQUEST":
                    Gson lockRequestGson =  new GsonBuilder().registerTypeAdapter(LockRequest.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return lockRequestGson.fromJson(msg, LockRequest.class);

                case "LOCK_DENIED":
                    Gson lockDenied =  new GsonBuilder().registerTypeAdapter(LockDenied.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return lockDenied.fromJson(msg, LockDenied.class);

                case "READ_REPLY":
                    Gson readReply =  new GsonBuilder().registerTypeAdapter(ReadReply.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return readReply.fromJson(msg, ReadReply.class);

                case "READ_REQUEST":
                    Gson readRequest =  new GsonBuilder().registerTypeAdapter(ReadRequest.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return readRequest.fromJson(msg, ReadRequest.class);

                case "WRITE_REPLY":
                    Gson writeReply =  new GsonBuilder().registerTypeAdapter(WriteReply.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return writeReply.fromJson(msg, WriteReply.class);

                case "WRITE_REQUEST":
                    Gson writeRequest =  new GsonBuilder().registerTypeAdapter(WriteRequest.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return writeRequest.fromJson(msg, WriteRequest.class);

                case "REGISTER_KEY":
                    Gson registerKey = new GsonBuilder().registerTypeAdapter(RegisterKey.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return registerKey.fromJson(msg, RegisterKey.class);
                    
                case "KEY_REGISTER_RESPONSE":
                	Gson keyRegisterResponse = new GsonBuilder().registerTypeAdapter(KeyRegisterResponse.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                	return keyRegisterResponse.fromJson(msg, KeyRegisterResponse.class);
                	
                case "GET_KEY":
                	Gson getKey = new GsonBuilder().registerTypeAdapter(GetKey.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                	return getKey.fromJson(msg, GetKey.class);
                	
                case "GET_KEY_SUCCESS":
                	Gson getKeySuccess = new GsonBuilder().registerTypeAdapter(GetKeySuccess.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                	return getKeySuccess.fromJson(msg, GetKeySuccess.class);
                	
                case "GET_KEY_FAILED":
                	Gson getKeyFailed = new GsonBuilder().registerTypeAdapter(GetKeyFailed.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                	return getKeyFailed.fromJson(msg, GetKeyFailed.class);

                case "ENCRYPTED_KEY":
                    Gson encryptedKey = new GsonBuilder().registerTypeAdapter(EncryptedKey.class, new EnforcedDeserializer<JsonMessage>(log)).create();
                    return encryptedKey.fromJson(msg, EncryptedKey.class);
                	
                default:
                    return null;

            }
        } catch (JsonParseException e ) {
            log.error(e);
            return null;
        }

    }

}
