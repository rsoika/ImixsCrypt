package org.imixs.crypt.rest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;
import java.util.logging.Logger;

import org.imixs.crypt.ImixsRSAKeyUtil;

/**
 * The CryptSession is used by the SessionService as a singelton instance. The
 * class provides methods to read and generate keypairs.
 * 
 * The CryptSession makes use of the property file 'imixs.properties' containing
 * path settings for the local key store.
 * 
 * @author rsoika
 * @version 1.0.0
 */
class CryptSession {

	public static String IMIXS_PROPERTY_FILE = "imixs.properties";
	public static String KEY_PATH = "/keys/";

	private String password;
	private String rootPath = "src/test/resources/";
	private Properties properties;

	private static CryptSession instance = null;

	private final static Logger logger = Logger.getLogger(CryptSession.class
			.getName());

	protected CryptSession() {
		// read properties
		try {
			properties.load(new FileInputStream(IMIXS_PROPERTY_FILE));
			if (properties == null) {
				logger.warning("[CryptSession] No imixs.properties file found!");
				createDefaultProperties();
			}
		} catch (Exception e) {
			createDefaultProperties();
		}

	}

	protected static CryptSession getInstance() {
		if (instance == null) {
			instance = new CryptSession();
		}
		return instance;
	}

	/**
	 * Returns the root path of the file based key storage.
	 * 
	 * @return
	 */
	protected String getRootPath() {
		return rootPath;
	}

	/**
	 * Set the root path of the file based key storage.
	 * 
	 * @param rootPath
	 */
	protected void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	/**
	 * returns the current private key password
	 * 
	 * @return
	 */
	protected String getPassword() {
		return password;
	}

	/**
	 * set the current private key password.
	 * 
	 * @param password
	 */
	protected void setPassword(String password) {
		this.password = password;
	}

	/**
	 * generates a new key pair with the current password
	 * 
	 * @throws Exception
	 */
	protected void generateKeyPair() throws Exception {
		logger.info("[CryptSession] generate new keypair....");
		ImixsRSAKeyUtil.generateKeyPair(getRootPath() + KEY_PATH + "id",
				getRootPath() + KEY_PATH + "id.pub", password);
	}

	/**
	 * returns the public key for a given UserID. The method first tries to read
	 * the file form the trusted pub directory (/keys/trusted) If not found the
	 * method reads the key form the public key directory /keys/public
	 * 
	 * the method returns null if no public key exists.
	 */
	protected PublicKey getPublicKey(String userid) {

		try {
			PublicKey publicKey = null;
			try {
				// try loading from trusted key path
				publicKey = ImixsRSAKeyUtil.getPemPublicKey(getRootPath()
						+ KEY_PATH + "trusted/" + userid + ".pub");
				logger.info("[CryptSession] trusted public key found for '" + userid
						+ "'");
			} catch (IOException e) {
				logger.info("[CryptSession] no trusted public key found for '" + userid
						+ "'");
				return null;
			}
			logger.info("[CryptSession] no trusted key found");
			// try loading from public key
			if (publicKey == null) {
				publicKey = ImixsRSAKeyUtil.getPemPublicKey(getRootPath()
						+ KEY_PATH + "public/" + userid + ".pub");
			}
			return publicKey;

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

	/**
	 * returns the local public key for the local cryptServer.. The local public
	 * key is stored under
	 * 
	 * /keys/id.pub
	 * 
	 * The method retuns null if no key exists
	 * 
	 */
	protected PublicKey getLocalPublicKey() {

		try {
			PublicKey publicKey = null;
			// try loading from trusted key path
			publicKey = ImixsRSAKeyUtil.getPemPublicKey(getRootPath()
					+ KEY_PATH + "id.pub");

			return publicKey;
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

	/**
	 * creates a default property file
	 */
	private void createDefaultProperties() {

		// init properties
		properties = new Properties();
		// set the properties value

		properties.setProperty("keypath", rootPath + KEY_PATH);
		// save properties to project root folder
		try {
			properties.store(new FileOutputStream(IMIXS_PROPERTY_FILE), null);
			logger.info("[CryptSession] imixs.properties created successfull");
		} catch (Exception e1) {
			logger.severe("[CryptSession] unable to generate imixs.properties! Please check file access!");
			e1.printStackTrace();
		}

	}
}
