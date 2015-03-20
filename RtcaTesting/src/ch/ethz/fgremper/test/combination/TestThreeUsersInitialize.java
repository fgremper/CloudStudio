package ch.ethz.fgremper.test.combination;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.ethz.fgremper.rtca.ClientMain;
import ch.ethz.fgremper.rtca.DatabaseConnection;
import ch.ethz.fgremper.rtca.PeriodicalAllOriginUpdater;
import ch.ethz.fgremper.rtca.test.helper.TestDBHelper;
import ch.ethz.fgremper.rtca.test.helper.TestGitHelper;

public class TestThreeUsersInitialize {

	@Test
	public void test() throws Exception {

		DatabaseConnection db = new DatabaseConnection();
		
		db.startTransaction();
		db.resetDatabase();
		db.addUser("Admin", "1234");
		db.makeUserAdmin("admin");
		db.addUser("John", "johnpw");
		db.addUser("David", "davidpw");
		db.addUser("Isabelle", "isabellepw");
		db.addRepository("TestRepository", "/Users/novocaine/Documents/masterthesis/testsandpit/origin", "John");
		db.addRepository("HelloWorld", "", "John");
		db.addRepository("BankAccountDemo", "", "John");
		db.addUserToRepository("John", "TestRepository");
		db.addUserToRepository("David", "TestRepository");
		db.addUserToRepository("Isabelle", "TestRepository");
		db.commitTransaction();
		
		// setup scenario
		
		System.out.println("[Test] Setting up scenario in sandpit");
		
		TestGitHelper.clearSandpit();
		
		TestGitHelper.createOrigin();
		
		TestGitHelper.createUser("John");
		TestGitHelper.cloneOrigin("John");
		TestGitHelper.createUser("David");
		TestGitHelper.cloneOrigin("David");
		TestGitHelper.createUser("Isabelle");
		TestGitHelper.cloneOrigin("Isabelle");

		TestGitHelper.createFolder("John", "main");
		TestGitHelper.createFolder("John", "main/v1");
		TestGitHelper.createFolder("John", "main/v2");
		TestGitHelper.createFolder("John", "test");
		TestGitHelper.createFolder("John", "docs");

		TestGitHelper.createOrModifyFile("John", "main/v1/main.java");
		TestGitHelper.createOrModifyFile("John", "main/v2/main.java");
		TestGitHelper.createOrModifyFile("John", "main/v2/feature.java");
		TestGitHelper.createOrModifyFile("John", "test/foo.java");
		TestGitHelper.createOrModifyFile("John", "docs/readme.txt");
		
		TestGitHelper.commit("John");
		
		
		TestGitHelper.push("John");

		TestGitHelper.pull("David");
		TestGitHelper.pull("Isabelle");
		
		TestGitHelper.createOrModifyFile("David", "main/v2/main.java");
		TestGitHelper.commit("David");
		

		TestGitHelper.createOrModifyFile("David", "test/foo.java");
		TestGitHelper.commit("David");
		

		TestGitHelper.createOrModifyFile("John", "test/foo.java");
		TestGitHelper.commit("John");

		

		String[] argsJohn = {"configJohn.xml", "--nogui"};
		String[] argsDavid = {"configDavid.xml", "--nogui"};
		String[] argsIsabelle = {"configIsabelle.xml", "--nogui"};
		
		ClientMain.main(argsJohn);
		ClientMain.main(argsDavid);
		ClientMain.main(argsIsabelle);

		PeriodicalAllOriginUpdater originUpdaterInterval = new PeriodicalAllOriginUpdater();
		originUpdaterInterval.updateAll();
			
		
	}

}
