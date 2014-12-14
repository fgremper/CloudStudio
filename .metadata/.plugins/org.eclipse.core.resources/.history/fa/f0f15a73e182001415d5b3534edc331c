package ch.ethz.fgremper.rtca;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DatabaseConnection {

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost/cloudstudio";

	//  Database credentials
	static final String USER = "dbadmin";
	static final String PASS = "";

	Connection con = null;

	public DatabaseConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");

		System.out.println("Connecting to database...");
		con = DriverManager.getConnection(DB_URL, USER,PASS);
		System.out.println("Connected to database.");
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
	}

	public void storeFile(String repositoryAlias, String username, String filename, String sha) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("INSERT INTO files (repositoryalias, username, filename, sha) VALUES (?, ?, ?, ?)");

		stmt.setString(1, repositoryAlias);
		stmt.setString(2, username);
		stmt.setString(3, filename);
		stmt.setString(4, sha);

		int rowsAffected = stmt.executeUpdate();
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
	
	public void addRepository(String repositoryAlias, String repositoryUrl) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("INSERT INTO repositories (repositoryalias, repositoryurl) VALUES (?, ?)");

		stmt.setString(1, repositoryAlias);
		stmt.setString(2, repositoryUrl);
		
		int rowsAffected = stmt.executeUpdate();
	}
		
}
