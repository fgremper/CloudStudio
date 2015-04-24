package ch.ethz.fgremper.cloudstudio.testing.server;

import static org.junit.Assert.*;

import java.io.File;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import ch.ethz.fgremper.cloudstudio.common.ParameterFilter;
import ch.ethz.fgremper.cloudstudio.server.ApiHttpHandler;
import ch.ethz.fgremper.cloudstudio.server.WebInterfaceHttpHandler;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

public class WebInterfaceHttpHandlerTest {

	@Test
	public void test() throws Exception {

		// Setup HTTP server
		HttpServer server = HttpServer.create(new InetSocketAddress(7331), 0);
		server.createContext("/", new WebInterfaceHttpHandler());
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		
		// URL
		String url = "http://127.0.0.1:7331/";

		// Send local git state to server
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setReadTimeout(10000);
		con.setConnectTimeout(15000);
		con.setRequestMethod("GET");

		int responseCode = con.getResponseCode();
		assertEquals(HttpURLConnection.HTTP_OK, responseCode);
		
		String response = IOUtils.toString(con.getInputStream(), "UTF-8");
		System.out.println(response);
		
		server.stop(0);
		
	}

}
