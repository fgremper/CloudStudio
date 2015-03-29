package ch.ethz.fgremper.rtca;

import java.io.File;
import java.io.InputStream;
import java.sql.SQLException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.json.JSONArray;
import org.json.JSONObject;

public class ContentConflictGitReader {

	private static final Logger log = LogManager.getLogger(ContentConflictGitReader.class);
	
	private String mySha = null;
	private String theirSha = null;
	private String ancestorSha = null;

	String fileStorageDirectory = ServerConfig.getInstance().fileStorageDirectory;
	
	public ContentConflictGitReader(String repositoryAlias, String branch, String filename, String sessionUsername, String compareToBranch, String theirUsername, boolean showUncommitted) {


		DatabaseConnection db = new DatabaseConnection();
		try {
			// Get database connection
			db.getConnection();
			
			
			mySha = db.getFileSha(repositoryAlias, sessionUsername, branch, filename, showUncommitted);
		 	theirSha = db.getFileSha(repositoryAlias, theirUsername, compareToBranch, filename, showUncommitted);

		 	
		 	if (mySha == null) {

		 		mySha = DigestUtils.sha1Hex("").toString();
    			FileUtils.writeStringToFile(new File(fileStorageDirectory + "/" + mySha), "");
		 	}
		 	
		 	if (theirSha == null) {

		 		theirSha = DigestUtils.sha1Hex("").toString();
    			FileUtils.writeStringToFile(new File(fileStorageDirectory + "/" + theirSha), "");
		 	}
		 	
		 	
		 	
		 	/*
			String myFileContent;
			if (mySha != null) {
				myFileContent = FileUtils.readFileToString(new File(fileStorageDirectory + "/" + db.getFileSha(repositoryAlias, sessionUsername, branch, filename, showUncommitted)), "UTF-8");
			}
			else {
				myFileContent = "";
			}
			*/
			String myCommitId = db.getCommitForBranchAndFile(repositoryAlias, sessionUsername, branch, filename);
			
			
			
			String theirCommitId = db.getCommitForBranchAndFile(repositoryAlias, theirUsername, compareToBranch, filename);
			/*log.info("C2: " + theirCommitId);
			String theirFileContent = FileUtils.readFileToString(new File(fileStorageDirectory + "/" + db.getFileSha(repositoryAlias, theirUsername, compareToBranch, filename, showUncommitted)), "UTF-8");
			log.info("Content: " + theirFileContent);
			log.info("MC: " + mergeBaseCommitId);*/
			
			String mergeBaseCommitId = db.getMergeBaseCommitId(repositoryAlias, myCommitId, theirCommitId);
			
			if (mergeBaseCommitId != null) {
				// get origin storage dir
				String originStorageDirectory = ServerConfig.getInstance().originStorageDirectory;
				String repositoryOriginDirectory = originStorageDirectory + "/" + repositoryAlias + "." + db.getRepositoryCloneCount(repositoryAlias);
				
				// Open the repository in JGit
				log.info("Reading: " + repositoryOriginDirectory);
				FileRepositoryBuilder builder = new FileRepositoryBuilder();
				builder.setGitDir(new File(repositoryOriginDirectory + "/.git"));
				builder.setMustExist(true);
				builder.readEnvironment(); // scan environment GIT_* variables
				builder.findGitDir(); // scan up the file system tree
				Repository repository = builder.build();
		
				// Check, is this an actual git repository?
				if (repository.getDirectory() == null) {
					throw new Exception("Not a git repository: " + repositoryOriginDirectory);
				}
		        log.info("Opened in jgit: " + repository.getDirectory());
		
		        // Walk
				ObjectId id = repository.resolve(mergeBaseCommitId);
				log.info("Id: " + id);
				RevWalk walk = new RevWalk(repository);
				RevCommit commit = walk.parseCommit(id);
				log.info("Commit: " + commit);
		        RevTree tree = commit.getTree();
		        
		        // Get a TreeWalk to walk through all the files of the commit
		        TreeWalk treeWalk = new TreeWalk(repository);
		        treeWalk.addTree(tree);
		        treeWalk.setRecursive(true);
		
		        // Iterate through the files (maybe some way to find it faster?)
		        boolean ancestorFound = false;
		        while (treeWalk.next() && !ancestorFound) {
		        	if (treeWalk.getPathString().equals(filename)) {
		        		log.info("found file");
		        		
		            	ObjectId objectId = treeWalk.getObjectId(0);
		            	ObjectLoader loader = repository.open(objectId);
		            	InputStream fileInputStream = loader.openStream();
		            	String ancestorFileContent = IOUtils.toString(fileInputStream, "UTF-8");
		            	
		            	log.info("Content MC: " + ancestorFileContent);
		            	
		            	ancestorSha = DigestUtils.sha1Hex(ancestorFileContent).toString();
		    			FileUtils.writeStringToFile(new File(fileStorageDirectory + "/" + ancestorSha), ancestorFileContent);
		
		    			ancestorFound = true;
		        	}
		        }
		        
		        if (!ancestorFound) {

	            	ancestorSha = DigestUtils.sha1Hex("").toString();
	    			FileUtils.writeStringToFile(new File(fileStorageDirectory + "/" + ancestorSha), "");
		        }
        
			}
			else {
				// No merge base found -> ancestor is empty
            	ancestorSha = DigestUtils.sha1Hex("").toString();
    			FileUtils.writeStringToFile(new File(fileStorageDirectory + "/" + ancestorSha), "");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// Close database connection
				db.closeConnection();
		
	}
	
	public String getMyContent() {
		return null;
	}
	
	public String getTheirContent() {
		return null;
	}
	
	public String getAncestorContent() {
		return null;
	}
	
	public JSONArray diff() throws Exception {
		return SideBySideThreeWayDiff.diff(fileStorageDirectory + "/" + mySha, fileStorageDirectory + "/" + ancestorSha, fileStorageDirectory + "/" + theirSha);
			
	}
	
	public Integer countConflicts() throws Exception {
		if (ancestorSha != null)
			return SideBySideThreeWayDiff.countConflicts(fileStorageDirectory + "/" + mySha, fileStorageDirectory + "/" + ancestorSha, fileStorageDirectory + "/" + theirSha);
		else
			return 0;
	}
	
}
