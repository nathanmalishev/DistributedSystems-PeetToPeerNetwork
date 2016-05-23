package activitystreamer.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class Helper {

    public static String publicKeyToString(PublicKey publicKey){
        byte array[] = publicKey.getEncoded();
        BASE64Encoder encoder = new BASE64Encoder();
        String publicKeyString = encoder.encode(array);
        return publicKeyString;
    }
    
    public static PublicKey stringToPublicKey(String keyString){
    	
    	byte array[];
    	PublicKey publicKey = null;
    	
		try {
			
			BASE64Decoder decoder = new BASE64Decoder();
			array = decoder.decodeBuffer(keyString);
			
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(array);
	    	
	    	KeyFactory keyFact = KeyFactory.getInstance("RSA");
	    	publicKey = keyFact.generatePublic(x509KeySpec);
	    	
		} catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
    	
    	return publicKey;
    }

    public static String createUniqueServerIdentifier(String LocalHost, String LocalPort){
        return LocalHost+':'+LocalPort;
    }
}
