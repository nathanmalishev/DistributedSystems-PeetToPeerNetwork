package activitystreamer;


import java.net.InetAddress;

import java.net.UnknownHostException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import activitystreamer.server.ControlSolution;
import activitystreamer.util.Settings;

public class Server {
	private static final Logger log = LogManager.getLogger();
	
	private static void help(Options options){
		String header = "An ActivityStream Server for Unimelb COMP90015\n\n";
		String footer = "\ncontact aharwood@unimelb.edu.au for issues.";
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ActivityStreamer.Server", header, options, footer, true);
		System.exit(-1);
	}
	
	public static void main(String[] args) {
		
		log.info("reading command line options");
		
		Options options = new Options();
		options.addOption("lp",true,"local port number");
		options.addOption("rp",true,"remote port number");
		options.addOption("rh",true,"remote hostname");
		options.addOption("lh",true,"local hostname");
		options.addOption("a",true,"activity interval in milliseconds");
		options.addOption("s",true,"secret for the server to use");
		
		
		// build the parser
		CommandLineParser parser = new DefaultParser();
		
		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, args);
		} catch (ParseException e1) {
			help(options);
		}
		
		if(cmd.hasOption("lp")){
			try{
				int port = Integer.parseInt(cmd.getOptionValue("lp"));
				Settings.setLocalPort(port);
			} catch (NumberFormatException e){
				log.info("-lp requires a port number, parsed: "+cmd.getOptionValue("lp"));
				help(options);
			}
		}
		
		if(cmd.hasOption("rh")){
			Settings.setRemoteHostname(cmd.getOptionValue("rh"));
		}
		
		if(cmd.hasOption("rp")){
			try{
				int port = Integer.parseInt(cmd.getOptionValue("rp"));
				Settings.setRemotePort(port);
			} catch (NumberFormatException e){
				log.error("-rp requires a port number, parsed: "+cmd.getOptionValue("rp"));
				help(options);
			}
		}
		
		if(cmd.hasOption("a")){
			try{
				int a = Integer.parseInt(cmd.getOptionValue("a"));
				Settings.setActivityInterval(a);
			} catch (NumberFormatException e){
				log.error("-a requires a number in milliseconds, parsed: "+cmd.getOptionValue("a"));
				help(options);
			}
		}
		
		try {
			Settings.setLocalHostname(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			log.warn("failed to get localhost IP address");
		}
		
		if(cmd.hasOption("lh")){
			Settings.setLocalHostname(cmd.getOptionValue("lh"));
		}
		if(cmd.hasOption("dbah")){
			Settings.setShardAHostname(cmd.getOptionValue("dbah"));
		} else {
			Settings.setShardAHostname("localhost");
		}
		if(cmd.hasOption("dbbh")){
			Settings.setShardBHostname(cmd.getOptionValue("dbbh"));
		} else {
			Settings.setShardBHostname("localhost");
		}
		if(cmd.hasOption("dbch")){
			Settings.setShardCHostname(cmd.getOptionValue("dbch"));
		} else {
			Settings.setShardCHostname("localhost");
		}
		if(cmd.hasOption("dbdh")){
			Settings.setShardDHostname(cmd.getOptionValue("dbdh"));
		} else {
			Settings.setShardDHostname("localhost");
		}
		if(cmd.hasOption("dbap")){
			Settings.setShardAPort(Integer.parseInt(cmd.getOptionValue("dbap")));
		} else {
			Settings.setShardAPort(2000);
		}
		if(cmd.hasOption("dbbp")){
			Settings.setShardBPort(Integer.parseInt(cmd.getOptionValue("dbbp")));
		} else {
			Settings.setShardBPort(2001);
		}
		if(cmd.hasOption("dbcp")){
			Settings.setShardCPort(Integer.parseInt(cmd.getOptionValue("dbcp")));
		} else {
			Settings.setShardCPort(2002);
		}
		if(cmd.hasOption("dbdp")){
			Settings.setShardDPort(Integer.parseInt(cmd.getOptionValue("dbdp")));
		} else {
			Settings.setShardDPort(2003);
		}
		// Assign Random secret if we haven't been given one
		if(cmd.hasOption("s")){
			Settings.setSecret(cmd.getOptionValue("s"));
		}
		else{
			Settings.setSecret(Settings.nextSecret());
		}
		
		// Assign Random ID to the server at startup
		Settings.setId(Settings.nextSecret());
		
		log.info("starting server with secret: " + Settings.getSecret());
		
		
		final ControlSolution c = ControlSolution.getInstance(); 
		
		// the following shutdown hook doesn't really work, it doesn't give us enough time to
		// cleanup all of our connections before the jvm is terminated.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {  
				c.setTerm(true);
				c.interrupt();
		    }
		 });
	}

}
