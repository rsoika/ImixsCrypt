package org.imixs.crypt.rest;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for imixs-jax-rs workflow rest api
 * 
 * The test class checks post scenarios
 * 
 * @author rsoika
 * 
 */
public class TestJsonPOST {

	
	
	/**
	 * <code>
	 * 
			{"user":"ralph.soika@imixs.com","key":"xxxxxxxxxxxxxxxxxxxxxxxxxx"}
     * </code>
	 */
	@Test
	public void testPostJsonWorkitem() {

		RestClient restClient = new RestClient();

		String uri = "http://localhost:8080/rest/keys/public";
		// create a json test string
		String json = "{\"user\":\"ralph.soika@imixs.com\",\"key\":\"\"}";
		
		try {
			int httpResult = restClient.post(uri, json);

			String sContent=restClient.getContent();
			// expected result 200
			Assert.assertEquals(200, httpResult);
			
		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}

	}
	
	
	
	
	/**
	 * Test encrypt message
	 * <code>
	 * 
			{"user":"ralph.soika@imixs.com","message":"abc"}
     * </code>
	 */
	@Test
	public void testPostEncryptDecryptJsonMessage() {

		RestClient restClient = new RestClient();

		String uri = "http://localhost:8080/rest/message/encrypt/";
		// create a json test string
		String json = "{\"user\":\"ralph.soika@imixs.com\",\"message\":\"Hallo Welt\"}";
		
		try {
			int httpResult = restClient.post(uri, json);

			String sContent=restClient.getContent();
			
			System.out.println(sContent);
			// expected result 200
			Assert.assertEquals(200, httpResult);
			
			
			
			// decrypt
			
		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
