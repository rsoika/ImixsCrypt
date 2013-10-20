package org.imixs.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
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
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.bind.DatatypeConverter;

/**
 * Generats differnt types of key pairs
 * 
 * 
 * 
 * 
 * 
 * Helpfull for open ssh
 * 
 * https://jsvnserve.googlecode.com/svn/trunk/src/main/java/com/googlecode/
 * jsvnserve/sshd/PublicKeyReaderUtil.java
 * 
 * 
 * http://stackoverflow.com/questions/19365940/convert-openssh-rsa-key-to-javax-
 * crypto-cipher-compatible-format
 * 
 * @author rsoika
 * 
 */
public class ImixsKeyGenerator {

	static String SECRET_KEY_ALGORYTHM = "PBEWithSHA1AndDESede";
	private static final int ITERATIONS = 1000;

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
	public static void generateKeyPair(String privateKeyFileName,
			String publicKeyFileName, String algorithm, String password)
			throws Exception {
		final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
		keyGen.initialize(1024);

		final KeyPair key = keyGen.generateKeyPair();

		writeKeyToFile(key.getPublic(), publicKeyFileName, null);

		writeKeyToFile(key.getPrivate(), privateKeyFileName, password);

	}

	/**
	 * Extracts a PrivateKey from a key file. The optional password is used to
	 * decrypt the key.
	 * 
	 * @param filename
	 * @param algorithm
	 * @return
	 * @throws Exception
	 */
	public static PrivateKey getPemPrivateKey(String filename,
			String algorithm, String password) throws Exception {
		File f = new File(filename);
		FileInputStream fis = new FileInputStream(f);
		DataInputStream dis = new DataInputStream(fis);
		byte[] keyBytes = new byte[(int) f.length()];
		dis.readFully(keyBytes);
		dis.close();

		String temp = new String(keyBytes);
		String privKeyPEM = temp.replace("-----BEGIN RSA PRIVATE KEY-----\n",
				"");
		privKeyPEM = privKeyPEM.replace("-----END RSA PRIVATE KEY-----", "");
		// System.out.println("Private key\n"+privKeyPEM);

		byte[] decoded = Base64Coder.decodeLines(privKeyPEM);
		
		/*
		 * Check if password is used to encrypt the key
		 */
		if (password != null && !password.isEmpty()) {
			// Here we actually encrypt the key
			decoded = passwordDecrypt(password.toCharArray(), decoded);
		}
		

		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
		KeyFactory kf = KeyFactory.getInstance(algorithm);
		PrivateKey key = kf.generatePrivate(spec);

		return key;
	}

	public static PublicKey getPemPublicKey(String filename, String algorithm)
			throws Exception {
		File f = new File(filename);
		FileInputStream fis = new FileInputStream(f);
		DataInputStream dis = new DataInputStream(fis);
		byte[] keyBytes = new byte[(int) f.length()];
		dis.readFully(keyBytes);
		dis.close();

		String temp = new String(keyBytes);
		String publicKeyPEM = temp.replace("-----BEGIN PUBLIC KEY-----\n", "");
		publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");

		// byte [] decoded = b64.decode(publicKeyPEM);
		byte[] decoded = Base64Coder.decodeLines(publicKeyPEM);

		X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
		KeyFactory kf = KeyFactory.getInstance(algorithm);
		PublicKey key = kf.generatePublic(spec);

		return key;
	}

	/**
	 * writes a key into the filesystem. using Base64 encoding
	 * 
	 * @throws Exception
	 */
	private static void writeKeyToFile(Key key, String keyFileName,
			String password) throws Exception {
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
			// Here we actually encrypt the key
			keyBytes = passwordEncrypt(password.toCharArray(), keyBytes);
		}

		String sEncodedKey = Base64Coder.encodeLines(keyBytes);

		System.out.println("Write Key : ");
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

}
