package org.imixs.crypt.rest;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.crypt.Base64Coder;
import org.imixs.crypt.xml.MessageItem;

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
	@Path("/encrypt")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putEncrypt(MessageItem message) {

		// validate key
		if (message == null) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();
		}
		if (message.getUser() != null && !message.getUser().isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();
		}

		try {

			byte[] data = message.getMessage().getBytes(ENCODING);
			byte[] encrypted = CryptSession.getInstance().ecryptLocal(data);

			message.setMessage(new String(Base64Coder.encode(encrypted)));

			logger.info("decrypted=" + message.getMessage());
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
	 * The message is expected  in base64 encoded data!
	 * 
	 * @param keyItem
	 * 
	 */
	@POST
	@Path("/decrypt")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putDecrypt(MessageItem message) {

		// validate key
		if (message == null) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();
		}
		if (message.getUser() != null && !message.getUser().isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();
		}

		// find the users public key....
		try {
			byte[] data = Base64Coder.decode(message.getMessage());
					
			byte[] decrypted = CryptSession.getInstance().decryptLocal(
					data);
			message.setMessage(new String(decrypted,ENCODING));

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