package ch.ethz.fgremper.cloudstudio.testing.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;

import ch.ethz.fgremper.cloudstudio.client.ClientMain;
import ch.ethz.fgremper.cloudstudio.server.DatabaseConnection;
import ch.ethz.fgremper.cloudstudio.server.PeriodicalAllOriginUpdater;

public class TestGitHelper {

	public static void clearSandpit() throws Exception {
		FileUtils.cleanDirectory(new File(TestSettings.SANDPIT_DIRECTORY_PATH)); 
	}
	
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
	
	public static void createUser(String user) throws Exception {
		System.out.println("[TestGitHelper] Creating user directory: " + user);
		File userDir = new File(TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user);
		userDir.mkdir();	
	}
	
	public static void cloneOrigin(String user) throws Exception {
		System.out.println("[TestGitHelper] Clone from origin for user " + user);
		executeCommand("git clone " + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + "origin " + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user);
	}
	
	public static void createFolder(String user, String folder) {
		new File(TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + File.separator + folder).mkdir();
	}
	
	public static void createOrModifyFile(String user, String filename) throws Exception {
		String content = randomString();
		System.out.println("[TestGitHelper] Creating file for user '" + user + "': " + filename);
		FileUtils.writeStringToFile(new File(TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + File.separator + filename), content);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " add " + filename);
	}

	public static void writeContentToFile(String user, String filename, String content) throws Exception {
		System.out.println("[TestGitHelper] Creating file for user '" + user + "': " + filename);
		FileUtils.writeStringToFile(new File(TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + File.separator + filename), content);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " add " + filename);
	}
	
	public static void deleteFile(String user, String filename) throws Exception {
		System.out.println("[TestGitHelper] Deleting file for user '" + user + "': " + filename);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " rm -f " + filename);
	}

	public static void commit(String user) throws Exception {
		System.out.println("[TestGitHelper] Committing user " + user);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " commit -a -m " + user);
	}

	public static void checkoutBranch(String user, String branchName) throws Exception {
		System.out.println("[TestGitHelper] Committing user " + user);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " checkout " + branchName);
	}

	public static void createBranch(String user, String branchName) throws Exception {
		System.out.println("[TestGitHelper] Committing user " + user);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " branch " + branchName);
	}

	public static void pull(String user) throws Exception {
		System.out.println("[TestGitHelper] Pulling as user " + user);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " pull --all");
	}

	public static void push(String user) throws Exception {
		System.out.println("[TestGitHelper] Pushing as user " + user);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " push --all");
	}

	public static void merge(String user, String branch) throws Exception {
		System.out.println("[TestGitHelper] Merging as user " + user);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + File.separator + user + " merge " + branch);
	}
	
	public static String randomString() {
		return Long.toHexString(Double.doubleToLongBits(Math.random()));
	}

	public static void executeCommand(String consoleInput) throws Exception {
		System.out.println("[TestGitHelper] Executing: " + consoleInput);
		Process p = Runtime.getRuntime().exec(consoleInput);
		p.waitFor();

		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println("[TestGitHelper] Console: " + line);
		}
	}
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
		
		db.createRepository("TestRepository", "/Users/novocaine/Documents/masterthesis/testsandpit/origin", "John", "just testing move along");
		db.addUserToRepository("John", "TestRepository");
		db.addUserToRepository("David", "TestRepository");
		db.addUserToRepository("Isabelle", "TestRepository");

		db.commitTransaction();
		
		db.closeConnection();
	}
	
	public static void runPlugins() throws Exception {


		String[] argsJohn = {"configJohn.xml", "--nogui"};
		String[] argsDavid = {"configDavid.xml", "--nogui"};
		String[] argsIsabelle = {"configIsabelle.xml", "--nogui"};
		
		ClientMain.main(argsJohn);
		ClientMain.main(argsDavid);
		ClientMain.main(argsIsabelle);

		PeriodicalAllOriginUpdater originUpdaterInterval = new PeriodicalAllOriginUpdater();
		originUpdaterInterval.updateAll();
			
	}
}