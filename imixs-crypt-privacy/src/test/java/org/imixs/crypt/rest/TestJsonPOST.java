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
			{ "mypassword" }
     * </code>
	 */
	@Test
	public void testPostJsonWorkitem() {

		RestClient restClient = new RestClient();

		String uri = "http://localhost:8080/rest/mykey";
		// create a json test string
		String json = "{ \"mypassword\" }";
		
		
		
		
		
		
		// http://www.jsonschema.net/
		try {
			int httpResult = restClient.postJsonEntity(uri, json);

			String sContent=restClient.getContent();
			// expected result 200
			Assert.assertEquals(200, httpResult);
			
		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}

	}
	
	
	
	
}
