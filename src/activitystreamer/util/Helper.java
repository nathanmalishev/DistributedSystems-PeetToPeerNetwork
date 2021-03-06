package activitystreamer.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
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

	/**
	 * Checks to see if a specific port is available.
	 */
	public static boolean available(int port) {

		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if (ds != null) {
				ds.close();
			}

			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
                /* should not be thrown */
				}
			}
		}

		return false;
	}
}
