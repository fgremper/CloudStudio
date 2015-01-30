package ch.ethz.fgremper.rtca;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.fgremper.rtca.helper.JSONHelper;

public class DatabaseConnection {

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost/cloudstudio";
	static final String USER = "dbadmin";
	static final String PASS = "";

	static final String salt = "heyoiamthesaltwhatisupwithyou";
	
	Connection con = null;

	public DatabaseConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");

		System.out.println("[DatabaseConnection] Connecting to database...");
		con = DriverManager.getConnection(DB_URL, USER,PASS);
		System.out.println("[DatabaseConnection] Connected to database.");
	}

	/* UTILITY */
	
	public void startTransaction() throws SQLException {
		con.setAutoCommit(false);
	}

	public void commitTransaction() throws SQLException {
		con.commit();
	}

	public void rollbackTransaction() throws SQLException {
		con.rollback();
	}

	/* UPDATE FROM CLIENT CYCLE */
	
	public void deleteAllFilesFromRepositoryAndUser(String repositoryAlias, String username) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("DELETE FROM files WHERE repositoryalias = ? AND username = ?");

		stmt.setString(1, repositoryAlias);
		stmt.setString(2, username);

		int rowsAffected = stmt.executeUpdate();

	
		stmt = con.prepareStatement("DELETE FROM commithistory WHERE repositoryalias = ? AND username = ?");

		stmt.setString(1, repositoryAlias);
		stmt.setString(2, username);

		rowsAffected = stmt.executeUpdate();
	}

	public void storeFile(String repositoryAlias, String username, String filename, String sha, String branch, String commit, String committed) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("INSERT INTO files (repositoryalias, username, filename, sha, branch, commit, committed) VALUES (?, ?, ?, ?, ?, ?, ?)");

		System.out.println("FILE: " + filename);
		stmt.setString(1, repositoryAlias);
		stmt.setString(2, username);
		stmt.setString(3, filename);
		stmt.setString(4, sha);
		stmt.setString(5, branch);
		stmt.setString(6, commit);
		stmt.setString(7, committed);

		int rowsAffected = stmt.executeUpdate();
	}
	
	public void storeCommitHistory(String repositoryAlias, String username, String commit, List<String> downstreamCommits) throws SQLException {

		PreparedStatement downstreamCommitInsertStatement = con.prepareStatement("INSERT INTO commithistory (repositoryalias, username, commit, downstreamcommit) VALUES (?, ?, ?, ?)");
		
		downstreamCommitInsertStatement.setString(1, repositoryAlias);
		downstreamCommitInsertStatement.setString(2, username);
		downstreamCommitInsertStatement.setString(3, commit);
		
		for (String downstreamCommit : downstreamCommits) {
			downstreamCommitInsertStatement.setString(4, downstreamCommit);
			downstreamCommitInsertStatement.executeUpdate();
		}
		
	}

	public void resetDatabase() throws SQLException {
		PreparedStatement stmt;
		stmt = con.prepareStatement("DELETE FROM usersessions");
		stmt.executeUpdate();
		stmt = con.prepareStatement("DELETE FROM useraccess");
		stmt.executeUpdate();
		stmt = con.prepareStatement("DELETE FROM files");
		stmt.executeUpdate();
		stmt = con.prepareStatement("DELETE FROM commithistory");
		stmt.executeUpdate();
		stmt = con.prepareStatement("DELETE FROM repositories");
		stmt.executeUpdate();
		stmt = con.prepareStatement("DELETE FROM users");
		stmt.executeUpdate();
		
	}
	

	public String getConflicts(JSONObject getConflictsObject, String username) throws Exception {
		String repositoryAlias = getConflictsObject.getString("repositoryAlias");
		String branch = getConflictsObject.getString("branch");
		boolean viewUncommitted = getConflictsObject.getBoolean("viewUncommitted");
		String committedView = viewUncommitted ? "uncommitted" : "committed";
		//boolean filterUsers = getConflictsObject.getBoolean("filterUsers");
		//JSONArray filteredUsers = getConflictsObject.getJSONArray("filteredUsers");
		//JSONArray compareAdditionalBranches = getConflictsObject.getJSONArray("compareAdditionalBranches");
		
		JSONObject responseObject = new JSONObject();
		
		PreparedStatement stmt = con.prepareStatement(
			"SELECT filename, mysha, theirusername, theirfilename, theirsha FROM (\n" + 
			"\n" + 
			"	# all possible files of me X (all possible files X all others)\n" + 
			"	SELECT me.filename AS filename, me.sha AS mysha, them.username AS theirusername, them.filename AS theirfilename, them.sha AS theirsha FROM (\n" + 
			"\n" + 
			"		# all the files X me\n" + 
			"		SELECT filelist.filename AS filename, f.sha AS sha FROM filelist\n" + 
			"		LEFT OUTER JOIN (\n" + 
			"			SELECT * FROM files\n" + 
			"			WHERE files.username = ? # my user\n" + 
			"			AND (committed = ? OR committed = 'both') # committed or uncommitted\n" + 
			"			AND branch = ? # my branch\n" + 
			"			AND repositoryalias = ? # repository\n" + 
			"		) AS f ON filelist.filename = f.filename\n" + 
			"\n" + 
			"	) AS me\n" + 
			"\n" + 
			"	CROSS JOIN\n" + 
			"\n" + 
			"	(\n" + 
			"\n" + 
			"		# all the files of all the other people\n" + 
			"		SELECT filelistxusers.username, filelistxusers.filename, f.sha FROM (\n" + 
			"\n" + 
			"			# all the filenames X all the users\n" + 
			"			SELECT u.username AS username, filelist.filename AS filename FROM filelist\n" + 
			"			CROSS JOIN (SELECT DISTINCT username FROM useraccess WHERE repositoryalias = ?) AS u # repository\n" + 
			"			WHERE filelist.repositoryalias = ? AND filelist.branch = ? # repository, compare to branch\n" + 
			"\n" + 
			"		) as filelistxusers\n" + 
			"		LEFT OUTER JOIN (\n" + 
			"\n" + 
			"			# the actual files\n" + 
			"			SELECT * FROM files\n" + 
			"			WHERE (committed = ? OR committed = 'both') # committed or uncommitted\n" + 
			"			AND branch = ? # compare to branch\n" + 
			"			AND repositoryalias = ? # repository\n" + 
			"\n" + 
			"		) AS f ON filelistxusers.filename = f.filename AND filelistxusers.username = f.username\n" + 
			"\n" + 
			"	) AS them\n" + 
			"	WHERE me.filename = them.filename\n" + 
			"\n" + 
			") AS t\n" + 
			"\n" + 
			"WHERE (t.mysha IS NULL AND t.theirsha IS NOT NULL) OR (t.mysha IS NOT NULL AND t.theirsha IS NULL) OR (t.mysha IS NOT NULL AND t.theirsha IS NOT NULL AND t.mysha <> t.theirsha) # complicated cause comparison with null always returns null\n" + 
			"ORDER BY t.filename, t.theirusername"
		);
		
		/* 
1 my user
2 committed
3 branch
4 repositoryalias
5 repositoryalias
6 repository alias
7 their branch
8 committed
9 branch
10 repository
*/
		
		stmt.setString(1, username);
		stmt.setString(2, committedView);
		stmt.setString(3, branch);
		stmt.setString(4, repositoryAlias);
		stmt.setString(5, repositoryAlias);
		stmt.setString(6, repositoryAlias);
		stmt.setString(7, branch);
		stmt.setString(8, committedView);
		stmt.setString(9, branch);
		stmt.setString(10, repositoryAlias);
		
		System.out.println("LISTING NOW AND STUFF!");

		ResultSet rs = stmt.executeQuery();
		HashMap<String, JSONObject> fileMap = new HashMap<String, JSONObject>();
		JSONArray conflictList = new JSONArray();
		while (rs.next()) {
			String filename = rs.getString("filename");
			String mySha = rs.getString("mysha");
			String theirUsername = rs.getString("theirusername");
			String theirSha = rs.getString("theirsha");
			
			JSONObject conflict;
			if (!fileMap.containsKey(filename)) {
				conflict = new JSONObject();
				fileMap.put(filename, conflict);
				conflictList.put(conflict);
				conflict.put("filename", filename);
				conflict.put("users", new JSONArray());
			}
			else {
				conflict = fileMap.get(filename);
			}
			
			JSONArray conflictUsers = conflict.getJSONArray("users");
			JSONObject user = new JSONObject();
			conflictUsers.put(user);
			
			user.put("username", theirUsername);
			
			String conflictType;
			if (mySha == null && theirSha != null) conflictType = "add";
			else if (mySha != null && theirSha == null) conflictType = "remove";
			else conflictType = "modify";

			user.put("conflictType", conflictType);
			
			//System.out.println(filename + "  -  " + mysha + "  -  " + theirusername + "  -  " + theirsha);
			
		}
		//stmt.setString(1, repositoryAlias);
		//stmt.setString(2, username);
		//stmt.setString(2, branch);
		
		responseObject.put("conflicts", conflictList);
		return responseObject.toString();
	}
	
	/* old method
	public JSONArray getFileConflicts() throws SQLException {
		JSONArray fileConflicts = new JSONArray();
		
		/
		PreparedStatement stmt = con.prepareStatement("select f1.repositoryalias, f1.filename, f1.username as username1, f2.username as username2, f1.branch as branch1, f2.branch as branch2 from files as f1 " +
			"cross join files as f2 " +
			"where f1.repositoryalias = 'testrepo' " +
			"and f1.repositoryalias = f2.repositoryalias " +
			"and f1.filename = f2.filename " +
			"and f1.username < f2.username " +
			"and f1.sha <> f2.sha " +
			"and f1.branch = f2.branch");
		/
		PreparedStatement stmt = con.prepareStatement("select f1.repositoryalias, f1.filename, f1.username as username1, f2.username as username2, f1.branch as branch1, f2.branch as branch2 from files as f1 " +
				"cross join files as f2 " +
				"where f1.repositoryalias = 'testrepo' " +
				"and f1.repositoryalias = f2.repositoryalias " +
				"and f1.filename = f2.filename " +
				"and f1.username < f2.username " +
				"and f1.sha <> f2.sha " +
				"and f1.branch = f2.branch " +
				"and f1.commit not in (SELECT h1.downstreamcommit FROM commithistory as h1 WHERE h1.commit = f2.commit) " +
				"and f2.commit not in (SELECT h2.downstreamcommit FROM commithistory as h2 WHERE h2.commit = f1.commit)");
		
		HashMap<List<String>, JSONObject> map = new HashMap<List<String>, JSONObject>();
		
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			JSONObject fileConflict = new JSONObject();
			String filename = rs.getString("filename");
			String branch = rs.getString("branch1");
			String username1 = rs.getString("username1");
			String username2 = rs.getString("username2");
			
			try {
				
				List<String> key = Arrays.asList(filename, branch);
				
				if (!map.containsKey(key)) {
					map.put(key, fileConflict);
					fileConflict.put("filename", filename);
					fileConflict.put("branch", branch);
					
					JSONArray involvedUsers = new JSONArray();
					
					fileConflict.put("involvedUsers", involvedUsers);
					involvedUsers.put(username1);
					involvedUsers.put(username2);

					fileConflicts.put(fileConflict);
				}
				else {
					JSONArray involvedUsers = map.get(key).getJSONArray("involvedUsers");
					if (!JSONHelper.contains(involvedUsers, username1)) involvedUsers.put(username1);
					if (!JSONHelper.contains(involvedUsers, username2)) involvedUsers.put(username2);
					
				}
			} catch (JSONException e) {
				System.err.println("Error while creating JSON string.");
				e.printStackTrace();
			}
		}
		
		return fileConflicts;
	}
	*/
	
	/* REPOSITORY MANAGEMENT */
	
	public String getRepositoryInformation(String repositoryAlias) throws Exception {

		JSONArray repositoryUsers = new JSONArray();
		ResultSet rs;
		
		PreparedStatement getRepositoryUsersStmt = con.prepareStatement(
			"SELECT DISTINCT username FROM useraccess WHERE repositoryalias = ?"
		);
		
		getRepositoryUsersStmt.setString(1, repositoryAlias);
		
		rs = getRepositoryUsersStmt.executeQuery();
		while (rs.next()) {
			repositoryUsers.put(rs.getString("username"));
		}

		JSONArray repositoryBranches = new JSONArray();
		PreparedStatement getRepositoryBranchesStmt = con.prepareStatement(
			"SELECT DISTINCT branch FROM files WHERE repositoryalias = ?"
		);
		
		getRepositoryBranchesStmt.setString(1, repositoryAlias);
		
		rs = getRepositoryBranchesStmt.executeQuery();
		while (rs.next()) {
			repositoryBranches.put(rs.getString("branch"));
		}
		
		JSONObject responseObject = new JSONObject();
		
		responseObject.put("repositoryUsers", repositoryUsers);
		responseObject.put("repositoryBranches", repositoryBranches);
		
		return responseObject.toString();
	}
	
	public JSONArray getRepositories(String sessionId) throws SQLException {
		JSONArray repositoriesArray = new JSONArray();
		
		PreparedStatement stmt = con.prepareStatement(
				"SELECT DISTINCT repositories.repositoryalias, repositories.repositoryurl, repositories.repositoryowner, useraccess.username FROM repositories LEFT OUTER JOIN useraccess ON repositories.repositoryalias = useraccess.repositoryalias WHERE EXISTS (SELECT users.username FROM users JOIN usersessions AS us1 ON users.username = us1.username WHERE users.isadmin = 'true' AND us1.sessionid = ?) OR EXISTS (SELECT useraccess.username FROM useraccess JOIN usersessions AS us2 ON useraccess.username = us2.username WHERE us2.sessionid = ? AND useraccess.repositoryalias = repositories.repositoryalias) OR repositories.repositoryowner = (SELECT username FROM usersessions WHERE sessionid = ?)");

		stmt.setString(1, sessionId);
		stmt.setString(2, sessionId);
		stmt.setString(3, sessionId);
		
		ResultSet rs = stmt.executeQuery();
		HashMap<String, JSONObject> index = new HashMap<String, JSONObject>();
		while (rs.next()) {
			String repositoryAlias = rs.getString("repositoryalias");
			String repositoryUrl = rs.getString("repositoryurl");
			String repositoryOwner = rs.getString("repositoryowner");
			String username = rs.getString("username");
			try {
				if (!index.containsKey(repositoryAlias)) {
					JSONObject repositoryObject = new JSONObject();
					index.put(repositoryAlias, repositoryObject);
					repositoryObject.put("repositoryAlias", repositoryAlias);
					repositoryObject.put("repositoryUrl", repositoryUrl);
					repositoryObject.put("repositoryOwner", repositoryOwner);
					repositoryObject.put("users", new JSONArray());
					repositoriesArray.put(repositoryObject);
				}
				if (username != null) {
					index.get(repositoryAlias).getJSONArray("users").put(username);
				}
			} catch (JSONException e) {
				System.err.println("Error while creating JSON string.");
				e.printStackTrace();
			}
		}
		
		return repositoriesArray;
	}
	
	public void addRepository(String repositoryAlias, String repositoryUrl, String repositoryOwner) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("INSERT INTO repositories (repositoryalias, repositoryurl, repositoryowner) VALUES (?, ?, ?)");

		stmt.setString(1, repositoryAlias);
		stmt.setString(2, repositoryUrl);
		stmt.setString(3, repositoryOwner);
		
		stmt.executeUpdate();
	}
	
	public void deleteRepository(String repositoryAlias) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("DELETE FROM repositories WHERE repositoryalias = ?");

		stmt.setString(1, repositoryAlias);
		
		stmt.executeUpdate();
	}
	
	/* USER MANAGEMENT */

	public JSONArray getUsers() throws SQLException {
		JSONArray usersArray = new JSONArray();
		
		PreparedStatement stmt = con.prepareStatement("SELECT username, isadmin, iscreator FROM users");
		
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String username = rs.getString("username");
			String isAdmin = rs.getString("isadmin");
			String isCreator = rs.getString("iscreator");
			try {
				JSONObject userObject = new JSONObject();
				userObject.put("username", username);
				userObject.put("isAdmin", isAdmin.equals("true"));
				userObject.put("isCreator", isCreator.equals("true"));
				usersArray.put(userObject);
			} catch (JSONException e) {
				System.err.println("Error while creating JSON string.");
				e.printStackTrace();
			}
		}
		
		return usersArray;
	}
	
	public void addUser(String username, String password) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("INSERT INTO users (username, passwordhash, isadmin, iscreator) VALUES (?, ?, 'false', 'false')");

		String passwordHash = DigestUtils.sha1Hex(salt + password).toString();
		
		stmt.setString(1, username);
		stmt.setString(2, passwordHash);
		
		stmt.executeUpdate();
	}
	
	public void deleteUser(String username) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("DELETE FROM users WHERE username = ?");

		stmt.setString(1, username);
		
		stmt.executeUpdate();
	}

	public void makeUserAdmin(String username) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("UPDATE users SET isadmin = 'true' WHERE username = ?");

		stmt.setString(1, username);
		
		stmt.executeUpdate();
	}
	

	public void revokeUserAdmin(String username) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("UPDATE users SET isadmin = 'false' WHERE username = ?");

		stmt.setString(1, username);
		
		stmt.executeUpdate();
	}

	public void makeUserCreator(String username) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("UPDATE users SET iscreator = 'true' WHERE username = ?");

		stmt.setString(1, username);
		
		stmt.executeUpdate();
	}

	public void revokeUserCreator(String username) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("UPDATE users SET iscreator = 'false' WHERE username = ?");

		stmt.setString(1, username);
		
		stmt.executeUpdate();
	}
	
	public void modifyRepositoryOwner(String repositoryAlias, String username) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("UPDATE repositories SET repositoryowner = ? WHERE repositoryalias = ?");

		stmt.setString(1, username);
		stmt.setString(2, repositoryAlias);
		
		stmt.executeUpdate();
	}

	public void addUserToRepository(String username, String repositoryAlias) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("INSERT INTO useraccess (username, repositoryalias) VALUES (?, ?)");

		stmt.setString(1, username);
		stmt.setString(2, repositoryAlias);
		
		stmt.executeUpdate();
	}

	public void deleteUserFromRepository(String username, String repositoryAlias) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("DELETE FROM useraccess WHERE username = ? AND repositoryalias = ?");

		stmt.setString(1, username);
		stmt.setString(2, repositoryAlias);
		
		stmt.executeUpdate();
	}
		
	public String getNewSessionIdForCorrectLogin(String username, String password) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("SELECT username FROM users WHERE username = ? AND passwordhash = ?");

		String passwordHash = DigestUtils.sha1Hex(salt + password).toString();
		
		stmt.setString(1, username);
		stmt.setString(2, passwordHash);
		
		ResultSet rs = stmt.executeQuery();
		// username/password correct
		if (rs.next()) {
			// create a session ID
			return getRandomHexString(32);
		}
		else {
			// give no session back
			return null;
		}
	}
	
	public void persistSessionIdForUser(String sessionId, String username) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("INSERT INTO usersessions (sessionid, username, expires) VALUES (?, ?, NOW() + INTERVAL 1 DAY)");

		stmt.setString(1, sessionId);
		stmt.setString(2, username);
		
		stmt.executeUpdate();
	}
	
	/* AUTHORIZATION CHECKS */
	
	public boolean doesUserHaveRepositoryAccess(String sessionId, String repositoryAlias) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("SELECT useraccess.username FROM usersessions JOIN useraccess ON usersessions.username = useraccess.username WHERE usersessions.sessionid = ? AND useraccess.repositoryalias = ?");

		stmt.setString(1, sessionId);
		stmt.setString(2, repositoryAlias);
		
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean isUserAdmin(String sessionId) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("SELECT users.username FROM usersessions JOIN users ON usersessions.username = users.username WHERE usersessions.sessionid = ? and users.isadmin = 'true'");

		stmt.setString(1, sessionId);
		
		ResultSet rs = stmt.executeQuery();
		return rs.next();
	}

	public boolean isUserCreator(String sessionId) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("SELECT users.username FROM usersessions JOIN users ON usersessions.username = users.username WHERE usersessions.sessionid = ? and users.iscreator = 'true'");

		stmt.setString(1, sessionId);
		
		ResultSet rs = stmt.executeQuery();
		return rs.next();
	}

	public boolean isUserRepositoryOwner(String sessionId, String repositoryAlias) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("SELECT repositories.username FROM usersessions JOIN repositories ON usersessions.username = repositories.username WHERE usersessions.sessionid = ? AND repositories.repositoryalias = ? AND repositories.repositoryowner = usersessions.username");

		stmt.setString(1, sessionId);
		stmt.setString(2, repositoryAlias);
		
		ResultSet rs = stmt.executeQuery();
		return rs.next();
	}

	public String getUsername(String sessionId) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("SELECT username FROM usersessions WHERE sessionid = ?");

		stmt.setString(1, sessionId);
		
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			return rs.getString("username");
		}
		else {
			return null;
		}
	}

	/* THE RANDOM STRING FUNCTION FOR SESSION IDS */
	
	private String getRandomHexString(int numchars){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < numchars){
            sb.append(Integer.toHexString(r.nextInt()));
        }

        return sb.toString().substring(0, numchars);
    }
}

