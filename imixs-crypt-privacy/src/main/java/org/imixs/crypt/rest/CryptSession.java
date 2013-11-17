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
import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import org.imixs.crypt.ImixsCryptException;
import org.imixs.crypt.ImixsCryptKeyUtil;
import org.imixs.crypt.util.Base64Coder;
import org.imixs.crypt.xml.MessageItem;

/**
 * The CryptSession is used by the REST API to manage keys and encrypt/decrypt
 * messages. The class provides methods to generate a new keypair and store/read
 * public keys.
 * 
 * The CryptSession makes use of the property file 'imixs.properties' containing
 * path settings for the local key store.
 * 
 * The CryptSession instantiates a KeyUtil Implementation for the key management
 * and encryption. The KeyUtil can be configured by the imixs.properties file.
 * 
 * The CryptSession is a singelton.
 * 
 * @author rsoika
 * @version 1.0.0
 */
class CryptSession {

	private String ENCODING = "UTF-8";
	public static String IMIXS_PROPERTY_FILE = "imixs.properties";

	public static String PROPERTY_KEY_UTIL = "keyutil";
	public static String PROPERTY_DEFAULT_IDENTITY = "default.identity";

	private static String rootPath = null;
	private String password = null;
	private String sessionId = null;
	private String identity = null;

	private static String DEFAULT_ROOT_PATH = "src/test/resources/";
	private static String DEFAULT_KEY_UTIL = "org.imixs.crypt.ImixsRSAKeyUtil";
	private static Properties properties;

	private static CryptSession instance = null;
	private static ImixsCryptKeyUtil keyUtil = null;

	private final static Logger logger = Logger.getLogger(CryptSession.class
			.getName());

	/**
	 * Default Constructor
	 */
	protected CryptSession() {
		super();
	}

	protected static CryptSession getInstance() {
		if (instance == null) {
			instance = new CryptSession();
			init();
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	protected static void init() {
		// read properties
		try {
			properties = new Properties();
			logger.fine("[CryptSession] read imixs.properties from:"
					+ getRootPath() + IMIXS_PROPERTY_FILE);

			properties.load(new FileInputStream(getRootPath()
					+ IMIXS_PROPERTY_FILE));
		} catch (Exception e) {
			logger.warning("[CryptSession] No imixs.properties file found!");
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
		new File(getRootPath() + "keys").mkdirs();
		new File(getRootPath() + "keys/trusted").mkdirs();
		new File(getRootPath() + "keys/public").mkdirs();
		new File(getRootPath() + "data/local").mkdirs();
		
		
		

	}

	/**
	 * Returns the root path of the file based key and data storage.
	 * 
	 * @return
	 */
	protected static String getRootPath() {
		if (rootPath == null)
			rootPath = DEFAULT_ROOT_PATH;
		return rootPath;
	}

	/**
	 * Set the root path of the file based key and data storage.
	 * 
	 * @param rootPath
	 */
	protected static void setRootPath(String arootPath) {
		rootPath = arootPath;
	}

	/**
	 * generates a new key pair for the current identity with the current
	 * password.
	 * 
	 * @throws Exception
	 */
	protected PublicKey generateKeyPair() throws Exception {
		if (getSessionId() == null) {
			logger.warning("[CryptSession] invalid sessionId!");
			return null;
		}

		logger.info("[CryptSession] generate new keypair....");

		PublicKey publicKey = keyUtil.generateKeyPair(getRootPath() + "keys/"
				+ getIdentity(), getRootPath() + "keys/" + getIdentity()
				+ ".pub", password);

		// update default identity
		properties.setProperty(PROPERTY_DEFAULT_IDENTITY, getIdentity());
		saveProperties();

		return publicKey;
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
	 * returns a local public key for a spcified idenity. The local public keys
	 * are stored under
	 * 
	 * /keys/
	 * 
	 * If the param id is empty then the method lookups the default identity in
	 * the properties file.
	 * 
	 * For this method call a sessionId is not necessary. 
	 * 
	 * 
	 * The method returns null if no key exists.
	 * 
	 */
	protected PublicKey getLocalPublicKey(String id) {
		try {
			PublicKey publicKey = null;

			if (id == null || id.isEmpty()) {
				// get current identity if set
				id = getIdentity();
			}
			if (id == null) {
				// get default identity from poroperties file
				id = properties.getProperty(PROPERTY_DEFAULT_IDENTITY);
				setIdentity(id);
			}
			// try loading from key path
			publicKey = keyUtil.getPublicKey(getRootPath() + "keys/" + id
					+ ".pub");

			return publicKey;
		} catch (ImixsCryptException e) {
			logger.warning("[CryptSession] local public key not found");
			return null;
		}

	}

	/**
	 * This method encrypt a MessageItem and returns a new MessageItem with the
	 * ecrypted and signed data.
	 * 
	 * @param message
	 * @param aUserID
	 * @param asessionId
	 * @return
	 * @throws ImixsCryptException
	 */
	protected MessageItem ecrypt(MessageItem message, String asessionId)
			throws ImixsCryptException {
		byte[] encrypted = null;

		// validate session
		if (!isValidSession(asessionId)) {
			logger.warning("[CryptSession] invalid sessionId!");
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY,
					"Invalid SessionID: " + asessionId);

		}

		MessageItem encryptedMessage = new MessageItem();
		encryptedMessage.setRecipient(message.getRecipient());
		encryptedMessage.setSender(message.getSender());

		// get public key of recipient
		PublicKey publicKey = this.getLocalPublicKey(message.getRecipient());
		try {

			// encrypt comment
			if (message.getComment() != null && !message.getComment().isEmpty()) {
				encrypted = keyUtil.encrypt(
						message.getComment().getBytes(ENCODING), publicKey);
				encryptedMessage.setComment(new String(Base64Coder
						.encode(encrypted)));
			}

			// encrypt message
			if (message.getMessage() != null && !message.getMessage().isEmpty()) {
				encrypted = keyUtil.encrypt(
						message.getMessage().getBytes(ENCODING), publicKey);
				encryptedMessage.setMessage(new String(Base64Coder
						.encode(encrypted)));
				logger.fine("[CryptSession] Encrypted Message Text="
						+ encryptedMessage.getMessage());
			}

		} catch (UnsupportedEncodingException e) {
			throw new ImixsCryptException(
					ImixsCryptException.UNSUPPORTED_ENCODING, e);
		}

		// sign message

		encryptedMessage = sign(encryptedMessage);

		return encryptedMessage;
	}

	/**
	 * This method decrypt a MessageItem with the local private key and returns
	 * a new MessageItem with the decrypted and verified data.
	 * 
	 * @param message
	 * @param aUserID
	 * @param asessionId
	 * @return
	 * @throws ImixsCryptException
	 */
	protected MessageItem decrypt(MessageItem message, String asessionId)
			throws ImixsCryptException {
		byte[] decrypted = null;

		// validate session
		if (!isValidSession(asessionId)) {
			logger.warning("[CryptSession] invalid sessionId!");
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY,
					"Invalid SessionID: " + asessionId);

		}

		PrivateKey privateKey = keyUtil.getPrivateKey(getRootPath() + "keys/"
				+ getIdentity(), password);

		MessageItem decryptedMessage = new MessageItem();
		decryptedMessage.setRecipient(message.getRecipient());
		decryptedMessage.setSender(message.getSender());
		decryptedMessage.setDigest(message.getDigest());
		decryptedMessage.setCreated(message.getCreated());

		// encrypt comment
		if (message.getComment() != null && !message.getComment().isEmpty()) {
			decrypted = keyUtil.decrypt(
					Base64Coder.decode(message.getComment()), privateKey);
			decryptedMessage.setComment(new String(decrypted));
		}

		// encrypt message
		if (message.getMessage() != null && !message.getMessage().isEmpty()) {

			logger.fine("[CryptSession] Decrypt encrypted Message Text="
					+ message.getMessage());
			decrypted = keyUtil.decrypt(
					Base64Coder.decode(message.getMessage()), privateKey);
			decryptedMessage.setMessage(new String(decrypted));
		}
		// sign message

		decryptedMessage = verify(decryptedMessage);

		return decryptedMessage;
	}

	/*
	 * todo - implenetation!
	 */
	private MessageItem verify(MessageItem message) {
		logger.warning("[CryptSession] verify message not implemented!");
		return message;
	}

	/**
	 * This method signd a messate witht a private key and generates a message
	 * digest
	 * 
	 * @param message
	 * @return
	 * @throws ImixsCryptException
	 */
	private MessageItem sign(MessageItem message) throws ImixsCryptException {
		PrivateKey privateKey = keyUtil.getPrivateKey(getRootPath() + "keys/"
				+ getIdentity(), password);
		logger.warning("[CryptSession] sign message not implemented!");

		// set created time
		message.setCreated(new Date().getTime());
		
		// dummy method....
		// @TODO- need implementation
		// @ToDo need to be computed with real digest
		String s = "";
		if (message.getComment() != null)
			s = s + message.getComment();
		if (message.getMessage() != null)
			s = s + message.getMessage();

		if (message.getRecipient() != null)
			s = s + message.getRecipient();
		if (message.getSender() != null)
			s = s + message.getSender();

		if (message.getCreated() > 0)
			s = s + message.getCreated();

		message.setDigest("" + s.hashCode());
		message.setSignature("Sig:" + s.hashCode());
		return message;
	}

	/**
	 * creates a default property file
	 */
	private static void createDefaultProperties() {

		// init properties
		properties = new Properties();
		// set the properties value

		properties.setProperty(PROPERTY_KEY_UTIL, DEFAULT_KEY_UTIL);
		// save properties to project root folder
		saveProperties();

	}

	/**
	 * Set a property value and saves the property file
	 * 
	 * @param property
	 * @param value
	 * @throws ImixsCryptException
	 */
	protected void setProperty(String property, String value, String asessionId)
			throws ImixsCryptException {

		if (!isValidSession(asessionId)) {
			logger.warning("[CryptSession] invalid sessionId!");
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY,
					"Invalid SessionID: " + asessionId);

		}
		properties.setProperty(property, value);
		saveProperties();
	}

	/**
	 * Returns a property value from the property file
	 * 
	 * @param property
	 * @return
	 * @throws ImixsCryptException
	 */
	protected String getProperty(String property, String asessionId)
			throws ImixsCryptException {

		if (!isValidSession(asessionId)) {
			logger.warning("[CryptSession] invalid sessionId!");
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY,
					"Invalid SessionID: " + asessionId);

		}
		return properties.getProperty(property);
	}

	private static void saveProperties() {
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

	/**
	 * Writes a plublic key into the key store
	 * 
	 * @param property
	 * @param asessionId
	 * @return
	 * @throws ImixsCryptException
	 */
	protected void savePublicKey(byte[] keyBytes, String keyFileName,
			String asessionId) throws ImixsCryptException {

		if (!isValidSession(asessionId)) {
			logger.warning("[CryptSession] invalid sessionId!");
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY,
					"Invalid SessionID: " + asessionId);

		}
		keyUtil.writeKeyToFile(keyBytes, keyFileName, asessionId);
	}
}
