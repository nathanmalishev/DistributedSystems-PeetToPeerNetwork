package activitystreamer.messages;

import com.google.gson.Gson;

public class Register extends JsonMessage {

    private String username;
    private String secret;

    public String getUsername() { return username; }
    public String getSecret() { return secret; }

    public Register(String username, String secret) {
        this.command = "REGISTER";
        this.username = username;
        this.secret = secret;
    }
}
