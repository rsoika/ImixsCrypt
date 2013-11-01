package org.imixs.crypt.rest;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.ws.rs.core.MediaType;

import org.imixs.crypt.RestClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for imixs-jax-rs workflow rest api
 * 
 * The test class test encryption and decryption
 * 
 * @author rsoika
 * 
 */
public class MessageTest {
	String HOST = "http://127.0.0.1:4040";
	String PASSWORD = "abc";
	String IDENTITY = "id";
	CookieManager cookieManager = null;

	/**
	 * Open Session
	 */
	@Before
	public void setup() {
		RestClient restClient = new RestClient();
		String uri = HOST + "/rest/session/" + IDENTITY;
		try {
			restClient.setMediaType(MediaType.TEXT_PLAIN);
			int httpResult = restClient.post(uri, PASSWORD);

			// store the session cookie
			cookieManager = restClient.getCookies();

			// expected result 200
			Assert.assertEquals(200, httpResult);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

		// copy a public test key
		File pbulicKeyFile = new File("src/test/resources/keys/id.pub");
		if (!pbulicKeyFile.exists())
			Assert.fail();
		try {
			CopyOption[] options = new CopyOption[] {
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.COPY_ATTRIBUTES };
			File tragetFile = new File(
					"src/test/resources/keys/public/test.user@imixs.org.pub");
			if (tragetFile.getParentFile() != null)
				tragetFile.getParentFile().mkdirs();
			Files.copy(pbulicKeyFile.toPath(), tragetFile.toPath(), options);
		} catch (IOException e) {
			Assert.fail();
			e.printStackTrace();
		}

	}

	/**
	 * Test encrypt and decrypt a message <code>
	 * 
			{"user":"test.user@imixs.org","message":"abc"}
     * </code>
	 */
	@Test
	public void testPostEncryptDecryptJsonMessage() {

		RestClient restClient = new RestClient();
		restClient.setCookies(cookieManager);
		restClient.setMediaType(MediaType.APPLICATION_JSON);

		String uri = HOST + "/rest/message/encrypt/";
		// create a json test string
		String json = "{\"user\":\"test.user@imixs.org\", \"message\":\"Hallo Welt\"}";

		try {
			int httpResult = restClient.post(uri, json);

			String sContent = restClient.getContent();

			System.out.println(sContent);
			// expected result 200
			Assert.assertEquals(200, httpResult);

			// decrypt
			uri = HOST + "/rest/message/decrypt/";

			restClient.setCookies(cookieManager);
			httpResult = restClient.post(uri, sContent);

			sContent = restClient.getContent();

			System.out.println(sContent);

			// expected result 200
			Assert.assertEquals(200, httpResult);

			Assert.assertTrue(sContent.contains("Hallo Welt"));
			Assert.assertFalse(sContent.contains("xHallo Weltx"));

		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}

	}

}
