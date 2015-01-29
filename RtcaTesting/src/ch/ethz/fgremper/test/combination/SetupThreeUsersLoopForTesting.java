package ch.ethz.fgremper.test.combination;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.ethz.fgremper.rtca.ClientMain;
import ch.ethz.fgremper.rtca.DatabaseConnection;
import ch.ethz.fgremper.rtca.test.helper.TestDBHelper;
import ch.ethz.fgremper.rtca.test.helper.TestGitHelper;

public class SetupThreeUsersLoopForTesting {

	@Test
	public void test() throws Exception {

		DatabaseConnection db = new DatabaseConnection();
		
		db.startTransaction();
		db.resetDatabase();
		db.addUser("admin", "1234");
		db.makeUserAdmin("admin");
		db.addUser("john", "johnpw");
		db.addUser("david", "davidpw");
		db.addUser("isabelle", "isabellepw");
		db.addRepository("test", "http://test/repository.git", "john");
		db.addUserToRepository("john", "test");
		db.addUserToRepository("david", "test");
		db.addUserToRepository("isabelle", "test");
		db.commitTransaction();
		
		// setup scenario
		
		System.out.println("[Test] Setting up scenario in sandpit");
		
		TestGitHelper.clearSandpit();
		
		TestGitHelper.createOrigin();
		
		TestGitHelper.createUser("john");
		TestGitHelper.cloneOrigin("john");
		TestGitHelper.createUser("david");
		TestGitHelper.cloneOrigin("david");
		TestGitHelper.createUser("isabelle");
		TestGitHelper.cloneOrigin("isabelle");

		
		String[] argsJohn = {"../RtcaClient/configJohn.xml"};
		String[] argsDavid = {"../RtcaClient/configDavid.xml"};
		String[] argsIsabelle = {"../RtcaClient/configIsabelle.xml"};
		
		while (true) {
			ClientMain.main(argsJohn);
			ClientMain.main(argsDavid);
			ClientMain.main(argsIsabelle);
			
			
			try {
			    Thread.sleep(10000);
			} catch (InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			
		}
	}

}
