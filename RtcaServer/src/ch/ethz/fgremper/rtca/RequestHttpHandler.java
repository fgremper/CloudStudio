package ch.ethz.fgremper.rtca;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.sql.SQLException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RequestHttpHandler implements HttpHandler {

	public void handle(HttpExchange exchange) throws IOException {
		
		URI uri = exchange.getRequestURI();
		String requestMethod = exchange.getRequestMethod();
		System.out.println("Incoming PULL request: " + requestMethod + " " + uri.getPath());
		
		String response = null;

		if (requestMethod.equalsIgnoreCase("GET") && uri.getPath().equals("/pull/getRepositories")) {
			System.out.println("Incoming GET REPOSITORIES.");
			
			DatabaseConnection db;
			try {
				db = new DatabaseConnection();
				db.startTransaction();
				response = db.getRepositories().toString();
				db.commitTransaction();
			} catch (Exception e) {
				//db.rollbackTransaction();
				System.err.println("Error while handling GET REPOSITORIES.");
				e.printStackTrace();
			}
			
		}
		
		
		if (requestMethod.equalsIgnoreCase("GET") && uri.getPath().startsWith("/pull/getFileConflicts")) {
			System.out.println("Incoming GET FILE CONFLICTS.");
			
			DatabaseConnection db;
			try {
				db = new DatabaseConnection();
				db.startTransaction();
				response = db.getFileConflicts().toString();
				db.commitTransaction();
			} catch (Exception e) {
				//db.rollbackTransaction();
				System.err.println("Error while handling GET FILE CONFLICTS.");
				e.printStackTrace();
			}
			
		}
		
		if (requestMethod.equalsIgnoreCase("POST") && uri.getPath().equals("/pull/addRepository")) {
			System.out.println("Incoming ADD REPOSITORY.");
			
			String jsonString = IOUtils.toString(exchange.getRequestBody(), "UTF-8");
			System.out.println("JSON string: " + jsonString);

			try {
				JSONObject repositoryObject = new JSONObject(jsonString);
				String repositoryAlias = repositoryObject.getString("repositoryAlias");
				String repositoryUrl = repositoryObject.getString("repositoryUrl");

				DatabaseConnection db;
				db = new DatabaseConnection();
				db.startTransaction();
				db.addRepository(repositoryAlias, repositoryUrl);
				db.commitTransaction();
				response = "Yeah totally worked fo sho haha.";
			}
			catch (Exception e) {
				System.err.println("Error while handling ADD REPOSITORY.");
				e.printStackTrace();
			}
		}
		

		
		
		if (response != null) {
			exchange.sendResponseHeaders(200, response.length());
		}
		else {
			response = "401 (Bad Request)";
			exchange.sendResponseHeaders(401, response.length());
		}
		OutputStream os = exchange.getResponseBody();
		os.write(response.getBytes());
		os.close();
		
	}
}
