package ch.ca.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {

	String username = "username";
	// my repo path
	// repo name!
	
	public static void main(String[] args) throws Exception {
		String gitRepository = "C:/gitrepos/test";
		uploadRepository(gitRepository);
	}
	
	public static void uploadRepository(String repositoryPath) throws Exception {
		uploadFilesFromConsoleOutput("git --git-dir=" + repositoryPath + "/.git" + " --work-tree=" + repositoryPath + " diff --name-only HEAD", repositoryPath);
		uploadFilesFromConsoleOutput("git --git-dir=" + repositoryPath + "/.git" + " --work-tree=" + repositoryPath + " ls-files --others --exclude-standard", repositoryPath);
	}
	
	public static void uploadFilesFromConsoleOutput(String consoleInput, String repositoryPath) throws Exception {
		Process p = Runtime.getRuntime().exec(consoleInput);
	    p.waitFor();

	    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

	    String line;
	    while ((line = reader.readLine()) != null) {
	    	File file = new File(repositoryPath, line);
	    	uploadFileIfModified(file);
	    }
	}
	
	public static void uploadFileIfModified(File file) throws Exception {
		// check for modified timestamp (local db!)
    	System.out.println("Uploading: " + file.toString());
	}
	
	public static void uploadFile(File file) throws Exception {
		String url = "http://127.0.0.1:8080/repo/path/file.txt";
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		con.setRequestMethod("PUT");
		// con.setRequestProperty("User-Agent", "CALocalPlugin");
		con.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
		out.write("JSON content! :)");
		out.close();
		
		int responseCode = con.getResponseCode();
		
		/*
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString()); */
	}
	
}
