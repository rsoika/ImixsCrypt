package org.imixs.crypt.rest;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.imixs.crypt.ImixsRSAKeyUtil;
import org.imixs.crypt.server.CryptSession;
import org.imixs.crypt.xml.KeyItem;

/**
 * Provides a service to get and put personal keys
 * 
 * @author rsoika
 * 
 */
@Path("/rest/keys")
public class KeyService {

	private final static Logger logger = Logger.getLogger(KeyService.class
			.getName());

	/**
	 * Get the public key
	 * 
	 * @return
	 */
	@GET
	@Path("/public/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	public KeyItem getPublic(@PathParam("user") String user) {
		
		// lower case
		user=user.toLowerCase();
		// Return the key
		KeyItem key = new KeyItem();
		key.setKey("xxxxxxxxxxxxxxxxxxxxxxxxxx");
		key.setUser(user);
		return key;
		
	}

	/**
	 * This method post a keyItem. The keyItem must contain a user. The value
	 * key is optional. If the key is empty the method will generate a new key
	 * for the user.
	 * 
	 * @param keyItem
	 * 
	 */
	@POST
	@Path("/key")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response putKey(KeyItem akey) {

		// validate key
		if (akey == null || akey.getUser().isEmpty()) {
			return Response.status(Response.Status.NOT_ACCEPTABLE)
					.type(MediaType.APPLICATION_JSON).entity(akey).build();

		}

		// test if new key should be generated
		if (akey.getKey().isEmpty()) {
			// generate new key pair
			logger.info("[KeyResource] generate new key pair....");
			try {
				ImixsRSAKeyUtil.generateKeyPair(getFilename(akey.getUser()),
						getFilename(akey.getUser()) + ".pub",
					CryptSession.getInstance().getPassword());
			} catch (Exception e) {
				e.printStackTrace();
				return Response.status(Response.Status.NOT_ACCEPTABLE).build();
			}
		}

		logger.info("Password=" + akey);

		// success HTTP 200
		return Response.ok(akey, MediaType.APPLICATION_JSON).build();

	}

	private String getFilename(String user) {
		user=user.toLowerCase();
		user = user.replace("@", "_");
		user = user.replace(" ", "_");

		return "src/test/resources/" + user;
	}

}