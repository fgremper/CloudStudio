package ch.ethz.fgremper.rtca;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class HttpClient {

	public String login(String serverUrl, String username, String password) throws Exception {

		String url = serverUrl + "/request/login";
		JSONObject loginObject = new JSONObject();
		loginObject.put("username", username);
		loginObject.put("password", password);
		String jsonString = loginObject.toString();
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
		out.write(jsonString);
		out.close();

		int responseCode = con.getResponseCode();

		System.out.println("[Http] Response code: " + responseCode);
		
		if (responseCode == 200) {
			String response = IOUtils.toString(con.getInputStream(), "UTF-8");
			System.out.println("[Http] Response: " + response);
			
			JSONObject loginResponseObject = new JSONObject(response);
			return loginResponseObject.getString("sessionId");
		}
		else {
			throw new Exception("Response code " + responseCode + " when trying to log in.");
		}

	}
	
	public void sendGitState(String serverUrl, String jsonString) throws Exception {

		String url = serverUrl + "/request/setLocalGitState";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("POST");
		con.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
		out.write(jsonString);
		out.close();

		int responseCode = con.getResponseCode();

		System.out.println("[Http] Response code: " + responseCode);

	}
	
}
