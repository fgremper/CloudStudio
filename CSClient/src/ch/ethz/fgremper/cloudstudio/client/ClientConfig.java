package ch.ethz.fgremper.cloudstudio.client;

import java.util.LinkedList;

/**
 * Contains the configuration for the RTCA client.
 * @author Fabian Gremper
 */
public class ClientConfig {
	
	public String username;
	public String password;
	public String serverUrl;
	public int resubmitInterval;
	public LinkedList<RepositoryInfo> repositoriesList = new LinkedList<RepositoryInfo>();
	
}
