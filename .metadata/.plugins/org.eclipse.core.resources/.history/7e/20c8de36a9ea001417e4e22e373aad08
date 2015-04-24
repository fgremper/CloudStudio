package ch.ethz.fgremper.cloudstudio.testing.setup;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.ethz.fgremper.cloudstudio.client.ClientMain;
import ch.ethz.fgremper.cloudstudio.server.DatabaseConnection;
import ch.ethz.fgremper.cloudstudio.server.PeriodicalAllOriginUpdater;
import ch.ethz.fgremper.cloudstudio.testing.helper.TestDBHelper;
import ch.ethz.fgremper.cloudstudio.testing.helper.TestGitHelper;

public class TestThreeUsersInitialize {

	@Test
	public void test() throws Exception {

		DatabaseConnection db = new DatabaseConnection();
		db.getConnection();
		
		TestGitHelper.setupTest();

		
		
		TestGitHelper.createFolder("John", "main");
		TestGitHelper.createFolder("John", "main/v1");
		TestGitHelper.createFolder("John", "main/v2");
		TestGitHelper.createFolder("John", "test");
		TestGitHelper.createFolder("John", "docs");
		
		TestGitHelper.writeContentToFile("John", "justafileconflict.txt", "one\ntwo\nthree\nfour\nfive");
		TestGitHelper.writeContentToFile("John", "acontentconflict.txt", "one\ntwo\nthree\nfour\nfive");
		TestGitHelper.writeContentToFile("John", "xmltest.txt", "<one>this</one>");

		TestGitHelper.createOrModifyFile("John", "main/v1/main.java");
		TestGitHelper.createOrModifyFile("John", "main/v2/main.java");
		TestGitHelper.createOrModifyFile("John", "main/v2/feature.java");
		TestGitHelper.createOrModifyFile("John", "test/foo.java");
		TestGitHelper.createOrModifyFile("John", "docs/readme.txt");
		
		TestGitHelper.createBranch("John", "test_branch");
		
		TestGitHelper.commit("John");
		
		
		TestGitHelper.push("John");

		TestGitHelper.pull("David");
		TestGitHelper.pull("Isabelle");
		
		TestGitHelper.createOrModifyFile("David", "main/v2/main.java");
		TestGitHelper.commit("David");
		


		TestGitHelper.writeContentToFile("John", "justafileconflict.txt", "one\nJohn added this.\ntwo\nthree\nfour\nfive");
		TestGitHelper.writeContentToFile("John", "acontentconflict.txt", "one\ntwo\nthree changed by john\nfour\nfive");


		TestGitHelper.writeContentToFile("David", "justafileconflict.txt", "one\ntwo\nthree\nfour\nDavid added this.\nfive");
		TestGitHelper.writeContentToFile("David", "acontentconflict.txt", "one\ntwo\nthree changed by david\nfour\nfive");

		
		TestGitHelper.createOrModifyFile("David", "test/foo.java");
		TestGitHelper.commit("David");
		

		TestGitHelper.createOrModifyFile("John", "test/foo.java");
		TestGitHelper.commit("John");

		

		TestGitHelper.writeContentToFile("John", "justafileconflict.txt", "one\nJohn added this. And changed it again without committing.\ntwo\nthree\nfour\nfive");
		TestGitHelper.writeContentToFile("John", "acontentconflict.txt", "one\ntwo\nthree changed by john uncommitted\nfour\nfive");


		TestGitHelper.writeContentToFile("David", "justafileconflict.txt", "one\ntwo\nthree\nfour\nDavid added this. And changed it again without committing.\nfive");
		TestGitHelper.writeContentToFile("David", "acontentconflict.txt", "one\ntwo\nthree changed by david uncommitted\nfour\nfive");

		TestGitHelper.writeContentToFile("David", "madeanewfilelol.txt", "hey\nwhats\nup");

				

		
		
		TestGitHelper.runPlugins();
		
		db.closeConnection();
	}

}
