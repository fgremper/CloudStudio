package ch.ethz.fgremper.cloudstudio.testing.client;

import static org.junit.Assert.assertNotNull;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.junit.Test;

import ch.ethz.fgremper.cloudstudio.client.HttpClient;
import ch.ethz.fgremper.cloudstudio.common.ParameterFilter;
import ch.ethz.fgremper.cloudstudio.server.ApiHttpHandler;
import ch.ethz.fgremper.cloudstudio.server.WebInterfaceHttpHandler;
import ch.ethz.fgremper.cloudstudio.testing.helper.TestGitHelper;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

/**
 * 
 * Class test for HttpClient
 * 
 * @author Fabian Gremper
 *
 */
public class HttpClientTest {

	/**
	 * 
	 * Test for logging in and sending the git state.
	 *
	 */
	@Test
	public void testCorrectData() throws Exception {
		
		// Setup test state
		TestGitHelper.setupTest();
		
		// Setup HTTP server
		HttpServer server = HttpServer.create(new InetSocketAddress(7331), 0);
		HttpContext context = server.createContext("/api", new ApiHttpHandler());
		context.getFilters().add(new ParameterFilter());
		server.createContext("/", new WebInterfaceHttpHandler());
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

	/**
	 * 
	 * Test login with wrong credentials
	 *
	 */
	@Test(expected=Exception.class)
	public void testBadDataLogin() throws Exception {
		
		HttpServer server = HttpServer.create(new InetSocketAddress(7331), 0);
		
		try {
			
			// Setup test state
			TestGitHelper.setupTest();
			
			// Setup HTTP server
			HttpContext context = server.createContext("/api", new ApiHttpHandler());
			context.getFilters().add(new ParameterFilter());
			server.createContext("/", new WebInterfaceHttpHandler());
			server.setExecutor(Executors.newCachedThreadPool());
			server.start();
	
			// Login
			HttpClient httpClient = new HttpClient();
			httpClient.login("http://127.0.0.1:7331", "John", "wrongpassword");

		}
		finally {
			
			// Shutdown server
			server.stop(0);
			
		}
		
	}

	/**
	 * 
	 * Test send git state with invalid session ID
	 * 
	 */
	@Test(expected=Exception.class)
	public void testBadDataGitState() throws Exception {
		
		HttpServer server = HttpServer.create(new InetSocketAddress(7331), 0);
		
		try {
			
			// Setup test state
			TestGitHelper.setupTest();
			
			// Setup HTTP server
			HttpContext context = server.createContext("/api", new ApiHttpHandler());
			context.getFilters().add(new ParameterFilter());
			server.createContext("/", new WebInterfaceHttpHandler());
			server.setExecutor(Executors.newCachedThreadPool());
			server.start();

			// Send request
			HttpClient httpClient = new HttpClient();
			httpClient.sendGitState("http://127.0.0.1:7331", "invalidsession", "TestRepository", "{files:[],commitHistory:[],branches:[]}");
		}
		finally {
			
			// Shutdown server
			server.stop(0);
			
		}
		
	}

	/**
	 * 
	 * Try to login when the server is down
	 *
	 */
	@Test(expected=Exception.class)
	public void testServerDownLogin() throws Exception {
	
		// Login
		HttpClient httpClient = new HttpClient();
		httpClient.login("http://127.0.0.1:7331", "John", "wrongpassword");
			
	}

	/**
	 * 
	 * Try to send git state when server is down
	 * @throws Exception
	 */
	@Test(expected=Exception.class)
	public void testServerDownGitState() throws Exception {

		// Send git state
		HttpClient httpClient = new HttpClient();
		httpClient.sendGitState("http://127.0.0.1:7331", "invalidsession", "TestRepository", "{files:[],commitHistory:[],branches:[]}");

	}

}
