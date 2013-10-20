package org.imixs.crypt.deprecated;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.openssl.PEMWriter;

/**
 * Generats differnt types of key pairs
 * 
 * 
 * 
 * Using Bouncy Castle
 * 
 * http://en.wikipedia.org/wiki/Bouncy_Castle_(cryptography)
 * 
 * 
 * @author rsoika
 * 
 */
public class ImixsSecretKeyGenerator {

	
	static String MYPBEALG = "PBEWithSHA1AndDESede";
	//static String MYPBEALG = "PBEWithSHAAndTwofish-CBC";
	//static String MYPBEALG = "RSA/ECB/PKCS1PADDING";
	
	
	private static final String ENCRYPTED_FILENAME_SUFFIX = ".encrypted";
	private static final String DECRYPTED_FILENAME_SUFFIX = ".decrypted";
	private static final int ITERATIONS = 1000;
	private final static Logger logger = Logger.getLogger(ImixsSecretKeyGenerator.class

			.getName());

	
	
	



	/**
	 * Creates a 1024 bit RSA key and stores it to the filesystem as two files.
	 */
	public static void generateKeyPair(String privateKeyFilename,String publicKeyFilename
			,String ALGORITHM,String password) throws Exception {

		logger.info("Password to encrypt the private key: ");
		logger.info("Generating an RSA keypair...");

		// Create an RSA key
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
		keyPairGenerator.initialize(1024);
		KeyPair keyPair = keyPairGenerator.genKeyPair();

		logger.info("Done generating the keypair.\n");

		// Now we need to write the public key out to a file
		logger.info("Public key filename: "+publicKeyFilename);
	
		// Get the encoded form of the public key so we can
		// use it again in the future. This is X.509 by default.
		byte[] publicKeyBytes = keyPair.getPublic().getEncoded();

		// Write the encoded public key out to the filesystem
		FileOutputStream fos = new FileOutputStream(publicKeyFilename);
		fos.write(publicKeyBytes);
		fos.close();

		// Now we need to do the same thing with the private key,
		// but we need to password encrypt it as well.
		logger.info("Private key filename: "+privateKeyFilename);
		
		// Get the encoded form. This is PKCS#8 by default.
		byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();

		
		
		
		// Here we actually encrypt the private key
		byte[] encryptedPrivateKeyBytes = passwordEncrypt(
				password.toCharArray(), privateKeyBytes);

		fos = new FileOutputStream(privateKeyFilename);
		fos.write(encryptedPrivateKeyBytes);
		fos.close();
		
		
		
		// bouncy test:
		// Solution 1: using BouncyCastle's PEMWriter
		PEMWriter pemWriter = new PEMWriter(new PrintWriter(System.out));
		pemWriter.writeObject(keyPair.getPublic());
		pemWriter.flush();
		
		pemWriter = new PEMWriter(new PrintWriter(System.out));
		pemWriter.writeObject(keyPair.getPrivate());
		pemWriter.flush();
		
	}

	/**
	 * Encrypt the given file with a session key encrypted with an RSA public
	 * key which will be read in from the filesystem.
	 */
	public static void encrypt(String publicKeyFilename,String fileInput) throws Exception {

		logger.info("Public Key to encrypt with: "+publicKeyFilename);

		// Load the public key bytes
		FileInputStream fis = new FileInputStream(publicKeyFilename);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		int theByte = 0;
		while ((theByte = fis.read()) != -1) {
			baos.write(theByte);
		}
		fis.close();

		byte[] keyBytes = baos.toByteArray();
		baos.close();

		// Turn the encoded key into a real RSA public key.
		// Public keys are encoded in X.509.
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(keySpec);

		// Open up an output file for the output of the encryption
		String fileOutput = fileInput + ENCRYPTED_FILENAME_SUFFIX;
		DataOutputStream output = new DataOutputStream(new FileOutputStream(
				fileOutput));

		// Create a cipher using that key to initialize it
		Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);

		// Now create a new 256 bit Rijndael key to encrypt the file itself.
		// This will be the session key.
		KeyGenerator rijndaelKeyGenerator = KeyGenerator
				.getInstance("Rijndael");
		rijndaelKeyGenerator.init(256);
		logger.info("Generating session key...");
		Key rijndaelKey = rijndaelKeyGenerator.generateKey();
		logger.info("Done generating key.");

		// Encrypt the Rijndael key with the RSA cipher
		// and write it to the beginning of the file.
		byte[] encodedKeyBytes = rsaCipher.doFinal(rijndaelKey.getEncoded());
		output.writeInt(encodedKeyBytes.length);
		output.write(encodedKeyBytes);

		// Now we need an Initialization Vector for the symmetric cipher in CBC
		// mode
		SecureRandom random = new SecureRandom();
		byte[] iv = new byte[16];
		random.nextBytes(iv);

		// Write the IV out to the file.
		output.write(iv);
		IvParameterSpec spec = new IvParameterSpec(iv);

		// Create the cipher for encrypting the file itself.
		Cipher symmetricCipher = Cipher
				.getInstance("Rijndael/CBC/PKCS5Padding");
		symmetricCipher.init(Cipher.ENCRYPT_MODE, rijndaelKey, spec);

		CipherOutputStream cos = new CipherOutputStream(output, symmetricCipher);

		logger.info("Encrypting the file...");

		FileInputStream input = new FileInputStream(fileInput);

		theByte = 0;
		while ((theByte = input.read()) != -1) {
			cos.write(theByte);
		}
		input.close();
		cos.close();
		logger.info("File encrypted.");
		return;
	}

	
	
	
	
	
	
	
	/**
	 * Decrypt the given file. Start by getting the RSA private key and
	 * decrypting the session key embedded in the file. Then decrypt the file
	 * with that session key.
	 */
	public static void decrypt(String privateKeyFilename,String password,String fileInput) throws Exception {

		logger.info("Private Key to decrypt with: "+privateKeyFilename);
		
		logger.info("Password for the private key: "+password);
	
		// Load the private key bytes
		FileInputStream fis = new FileInputStream(privateKeyFilename);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		int theByte = 0;
		while ((theByte = fis.read()) != -1) {
			baos.write(theByte);
		}
		fis.close();

		byte[] keyBytes = baos.toByteArray();
		baos.close();

		keyBytes = passwordDecrypt(password.toCharArray(), keyBytes);

		// Turn the encoded key into a real RSA private key.
		// Private keys are encoded in PKCS#8.
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

		// Create a cipher using that key to initialize it
		Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

		// Read in the encrypted bytes of the session key
		DataInputStream dis = new DataInputStream(
				new FileInputStream(fileInput));
		byte[] encryptedKeyBytes = new byte[dis.readInt()];
		dis.readFully(encryptedKeyBytes);

		// Decrypt the session key bytes.
		rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] rijndaelKeyBytes = rsaCipher.doFinal(encryptedKeyBytes);

		// Transform the key bytes into an actual key.
		SecretKey rijndaelKey = new SecretKeySpec(rijndaelKeyBytes, "Rijndael");

		// Read in the Initialization Vector from the file.
		byte[] iv = new byte[16];
		dis.read(iv);
		IvParameterSpec spec = new IvParameterSpec(iv);

		Cipher cipher = Cipher.getInstance("Rijndael/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, rijndaelKey, spec);
		CipherInputStream cis = new CipherInputStream(dis, cipher);

		logger.info("Decrypting the file...");
		FileOutputStream fos = new FileOutputStream(fileInput
				+ DECRYPTED_FILENAME_SUFFIX);

		// Read through the file, decrypting each byte.
		theByte = 0;
		while ((theByte = cis.read()) != -1) {
			fos.write(theByte);
		}
		cis.close();
		fos.close();
		logger.info("Done.");
		return;
	}

	/**
	 * Utility method to encrypt a byte array with a given password. Salt will
	 * be the first 8 bytes of the byte array returned.
	 */
	private static byte[] passwordEncrypt(char[] password, byte[] plaintext)
			throws Exception {

		// Create the salt.
		byte[] salt = new byte[8];
		Random random = new Random();
		random.nextBytes(salt);

		// Create a PBE key and cipher.
		PBEKeySpec keySpec = new PBEKeySpec(password);
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance(MYPBEALG); 
		SecretKey key = keyFactory.generateSecret(keySpec);
		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, ITERATIONS);
		Cipher cipher = Cipher.getInstance(MYPBEALG);
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
	 */
	private static byte[] passwordDecrypt(char[] password, byte[] ciphertext)
			throws Exception {

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
				.getInstance(MYPBEALG);
		SecretKey key = keyFactory.generateSecret(keySpec);
		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, ITERATIONS);
		Cipher cipher = Cipher.getInstance(MYPBEALG);

		// Perform the actual decryption.
		cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		return cipher.doFinal(remainingCiphertext);
	}
}
