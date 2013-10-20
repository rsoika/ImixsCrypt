

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
@Path("/rest")
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
	 @Path("/message")
    public MessageItem getClichedMessage() {
    	
    	MessageItem m=new MessageItem();
    	m.setMessage("Hello");
    	m.setUser("user");
        // Return some cliched textual content
        return m;
    }
	
	
	@GET 
    @Produces("application/json")
    @Path("/message/json")
	public MessageItem getJsonMessage() {
    	
    	MessageItem m=new MessageItem();
    	m.setMessage("Hello");
    	m.setUser("user");
        // Return some cliched textual content
        return m;
    }
}