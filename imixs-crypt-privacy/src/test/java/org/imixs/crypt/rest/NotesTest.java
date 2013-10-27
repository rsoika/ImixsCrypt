package org.imixs.crypt.rest;

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
public class NotesTest {
	String HOST = "http://127.0.0.1:4040";
	String PASSWORD = "abc";

	/**
	 * Open Session
	 */
	@Before
	public void setup() {
		RestClient restClient = new RestClient();
		String uri = HOST + "/rest/session";
		try {
			restClient.setMediaType(MediaType.TEXT_PLAIN);
			int httpResult = restClient.post(uri, PASSWORD);
			// expected result 200
			Assert.assertEquals(200, httpResult);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}

	}

	/**
	 * Test encrypt and text into a notes data file
	 */
	@Test
	public void testPostEncryptNotes() {

		RestClient restClient = new RestClient();
		restClient.setMediaType(MediaType.APPLICATION_JSON);

		String notesName = "test";

		String uri = HOST + "/rest/notes/encrypt/" + notesName;
		// create a json test string
		String message = "Hallo Welt";

		try {
			int httpResult = restClient.post(uri, message);

			String sContent = restClient.getContent();

			System.out.println(sContent);
			// expected result 200
			Assert.assertEquals(200, httpResult);

			// decrypt again....

			uri = HOST + "/rest/notes/decrypt/" + notesName;
			String result = null;

			httpResult = restClient.get(uri);

			result = restClient.getContent();

			System.out.println(sContent);
			// expected result 200
			Assert.assertEquals(200, httpResult);

			Assert.assertEquals(message, result);

		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}

	}

}
