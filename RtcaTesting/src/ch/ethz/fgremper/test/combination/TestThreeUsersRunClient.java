package ch.ethz.fgremper.test.combination;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.ethz.fgremper.rtca.ClientMain;

public class TestThreeUsersRunClient {

	@Test
	public void test() throws Exception {

		
		String[] argsJohn = {"configJohn.xml"};
		String[] argsDavid = {"configDavid.xml"};
		String[] argsIsabelle = {"configIsabelle.xml"};
		
		//while (true) {
			ClientMain.main(argsJohn);
			ClientMain.main(argsDavid);
			ClientMain.main(argsIsabelle);
			/*
			
			try {
			    Thread.sleep(10000);
			} catch (InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			
		}
		*/
	}

}
