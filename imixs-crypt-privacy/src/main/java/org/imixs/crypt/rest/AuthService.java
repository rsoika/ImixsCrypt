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

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The Auth Service provides authentication functioanlity
 * 
 * @author rsoika
 * 
 */
@Path("/rest/auth")
public class AuthService {

	private String ENCODING = "UTF-8";

	private final static Logger logger = Logger.getLogger(AuthService.class
			.getName());

	/**
	 * This method should implement an openID server
	 * 
	 * @see https://github.com/rsoika/ImixsCrypt/issues/2
	 * 
	 * Seems not to work because local server is not connected from OpenID clients 
	 * 
	 */
	@GET
	@Consumes(MediaType.TEXT_HTML)
	@Produces(MediaType.TEXT_HTML)
	@Path("/openid")
	public Response getMyID() {
		logger.info("[AuthService] OpenID request started....");
		
		
		//String query=httpRequest.getQueryString();
		logger.info("[AuthService] query=" );
		

		// success HTTP 200
		return Response.status(Response.Status.OK)
				.type(MediaType.APPLICATION_JSON).build();
	}

	
}