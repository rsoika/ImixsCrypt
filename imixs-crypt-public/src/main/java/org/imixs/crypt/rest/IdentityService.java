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
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.crypt.xml.KeyItem;

/**
 * The IdentityService provides methods to post and get PublicKeys.
 * 
 * Public keys are stored into the data directory ./ImixsCrpyt/keys/
 * 
 * @author rsoika
 * 
 */
@Path("/identities")
public class IdentityService {

	private String ENCODING = "UTF-8";
	private String DATA_DIRECTORY="ImixsCrypt/";

	private final static Logger logger = Logger.getLogger(IdentityService.class
			.getName());

	/**
	 * This method posts a public key and stores the key into the data directory /keys/
	 * 
	 * @param keyItem
	 * 
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putPublicKey(KeyItem keydata) {

		if (keydata == null || keydata.getUser() == null
				|| keydata.getUser().isEmpty() || keydata.getKey().isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(keydata).build();
		}

		try {
			byte[] data = keydata.getKey().getBytes(ENCODING);
			String keyFileName=DATA_DIRECTORY +"keys/"+ keydata.getUser();
			File keyFile = new File(keyFileName);
			// Create files to store public and private key
			if (keyFile.getParentFile() != null) {
				keyFile.getParentFile().mkdirs();
			}
			// save data into file
			Files.write(Paths.get(keyFileName), data);
			logger.info("[IdentityService] stored Public key:" + keydata.getUser());
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
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPublicKey(@PathParam("id") String user) {
		KeyItem key = new KeyItem();
		try {
			// save data into file
			byte[] keyData = Files.readAllBytes(Paths
					.get(DATA_DIRECTORY + "keys/"+user));

			key.setUser(user);
			key.setKey(new String(keyData));

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(key).type(MediaType.APPLICATION_JSON).build();
		}
		return Response.status(Response.Status.OK).entity(key)
				.type(MediaType.APPLICATION_JSON).build();
	}

}