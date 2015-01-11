package ch.ethz.fgremper.rtca;

import org.apache.commons.configuration.XMLConfiguration;

public class ClientConfigReader {

	private ClientConfig config = new ClientConfig();
	
	public ClientConfigReader(String configFilename) throws Exception {
		
		System.out.println("[Config] Reading config from xml");
		
		XMLConfiguration xmlConfig = new XMLConfiguration(configFilename);

		String username = xmlConfig.getString("username");
		if (username == null || username.equals("")) throw new Exception("No username in config");
		config.username = username;
		System.out.println("[Config] username: " + username);
		
		String serverUrl = xmlConfig.getString("serverUrl");
		if (serverUrl == null || serverUrl.equals("")) throw new Exception("No serverUrl in config");
		config.serverUrl = serverUrl;
		System.out.println("[Config] serverUrl: " + serverUrl);
		
		boolean atLeastOneRepository = false;
		for (int i = 0; ; i++) {
			String repositoryAlias = xmlConfig.getString("repositories.repository(" + i + ").alias");
			String repositoryLocalPath = xmlConfig.getString("repositories.repository(" + i + ").localPath");
			if (repositoryAlias == null || repositoryAlias.equals("")) break;
			if (repositoryLocalPath == null || repositoryLocalPath.equals("")) break;
			atLeastOneRepository = true;
			config.repositoriesList.add(new RepositoryInfo(repositoryAlias, repositoryLocalPath));
			System.out.println("[Config] repository(" + i + "): \"" + repositoryAlias + "\" (" + repositoryLocalPath + ")");
		}
		if (!atLeastOneRepository) throw new Exception("No repositories in config");

	}
	
	public ClientConfig getConfig() {
		return config;
	}
	
}
