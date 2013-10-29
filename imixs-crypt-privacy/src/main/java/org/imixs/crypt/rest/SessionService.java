package org.imixs.crypt.rest;

import java.security.PublicKey;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.imixs.crypt.Base64Coder;
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
	 * This method post a password and stores it into the local vm's
	 * CryptSession instance. The password will be stored in the local instance
	 * of the CryptSession and a sessionId will be returnd.
	 * 
	 * The Method verifies if a local key pair exists. If not the method will
	 * generate a new key pair, encrypted with the given password.
	 * 
	 * Finally the method creates the ImixsCryptSession Cookie with the current
	 * sessionId.
	 * 
	 * @param password
	 *            - password to be set
	 */
	@POST
	@Path("/session")
	@Consumes("text/plain")
	public Response setPrivateKeyPassword(String password) {
		CryptSession.getInstance().setPassword(password);
		if (password != null && !password.isEmpty()) {
			// verify if a key pair exists
			PublicKey publicKey = CryptSession.getInstance()
					.getLocalPublicKey();
			if (publicKey == null) {
				logger.info("[SessionService] generate new KeyPair...");
				try {
					CryptSession.getInstance().generateKeyPair();
				} catch (Exception e) {
					e.printStackTrace();
					return Response
							.status(Response.Status.INTERNAL_SERVER_ERROR)
							.type(MediaType.TEXT_PLAIN)
							.cookie(new NewCookie("ImixsCryptSessionID", ""))
							.build();
				}
			} else {
				logger.info("Session opened");
			}

		} else {
			logger.info("Session closed");
		}

		// update cookie
		String newSessionId = CryptSession.getInstance().getSessionId();
		NewCookie sessionCookie = new NewCookie("ImixsCryptSessionID",
				newSessionId);

		// success HTTP 200
		return Response.ok(MediaType.TEXT_PLAIN).cookie(sessionCookie).build();

		// return Response.ok(MediaType.TEXT_PLAIN).build();

	}

	/**
	 * Returns the public key. If not yet created the method returns an empty
	 * key. The method can be used to test if the PrivateCrypt Server is
	 * initalized with a valid key pair.
	 * 
	 * @return
	 */
	@GET
	@Path("/session")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPublicKey() {
		PublicKey publicKey = null;
		KeyItem key = new KeyItem();

		try {
			publicKey = CryptSession.getInstance().getLocalPublicKey();
			if (publicKey == null) {
				logger.info("KeyPair not yet created");

				// return Response.status(Response.Status.ACCEPTED).entity(null)
				// .type(MediaType.APPLICATION_JSON).build();
				// Return an emypt key
				return Response.status(Response.Status.OK).entity(key)
						.type(MediaType.APPLICATION_JSON).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(null).type(MediaType.APPLICATION_JSON).build();
		}
		// lower case

		// Return the key
		key.setKey(Base64Coder.encodeLines(publicKey.getEncoded()));
		key.setUser("");

		return Response.status(Response.Status.OK).entity(key)
				.type(MediaType.APPLICATION_JSON).build();

	}
}