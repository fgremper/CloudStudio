package ch.ethz.fgremper.rtca;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RequestHttpHandler implements HttpHandler {

	private static final Logger log = LogManager.getLogger(RequestHttpHandler.class);
	
	public void handle(HttpExchange exchange) throws IOException {
		
		// Get HTTP exchange information
		URI uri = exchange.getRequestURI();
		String requestMethod = exchange.getRequestMethod();
		System.out.println("Incoming request: " + requestMethod + " " + uri.getPath());
		String inputJsonString = IOUtils.toString(exchange.getRequestBody(), "UTF-8");
		System.out.println("JSON string: " + inputJsonString);
		
		// Variables
		String response = null;
		String prefix = "/request";

		DatabaseConnection db = null;
		try {
			// Get database connection
			db = new DatabaseConnection();

			// POST request?
			if (requestMethod.equalsIgnoreCase("POST")) {

				String sessionId = null;
				String sessionUsername = null;
				
				// Get the JSON object, session and username
				JSONObject incomingObject = new JSONObject(inputJsonString);
				try {
					sessionId = incomingObject.getString("sessionId");
					sessionUsername = db.getUsername(sessionId);
				}
				catch (Exception e) {
					// No user found
				}
				
				// Login request
				if (uri.getPath().equals(prefix + "/login") || uri.getPath().equals(prefix + "/createUserAndLogin")) {
					log.info("Incoming LOGIN.");
					
					// Read parameters
					String username = incomingObject.getString("username");
					String password = incomingObject.getString("password");
					
					// Do we need to create the user?
					if (uri.getPath().equals(prefix + "/createUserAndLogin")) {
						db.addUser(username, password);
					}
					
					// Request new session ID from database
					String newSessionId = db.getNewSessionIdForCorrectLogin(username, password);
	
					// Initialize response object
					JSONObject responseObject = new JSONObject();
					
					if (newSessionId != null) {
						// Persist session ID
						db.startTransaction();
						db.persistSessionIdForUser(newSessionId, username);
						db.commitTransaction();
	
						// Create user object
						responseObject.put("isAdmin", db.isUserAdmin(username));
						responseObject.put("isCreator", db.isUserCreator(username));
						responseObject.put("sessionId", newSessionId);
						responseObject.put("username", username);
					}
					
					response = responseObject.toString();
					
				}
			
				// Get repositories request
				if (uri.getPath().equals(prefix + "/getRepositories")) {
					log.info("Incoming GET REPOSITORIES.");
					
					// Read repository information from database
					if (sessionUsername != null) {
						response = db.getRepositories(sessionUsername).toString();
					}
				}
	
				// Get users request
				if (uri.getPath().equals(prefix + "/getUsers")) {
					log.info("Incoming GET USERS.");
					
					// We need to be admin to retrieve this information
					if (db.isUserAdmin(sessionUsername)) {
						response = db.getUsers().toString();
					}
				}

				// Get branch level awareness
				if (uri.getPath().equals(prefix + "/getBranchLevelAwareness")) {
					System.out.println("Incoming GET BRANCH LEVEL AWARENESS.");

					// Get repository alias
					String repositoryAlias = incomingObject.getString("repositoryAlias");
					
					// Need to be admin or have repository access
					if (db.isUserAdmin(sessionUsername) || db.doesUserHaveRepositoryAccess(sessionUsername, repositoryAlias)) {
						response = db.getBranchLevelAwareness(repositoryAlias).toString();
					}
				}

				// Get content level awareness
				if (uri.getPath().equals(prefix + "/getContentLevelAwareness")) {
					System.out.println("Incoming GET CONTENT LEVEL AWARENESS.");

					// Get data
					String repositoryAlias = incomingObject.getString("repositoryAlias");
					String theirUsername = incomingObject.getString("username");
					String branch = incomingObject.getString("branch");
					String compareToBranch = incomingObject.getString("compareToBranch");
					String filename = incomingObject.getString("filename");
					boolean showUncommitted = incomingObject.getBoolean("showUncommitted");
					boolean showConflicts = incomingObject.getBoolean("showConflicts");

					// Need to be admin or have repository access
					if (db.isUserAdmin(sessionUsername) || db.doesUserHaveRepositoryAccess(sessionUsername, repositoryAlias)) {
						String fileStorageDirectory = ServerConfig.getInstance().fileStorageDirectory;
							
						
						if (!showConflicts) {
							
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
				            GitReader gitReader = new GitReader(repositoryAlias, branch, filename, sessionUsername, compareToBranch, theirUsername, showUncommitted);
				            

							JSONObject responseObject = new JSONObject();
					
							responseObject.put("content", gitReader.diff());
							
							response = responseObject.toString();
				            
						}
					}
				}

				// Get file awareness requests
				if (uri.getPath().equals(prefix + "/getFileLevelAwareness")) {
					System.out.println("Incoming GET FILE LEVEL CONFLICTS.");

					String repositoryAlias = incomingObject.getString("repositoryAlias");

					String branch = incomingObject.getString("branch");
					
					// Need to be admin or have repository access
					if (db.isUserAdmin(sessionUsername) || db.doesUserHaveRepositoryAccess(sessionUsername, repositoryAlias)) {
						JSONObject responseObject = db.getFileLevelAwareness(incomingObject, sessionUsername);
						
						

						boolean showConflicts = incomingObject.getBoolean("showConflicts");

						boolean showUncommitted = incomingObject.getBoolean("showUncommitted");

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
										
										if (user.getString("type").equals("file conflict")) {
											
											String theirUsername = user.getString("username");

								            GitReader gitReader = new GitReader(repositoryAlias, branch, filename, sessionUsername, compareToBranch, theirUsername, showUncommitted);
								            
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
				}
				
				// Get repository information
				if (uri.getPath().equals(prefix + "/getRepositoryInformation")) {
					System.out.println("Incoming GET REPOSITORY INFORMATION.");

					String repositoryAlias = incomingObject.getString("repositoryAlias");
					
					if (db.isUserAdmin(sessionUsername) || db.doesUserHaveRepositoryAccess(sessionUsername, repositoryAlias)) {
						response = db.getRepositoryInformation(repositoryAlias);
					}
				}
			
				// Add repository request
				if (uri.getPath().equals(prefix + "/addRepository")) {
					System.out.println("Incoming ADD REPOSITORY.");
		
					String repositoryAlias = incomingObject.getString("repositoryAlias");
					String repositoryUrl = incomingObject.getString("repositoryUrl");
	
					if (db.isUserAdmin(sessionUsername) || db.isUserCreator(sessionUsername)) {
						db = new DatabaseConnection();
						db.startTransaction();
						String repositoryOwner = sessionUsername;
						db.addRepository(repositoryAlias, repositoryUrl, repositoryOwner);
						db.commitTransaction();
						response = "{}";
					}
				}

				// Delete repository request
				if (uri.getPath().equals(prefix + "/deleteRepository")) {
					System.out.println("Incoming DELETE REPOSITORY.");

					String repositoryAlias = incomingObject.getString("repositoryAlias");
	
					if (db.isUserAdmin(sessionUsername) || db.isUserRepositoryOwner(sessionUsername, repositoryAlias)) {
						db = new DatabaseConnection();
						db.startTransaction();
						db.deleteRepository(repositoryAlias);
						db.commitTransaction();
						response = "{}";
					}
				}

				// Add user to repository request
				if (uri.getPath().equals(prefix + "/addUserToRepository")) {
					System.out.println("Incoming ADD USER TO REPOSITORY.");

					String username = incomingObject.getString("username");
					String repositoryAlias = incomingObject.getString("repositoryAlias");
	
					if (db.isUserAdmin(sessionUsername) || db.isUserRepositoryOwner(sessionUsername, repositoryAlias)) {
						db = new DatabaseConnection();
						db.startTransaction();
						db.addUserToRepository(username, repositoryAlias);
						db.commitTransaction();
						response = "{}";
					}
				}
				
				// Delete user from repository request
				if (uri.getPath().equals(prefix + "/deleteUserFromRepository")) {
					System.out.println("Incoming DELETE USER FROM REPOSITORY.");

					String username = incomingObject.getString("username");
					String repositoryAlias = incomingObject.getString("repositoryAlias");
	
					if (db.isUserAdmin(sessionUsername) || db.isUserRepositoryOwner(sessionUsername, repositoryAlias)) {
						db = new DatabaseConnection();
						db.startTransaction();
						db.deleteUserFromRepository(username, repositoryAlias);
						db.commitTransaction();
						response = "{}";
					}
				}

				// Modify repository owner
				if (uri.getPath().equals(prefix + "/modifyRepositoryOwner")) {
					System.out.println("Incoming MODIFY REPOSITORY OWNER.");

					String repositoryAlias = incomingObject.getString("repositoryAlias");
					String username = incomingObject.getString("username");
	
					if (db.isUserAdmin(sessionUsername) || db.isUserRepositoryOwner(sessionUsername, repositoryAlias)) {
						db = new DatabaseConnection();
						db.startTransaction();
						db.modifyRepositoryOwner(repositoryAlias, username);
						db.commitTransaction();
						response = "{}";
					}
				}

				// Delete user
				if (uri.getPath().equals(prefix + "/deleteUser")) {
					System.out.println("Incoming DELETE USER.");

					String username = incomingObject.getString("username");
	
					if (db.isUserAdmin(sessionUsername)) {
						db = new DatabaseConnection();
						db.startTransaction();
						db.deleteUser(username);
						db.commitTransaction();
						response = "{}";
					}
				}

				// Make user admin
				if (uri.getPath().equals(prefix + "/makeUserAdmin")) {
					System.out.println("Incoming MAKE USER ADMIN.");

					String username = incomingObject.getString("username");
	
					if (db.isUserAdmin(sessionUsername)) {
						db = new DatabaseConnection();
						db.startTransaction();
						db.makeUserAdmin(username);
						db.commitTransaction();
						response = "{}";
					}
				}

				// Revoke user admin
				if (uri.getPath().equals(prefix + "/revokeUserAdmin")) {
					System.out.println("Incoming REVOKE USER ADMIN.");

					String username = incomingObject.getString("username");
	
					if (db.isUserAdmin(sessionUsername)) {
						db = new DatabaseConnection();
						db.startTransaction();
						db.revokeUserAdmin(username);
						db.commitTransaction();
						response = "{}";
					}
				}

				// Make user creator
				if (uri.getPath().equals(prefix + "/makeUserCreator")) {
					System.out.println("Incoming MAKE USER CREATOR.");

					String username = incomingObject.getString("username");
	
					if (db.isUserAdmin(sessionUsername)) {
						db = new DatabaseConnection();
						db.startTransaction();
						db.makeUserCreator(username);
						db.commitTransaction();
						response = "{}";
					}
				}

				// Revoke user creator
				if (uri.getPath().equals(prefix + "/revokeUserCreator")) {
					System.out.println("Incoming REVOKE USER CREATOR.");

					String username = incomingObject.getString("username");
	
					if (db.isUserAdmin(sessionUsername)) {
						db = new DatabaseConnection();
						db.startTransaction();
						db.revokeUserCreator(username);
						db.commitTransaction();
						response = "{}";
					}
				}
				
				// Set local git state request
				if (uri.getPath().equals(prefix + "/setLocalGitState")) {
					System.out.println("Incoming SET LOCAL GIT STATE.");

					String repositoryAlias = incomingObject.getString("repositoryAlias");
					
					if (db.doesUserHaveRepositoryAccess(sessionUsername, repositoryAlias)) {
						db.startTransaction();
						db.setEntireUserGitState(inputJsonString, sessionUsername);
						db.commitTransaction();
						response = "{}";
					}
					
				}

			}
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// Close database connection
		try {
			if (db != null) {
				db.closeConnection();
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (response != null) {
			exchange.sendResponseHeaders(200, response.length());
		}
		else {
			response = "500 (Error)";
			exchange.sendResponseHeaders(500, response.length());
		}
		log.info("Sending response: " + response);
		OutputStream os = exchange.getResponseBody();
		os.write(response.getBytes());
		os.close();
		
	}

}
