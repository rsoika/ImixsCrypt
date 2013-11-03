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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.crypt.xml.MessageItem;

/**
 * The Message is used to encrypt and decrypt messages provided in a MessageItem.
 * A Message will be encrypted with the public key of the receifer and decrypted with the
 * local private key.
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

	
	private final static Logger logger = Logger.getLogger(MessageService.class
			.getName());

	
	

	/**
	 * This method encryps a message with a public key. The property user
	 * is the name of the public ke
	 * 
	 * @param keyItem
	 * 
	 */
	@GET
	@Path("/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public MessageItem[] getMessageList( @PathParam("name") String name) {

		// validate key - check if user is available 
		if (name == null) {
			return null;
		}		

		//List<MessageItem> result=MessageRepository.getInstance().getMessage(name);
		
		// simulate
		List<MessageItem> result=new ArrayList<MessageItem>();
		MessageItem m=new MessageItem();
		m.setMessage("hallo");
		m.setUser("sepp");
		result.add(m);
		 m=new MessageItem();
		m.setMessage("hallo Welt");
		m.setUser("Anna");
		result.add(m);
		
		MessageItem [] messageArray = result.toArray(new MessageItem[result.size()]);
		// success HTTP 200
		return messageArray;

	}
	
	
	
	/**
	 * This method puts an encrypted message into the message store.
	 * 
	 * @param message
	 * 
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putMessage(MessageItem message) {

		// validate key - check if user is available 
		if (message == null) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();
		}
		if (message.getUser() == null || message.getUser().isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();
		}

		try {
		
			logger.info("decrypted=" + message.getMessage());
		} catch (Exception e) {

			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();

		}

		// success HTTP 200
		return Response.ok(message, MediaType.APPLICATION_JSON).build();

	}
}