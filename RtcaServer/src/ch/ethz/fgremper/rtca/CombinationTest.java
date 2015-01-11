package ch.ethz.fgremper.rtca;

import static org.junit.Assert.*;

import org.junit.Test;

public class CombinationTest {


	@Test
	public void test() throws Exception {

		RepositoryReader repositoryReader = new RepositoryReader(TestSettings.SANDPIT_DIRECTORY_PATH + "/john");
		String jsonString = repositoryReader.getJsonString();
		
		System.out.println("[Test] JSON String: " + jsonString);
			
		UpdateHttpHandler updateHttpHandler = new UpdateHttpHandler();
		
		boolean success = updateHttpHandler.executeDataUpdate("testrepo", "realtestuser", jsonString);
		
		assertEquals(success, true);
		
	}

}
