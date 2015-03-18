package ch.ethz.fgremper.rtca;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sun.net.www.protocol.http.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ApiHttpHandler implements HttpHandler {

	private static final Logger log = LogManager.getLogger(ApiHttpHandler.class);
	
	@SuppressWarnings("unchecked")
	public void handle(HttpExchange exchange) throws IOException {
		
		// Get HTTP exchange information
		URI uri = exchange.getRequestURI();
		
		// Request method
		String requestMethod = exchange.getRequestMethod();

		// API name
		String prefix = "/api/";
		String apiName = uri.getPath().substring(prefix.length(), uri.getPath().length());
		
		// Parameters
        Map<String, Object> params = (Map<String, Object>) exchange.getAttribute("parameters");

        // Body
		String body = IOUtils.toString(exchange.getRequestBody(), "UTF-8");
        log.info("Incoming request: " + requestMethod + " " + uri.getPath());
        log.info("Body: " + body);

		// Variables
		String response = null;
		String error = null;

		DatabaseConnection db = null;
		try {
			
			// Get database connection
			db = new DatabaseConnection();

			String sessionId = null;
			String sessionUsername = null;
			sessionId = (String) params.get("sessionId");
			if (sessionId != null) sessionUsername = db.getUsername(sessionId);
			
			/* GET REQUESTS */
			
			// Repositories request
			if (apiName.equals("repositories") && requestMethod.equalsIgnoreCase("get")) {
				
				// Read repository information from database
				if (sessionUsername != null) {
					response = db.getRepositories(sessionUsername).toString();
				}
				else {
					throw new Exception("No session ID given");
				}
				
			}

			// Get users request
			else if (apiName.equals("repositories") && requestMethod.equalsIgnoreCase("get")) {
				
				// We need to be admin to retrieve this information
				if (db.isUserAdmin(sessionUsername)) {
					response = db.getUsers().toString();
				}
				else {
					throw new Exception("No administrator privileges");
				}
				
			}

			// Get repository information
			else if (apiName.equals("repositoryInformation") && requestMethod.equalsIgnoreCase("get")) {

				// Get parameters
				String repositoryAlias = (String) params.get("repositoryAlias");
				
				if (db.isUserAdmin(sessionUsername) || db.doesUserHaveRepositoryAccess(sessionUsername, repositoryAlias)) {
					response = db.getRepositoryInformation(repositoryAlias);
				}
				else {
					throw new Exception("No repository access");
				}
				
			}
			
			// Get branch level awareness
			else if (apiName.equals("branchAwareness") && requestMethod.equalsIgnoreCase("get")) {
				
				// Get parameters
				String repositoryAlias = (String) params.get("repositoryAlias");
				
				// Need to be admin or have repository access
				if (db.isUserAdmin(sessionUsername) || db.doesUserHaveRepositoryAccess(sessionUsername, repositoryAlias)) {
					response = db.getBranchLevelAwareness(repositoryAlias).toString();
				}
				else {
					throw new Exception("No repository access");
				}
				
			}
			
			// Get file awareness requests
			else if (apiName.equals("fileAwareness") && requestMethod.equalsIgnoreCase("get")) {

				// Get parameters
				String repositoryAlias = (String) params.get("repositoryAlias");
				String branch = (String) params.get("branch");
				List<String> selectedAdditionalBranches = (List<String>) params.get("selectedAdditionalBranches");
				if (selectedAdditionalBranches == null) selectedAdditionalBranches = new ArrayList<String>();
				boolean showConflicts = ((String) params.get("showConflicts")).equalsIgnoreCase("true");
				boolean showUncommitted = ((String) params.get("showUncommitted")).equalsIgnoreCase("true");
				
				// Need to be admin or have repository access
				if (db.isUserAdmin(sessionUsername) || db.doesUserHaveRepositoryAccess(sessionUsername, repositoryAlias)) {
					JSONObject responseObject = db.getFileLevelAwareness(repositoryAlias, sessionUsername, branch, selectedAdditionalBranches, showUncommitted, showConflicts);
					
					// If we show conflicts, go through all of the items and check the ones with file conflicts for line conflicts
					if (showConflicts) {
						JSONArray branches = responseObject.getJSONArray("branches");
						for (int i = 0; i < branches.length(); i++) {
							JSONObject branchObject = branches.getJSONObject(i);
							String compareToBranch = branchObject.getString("branch");
							JSONArray fileArray = branchObject.getJSONArray("files");
							for (int j = 0; j < fileArray.length(); j++) {
								JSONObject conflict = fileArray.getJSONObject(j);
								String filename = conflict.getString("filename");
								JSONArray users = conflict.getJSONArray("users");
								for (int k = 0; k < users.length(); k++) {
									JSONObject user = users.getJSONObject(k);
									
									// File conflict?
									if (user.getString("type").equals("file conflict")) {
										String theirUsername = user.getString("username");
										
										// Look up the ancestor file in the git repository
							            ContentConflictGitReader gitReader = new ContentConflictGitReader(repositoryAlias, branch, filename, sessionUsername, compareToBranch, theirUsername, showUncommitted);

							            // If there are content conflicts, put this instead
							            int lineConflicts = gitReader.countConflicts();
							            if (lineConflicts > 0) {
							            	user.put("type", lineConflicts + " line conflict");
							            }
										
									}
								}
							}
						}
					}
					response = responseObject.toString();
				}
				else {
					throw new Exception("No repository access");
				}
				
			}
			

			// Get content level awareness
			else if (apiName.equals("contentAwareness") && requestMethod.equalsIgnoreCase("get")) {
				
				// Get parameters
				String repositoryAlias = (String) params.get("repositoryAlias");
				String branch = (String) params.get("branch");
				String compareToBranch = (String) params.get("compareToBranch");
				String theirUsername = (String) params.get("theirUsername");
				String filename = (String) params.get("filename");
				List<String> selectedAdditionalBranches = (List<String>) params.get("selectedAdditionalBranches");
				if (selectedAdditionalBranches == null) selectedAdditionalBranches = new ArrayList<String>();
				boolean showUncommitted = ((String) params.get("showUncommitted")).equalsIgnoreCase("true");
				
				// Need to be admin or have repository access
				if (db.isUserAdmin(sessionUsername) || db.doesUserHaveRepositoryAccess(sessionUsername, repositoryAlias)) {
					
					String fileStorageDirectory = ServerConfig.getInstance().fileStorageDirectory;
						
					String mySha = db.getFileSha(repositoryAlias, sessionUsername, branch, filename, showUncommitted);
					String theirSha = db.getFileSha(repositoryAlias, theirUsername, compareToBranch, filename, showUncommitted);

					JSONObject responseObject = new JSONObject();
					
					List<String> myContent;
					List<String> theirContent;
					
					if (mySha != null) {
				        myContent = SideBySideDiff.fileToLines(fileStorageDirectory + "/" + mySha);
					}
					else {
						myContent = new LinkedList<String>();
					}

					if (theirSha != null) {
				        theirContent = SideBySideDiff.fileToLines(fileStorageDirectory + "/" + theirSha);
					}
					else {
						theirContent = new LinkedList<String>();
					}
					
					responseObject.put("content", SideBySideDiff.diff(myContent, theirContent));
					
					response = responseObject.toString();

				}
				else {
					throw new Exception("No repository access");
				}
				
			}
			
			// Get content level conflicts
			else if (apiName.equals("contentConflicts") && requestMethod.equalsIgnoreCase("get")) {
				
				// Get parameters
				String repositoryAlias = (String) params.get("repositoryAlias");
				String branch = (String) params.get("branch");
				String compareToBranch = (String) params.get("compareToBranch");
				String theirUsername = (String) params.get("theirUsername");
				String filename = (String) params.get("filename");
				List<String> selectedAdditionalBranches = (List<String>) params.get("selectedAdditionalBranches");
				if (selectedAdditionalBranches == null) selectedAdditionalBranches = new ArrayList<String>();
				boolean showUncommitted = ((String) params.get("showUncommitted")).equalsIgnoreCase("true");
				
				// Need to be admin or have repository access
				if (db.isUserAdmin(sessionUsername) || db.doesUserHaveRepositoryAccess(sessionUsername, repositoryAlias)) {
					
		            ContentConflictGitReader gitReader = new ContentConflictGitReader(repositoryAlias, branch, filename, sessionUsername, compareToBranch, theirUsername, showUncommitted);
					JSONObject responseObject = new JSONObject();
					responseObject.put("content", gitReader.diff());
					response = responseObject.toString();
		            
				}
				else {
					throw new Exception("No repository access");
				}
				
			}
			
			/* POST REQUESTS */

			// Auth request
			else if (apiName.equals("login") && requestMethod.equalsIgnoreCase("post")) {

				// Read parameters
				String username = (String) params.get("username");
				String password = (String) params.get("password");

				// Request new session ID from database
				String newSessionId = db.getNewSessionIdForCorrectLogin(username, password);

				if (newSessionId != null) {
				
					// Initialize response object
					JSONObject responseObject = new JSONObject();
					
					// Persist session ID
					db.startTransaction();
					db.persistSessionIdForUser(newSessionId, username);
					db.commitTransaction();

					// Create user object
					responseObject.put("isAdmin", db.isUserAdmin(username));
					responseObject.put("isCreator", db.isUserCreator(username));
					responseObject.put("sessionId", newSessionId);
					responseObject.put("username", username);
					
					response = responseObject.toString();
					
				}
				else {
					throw new Exception("Incorrect login credentials");
				}
				
			}
			
			// Create repository
			else if (apiName.equals("createRepository") && requestMethod.equalsIgnoreCase("post")) {

				// Get parameters
				String repositoryAlias = (String) params.get("repositoryAlias");
				String repositoryUrl = (String) params.get("repositoryUrl");

				// Need to be administrator or creator
				if (db.isUserAdmin(sessionUsername) || db.isUserCreator(sessionUsername)) {
					db = new DatabaseConnection();
					db.startTransaction();
					String repositoryOwner = sessionUsername;
					db.addRepository(repositoryAlias, repositoryUrl, repositoryOwner);
					db.commitTransaction();
					response = "{}";
				}
				else {
					throw new Exception("Insufficient privileges");
				}
				
			}

			// Delete repository
			else if (apiName.equals("deleteRepository") && requestMethod.equalsIgnoreCase("post")) {
				
				// Get parameters
				String repositoryAlias = (String) params.get("repositoryAlias");

				// Need to be administrator or repository owner
				if (db.isUserAdmin(sessionUsername) || db.isUserRepositoryOwner(sessionUsername, repositoryAlias)) {
					db = new DatabaseConnection();
					db.startTransaction();
					db.deleteRepository(repositoryAlias);
					db.commitTransaction();
					response = "{}";
				}
				else {
					throw new Exception("Insufficient privileges");
				}
				
			}

			// Add user to repository
			else if (apiName.equals("addUserToRepository") && requestMethod.equalsIgnoreCase("post")) {

				// Get parameters
				String repositoryAlias = (String) params.get("repositoryAlias");
				String username = (String) params.get("username");

				// Need to be administrator or repository owner
				if (db.isUserAdmin(sessionUsername) || db.isUserRepositoryOwner(sessionUsername, repositoryAlias)) {
					db = new DatabaseConnection();
					db.startTransaction();
					db.addUserToRepository(username, repositoryAlias);
					db.commitTransaction();
					response = "{}";
				}
				else {
					throw new Exception("Insufficient privileges");
				}
				
			}
			
			// Remove user from repository
			else if (apiName.equals("removeUserFromRepository") && requestMethod.equalsIgnoreCase("post")) {

				// Get parameters
				String repositoryAlias = (String) params.get("repositoryAlias");
				String username = (String) params.get("username");

				// Need to be administrator or repository owner
				if (db.isUserAdmin(sessionUsername) || db.isUserRepositoryOwner(sessionUsername, repositoryAlias)) {
					db = new DatabaseConnection();
					db.startTransaction();
					db.deleteUserFromRepository(username, repositoryAlias);
					db.commitTransaction();
					response = "{}";
				}
				else {
					throw new Exception("Insufficient privileges");
				}
			}

			// Modify repository owner
			else if (apiName.equals("modifyRepositoryOwner") && requestMethod.equalsIgnoreCase("post")) {

				// Get parameters
				String repositoryAlias = (String) params.get("repositoryAlias");
				String username = (String) params.get("username");

				// Need to be administrator or repository owner
				if (db.isUserAdmin(sessionUsername) || db.isUserRepositoryOwner(sessionUsername, repositoryAlias)) {
					db = new DatabaseConnection();
					db.startTransaction();
					db.modifyRepositoryOwner(repositoryAlias, username);
					db.commitTransaction();
					response = "{}";
				}
				else {
					throw new Exception("Insufficient privileges");
				}
				
			}

			// Create user
			else if (apiName.equals("createUser") && requestMethod.equalsIgnoreCase("post")) {

				// Read parameters
				String username = (String) params.get("username");
				String password = (String) params.get("password");

				db.addUser(username, password);
				
			}
			
			// Delete user
			else if (apiName.equals("deleteUser") && requestMethod.equalsIgnoreCase("post")) {

				// Get parameters
				String username = (String) params.get("username");

				// Need to be administrator
				if (db.isUserAdmin(sessionUsername)) {
					db = new DatabaseConnection();
					db.startTransaction();
					db.deleteUser(username);
					db.commitTransaction();
					response = "{}";
				}
				else {
					throw new Exception("No administrator privileges");
				}
				
			}

			// Give admin privileges
			else if (apiName.equals("giveAdminPrivileges") && requestMethod.equalsIgnoreCase("post")) {

				// Get parameters
				String username = (String) params.get("username");

				// Need to be administrator
				if (db.isUserAdmin(sessionUsername)) {
					db = new DatabaseConnection();
					db.startTransaction();
					db.makeUserAdmin(username);
					db.commitTransaction();
					response = "{}";
				}
				else {
					throw new Exception("No administrator privileges");
				}
				
			}

			// Revoke admin privileges
			else if (apiName.equals("revokeAdminPrivileges") && requestMethod.equalsIgnoreCase("post")) {

				// Get parameters
				String username = (String) params.get("username");

				// Need to be administrator
				if (db.isUserAdmin(sessionUsername)) {
					db = new DatabaseConnection();
					db.startTransaction();
					db.revokeUserAdmin(username);
					db.commitTransaction();
					response = "{}";
				}
				else {
					throw new Exception("No administrator privileges");
				}
				
			}

			// Give creator privileges
			else if (apiName.equals("giveCreatorPrivileges") && requestMethod.equalsIgnoreCase("post")) {

				// Get parameters
				String username = (String) params.get("username");

				// Need to be administrator
				if (db.isUserAdmin(sessionUsername)) {
					db = new DatabaseConnection();
					db.startTransaction();
					db.makeUserCreator(username);
					db.commitTransaction();
					response = "{}";
				}
				else {
					throw new Exception("No administrator privileges");
				}
				
			}

			// Revoke creator privileges
			else if (apiName.equals("revokeCreatorPrivileges") && requestMethod.equalsIgnoreCase("post")) {

				// Get parameters
				String username = (String) params.get("username");

				// Need to be administrator
				if (db.isUserAdmin(sessionUsername)) {
					db = new DatabaseConnection();
					db.startTransaction();
					db.revokeUserCreator(username);
					db.commitTransaction();
					response = "{}";
				}
				else {
					throw new Exception("No administrator privileges");
				}
				
			}
			
			/* ALSO POST BUT THE ONLY ONE THAT ACCESSES POST DATA */
			
			// Set local git state request
			else if (apiName.equals("localState") && requestMethod.equalsIgnoreCase("post")) {
				
				// Get parameters
				String repositoryAlias = (String) params.get("repositoryAlias");
				
				if (db.doesUserHaveRepositoryAccess(sessionUsername, repositoryAlias)) {
					db.startTransaction();
					db.setEntireUserGitState(body, sessionUsername, repositoryAlias);
					db.commitTransaction();
					response = "{}";
				}
				else {
					throw new Exception("No access to repository");
				}
				
			}
			
			// Unknown API call
			else {
				throw new Exception("Unknown API call");
			}

		}
		catch (Exception e) {
			error = e.getMessage();
			log.error("Handling request error: " + e.getMessage());
		}

		// Close database connection
		try {
			if (db != null) {
				db.closeConnection();
			}
		}
		catch (SQLException e) {
			// Do nothing
		}
		
		if (response != null) {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
		}
		else {
			response = "{}";
			try {
				JSONObject errorObject = new JSONObject();
				errorObject.put("error", (error == null ? "Unknown error" : error));
				response = errorObject.toString();
			}
			catch (JSONException e) {
				// Don't see how this can happen, we're just putting stuff into an object
			}
			exchange.sendResponseHeaders(500, response.length());
		}
		log.info("Sending response: " + response);
		OutputStream os = exchange.getResponseBody();
		os.write(response.getBytes());
		os.close();
		
	}

}
