package ch.ethz.fgremper.rtca;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost/cloudstudio";

	//  Database credentials
	static final String USER = "dbadmin";
	static final String PASS = "1234";

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
		PreparedStatement stmt = con.prepareStatement("DELTE FROM files WHERE repositoryalias = ? AND username = ?");

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
}
