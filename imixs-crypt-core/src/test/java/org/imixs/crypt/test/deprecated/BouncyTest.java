package org.imixs.crypt.test.deprecated;

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

import org.imixs.crypt.deprecated.BouncyGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BouncyTest {

	static final String ALGORITHM = "RSA";
	static final String PRIVATE_KEY_FILE = "src/test/resources/bouncyprivate.key";
	static final String PUBLIC_KEY_FILE = "src/test/resources/bouncypublic.key";

	PublicKey publicKey = null;
	PrivateKey privateKey = null;

	private final static Logger logger = Logger.getLogger(BouncyTest.class
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
	

	}

	/**
	 * 
	 */
	@Test
	public void testGeneration() {
		BouncyGenerator.generate(PUBLIC_KEY_FILE, PRIVATE_KEY_FILE);
	}

}
