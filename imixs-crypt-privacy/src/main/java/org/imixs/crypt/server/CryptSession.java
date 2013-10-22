package org.imixs.crypt.server;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.imixs.crypt.ImixsRSAKeyUtil;

public class CryptSession {

	private String password;
	private String keyPath;

	private static CryptSession instance = null;

	private final static Logger logger = Logger.getLogger(CryptSession.class
			.getName());

	protected CryptSession() {
		// generate defaut key path
		keyPath = "src/test/resources/";
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

	public String getKeyPath() {
		return keyPath;
	}

	public void setKeyPath(String keyPath) {
		this.keyPath = keyPath;
	}

	/**
	 * generates a new key pair with the current password
	 * 
	 * @throws Exception
	 */
	public void generateKeyPair() throws Exception {
		logger.info("[CryptSession] generate new keypair....");
		ImixsRSAKeyUtil.generateKeyPair(keyPath + "id", keyPath + "id.pub",
				password);
	}

	

	/**
	 * returns the public key
	 * 
	 * @throws Exception
	 */
	public PublicKey getPublicKey() {

		try {
			return ImixsRSAKeyUtil.getPemPublicKey(keyPath + "id.pub");
		} catch (NoSuchAlgorithmException e) {
			logger.warning("[CryptSession] " + e.getMessage());
			e.printStackTrace();
			return null;
		} catch (InvalidKeySpecException e) {
			logger.warning("[CryptSession] " + e.getMessage());
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			logger.info("[CryptSession] no public key found!");
			return null;
		}

	}

}
