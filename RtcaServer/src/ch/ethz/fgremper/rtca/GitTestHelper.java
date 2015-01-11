package ch.ethz.fgremper.rtca;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;

public class GitTestHelper {

	public static void clearSandpit() throws Exception {
		FileUtils.cleanDirectory(new File(TestSettings.SANDPIT_DIRECTORY_PATH)); 
	}
	
	public static void createOrigin() throws Exception {
		System.out.println("[TestGitHelper] Creating origin");

		String user = "origin";
		
		// make folder
		File userDir = new File(TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user);
		userDir.mkdir();
		
		// git init
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + " init");

		// create default file
		FileUtils.writeStringToFile(new File(TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + "/default.txt"), "default content");
		
		// git add
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + " add default.txt");

		// git commit
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + " commit -m first_commit");
		
		// make bare repo
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + " config --bool core.bare true");

		// delete file
		new File(TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + "/default.txt").delete();
	}
	
	public static void createUser(String user) throws Exception {
		System.out.println("[TestGitHelper] Creating user directory: " + user);
		File userDir = new File(TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user);
		userDir.mkdir();	
	}
	
	public static void cloneOrigin(String user) throws Exception {
		System.out.println("[TestGitHelper] Clone from origin for user " + user);
		executeCommand("git clone " + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + "origin " + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user);
	}
	
	public static void createOrModifyFile(String user, String filename) throws Exception {
		String content = randomString();
		System.out.println("[TestGitHelper] Creating file for user '" + user + "': " + filename);
		FileUtils.writeStringToFile(new File(TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + "/" + filename), content);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + " add " + filename);
	}

	public static void deleteFile(String user, String filename) throws Exception {
		System.out.println("[TestGitHelper] Deleting file for user '" + user + "': " + filename);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + " rm -f " + filename);
	}

	public static void commit(String user) throws Exception {
		System.out.println("[TestGitHelper] Committing user " + user);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + " commit -a -m " + user);
	}

	public static void pull(String user) throws Exception {
		System.out.println("[TestGitHelper] Pulling as user " + user);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + " pull");
	}
	
	public static void push(String user) throws Exception {
		System.out.println("[TestGitHelper] Pushing as user " + user);
		executeCommand("git --git-dir=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + "/.git" + " --work-tree=" + TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user + " push --all");
	}
	
	public static String randomString() {
		return "this is not yet a random string.";
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
}
