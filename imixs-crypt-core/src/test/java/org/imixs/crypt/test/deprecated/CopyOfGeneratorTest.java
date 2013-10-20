package org.imixs.crypt.test.deprecated;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import junit.framework.Assert;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.imixs.crypt.deprecated.ImixsSecretKeyGenerator;
import org.imixs.crypt.deprecated.OldImixsKeyGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class test the imixs.crypt KeyGenerator
 * 
 * @author rsoika
 * 
 */
public class CopyOfGeneratorTest {

	static final String ALGORITHM = "RSA";
	static final String PRIVATE_KEY_FILE = "src/test/resources/private.key";
	static final String PUBLIC_KEY_FILE = "src/test/resources/public.key";

	PublicKey publicKey = null;
	PrivateKey privateKey = null;

	private final static Logger logger = Logger.getLogger(CopyOfGeneratorTest.class
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
	
	
	

	/**
	 * Simple crypt/decrypt test
	 * 
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void encryptDecryptTest() throws NoSuchAlgorithmException,
			IOException, ClassNotFoundException {

		OldImixsKeyGenerator.generateKeyPair(PRIVATE_KEY_FILE, PUBLIC_KEY_FILE,
				ALGORITHM);

		publicKey = OldImixsKeyGenerator.getPublicKey(PUBLIC_KEY_FILE);
		privateKey = OldImixsKeyGenerator.getPrivatKey(PRIVATE_KEY_FILE);

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
