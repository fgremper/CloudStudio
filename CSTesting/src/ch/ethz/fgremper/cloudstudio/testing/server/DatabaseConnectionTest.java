package ch.ethz.fgremper.cloudstudio.testing.server;

import static org.junit.Assert.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import ch.ethz.fgremper.cloudstudio.server.DatabaseConnection;
import ch.ethz.fgremper.cloudstudio.server.ServerConfig;

/**
 * 
 * Class test for Database Connection
 * 
 * @author Fabian Gremper
 *
 */
public class DatabaseConnectionTest {

	/**
	 * 
	 * Test user related database queries
	 *
	 */
	@Test
	public void testUsers() throws Exception {
		
		DatabaseConnection db = new DatabaseConnection();
		
		try {
			
			// Get connection
			db.getConnection();
			
			// Reset
			db.resetDatabase();
			
			// Test get user
			db.addUser("John", "johnpw");
			JSONObject users = db.getUsers();
			JSONArray userList = users.getJSONArray("users");
			assertEquals(1, userList.length());
			assertEquals(false, userList.getJSONObject(0).getBoolean("isAdmin"));
			assertEquals(ServerConfig.getInstance().giveCreatorPrivilegesOnSignUp, userList.getJSONObject(0).getBoolean("isCreator"));
			
			// Test make user admin
			db.makeUserAdmin("John");
			users = db.getUsers();
			userList = users.getJSONArray("users");
			assertEquals(1, userList.length());
			assertEquals(true, userList.getJSONObject(0).getBoolean("isAdmin"));
			
			// Test revoke user admin
			db.revokeUserAdmin("John");
			users = db.getUsers();
			userList = users.getJSONArray("users");
			assertEquals(1, userList.length());
			assertEquals(false, userList.getJSONObject(0).getBoolean("isAdmin"));
			
			// Test make user creator
			db.makeUserCreator("John");
			users = db.getUsers();
			userList = users.getJSONArray("users");
			assertEquals(1, userList.length());
			assertEquals(true, userList.getJSONObject(0).getBoolean("isCreator"));
			
			// Test revoke user creator
			db.revokeUserCreator("John");
			users = db.getUsers();
			userList = users.getJSONArray("users");
			assertEquals(1, userList.length());
			assertEquals(false, userList.getJSONObject(0).getBoolean("isCreator"));

			// Test change password
			assertEquals("John", db.getUsernameForCorrectLogin("John", "johnpw"));
			assertEquals(null, db.getUsernameForCorrectLogin("John", "wrongpw"));
			db.changePassword("John", "newjohnpw");
			assertEquals(null, db.getUsernameForCorrectLogin("John", "johnpw"));
			assertEquals("John", db.getUsernameForCorrectLogin("John", "newjohnpw"));
			db.deleteUser("John");
			assertEquals(null, db.getUsernameForCorrectLogin("John", "johnpw"));
			
		}
		finally {
			db.closeConnection();
		}
		
	}

	/**
	 * 
	 * Test repository related database queries
	 *
	 */
	@Test
	public void testRepositories() throws Exception {
		
		DatabaseConnection db = new DatabaseConnection();
		
		try {
			
			// Get database connection
			db.getConnection();
			
			// Reset
			db.resetDatabase();
			
			// Create users
			db.addUser("John", "johnpw");
			db.addUser("David", "davidpw");
			JSONObject repositories = db.getRepositories("John");
			assertEquals(0, repositories.getJSONArray("repositories").length());
			
			// Test create repository
			db.createRepository("TestRepository", "TestURL", "John", "TestDescription");
			repositories = db.getRepositories("John");
			assertEquals(1, repositories.getJSONArray("repositories").length());
			assertEquals(true, db.isUserRepositoryOwner("John", "TestRepository"));
			assertEquals(false, db.isUserRepositoryOwner("David", "TestRepository"));
			
			// Test modify repository owner
			db.modifyRepositoryOwner("TestRepository", "David");
			assertEquals(false, db.isUserRepositoryOwner("John", "TestRepository"));
			assertEquals(true, db.isUserRepositoryOwner("David", "TestRepository"));
			
			// Test get repository information
			JSONObject repositoryInformation = db.getRepositoryInformation("TestRepository");
			assertEquals("TestDescription", repositoryInformation.getString("repositoryDescription"));
			assertEquals("TestURL", repositoryInformation.getString("repositoryUrl"));
			assertEquals("David", repositoryInformation.getString("repositoryOwner"));
			
			// Test update repository information
			db.updateRepositoryInformation("TestRepository", "NewDescription", "NewURL");
			repositoryInformation = db.getRepositoryInformation("TestRepository");
			assertEquals("NewDescription", repositoryInformation.getString("repositoryDescription"));
			assertEquals("NewURL", repositoryInformation.getString("repositoryUrl"));

			// Test add user to repository
			db.addUserToRepository("David", "TestRepository");
			repositoryInformation = db.getRepositoryInformation("TestRepository");
			assertEquals(1, repositoryInformation.getJSONArray("repositoryUsers").length());
			
			// Test delete user from repository
			db.deleteUserFromRepository("David", "TestRepository");
			repositoryInformation = db.getRepositoryInformation("TestRepository");
			assertEquals(0, repositoryInformation.getJSONArray("repositoryUsers").length());
			
		}
		finally {
			db.closeConnection();
		}
	}

}
