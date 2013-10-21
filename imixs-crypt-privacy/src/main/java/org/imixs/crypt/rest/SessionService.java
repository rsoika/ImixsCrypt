package org.imixs.crypt.rest;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.crypt.server.CryptSession;

/**
 * Provides a service to open / close a crypt session
 * 
 * @author rsoika
 * 
 */
@Path("/rest/session")
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
	@Path("/open")
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
	@Path("/close")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response closeSession() {
		CryptSession.getInstance().setPassword(null);
		logger.info("Session closed");
		// success HTTP 200
		return Response.ok(MediaType.TEXT_PLAIN).build();

	}

}