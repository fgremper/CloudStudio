package ch.ethz.fgremper.rtca;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DatabaseConnection {

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost/cloudstudio";
	static final String USER = "dbadmin";
	static final String PASS = "";

	Connection con = null;

	public DatabaseConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");

		System.out.println("[DatabaseConnection] Connecting to database...");
		con = DriverManager.getConnection(DB_URL, USER,PASS);
		System.out.println("[DatabaseConnection] Connected to database.");
	}

	public void startTransaction() throws SQLException {
		con.setAutoCommit(false);
	}

	public void commitTransaction() throws SQLException {
		con.commit();
	}

	public void rollbackTransaction() throws SQLException {
		con.rollback();
	}

	public void deleteAllFilesFromRepositoryAndUser(String repositoryAlias, String username) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("DELETE FROM files WHERE repositoryalias = ? AND username = ?");

		stmt.setString(1, repositoryAlias);
		stmt.setString(2, username);

		int rowsAffected = stmt.executeUpdate();

	
		stmt = con.prepareStatement("DELETE FROM commithistory WHERE repositoryalias = ? AND username = ?");

		stmt.setString(1, repositoryAlias);
		stmt.setString(2, username);

		rowsAffected = stmt.executeUpdate();
	}

	public void storeFile(String repositoryAlias, String username, String filename, String sha, String branch, String commit) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("INSERT INTO files (repositoryalias, username, filename, sha, branch, commit) VALUES (?, ?, ?, ?, ?, ?)");

		System.out.println("FILE: " + filename);
		stmt.setString(1, repositoryAlias);
		stmt.setString(2, username);
		stmt.setString(3, filename);
		stmt.setString(4, sha);
		stmt.setString(5, branch);
		stmt.setString(6, commit);

		int rowsAffected = stmt.executeUpdate();
	}
	
	public void storeCommitHistory(String repositoryAlias, String username, String commit, List<String> downstreamCommits) throws SQLException {

		PreparedStatement downstreamCommitInsertStatement = con.prepareStatement("INSERT INTO commithistory (repositoryalias, username, commit, downstreamcommit) VALUES (?, ?, ?, ?)");
		
		downstreamCommitInsertStatement.setString(1, repositoryAlias);
		downstreamCommitInsertStatement.setString(2, username);
		downstreamCommitInsertStatement.setString(3, commit);
		
		for (String downstreamCommit : downstreamCommits) {
			downstreamCommitInsertStatement.setString(4, downstreamCommit);
			downstreamCommitInsertStatement.executeUpdate();
		}
		
	}
	
	public JSONArray getRepositories() throws SQLException {
		JSONArray repositories = new JSONArray();
		
		PreparedStatement stmt = con.prepareStatement("SELECT repositoryalias, repositoryurl FROM repositories");

		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			JSONObject repository = new JSONObject();
			String repositoryAlias = rs.getString("repositoryalias");
			String repositoryUrl = rs.getString("repositoryurl");
			try {
				repository.put("repositoryAlias", repositoryAlias);
				repository.put("repositoryUrl", repositoryUrl);
			} catch (JSONException e) {
				System.err.println("Error while creating JSON string.");
				e.printStackTrace();
			}
			repositories.put(repository);
		}
		
		return repositories;
	}
	
	public JSONArray getFileConflicts() throws SQLException {
		JSONArray fileConflicts = new JSONArray();
		
		/*
		PreparedStatement stmt = con.prepareStatement("select f1.repositoryalias, f1.filename, f1.username as username1, f2.username as username2, f1.branch as branch1, f2.branch as branch2 from files as f1 " +
			"cross join files as f2 " +
			"where f1.repositoryalias = 'testrepo' " +
			"and f1.repositoryalias = f2.repositoryalias " +
			"and f1.filename = f2.filename " +
			"and f1.username < f2.username " +
			"and f1.sha <> f2.sha " +
			"and f1.branch = f2.branch");
		*/
		PreparedStatement stmt = con.prepareStatement("select f1.repositoryalias, f1.filename, f1.username as username1, f2.username as username2, f1.branch as branch1, f2.branch as branch2 from files as f1 " +
				"cross join files as f2 " +
				"where f1.repositoryalias = 'testrepo' " +
				"and f1.repositoryalias = f2.repositoryalias " +
				"and f1.filename = f2.filename " +
				"and f1.username < f2.username " +
				"and f1.sha <> f2.sha " +
				"and f1.branch = f2.branch " +
				"and f1.commit not in (SELECT h1.downstreamcommit FROM commithistory as h1 WHERE h1.commit = f2.commit) " +
				"and f2.commit not in (SELECT h2.downstreamcommit FROM commithistory as h2 WHERE h2.commit = f1.commit)");
		
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			JSONObject repository = new JSONObject();
			String filename = rs.getString("filename");
			String branch = rs.getString("branch1");
			String username1 = rs.getString("username1");
			String username2 = rs.getString("username2");
			try {
				System.out.println(filename);
				repository.put("filename", filename);
				repository.put("branch", branch);
				repository.put("username1", username1);
				repository.put("username2", username2);
			} catch (JSONException e) {
				System.err.println("Error while creating JSON string.");
				e.printStackTrace();
			}
			fileConflicts.put(repository);
		}
		
		return fileConflicts;
	}
	
	public void addRepository(String repositoryAlias, String repositoryUrl) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("INSERT INTO repositories (repositoryalias, repositoryurl) VALUES (?, ?)");

		stmt.setString(1, repositoryAlias);
		stmt.setString(2, repositoryUrl);
		
		int rowsAffected = stmt.executeUpdate();
	}
	
	public void resetDatabase() throws SQLException {
		PreparedStatement stmt = con.prepareStatement("TRUNCATE TABLE files");
		stmt.executeUpdate();
		stmt = con.prepareStatement("TRUNCATE TABLE repositories");
		stmt.executeUpdate();
		stmt = con.prepareStatement("TRUNCATE TABLE commithistory");
		stmt.executeUpdate();
	}
		
}
