package ch.ethz.fgremper.cloudstudio.client;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * Communication with the RTCA server.
 * 
 * @author Fabian Gremper
 * 
 */
public class HttpClient {

	private static final Logger log = LogManager.getLogger(HttpClient.class);
	
	/**
	 * 
	 * Sends login request to the RTCA server and returns session ID.
	 * 
	 * @param serverUrl RTCA server URL
	 * @param username username
	 * @param password password
	 * @return session ID
	 * 
	 * @throws Exception
	 * 
	 */
	public String login(String serverUrl, String username, String password) throws Exception {

		// URL
		String url = serverUrl + "/api/login?";
		String data = "username=" + URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");
		
		// Send request to server
		log.debug("Sending login request to server...");
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
		log.debug("Response code: " + responseCode);

		// Login successful?
		if (responseCode == HttpURLConnection.HTTP_OK) {
			
			// Response text
			String response = IOUtils.toString(con.getInputStream(), "UTF-8");
			log.debug("Response text: " + response);
			
			// Unwrap the session ID from the response object
			JSONObject loginResponseObject = new JSONObject(response);
			return loginResponseObject.getString("sessionId");
			
		}
		else {
			
			// Response text
			String response = IOUtils.toString(con.getErrorStream(), "UTF-8");
			log.debug("Response text: " + response);
			
			// Get error message if there is one
			String errorMessage = null;
			try {
				JSONObject responseObject = new JSONObject(response);
				errorMessage = responseObject.getString("error");
			}
			catch (JSONException e) {
				// There's no error message in the response
			}
			
			// Throw an exception
			if (errorMessage != null) {
				throw new Exception(errorMessage);
			}
			else {
				throw new Exception("Unknown error");
			}
			
		}

	}
	
	/**
	 * 
	 * Send the entire local git state of a single git repository to the RTCA server.
	 * 
	 * @param serverUrl RTCA server URL
	 * @param body JSON string provided by the RepositoryReader
	 * 
	 * @throws Exception
	 * 
	 */
	public void sendGitState(String serverUrl, String sessionId, String repositoryAlias, String body) throws Exception {

		// URL
		String url = serverUrl + "/api/localState?sessionId=" + URLEncoder.encode(sessionId, "UTF-8") + "&repositoryAlias=" + URLEncoder.encode(repositoryAlias, "UTF-8");;

		// Send local git state to server
		log.debug("Sending login request to server...");
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setReadTimeout(10000);
		con.setConnectTimeout(15000);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		
		// Write content
		con.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
		out.write(body);
		out.close();

		// Response code
		int responseCode = con.getResponseCode();
		log.debug("Response code: " + responseCode);

		// Not successful?
		if (responseCode != HttpURLConnection.HTTP_OK) {

			// Response text
			String response = IOUtils.toString(con.getErrorStream(), "UTF-8");
			log.debug("Response text: " + response);
			
			// Get error message if there is one
			String errorMessage = null;
			try {
				JSONObject responseObject = new JSONObject(response);
				errorMessage = responseObject.getString("error");
			}
			catch (JSONException e) {
				// There's no error message in the response
			}
			
			// Throw an exception
			if (errorMessage != null) {
				throw new Exception(errorMessage);
			}
			else {
				throw new Exception("Unknown error");
			}
			
		}
		
	}
	
}
