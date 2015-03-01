package ch.ethz.fgremper.rtca;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import JLibDiff.Diff3;
import JLibDiff.Hunk3;

import com.sun.net.httpserver.HttpServer;

import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffAlgorithm;
import difflib.DiffUtils;
import difflib.Patch;

public class ServerMain {

	static int port = 7330;

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

	public static List<String> fileToLines(String filename) {
        List<String> lines = new LinkedList<String>();
        String line = "";
        BufferedReader in = null;
        try {
                in = new BufferedReader(new InputStreamReader(
                        new FileInputStream(filename), "UTF8"));
                while ((line = in.readLine()) != null) {
                        lines.add(line);
                }
        } catch (IOException e) {
                e.printStackTrace();
        } finally {
                if (in != null) {
                        try {
                                in.close();
                        } catch (IOException e) {
                                // ignore ... any errors should already have been
                                // reported via an IOException from the final flush.
                        }
                }
        }
        return lines;
	}
	
	public static void main(String[] args) throws Exception {

		Vector<List<String>> fileContent = new Vector<List<String>>(3);
		fileContent.set(0, fileToLines("one.txt"));
		fileContent.set(1, fileToLines("two.txt"));
		fileContent.set(2, fileToLines("three.txt"));

		Vector<List<String>> fileContentType = new Vector<List<String>>(3);
	    for (int i = 0; i < 3; i++) {
			fileContent.set(i, new LinkedList<String>());
	    	for (int j = 0; j < fileContent.get(i).size(); j++) {
	    		fileContentType.get(i).add("unchanged");
	    	}
	    }
	    
	    
	    
	    
		Process p = Runtime.getRuntime().exec("diff3 one.txt two.txt three.txt");
		p.waitFor();

		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line;
		
		Pattern pattern = Pattern.compile("([1-3]):([0-9]+)(,([0-9]+))?([ac])");

		Vector<Integer> fileStart = new Vector<Integer>(3);
		Vector<Integer> fileEnd = new Vector<Integer>(3);
		Vector<String> fileType = new Vector<String>(3);
		int maxLength = 0;
		
		while ((line = reader.readLine()) != null) {
			Matcher m = pattern.matcher(line);
			if (m.matches()) {
				int fileNum = Integer.parseInt(m.group(1));
				
				if (fileNum == 1) {
					maxLength = 0;
				}
				
				fileStart.set(fileNum, Integer.parseInt(m.group(2)));
				fileEnd.set(fileNum, m.group(4) != null ? Integer.parseInt(m.group(4)) : Integer.parseInt(m.group(2)));
				maxLength = Math.max(maxLength, (m.group(4) != null ? (Integer.parseInt(m.group(4)) - Integer.parseInt(m.group(2)) + 1) : 1));
				fileType.set(fileNum, m.group(5));
				

				if (fileNum == 3) {
					for (int i = 1; i <= 3; i++) {
						if (fileType.get(i) == "c") {
							for (int j = fileStart.get(i); j < fileEnd.get(i); j++) {
								fileContentType.get(i).set(j, "modified");
							}
						}
					}
				}
				
				//System.out.println(m.groupCount());
				//System.out.println("[TestGitHelper] Console: " + line);
				//System.out.println("[TestGitHelper]  >> " + fileNum + " " + fileStart + " " + fileEnd + " " + fileType);
			}
		}

	    
		for (int i = 1; i <= 3; i++) {
			System.out.println("File " + i);
			for (int j = 0; j < fileContent.get(i).size(); j++) {
				System.out.println(fileContentType.get(i).get(j) + " " + fileContent.get(i).get(j));
			}
		}
	    
		/*
				// create admin if it doens't exist
		

        try {

    		DatabaseConnection db = new DatabaseConnection();
    		
    		db.startTransaction();
    		db.addUser("admin", "1234");
    		db.makeUserAdmin("admin");
    		db.commitTransaction();
    		
        }
        catch (Exception e) {
        	// nothing
        }
        */
		/*
		
		// Periodically origin updater
		System.out.println("[Main] Starting periodical origin updater");
		
		PeriodicalAllOriginUpdater originUpdaterInterval = new PeriodicalAllOriginUpdater();
		new Thread(originUpdaterInterval).start();

		*/
		
		// HTTP server
		/*
		int port = ServerConfig.getInstance().serverPort;
		
		System.out.println("[Main] Starting HTTP server on port " + port + "...");

		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/webinterface", new WebInterfaceHttpHandler());
		server.createContext("/request", new RequestHttpHandler());
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		System.out.println("[Main] Server up!");
		*/
	}

}
