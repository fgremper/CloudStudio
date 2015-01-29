package ch.ethz.fgremper.test.combination;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.junit.Test;

import ch.ethz.fgremper.rtca.RepositoryReader;
import ch.ethz.fgremper.rtca.UpdateHttpHandler;
import ch.ethz.fgremper.rtca.test.helper.TestDBHelper;
import ch.ethz.fgremper.rtca.test.helper.TestGitHelper;
import ch.ethz.fgremper.rtca.test.helper.TestSettings;

public class SimpleShaConflictTest {

	@Test
	public void testSimpleShaConflictNoFastForward() throws Exception {

		// reset database
		
		TestDBHelper.resetDatabase();
		TestDBHelper.addRepository("testrepo", "http://test/repository.git");
		
		// setup scenario
		
		System.out.println("[Test] Setting up scenario in sandpit");
		
		TestGitHelper.clearSandpit();
		
		TestGitHelper.createOrigin();
		
		TestGitHelper.createUser("john");
		TestGitHelper.cloneOrigin("john");
		TestGitHelper.createUser("david");
		TestGitHelper.cloneOrigin("david");
		
		TestGitHelper.createOrModifyFile("john", "test.txt");
		TestGitHelper.commit("john");
		
		TestGitHelper.createOrModifyFile("david", "test.txt");
		TestGitHelper.commit("david");
		
		// run logic
		
		List<String> involvedUsers = new LinkedList<String>();
		involvedUsers.add("john");
		involvedUsers.add("david");
		
		for (String user : involvedUsers) {
		
			System.out.println("[Test] Running plugin for user " + user);
			
			RepositoryReader repositoryReader = new RepositoryReader(TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user);
			String jsonString = repositoryReader.getJsonString();
			
			System.out.println("[Test] JSON String: " + jsonString);

			System.out.println("[Test] Executing database update for " + user);
			
			UpdateHttpHandler updateHttpHandler = new UpdateHttpHandler();
			
			boolean success = updateHttpHandler.executeDataUpdate("testrepo", user, jsonString);
			
			assertEquals(success, true);
			
		}
	
		// verify
		
		JSONArray interBranchFileConflicts = TestDBHelper.getAllInterBranchFileConflicts();
		
		System.out.println("[Test] Inter-branch file conflicts: " + interBranchFileConflicts.toString());
		assertEquals(interBranchFileConflicts.length(), 1);
		
	}

	@Test
	public void testSimpleShaConflictFastForward() throws Exception {

		// reset database
		
		TestDBHelper.resetDatabase();
		TestDBHelper.addRepository("testrepo", "http://test/repository.git");
		
		// setup scenario
		
		System.out.println("[Test] Setting up scenario in sandpit");
		
		TestGitHelper.clearSandpit();
		
		TestGitHelper.createOrigin();
		
		TestGitHelper.createUser("john");
		TestGitHelper.cloneOrigin("john");
		
		TestGitHelper.createOrModifyFile("john", "test.txt");
		TestGitHelper.commit("john");
		TestGitHelper.push("john");
		
		TestGitHelper.createUser("david");
		TestGitHelper.cloneOrigin("david");
		
		TestGitHelper.createOrModifyFile("david", "test.txt");
		TestGitHelper.commit("david");
		
		// run logic
		
		List<String> involvedUsers = new LinkedList<String>();
		involvedUsers.add("john");
		involvedUsers.add("david");
		
		for (String user : involvedUsers) {
		
			System.out.println("[Test] Running plugin for user " + user);
			
			RepositoryReader repositoryReader = new RepositoryReader(TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user);
			String jsonString = repositoryReader.getJsonString();
			
			System.out.println("[Test] JSON String: " + jsonString);

			System.out.println("[Test] Executing database update for " + user);
			
			UpdateHttpHandler updateHttpHandler = new UpdateHttpHandler();
			
			boolean success = updateHttpHandler.executeDataUpdate("testrepo", user, jsonString);
			
			assertEquals(success, true);
			
		}
	
		// verify

		JSONArray interBranchFileConflicts = TestDBHelper.getAllInterBranchFileConflicts();
		
		System.out.println("[Test] Inter-branch file conflicts: " + interBranchFileConflicts.toString());
		assertEquals(interBranchFileConflicts.length(), 0);
		
	}
	

	@Test
	public void testThreeFiles() throws Exception {

		// reset database
		
		TestDBHelper.resetDatabase();
		TestDBHelper.addRepository("testrepo", "http://test/repository.git");
		
		// setup scenario
		
		System.out.println("[Test] Setting up scenario in sandpit");
		
		TestGitHelper.clearSandpit();
		
		TestGitHelper.createOrigin();
		
		TestGitHelper.createUser("john");
		TestGitHelper.cloneOrigin("john");
		TestGitHelper.createUser("david");
		TestGitHelper.cloneOrigin("david");
		TestGitHelper.createUser("marc");
		TestGitHelper.cloneOrigin("marc");

		TestGitHelper.createOrModifyFile("john", "foo.txt");
		TestGitHelper.createOrModifyFile("john", "bar.txt");
		TestGitHelper.commit("john");

		TestGitHelper.createOrModifyFile("david", "foo.txt");
		TestGitHelper.createOrModifyFile("david", "bar.txt");
		TestGitHelper.commit("david");
		
		TestGitHelper.createOrModifyFile("marc", "foo.txt");
		TestGitHelper.commit("marc");
		
		// run logic
		
		List<String> involvedUsers = new LinkedList<String>();
		involvedUsers.add("john");
		involvedUsers.add("david");
		involvedUsers.add("marc");
		
		for (String user : involvedUsers) {
		
			System.out.println("[Test] Running plugin for user " + user);
			
			RepositoryReader repositoryReader = new RepositoryReader(TestSettings.SANDPIT_DIRECTORY_PATH + "/" + user);
			String jsonString = repositoryReader.getJsonString();
			
			System.out.println("[Test] JSON String: " + jsonString);

			System.out.println("[Test] Executing database update for " + user);
			
			UpdateHttpHandler updateHttpHandler = new UpdateHttpHandler();
			
			boolean success = updateHttpHandler.executeDataUpdate("testrepo", user, jsonString);
			
			assertEquals(success, true);
			
		}
		
	}

}
