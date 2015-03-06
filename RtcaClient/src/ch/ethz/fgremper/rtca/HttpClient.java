package ch.ethz.fgremper.rtca;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * Communication with the RTCA server.
 * @author Fabian Gremper
 */
public class HttpClient {

	private static final Logger log = LogManager.getLogger(HttpClient.class);
	
	/**
	 * Sends login request to the RTCA server and returns session ID.
	 * @param serverUrl RTCA server URL
	 * @param username username
	 * @param password password
	 * @return session ID
	 * @throws Exception
	 */
	public String login(String serverUrl, String username, String password) throws Exception {

		// URL
		String url = serverUrl + "/request/login";
		
		// Create login request JSON object
		JSONObject loginObject = new JSONObject();
		loginObject.put("username", username);
		loginObject.put("password", password);
		String jsonString = loginObject.toString();
		
		// Send request to server
		log.debug("Sending login request to server...");
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
		out.write(jsonString);
		out.close();

		// Response code
		int responseCode = con.getResponseCode();
		log.debug("Response code: " + responseCode);
		
		// Login successful?
		if (responseCode == 200) {
			String response = IOUtils.toString(con.getInputStream(), "UTF-8");
			log.debug("Response text: " + response);
			
			// Unwrap the session ID from the response object
			JSONObject loginResponseObject = new JSONObject(response);
			return loginResponseObject.getString("sessionId");
		}
		else {
			throw new Exception("Response code " + responseCode + " when trying to log in.");
		}

	}
	
	/**
	 * Send the entire local git state of a single git repository to the RTCA server.
	 * @param serverUrl RTCA server URL
	 * @param jsonString JSON string provided by the RepositoryReader
	 * @throws Exception
	 */
	public void sendGitState(String serverUrl, String jsonString) throws Exception {

		// URL
		String url = serverUrl + "/request/setLocalGitState";

		// Send local git state to server
		log.debug("Sending login request to server...");
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
		out.write(jsonString);
		out.close();

		// Response code
		int responseCode = con.getResponseCode();
		log.debug("Response code: " + responseCode);

	}
	
}
