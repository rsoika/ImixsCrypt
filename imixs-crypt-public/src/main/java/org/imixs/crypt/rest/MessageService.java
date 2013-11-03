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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.crypt.xml.MessageItem;

/**
 * The MessageService of a public node can be used to send an encrypted message
 * to a receifer or to get encrypted messages.
 * 
 * To send a message the public key digest need to be send as a param togehter
 * with the encrypted message
 * 
 * 
 * To get messages the local public key must be send. The message service
 * compares the key with the key storage and returns open messages.
 * 
 * To encrypt messages the MessageService provides methods to get and to post
 * public keys.
 * 
 * 
 * 
 * All messages are stored in a local hashmap
 * 
 * 
 * @author rsoika
 * 
 */
@Path("/messages")
public class MessageService {

	private String ENCODING = "UTF-8";
	private String DATA_DIRECTORY = "ImixsCrypt/";

	private final static Logger logger = Logger.getLogger(MessageService.class
			.getName());

	/**
	 * This method posts a public key and stores the key into the data directory
	 * /keys/ The name of the key is the identity provided in the request uri
	 * 
	 * @param keyItem
	 * 
	 */
	@POST
	@Path("/identity/{id}")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response postPublicKey(String keydata, @PathParam("id") String id) {

		if (keydata == null || keydata.isEmpty() || id == null || id.isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(keydata).build();
		}

		try {
			byte[] data = keydata.getBytes(ENCODING);

			String keyFileName = DATA_DIRECTORY + "keys/" + id;
			File keyFile = new File(keyFileName);
			// Create files to store public and private key
			if (keyFile.getParentFile() != null) {
				keyFile.getParentFile().mkdirs();
			}
			// save data into file
			Files.write(Paths.get(keyFileName), data);
			logger.info("[IdentityService] stored Public key:" + id);
		} catch (Exception e) {

			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).build();

		}

		// success HTTP 200
		return Response.ok(MediaType.APPLICATION_JSON).build();

	}

	/**
	 * This method reads a public key from the data directory /keys/
	 * 
	 * @param keyItem
	 * 
	 */
	@GET
	@Path("/identity/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getPublicKey(@PathParam("id") String id) {

		if (id == null || id.isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).build();
		}

		try {
			// save data into file
			byte[] keyData = Files.readAllBytes(Paths.get(DATA_DIRECTORY
					+ "keys/" + id));

			return Response.status(Response.Status.OK)
					.entity(new String(keyData))
					.type(MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		}

	}

	/**
	 * This method puts an encrypted message into the message store.
	 * 
	 * @param message
	 * 
	 */
	@POST
	@Path("/messages/{id}")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response postMessage(String encryptedMessage,
			@PathParam("id") String id) {

		// validate message and id
		if (encryptedMessage == null || encryptedMessage.isEmpty()
				|| id == null || id.isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.TEXT_PLAIN).build();
		}

		try {
			MessageItem message = new MessageItem();
			message.setMessage(encryptedMessage);
			message.setUser(id);
			MessageRepository.getInstance().putMessage(message);
			logger.info("decrypted=" + encryptedMessage);
		} catch (Exception e) {

			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.TEXT_PLAIN).build();

		}

		// success HTTP 200
		return Response.ok(MediaType.TEXT_PLAIN).build();

	}

	/**
	 * This method returns all messages for the given id
	 * 
	 * @Todo - did we need to verify the request ??
	 * 
	 * @param keyItem
	 * 
	 */
	@GET
	@Path("/messages/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public MessageItem[] getMessageList(@PathParam("id") String name) {

		// validate key - check if user is available
		if (name == null) {
			return null;
		}

		// List<MessageItem>
		// result=MessageRepository.getInstance().getMessage(name);

		// simulate
		List<MessageItem> result = new ArrayList<MessageItem>();
		MessageItem m = new MessageItem();
		m.setMessage("hallo");
		m.setUser("sepp");
		result.add(m);
		m = new MessageItem();
		m.setMessage("hallo Welt");
		m.setUser("Anna");
		result.add(m);

		MessageItem[] messageArray = result.toArray(new MessageItem[result
				.size()]);
		// success HTTP 200
		return messageArray;

	}
}