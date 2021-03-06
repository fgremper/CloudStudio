package ch.ethz.fgremper.cloudstudio.client;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * Reads the RTCA client configuration.
 * 
 * @author Fabian Gremper
 */
public class ClientConfigReader {

	private static final Logger log = LogManager.getLogger(ClientConfigReader.class);
	
	private ClientConfig config = new ClientConfig();
	
	/**
	 * 
	 * Reads the RTCA client configuration.
	 * 
	 * @param configFilename Filename of the config XML
	 * @throws Exception
	 * 
	 */
	public ClientConfigReader(String configFilename) throws Exception {
		
		log.info("Reading config file: " + configFilename);
		
		XMLConfiguration xmlConfig = new XMLConfiguration(configFilename);

		// Read username
		String username = xmlConfig.getString("username");
		if (username == null || username.equals("")) throw new Exception("No username in config");
		config.username = username;
		log.info("* username: " + username);

		// Read password
		String password = xmlConfig.getString("password");
		if (password == null || password.equals("")) throw new Exception("No password in config");
		config.password = password;
		log.info("* password: " + password);

		// Raad server URL
		String serverUrl = xmlConfig.getString("serverUrl");
		if (serverUrl == null || serverUrl.equals("")) throw new Exception("No serverUrl in config");
		config.serverUrl = serverUrl;
		log.info("* serverUrl: " + serverUrl);

		// Raad server URL
		String resubmitInterval = xmlConfig.getString("resubmitInterval");
		if (resubmitInterval == null || resubmitInterval.equals("")) throw new Exception("No resubmitInterval in config");
		config.resubmitInterval = Integer.parseInt(resubmitInterval);
		log.info("* resubmitInterval: " + resubmitInterval);
		
		// Read repository and their alias and local path
		boolean atLeastOneRepository = false;
		for (int i = 0; ; i++) {
			String repositoryAlias = xmlConfig.getString("repositories.repository(" + i + ").alias");
			String repositoryLocalPath = xmlConfig.getString("repositories.repository(" + i + ").localPath");
			if (repositoryAlias == null || repositoryAlias.equals("")) break;
			if (repositoryLocalPath == null || repositoryLocalPath.equals("")) break;
			atLeastOneRepository = true;
			config.repositoriesList.add(new RepositoryInfo(repositoryAlias, repositoryLocalPath));
			log.info("* repository(" + i + "): \"" + repositoryAlias + "\" (" + repositoryLocalPath + ")");
		}
		if (!atLeastOneRepository) throw new Exception("No repositories in config");

	}
	
	/**
	 * 
	 * @return the filled out ClientConfig object
	 * 
	 */
	public ClientConfig getConfig() {
		return config;
	}
	
}
