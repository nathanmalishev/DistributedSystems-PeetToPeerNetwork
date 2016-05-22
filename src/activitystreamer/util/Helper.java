package activitystreamer.util;

import sun.misc.BASE64Encoder;

import java.security.PublicKey;

public class Helper {

    public static String publicKeyToString(PublicKey publicKey){
        byte array[] = publicKey.getEncoded();
        BASE64Encoder encoder = new BASE64Encoder();
        String publicKeyString = encoder.encode(array);
        return publicKeyString;
    }

    public static String createUniqueServerIdentifier(String LocalHost, String LocalPort){
        return LocalHost+':'+LocalPort;
    }
}
