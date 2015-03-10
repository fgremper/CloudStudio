package ch.ethz.fgremper.rtca;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * RTCA client main class.
 * @author Fabian Gremper
 */
public class ClientMain {

	private static final Logger log = LogManager.getLogger(ClientMain.class);
	
	/**
	 * Run the RTCA client.
	 * @param args Filename of config XML can be specified as first argument (default is "config.xml")
	 * @throws Exception
	 */
	public static void main(String[] args) {

		ClientConfig config;
		String sessionId;

		// Starting
		log.info("RTCA client starting...");
		
		// Read the config XML
		try {
			log.info("Reading config...");
			String configFileName = "config.xml";
			if (args.length >= 1) configFileName = args[0];
			config = new ClientConfigReader(configFileName).getConfig();
		}
		catch (Exception e) {
			log.error("Error while reading config file: " + e.getMessage());
			return;
		}
		
		// Get our HTTP client
		HttpClient httpClient = new HttpClient();

		// Login and get a session ID
		log.info("Login and requesting session ID...");
		try {
			sessionId = httpClient.login(config.serverUrl, config.username, config.password);
		}
		catch (Exception e) {
			log.error("Error while requesting session ID: " + e.getMessage());
			return;
		}
		log.info("Retrieved session ID: " + sessionId);
		
		// Keep updating the RTCA server
		while (true) {

			// For all repositories we're going to read the local data and send some of it to the server
			for (RepositoryInfo repositoryInfo : config.repositoriesList) {	
				
				try {
					
					log.info("Reading and sending repository \"" + repositoryInfo.alias + "\" at " + repositoryInfo.localPath);
					
					// Read repository info
					RepositoryReader repositoryReader = new RepositoryReader(repositoryInfo.localPath);
					JSONObject updateObject = repositoryReader.getUpdateObject();
					
			        // Store user information
					updateObject.put("sessionId", sessionId);
					updateObject.put("repositoryAlias", repositoryInfo.alias);
					
					// Send it to to the server
					String jsonString = updateObject.toString();
					httpClient.sendGitState(config.serverUrl, jsonString);
				
				}
				catch (Exception e) {
					log.error("Error while reading/sending local git state for " + repositoryInfo.alias + ": " + e.getMessage());
				}
				
			}
			
			// If interval is 0, we only submit once, otherwise wait and repeat periodically
			if (config.resubmitInterval == 0) {
				break;
			}
			else {
				log.info("Waiting " + config.resubmitInterval + " seconds...");
				try {
				    Thread.sleep(config.resubmitInterval * 1000);
				} catch (InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}
			}
		}

		log.info("RTCA client stopping...");
		
	}

}
