package org.imixs.crypt.rest;

import javax.ws.rs.core.MediaType;

import org.imixs.crypt.server.CryptSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for KeyService
 * 
 * @author rsoika
 * 
 */
public class KeyTest {

	String PASSWORD="abc";
	/**
	 * Open Session
	 */ 
	@Before
	public void setup() {
		RestClient restClient = new RestClient();
		String uri = "http://localhost:8080/rest/session/open";
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
	 * Close session
	 */
	@After
	public void teardown() {

		RestClient restClient = new RestClient();
		String uri = "http://localhost:8080/rest/session/close";
		try { 
			restClient.setMediaType(MediaType.TEXT_PLAIN);
			int httpResult = restClient.delete(uri, "");
			// expected result 200
			Assert.assertEquals(200, httpResult);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	/**
	 * Test get public key
	 */
	@Test
	public void testGetPublicKey() {

		String uri = "http://localhost:8080/rest/keys/public/a.b@imixs.org";
		int httpResponse;

		try {
			RestClient restClient = new RestClient();
			httpResponse = restClient.get(uri);
			String content = restClient.getContent();
			
			System.out.println(content);
			
			Assert.assertEquals(200, httpResponse);
			Assert.assertTrue(!content.isEmpty());
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}


	}
}
