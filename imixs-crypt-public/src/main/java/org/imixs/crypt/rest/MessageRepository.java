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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.imixs.crypt.xml.MessageItem;

/**
 * The MessageStoreag manages messages as a singelton instance. The class
 * provides methods to read and store encrypted messages.
 * 
 * @author rsoika
 * @version 1.0.0
 */
class MessageRepository {

	public static String IMIXS_PROPERTY_FILE = "imixs.properties";
	private static String DEFAULT_ROOT_PATH = "imixs-crypt-public/";

	private static MessageRepository instance = null;
	private String rootPath = null;
	private Map<String, List<MessageItem>> repositroy = null;
	private Properties properties;

	private final static Logger logger = Logger
			.getLogger(MessageRepository.class.getName());

	/**
	 * Default Constructor
	 */
	protected MessageRepository() {
		init();
	}

	/**
	 * Constructor with a default rootPath setting
	 * 
	 * @param rootPath
	 *            for data storage
	 */
	protected MessageRepository(String rootPath) {
		this();
		setRootPath(rootPath);
	}

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

		repositroy = new HashMap<String, List<MessageItem>>();
	}

	/**
	 * Puts a new message into the local repository
	 * 
	 * @param message
	 */
	public void putMessage(MessageItem message) {

		if (message == null || message.getSender() == null
				|| message.getSender().isEmpty())
			return;

		// get Message for user
		List<MessageItem> messageList = repositroy.get(message.getSender());
		if (messageList == null)
			messageList = new ArrayList<MessageItem>();

		messageList.add(message);
		repositroy.put(message.getSender(), messageList);

		logger.fine("[MessageRepository] add message for " + message.getSender());
	}

	/**
	 * Returns all messages for a specific user
	 * 
	 * @param message
	 */
	public List<MessageItem> getMessage(String user) {
		List<MessageItem> messageList = null;
		if (user == null || user.isEmpty())
			return new ArrayList<MessageItem>();

		// get Messagelist for user
		messageList = repositroy.get(user.trim());
		if (messageList == null) {
			// return empty message list
			logger.fine("[MessageRepository] no messages found for " + user);
			messageList = new ArrayList<MessageItem>();
		}

		return messageList;

	}

	protected static MessageRepository getInstance() {
		if (instance == null) {
			instance = new MessageRepository();
		}
		return instance;
	}

	/**
	 * creates a default property file
	 */
	private void createDefaultProperties() {

		// init properties
		properties = new Properties();

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
	 * Set the root path of the file based key and data storage.
	 * 
	 * @param rootPath
	 */
	protected void setRootPath(String rootPath) {
		this.rootPath = rootPath;
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
}
