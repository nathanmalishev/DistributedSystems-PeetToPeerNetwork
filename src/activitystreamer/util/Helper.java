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
    

    public static byte[] asymmetricEncryption(PublicKey key, String msg){
    	
    	byte[] message = msg.getBytes();
    	byte[] textEncrypted = null;
    	
    	Cipher cipher;
		try {
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			textEncrypted = cipher.doFinal(message);
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
		
			e.printStackTrace();
		}
	    
	    return textEncrypted;
    }
    

    public static byte[] asymmetricDecryption(PrivateKey key, byte[] message){
    	
    	byte[] textDecrypted = null;
    	
    	Cipher cipher;
		try {
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, key);
			textDecrypted = cipher.doFinal(message);
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
		return textDecrypted;
    }
    
    public static byte[] symmetricEncryption(SecretKey key, String msg){
    	
    	byte[] message = msg.getBytes();
    	byte[] textEncrypted = null;
    	
    	Cipher desCipher;
		try {
			desCipher = Cipher.getInstance("DES");
			desCipher.init(Cipher.ENCRYPT_MODE, key);
			textEncrypted = desCipher.doFinal(message);
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
    	
    	return textEncrypted;
    }
    
    public static byte[] symmetricDecryption(SecretKey key, byte[] encrypted){
    	
    	byte[] textDecrypted = null;
    	
    	Cipher desCipher;
    	try {
    		desCipher = Cipher.getInstance("DES");
			desCipher.init(Cipher.DECRYPT_MODE, key);
			textDecrypted = desCipher.doFinal(encrypted);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		
		return textDecrypted;    
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
