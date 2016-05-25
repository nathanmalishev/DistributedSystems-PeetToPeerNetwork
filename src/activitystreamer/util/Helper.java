package activitystreamer.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Helper {

    public static String publicKeyToString(PublicKey publicKey){
        byte array[] = publicKey.getEncoded();
        BASE64Encoder encoder = new BASE64Encoder();
        String publicKeyString = encoder.encode(array);
        return publicKeyString;
    }
    
    // TODO: Test
    public static String secretKeyToString(SecretKey secretKey){
        byte array[] = secretKey.getEncoded();
        BASE64Encoder encoder = new BASE64Encoder();
        String secretKeyString = encoder.encode(array);
        return secretKeyString;
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
    
    //TODO: Test
    public static SecretKey stringToSecretKey(String keyString){
    	
    	byte array[];
    	SecretKey secretKey = null;
    	
		try {
			
			BASE64Decoder decoder = new BASE64Decoder();
			array = decoder.decodeBuffer(keyString);
			
			secretKey = new SecretKeySpec(array, 0, array.length, "DES");
	    	
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return secretKey;
    }
    
    // TODO: Test
    public static byte[] asymmetricEncryption(PublicKey key, String msg){
    	
    	byte[] message = msg.getBytes();
    	byte[] textEncrypted = null;
    	
    	Cipher cipher;
		try {
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			textEncrypted = cipher.doFinal(message);
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return textEncrypted;
    }
    
    // TODO: Test
    public static byte[] asymmetricDecryption(PrivateKey key, byte[] message){
    	
    	byte[] textDecrypted = null;
    	
    	Cipher cipher;
		try {
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, key);
			textDecrypted = cipher.doFinal(message);
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return textDecrypted;
//    	
//    	
//    	
//    	byte[] encoded = encrypted.getBytes();
//    	System.out.println("Size of message: "+ encoded.length + " bytes");
//    	byte[] textDecoded = null;
//    	
//    	
//    	try {
//    		Cipher cipher = Cipher.getInstance("RSA");
//			cipher.init(Cipher.DECRYPT_MODE, key);
//			textDecoded = cipher.doFinal(encoded);
//			
//		} catch (InvalidKeyException e) {
//			e.printStackTrace();
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		} catch (NoSuchPaddingException e) {
//			e.printStackTrace();
//		} catch (IllegalBlockSizeException e) {
//			e.printStackTrace();
//		} catch (BadPaddingException e) {
//			e.printStackTrace();
//		}
//    	
//    	String decodedMessage = new String(textDecoded);
//    	
//    	return decodedMessage;
    }
    
    

    public static String createUniqueServerIdentifier(String LocalHost, String LocalPort){
    	
    	String address = LocalHost;
    	
    	if(LocalHost.equals("localhost")){
    		
    		try {
				address = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
    	}
    	
        return address+':'+LocalPort;
    }
}
