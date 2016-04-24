package activitystreamer.messages;

import com.google.gson.Gson;

public class JsonMessage {

	public static final String invalidMessageTypeError = "the message type was not recognised";
	public static final String alreadyAuthenticatedError = "this server has already authenticated";

	protected String command;
	
	public String getCommand() {
		return command;
	}

	public String toData() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		return json;
	}

}
