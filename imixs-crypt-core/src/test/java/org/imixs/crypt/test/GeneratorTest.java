package org.imixs.crypt.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import junit.framework.Assert;

import org.imixs.crypt.ImixsKeyGenerator;
import org.imixs.crypt.incubator.PublicKeyReaderUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class test the imixs.crypt KeyGenerator
 * 
 * @author rsoika
 * 
 */
public class GeneratorTest {

	static final String ALGORITHM = "RSA";
	static final String PRIVATE_KEY_FILE = "src/test/resources/private.key";
	static final String PUBLIC_KEY_FILE = "src/test/resources/public.key";

	PublicKey publicKey = null;
	PrivateKey privateKey = null;

	private final static Logger logger = Logger.getLogger(GeneratorTest.class
			.getName());

	/**
	 * Generate key which contains a pair of private and public key using 1024
	 * bytes. Store the set of keys in Prvate.key and Public.key files.
	 * 
	 */
	@Before
	public void setup() {
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
	public void generateKeyTest() throws NoSuchAlgorithmException, IOException {
		ImixsKeyGenerator.generateKeyPair(PRIVATE_KEY_FILE, PUBLIC_KEY_FILE,
				ALGORITHM);
	}

	@Test
	public void testGetKeys() throws Exception {
		ImixsKeyGenerator.generateKeyPair(PRIVATE_KEY_FILE, PUBLIC_KEY_FILE,
				ALGORITHM);

		privateKey = ImixsKeyGenerator.getPemPrivateKey(PRIVATE_KEY_FILE,
				ALGORITHM);

		Assert.assertNotNull(privateKey);

		publicKey = ImixsKeyGenerator.getPemPublicKey(PUBLIC_KEY_FILE,
				ALGORITHM);

		Assert.assertNotNull(publicKey);
	}
	
	
	/**
	 * Simple crypt/decrypt test
	 * @throws Exception 
	 */
	@Test
	public void encryptDecryptTest() throws Exception {

		ImixsKeyGenerator.generateKeyPair(PRIVATE_KEY_FILE, PUBLIC_KEY_FILE,
				ALGORITHM);

		privateKey = ImixsKeyGenerator.getPemPrivateKey(PRIVATE_KEY_FILE,
				ALGORITHM);

		Assert.assertNotNull(privateKey);

		publicKey = ImixsKeyGenerator.getPemPublicKey(PUBLIC_KEY_FILE,
				ALGORITHM);
		
		Assert.assertNotNull(publicKey);

		byte[] cipherText = null;

		String originalText = "Hello World";

		cipherText = encrypt(originalText, publicKey);

		logger.info(cipherText.toString());

		String plainText = decrypt(cipherText, privateKey);

		Assert.assertEquals(originalText, plainText);

		Assert.assertNotSame(plainText + "x", originalText);

	}
	
	
	
	
	
	
	/**
	 * Encrypt the plain text using public key.
	 * 
	 * @param text
	 *            : original plain text
	 * @param key
	 *            :The public key
	 * @return Encrypted text
	 * @throws java.lang.Exception
	 */
	private static byte[] encrypt(String text, PublicKey key) {
		byte[] cipherText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance(ALGORITHM);
			// encrypt the plain text using the public key
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(text.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cipherText;
	}

	/**
	 * Decrypt text using private key.
	 * 
	 * @param text
	 *            :encrypted text
	 * @param key
	 *            :The private key
	 * @return plain text
	 * @throws java.lang.Exception
	 */
	private static String decrypt(byte[] text, PrivateKey key) {
		byte[] dectyptedText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance(ALGORITHM);

			// decrypt the text using the private key
			cipher.init(Cipher.DECRYPT_MODE, key);
			dectyptedText = cipher.doFinal(text);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new String(dectyptedText);
	}


}
