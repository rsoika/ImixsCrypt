package org.imixs.crypt.rest;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.crypt.Base64Coder;
import org.imixs.crypt.ImixsRSAKeyUtil;
import org.imixs.crypt.server.CryptSession;
import org.imixs.crypt.xml.MessageItem;

/**
 * Message Item Resource
 * 
 * Encrypted Messages will be base64 encoded
 * 
 * @author rsoika
 * 
 */
@Path("/rest/message")
public class MessageService {

	private final static Logger logger = Logger.getLogger(KeyService.class
			.getName());

	@GET
	@Produces("text/plain")
	@Path("/world")
	public String getHelloWorld() {
		// Return some cliched textual content
		return "Hello World";
	}

	

	/**
	 * This method ecryps a message with the users public key
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
		if (message == null || message.getUser().isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();

		}

		// find the users public key....
		PublicKey publicKey;
		try {
			publicKey = ImixsRSAKeyUtil.getPemPublicKey(getFilename(message
					.getUser()) + ".pub");

			byte[] encrypted = ImixsRSAKeyUtil.encrypt(message.getMessage(),
					publicKey);
			
			message.setMessage(Base64Coder.encodeLines(encrypted) );

			
			logger.info("encrypted=" + message.getMessage());
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
	 * @param keyItem
	 * 
	 */
	@POST
	@Path("/decrypt")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putDecrypt(MessageItem message) {

		// validate key
		if (message == null || message.getUser().isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();

		}

		// find the users public key....
		PrivateKey privateKey;
		try {
			privateKey = ImixsRSAKeyUtil.getPemPrivateKey(getFilename(message
					.getUser()) + "",CryptSession.getInstance().getPassword());

			String decrypted= ImixsRSAKeyUtil.decrypt(message.getMessage().getBytes(),
					privateKey);
			
			message.setMessage(decrypted);

			
			logger.info("decrypted=" + message.getMessage());
		} catch (Exception e) {

			e.printStackTrace();
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(message).build();

		}
		// success HTTP 200
		return Response.ok(message, MediaType.APPLICATION_JSON).build();

	}

	
	
	
	
	
	
	
	
	
	
	

	private String getFilename(String user) {
		user = user.toLowerCase();
		user = user.replace("@", "_");
		user = user.replace(" ", "_");

		return "src/test/resources/" + user;
	}

}