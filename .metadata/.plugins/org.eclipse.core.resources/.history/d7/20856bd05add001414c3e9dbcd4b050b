package ch.ethz.fgremper.cloudstudio.server;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 
 * Database connection pool.
 * 
 * @author Fabian Gremper
 *
 */
public class DatabaseConnectionPool {

    private static DatabaseConnectionPool datasource;
    private ComboPooledDataSource cpds;

    /**
     * 
     * Setup C3P0 with settings from server config
     * 
     */
    private DatabaseConnectionPool() throws IOException, SQLException, PropertyVetoException {
    	
    	ServerConfig serverConfig = ServerConfig.getInstance();
    	
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass(serverConfig.dbDriverClass);
        cpds.setJdbcUrl(serverConfig.dbJdbcUrl);
        cpds.setUser(serverConfig.dbUser);
        cpds.setPassword(serverConfig.dbPassword);
    	cpds.setAutoCommitOnClose(true);
    	
        cpds.setMinPoolSize(serverConfig.dbMinPoolSize);
        cpds.setAcquireIncrement(serverConfig.dbAcquireIncrement);
        cpds.setMaxPoolSize(serverConfig.dbMaxPoolSize);
        cpds.setMaxStatements(serverConfig.dbMaxStatements);

    }

    /**
     * 
     * Access the data source (singleton pattern)
     * 
     * @return datasource
     * 
     */
    public static DatabaseConnectionPool getInstance() throws IOException, SQLException, PropertyVetoException {
        if (datasource == null) {
            datasource = new DatabaseConnectionPool();
            return datasource;
        } else {
            return datasource;
        }
        
    }

    /**
     * 
     * Get a database connection
     * 
     */
    public Connection getConnection() throws SQLException {
        return this.cpds.getConnection();
    }

}
