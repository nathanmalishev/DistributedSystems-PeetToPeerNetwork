package activitystreamer.messages;


import com.google.gson.Gson;

/**
 * Created by Jeames on 22/04/2016.
 */
public class Login extends JsonMessage {

    private String username;
    private String secret;

    public String getUsername() { return username; }
    public String getSecret() { return secret; }

    public Login(String username, String secret) {

        this.command = "LOGIN";
        this.username = username;
        // Going to use null values for when username is anonymous
        if (!isAnonymous()) {
            this.secret = secret;
        } else {
            this.secret = null;
        }
    }

    @Override
    /* Overrides in order to give a value to secret to pass the deserializer when message
     * is received
     */
    public String toData() {
        Gson gson = new Gson();
        if (isAnonymous()) {
            this.secret = "";
        }
        String json = gson.toJson(this);
        return json;
    }

    public boolean isAnonymous() {
        return username.equals("anonymous");
    }

}

