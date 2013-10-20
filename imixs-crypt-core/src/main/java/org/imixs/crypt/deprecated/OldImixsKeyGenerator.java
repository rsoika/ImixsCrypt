package org.imixs.crypt.deprecated;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.imixs.crypt.Base64Coder;

import sun.misc.BASE64Encoder;

/**
 * Generats differnt types of key pairs
 * 
 * @author rsoika
 * 
 */
public class OldImixsKeyGenerator {

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

		BASE64Encoder b64 = new BASE64Encoder();
		System.out.println("");
		System.out.println("");

		System.out.println("publicKey : "
				+ b64.encode(key.getPublic().getEncoded()));
		System.out.println("");
		System.out.println("");
		System.out.println("privateKey : "
				+ b64.encode(key.getPrivate().getEncoded()));
		System.out.println("");
		System.out.println("");

		// Alternativtest
		System.out.println("");
		System.out.println("Alternative:::::");

		
	
		System.out.println("publicKey : "
				+ Base64Coder.encodeLines(key.getPublic().getEncoded()));
		System.out.println("");
		System.out.println(""); 
		System.out.println("privateKey : "
				+ Base64.encodeBase64String(key.getPrivate().getEncoded()));
		System.out.println("");
		System.out.println("");
		
		
		
		writeKeyToFile(key.getPublic(), publicKeyFileName);

		writeKeyToFile(key.getPrivate(), privateKeyFileName);

	}

	/**
	 * Generates a key pair with password
	 * 
	 * http://stackoverflow.com/questions/5127379/how-to-generate-a-rsa-keypair-
	 * with-a-privatekey-encrypted-with-password
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidParameterSpecException
	 * @throws IOException
	 */
	public static void generateSecureKeyPair(String privateKeyFileName,
			String publicKeyFileName, String algorithm, String password)
			throws NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException,
			BadPaddingException, InvalidParameterSpecException, IOException {
		// generate key pair

		final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
		keyGen.initialize(1024);

		final KeyPair keyPair = keyGen.generateKeyPair();

		// extract the encoded private key, this is an unencrypted PKCS#8
		// private key
		byte[] encodedprivkey = keyPair.getPrivate().getEncoded();

		// We must use a PasswordBasedEncryption algorithm in order to encrypt
		// the private key, you may use any common algorithm supported by
		// openssl, you can check them in the openssl documentation
		// http://www.openssl.org/docs/apps/pkcs8.html
		String MYPBEALG = "PBEWithSHA1AndDESede";

		int count = 20;// hash iteration count
		Random random = new Random();
		byte[] salt = new byte[8];
		random.nextBytes(salt);

		// Create PBE parameter set
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
		PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
		SecretKeyFactory keyFac = SecretKeyFactory.getInstance(MYPBEALG);
		SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

		Cipher pbeCipher = Cipher.getInstance(MYPBEALG);

		// Initialize PBE Cipher with key and parameters
		pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

		// Encrypt the encoded Private Key with the PBE key
		byte[] ciphertext = pbeCipher.doFinal(encodedprivkey);

		// Now construct PKCS #8 EncryptedPrivateKeyInfo object
		AlgorithmParameters algparms = AlgorithmParameters
				.getInstance(MYPBEALG);
		algparms.init(pbeParamSpec);
		EncryptedPrivateKeyInfo encinfo = new EncryptedPrivateKeyInfo(algparms,
				ciphertext);

		// and here we have it! a DER encoded PKCS#8 encrypted key!
		byte[] encryptedPkcs8 = encinfo.getEncoded();

		writeKeyToFile(keyPair.getPublic(), publicKeyFileName);

		writeKeyToFile(encryptedPkcs8, privateKeyFileName);
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

	/**
	 * writes a key into the filesystem
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void writeKeyToFile(Object key, String keyFileName)
			throws FileNotFoundException, IOException {
		// Saving the key in a file

		File keyFile = new File(keyFileName);
		// Create files to store public and private key
		if (keyFile.getParentFile() != null) {
			keyFile.getParentFile().mkdirs();
		}
		keyFile.createNewFile();

		ObjectOutputStream privateKeyOS = new ObjectOutputStream(
				new FileOutputStream(keyFile));
		privateKeyOS.writeObject(key);
		privateKeyOS.close();
	}

}
