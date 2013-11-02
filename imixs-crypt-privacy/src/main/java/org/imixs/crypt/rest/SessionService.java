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

import java.security.PublicKey;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.imixs.crypt.Base64Coder;
import org.imixs.crypt.ImixsCryptException;
import org.imixs.crypt.xml.KeyItem;

/**
 * Provides a service to open and close a crypt session. Setting the private key
 * password creates a new sessionId. The sessionId is stored in the cookie
 * ImixsCryptSessionID.
 * 
 * @author rsoika
 * 
 */
@Path("/rest")
public class SessionService {

	public final static String SESSION_COOKIE = "ImixsCryptSessionID";

	private final static Logger logger = Logger.getLogger(SessionService.class
			.getName());

	/**
	 * This method opens a new session. The POST method expects the ID and a
	 * password for the corresponding local private key.
	 * 
	 * The password and ID will be stored in the local instance of the
	 * CryptSession. A sessionId will be returned to be used for further method
	 * calls.
	 * 
	 * The Method verifies if a local key pair for the given Id exists. If not
	 * the method will generate a new key pair, encrypted with the given
	 * password.
	 * 
	 * Finally the method creates the ImixsCryptSession Cookie with the current
	 * sessionId.
	 * 
	 * @param password
	 *            - password to be set
	 */
	@POST
	@Path("/session/{id}")
	@Consumes("text/plain")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createSession(String password, @PathParam("id") String id) {
		PublicKey publicKey = null;
		// test if id and password a provided
		if (password == null || password.isEmpty()) {
			CryptSession.getInstance().closeSession();
			logger.info("Session closed");
		} else {
			// open new Session

			try {
				CryptSession.getInstance().openSession(id, password);

				if (password != null && !password.isEmpty()) {
					// verify if a key pair exists
					publicKey = CryptSession.getInstance().getLocalPublicKey();
					if (publicKey == null) {
						logger.info("[SessionService] generate new KeyPair...");
						try {
							publicKey = CryptSession.getInstance()
									.generateKeyPair();
						} catch (Exception e) {
							e.printStackTrace();
							return Response
									.status(Response.Status.INTERNAL_SERVER_ERROR)
									.type(MediaType.TEXT_PLAIN)
									.cookie(new NewCookie(SESSION_COOKIE, ""))
									.build();
						}
					} else {
						logger.info("Session opened");
					}
				}
			} catch (ImixsCryptException e1) {
				e1.printStackTrace();
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.type(MediaType.TEXT_PLAIN)
						.cookie(new NewCookie(SESSION_COOKIE, "")).build();
			}

		}

		// update cookie
		String path = "/rest/";
		String domain = "";

		String newSessionId = CryptSession.getInstance().getSessionId();
		NewCookie sessionCookie = new NewCookie(SESSION_COOKIE, newSessionId,
				 path, domain,"", -1, false);

		KeyItem key = new KeyItem();
		key.setUser(id);
		key.setKey(Base64Coder.encodeLines(publicKey.getEncoded()));

		// success HTTP 200
		return Response.status(Response.Status.OK).entity(key)
				.type(MediaType.APPLICATION_JSON).cookie(sessionCookie)
				.entity(key).build();

		// return
		// Response.ok(MediaType.TEXT_PLAIN).cookie(sessionCookie).build();

		// return Response.ok(MediaType.TEXT_PLAIN).build();

	}

	/**
	 * Returns the default local public key. If not yet created the method
	 * returns an empty keyItem. The method can be used to test if the
	 * PrivateCrypt Server is initalized with a valid key pair.
	 * 
	 * @return
	 */
	@GET
	@Path("/session")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPublicKey() {
		PublicKey publicKey = null;
		KeyItem key = new KeyItem();

		try {
			publicKey = CryptSession.getInstance().getLocalPublicKey();
			if (publicKey == null) {
				logger.info("KeyPair not yet created");

				// return Response.status(Response.Status.ACCEPTED).entity(null)
				// .type(MediaType.APPLICATION_JSON).build();
				// Return an emypt key
				return Response.status(Response.Status.OK).entity(key)
						.type(MediaType.APPLICATION_JSON).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(null).type(MediaType.APPLICATION_JSON).build();
		}
		// lower case

		// Return the key
		key.setKey(Base64Coder.encodeLines(publicKey.getEncoded()));
		key.setUser(CryptSession.getInstance().getIdentity());

		return Response.status(Response.Status.OK).entity(key)
				.type(MediaType.APPLICATION_JSON).build();

	}
}