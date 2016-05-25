package activitystreamer.messages;

import com.google.gson.Gson;

public class JsonMessage {
	public static final int VERSION_NUMBER = 2;
	public static final String invalidMessageTypeError = "the message type was not recognised";
	public static final String alreadyAuthenticatedError = "this server has already authenticated";
	public static final String unauthorisedServerError = "message sent from an unauthorised server";
	public static final String alreadyLoggedInError = "Cannot register a username and secret when already logged in";
	public static final String authenticationFailError = "This username and password is not valid";
	public static final String invalidSecretTypeError = "the supplied secret is incorrect: ";
	public static final String noMatchingUsernameError = "there was no client matching this username";
	public static final String incorrectSecretError = "the secret was incorrect for this username";
	public static final String genericLoginFailedError = "failed to login: incorrect details";
	public static final String loginSuccess = "logged in as user ";
	public static final String disconnectLogout = "client has disconnected";
	public static final String userAlreadyRegistered = "this username has already been registered";
	public static final String invalidUsernameError = "this username is invalid, it must start with a letter";
	public static final String invalidShardBoundaryError = "this username is invalid for this shard boudnary";
    public static final String registerSuccessMsg = "successful registration";

	protected String command;

	public String getCommand() {
		return command;
	}

	protected int version;

	public int getVersion() { return version; }

	public JsonMessage() {
		this.version = VERSION_NUMBER;
	}

	public String toData() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		return json;
	}

}
