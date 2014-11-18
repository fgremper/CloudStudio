package ch.ethz.fgremper.rtca;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class LocalMain {

	static LinkedList<String> modifiedFilesList = new LinkedList<String>();
	static String jsonOutputString;

	static String serverUrl;
	static String username;
	static LinkedList<Repository> repositories = new LinkedList<Repository>();

	public static void main(String[] args) {

		/* READ CONFIG */
		// TODO: actually read this from a config file
		serverUrl = "http://127.0.0.1:8080"; // no dash at the end
		username = "fgremper";
		repositories.add(new Repository("test", "C:/GDev/newmtworkspace/testrepo"));

		for (Repository repository : repositories) {

			System.out.println("Active repository: \"" + repository.alias + "\" (" + repository.localPath + ")");

			/* FIND CHANGED FILES */
			try {
				modifiedFilesList.clear();
				findChangedFiles(repository);
			}
			catch (Exception e) {
				System.err.println("Error while reading git changes.");
				e.printStackTrace();
				continue;
			}

			/* CREATE JSON STRING */
			try {
				createJsonOutputString();
			}
			catch (Exception e) {
				System.err.println("Error while creating JSON string.");
				e.printStackTrace();
				continue;
			}

			/* SEND CHANGES TO SERVER */
			try {
				sendChangesToServer(repository);
			}
			catch (Exception e) {
				System.err.println("Error while sending changes to server.");
				e.printStackTrace();
				continue;
			}

		}

	}

	public static void findChangedFiles(Repository repository) throws Exception {
		findChangedFilesFromConsoleInput("git --git-dir=" + repository.localPath + "/.git" + " --work-tree=" + repository.localPath + " diff --name-only HEAD", repository);
		findChangedFilesFromConsoleInput("git --git-dir=" + repository.localPath + "/.git" + " --work-tree=" + repository.localPath + " ls-files --others --exclude-standard", repository);
	}

	public static void findChangedFilesFromConsoleInput(String consoleInput, Repository repository) throws Exception {
		Process p = Runtime.getRuntime().exec(consoleInput);
		p.waitFor();

		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line;
		while ((line = reader.readLine()) != null) {
			String filename = new File(repository.localPath, line).getPath();
			System.out.println("Added changed file: " + filename);
			modifiedFilesList.add(filename);
		}
	}

	public static void createJsonOutputString() throws Exception {
		JSONArray fileArray = new JSONArray();

		while(!modifiedFilesList.isEmpty()) {
			String file = modifiedFilesList.pop();

			// read file content
			FileInputStream inputStream = new FileInputStream(file);
			String fileContent = IOUtils.toString(inputStream, "UTF-8");

			JSONObject fileObject = new JSONObject();
			fileObject.put("filename", file);
			fileObject.put("content", fileContent);
			// TODO: send some sort of file history at some point, so 3-way merges work...

			fileArray.put(fileObject);
		}

		jsonOutputString = fileArray.toString();

		System.out.println("Created JSON string: " + jsonOutputString);
	}

	public static void sendChangesToServer(Repository repository) throws Exception {
		String url = serverUrl + "/" + repository.alias + "/" + username;

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("PUT");
		con.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
		out.write(jsonOutputString);
		out.close();

		int responseCode = con.getResponseCode();

		System.out.println("Response code from server: " + responseCode);
	}

}