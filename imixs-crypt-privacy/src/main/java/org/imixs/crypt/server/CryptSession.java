package org.imixs.crypt.server;

import java.security.PublicKey;
import java.util.logging.Logger;

import org.imixs.crypt.ImixsRSAKeyUtil;

public class CryptSession {

	protected String password;

	private static CryptSession instance = null;

	private final static Logger logger = Logger.getLogger(CryptSession.class
			.getName());

	protected CryptSession() {
		// Exists only to defeat instantiation.
	}

	public static CryptSession getInstance() {
		if (instance == null) {
			instance = new CryptSession();
		}
		return instance;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * generates a new key pair with the current password
	 * 
	 * @throws Exception
	 */
	public void generateKeyPair() throws Exception {
		logger.info("[CryptSession] generate new keypair....");
		ImixsRSAKeyUtil.generateKeyPair("src/test/resources/id",
				"src/test/resources/id.pub", password);
	}

	/**
	 * returns the public key
	 * 
	 * @throws Exception
	 */
	public PublicKey getPublicKey() throws Exception {

		return ImixsRSAKeyUtil
				.getPemPublicKey("src/test/resources/id.pub");

	}

}
