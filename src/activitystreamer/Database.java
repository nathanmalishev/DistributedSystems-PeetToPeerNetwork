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

import activitystreamer.database.DBShard;
import activitystreamer.util.Settings;

public class Database {
	private static final Logger log = LogManager.getLogger();
	
	private static void help(Options options){
		String header = "An ActivityStream Database chard for Unimelb COMP90015\n\n";
		String footer = "\ncontact jeams@student.unimelb.edu.au for issues.";
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ActivityStreamer.Database", header, options, footer, true);
		System.exit(-1);
	}
	
	public static void main(String[] args) {
		
		log.info("reading command line options");

		int dbnum = 0;

		Options options = new Options();
		options.addOption("lp",true,"local port number");
		options.addOption("lh",true,"local hostname");
		options.addOption("s",true,"secret for the server to use");
		options.addOption("dbnum",true,"number of the database {0, 1, 2, 3}");


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
		
		try {
			Settings.setLocalHostname(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			log.warn("failed to get localhost IP address");
		}
		
		if(cmd.hasOption("lh")){
			Settings.setLocalHostname(cmd.getOptionValue("lh"));
		}

		if(cmd.hasOption("dbnum")){
			dbnum = Integer.parseInt((cmd.getOptionValue("dbnum")));
		}

		// Assign Random secret if we haven't been given one
		if(cmd.hasOption("s")){
			Settings.setSecret(cmd.getOptionValue("s"));
		}
		else{
			Settings.setSecret(Settings.nextSecret());
		}
		
		// Assign Random ID to the db at startup
		Settings.setId(Settings.nextSecret());
		
		log.info("starting server with secret: " + Settings.getSecret());
		
		
		final DBShard db = new DBShard(dbnum);
		
		// the following shutdown hook doesn't really work, it doesn't give us enough time to
		// cleanup all of our connections before the jvm is terminated.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {  
				db.setTerm(true);
				db.interrupt();
		    }
		 });
	}

}
