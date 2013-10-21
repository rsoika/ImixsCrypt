package org.imixs.crypt.rest;

import javax.ws.rs.core.MediaType;

import org.imixs.crypt.server.CryptSession;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for open close a session
 * @author rsoika
 * 
 */
public class SessionTest {

	
	
	/**
	* post password to open session
	 */
	@Test
	public void testOpenSession() { 
 
		RestClient restClient = new RestClient();
 
		String uri = "http://localhost:8080/rest/session/open";
		// create a json test string
		String value = "abcdefg";
		
		try { 
			restClient.setMediaType(MediaType.TEXT_PLAIN);
			int httpResult = restClient.post(uri, value);

			String sContent=restClient.getContent();
			// expected result 200
			Assert.assertEquals(200, httpResult);
			
		Assert.assertEquals(value,CryptSession.getInstance().getPassword());
		
			
			
		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}

	}
	
	
	
	
	
	/**
	* post password to open session
	 */
	@Test
	public void testCloseSession() {

		RestClient restClient = new RestClient();
		restClient.setMediaType(MediaType.TEXT_PLAIN);
		String uri = "http://localhost:8080/rest/session/close";
	
		
		try { 
			int httpResult = restClient.delete(uri, "");

			String sContent=restClient.getContent();
			// expected result 200
			Assert.assertEquals(200, httpResult);
			
		} catch (Exception e) {

			e.printStackTrace();
			Assert.fail();
		}

	}
	
	
	
	
	
	
	
	
	
	
}
