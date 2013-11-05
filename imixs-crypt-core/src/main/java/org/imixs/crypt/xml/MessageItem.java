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
package org.imixs.crypt.xml;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a single message item.
 * 
 * @author rsoika
 * 
 */
@XmlRootElement(name = "messageItem")
public class MessageItem implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String recipient;
	private String sender;
	private String message;
	private String comment;
	private long created;
	private String signature;
	private String digest;

	public String getRecipient() {
		return recipient;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String asender) {
		if (asender != null)
			asender = asender.trim().toLowerCase();
		this.sender = asender;
	}

	/**
	 * usernames are lower cased automatically!
	 * @param arecipient
	 */
	public void setRecipient(String arecipient) {
		if (arecipient != null)
			arecipient = arecipient.trim().toLowerCase();
		this.recipient = arecipient;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getDigest() {
		return digest;
	}

	public void setDigest(String digest) {
		this.digest = digest;
	}

	
	
}
