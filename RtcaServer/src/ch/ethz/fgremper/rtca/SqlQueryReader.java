package ch.ethz.fgremper.rtca;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;

public class SqlQueryReader {
	private static SqlQueryReader sqlQueryReader;
	private HashMap<String, String> map = new HashMap<String, String>();
	
	public SqlQueryReader() {
		
	}
	
	public String getQuery(String queryName) {
		if (map.containsKey(queryName)) {
			return map.get(queryName);
		}
		else {
			try {
				String content = FileUtils.readFileToString(new File(queryName + ".sql"), "UTF-8");
				System.out.println("READ: " + content);
				map.put(queryName, content);
				return content;
			}
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public static SqlQueryReader getInstance() {
		if (sqlQueryReader == null) {
			sqlQueryReader = new SqlQueryReader();
		}
		return sqlQueryReader;
	}

}