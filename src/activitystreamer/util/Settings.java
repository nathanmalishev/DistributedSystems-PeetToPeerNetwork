package activitystreamer.util;

import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Settings {
	private static final Logger log = LogManager.getLogger();
	private static SecureRandom random = new SecureRandom();
	private static int localPort = 3780;
	private static String localHostname = "localhost";
	private static String remoteHostname = null;
	private static int remotePort = 3780;
	private static int activityInterval = 5000; // milliseconds
	private static String secret = null;
	private static String username = "anonymous";
	private static String id;
	private static String keyRegisterHostname = null;
	private static int keyRegisterPort;
	private static String shardAHostname = null;
	private static String shardBHostname = null;
	private static String shardCHostname = null;
	private static String shardDHostname = null;
	private static int shardAPort;
	private static int shardBPort;
	private static int shardCPort;
	private static int shardDPort;

	public static void setId(String id){
		Settings.id = id;
	}
	
	public static String getId(){
		return id;
	}
	
	public static int getLocalPort() {
		return localPort;
	}

	public static void setLocalPort(int localPort) {
		if(localPort<0 || localPort>65535){
			log.error("supplied port "+localPort+" is out of range, using "+getLocalPort());
		} else {
			Settings.localPort = localPort;
		}
	}
	
	public static int getRemotePort() {
		return remotePort;
	}

	public static void setRemotePort(int remotePort) {
		if(remotePort<0 || remotePort>65535){
			log.error("supplied port "+remotePort+" is out of range, using "+getRemotePort());
		} else {
			Settings.remotePort = remotePort;
		}
	}
	
	public static String getRemoteHostname() {
		return remoteHostname;
	}

	public static void setRemoteHostname(String remoteHostname) {
		Settings.remoteHostname = remoteHostname;
	}
	
	public static int getActivityInterval() {
		return activityInterval;
	}

	public static void setActivityInterval(int activityInterval) {
		Settings.activityInterval = activityInterval;
	}
	
	public static String getSecret() {
		return secret;
	}

	public static void setSecret(String s) {
		secret = s;
	}
	
	public static String getUsername() {
		return username;
	}

	public static void setUsername(String username) {
		Settings.username = username;
	}
	
	public static String getLocalHostname() {
		return localHostname;
	}

	public static void setLocalHostname(String localHostname) {
		Settings.localHostname = localHostname;
	}

	
	/*
	 * some general helper functions
	 */
	
	public static String socketAddress(Socket socket){
		return socket.getInetAddress()+":"+socket.getPort();
	}

	public static String nextSecret() {
	    return new BigInteger(130, random).toString(32);
	 }

	public static String getKeyRegisterHostname() { return keyRegisterHostname; }

	public static String getShardAHostname() { return shardAHostname; }

	public static void setKeyRegisterHostname(String keyRegisterHostname) {
		Settings.keyRegisterHostname = keyRegisterHostname;
	}

	public static int getKeyRegisterPort() {
		return keyRegisterPort;
	}

	public static void setKeyRegisterPort(int keyRegisterPort) {
		Settings.keyRegisterPort = keyRegisterPort;
	}

	public static int getDefaultKeyRegisterPort() { return 2005; }

	public static String getShardBHostname() { return shardBHostname; }

	public static String getShardCHostname() { return shardCHostname; }

	public static String getShardDHostname() { return shardDHostname; }

	public static int getShardAPort() { return shardAPort; }

	public static int getShardBPort() { return shardBPort; }

	public static int getShardCPort() { return shardCPort; }

	public static int getShardDPort() { return shardDPort; }

	public static void setShardAHostname(String name) { shardAHostname = name; }

	public static void setShardBHostname(String name) { shardBHostname = name; }

	public static void setShardCHostname(String name) { shardCHostname = name; }

	public static void setShardDHostname(String name) { shardDHostname = name; }

	public static void setShardAPort(int port) { shardAPort = port; }

	public static void setShardBPort(int port) { shardBPort = port; }

	public static void setShardCPort(int port) { shardCPort = port; }

	public static void setShardDPort(int port) { shardDPort = port; }

	public static int defaultShardAPort() { return 2000; }

	public static int defaultShardBPort() { return 2001; }

	public static int defaultShardCPort() { return 2002; }

	public static int defaultShardDPort() { return 2003; }

	public static String defaultShardHostname() { return "localhost"; }

}
