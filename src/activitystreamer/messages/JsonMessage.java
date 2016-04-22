package activitystreamer.messages;

import com.google.gson.Gson;

public class JsonMessage {

	public static final String invalidMessageTypeError = "the message type was not recognised";

	private String messageType;
	
	public String getMessageType() {
		return messageType;
	}

	public JsonMessage respond() { return new InvalidMessage(invalidMessageTypeError); }

	public String toData() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		return json;
	}

}
