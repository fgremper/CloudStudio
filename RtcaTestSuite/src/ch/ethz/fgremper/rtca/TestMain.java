package ch.ethz.fgremper.rtca;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;

public class TestMain {

	/*
	 *  SANDPIT DIRECTORY:
	 *  The sandpit directory is one directory up from the workspace, so there are no
	 *  nested git repositories. You have to create the directory before you run this.
	 */
	
	static String sandpitDir = "../../testsandpit/";
	static String testcaseDir = "testcases";
	
	public static void main(String[] args) throws Exception {
		readAllFilesFromTestcaseDirectory();
	}
	
	public static void readAllFilesFromTestcaseDirectory() throws Exception {
		File folder = new File("testcases");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				System.out.println("Executing testcase: " + listOfFiles[i].getName());
				clearSandpit();
				executeTestcase(listOfFiles[i]);
			}
		}
	}
	
	public static void executeTestcase(File file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.equals("")) continue;
			
			System.out.println("Line: " + line);
			String[] tokens = line.split(" ");

			// one argument commands
			if (tokens.length < 1) continue;
			String command = tokens[0];
			if (command.equalsIgnoreCase("create_origin")) createOrigin();
			
			// two argument commands
			if (tokens.length < 2) continue;
			String user = tokens[1];
			if (command.equalsIgnoreCase("create_user")) createUser(user);
			if (command.equalsIgnoreCase("clone_origin")) cloneOrigin(user);
			if (command.equalsIgnoreCase("commit")) commit(user);
			if (command.equalsIgnoreCase("push")) push(user);
			if (command.equalsIgnoreCase("pull")) pull(user);
			
			// three argument commands
			if (tokens.length < 3) continue;
			String arg1 = tokens[2];
			if (command.equalsIgnoreCase("create_or_modify_file")) createOrModifyFile(user, arg1);
			
		}
		br.close();
	}
	
	public static void clearSandpit() throws Exception {
		FileUtils.cleanDirectory(new File(sandpitDir)); 
	}
	
	public static void createOrigin() throws Exception {
		System.out.println("Creating origin");

		String user = "origin";
		
		// make folder
		File userDir = new File(sandpitDir + user);
		userDir.mkdir();
		
		// git init
		executeCommand("git --git-dir=" + sandpitDir + user + "/.git" + " --work-tree=" + sandpitDir + user + " init");

		// create default file
		FileUtils.writeStringToFile(new File(sandpitDir + user + "/default.txt"), "default content");
		
		// git add
		executeCommand("git --git-dir=" + sandpitDir + user + "/.git" + " --work-tree=" + sandpitDir + user + " add default.txt");

		// git commit
		executeCommand("git --git-dir=" + sandpitDir + user + "/.git" + " --work-tree=" + sandpitDir + user + " commit -m first_commit");
		
		// make bare repo
		executeCommand("git --git-dir=" + sandpitDir + user + "/.git" + " --work-tree=" + sandpitDir + user + " config --bool core.bare true");

		// delete file
		new File(sandpitDir + user + "/default.txt").delete();
	}
	
	public static void createUser(String user) throws Exception {
		System.out.println("Creating user directory: " + user);
		File userDir = new File(sandpitDir + user);
		userDir.mkdir();	
	}
	
	public static void cloneOrigin(String user) throws Exception {
		System.out.println("Clone from origin for user " + user);
		executeCommand("git clone " + sandpitDir + "origin " + sandpitDir + user);
	}
	
	public static void createOrModifyFile(String user, String filename) throws Exception {
		String content = randomString();
		System.out.println("Creating file for user '" + user + "': " + filename);
		FileUtils.writeStringToFile(new File(sandpitDir + user + "/" + filename), content);
		executeCommand("git --git-dir=" + sandpitDir + user + "/.git" + " --work-tree=" + sandpitDir + user + " add " + filename);
	}

	public static void deleteFile(String user, String filename) throws Exception {
		System.out.println("Deleting file for user '" + user + "': " + filename);
		executeCommand("git --git-dir=" + sandpitDir + user + "/.git" + " --work-tree=" + sandpitDir + user + " rm -f " + filename);
	}

	public static void commit(String user) throws Exception {
		System.out.println("Committing user " + user);
		executeCommand("git --git-dir=" + sandpitDir + user + "/.git" + " --work-tree=" + sandpitDir + user + " commit -a -m " + user);
	}

	public static void pull(String user) throws Exception {
		System.out.println("Pulling as user " + user);
		executeCommand("git --git-dir=" + sandpitDir + user + "/.git" + " --work-tree=" + sandpitDir + user + " pull");
	}
	
	public static void push(String user) throws Exception {
		System.out.println("Pushing as user " + user);
		executeCommand("git --git-dir=" + sandpitDir + user + "/.git" + " --work-tree=" + sandpitDir + user + " push --all");
	}
	
	public static String randomString() {
		return "this is not yet a random string.";
	}

	public static void executeCommand(String consoleInput) throws Exception {
		System.out.println("Executing: " + consoleInput);
		Process p = Runtime.getRuntime().exec(consoleInput);
		p.waitFor();

		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println("Console: " + line);
		}
	}
}

