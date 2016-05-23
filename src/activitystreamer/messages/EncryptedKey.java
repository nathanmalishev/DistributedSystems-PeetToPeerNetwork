package activitystreamer.messages;

/**
 * Created by nathan on 24/05/2016.
 */
public class EncryptedKey extends JsonMessage {

    private byte[] encryptedSecretKey;

    public EncryptedKey(byte[] encryptedSecretKey){

        this.command = "ENCRYPTED_KEY";
        this.encryptedSecretKey = encryptedSecretKey;
    }

    public byte[] getEncryptedSecretKey() {
        return encryptedSecretKey;
    }
}
