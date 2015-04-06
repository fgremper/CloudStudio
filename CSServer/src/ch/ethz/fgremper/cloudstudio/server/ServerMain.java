package ch.ethz.fgremper.cloudstudio.server;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.ethz.fgremper.cloudstudio.common.ParameterFilter;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

/**
 * 
 * Main server routine. Runs the PeriodicalAllOriginUpdater and the HTTP server.
 * 
 * @author Fabian Gremper
 *
 */
public class ServerMain {

	private static final Logger log = LogManager.getLogger(ServerMain.class);
	
	private static int port = ServerConfig.getInstance().serverPort;

	public static void main(String[] args) throws Exception {

		/*
		
		log.info("Server started.");
		
        try {

    		DatabaseConnection db = new DatabaseConnection();
    		
    		db.startTransaction();
    		db.addUser("admin", "1234");
    		db.makeUserAdmin("admin");
    		db.commitTransaction();
    		
        }
        catch (Exception e) {
        	// nothing
        }
        
        */
        
		/*
		
		// Periodically origin updater
		log.info("Starting periodical origin updater...");
		
		PeriodicalAllOriginUpdater originUpdaterInterval = new PeriodicalAllOriginUpdater();
		new Thread(originUpdaterInterval).start();

		*/
		
		// HTTP server
		
		log.info("Starting HTTP server on port " + port + "...");

		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		
		// API Handler
		HttpContext context = server.createContext("/api", new ApiHttpHandler());
		context.getFilters().add(new ParameterFilter());
		
		// Web interface handler
		server.createContext(File.separator, new WebInterfaceHttpHandler());
		
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		log.info("Server up!");
		
	}

}