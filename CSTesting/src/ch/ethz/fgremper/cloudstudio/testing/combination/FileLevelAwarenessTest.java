package ch.ethz.fgremper.cloudstudio.testing.combination;

import static org.junit.Assert.assertEquals;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import ch.ethz.fgremper.cloudstudio.server.DatabaseConnection;
import ch.ethz.fgremper.cloudstudio.testing.helper.TestGitHelper;

/**
 * 
 * Combination test for file awareness for a given scenario
 * 
 * @author Fabian Gremper
 *
 */
public class FileLevelAwarenessTest {

	/**
	 * 
	 * Helper function to find an item in a JSONObject
	 * 
	 */
	public JSONObject findItem(JSONArray array, String key, String value) throws Exception {
		for (int i = 0; i < array.length(); i++) {
			if (array.getJSONObject(i).getString(key).equals(value)) return array.getJSONObject(i);
		}
		return null;
	}

	/**
	 * 
	 * Test branch internal file conflicts
	 *
	 */
	@Test
	public void testBranchInternal() throws Exception {

		// Database connection
		DatabaseConnection db = new DatabaseConnection();
		db.getConnection();

		// Objects
		JSONObject responseObject;
		JSONArray filesArray;
		JSONObject fileObject;
		
		// Setup default state
		TestGitHelper.setupTest();
		TestGitHelper.runPlugins();

		// Test default state, uncommitted
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "master", "master", true, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Test default state, committed
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "master", "master", false, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// David makes some changes
		TestGitHelper.createOrModifyFile("David", "default.txt");
		TestGitHelper.runPlugins();

		// Test, uncommitted
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "master", "master", true, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Test, committed
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "master", "master", false, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// David commits
		TestGitHelper.commit("David");
		TestGitHelper.runPlugins();

		// Test, uncommitted
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "master", "master", true, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Test, committed
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "master", "master", false, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// David pushes
		TestGitHelper.push("David");
		TestGitHelper.runPlugins();

		// Test, uncommitted
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "master", "master", true, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Test, committed
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "master", "master", false, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// John pulls
		TestGitHelper.pull("John");
		TestGitHelper.runPlugins();

		// Test, uncommitted
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "master", "master", true, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Test, committed
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "master", "master", false, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		db.closeConnection();

	}

	/**
	 * 
	 * Testing comparison to another branch
	 *
	 */
	@Test
	public void testBranchCompare() throws Exception {

		// Database connection
		DatabaseConnection db = new DatabaseConnection();
		db.getConnection();

		// Objects
		JSONObject responseObject;
		JSONArray filesArray;
		JSONObject fileObject;
		
		// Setup default state
		TestGitHelper.setupTest();
		TestGitHelper.createBranch("John", "new_branch");
		TestGitHelper.checkoutBranch("John", "new_branch");
		TestGitHelper.push("John");
		TestGitHelper.pull("David");
		TestGitHelper.pull("Isabelle");
		TestGitHelper.runPlugins();

		// Test default state, uncommitted
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "master", true, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Test default state, committed
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "master", false, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// David modifies a file in master
		// John is in new_branch
		TestGitHelper.createOrModifyFile("David", "default.txt");
		TestGitHelper.runPlugins();
		
		// Branch internal tests
		
		// Test, uncommitted
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "new_branch", true, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Test, committed
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "new_branch", false, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// But conflicts against master
		
		// Test, uncommitted
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "master", true, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Test, committed
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "master", false, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Commit for David
		TestGitHelper.commit("David");
		TestGitHelper.runPlugins();
		
		// Branch internal conflicts
		
		// Test, uncommitted
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "new_branch", true, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Test, committed
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "new_branch", false, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Conflicts against master
		
		// Test, uncommitted
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "master", true, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Test, committed
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "master", false, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Push for David
		TestGitHelper.push("David");
		TestGitHelper.runPlugins();

		// Branch internal conflicts
		
		// Test, uncommitted
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "new_branch", true, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Test, committed
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "new_branch", false, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Conflicts against master
		
		// Test, uncommitted
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "master", true, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Test, committed
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "master", false, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// John checks out master, pulls, and merges the master changes into new_branch
		TestGitHelper.checkoutBranch("John", "master");
		TestGitHelper.pull("John");
		TestGitHelper.checkoutBranch("John", "new_branch");
		TestGitHelper.merge("John", "master");
		TestGitHelper.runPlugins();

		// Branch internal conflicts
		
		// Test, uncommitted
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "new_branch", true, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Test, committed
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "new_branch", false, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Conflicts against master
		
		// Test, uncommitted
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "master", true, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		// Test, committed
		responseObject = db.getFileLevelAwareness("TestRepository", "John", "new_branch", "master", false, false);
		filesArray = responseObject.getJSONArray("files");
		assertEquals(1, filesArray.length());
		fileObject = filesArray.getJSONObject(0);
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "John").getString("type"));
		assertEquals("NO_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "David").getString("type"));
		assertEquals("FILE_CONFLICT", findItem(fileObject.getJSONArray("users"), "username", "Isabelle").getString("type"));

		db.closeConnection();

	}

}
