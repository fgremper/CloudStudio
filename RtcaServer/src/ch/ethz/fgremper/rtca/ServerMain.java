package ch.ethz.fgremper.rtca;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;

import jlibdiff.Diff;
import jlibdiff.Diff3;
import jlibdiff.Hunk;
import jlibdiff.Hunk3;

import com.sun.net.httpserver.HttpServer;

import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffAlgorithm;
import difflib.DiffUtils;
import difflib.Patch;

public class ServerMain {

	static int port = 7330;
	
	public static void main(String[] args) throws Exception {

		Diff3 diff3 = new Diff3();
		
		diff3.diffBuffer(new BufferedReader(new FileReader("one.txt")), new BufferedReader(new FileReader("three.txt")), new BufferedReader(new FileReader("two.txt")));
		diff3.save("merge.txt");
		Vector<Hunk3> vector = diff3.getHunk3();

		for (Hunk3 hunk3 : vector) {
		    //System.out.println(">>>>>>>>>>>>>>> " + hunk3.convert());
		    System.out.println("C: " + hunk3.getType());
		    System.out.println("1:" + hunk3.lowLine(0) + "," + hunk3.highLine(0) + " lines " + hunk3.numLines(0));
		    System.out.println("2:" + hunk3.lowLine(1) + "," + hunk3.highLine(1) + " lines " + hunk3.numLines(1));
		    System.out.println("3:" + hunk3.lowLine(2) + "," + hunk3.highLine(2) + " lines " + hunk3.numLines(2));
		    
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
