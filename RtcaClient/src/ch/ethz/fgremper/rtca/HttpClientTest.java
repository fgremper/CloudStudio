package ch.ethz.fgremper.rtca;

import static org.junit.Assert.*;

import org.junit.Test;

public class HttpClientTest {

	@Test
	public void authTest() throws Exception {
		HttpClient httpClient = new HttpClient();
		httpClient.login("http://127.0.0.1:7331", "john", "totallysecret");
	}

}
