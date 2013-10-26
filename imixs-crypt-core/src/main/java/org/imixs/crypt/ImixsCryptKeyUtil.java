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

	public byte[] encrypt(String text, PublicKey key) throws ImixsCryptException;

	public String decrypt(byte[] text, PrivateKey key) throws ImixsCryptException;

	public PrivateKey getPrivateKey(String filename, String password)
			throws ImixsCryptException;
}
