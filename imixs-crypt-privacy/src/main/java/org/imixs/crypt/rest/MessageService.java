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
 * The Message is used to encrypt and decrypt messages provided in a MessageItem.
 * A Message will be encrypted with the public key of the receifer and decrypted with the
 * local private key.
 *  
 * 
 * Encrypted Messages will be base64 encoded
 * 
 * @author rsoika
 * 
 */
@Path("/rest/message")
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
	@POST
	@Path("/encrypt")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putEncrypt(MessageItem message) {

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
			logger.info("[MessageService] encrypting message for '" + message.getUser() + "'");
			byte[] data = message.getMessage().getBytes(ENCODING);
			byte[] encrypted = CryptSession.getInstance().ecrypt(data,message.getUser());

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
		
		// find the users public key....
		try {
			logger.info("[MessageService] decrypting message from '" + message.getUser() + "'");

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