package org.imixs.crypt.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RSATest {

	static final String ALGORITHM = "RSA";
	static final String PRIVATE_KEY_FILE = "src/test/resources/private.key";
	static final String PUBLIC_KEY_FILE = "src/test/resources/public.key";

	PublicKey publicKey = null;
	PrivateKey privateKey = null;

	private final static Logger logger = Logger.getLogger(RSATest.class
			.getName());

	/**
	 * Generate key which contains a pair of private and public key using 1024
	 * bytes. Store the set of keys in Prvate.key and Public.key files.
	 * 
	 */
	@Before
	public void setup() {
		try {
			final KeyPairGenerator keyGen = KeyPairGenerator
					.getInstance(ALGORITHM);
			keyGen.initialize(1024);
			final KeyPair key = keyGen.generateKeyPair();

			File privateKeyFile = new File(PRIVATE_KEY_FILE);
			File publicKeyFile = new File(PUBLIC_KEY_FILE);

			// Create files to store public and private key
			if (privateKeyFile.getParentFile() != null) {
				privateKeyFile.getParentFile().mkdirs();
			}
			privateKeyFile.createNewFile();

			if (publicKeyFile.getParentFile() != null) {
				publicKeyFile.getParentFile().mkdirs();
			}
			publicKeyFile.createNewFile();

			// Saving the Public key in a file
			ObjectOutputStream publicKeyOS = new ObjectOutputStream(
					new FileOutputStream(publicKeyFile));
			publicKeyOS.writeObject(key.getPublic());
			publicKeyOS.close();

			// Saving the Private key in a file
			ObjectOutputStream privateKeyOS = new ObjectOutputStream(
					new FileOutputStream(privateKeyFile));
			privateKeyOS.writeObject(key.getPrivate());
			privateKeyOS.close();

			// now generyte keys

			// Encrypt the string using the public key
			InputStream inputStream = new ObjectInputStream(
					new FileInputStream(PUBLIC_KEY_FILE));
			publicKey = (PublicKey) ((ObjectInputStream) inputStream)
					.readObject();

			// Decrypt the cipher text using the private key.
			inputStream = new ObjectInputStream(new FileInputStream(
					PRIVATE_KEY_FILE));
			privateKey = (PrivateKey) ((ObjectInputStream) inputStream)
					.readObject();

		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/**
	 * finally remove key files
	 */
	@After
	public void teardown() {

		File privateKeyFile = new File(PRIVATE_KEY_FILE);
		File publicKeyFile = new File(PUBLIC_KEY_FILE);

		privateKeyFile.delete();
		publicKeyFile.delete();
	}

	/**
	 * The method checks if the pair of public and private key has been
	 * generated.
	 * 
	 */
	@Test
	public void areKeysPresent() {
		File privateKeyFile = new File(PRIVATE_KEY_FILE);
		File publicKeyFile = new File(PUBLIC_KEY_FILE);

		Assert.assertTrue(privateKeyFile.exists());
		Assert.assertTrue(publicKeyFile.exists());

	}

	/**
	 * Simple crypt/decrypt test
	 */
	@Test
	public void encryptDecryptTest() {
		byte[] cipherText = null;

		String originalText = "Hello World";

		cipherText = encrypt(originalText, publicKey);

		logger.info(cipherText.toString());

		String plainText = decrypt(cipherText, privateKey);

		Assert.assertEquals(originalText, plainText);
		
		Assert.assertNotSame(plainText+"x", originalText);
		
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
	public static byte[] encrypt(String text, PublicKey key) {
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
	public static String decrypt(byte[] text, PrivateKey key) {
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
