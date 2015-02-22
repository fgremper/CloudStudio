package ch.ethz.fgremper.rtca;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.SQLException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class OriginUpdater {
	
	public static void update(String repositoryAlias, String repositoryUrl) {
		DatabaseConnection db = null;
		try {
			db = new DatabaseConnection();
			
			String originStorageDirectory = ServerConfig.getInstance().originStorageDirectory;
			
			System.out.println("[OriginUpdater] Cloning repository \"" + repositoryAlias + "\": " + repositoryUrl);

			String repositoryOriginDirectory = originStorageDirectory + "/" + repositoryAlias + "." + (db.getRepositoryCloneCount(repositoryAlias) + 1);
			
			// Create directory to clone repository in
			File userDir = new File(repositoryOriginDirectory);
			userDir.mkdir();
			FileUtils.cleanDirectory(userDir); 
			
			// Clone repository
			executeCommand("git clone " + repositoryUrl + " " + repositoryOriginDirectory);
			
			System.out.println("[OriginUpdater] Reading repository \"" + repositoryAlias + "\"");
			// Read repository information like we would normally
			RepositoryReader repositoryReader = new RepositoryReader(repositoryOriginDirectory);
			JSONObject updateObject = repositoryReader.getUpdateObject();
			updateObject.put("repositoryAlias", repositoryAlias);
			String inputJsonString = updateObject.toString();
			System.out.println("[OriginUpdater] JSON string: " + inputJsonString);
			
			// Inserting into database
			System.out.println("[OriginUpdater] Doing database stuff");
			db.startTransaction();
			
			// Create database user if it doesn't exist
			// This can throw an exception if the user already exists
			try { db.addUserToRepository("origin", repositoryAlias); }
			catch (Exception e) { }

			db.setEntireUserGitState(inputJsonString, "origin");
			db.commitTransaction();
			
			db.incRepositoryCloneCount(repositoryAlias);
			db.commitTransaction();
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
	
	public static void executeCommand(String consoleInput) throws Exception {
		System.out.println("[OriginUpdater] Executing: " + consoleInput);
		Process p = Runtime.getRuntime().exec(consoleInput);
		p.waitFor();

		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println("[OriginUpdater] Console: " + line);
		}
	}
	
}
