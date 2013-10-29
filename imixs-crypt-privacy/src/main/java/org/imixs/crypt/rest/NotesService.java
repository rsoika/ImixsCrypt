package org.imixs.crypt.rest;

import java.nio.file.Files;
import java.nio.file.Paths;
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

/**
 * The Notes Service ecnrypts and decrypt local data with the local key pair
 * 
 * 
 * Encrypted Messages will be base64 encoded
 * 
 * @author rsoika
 * 
 */
@Path("/rest/notes")
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
	@Path("/encrypt/{name}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putNotes(String message, @PathParam("name") String name,
			@CookieParam(value = "ImixsCryptSessionID") String sessionId) {

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

			byte[] data = message.getBytes(ENCODING);
			byte[] encrypted = CryptSession.getInstance().ecryptLocal(data,
					sessionId);

			// save data into file
			Files.write(
					Paths.get(CryptSession.getInstance().getRootPath()
							+ "data/notes/" + name), encrypted);

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
	@Path("/decrypt/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getNotes(@PathParam("name") String name,
			@CookieParam(value = "ImixsCryptSessionID") String sessionId) {
		String text = null;
		// validate key
		if (name == null || name.isEmpty()) {
			return null;
		}

		// find the users public key....
		try {

			byte[] data = Files.readAllBytes(Paths.get(CryptSession
					.getInstance().getRootPath() + "data/notes/" + name));

			byte[] decrypted = CryptSession.getInstance().decryptLocal(data,sessionId);
			text = new String(decrypted, ENCODING);

			logger.info("[NotesService] decrypted=" + name);
		} catch (Exception e) {

			e.printStackTrace();
			return null;

		}
		// success HTTP 200
		return text;

	}

}