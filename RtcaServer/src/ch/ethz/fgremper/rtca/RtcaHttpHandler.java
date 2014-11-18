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

public class RtcaHttpHandler implements HttpHandler {

	public void handle(HttpExchange exchange) throws IOException {
		URI uri = exchange.getRequestURI();
		String requestMethod = exchange.getRequestMethod();
		System.out.println("Incoming request: " + requestMethod + " " + uri.getPath());

		String username = "testuser";
		String repositoryAlias = "testrepo";

		if (requestMethod.equalsIgnoreCase("GET")) {

			// serve static html!
			String root = "static";
			System.out.println("looking for: "+ root + uri.getPath());
			String path = uri.getPath();
			File file = new File(root + path).getCanonicalFile();

			if (!file.isFile()) {
				// Object does not exist or is not a file: reject with 404 error.
				String response = "404 (Not Found)\n";
				exchange.sendResponseHeaders(404, response.length());
				OutputStream os = exchange.getResponseBody();
				os.write(response.getBytes());
				os.close();
			} else {
				// Object exists and is a file: accept with response code 200.
				String mime = "text/html";
				if (path.substring(path.length()-3).equals(".js")) mime = "application/javascript";
				if (path.substring(path.length()-4).equals(".css")) mime = "text/css";            

				Headers h = exchange.getResponseHeaders();
				h.set("Content-Type", mime);
				exchange.sendResponseHeaders(200, 0);              

				OutputStream os = exchange.getResponseBody();
				FileInputStream fs = new FileInputStream(file);
				final byte[] buffer = new byte[0x10000];
				int count = 0;
				while ((count = fs.read(buffer)) >= 0) {
					os.write(buffer,0,count);
				}
				fs.close();
				os.close();

			}
		}

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

					FileUtils.writeStringToFile(new File("C:/GDev/newmtworkspace/mtcode/filestorage/" + sha), content);

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
			exchange.sendResponseHeaders(400, response.length());
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}

	}
}
