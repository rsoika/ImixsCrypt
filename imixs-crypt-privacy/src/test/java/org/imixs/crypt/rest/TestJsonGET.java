package org.imixs.crypt.rest;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for imixs-jax-rs
 * 
 * @author rsoika
 * 
 */
public class TestJsonGET {

	/**
	 * Test get public key
	 */
	@Test
	public void testGetPublicKey() {

		String uri = "http://localhost:8080/rest/mykey/public";
		int httpResponse;

		try {
			RestClient restClient = new RestClient();
			httpResponse = restClient.getJsonEntity(uri);
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
