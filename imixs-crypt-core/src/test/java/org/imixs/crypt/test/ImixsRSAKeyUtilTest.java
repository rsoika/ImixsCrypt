package org.imixs.crypt.test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.bouncycastle.util.encoders.Hex;
import org.imixs.crypt.Base64Coder;
import org.imixs.crypt.ImixsRSAKeyUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class test the ImixsCryptKeyUtil implementation ImixsRSAKeyUtil
 * 
 * @author rsoika
 * 
 */
public class ImixsRSAKeyUtilTest {

	static final String PRIVATE_KEY_FILE = "src/test/resources/private.key";
	static final String PUBLIC_KEY_FILE = "src/test/resources/public.key";

	PublicKey publicKey = null;
	PrivateKey privateKey = null;
	ImixsRSAKeyUtil rsaKeyUtil = null;

	private final static Logger logger = Logger
			.getLogger(ImixsRSAKeyUtilTest.class.getName());

	/**
	 * Generate key which contains a pair of private and public key using 1024
	 * bytes. Store the set of keys in Prvate.key and Public.key files.
	 * 
	 */
	@Before
	public void setup() {
		rsaKeyUtil = new ImixsRSAKeyUtil();
	}

	/**
	 * finally remove key files
	 */
	@After
	public void teardown() {

		// File privateKeyFile = new File(PRIVATE_KEY_FILE);
		// File publicKeyFile = new File(PUBLIC_KEY_FILE);
		// if (privateKeyFile.exists())
		// privateKeyFile.delete();
		// if (publicKeyFile.exists())
		// publicKeyFile.delete();
	}

	@Test
	public void checkAlgorithms() {
		for (Object obj : java.security.Security.getAlgorithms("Cipher")) {
			System.out.println(obj.toString());
		}
	}

	@Test
	public void generateKeyTest() throws Exception {
		rsaKeyUtil.generateKeyPair(PRIVATE_KEY_FILE, PUBLIC_KEY_FILE, null);

		// test with password
		rsaKeyUtil.generateKeyPair(PRIVATE_KEY_FILE, PUBLIC_KEY_FILE,
				"mypassword");
	}

	@Test
	public void testGetKeys() throws Exception {
		rsaKeyUtil.generateKeyPair(PRIVATE_KEY_FILE, PUBLIC_KEY_FILE, null);

		privateKey = rsaKeyUtil.getPrivateKey(PRIVATE_KEY_FILE, null);

		Assert.assertNotNull(privateKey);

		publicKey = rsaKeyUtil.getPublicKey(PUBLIC_KEY_FILE);

		Assert.assertNotNull(publicKey);
	}

	/**
	 * Test generyte key pair with password encryption
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetEncryptedKeys() throws Exception {

		String password = "mypassword";

		rsaKeyUtil.generateKeyPair(PRIVATE_KEY_FILE, PUBLIC_KEY_FILE, password);

		privateKey = rsaKeyUtil.getPrivateKey(PRIVATE_KEY_FILE, password);

		Assert.assertNotNull(privateKey);

		publicKey = rsaKeyUtil.getPublicKey(PUBLIC_KEY_FILE);

		Assert.assertNotNull(publicKey);

		// test get key with wrong password - exception expected
		try {
			privateKey = rsaKeyUtil.getPrivateKey(PRIVATE_KEY_FILE,
					"wrong-password");

			Assert.fail();
		} catch (Exception e) {
			// works!
		}

	}

	/**
	 * Simple crypt/decrypt test
	 * 
	 * @throws Exception
	 */
	@Test
	public void encryptDecryptTest() throws Exception {

		rsaKeyUtil.generateKeyPair(PRIVATE_KEY_FILE, PUBLIC_KEY_FILE, null);

		privateKey = rsaKeyUtil.getPrivateKey(PRIVATE_KEY_FILE, null);

		Assert.assertNotNull(privateKey);

		publicKey = rsaKeyUtil.getPublicKey(PUBLIC_KEY_FILE);

		Assert.assertNotNull(publicKey);

		byte[] cipherText = null;

		String originalText = "Hello World";
		
		cipherText = rsaKeyUtil.encrypt(originalText.getBytes(), publicKey);

		logger.info(cipherText.toString());

		String plainText = new String(rsaKeyUtil.decrypt(cipherText, privateKey));
		
		Assert.assertEquals(originalText, plainText);

		Assert.assertNotSame(plainText + "x", originalText);

	}

	/**
	 * crypt/decrypt a string longer than 117 bytes!
	 * 
	 * @throws Exception
	 */
	@Test
	public void encryptDecryptLongStringTest() throws Exception {

		rsaKeyUtil.generateKeyPair(PRIVATE_KEY_FILE, PUBLIC_KEY_FILE, null);

		privateKey = rsaKeyUtil.getPrivateKey(PRIVATE_KEY_FILE, null);

		Assert.assertNotNull(privateKey);

		publicKey = rsaKeyUtil.getPublicKey(PUBLIC_KEY_FILE);

		Assert.assertNotNull(publicKey);

		byte[] cipherText = null;

		String originalText = " Hellöchen Schluß World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello  World Hello d Hello World  Hello World Hello World Hello World Hello World  Hello World Hello World Hell12345671";
		//String originalText = "Hello World";
		
		originalText=originalText+originalText+originalText;
		
		
		cipherText = rsaKeyUtil.encrypt(originalText.getBytes("UTF-8"), publicKey);

		logger.info("cipher Text="+new String(cipherText));
		
		
	
		
		String plainText =new String( rsaKeyUtil.decrypt(cipherText, privateKey),"UTF-8");

		Assert.assertEquals(originalText, plainText);

		Assert.assertNotSame(plainText + "x", originalText);
		logger.info("Length Original : " + originalText.getBytes().length);
		logger.info("Length Encrypted: " + cipherText.length);
		
	
	}

	
	
	/**
	 * crypt/decrypt a string with 100 bytes...
	 * @throws Exception
	 */
	@Test
	public void encryptDecrypt100ByteStringTest() throws Exception {

		rsaKeyUtil.generateKeyPair(PRIVATE_KEY_FILE, PUBLIC_KEY_FILE, null);

		privateKey = rsaKeyUtil.getPrivateKey(PRIVATE_KEY_FILE, null);

		Assert.assertNotNull(privateKey);

		publicKey = rsaKeyUtil.getPublicKey(PUBLIC_KEY_FILE);

		Assert.assertNotNull(publicKey);

		byte[] cipherText = null;

		String originalText = " aHellchen Schluorld Hefll1423671";
		//String originalText = "Hello World";
		
		originalText=originalText+originalText+originalText;
		
		
		cipherText = rsaKeyUtil.encrypt(originalText.getBytes(), publicKey);

		logger.info("cipher Text="+new String(cipherText));
		
		
	
		
		String plainText =new String( rsaKeyUtil.decrypt(cipherText, privateKey));

		Assert.assertEquals(originalText, plainText);

		Assert.assertNotSame(plainText + "x", originalText);
		logger.info("Length Original : " + originalText.getBytes().length);
		logger.info("Length Encrypted: " + cipherText.length);
		
	
	}

	
	
	/**
	 * Simple crypt/decrypt test with encrypted Keys
	 * 
	 * @throws Exception
	 */
	@Test
	public void encryptDecryptTestWithEncryptedKey() throws Exception {

		String password = "my-password47";

		rsaKeyUtil.generateKeyPair(PRIVATE_KEY_FILE, PUBLIC_KEY_FILE, password);

		privateKey = rsaKeyUtil.getPrivateKey(PRIVATE_KEY_FILE, password);

		Assert.assertNotNull(privateKey);

		publicKey = rsaKeyUtil.getPublicKey(PUBLIC_KEY_FILE);

		Assert.assertNotNull(publicKey);

		byte[] cipherText = null;

		String originalText = "Hello World";

		cipherText = rsaKeyUtil.encrypt(originalText.getBytes(), publicKey);

		logger.info(cipherText.toString());

	String plainText =new String( rsaKeyUtil.decrypt(cipherText, privateKey));

		Assert.assertEquals(originalText,plainText);

		Assert.assertNotSame(plainText + "x", originalText);

		

	}
}
