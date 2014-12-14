package ch.ethz.fgremper.rtca;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.json.JSONArray;
import org.json.JSONObject;

public class LocalMain {

	// stuff we read from config
	static String serverUrl;
	static String username;
	static LinkedList<RepositoryInfo> repositoriesList = new LinkedList<RepositoryInfo>();

	public static void main(String[] args) throws Exception {

		/* READ CONFIG */
		
		XMLConfiguration config = new XMLConfiguration("config.xml");

		username = config.getString("username");
		if (username == null) throw new Exception("No username in config!");
		System.out.println("Config -- username: " + username);
		
		serverUrl = config.getString("serverUrl");
		if (serverUrl == null) throw new Exception("No serverUrl in config!");
		System.out.println("Config -- serverUrl: " + serverUrl);
		
		boolean atLeastOneRepository = false;
		for (int i = 0; ; i++) {
			String repositoryAlias = config.getString("repositories.repository(" + i + ").alias");
			String repositoryLocalPath = config.getString("repositories.repository(" + i + ").localPath");
			if (repositoryAlias == null) break;
			if (repositoryLocalPath == null) break;
			atLeastOneRepository = true;
			System.out.println("Config -- repository(" + i + "): \"" + repositoryAlias + "\" (" + repositoryLocalPath + ")");
			repositoriesList.add(new RepositoryInfo(repositoryAlias, repositoryLocalPath));
		}
		if (!atLeastOneRepository) throw new Exception("No repositories in config!");

		
		
		/* FOR ALL REPOSITORIES: READ FILES AND SEND TO SERVER */
		
		for (RepositoryInfo repositoryInfo : repositoriesList) {

			/* READ FILES */
			
			JSONArray fileArray = new JSONArray();
			
			System.out.println("Active repository: \"" + repositoryInfo.alias + "\" (" + repositoryInfo.localPath + ")");

			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			Repository repository = builder.setGitDir(new File(repositoryInfo.localPath + "/.git"))
				.setMustExist(true)
				.readEnvironment() // scan environment GIT_* variables
				.findGitDir() // scan up the file system tree
				.build();
			
			if (repository.getDirectory() == null) {
				System.err.println("This is not a git repository.");
				return;
			}
	        System.out.println("Having repository: " + repository.getDirectory());
	        
	        Map<String, Ref> allRefs = repository.getAllRefs();
	        Collection<Ref> values = allRefs.values();
	        for (Ref ref : values) {
	        	System.out.println("Ref: " + ref.getName());
	        	if (ref.getName().startsWith("refs/heads/")) {
	        		// this is a local branch
	
	        		Ref head = repository.getRef(ref.getName());
	
	        		// a RevWalk allows to walk over commits based on some filtering that is
	        		// defined
	        		RevWalk walk = new RevWalk(repository);
	
		            RevCommit commit = walk.parseCommit(head.getObjectId());
		            RevTree tree = commit.getTree();
		            System.out.println("Tree: " + tree);
		
		            // now use a TreeWalk to iterate over all files in the Tree recursively
		            // you can set Filters to narrow down the results if needed
		            TreeWalk treeWalk = new TreeWalk(repository);
		            treeWalk.addTree(tree);
		            treeWalk.setRecursive(true);
		            while (treeWalk.next()) {
		            	// iterate through files in the latest commit of a branch
		            	System.out.println("File: " + treeWalk.getPathString());

						JSONObject fileObject = new JSONObject();
						fileObject.put("filename", treeWalk.getPathString()); // TODO: send some sort of relative filename here
						
						// read the file content
		            	ObjectId objectId = treeWalk.getObjectId(0);
		            	ObjectLoader loader = repository.open(objectId);
	
		            	InputStream fileInputStream = loader.openStream();
	            		
		            	String fileContent = IOUtils.toString(fileInputStream, "UTF-8");
		            	System.out.println(fileContent);

						fileObject.put("content", fileContent);
						fileObject.put("branch", ref.getName().substring("ref/heads/".length() + 1));
						
						fileArray.put(fileObject);
		            }
		            
		            if (ref.getName().equals(repository.getFullBranch())) {
		            	System.out.println("THIS IS MY CURRENT BRANCH BTW!");
		            	// this is my currently checked out branch.
		            	// read the files from filesystem also!
		            }
	        	}
	            
	        }
	        
	        String jsonOutputString = fileArray.toString();
			System.out.println("Created JSON string: " + jsonOutputString);

			/* SEND JSON STRING TO SERVER */
			
			String url = serverUrl + "/push/" + repositoryInfo.alias + "/" + username;

			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("PUT");
			con.setDoOutput(true);
			OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
			out.write(jsonOutputString);
			out.close();

			int responseCode = con.getResponseCode();

			System.out.println("Response code from server: " + responseCode);
	        
		}
		
	}

}
