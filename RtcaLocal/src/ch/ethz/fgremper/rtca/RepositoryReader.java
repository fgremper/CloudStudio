package ch.ethz.fgremper.rtca;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.json.JSONArray;
import org.json.JSONObject;

public class RepositoryReader {
	
	private String jsonString;
	
	public RepositoryReader(String localPath) throws Exception {
		JSONArray fileArray = new JSONArray();
		JSONArray commitHistory = new JSONArray();
		JSONObject updateObject = new JSONObject();
		
		System.out.println("[Repository] Reading: " + localPath);

		// open the repository in jgit
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		builder.setGitDir(new File(localPath + "/.git"));
		builder.setMustExist(true);
		builder.readEnvironment(); // scan environment GIT_* variables
		builder.findGitDir(); // scan up the file system tree
		Repository repository = builder.build();
		
		if (repository.getDirectory() == null) {
			throw new Exception("Not a git repository: " + localPath);
		}
        System.out.println("[Repository] Opened in jgit: " + repository.getDirectory());
        
        // get all references
        Collection<Ref> refs = repository.getAllRefs().values();
        List<String> usedCommitIds = new LinkedList<String>();
        
        for (Ref ref : refs) {
        	System.out.println("[Repository] Ref: " + ref.getName());
        	
        	// local branch?
        	if (ref.getName().startsWith("refs/heads/")) {
        		
        		Ref branch = repository.getRef(ref.getName());
        		RevWalk walk = new RevWalk(repository);
        		RevCommit commit = walk.parseCommit(branch.getObjectId());

	            RevTree tree = commit.getTree();
	            System.out.println("[Repository] Tree: " + tree);
	            
	            String commitId = commit.getName();
	            System.out.println("[Repository] Commit: " + commit.getName());
	            
	            if (!usedCommitIds.contains(commitId)) {
	            	
	            	JSONObject commitObject = new JSONObject();
	            	
	            	usedCommitIds.add(commitId);
	            	
		            // find downstream commits
		            
		            List<String> downstreamCommits = new LinkedList<String>();
		            List<RevCommit> todoCommits = new LinkedList<RevCommit>();
		            
		            if (commit.getParents() != null) todoCommits.addAll(Arrays.asList(commit.getParents()));
		            while (!todoCommits.isEmpty()) {
		            	RevCommit pop = todoCommits.remove(0);
		            	RevCommit currentCommit = walk.parseCommit(pop.getId());
		            	if (!downstreamCommits.contains(currentCommit.getName())) {
			            	downstreamCommits.add(currentCommit.getName());
				            if (currentCommit.getParents() != null) todoCommits.addAll(Arrays.asList(currentCommit.getParents()));
		            	}
		            }
		            
		            for (String u : downstreamCommits) {
		            	System.out.println("[Repository] Downstream commit: "  + u);
		            }
		            
		            commitObject.put("commit", commitId);
		            commitObject.put("downstreamCommits", downstreamCommits);
		            
		            commitHistory.put(commitObject);
					
	            }
	            
	            // get file contents and append to json string
	            
	            TreeWalk treeWalk = new TreeWalk(repository);
	            treeWalk.addTree(tree);
	            treeWalk.setRecursive(true);
	            
	            // iterate through the files of the commit
	            while (treeWalk.next()) {
	            	
	            	System.out.println("[Repository] File: " + treeWalk.getPathString());

					JSONObject fileObject = new JSONObject();
					fileObject.put("filename", treeWalk.getPathString());
					
					// read the file content
	            	ObjectId objectId = treeWalk.getObjectId(0);
	            	ObjectLoader loader = repository.open(objectId);

	            	InputStream fileInputStream = loader.openStream();
            		
	            	String fileContent = IOUtils.toString(fileInputStream, "UTF-8");

					fileObject.put("content", fileContent);
					fileObject.put("branch", ref.getName().substring("ref/heads/".length() + 1));
					fileObject.put("commit", commitId);
					
					fileArray.put(fileObject);
					
	            }
	            
	            if (ref.getName().equals(repository.getFullBranch())) {
	            	// TODO: this is the current branch, update stuff from file directory too maybe
	            }
        	}
            
        }
     
        updateObject.put("files", fileArray);
        updateObject.put("commitHistory", commitHistory);

        jsonString = updateObject.toString();
        System.out.println("[Repository] JSON String: " + jsonString);
        
	}
	
	public String getJsonString() {
		return jsonString;
	}
}
