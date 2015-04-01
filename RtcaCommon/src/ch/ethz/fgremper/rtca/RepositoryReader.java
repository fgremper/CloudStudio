package ch.ethz.fgremper.rtca;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * Uses JGit to read a local git repository.
 * 
 * @author Fabian Gremper
 * 
 */
public class RepositoryReader {

	private static final Logger log = LogManager.getLogger(RepositoryReader.class);
	
	private JSONObject updateObject;
	
	/**
	 * 
	 * Reads a local git repository and builds an update JSON object to send to the RTCA server.
	 * 
	 * @param localPath path to the local git repository
	 * 
	 * @throws Exception
	 * 
	 */
	public RepositoryReader(String localPath) throws Exception {
		
		// Create the object that we're going to send to the server
		updateObject = new JSONObject();
		
		// Add the file array and commit history to the object
		JSONArray fileArray = new JSONArray();
		JSONArray commitHistory = new JSONArray();
		JSONArray branchesArray = new JSONArray();
        updateObject.put("files", fileArray);
        updateObject.put("commitHistory", commitHistory);
        updateObject.put("branches", branchesArray);
		
		// Open the repository in JGit
		log.debug("Reading: " + localPath);
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		builder.setGitDir(new File(localPath + "/.git"));
		builder.setMustExist(true);
		builder.readEnvironment(); // scan environment GIT_* variables
		builder.findGitDir(); // scan up the file system tree
		Repository repository = builder.build();
		
		// Check, is this an actual git repository?
		if (repository.getDirectory() == null) {
			throw new Exception("Not a git repository: " + localPath);
		}
        log.debug("Opened in jgit: " + repository.getDirectory());
        
        // Get all references we have in this git repository
        Collection<Ref> refs = repository.getAllRefs().values();
        
        // List of all commits we came along
        List<String> usedCommitIds = new LinkedList<String>();
        
        List<String> usedBranchNames = new LinkedList<String>();
        
        // Loop through all references
        for (Ref ref : refs) {
        	log.debug("Ref: " + ref.getName());
        	
        	// Is this a reference to a local branch?
        	if (ref.getName().startsWith("refs/heads/") || ref.getName().startsWith("refs/remotes/origin/")) {
        		
        		// Get branch name
        		String branchName;
        		if (ref.getName().startsWith("refs/heads/")) {
        			branchName = ref.getName().substring("ref/heads/".length() + 1);
        		}
        		else {
        			branchName = ref.getName().substring("refs/remotes/origin/".length());
        		}
        		
        		// Skip "HEAD"
        		if (branchName.equals("HEAD")) continue;
        		
        		// Only use a branch once (we get it twice maybe because we have a reference
        		// from refs/heads and one from refs/remotes/origin.
        		if (usedBranchNames.contains(branchName)) {
        			continue;
        		}
        		usedBranchNames.add(branchName);
        		
        		// Active branche? (currently checked out)
        		boolean isActiveBranch = ref.getName().equals(repository.getFullBranch());
        		
        		// Get variables necessary to do the following actions
        		Ref branch = repository.getRef(ref.getName());
        		RevWalk walk = new RevWalk(repository);
        		RevCommit commit = walk.parseCommit(branch.getObjectId());
	            RevTree tree = commit.getTree();
	            String commitId = commit.getName();
	            log.debug("Commit: " + commit.getName());

            	// Store branch
	            JSONObject branchObject = new JSONObject();
	            branchObject.put("branch", branchName);
	            branchObject.put("commit", commitId);
	            branchObject.put("active", isActiveBranch);
	            branchesArray.put(branchObject);
	            
	            // If we didn't already, add the current commit to the commit history
	            if (!usedCommitIds.contains(commitId)) {
	            	
	            	usedCommitIds.add(commitId);
	            	
	            	// New commit object
	            	JSONObject commitObject = new JSONObject();
	            	JSONArray downstreamCommitsObject = new JSONArray();
	            	
		            commitObject.put("commit", commitId);
		            commitObject.put("downstreamCommits", downstreamCommitsObject);
		            commitHistory.put(commitObject);

		            List<RevCommit> todoCommits = new LinkedList<RevCommit>();
		            List<String> downstreamCommits = new LinkedList<String>();

		            // Put current commit in commit history with distance 0.
		            // This is necessary for some queries concerning uncommitted files in the server.
	            	JSONObject downstreamCommit = new JSONObject();
	            	downstreamCommit.put("commit", commitId);
	            	downstreamCommit.put("distance", 0);
	            	downstreamCommitsObject.put(downstreamCommit);

	            	// Put all direct parents of a commit into the todo list
		            HashMap<RevCommit, Integer> todoCommitsDistance = new HashMap<RevCommit, Integer>();
		            if (commit.getParents() != null) { 
		            	todoCommits.addAll(Arrays.asList(commit.getParents()));
		            	for (RevCommit revCommit : Arrays.asList(commit.getParents())) {
		            		todoCommitsDistance.put(revCommit, 1);
		            	}
		            }
		            
		            // Handle all commits and their parents from the todo list until it's empty
		            // (we traversed the whole graph)
		            while (!todoCommits.isEmpty()) {
		            	RevCommit pop = todoCommits.remove(0);
		            	RevCommit currentCommit = walk.parseCommit(pop.getId());
		            	Integer distanceToCurrentCommit = todoCommitsDistance.get(pop);
	            		
		            	// Handle commit we haven't handled before
		            	if (!downstreamCommits.contains(currentCommit.getName())) {
			            	downstreamCommits.add(currentCommit.getName());
			            	downstreamCommit = new JSONObject();
			            	downstreamCommit.put("commit", currentCommit.getName());
			            	downstreamCommit.put("distance", distanceToCurrentCommit);
			            	downstreamCommitsObject.put(downstreamCommit);
				            if (currentCommit.getParents() != null) {
				            	todoCommits.addAll(Arrays.asList(currentCommit.getParents()));
				            	for (RevCommit revCommit : Arrays.asList(currentCommit.getParents())) {
				            		todoCommitsDistance.put(revCommit, distanceToCurrentCommit + 1);
				            	}
				            }
		            	}
		            }
		            
	            }
	            
	            // Get a TreeWalk to walk through all the files of the commit
	            TreeWalk treeWalk = new TreeWalk(repository);
	            treeWalk.addTree(tree);
	            treeWalk.setRecursive(true);
	            
	            // Iterate through the files and add them to the file array
	            while (treeWalk.next()) {
	            	
	            	// Get file name
	            	log.debug("File: " + treeWalk.getPathString());
					JSONObject fileObject = new JSONObject();
					fileObject.put("filename", treeWalk.getPathString());
					
					// Get file content
	            	ObjectId objectId = treeWalk.getObjectId(0);
	            	ObjectLoader loader = repository.open(objectId);
	            	InputStream fileInputStream = loader.openStream();
	            	String fileContent = IOUtils.toString(fileInputStream, "UTF-8");

	            	// Some other file properties
					fileObject.put("content", fileContent);
					fileObject.put("branch", branchName);
					fileObject.put("commit", commitId);
					
					// If we're in the active branch, there's separate.debug for uncommitted
					fileObject.put("committed", isActiveBranch ? "committed" : "both");
					
					// Store the file object in the file array
					fileArray.put(fileObject);
					
	            }
	            
	            if (isActiveBranch) {
		            
	            	// Get a TreeWalk to get the file system content
	            	treeWalk = new TreeWalk(repository);
	            	treeWalk.setRecursive(true);
	            	treeWalk.addTree(new FileTreeIterator(repository));

		            // Iterate through the files and add them to the file array
		            while (treeWalk.next()) {
		            	
		            	// Get file name
		            	log.debug("Local File: " + treeWalk.getPathString());
						JSONObject fileObject = new JSONObject();
						fileObject.put("filename", treeWalk.getPathString());

						// Get file content
						String fileContent = FileUtils.readFileToString(new File(localPath + File.separator + treeWalk.getPathString()), "UTF-8");
						
		            	// Some other file properties
						fileObject.put("content", fileContent);
						fileObject.put("branch", branchName);
						fileObject.put("commit", commitId);
						fileObject.put("committed", "uncommitted");
						
						// Store the file object in the file array
						fileArray.put(fileObject);
						
		            }
	            }
        	}
            
        }

        // log.debug("JSON String: " + updateObject.toString()); // Don't log this because the string can be really long.
        
	}
	
	/**
	 * 
	 * Get the JSON object created by the reader to send to the RTCA server as local git state.
	 * 
	 * @return JSON object to send to the RTCA server
	 * 
	 */
	public JSONObject getUpdateObject() {
		return updateObject;
	}
}
