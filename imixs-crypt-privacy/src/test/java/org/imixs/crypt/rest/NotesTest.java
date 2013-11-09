package org.imixs.crypt.rest;

import java.net.CookieManager;

import javax.ws.rs.core.MediaType;

import org.imixs.crypt.json.JSONWriter;
import org.imixs.crypt.util.Base64Coder;
import org.imixs.crypt.util.RestClient;
import org.imixs.crypt.xml.MessageItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class to encrypt and decrypt messages locally with the local key pair.
 * Local messages are stored in the /data/notes directory.
 * 
 * @author rsoika
 * 
 */
public class NotesTest {
	String HOST = "http://127.0.0.1:4040";
	String PASSWORD = "abc";
	String IDENTITY = "id";
	String sessionId = null;
	CookieManager cookieManager = null;

	/**
	 * Open Session
	 */
	@Before
	public void setup() {
		RestClient restClient = new RestClient();
		String uri = HOST + "/rest/identities";
		String json = "{\"id\":\"" + IDENTITY + "\",\"key\":\"" + Base64Coder.encodeString(PASSWORD)
				+ "\"}";
		try {
			restClient.setMediaType(MediaType.APPLICATION_JSON);
			int httpResult = restClient.post(uri, json);

			// store the session cookie
			cookieManager = restClient.getCookies();

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
	public void testPostNote() {

		RestClient restClient = new RestClient();
		restClient.setCookies(cookieManager);
		restClient.setMediaType(MediaType.APPLICATION_JSON);

		String uri = HOST + "/rest/messages";
		// create a json test string
		MessageItem message = new MessageItem();
		message.setMessage(Base64Coder.encodeString("Hallo Welt"));
		message.setComment("");
		message.setCreated(1);
		message.setDigest("");
		message.setRecipient("");
		message.setSender("");
		message.setSignature("");
		// create json string....
		String json = JSONWriter.toString(message);

		try {
			int httpResult = restClient.post(uri, json);

			String sContent = restClient.getContent();

			System.out.println(sContent);
			// expected result 200
			Assert.assertEquals(200, httpResult);

			// read message list - current message digest should be contained

			uri = HOST + "/rest/messages";
			String result = null;

			// set session cookie
			restClient.setCookies(cookieManager);
			httpResult = restClient.get(uri);

			result = restClient.getContent();

			System.out.println(sContent);
			// expected result 200
			Assert.assertEquals(200, httpResult);

		

		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}

	}

	/**
	 * Test list of notes
	 */
	@Test
	public void testReadNotes() {

		RestClient restClient = new RestClient();
		restClient.setCookies(cookieManager);
		restClient.setMediaType(MediaType.APPLICATION_JSON);

		String uri = HOST + "/rest/messages/";

		try {
			int httpResult = restClient.get(uri);

			String sContent = restClient.getContent();

			System.out.println(sContent);
			// expected result 200
			Assert.assertEquals(200, httpResult);

		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}

	}

}
