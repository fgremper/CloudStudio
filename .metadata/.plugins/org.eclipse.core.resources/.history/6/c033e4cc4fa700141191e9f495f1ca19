package ch.ethz.fgremper.rtca;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

public class ServerMain {

	static int port = 7330;

	public static void main(String[] args) throws Exception {
		DatabaseConnection db = new DatabaseConnection();
		db.startTransaction();
		db.addUser("newadmin", "1234");
		db.commitTransaction();
		
		System.out.println("[Main] Starting HTTP server on port " + port + "...");

		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/webinterface", new WebInterfaceHttpHandler());
		server.createContext("/update", new UpdateHttpHandler());
		server.createContext("/pull", new RequestHttpHandler());
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		System.out.println("[Main] Server up!");
	}

}
