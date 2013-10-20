package org.imixs.crypt.deprecated;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.util.io.pem.PemObject;

import sun.misc.BASE64Encoder;

/**
 * Generates rsa key pairs with boncy gard
 * 
 * 
 * http://www.mysamplecode.com/2011/08/rsa-encryption-decryption-using-bouncy.html
 * 
 * @author rsoika
 * 
 */
public class BouncyGenerator {

	
	
	public static void generate(String publicKeyFilename, String privateFilename) {

		try {

			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

			// Create the public and private keys
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA",
					"BC");
			BASE64Encoder b64 = new BASE64Encoder();

			SecureRandom random = createFixedRandom();
			generator.initialize(1024, random);

			KeyPair pair = generator.generateKeyPair();
			Key pubKey = pair.getPublic();
			Key privKey = pair.getPrivate();

			
			
			System.out
					.println("publicKey : " + b64.encode(pubKey.getEncoded()));
			System.out.println("privateKey : "
					+ b64.encode(privKey.getEncoded()));

			BufferedWriter out = new BufferedWriter(new FileWriter(
					publicKeyFilename));
			out.write(b64.encode(pubKey.getEncoded()));
			out.close();

			out = new BufferedWriter(new FileWriter(privateFilename));
			out.write(b64.encode(privKey.getEncoded()));
			out.close();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	public static SecureRandom createFixedRandom() {
		return new FixedRand();
	}

	private static class FixedRand extends SecureRandom {

		MessageDigest sha;
		byte[] state;

		FixedRand() {
			try {
				this.sha = MessageDigest.getInstance("SHA-1");
				this.state = sha.digest();
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("can't find SHA-1!");
			}
		}

		public void nextBytes(byte[] bytes) {

			int off = 0;

			sha.update(state);

			while (off < bytes.length) {
				state = sha.digest();

				if (bytes.length - off > state.length) {
					System.arraycopy(state, 0, bytes, off, state.length);
				} else {
					System.arraycopy(state, 0, bytes, off, bytes.length - off);
				}

				off += state.length;

				sha.update(state);
			}
		}
	}

}
