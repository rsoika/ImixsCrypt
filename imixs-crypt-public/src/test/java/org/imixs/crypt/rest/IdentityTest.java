package org.imixs.crypt.rest;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.ws.rs.core.MediaType;

import org.imixs.crypt.util.RestClient;
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
public class IdentityTest {
	String HOST = "http://localhost:8080";

	/**
	 * Open Session
	 */
	@Before
	public void setup() {

	}

	/**
	 * Test get key
	 * */
	@Test
	public void testGetPublickey() {

		RestClient restClient = new RestClient();

		restClient.setMediaType(MediaType.APPLICATION_JSON);

		String uri = HOST + "/imixs-crypt-public/rest/identities/test";
		// create a json test string

		// uri: http://localhost:8080/imixs-crypt-public/rest/identities/sepp
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

	/**
	 * Test encrypt and decrypt a message <code>
	 * 
			{"user":"test.user@imixs.org","key":"abc"}
     * </code>
	 */
	@Test
	public void testPostPublickey() {

		RestClient restClient = new RestClient();

		restClient.setMediaType(MediaType.APPLICATION_JSON);

		String uri = HOST + "/imixs-crypt-public/rest/identities/";
		String json = "{\"user\":\"test.user@imixs.org\", \"key\":\"Hallo Welt\"}";

		uri = "http://localhost:8080/imixs-crypt-public/rest/identities/";
		json = "{\"user\":\"ralph.soika@imixs.com\", \"key\":\"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCet0H7qeQapHgIsyujUUoyurJUggYbasOt35D5qO48ik8gsFqXhys8Vkevkx31Q1S8mh9f+NQQ7ljguEvzdGjuAOOtQtJ4RhG4WqKE8O1J28YbBgmxOQlBUgL8cbJYp7egNVgqCPZNjCEj2pm1W8Zmd0rZTDAnRHST0Ztpkvk5MQIDAQAB\"}";

		try {
			int httpResult = restClient.post(uri, json);

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
