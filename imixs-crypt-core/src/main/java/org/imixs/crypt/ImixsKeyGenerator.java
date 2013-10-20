package org.imixs.crypt;

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
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

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

		writeKeyToFile(key.getPublic(), publicKeyFileName);

		writeKeyToFile(key.getPrivate(), privateKeyFileName);

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

	public static PrivateKey getPemPrivateKey(String filename, String algorithm)
			throws Exception {
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

	public static PrivateKey oldgetPrivatKey(String PRIVATE_KEY_FILE)
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
	 * writes a key into the filesystem. using Base64 encoding
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void writeKeyToFile(Key key, String keyFileName)
			throws FileNotFoundException, IOException {
		// Saving the key in a file

		File keyFile = new File(keyFileName);
		// Create files to store public and private key
		if (keyFile.getParentFile() != null) {
			keyFile.getParentFile().mkdirs();
		}
		keyFile.createNewFile();

		String sEncodedKey = Base64Coder.encodeLines(key.getEncoded());

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

	public static KeyPair demo(InputStream pub, InputStream pvt) throws IOException,
			GeneralSecurityException {
		KeyFactory f = KeyFactory.getInstance("RSA");

		RSAPublicKeySpec pubspec = decodeRSAPublicSSH(readAllBase64Bytes(pub));
		RSAPrivateCrtKeySpec pvtspec = decodeRSAPrivatePKCS1(readAllBase64Bytes(pvt));

		return new KeyPair(f.generatePublic(pubspec),
				f.generatePrivate(pvtspec));
	}

	static RSAPublicKeySpec decodeRSAPublicSSH(byte[] encoded) {
		ByteBuffer input = ByteBuffer.wrap(encoded);
		String type = string(input);
		if (!"ssh-rsa".equals(type))
			throw new IllegalArgumentException("Unsupported type");
		BigInteger exp = sshint(input);
		BigInteger mod = sshint(input);
		if (input.hasRemaining())
			throw new IllegalArgumentException("Excess data");
		return new RSAPublicKeySpec(mod, exp);
	}

	static RSAPrivateCrtKeySpec decodeRSAPrivatePKCS1(byte[] encoded) {
		ByteBuffer input = ByteBuffer.wrap(encoded);
		if (der(input, 0x30) != input.remaining())
			throw new IllegalArgumentException("Excess data");
		if (!BigInteger.ZERO.equals(derint(input)))
			throw new IllegalArgumentException("Unsupported version");
		BigInteger n = derint(input);
		BigInteger e = derint(input);
		BigInteger d = derint(input);
		BigInteger p = derint(input);
		BigInteger q = derint(input);
		BigInteger ep = derint(input);
		BigInteger eq = derint(input);
		BigInteger c = derint(input);
		return new RSAPrivateCrtKeySpec(n, e, d, p, q, ep, eq, c);
	}

	private static String string(ByteBuffer buf) {
		return new String(lenval(buf), Charset.forName("US-ASCII"));
	}

	private static BigInteger sshint(ByteBuffer buf) {
		return new BigInteger(+1, lenval(buf));
	}

	private static byte[] lenval(ByteBuffer buf) {
		int len = buf.getInt();
		byte[] copy = new byte[len];
		buf.get(copy);
		return copy;
	}

	private static BigInteger derint(ByteBuffer input) {
		byte[] value = new byte[der(input, 0x02)];
		input.get(value);
		return new BigInteger(+1, value);
	}

	private static int der(ByteBuffer input, int exp) {
		int tag = input.get() & 0xFF;
		if (tag != exp)
			throw new IllegalArgumentException("Unexpected tag");
		int n = input.get() & 0xFF;
		if (n < 128)
			return n;
		n &= 0x7F;
		if ((n < 1) || (n > 2))
			throw new IllegalArgumentException("Invalid length");
		int len = 0;
		while (n-- > 0) {
			len <<= 8;
			len |= input.get() & 0xFF;
		}
		return len;
	}

	private static byte[] readAllBase64Bytes(InputStream input) {
		StringBuilder buf = new StringBuilder();
		Scanner scanner = new Scanner(input, "US-ASCII");
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();
			if (!line.startsWith("-----"))
				buf.append(line);
		}
		return DatatypeConverter.parseBase64Binary(buf.toString());
	}

}
