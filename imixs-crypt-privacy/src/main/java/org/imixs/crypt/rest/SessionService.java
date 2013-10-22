package org.imixs.crypt.rest;

import java.security.PublicKey;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.crypt.Base64Coder;
import org.imixs.crypt.server.CryptSession;
import org.imixs.crypt.xml.KeyItem;

/**
 * Provides a service to open / close a crypt session
 * 
 * @author rsoika
 * 
 */
@Path("/rest")
public class SessionService {

	private final static Logger logger = Logger.getLogger(SessionService.class
			.getName());

	/**
	 * This method post a password and opens a new session
	 * 
	 * @param keyItem
	 * 
	 */
	@POST
	@Path("/session")
	@Consumes("text/plain")
	public Response openSession(String password) {
		// validate key
		if (password == null || password.isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).build();
		}
		CryptSession.getInstance().setPassword(password);
		logger.info("Session opened");
		// success HTTP 200
		return Response.ok(MediaType.TEXT_PLAIN).build();

	}

	/**
	 * Closes a session
	 * 
	 * @return
	 */
	@DELETE
	@Path("/session")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response closeSession() {

		CryptSession.getInstance().setPassword(null);
		logger.info("Session closed");
		// success HTTP 200
		return Response.ok(MediaType.TEXT_PLAIN).build();

	}

	/**
	 * This method creates a new Key Pair
	 * 
	 * @return
	 */
	@PUT
	@Path("/session")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response createKeyPair(String password) {

		if (password == null || password.isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).build();
		}
		CryptSession.getInstance().setPassword(password);

		try {
			CryptSession.getInstance().generateKeyPair();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.type(MediaType.TEXT_PLAIN).build();
		}
		logger.info("Session opened");
		// success HTTP 200
		return Response.ok(MediaType.TEXT_PLAIN).build();

	}

	/**
	 * Returns the public key. If not yet created the method returns a error
	 * code
	 * 
	 * @return
	 */
	@GET
	@Path("/session/public")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPublicKey() {
		PublicKey publicKey=null;
		try {
			 publicKey = CryptSession.getInstance().getPublicKey();
			if (publicKey==null)  {
				logger.info("KeyPair not yet created");
				
				return Response.status(Response.Status.ACCEPTED).entity(null)
						.type(MediaType.APPLICATION_JSON).build();
				
			
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(null)
					.type(MediaType.APPLICATION_JSON).build();
		}
		// lower case

		// Return the key
		KeyItem key = new KeyItem();
		key.setKey(Base64Coder.encodeLines(publicKey.getEncoded()));
		key.setUser("");
		
		return Response.status(Response.Status.OK).entity(key)
				.type(MediaType.APPLICATION_JSON).build();

	}
}