package ch.ethz.fgremper.rtca;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Periodically calls the OriginUpdater.
 * @author Fabian Gremper
 */
public class PeriodicalAllOriginUpdater implements Runnable {

	private static final Logger log = LogManager.getLogger(PeriodicalAllOriginUpdater.class);
	
	private int originUpdateInterval = ServerConfig.getInstance().originUpdateInterval;
	
	public void run() {
		while (true) {
			
			// Update origins
			log.info("Updating all origins...");
			updateAll();
			
			// Sleep
			log.info("Waiting " + originUpdateInterval + " seconds before updating origins again...");
			try {
				Thread.sleep(originUpdateInterval * 1000);
			}
			catch (InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		}
	}
	
	public void updateAll() {
		
		DatabaseConnection db = null;
		
		JSONArray repositoriesArray;
		
		// Reading all repositories from database
		try {
			db = new DatabaseConnection();
			repositoriesArray = db.getAllRepositories();
		}
		catch (Exception e) {
			log.error("Error reading repositories from database...");
			return;
		}

		// Close database connection
		try {
			if (db != null) {
				db.closeConnection();
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Go through all the repositories and clone them
		for (int i = 0; i < repositoriesArray.length(); i++) {
			try {
				JSONObject repositoryObject = repositoriesArray.getJSONObject(i);
				String repositoryAlias = repositoryObject.getString("repositoryAlias");
				String repositoryUrl = repositoryObject.getString("repositoryUrl");
				OriginUpdater.update(repositoryAlias, repositoryUrl);
			}
			catch (Exception e) {
				//log.error("Error reading repository \"" + repositoryObject.getString("repositoryAlias"));
				return;
			}
		}
	}
}
