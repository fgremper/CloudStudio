package ch.ethz.fgremper.cloudstudio.testing.client;

import static org.junit.Assert.*;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import ch.ethz.fgremper.cloudstudio.client.HttpClient;
import ch.ethz.fgremper.cloudstudio.common.ParameterFilter;
import ch.ethz.fgremper.cloudstudio.server.ApiHttpHandler;
import ch.ethz.fgremper.cloudstudio.server.DatabaseConnection;
import ch.ethz.fgremper.cloudstudio.server.WebInterfaceHttpHandler;
import ch.ethz.fgremper.cloudstudio.testing.helper.TestGitHelper;

public class HttpClientTest {

	@Test
	public void test() throws Exception {
		
		// Setup test state
		TestGitHelper.setupTest();
		
		// Setup HTTP server
		HttpServer server = HttpServer.create(new InetSocketAddress(7331), 0);
		HttpContext context = server.createContext("/api", new ApiHttpHandler());
		context.getFilters().add(new ParameterFilter());
		server.createContext(File.separator, new WebInterfaceHttpHandler());
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		// Login
		HttpClient httpClient = new HttpClient();
		String sessionId = httpClient.login("http://127.0.0.1:7331", "John", "johnpw");
		assertNotNull(sessionId);
		
		// Set Git state
		httpClient.sendGitState("http://127.0.0.1:7331", sessionId, "TestRepository", "{files:[],commitHistory:[],branches:[]}");
		
		// Shutdown server
		server.stop(0);
		
	}

}
