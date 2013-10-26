package org.imixs.crypt.rest;

import java.io.File;

import javax.ws.rs.core.MediaType;

import org.imixs.crypt.RestClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This test class generates key pairs and open/closes a password protected
 * crypt session
 * 
 * @author rsoika
 * 
 */
public class SessionTest { 
	String PASSWORD = "abc";
	String HOST = "http://127.0.0.1:4040";
	
	/**
	 * generate key pair
	 */
	@Before
	public void setup() {

		try {
			File keyFile = new File("src/test/resources/id");
			if (!keyFile.exists()) {
				CryptSession.getInstance().setPassword(PASSWORD);
				CryptSession.getInstance().generateKeyPair();
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	/**
	 * Close session
	 */
	@After
	public void teardown() {

	}

	/**
	 * Test generating a new key pair
	 */
	@Test
	public void testGenerationOfKeyPair() {
		RestClient restClient = new RestClient();
 
		// first check to delete a key pair.
		File keyFile = new File("src/test/resources/keys/id");
		if (keyFile.exists())
			keyFile.delete();

		keyFile = new File("src/test/resources/keys/id.pub");
		if (keyFile.exists())
			keyFile.delete();

		keyFile = new File("src/test/resources/keys/id");
		Assert.assertFalse(keyFile.exists());

		keyFile = new File("src/test/resources/keys/id.pub");
		Assert.assertFalse(keyFile.exists());

		// generate new key by opening a session with a password
		String uri = HOST + "/rest/session";
		String value = "some-password";
		try { 
			restClient.setMediaType(MediaType.TEXT_PLAIN);
			int httpResult = restClient.post(uri, value);
			// expected result 200
			Assert.assertEquals(200, httpResult);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} 

		// now new key files should exist

		keyFile = new File("src/test/resources/keys/id");
		Assert.assertTrue(keyFile.exists());

		keyFile = new File("src/test/resources/keys/id.pub");
		Assert.assertTrue(keyFile.exists());

	}

	/**
	 * Test GET the local public key
	 */
	@Test
	public void testGetSession() {

		RestClient restClient = new RestClient();

		String uri = HOST + "/rest/session/";
		try {
			restClient.setMediaType(MediaType.TEXT_PLAIN);
			int httpResult = restClient.get(uri);

			String sContent = restClient.getContent();

			Assert.assertEquals(200, httpResult);

			Assert.assertTrue(sContent.length() > 64);

		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}

		// next delete key pair and test again - http result 202 exptected
		File keyFile = new File("src/test/resources/keys/id");
		if (keyFile.exists())
			keyFile.delete();
		keyFile = new File("src/test/resources/keys/id.pub");
		if (keyFile.exists())
			keyFile.delete();

		keyFile = new File("src/test/resources/keys/id");
		Assert.assertFalse(keyFile.exists());

		keyFile = new File("src/test/resources/id.pub");
		Assert.assertFalse(keyFile.exists());

		// now call rest service....

		try {
			restClient.setMediaType(MediaType.TEXT_PLAIN);
			int httpResult = restClient.get(uri);

			Assert.assertEquals(202, httpResult);
		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}

	}

}
