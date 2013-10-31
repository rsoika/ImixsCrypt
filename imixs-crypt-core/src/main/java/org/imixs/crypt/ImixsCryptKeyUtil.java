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

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * KeyUtil is the general interface for key management and encryption methods.
 * 
 * @author rsoika
 * 
 */
public interface ImixsCryptKeyUtil {

	public void generateKeyPair(String privateKeyFileName,
			String publicKeyFileName, String password) throws ImixsCryptException;

	public PublicKey getPublicKey(String filename) throws ImixsCryptException;

	public byte[] encrypt(byte[] data, PublicKey key) throws ImixsCryptException;

	public byte[] decrypt(byte[] encryptedData, PrivateKey key) throws ImixsCryptException;

	public PrivateKey getPrivateKey(String filename, String password)
			throws ImixsCryptException;
}
