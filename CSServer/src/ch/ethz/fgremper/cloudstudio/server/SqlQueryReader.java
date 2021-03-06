package ch.ethz.fgremper.cloudstudio.server;

import java.util.HashMap;

import org.apache.commons.io.IOUtils;

/**
 * 
 * Utility to read in .sql files to string for big SQL queries. Also caching queries that we already read.
 * 
 * @author Fabian Gremper
 * 
 */
public class SqlQueryReader {
	
	// Instance
	private static SqlQueryReader sqlQueryReader;
	
	// Cache
	private HashMap<String, String> cache = new HashMap<String, String>();
	
	/**
	 * 
	 * Read an SQL query
	 * 
	 * @param queryName name of the query which maps to the filename without the ".sql" extension
	 * 
	 * @return content of the file
	 * 
	 */
	public String getQuery(String queryName) {
		// Did we already read this file?
		if (cache.containsKey(queryName)) {
			// Get from cache
			return cache.get(queryName);
		}
		else {
			try {
				// Read file contents and store them in the cache
				String content = IOUtils.toString(SqlQueryReader.class.getResourceAsStream(queryName + ".sql"), "UTF-8");
				cache.put(queryName, content);
				return content;
			}
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	/**
	 * 
	 * Get instance of the SqlQueryReader
	 * 
	 * @return SqlQueryReader instance
	 * 
	 */
	public static SqlQueryReader getInstance() {
		if (sqlQueryReader == null) {
			sqlQueryReader = new SqlQueryReader();
		}
		return sqlQueryReader;
	}

}
