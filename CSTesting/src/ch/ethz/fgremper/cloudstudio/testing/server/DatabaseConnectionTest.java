package ch.ethz.fgremper.cloudstudio.testing.server;

import static org.junit.Assert.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import ch.ethz.fgremper.cloudstudio.server.DatabaseConnection;
import ch.ethz.fgremper.cloudstudio.server.ServerConfig;

public class DatabaseConnectionTest {

	@Test
	public void test() throws Exception {
		DatabaseConnection db = new DatabaseConnection();
		db.getConnection();
		db.resetDatabase();
		db.addUser("John", "burgers");
		JSONObject users = db.getUsers();
		JSONArray userList = users.getJSONArray("users");
		assertEquals(1, userList.length());
		assertEquals(false, userList.getJSONObject(0).getBoolean("isAdmin"));
		assertEquals(ServerConfig.getInstance().giveCreatorPrivilegesOnSignUp, userList.getJSONObject(0).getBoolean("isCreator"));
		db.makeUserAdmin("John");
		users = db.getUsers();
		userList = users.getJSONArray("users");
		assertEquals(1, userList.length());
		assertEquals(true, userList.getJSONObject(0).getBoolean("isAdmin"));
		db.revokeUserAdmin("John");
		users = db.getUsers();
		userList = users.getJSONArray("users");
		assertEquals(1, userList.length());
		assertEquals(false, userList.getJSONObject(0).getBoolean("isAdmin"));
		db.makeUserCreator("John");
		users = db.getUsers();
		userList = users.getJSONArray("users");
		assertEquals(1, userList.length());
		assertEquals(true, userList.getJSONObject(0).getBoolean("isCreator"));
		db.revokeUserCreator("John");
		users = db.getUsers();
		userList = users.getJSONArray("users");
		assertEquals(1, userList.length());
		assertEquals(false, userList.getJSONObject(0).getBoolean("isCreator"));
		
	}

}