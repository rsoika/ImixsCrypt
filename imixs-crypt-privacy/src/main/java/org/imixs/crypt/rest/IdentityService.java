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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.imixs.crypt.ImixsCryptException;
import org.imixs.crypt.util.Base64Coder;
import org.imixs.crypt.util.RestClient;
import org.imixs.crypt.xml.IdentityItem;

/**
 * Provides a service to open and close a session. Setting the private key
 * password creates a new sessionId. The sessionId is stored in the cookie
 * ImixsCryptSessionID.
 * 
 * Key values are are always Base64 encoded.
 * 
 * @author rsoika
 * 
 */
@Path("/rest")
public class IdentityService {

	public final static String SESSION_COOKIE = "ImixsCryptSessionID";
	public final static String PUBLIC_NODE_ = "public.node.";

	private final static Logger logger = Logger.getLogger(IdentityService.class
			.getName());

	/**
	 * This method opens a new session or sends the local public key to a remote
	 * node.
	 * 
	 * The method returns the identity with the local public key
	 * 
	 * @param password
	 *            - password to be set
	 */
	@POST
	@Path("/identities")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postIdentity(
			IdentityItem identity,
			@QueryParam("node") String node,
			@CookieParam(value = IdentityService.SESSION_COOKIE) String sessionId) {
		if (node == null || node.isEmpty())
			return createSession(identity);
		else
			return sendPublicKey(node, sessionId);
	}

	/**
	 * This method opens a new session. The POST method expects an IdentityItem
	 * with the local ID and the password for the corresponding local private
	 * key.
	 * 
	 * A sessionId will be returned to be used for further method calls. The
	 * identity will be stored in the local instance of the CryptSession.
	 * 
	 * The Method verifies if a local key pair for the given Id exists. If not
	 * the method will generate a new key pair, encrypted with the given
	 * password.
	 * 
	 * Finally the method creates the ImixsCryptSession Cookie with the current
	 * sessionId.
	 * 
	 * The method returns the identity with the local public key
	 * 
	 * @param password
	 *            - password to be set
	 */
	private Response createSession(IdentityItem identity) {
		PublicKey publicKey = null;
		// test if id and password a provided
		if (identity == null || identity.getKey().isEmpty()) {
			CryptSession.getInstance().closeSession();
			logger.info("Session closed");
		} else {
			// open new Session

			try {
				CryptSession.getInstance().openSession(identity.getId(),
						Base64Coder.decodeString(identity.getKey()));

				// verify if a key pair exists
				publicKey = CryptSession.getInstance().getLocalPublicKey(
						identity.getId());
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
				path, domain, "", -1, false);

		// return an identity with the public key
		identity.setKey(new String(Base64Coder.encode(publicKey.getEncoded())));
		identity.setId(CryptSession.getInstance().getIdentity());

		// success HTTP 200
		return Response.status(Response.Status.OK).entity(identity)
				.type(MediaType.APPLICATION_JSON).cookie(sessionCookie).build();

	}

	/**
	 * This method sends the local public key to a remote public node.
	 * 
	 * After a successful call of the remote server, the node will be stored in
	 * the properties.
	 * 
	 * @param node
	 *            - remote public cryptServer
	 * @param sessionId
	 *            - session id
	 */
	private Response sendPublicKey(String node, String sessionId) {
		PublicKey publicKey = null;
		IdentityItem identity = new IdentityItem();

		RestClient restClient = new RestClient();
		restClient.setMediaType(MediaType.APPLICATION_JSON);

		try {
			String id = CryptSession.getInstance().getIdentity();
			publicKey = CryptSession.getInstance().getLocalPublicKey(id);
			identity.setId(id);
			identity.setKey(new String(Base64Coder.encode(publicKey
					.getEncoded())));
			// test default identity
			if (!node.endsWith("/"))
				node = node + "/";
			String uri = node + "identities";
			String json = "{\"id\":\"" + id + "\",\"key\":\""
					+ identity.getKey() + "\"}";

			int httpResult = restClient.post(uri, json);

			if (httpResult < 200 || httpResult >= 300) {
				return Response.status(Response.Status.NOT_ACCEPTABLE)
						.type(MediaType.APPLICATION_JSON).entity(null).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(null).build();

		}

		// store node in properties
		try {
			addPublicNodeToProperties(node, sessionId);
		} catch (ImixsCryptException e) {
			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(null).build();
		}

		// success HTTP 200
		return Response.status(Response.Status.OK).entity(identity).build();

	}

	/**
	 * Returns the local public key for a given identity.
	 * 
	 * @return
	 */
	@GET
	@Path("/identities/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPublicKey(
			@PathParam("id") String id,
			@CookieParam(value = IdentityService.SESSION_COOKIE) String sessionId) {
		PublicKey publicKey = null;
		IdentityItem identity = new IdentityItem();

		try {
			if (id == null || id.isEmpty()) {
				publicKey = CryptSession.getInstance().getLocalPublicKey(id);
				if (publicKey == null) {
					logger.info("KeyPair not yet created");
					// Return an empty key
					identity.setKey(null);
					identity.setId(null);
				} else {
					// update the local identity and return the local public key
					id = CryptSession.getInstance().getIdentity();
					identity.setId(id);
					identity.setKey(new String(Base64Coder.encode(publicKey
							.getEncoded())));

				}
			} else {
				// get the foreign public key
				publicKey = CryptSession.getInstance().getPublicKey(id,
						sessionId);
				if (publicKey != null) {
					identity.setKey(new String(Base64Coder.encode(publicKey
							.getEncoded())));
					identity.setId(id);
				} else {
					// Return an emypt key
					identity.setKey(null);
					identity.setId(null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(null).type(MediaType.APPLICATION_JSON).build();
		}

		// Return the identity
		return Response.status(Response.Status.OK).entity(identity)
				.type(MediaType.APPLICATION_JSON).build();

	}

	/**
	 * Returns the local default public key if a local key pair exits.
	 * 
	 * @return
	 */
	@GET
	@Path("/identities")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDefaultPublicKey(
			@CookieParam(value = IdentityService.SESSION_COOKIE) String sessionId) {
		return getPublicKey(null, sessionId);

	}

	/**
	 * This method returns a list of registred public nodes. These nodes can be
	 * used to exchange messageItems.
	 * 
	 * @param sessionId
	 * @return array of strings
	 */
	@GET
	@Path("/nodes")
	@Produces(MediaType.APPLICATION_JSON)
	public String[] getPublicNodes(
			@CookieParam(value = IdentityService.SESSION_COOKIE) String sessionId) {

		List<String> nodeList = new ArrayList<String>();
		int size = 0;
		try {

			// verify existing nodes
			// Property public.node.0, public.node.1, ...
			for (int j = 0; j < 10; j++) {
				String value;
				value = CryptSession.getInstance().getProperty(
						PUBLIC_NODE_ + j, sessionId);
				if (value != null) {
					nodeList.add(value);
					size++;
				}
			}

		} catch (ImixsCryptException e) {
			e.printStackTrace();
			// return empty list
			return null;
			
		}
		String[] result = new String[size];

		for (int j = 0; j < size; j++) {
			result[j] = nodeList.get(j);
		}

		return result;
	}

	/**
	 * This method sets a value in the local property file. The value is part of
	 * the body
	 * 
	 * @param property
	 *            - property to be set
	 */
	@POST
	@Path("/session/properties/{property}")
	@Consumes("text/plain")
	@Deprecated
	public Response setProperty(
			String value,
			@PathParam("property") String property,
			@CookieParam(value = IdentityService.SESSION_COOKIE) String sessionId) {

		try {
			CryptSession.getInstance().setProperty(property, value, sessionId);
		} catch (Exception e) {

			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).build();

		}

		// success HTTP 200
		return Response.status(Response.Status.OK).build();

	}

	/**
	 * This method gets a value of the local property file.
	 * 
	 * @param property
	 *            - property to be read
	 */
	@GET
	@Path("/session/properties/{property}")
	@Produces("text/plain")
	@Deprecated
	public Response getProperty(
			@PathParam("property") String property,
			@CookieParam(value = IdentityService.SESSION_COOKIE) String sessionId) {
		String value = null;
		try {
			value = CryptSession.getInstance().getProperty(property, sessionId);
		} catch (Exception e) {

			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(null).build();

		}
		// success HTTP 200
		return Response.status(Response.Status.OK).entity(value).build();

	}

	/**
	 * Adds a new public node entry into the propties file
	 * 
	 * public node entries are a sequence of numbers
	 * 
	 * <code>
	 *  public.node.0=xxx
	 *  public.node.1=yyy
	 *  public.node.2.zzz
	 * </code>
	 * 
	 * @param aNode
	 * @throws ImixsCryptException
	 */
	private void addPublicNodeToProperties(String aNode, String asessionId)
			throws ImixsCryptException {
		int pos = 0;
		// verify if node exists
		for (int j = 0; j < 10; j++) {
			String value = CryptSession.getInstance().getProperty(
					PUBLIC_NODE_ + j, asessionId);
			if (value != null && value.equals(aNode)) {
				// still exits
				return;
			}
			if (value != null)
				pos++;
		}

		// add new value

		CryptSession.getInstance().setProperty(PUBLIC_NODE_ + pos, aNode,
				asessionId);
	}

}