package ch.ca.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;


public class Main {

	public static void main(String[] args) throws Exception {
		System.out.println("Starting HTTP server...");
		
		HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/", new CAHttpHandler()); 
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		System.out.println("Server up!");
	}
	
	   
}

