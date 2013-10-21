

package org.imixs.crypt.rest;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
 
/**
 * Provides a service to get and put personal keys
 * 
 * @author rsoika
 *
 */
@Path("/rest/mykey")
public class MyKeyResource {

	private final static Logger logger = Logger.getLogger(MyKeyResource.class.getName());

	
	/**
	 * Get the public key
	 * @return
	 */
    @GET 
    @Produces("text/plain")
    @Path("/public")
    public String getPublic() {
        // Return some cliched textual content
        return "Hello This is my public key";
    }
	
	
    
    /**
	 * This method post a collection of ItemCollection objects to be processed
	 * by the WorkflowManager.
	 * 
	 * @param worklist
	 *            - workitem list data
	 */
	@PUT
	@Path("/")
	@Consumes({ "application/json"})
	public Response putKey(String password) {

		logger.info("Password=" + password);
	
		return Response.status(Response.Status.OK).build();
	}

}