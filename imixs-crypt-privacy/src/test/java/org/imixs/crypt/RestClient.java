/*******************************************************************************
 *  Imixs Workflow 
 *  Copyright (C) 2001, 2011 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
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
 *  	http://www.imixs.org
 *  	http://java.net/projects/imixs-workflow
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika - Software Developer
 *******************************************************************************/
package org.imixs.crypt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

/**
 * This ServiceClient is a WebService REST Client which encapsulate the
 * communication with a REST web serice based on the Imixs Workflow REST API.
 * The Implementation is based on the JAXB API.
 * 
 * The ServiceClient supports methods for posting EntityCollections and
 * XMLItemCollections.
 * 
 * The post method expects the rest service URI and a Dataobject based ont the
 * Imixs Workflow XML API
 * 
 * @see org.imixs.workflow.jee.rest
 * @author Ralph Soika
 * 
 */
public class RestClient {

	private String serviceEndpoint;

	private String encoding = "UTF-8";

	private String mediaType = MediaType.APPLICATION_JSON;

	private int iLastHTTPResult = 0;

	private CookieManager cookieManager = null;
	private String content = null;

	private final static Logger logger = Logger.getLogger(RestClient.class
			.getName());

	public void setEncoding(String aEncoding) {
		encoding = aEncoding;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMediaType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getEncoding() {
		return encoding;
	}

	/**
	 * Returns all cookies set during the last request
	 * @return
	 */
	public CookieManager getCookies() {
		return cookieManager;
	}

	/**
	 * Set the cookies to be used for the next request
	 * @param cookieManager
	 */
	public void setCookies(CookieManager cookieManager) {
		 this.cookieManager=cookieManager;
	}

	/**
	 * This method posts an JSON String to a Rest Service URI Endpoint.
	 * 
	 * 
	 * @param uri
	 *            - Rest Endpoint RUI
	 * @param entityCol
	 *            - an Entity Collection
	 * @return HTTPResult
	 */
	public int get(String uri) throws Exception {
		URL obj = new URL(uri);
		HttpURLConnection urlConnection = (HttpURLConnection) obj
				.openConnection();

		// optional default is GET
		urlConnection.setRequestMethod("GET");
		
		addCookies(urlConnection);

		int responseCode = urlConnection.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + uri);
		System.out.println("Response Code : " + responseCode);
		readResponse(urlConnection);

		return responseCode;
	}

	/**
	 * This method posts an JSON String to a Rest Service URI Endpoint.
	 * 
	 * 
	 * @param uri
	 *            - Rest Endpoint RUI
	 * @param entityCol
	 *            - an Entity Collection
	 * @return HTTPResult
	 */
	public int post(String uri, String aContent) throws Exception {
		return sendData(uri, aContent, "POST");
	}

	public int delete(String uri, String aContent) throws Exception {
		return sendData(uri, aContent, "DELETE");
	}

	private int sendData(String uri, String aContent, String aMethod)
			throws Exception {
		PrintWriter printWriter = null;
		HttpURLConnection urlConnection = null;
		boolean hasOutput = true;

		try {
			serviceEndpoint = uri;
			iLastHTTPResult = 500;

			urlConnection = (HttpURLConnection) new URL(serviceEndpoint)
					.openConnection();
			urlConnection.setRequestMethod(aMethod);
			if ("DELETE".equals(aMethod))
				hasOutput = false;

			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			urlConnection.setAllowUserInteraction(false);

			/** * HEADER ** */
			urlConnection.setRequestProperty("Content-Type", mediaType
					+ "; charset=" + encoding);

			addCookies(urlConnection);
			
			if (aContent != null && !aContent.isEmpty()) {
				StringWriter writer = new StringWriter();

				writer.write(aContent);
				writer.flush();

				// compute length
				urlConnection
						.setRequestProperty(
								"Content-Length",
								""
										+ Integer.valueOf(writer.toString()
												.getBytes().length));

				printWriter = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(urlConnection.getOutputStream(),
								encoding)));

				printWriter.write(writer.toString());
				printWriter.close();

			}
			String sHTTPResponse = urlConnection.getHeaderField(0);
			readCookies(urlConnection);
			try {
				iLastHTTPResult = Integer.parseInt(sHTTPResponse.substring(9,
						12));
			} catch (Exception eNumber) {
				// eNumber.printStackTrace();
				iLastHTTPResult = 500;
			}

			// get content of result
			if (hasOutput)
				readResponse(urlConnection);

		} catch (Exception ioe) {
			// ioe.printStackTrace();
			throw ioe;
		} finally {
			// Release current connection
			if (printWriter != null)
				printWriter.close();
		}

		return iLastHTTPResult;
	}

	/**
	 * Put the resonse string into the content property
	 * 
	 * @param urlConnection
	 * @throws IOException
	 */
	private void readResponse(URLConnection urlConnection) throws IOException {
		// get content of result
		StringWriter writer = new StringWriter();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				logger.fine(inputLine);
				writer.write(inputLine);
			}
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			if (in != null)
				in.close();
		}

		setContent(writer.toString());

	}

	
	private void readCookies(HttpURLConnection connection) throws URISyntaxException {
		String COOKIES_HEADER = "Set-Cookie";
		cookieManager = new java.net.CookieManager();

		Map<String, List<String>> headerFields = connection.getHeaderFields();
		List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

		if (cookiesHeader != null) {
			for (String cookie : cookiesHeader) {
				HttpCookie ding = HttpCookie.parse(cookie).get(0);
				
				cookieManager.getCookieStore().add(connection.getURL().toURI(),ding);
			}
		}
	}
	
	private void addCookies(
			HttpURLConnection connection) {
		if (cookieManager==null)
			return;
		
		String values="";
		for (HttpCookie acookie: cookieManager.getCookieStore().getCookies()) {
			values=values+acookie+",";
		}
		
	
		
		if (cookieManager.getCookieStore().getCookies().size() > 0) {
			connection.setRequestProperty("Cookie",values);
		}
	}

}
