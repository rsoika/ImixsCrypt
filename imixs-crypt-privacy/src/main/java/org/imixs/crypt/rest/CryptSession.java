package org.imixs.crypt.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;
import java.util.logging.Logger;

import org.imixs.crypt.ImixsCryptException;
import org.imixs.crypt.ImixsCryptKeyUtil;

/**
 * The CryptSession is used by the SessionService as a singelton instance. The
 * class provides methods to read and generate keypairs.
 * 
 * The CryptSession makes use of the property file 'imixs.properties' containing
 * path settings for the local key store.
 * 
 * The CryptSession instantiates keyUtil Implementation for key the management
 * and encryption. The KeyUtil can be configured by the imixs.properties file.
 * 
 * @author rsoika
 * @version 1.0.0
 */
class CryptSession {

	public static String IMIXS_PROPERTY_FILE = "imixs.properties";

	public static String PROPERTY_KEY_UTIL = "keyutil";

	private String rootPath = null;
	private String password = null;

	private static String DEFAULT_ROOT_PATH = "src/test/resources/";
	private static String DEFAULT_KEY_UTIL = "org.imixs.crypt.ImixsRSAKeyUtil";
	private Properties properties;

	private static CryptSession instance = null;
	private ImixsCryptKeyUtil keyUtil = null;

	private final static Logger logger = Logger.getLogger(CryptSession.class
			.getName());

	/**
	 * Default Constructor
	 */
	protected CryptSession() {
		init();
	}

	/**
	 * Constructor with a default rootPath setting
	 * 
	 * @param rootPath
	 *            for data storage
	 */
	protected CryptSession(String rootPath) {
		this();
		setRootPath(rootPath);
	}

	@SuppressWarnings("unchecked")
	protected void init() {
		// read properties
		try {
			properties.load(new FileInputStream(getRootPath()
					+ IMIXS_PROPERTY_FILE));
			if (properties == null) {
				logger.warning("[CryptSession] No imixs.properties file found!");
				createDefaultProperties();
			}

		} catch (Exception e) {
			createDefaultProperties();
		}

		// create default KeyUtil Class
		String keyUtilClassName = properties.getProperty(PROPERTY_KEY_UTIL);
		try {
			Class<ImixsCryptKeyUtil> keyUtilClass;
			keyUtilClass = (Class<ImixsCryptKeyUtil>) Class
					.forName(keyUtilClassName);
			keyUtil = (ImixsCryptKeyUtil) keyUtilClass.newInstance();
		} catch (ClassNotFoundException e) {
			logger.severe("[CryptSession] can not create ImixsCryptKeyUtil!");
			e.printStackTrace();
		} catch (InstantiationException e) {
			logger.severe("[CryptSession] can not create ImixsCryptKeyUtil!");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			logger.severe("[CryptSession] can not create ImixsCryptKeyUtil!");
			e.printStackTrace();
		}
		
		// create key and data directories
		new File(this.getRootPath() + "keys").mkdirs();
		new File(this.getRootPath() + "data/notes").mkdirs();
		
	}

	protected static CryptSession getInstance() {
		if (instance == null) {
			instance = new CryptSession();
		}
		return instance;
	}

	/**
	 * Returns the root path of the file based key and data storage.
	 * 
	 * @return
	 */
	protected String getRootPath() {
		if (rootPath == null)
			rootPath = DEFAULT_ROOT_PATH;
		return rootPath;
	}

	/**
	 * Set the root path of the file based key and data storage.
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
		keyUtil.generateKeyPair(getRootPath() + "keys/id", getRootPath()
				+ "keys/id.pub", password);
	}

	/**
	 * returns the public key for a given UserID. The method first tries to read
	 * the file form the trusted pub directory (/keys/trusted) If not found the
	 * method reads the key form the public key directory /keys/public
	 * 
	 * the method returns null if no public key exists.
	 */
	protected PublicKey getPublicKey(String userid) {
		PublicKey publicKey = null;
		try {
			// try loading from trusted key path
			publicKey = keyUtil.getPublicKey(getRootPath() + "keys/trusted/"
					+ userid + ".pub");
			logger.info("[CryptSession] trusted public key found for '"
					+ userid + "'");
		} catch (ImixsCryptException e) {
			// try loading public key
			try {
				publicKey = keyUtil.getPublicKey(getRootPath() + "keys/public/"
						+ userid + ".pub");

			} catch (ImixsCryptException e2) {
				logger.info("[CryptSession] no public key found for '" + userid
						+ "' :" + e2.getMessage());
			}

		}
		return publicKey;

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
			publicKey = keyUtil.getPublicKey(getRootPath() + "keys/id.pub");

			return publicKey;
		} catch (ImixsCryptException e) {
			logger.warning("[CryptSession] local public key not found");
			return null;
		}

	}

	/**
	 * Encrypts a message String with a public key. The encrypted byte array
	 * will be returned Base64 encoded.
	 * 
	 * 
	 * @param message
	 * @return
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 */
	protected byte[] ecrypt(byte[] data, String aUserID) {
		try {
			PublicKey publicKey = this.getPublicKey(aUserID);
			return keyUtil.encrypt(data, publicKey);
		} catch (ImixsCryptException e) {
			logger.warning("[CryptSession] " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Encrypts a message String with the local public key. The encrypted byte
	 * array will be returned Base64 encoded.
	 * 
	 * 
	 * @param message
	 * @return
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 */
	protected byte[] ecryptLocal(byte[] data) {
		try {
			// get the local public key
			PublicKey publicKey = this.getLocalPublicKey();
			return keyUtil.encrypt(data, publicKey);
		} catch (ImixsCryptException e) {
			logger.warning("[CryptSession] " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Decrypts a message with the local private key. The encrypted message is
	 * 
	 * @param message
	 * @return
	 */
	protected byte[] decryptLocal(byte[] encryptedData) {

		PrivateKey privateKey;
		try {
			privateKey = keyUtil.getPrivateKey(getRootPath() + "keys/id",
					password);

			return keyUtil.decrypt(encryptedData, privateKey);

		} catch (ImixsCryptException e) {
			logger.warning("[CryptSession] " + e.getMessage());
			e.printStackTrace();
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

		properties.setProperty(PROPERTY_KEY_UTIL, DEFAULT_KEY_UTIL);
		// save properties to project root folder
		try {
			properties.store(new FileOutputStream(getRootPath()
					+ IMIXS_PROPERTY_FILE), null);
			logger.info("[CryptSession] imixs.properties created successfull");
		} catch (Exception e1) {
			logger.severe("[CryptSession] unable to generate imixs.properties! Please check file access!");
			e1.printStackTrace();
		}

	}
}
