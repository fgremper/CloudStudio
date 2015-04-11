package ch.ethz.fgremper.cloudstudio.testing.combination;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;

import ch.ethz.fgremper.cloudstudio.client.ClientMain;
import ch.ethz.fgremper.cloudstudio.common.RepositoryReader;
import ch.ethz.fgremper.cloudstudio.server.PeriodicalAllOriginUpdater;

public class TestThreeUsersRunClient {

	@Test
	public void test() throws Exception {
		
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
