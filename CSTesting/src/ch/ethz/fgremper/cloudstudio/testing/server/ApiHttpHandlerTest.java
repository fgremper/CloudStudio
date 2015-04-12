package ch.ethz.fgremper.cloudstudio.testing.server;

import static org.junit.Assert.*;

import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Test;

import ch.ethz.fgremper.cloudstudio.client.HttpClient;
import ch.ethz.fgremper.cloudstudio.common.ParameterFilter;
import ch.ethz.fgremper.cloudstudio.server.ApiHttpHandler;
import ch.ethz.fgremper.cloudstudio.server.DatabaseConnection;
import ch.ethz.fgremper.cloudstudio.server.WebInterfaceHttpHandler;
import ch.ethz.fgremper.cloudstudio.testing.helper.TestGitHelper;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

public class ApiHttpHandlerTest {

	public String sendPostRequest(String path, String data, int expectedResponseCode) throws Exception {

		// URL
		String url = "http://127.0.0.1:7331/api/" + path;

		// Send local git state to server
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setReadTimeout(10000);
		con.setConnectTimeout(15000);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		
		// Write content
		con.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
		out.write(data);
		out.close();

		// Response code
		int responseCode = con.getResponseCode();
		assertEquals(expectedResponseCode, responseCode);

		// Response
		String response = null;
		if (responseCode == HttpURLConnection.HTTP_OK) {
			response = IOUtils.toString(con.getInputStream(), "UTF-8");
		}
		else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
			response = IOUtils.toString(con.getErrorStream(), "UTF-8");
		}
		
		return response;
			
	}
	
	@Test
	public void testCreateAndDeleteRepository() throws Exception {

		// Get database connection
		DatabaseConnection db = new DatabaseConnection();
		db.getConnection();
		
		// Setup test state
		TestGitHelper.setupTest();
		
		db.makeUserCreator("John");
		
		// Setup HTTP server
		HttpServer server = HttpServer.create(new InetSocketAddress(7331), 0);
		HttpContext context = server.createContext("/api", new ApiHttpHandler());
		context.getFilters().add(new ParameterFilter());
		server.createContext(File.separator, new WebInterfaceHttpHandler());
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		HttpClient httpClient = new HttpClient();
		String sessionId = httpClient.login("http://127.0.0.1:7331", "Admin", "1234");
		
		assertEquals(1, db.getAllRepositories().length());
		
		// Create repository
		sendPostRequest("createRepository", "sessionId=" + sessionId + "&repositoryAlias=HelloWorld&repositoryUrl=HelloUrl&repositoryDescription=HelloDescription", HttpURLConnection.HTTP_OK);
		
		assertEquals(2, db.getAllRepositories().length());
		
		// Delete repository
		sendPostRequest("deleteRepository", "sessionId=" + sessionId + "&repositoryAlias=HelloWorld", HttpURLConnection.HTTP_OK);

		assertEquals(1, db.getAllRepositories().length());
		
		server.stop(0);
		
		db.closeConnection();

	}

}
