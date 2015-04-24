package ch.ethz.fgremper.cloudstudio.testing.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import ch.ethz.fgremper.cloudstudio.common.ProcessWithTimeout;
import ch.ethz.fgremper.cloudstudio.common.RepositoryReader;
import ch.ethz.fgremper.cloudstudio.server.DatabaseConnection;
import ch.ethz.fgremper.cloudstudio.server.OriginUpdater;

/**
 * 
 * Helps setting up the test environment and do actions with our test characters
 * 
 * @author Fabian Gremper
 *
 */
public class TestGitHelper {

	/**
	 * 
	 * Clear sandpit
	 *
	 */
	public static void clearSandpit() throws Exception {
		FileUtils.cleanDirectory(new File(TestSettings.SANDPIT_DIRECTORY_PATH)); 
	}
	
	/**
	 * 
	 * Create origin
	 *
	 */
	public static void createOrigin() throws Exception {
		
		System.out.println("[TestGitHelper] Creating origin");

		String user = "origin";
		
		// make folder
		File userDir = new File(TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user);
		userDir.mkdir();
		
		// git init
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " init");

		// create default file
		FileUtils.writeStringToFile(new File(TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/default.txt"), "default content");
		
		// git add
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " add default.txt");

		// git commit
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " commit -m first_commit");
		
		// make bare repo
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " config --bool core.bare true");

		// delete file
		new File(TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/default.txt").delete();
	
	}
	
	/**
	 * 
	 * Create user
	 *
	 */
	public static void createUser(String user) throws Exception {
		System.out.println("[TestGitHelper] Creating user directory: " + user);
		File userDir = new File(TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user);
		userDir.mkdir();	
	}
	
	/**
	 * 
	 * Clone origin for user
	 *
	 */
	public static void cloneOrigin(String user) throws Exception {
		System.out.println("[TestGitHelper] Clone from origin for user " + user);
		executeCommand("git clone " + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + "origin " + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user);
	}
	
	/**
	 * 
	 * Create folder
	 *
	 */
	public static void createFolder(String user, String folder) {
		new File(TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + File.separator + folder).mkdir();
	}
	
	/**
	 * 
	 * Write random string to file
	 *
	 */
	public static void createOrModifyFile(String user, String filename) throws Exception {
		String content = randomString();
		System.out.println("[TestGitHelper] Creating file for user '" + user + "': " + filename);
		FileUtils.writeStringToFile(new File(TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + File.separator + filename), content);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " add " + filename);
	}

	/**
	 * 
	 * Write content to file
	 *
	 */
	public static void writeContentToFile(String user, String filename, String content) throws Exception {
		System.out.println("[TestGitHelper] Creating file for user '" + user + "': " + filename);
		FileUtils.writeStringToFile(new File(TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + File.separator + filename), content);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " add " + filename);
	}
	
	/**
	 * 
	 * Delete a file
	 *
	 */
	public static void deleteFile(String user, String filename) throws Exception {
		System.out.println("[TestGitHelper] Deleting file for user '" + user + "': " + filename);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " rm -f " + filename);
	}

	/**
	 * 
	 * Do a git commit
	 *
	 */
	public static void commit(String user) throws Exception {
		System.out.println("[TestGitHelper] Committing user " + user);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " commit -a -m " + user);
	}

	/**
	 * 
	 * Checkout a branch in git
	 *
	 */
	public static void checkoutBranch(String user, String branchName) throws Exception {
		System.out.println("[TestGitHelper] Committing user " + user);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " checkout " + branchName);
	}

	/**
	 * 
	 * Create a new branch in git
	 *
	 */
	public static void createBranch(String user, String branchName) throws Exception {
		System.out.println("[TestGitHelper] Committing user " + user);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " branch " + branchName);
	}

	/**
	 * 
	 * Pull from origin
	 *
	 */
	public static void pull(String user) throws Exception {
		System.out.println("[TestGitHelper] Pulling as user " + user);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " pull --all");
	}

	/**
	 * 
	 * Push to origin
	 *
	 */
	public static void push(String user) throws Exception {
		System.out.println("[TestGitHelper] Pushing as user " + user);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " push --all");
	}

	/**
	 * 
	 * Merge in branch
	 *
	 */
	public static void merge(String user, String branch) throws Exception {
		System.out.println("[TestGitHelper] Merging as user " + user);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " merge " + branch);
	}
	
	/**
	 * 
	 * Returns a random string
	 *
	 */
	public static String randomString() {
		return Long.toHexString(Double.doubleToLongBits(Math.random()));
	}

	/**
	 * 
	 * Execute a command on the command line
	 *
	 */
	public static void executeCommand(String consoleInput) throws Exception {
		System.out.println("[TestGitHelper] Executing: " + consoleInput);
		Process p = Runtime.getRuntime().exec(consoleInput);
		ProcessWithTimeout processWithTimeout = new ProcessWithTimeout(p);
		processWithTimeout.waitForProcess(5000);

		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println("[TestGitHelper] Console: " + line);
		}
	}
	
	/**
	 * 
	 * Setup a scenario that is used as a base for most tests
	 *
	 */
	public static void setupTest() throws Exception {
		
		clearSandpit();
		
		createOrigin();
		
		createUser("John");
		cloneOrigin("John");
		createUser("David");
		cloneOrigin("David");
		createUser("Isabelle");
		cloneOrigin("Isabelle");
		
		DatabaseConnection db = new DatabaseConnection();
		db.getConnection();
		
		db.startTransaction();
		db.resetDatabase();
		db.addUser("Admin", "1234");
		db.makeUserAdmin("admin");
		db.addUser("John", "johnpw");
		db.addUser("David", "davidpw");
		db.addUser("Isabelle", "isabellepw");
		
		db.createRepository("TestRepository", TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + "origin", "John", "Verifying CloudStudio correctness");
		db.addUserToRepository("John", "TestRepository");
		db.addUserToRepository("David", "TestRepository");
		db.addUserToRepository("Isabelle", "TestRepository");

		db.commitTransaction();
		
		db.closeConnection();
		
	}
	
	/**
	 * 
	 * Simulate running all plugins to submit information to CloudStudio
	 * 
	 */
	public static void runPlugins() throws Exception {
		
		List<String> users = new LinkedList<String>();
		users.add("John");
		users.add("David");
		users.add("Isabelle");
		
		DatabaseConnection db = new DatabaseConnection();
		db.getConnection();

		db.startTransaction();
		for (String user : users) {
			RepositoryReader repositoryReader = new RepositoryReader(TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user);
            db.setEntireUserGitState(repositoryReader.getUpdateObject().toString(), user, "TestRepository");
		}
		db.commitTransaction();

		db.closeConnection();
		
		OriginUpdater.update("TestRepository", TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + "origin");

	}
	
}
