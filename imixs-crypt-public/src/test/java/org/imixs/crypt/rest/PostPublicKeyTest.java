package org.imixs.crypt.rest;

import org.imixs.crypt.RestClient;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for imixs-jax-rs workflow rest api
 * 
 * The test class test posting a public key
 * 
 * @author rsoika
 * 
 */
public class PostPublicKeyTest {

	
	
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
	
	
	
	
	
	
	
	
	
}
