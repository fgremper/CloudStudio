package ch.ethz.fgremper.cloudstudio.testing.setup;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.ethz.fgremper.cloudstudio.client.ClientMain;
import ch.ethz.fgremper.cloudstudio.server.DatabaseConnection;
import ch.ethz.fgremper.cloudstudio.server.PeriodicalAllOriginUpdater;
import ch.ethz.fgremper.cloudstudio.testing.helper.TestDBHelper;
import ch.ethz.fgremper.cloudstudio.testing.helper.TestGitHelper;

public class TestViewAsOrigin {

	@Test
	public void test() throws Exception {

		DatabaseConnection db = new DatabaseConnection();
		db.getConnection();
		
		TestGitHelper.setupTest();

		TestGitHelper.writeContentToFile("John", "default.txt", "hey im john");
		TestGitHelper.writeContentToFile("David", "default.txt", "hey im david");
		

		
		
		TestGitHelper.runPlugins();
		
		db.closeConnection();
	}

}
