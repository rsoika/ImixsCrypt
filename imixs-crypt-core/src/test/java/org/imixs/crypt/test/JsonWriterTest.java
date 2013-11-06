package org.imixs.crypt.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.imixs.crypt.json.JSONWriter;
import org.imixs.crypt.xml.IdentityItem;
import org.imixs.crypt.xml.MessageItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class test JSONWriter
 * 
 * @author rsoika
 * 
 */
public class JsonWriterTest {

	static final String FILE_PATH = "src/test/resources/json/identitytest.json";

	private final static Logger logger = Logger.getLogger(JsonWriterTest.class
			.getName());

	/**
	 */
	@Before
	public void setup() {
		// first check to delete test file
		File keyFile = new File(FILE_PATH);
		if (keyFile.exists())
			keyFile.delete();
	}

	/**
	 *
	 */
	@After
	public void teardown() {

	}

	/**
	 * Test filewriter
	 */
	@Test
	public void writeIdentityToFile() {
		IdentityItem identity = new IdentityItem();

		identity.setId("test");
		identity.setKey("mykey");

		try {
			JSONWriter.writeFile(identity, FILE_PATH);

			// test content of new file...
			String jsonContent = readFile(FILE_PATH);

			logger.info(jsonContent);
			
			Assert.assertTrue(jsonContent.contains("{\"id\":\"test\",\"key\":\"mykey\"}"));
		} catch (IOException e) {
			Assert.fail();
			e.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * Test filewriter
	 */
	@Test
	public void writeMessageToFile() {
		MessageItem message = new MessageItem();

		message.setMessage("TEST DATA");

		
			String jsonContent =	JSONWriter.toString(message);

			// test content of new file...
			
			logger.info(jsonContent);
			
			Assert.assertTrue(jsonContent.contains("\"message\":\"TEST DATA\""));
	
	}
	
	
	
	
	
	
	
	
	
	

	private String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}

		reader.close();
		return stringBuilder.toString();
	}

}
