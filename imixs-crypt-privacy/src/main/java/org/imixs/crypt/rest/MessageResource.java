

package org.imixs.crypt.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.imixs.crypt.xml.MessageItem;
 
/**
 * Message Item Resource 
 * @author rsoika
 *
 */
@Path("/message")
public class MessageResource {


    @GET 
    @Produces("text/plain")
    @Path("/world")
    public String getHelloWorld() {
        // Return some cliched textual content
        return "Hello World";
    }
	
	
	
	@GET 
    //@Produces("application/json")
	@Produces("application/xml")
    public MessageItem getClichedMessage() {
    	
    	MessageItem m=new MessageItem();
    	m.setMessage("Hello");
    	m.setUser("user");
        // Return some cliched textual content
        return m;
    }
	
	
	@GET 
    @Produces("application/json")
    @Path("/json")
	public MessageItem getJsonMessage() {
    	
    	MessageItem m=new MessageItem();
    	m.setMessage("Hello");
    	m.setUser("user");
        // Return some cliched textual content
        return m;
    }
}