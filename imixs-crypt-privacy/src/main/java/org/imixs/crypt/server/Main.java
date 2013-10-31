/*******************************************************************************
 *  ImixsCrypt
 *  Copyright (C) 2013 Ralph Soika,  
 *  https://github.com/rsoika/ImixsCrypt
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Project: 
 *  	https://github.com/rsoika/ImixsCrypt
 *  
 *  Contributors:    	
 *  	Ralph Soika - Software Developer
 *******************************************************************************/

package org.imixs.crypt.server;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.utils.ArraySet;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class Main {

	public static void main(String[] args) throws IOException {
		try {
			// HttpServer server = HttpServer.createSimpleServer();
			// create jersey-grizzly server
			ResourceConfig rc = new PackagesResourceConfig(
					"org.imixs.crypt.rest");

			// add json support
			rc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);

			HttpServer server = GrizzlyServerFactory.createHttpServer(
					"http://127.0.0.1:4040", rc);

			// add static pages handler
			StaticHttpHandler staticHandler=new StaticHttpHandler(getTemplatePath());
			staticHandler.addDocRoot("/app");
			server.getServerConfiguration().addHttpHandler(staticHandler, "/app");
			
			
			
			
			
			
			server.start();
			
			
		 ArraySet<File>	ding=staticHandler.getDocRoots();
		 

			System.out.println("Press any key to stop the server...");
			System.in.read();
		} catch (Exception e) {
			System.err.println(e);
		}

	}

	private static String getTemplatePath() throws URISyntaxException {
		
		//return "src/main/resources/webapp/";
		
		return Main.class.getClassLoader().getResource("webapp")
				.getPath();
	}
}
