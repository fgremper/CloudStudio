package ch.ethz.fgremper.cloudstudio.testing.client;

import static org.junit.Assert.*;

import java.io.File;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import ch.ethz.fgremper.cloudstudio.client.ClientConfig;
import ch.ethz.fgremper.cloudstudio.client.ClientConfigReader;

public class ClientConfigReaderTest {

	@Test
	public void testAllInfo() throws Exception {
		FileUtils.writeStringToFile(new File("config.xml"),
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<config>" +
				"<username>John</username>" +
				"<password>johnpw</password>" +
				"<serverUrl>http://my.server.com</serverUrl>" +
				"<repositories>" +
				"<repository>" +
				"<alias>foo</alias>" +
				"<localPath>/path/to/foo</localPath>" +
				"</repository>" +
				"</repositories>" +
				"<resubmitInterval>60</resubmitInterval>" +
				"</config>");
		
		ClientConfig config = new ClientConfigReader("config.xml").getConfig();

		assertEquals(config.username, "John");
		assertEquals(config.password, "johnpw");
		assertEquals(config.serverUrl, "http://my.server.com");
		assertEquals(config.resubmitInterval, 60);
		assertEquals(config.repositoriesList.size(), 1);
		assertEquals(config.repositoriesList.get(0).alias, "foo");
		assertEquals(config.repositoriesList.get(0).localPath, "/path/to/foo");		
	}

}
