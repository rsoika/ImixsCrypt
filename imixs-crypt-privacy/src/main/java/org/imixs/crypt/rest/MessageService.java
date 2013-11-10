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
import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.crypt.ImixsCryptException;
import org.imixs.crypt.json.JSONWriter;
import org.imixs.crypt.util.RestClient;
import org.imixs.crypt.xml.MessageItem;

/**
 * The MessageService can be used to encrypt and decrypt messages provided in a
 * MessageItem. A Message will be encrypted with the public key of the receiver
 * and decrypted with the local private key.
 * 
 * The values of the message body and message comment are expected in Base64
 * encoded string format. Encrypted Messages will be returned also Base64
 * encoded
 * 
 * @author rsoika
 * 
 */
@Path("/rest/messages")
public class MessageService {

	private final static Logger logger = Logger.getLogger(MessageService.class
			.getName());

	/**
	 * This method posts a messageItem and encrypt the message with the public
	 * key of the recipient.
	 * 
	 * If not recipient is defined the message will be encrypted with the local
	 * public key and stored into the filesystem /data/messages
	 * 
	 * @param MessageItem
	 * 
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postMessage(
			MessageItem message,
			@CookieParam(value = IdentityService.SESSION_COOKIE) String sessionId) {

		// validate key - check if user is available
		if (message == null) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();
		}
		try {
			// encrypt message
			MessageItem encryptedMessage = CryptSession.getInstance().ecrypt(
					message, sessionId);

			// if no recipient is define store the message locally
			if (encryptedMessage.getRecipient() == null
					|| encryptedMessage.getRecipient().isEmpty()) {

				// save data into file
				JSONWriter.writeFile(encryptedMessage, CryptSession
						.getInstance().getRootPath()
						+ "data/notes/"
						+ encryptedMessage.getDigest());

				return Response.status(Response.Status.OK)
						.type(MediaType.APPLICATION_JSON)
						.entity(encryptedMessage).build();
			}

			// No local message so we need to send it over the internet.....

			// test if we have a public key...
			PublicKey publicKey = CryptSession.getInstance().getPublicKey(
					encryptedMessage.getRecipient(), sessionId);

			if (publicKey == null) {
				// fetch key from public node....
				fetchPublicKeyFromPublicServer(encryptedMessage.getRecipient(),
						sessionId);
			}

			logger.info("[MessageService] encrypting message for '"
					+ encryptedMessage.getRecipient() + "'");

			// sendMessageToPublicServer(text, receipient, sessionId);

			logger.info("encrypted=" + encryptedMessage.getMessage());
		} catch (ImixsCryptException e) {

			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();

		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();
		}

		// success HTTP 200
		return Response.status(Response.Status.OK)
				.type(MediaType.APPLICATION_JSON).entity(message).build();

	}

	/**
	 * This method returns a list with all available local message headers
	 * 
	 * The 'message' property will be removed from the returned messageItem. The
	 * 'comment' property will be decrypted.
	 * 
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<MessageItem> getLocalMessages(
			@CookieParam(value = IdentityService.SESSION_COOKIE) String sessionId) {

		if (!CryptSession.getInstance().isValidSession(sessionId))
			return null;

		File folder = new File(CryptSession.getRootPath() + "data/notes");
		File[] listOfFiles = folder.listFiles();

		// read local files
		List<MessageItem> result = new ArrayList<>();
		for (File file : listOfFiles) {
			Long lastModified = file.lastModified();
			String name = file.getName();

			// create encrypted MessageItem
			MessageItem encryptedMessage = JSONWriter.readMessageFromFile(file
					.getPath());
			// decrypt message
			MessageItem decryptedMessage = null;
			try {
				decryptedMessage = CryptSession.getInstance().decrypt(
						encryptedMessage, sessionId);
				// remove message body
				decryptedMessage.setMessage(null);
				// set digest
				decryptedMessage.setDigest(name);
			} catch (ImixsCryptException e) {
				e.printStackTrace();
				// add null message
				result.add(encryptedMessage);
			}

			if (decryptedMessage != null) {
				result.add(decryptedMessage);
			}
		}

		return result;
	}

	/**
	 * This method gets a message by its message digest
	 * 
	 * @param MessageItem
	 * 
	 */
	@GET
	@Path("/{digest}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMessageByDigest(@PathParam("digest") String digest,
			@CookieParam(value = "ImixsCryptSessionID") String sessionId) {

		// validate key
		if (digest == null || digest.isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).build();
		}

		MessageItem encryptedMessage = JSONWriter
				.readMessageFromFile(CryptSession.getInstance().getRootPath()
						+ "data/notes/" + digest);

		// decrypt message
		MessageItem decryptedMessage;
		try {
			decryptedMessage = CryptSession.getInstance().decrypt(
					encryptedMessage, sessionId);
		} catch (ImixsCryptException e) {
			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).build();
		}

		// success HTTP 200
		return Response.ok(decryptedMessage, MediaType.APPLICATION_JSON)
				.build();

	}

	private PublicKey fetchPublicKeyFromPublicServer(String user,
			String asessionId) throws ImixsCryptException {

		RestClient restClient = new RestClient();
		// get Host
		String host = CryptSession.getInstance().getProperty(
				IdentityService.DEFAULT_PUBLIC_NODE, asessionId);

		// test default identity
		String uri = host + "/rest/identities/" + user;

		restClient.setMediaType(MediaType.APPLICATION_JSON);
		try {
			int httpResult = restClient.get(uri);
		} catch (Exception e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		}

		String sContent = restClient.getContent();

		String path = CryptSession.getInstance().getRootPath()
				+ "/keys/public/";

		CryptSession.getInstance().savePublicKey(sContent.getBytes(),
				path + user, asessionId);

		return CryptSession.getInstance().getPublicKey(user, asessionId);

	}

	/**
	 * Sends a encrypted message to the default public node
	 * 
	 * @param user
	 * @param asessionId
	 * @throws ImixsCryptException
	 */
	private void sendMessageToPublicServer(String message, String user,
			String asessionId) throws ImixsCryptException {

		RestClient restClient = new RestClient();
		// get Host
		String host = CryptSession.getInstance().getProperty(
				IdentityService.DEFAULT_PUBLIC_NODE, asessionId);

		// test default identity
		String uri = host + "/rest/session/" + user;

		String json = "{\"user\":\"" + user + "\", \"message\":\"" + message
				+ "\"}";
		restClient.setMediaType(MediaType.APPLICATION_JSON);
		try {
			int httpResult = restClient.post(uri, json);
		} catch (Exception e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		}

	}
}