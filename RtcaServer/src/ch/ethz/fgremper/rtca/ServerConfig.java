package ch.ethz.fgremper.rtca;

import org.apache.commons.configuration.XMLConfiguration;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class ServerConfig {
	private static ServerConfig serverConfig;

	public String dbDriverClass;
	public String dbJdbcUrl;
	public String dbUser;
	public String dbPassword;

	public String dbMinPoolSize;
	public String dbAcquireIncrement;
	public String dbMaxPoolSize;
	public String dbMaxStatements;
	
	public ServerConfig() {
		try {
			XMLConfiguration xmlConfig = new XMLConfiguration("serverConfig.xml");
		
			dbDriverClass = xmlConfig.getString("dbDriverClass");
			dbJdbcUrl = xmlConfig.getString("dbJdbcUrl");
			dbUser = xmlConfig.getString("dbUser");
			dbPassword = xmlConfig.getString("dbPassword");

			dbMinPoolSize = xmlConfig.getString("dbMinPoolSize");
			dbAcquireIncrement = xmlConfig.getString("dbAcquireIncrement");
			dbMaxPoolSize = xmlConfig.getString("dbMaxPoolSize");
			dbMaxStatements = xmlConfig.getString("dbMaxStatements");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ServerConfig getInstance() {
		if (serverConfig == null) {
			serverConfig = new ServerConfig();
		}
		return serverConfig;
	}
}

