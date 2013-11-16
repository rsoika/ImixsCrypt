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

import java.io.IOException;
import java.security.PublicKey;
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
import org.imixs.crypt.json.JSONWriter;
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
@Path("/api")
public class IdentityService {

	public final static String SESSION_COOKIE = "ImixsCryptSessionID";
	public final static String DEFAULT_PUBLIC_NODE = "default.public.node";
	private static String DEFAULT_ROOT_PATH = "imixscrypt/";

	private final static Logger logger = Logger.getLogger(IdentityService.class
			.getName());

	/**
	 * This method  stores a public key
	 */
	@POST
	@Path("/identities")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postIdentity(
			IdentityItem identity) {
		
		logger.info("new public key receifed");
		
		try {
			JSONWriter.writeFile(identity, DEFAULT_ROOT_PATH+ identity.getId()+".pub");
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(null).type(MediaType.APPLICATION_JSON).build();
		}
		
		
		// Return OK
		return Response.status(Response.Status.OK).build();
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
				// get the foreign public key
//				publicKey = CryptSession.getInstance().getPublicKey(id,
//						sessionId);
//				if (publicKey != null) {
//					identity.setKey(Base64Coder.encodeLines(publicKey
//							.getEncoded()));
//					identity.setId(id);
//				} else {
//					// Return an emypt key
//					identity.setKey(null);
//					identity.setId(null);
//				}
			
			
			// dummy
			identity.setId("Sepp");
			identity.setKey("12523452345");
			
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(null).type(MediaType.APPLICATION_JSON).build();
		}

		// Return the identity
		return Response.status(Response.Status.OK).entity(identity)
				.type(MediaType.APPLICATION_JSON).build();

	}

}