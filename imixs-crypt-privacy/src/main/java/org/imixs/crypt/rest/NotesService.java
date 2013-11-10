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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.crypt.xml.NoteItem;

/**
 * The Notes Service ecnrypts and decrypt local data with the local key pair
 * 
 * Encrypted Messages will be base64 encoded
 * 
 * The method getNotes returns a list of all notes stored in the local data
 * directory
 * 
 * @author rsoika
 * 
 */
@Path("/rest/notes")
@Deprecated
public class NotesService {

	private String ENCODING = "UTF-8";

	private final static Logger logger = Logger.getLogger(NotesService.class
			.getName());

	/**
	 * This method encryps a message with the users public key The property user
	 * have to be empty!
	 * 
	 * @param keyItem
	 * 
	 */
	@POST
	@Path("/{name}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putNote(String message, @PathParam("name") String name,
			@CookieParam(value = IdentityService.SESSION_COOKIE) String sessionId) {

		// validate key
		if (message == null) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();
		}
		if (name == null || name.isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();
		}

		try {

//			byte[] data = message.getBytes(ENCODING);
//			byte[] encrypted = CryptSession.getInstance().ecryptLocal(data,
//					sessionId);



			logger.info("[NotesService] encrypted=" + name);
		} catch (Exception e) {

			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();

		}

		// success HTTP 200
		return Response.ok(message, MediaType.APPLICATION_JSON).build();

	}

	/**
	 * This method decrypts a message with the users private key
	 * 
	 * The message is expected in base64 encoded data!
	 * 
	 * @param keyItem
	 * 
	 */
	@GET
	@Path("/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getNote(@PathParam("name") String name,
			@CookieParam(value =  IdentityService.SESSION_COOKIE) String sessionId) {
		String text = null;
		// validate key
		if (name == null || name.isEmpty()) {
			return null;
		}

		// decrypt file
		try {

			byte[] data = Files.readAllBytes(Paths.get(CryptSession
					.getInstance().getRootPath() + "data/local/" + name));

//			byte[] decrypted = CryptSession.getInstance().decryptLocal(data,
//					sessionId);
//			text = new String(decrypted, ENCODING);

			logger.info("[NotesService] decrypted=" + name);
		} catch (Exception e) {

			e.printStackTrace();
			return null;

		}
		// success HTTP 200
		return text;

	}

	/**
	 * This method returns a list with all notes
	 * 
	 * The message is expected in base64 encoded data!
	 * 
	 * @param keyItem
	 * 
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<NoteItem> getNotes(
			@CookieParam(value =  IdentityService.SESSION_COOKIE) String sessionId) {

		if (!CryptSession.getInstance().isValidSession(sessionId))
			return null;

		File folder = new File(CryptSession.getInstance().getRootPath()
				+ "data/local");
		File[] listOfFiles = folder.listFiles();

		List<NoteItem> result = new ArrayList<>();
		for (File file : listOfFiles) {
			Long lastModified = file.lastModified();
			String name = file.getName();

			NoteItem note = new NoteItem();
			note.setModified(new Date(lastModified));
			note.setName(name);
			result.add(note);
		}

		return result;
	}

	/**
	 * This method deletes a note
	 * 
	 */
	@DELETE
	@Path("/{name}")
	public Response deleteNote(@PathParam("name") String name,
			@CookieParam(value =  IdentityService.SESSION_COOKIE) String sessionId) {

		// validate key
		if (name == null || name.isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (!CryptSession.getInstance().isValidSession(sessionId))
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).build();

		logger.info("[NotesService] delete '" + name + "'");
		// delete file
		try {
			Files.delete(Paths.get(CryptSession.getInstance().getRootPath()
					+ "data/local/" + name));
		} catch (IOException e) {

			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).build();

		}

		// success HTTP 200
		return Response.status(Response.Status.OK)
				.type(MediaType.APPLICATION_JSON).build();

	}

}