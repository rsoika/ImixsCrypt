/*******************************************************************************
 *  ImixsCrypt
 *  Copyright (C) 2013 Ralph Soika,  
 *  https://github.com/rsoika/ImixsCrypt
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Project: 
 *  	https://github.com/rsoika/ImixsCrypt
 *  
 *  Contributors:    	
 *  	Ralph Soika - Software Developer
 *******************************************************************************/

package org.imixs.crypt.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;
import java.util.logging.Logger;

import org.imixs.crypt.Base64Coder;
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
	public static String PROPERTY_DEFAULT_IDENTITY = "default.identity";

	private String rootPath = null;
	private String password = null;
	private String sessionId = null;
	private String identity = null;

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
	 * Creates a new session for the given identity. The method stores the
	 * private key password and generates a SessionId. The sessionId is used for
	 * further authentication. If no id is provided the method reads the default
	 * identity from properties file.
	 * 
	 * If the password is null the sessionId will be invalidated.
	 * 
	 * @param password
	 * @throws ImixsCryptException
	 */
	protected void openSession(String id, String password)
			throws ImixsCryptException {

		if (id == null || id.isEmpty()) {
			id = properties.getProperty(PROPERTY_DEFAULT_IDENTITY);
			if (id == null || id.isEmpty())
				throw new ImixsCryptException(ImixsCryptException.INVALID_KEY,
						"Invalid ID ");
		}

		this.password = password;
		if (password == null || password.isEmpty()) {
			// invalidate sessionId
			setSessionId(null);
			setIdentity(null);
			logger.warning("[CryptSession] can not open session - missing password!");
		} else {
			// generate new SessionId
			SecureRandom random = new SecureRandom();
			byte bytes[] = new byte[20];
			random.nextBytes(bytes);
			setSessionId(new String(Base64Coder.encode(bytes)));
			// set identity
			setIdentity(id);
			logger.info("[CryptSession] session opened");
		}
	}

	/**
	 * Closes the current session
	 */
	protected void closeSession() {
		setSessionId(null);
		setIdentity(null);
		logger.info("[CryptSession] session closed");
	}

	protected String getIdentity() {
		return identity;
	}

	/**
	 * returns the current sessionId or null if no session was stared.
	 * 
	 * @return
	 */
	protected String getSessionId() {
		return sessionId;
	}

	private void setIdentity(String identity) {
		this.identity = identity;
	}

	private void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * compares a given sessionId with current sessionId
	 * 
	 * @param aSessionID
	 */
	protected boolean isValidSession(String aSessionID) {
		if (getSessionId() == null || aSessionID == null)
			return false;
		return getSessionId().equals(aSessionID);
	}

	/**
	 * generates a new key pair for the current identity with the current
	 * password.
	 * 
	 * @throws Exception
	 */
	protected void generateKeyPair() throws Exception {
		if (getSessionId() == null) {
			logger.warning("[CryptSession] invalid sessionId!");
			return;
		}

		logger.info("[CryptSession] generate new keypair....");

		keyUtil.generateKeyPair(getRootPath() + "keys/" + getIdentity(),
				getRootPath() + "keys/" + getIdentity() + ".pub", password);

		// update default identity
		properties.setProperty(PROPERTY_DEFAULT_IDENTITY, getIdentity());
		saveProperties();
	}

	/**
	 * returns the public key for a given UserID. The method first tries to read
	 * the file form the trusted pub directory (/keys/trusted) If not found the
	 * method reads the key form the public key directory /keys/public
	 * 
	 * the method returns null if no public key exists.
	 * 
	 * @throws ImixsCryptException
	 */
	protected PublicKey getPublicKey(String userid, String asessionId)
			throws ImixsCryptException {

		if (!isValidSession(asessionId)) {
			logger.warning("[CryptSession] invalid sessionId!");
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY,
					"Invalid SessionID: " + asessionId);

		}

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
	 * returns a local public key for the local cryptServer. The local public
	 * keys are stored under
	 * 
	 * /keys/
	 * 
	 * If the param name is empty then the method lookups the default identity
	 * in the prpeerties file.
	 * 
	 * 
	 * The method retuns null if no key exists
	 * 
	 */
	protected PublicKey getLocalPublicKey() {
		try {
			PublicKey publicKey = null;

			// try loading from key path
			publicKey = keyUtil.getPublicKey(getRootPath() + "keys/"
					+ getIdentity() + ".pub");

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
	 * @throws ImixsCryptException
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 */
	protected byte[] ecrypt(byte[] data, String aUserID, String asessionId)
			throws ImixsCryptException {
		if (!isValidSession(asessionId)) {
			logger.warning("[CryptSession] invalid sessionId!");
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY,
					"Invalid SessionID: " + asessionId);

		}

		try {
			PublicKey publicKey = this.getPublicKey(aUserID, asessionId);
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
	 * @throws ImixsCryptException
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 */
	protected byte[] ecryptLocal(byte[] data, String asessionId)
			throws ImixsCryptException {

		if (!isValidSession(asessionId)) {
			logger.warning("[CryptSession] invalid sessionId!");
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY,
					"Invalid SessionID: " + asessionId);

		}
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
	 * @throws ImixsCryptException
	 */
	protected byte[] decryptLocal(byte[] encryptedData, String asessionId)
			throws ImixsCryptException {

		PrivateKey privateKey;

		if (!isValidSession(asessionId)) {
			logger.warning("[CryptSession] invalid sessionId!");
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY,
					"Invalid SessionID: " + asessionId);
		}
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
		saveProperties();

	}

	private void saveProperties() {
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
