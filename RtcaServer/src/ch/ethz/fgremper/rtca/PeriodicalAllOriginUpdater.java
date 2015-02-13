package ch.ethz.fgremper.rtca;

import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

public class PeriodicalAllOriginUpdater implements Runnable {
	public PeriodicalAllOriginUpdater() {
		
	}
	
	public void run() {
		while (true) {
			updateAll();
			
			// Sleep
			try {
				Thread.sleep(10000);
			}
			catch (InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
		}
	}
	
	public void updateAll() {
		DatabaseConnection db = null;
		try {
			db = new DatabaseConnection();
			JSONArray repositoriesArray = db.getAllRepositories();
			for (int i = 0; i < repositoriesArray.length(); i++) {
				JSONObject repositoryObject = repositoriesArray.getJSONObject(i);
				String repositoryAlias = repositoryObject.getString("repositoryAlias");
				String repositoryUrl = repositoryObject.getString("repositoryUrl");
				OriginUpdater.update(repositoryAlias, repositoryUrl);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
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
	}
}
