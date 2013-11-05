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

package org.imixs.crypt;

/**
 * ImixsCryptException is the wrapper exception for all keyUtil exceptions
 * 
 * @author rsoika
 * 
 */
public class ImixsCryptException extends Exception {

	private static final long serialVersionUID = 1L;

	public static String FILE_NOT_FOUND = "FILE_NOT_FOUND";
	public static String NO_SUCH_ALGORITHM = "NO_SUCH_ALGORITHM";
	public static String INVALID_KEY = "INVALID_KEY";
	public static String UNSUPPORTED_ENCODING = "UNSUPPORTED_ENCODING";

	protected String errorCode = "UNDEFINED";

	public ImixsCryptException(String aErrorCode, String message) {
		super(message);
		errorCode = aErrorCode;

	}

	public ImixsCryptException(String aErrorCode, Exception e) {
		super(aErrorCode, e);
		errorCode = aErrorCode;

	}

	public ImixsCryptException(String aErrorCode, String message, Exception e) {
		super(message, e);

		errorCode = aErrorCode;

	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

}
