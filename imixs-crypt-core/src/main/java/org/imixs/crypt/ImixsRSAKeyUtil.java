package org.imixs.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * This class implements the imixsCrypt KeyUitl interface and provides methods
 * to generate RSA Key pairs and encrypt or decrypt messages.
 * 
 * The class provides methods to write a key pair into the filesystem and load
 * the public and private key form a file.
 * 
 * 
 * @see Encrypting and decrypting large data using Java and RSA
 *      http://coding.westreicher.org/?p=23
 * 
 * @author rsoika
 * 
 */
public class ImixsRSAKeyUtil implements ImixsCryptKeyUtil {

	private static final String ALGORITHM = "RSA";
	private static final String SECRET_KEY_ALGORYTHM = "PBEWithSHA1AndDESede";
	private static final int ITERATIONS = 1000;

	/**
	 * Default Constructor
	 */
	public ImixsRSAKeyUtil() {
		super();
	}

	/**
	 * generates a key pair and stores it in the filesystem. An optional
	 * password will encrypt the private key
	 * 
	 * @param privateKeyFile
	 * @param publicKeyFile
	 * @param algorithm
	 * @param password
	 *            optional password to encrypt the private key
	 * @throws Exception
	 */
	public void generateKeyPair(String privateKeyFileName,
			String publicKeyFileName, String password)
			throws ImixsCryptException {
		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance(ALGORITHM);
			keyGen.initialize(1024);

			final KeyPair key = keyGen.generateKeyPair();

			writeKeyToFile(key.getPublic(), publicKeyFileName, null);

			writeKeyToFile(key.getPrivate(), privateKeyFileName, password);
		} catch (NoSuchAlgorithmException e) {
			throw new ImixsCryptException(
					ImixsCryptException.NO_SUCH_ALGORITHM, e);

		} catch (InvalidKeyException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (InvalidKeySpecException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (NoSuchPaddingException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (IllegalBlockSizeException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (BadPaddingException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (IOException e) {
			throw new ImixsCryptException(ImixsCryptException.FILE_NOT_FOUND, e);
		}

	}

	/**
	 * Extracts a PrivateKey from a key file. The optional password is used to
	 * decrypt the key.
	 * 
	 * @param filename
	 * @param algorithm
	 * @return
	 * @throws ImixsCryptException
	 * @throws
	 */
	public PrivateKey getPrivateKey(String filename, String password)
			throws ImixsCryptException {
		PrivateKey privateKey = null;
		File f = new File(filename);
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);

			DataInputStream dis = new DataInputStream(fis);
			byte[] keyBytes = new byte[(int) f.length()];
			dis.readFully(keyBytes);
			dis.close();

			String temp = new String(keyBytes);
			String privKeyPEM = temp.replace(
					"-----BEGIN RSA PRIVATE KEY-----\n", "");
			privKeyPEM = privKeyPEM
					.replace("-----END RSA PRIVATE KEY-----", "");
			// System.out.println("Private key\n"+privKeyPEM);

			byte[] decoded = Base64Coder.decode(privKeyPEM);

			/*
			 * Check if password is used to encrypt the key
			 */
			if (password != null && !password.isEmpty()) {
				// Here we actually encrypt the key
				decoded = passwordDecrypt(password.toCharArray(), decoded);
			}

			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
			KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
			privateKey = kf.generatePrivate(spec);
		} catch (FileNotFoundException e) {
			throw new ImixsCryptException(ImixsCryptException.FILE_NOT_FOUND, e);
		} catch (InvalidKeySpecException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (InvalidKeyException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (NoSuchAlgorithmException e) {
			throw new ImixsCryptException(
					ImixsCryptException.NO_SUCH_ALGORITHM, e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (IllegalBlockSizeException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (BadPaddingException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (NoSuchPaddingException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (IOException e) {
			throw new ImixsCryptException(ImixsCryptException.FILE_NOT_FOUND, e);
		}
		return privateKey;
	}

	public PublicKey getPublicKey(String filename) throws ImixsCryptException {
		PublicKey key = null;
		File f = new File(filename);
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);

			DataInputStream dis = new DataInputStream(fis);
			byte[] keyBytes = new byte[(int) f.length()];
			dis.readFully(keyBytes);
			dis.close();

			String temp = new String(keyBytes);
			String publicKeyPEM = temp.replace("-----BEGIN PUBLIC KEY-----\n",
					"");
			publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");

			// byte [] decoded = b64.decode(publicKeyPEM);
			byte[] decoded = Base64Coder.decode(publicKeyPEM);

			X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
			KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
			key = kf.generatePublic(spec);

		} catch (FileNotFoundException e) {
			throw new ImixsCryptException(ImixsCryptException.FILE_NOT_FOUND, e);

		} catch (NoSuchAlgorithmException e) {
			throw new ImixsCryptException(
					ImixsCryptException.NO_SUCH_ALGORITHM, e);
		} catch (InvalidKeySpecException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);

		} catch (IOException e) {
			throw new ImixsCryptException(ImixsCryptException.FILE_NOT_FOUND, e);
		}
		return key;
	}

	/**
	 * Encrypt the plain text using public key.
	 * 
	 * @param text
	 *            : original plain text
	 * @param key
	 *            :The public key
	 * @return Encrypted text
	 * @throws ImixsCryptException
	 */
	public byte[] encrypt(byte[] data, PublicKey key)
			throws ImixsCryptException {
		byte[] cipherText = null;

		// get an RSA cipher object and print the provider
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			// encrypt the plain text using the public key
			cipher.init(Cipher.ENCRYPT_MODE, key);

			// to decrypt text longer the 117 bytes we can not use the simple
			// cipher.
			cipherText = blockCipher(cipher, data, Cipher.ENCRYPT_MODE);

		} catch (NoSuchAlgorithmException e) {
			throw new ImixsCryptException(
					ImixsCryptException.NO_SUCH_ALGORITHM, e);
		} catch (NoSuchPaddingException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (InvalidKeyException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (IllegalBlockSizeException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (BadPaddingException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		}

		return cipherText;
	}

	/**
	 * Decrypt text using private key.
	 * 
	 * @param text
	 *            :encrypted text
	 * @param key
	 *            :The private key
	 * @return plain text
	 * @throws ImixsCryptException
	 */
	public byte[] decrypt(byte[] encryptedData, PrivateKey key)
			throws ImixsCryptException {
		byte[] dectyptedText = null;

		// get an RSA cipher object and print the provider
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			// decrypt the text using the private key
			cipher.init(Cipher.DECRYPT_MODE, key);

			// to decrypt text longer the 117 bytes we can not use the simple
			// cipher.
			dectyptedText = blockCipher(cipher, encryptedData, Cipher.DECRYPT_MODE);

		} catch (NoSuchAlgorithmException e) {
			throw new ImixsCryptException(
					ImixsCryptException.NO_SUCH_ALGORITHM, e);
		} catch (NoSuchPaddingException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (InvalidKeyException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (IllegalBlockSizeException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		} catch (BadPaddingException e) {
			throw new ImixsCryptException(ImixsCryptException.INVALID_KEY, e);
		}

		return dectyptedText;
	}

	/**
	 * Delete key pair
	 * 
	 * @param privateKeyFileName
	 * @param publicKeyFileName
	 */
	public static void deleteKeyPair(String privateKeyFileName,
			String publicKeyFileName) {
		File keyFile = null;

		if (privateKeyFileName != null && !privateKeyFileName.isEmpty()) {
			keyFile = new File(privateKeyFileName);
			if (keyFile.exists())
				keyFile.delete();
		}
		if (publicKeyFileName != null && !publicKeyFileName.isEmpty()) {
			keyFile = new File(publicKeyFileName);
			if (keyFile.exists())
				keyFile.delete();
		}
	}

	/**
	 * writes a key into the filesystem. using Base64 encoding
	 * 
	 * @throws IOException
	 * @throws InvalidAlgorithmParameterException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * 
	 * @throws Exception
	 */
	private static void writeKeyToFile(Key key, String keyFileName,
			String password) throws IOException, InvalidKeyException,
			NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException {
		// Saving the key in a file

		File keyFile = new File(keyFileName);
		// Create files to store public and private key
		if (keyFile.getParentFile() != null) {
			keyFile.getParentFile().mkdirs();
		}
		keyFile.createNewFile();

		/*
		 * Check if password is used to encrypt the key
		 */
		byte[] keyBytes = key.getEncoded();
		if (password != null && !password.isEmpty()) {
			System.out.println("Encrypting Key with password....");
			// Here we actually encrypt the key
			keyBytes = passwordEncrypt(password.toCharArray(), keyBytes);
		}

		char[] encodedCharArray = Base64Coder.encode(keyBytes);
		String sEncodedKey = new String(encodedCharArray);

		System.out.println("Write KeyFile: " + keyFileName);
		System.out.println("");
		System.out.println(sEncodedKey);

		PrintStream out = null;
		try {
			out = new PrintStream(new FileOutputStream(keyFile));
			out.print(sEncodedKey);
		} finally {
			if (out != null)
				out.close();
		}

	}

	/**
	 * Utility method to encrypt a byte array with a given password. Salt will
	 * be the first 8 bytes of the byte array returned.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchPaddingException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 * @throws IOException
	 */
	private static byte[] passwordEncrypt(char[] password, byte[] plaintext)
			throws NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IOException {

		// Create the salt.
		byte[] salt = new byte[8];
		Random random = new Random();
		random.nextBytes(salt);

		// Create a PBE key and cipher.
		PBEKeySpec keySpec = new PBEKeySpec(password);
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance(SECRET_KEY_ALGORYTHM);
		SecretKey key = keyFactory.generateSecret(keySpec);
		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, ITERATIONS);
		Cipher cipher = Cipher.getInstance(SECRET_KEY_ALGORYTHM);
		cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);

		// Encrypt the array
		byte[] ciphertext = cipher.doFinal(plaintext);

		// Write out the salt, then the ciphertext and return it.
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(salt);
		baos.write(ciphertext);
		return baos.toByteArray();
	}

	/**
	 * Utility method to decrypt a byte array with a given password. Salt will
	 * be the first 8 bytes in the array passed in.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchPaddingException
	 */
	private static byte[] passwordDecrypt(char[] password, byte[] ciphertext)
			throws NoSuchAlgorithmException, InvalidKeySpecException,
			InvalidKeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException,
			NoSuchPaddingException {

		// Read in the salt.
		byte[] salt = new byte[8];
		ByteArrayInputStream bais = new ByteArrayInputStream(ciphertext);
		bais.read(salt, 0, 8);

		// The remaining bytes are the actual ciphertext.
		byte[] remainingCiphertext = new byte[ciphertext.length - 8];
		bais.read(remainingCiphertext, 0, ciphertext.length - 8);

		// Create a PBE cipher to decrypt the byte array.
		PBEKeySpec keySpec = new PBEKeySpec(password);
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance(SECRET_KEY_ALGORYTHM);
		SecretKey key = keyFactory.generateSecret(keySpec);
		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, ITERATIONS);
		Cipher cipher = Cipher.getInstance(SECRET_KEY_ALGORYTHM);

		// Perform the actual decryption.
		cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		return cipher.doFinal(remainingCiphertext);
	}

	/**
	 * From http://coding.westreicher.org/?p=23
	 * 
	 * @param bytes
	 * @param mode
	 * @return
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	private byte[] blockCipher(Cipher cipher, byte[] bytes, int mode)
			throws IllegalBlockSizeException, BadPaddingException {

		// if we encrypt we use 100 byte long blocks. Decryption requires 128
		// byte long blocks (because of RSA)
		int length = (mode == Cipher.ENCRYPT_MODE) ? 100 : 128;

		// test if block size is not grater then length....
		if (bytes.length < length) {
			// use simple block cipher....
			return cipher.doFinal(bytes);
		}

		// encrypt and decrypt in multiple blocks...

		// string initialize 2 buffers.
		// scrambled will hold intermediate results
		byte[] scrambled = new byte[0];

		// toReturn will hold the total result
		byte[] toReturn = new byte[0];

		// another buffer. this one will hold the bytes that have to be modified
		// in this step
		byte[] buffer = new byte[length];

		for (int i = 0; i < bytes.length; i++) {

			// if we filled our buffer array we have our block ready for de- or
			// encryption
			if ((i > 0) && (i % length == 0)) {
				// execute the operation
				scrambled = cipher.doFinal(buffer);
				// add the result to our total result.
				toReturn = append(toReturn, scrambled);
				// here we calculate the length of the next buffer required
				int newlength = length;

				// if newlength would be longer than remaining bytes in the
				// bytes array we shorten it.
				if (i + length > bytes.length) {
					newlength = bytes.length - i;
				}
				// clean the buffer array
				buffer = new byte[newlength];
			}
			// copy byte into our buffer.
			buffer[i % length] = bytes[i];
		}

		// this step is needed if we had a trailing buffer. should only happen
		// when encrypting.
		// example: we encrypt 110 bytes. 100 bytes per run means we "forgot"
		// the last 10 bytes. they are in the buffer array
		scrambled = cipher.doFinal(buffer);

		// final step before we can return the modified data.
		toReturn = append(toReturn, scrambled);

		return toReturn;
	}

	private byte[] append(byte[] prefix, byte[] suffix) {
		byte[] toReturn = new byte[prefix.length + suffix.length];
		for (int i = 0; i < prefix.length; i++) {
			toReturn[i] = prefix[i];
		}
		for (int i = 0; i < suffix.length; i++) {
			toReturn[i + prefix.length] = suffix[i];
		}
		return toReturn;
	}

}
