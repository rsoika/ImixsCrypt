package org.imixs.crypt.rest;

import java.util.logging.Logger;

import org.imixs.crypt.ImixsCryptException;
import org.imixs.crypt.util.Base64Coder;
import org.imixs.crypt.xml.MessageItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for CryptSession
 * 
 * @author rsoika
 * 
 */
public class CryptSessionTest {
	String PASSWORD = "abc";
	String IDENTITY = "id";
	String sessionId = null;
	private final static Logger logger = Logger.getLogger(CryptSessionTest.class
			.getName());

	/**
	 * Open Session
	 */
	@Before
	public void setup() {

		try {
			CryptSession.getInstance().openSession(IDENTITY, PASSWORD);
			CryptSession.getInstance().generateKeyPair();
			sessionId = CryptSession.getInstance().getSessionId();
		} catch (ImixsCryptException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		}

	}

	
	/**
	 * Test encrypt and decrypt a messageItem with a simple string
	 * 
	 * */
	@Test
	public void testSimpleEncryptDecryptMessage() {

		MessageItem message = new MessageItem();
		message.setSender(IDENTITY);
		message.setRecipient(IDENTITY);
		message.setMessage("TEST DATA");

		// encrypt
		MessageItem encryptedMessage;
		try {
			encryptedMessage = CryptSession.getInstance().ecrypt(message,
					sessionId);
			logger.info("Encrypted Message Text=" + encryptedMessage.getMessage());
			// decrypt
			MessageItem decryptedMessage = CryptSession.getInstance().decrypt(
					encryptedMessage, sessionId);

			logger.info("Decrypted Message Text=" + decryptedMessage.getMessage());
					
			Assert.assertEquals("TEST DATA", decryptedMessage.getMessage());
		} catch (ImixsCryptException e) {

			e.printStackTrace();
			Assert.fail();
		}
	}

	
	
	/**
	 * Test encrypt and decrypt a messageItem with a Base64 encoded message text
	 * 
	 * */
	@Test
	public void testEncryptDecryptMessageBase64() {

		MessageItem message = new MessageItem();
		message.setSender(IDENTITY);
		message.setRecipient(IDENTITY);
		message.setMessage(Base64Coder.encodeString("TEST DATA"));

		// encrypt
		MessageItem encryptedMessage;
		try {
			logger.info("Base64 Encoded Message=" + message.getMessage());
			encryptedMessage = CryptSession.getInstance().ecrypt(message,
					sessionId);
			logger.info("Encrypted Message=" + encryptedMessage.getMessage());
			// decrypt
			MessageItem decryptedMessage = CryptSession.getInstance().decrypt(
					encryptedMessage, sessionId);
			logger.info("Decrypted Message=" + decryptedMessage.getMessage());
					
			Assert.assertEquals(Base64Coder.encodeString("TEST DATA"), decryptedMessage.getMessage());
		} catch (ImixsCryptException e) {

			e.printStackTrace();
			Assert.fail();
		}
	}

}
