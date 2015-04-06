package ch.ethz.fgremper.cloudstudio.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import ch.ethz.fgremper.cloudstudio.common.RepositoryReader;

/**
 * 
 * RTCA client main class.
 * 
 * @author Fabian Gremper
 * 
 */
public class ClientMain {

	private static final Logger log = LogManager.getLogger(ClientMain.class);
    
	/**
	 * 
	 * Run the RTCA client.
	 * 
	 * @param args Filename of config XML can be specified as first argument (default is "config.xml")
	 * 
	 * @throws Exception
	 * 
	 */
	public static void main(String[] args) {
		
		// Set system look and feel
		/*
		try {
	        // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } 
		catch (Exception e) {
			// Do nothing
		}
		*/
		
		boolean showGui = true;
		if (args.length >= 2 && args[1].equals("--nogui")) showGui = false;
		
		// Load GUI
		if (showGui) {
			ClientGUI.createGuiContents();
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                ClientGUI.createAndShowGUI();
	            }
	        });
		}

		ClientConfig config;
		String sessionId;

		// Starting
		log.info("RTCA client starting...");
		
		// Read the config XML
		try {
			if (showGui) ClientGUI.setStatus("Reading config...");
			if (showGui) ClientGUI.addLogMessage("Reading config...");
			log.info("Reading config...");
			String configFileName = "config.xml";
			if (args.length >= 1) configFileName = args[0];
			config = new ClientConfigReader(configFileName).getConfig();
		}
		catch (Exception e) {
			if (showGui) ClientGUI.setStatusRed();
			if (showGui) ClientGUI.addLogMessage("Error while reading config file: " + e.getMessage());
			log.error("Error while reading config file: " + e.getMessage());
			return;
		}
		if (showGui) ClientGUI.setMonitoringText("Monitoring " + config.repositoriesList.size() + " local " + (config.repositoriesList.size() == 1 ? "repository" : "repositories") + ".");
		
		// Get our HTTP client
		HttpClient httpClient = new HttpClient();

		// Login and get a session ID
		if (showGui) ClientGUI.setStatus("Requesting session ID for login...");
		if (showGui) ClientGUI.addLogMessage("Requesting session ID for login...");
		log.info("Requesting session ID for login...");
		try {
			sessionId = httpClient.login(config.serverUrl, config.username, config.password);
		}
		catch (Exception e) {
			if (showGui) ClientGUI.setStatusRed();
			if (showGui) ClientGUI.addLogMessage("Error while requesting session ID: " + e.getMessage());
			log.error("Error while requesting session ID: " + e.getMessage());
			return;
		}
		if (showGui) ClientGUI.addLogMessage("Successfully retrieved session ID.");
		log.info("Retrieved session ID: " + sessionId);
		
		// Keep updating the RTCA server
		while (true) {

			// For all repositories we're going to read the local data and send some of it to the server
			for (RepositoryInfo repositoryInfo : config.repositoriesList) {	
				
				try {

					if (showGui) ClientGUI.setStatus("Reading and sending \"" + repositoryInfo.alias + "\"...");
					if (showGui) ClientGUI.addLogMessage("Reading and sending state for repository \"" + repositoryInfo.alias + "\"...");
					log.info("Reading and sending repository \"" + repositoryInfo.alias + "\" at " + repositoryInfo.localPath);

					// Read repository info
					RepositoryReader repositoryReader = new RepositoryReader(repositoryInfo.localPath);
					JSONObject updateObject = repositoryReader.getUpdateObject();
					
					// Send it to to the server
					String body = updateObject.toString();
					httpClient.sendGitState(config.serverUrl, sessionId, repositoryInfo.alias, body);
				
					if (showGui) ClientGUI.setLastUpdate();
				}
				catch (Exception e) {
					if (showGui) ClientGUI.setStatusYellow();
					if (showGui) ClientGUI.addLogMessage("Error reading or sending " + repositoryInfo.alias + ": " + e.getMessage());
					log.error("Error reading or sending " + repositoryInfo.alias + ": " + e.getMessage());
					e.printStackTrace();
				}
				
			}
			
			// If interval is 0, we only submit once, otherwise wait and repeat periodically
			if (config.resubmitInterval == 0) {
				break;
			}
			else {
				if (showGui) ClientGUI.setStatus("Idle");
				if (showGui) ClientGUI.addLogMessage("Waiting " + config.resubmitInterval + " seconds.");
				log.info("Waiting " + config.resubmitInterval + " seconds...");
				
				for (int i = 0; i < config.resubmitInterval * 10; i++) {
					if (ClientGUI.getForceUpdate() == true) {
						if (showGui) ClientGUI.addLogMessage("Update forced.");
						if (showGui) ClientGUI.setForceUpdate(false);
						break;
					}
					if (showGui) ClientGUI.setTimeTillNextUpdate((int) (1000.0 / config.resubmitInterval * i));
					try {
					    Thread.sleep(100);
					} catch (InterruptedException ex) {
					    Thread.currentThread().interrupt();
					}
				}
			}
		}

		log.info("RTCA client stopping...");
		
	}

}
