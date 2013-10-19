package org.imixs.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Generats differnt types of key pairs
 * 
 * @author rsoika
 * 
 */
public class ImixsKeyGenerator {

	/**
	 * generates a key pair and stores it in the filesystem
	 * 
	 * @param privateKeyFile
	 * @param publicKeyFile
	 * @param algorithm
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static void generateKeyPair(String privateKeyFileName,
			String publicKeyFileName, String algorithm)
			throws NoSuchAlgorithmException, IOException {
		final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
		keyGen.initialize(1024);

		final KeyPair key = keyGen.generateKeyPair();

		File privateKeyFile = new File(privateKeyFileName);
		File publicKeyFile = new File(publicKeyFileName);

		// Create files to store public and private key
		if (privateKeyFile.getParentFile() != null) {
			privateKeyFile.getParentFile().mkdirs();
		}
		privateKeyFile.createNewFile();

		if (publicKeyFile.getParentFile() != null) {
			publicKeyFile.getParentFile().mkdirs();
		}
		publicKeyFile.createNewFile();

		// Saving the Public key in a file
		ObjectOutputStream publicKeyOS = new ObjectOutputStream(
				new FileOutputStream(publicKeyFile));
		publicKeyOS.writeObject(key.getPublic());
		publicKeyOS.close();

		// Saving the Private key in a file
		ObjectOutputStream privateKeyOS = new ObjectOutputStream(
				new FileOutputStream(privateKeyFile));
		privateKeyOS.writeObject(key.getPrivate());
		privateKeyOS.close();

		// now generyte keys

	}

	public static PublicKey getPublicKey(String PUBLIC_KEY_FILE)
			throws FileNotFoundException, ClassNotFoundException {
		PublicKey key = null;
		InputStream inputStream = null;

		try {
			inputStream = new ObjectInputStream(new FileInputStream(
					PUBLIC_KEY_FILE));
			key = (PublicKey) ((ObjectInputStream) inputStream).readObject();
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return key;
	}

	public static PrivateKey getPrivatKey(String PRIVATE_KEY_FILE)
			throws FileNotFoundException, ClassNotFoundException {
		PrivateKey privateKey = null;
		InputStream inputStream = null;
		try {
			inputStream = new ObjectInputStream(new FileInputStream(
					PRIVATE_KEY_FILE));
			privateKey = (PrivateKey) ((ObjectInputStream) inputStream)
					.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return privateKey;
	}

}
