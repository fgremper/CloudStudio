package ch.ethz.fgremper.rtca;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class PushHttpHandler implements HttpHandler {

	public void handle(HttpExchange exchange) throws IOException {
		URI uri = exchange.getRequestURI();
		String requestMethod = exchange.getRequestMethod();
		System.out.println("Incoming PUSH request: " + requestMethod + " " + uri.getPath());

		String username = "testuser";
		String repositoryAlias = "testrepo";

		if (requestMethod.equalsIgnoreCase("PUT")) {
			System.out.println("Incoming PUT:");

			String jsonString = IOUtils.toString(exchange.getRequestBody(), "UTF-8");
			System.out.println("JSON string: " + jsonString);

			DatabaseConnection db;
			try {
				JSONArray fileArray = new JSONArray(jsonString);

				// TODO: use a pool of database connections
				db = new DatabaseConnection();
				db.startTransaction();
				db.deleteAllFilesFromRepositoryAndUser(repositoryAlias, username);

				for (int i = 0; i < fileArray.length(); i++) {
					JSONObject fileObject = fileArray.getJSONObject(i);

					String filename = fileObject.getString("filename");
					String content = fileObject.getString("content");
					String sha = DigestUtils.sha1Hex(content).toString();

					System.out.println("Filename: " + filename);
					System.out.println("Content: " + content);
					System.out.println("SHA: " + sha);

					FileUtils.writeStringToFile(new File("/Users/novocaine/Documents/masterthesis/workspace/RtcaServer/filestorage/" + sha), content);

					db.storeFile(repositoryAlias, username, filename, sha);
				}

				db.commitTransaction();
			} catch (Exception e) {
				// db.rollbackTransaction();
				System.err.println("Error while handling PUT request.");
				e.printStackTrace();
			}

			// TODO: send a good response!
			String response = "Thanks mate!";
			exchange.sendResponseHeaders(200, response.length());
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}

	}
}
