package org.imixs.crypt.json;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import org.imixs.crypt.xml.IdentityItem;
import org.imixs.crypt.xml.MessageItem;

/**
 * This class provides methods to write the Objects IdentityItem and MessageItem
 * into a JSON format to a file or read a json file and transform it back into
 * the corrsponding object.
 * 
 * @See http://www.oracle.com/technetwork/articles/java/json-1973242.html
 * 
 *      http://java.dzone.com/articles/java-api-json-processing-%E2%80%93
 * 
 * @author rsoika
 * 
 */
public class JSONWriter {

	public static void writeFile(IdentityItem identityItem, String filePath)
			throws IOException {

		File jsonFile = new File(filePath);

		// Create files to store public and private key
		if (jsonFile.getParentFile() != null) {
			jsonFile.getParentFile().mkdirs();
		}

		String json = identityItemToString(identityItem);

		FileOutputStream fop = null;
		fop = new FileOutputStream(jsonFile);

		// get the content in bytes
		byte[] contentInBytes = json.getBytes();

		fop.write(contentInBytes);
		fop.flush();
		fop.close();
	}

	public static void writeFile(MessageItem messageItem, String filePath)
			throws IOException {

		File jsonFile = new File(filePath);

		// Create files to store public and private key
		if (jsonFile.getParentFile() != null) {
			jsonFile.getParentFile().mkdirs();
		}

		String json = messageItemToString(messageItem);

		FileOutputStream fop = null;
		fop = new FileOutputStream(jsonFile);

		// get the content in bytes
		byte[] contentInBytes = json.getBytes();

		fop.write(contentInBytes);
		fop.flush();
		fop.close();

		fop.close();
	}

	public static MessageItem readMessageFromFile(String aFile) {
		MessageItem messageItem = new MessageItem();
		FileInputStream is;
		try {

			is = new FileInputStream(aFile);
			String jsonString = getStringFromInputStream(is);

			messageItem = stringToMessageItem(jsonString);

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

		return messageItem;
	}

	/**
	 * Converts a JSON String back into a MessageItem
	 * 
	 * @param jsonString
	 * @return MessageItem
	 */
	public static MessageItem stringToMessageItem(String jsonData) {
		MessageItem messageItem = new MessageItem();

		InputStream is = new ByteArrayInputStream(jsonData.getBytes());
		JsonReader rdr = Json.createReader(is);

		JsonObject jsonMessage = rdr.readObject();

		JsonString jsonString = null;
		if (jsonMessage.containsKey("recipient") && !jsonMessage.isNull("recipient")) {
			jsonString = jsonMessage.getJsonString("recipient");
			if (jsonString != null)
				messageItem.setRecipient(jsonString.getString());
		}

		if (jsonMessage.containsKey("sender") && !jsonMessage.isNull("sender")) {
			jsonString = jsonMessage.getJsonString("sender");
			if (jsonString != null)
				messageItem.setSender(jsonString.getString());
		}

		
		
		if (jsonMessage.containsKey("comment") && !jsonMessage.isNull("comment")) {
			jsonString = jsonMessage.getJsonString("comment");
			if (jsonString != null)
				messageItem.setComment(jsonString.getString());
		}

		if (jsonMessage.containsKey("message") && !jsonMessage.isNull("message")) {
			jsonString = jsonMessage.getJsonString("message");
			if (jsonString != null)
				messageItem.setMessage(jsonString.getString());
		}

		if (jsonMessage.containsKey("signature") && !jsonMessage.isNull("signature")) {
			jsonString = jsonMessage.getJsonString("signature");
			if (jsonString != null)
				messageItem.setSignature(jsonString.getString());
		}

		if (jsonMessage.containsKey("digest") && !jsonMessage.isNull("digest")) {
			jsonString = jsonMessage.getJsonString("digest");
			if (jsonString != null)
				messageItem.setDigest(jsonString.getString());
		}

		if (jsonMessage.containsKey("created") && !jsonMessage.isNull("created")
				&& jsonMessage.getInt("created") != 0) {
			
			long l=0;
			// try to get number
			try {
			l=jsonMessage.getInt("created");
			} catch (Exception e) {
				// no luck
			}
			if (l==0) {
				jsonString = jsonMessage.getJsonString("created");
				l=new Long(jsonString.getString());
			}
			if (l>0)
				messageItem.setCreated(l);
		}
		return messageItem;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String messageItemToString(MessageItem messageItem) {

		StringWriter writer = new StringWriter();
		Map map = new HashMap<>();
		map.put(JsonGenerator.PRETTY_PRINTING, true);

		JsonGeneratorFactory jgf = Json.createGeneratorFactory(map);
		JsonGenerator generator = jgf.createGenerator(writer);

		generator.writeStartObject();

		if (messageItem.getRecipient() != null)
			generator.write("recipient", messageItem.getRecipient());
		if (messageItem.getSender() != null)
			generator.write("sender", messageItem.getSender());
		if (messageItem.getComment() != null)
			generator.write("comment", messageItem.getComment());
		if (messageItem.getMessage() != null)
			generator.write("message", messageItem.getMessage());

		if (messageItem.getCreated() > 0)
			generator.write("created", messageItem.getCreated());
		if (messageItem.getSignature() != null)
			generator.write("signature", messageItem.getSignature());
		if (messageItem.getDigest() != null)
			generator.write("digest", messageItem.getDigest());
		generator.writeEnd();
		generator.close();

		return writer.toString();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String identityItemToString(IdentityItem identityItem) {
		StringWriter writer = new StringWriter();

		Map map = new HashMap<>();
		map.put(JsonGenerator.PRETTY_PRINTING, true);

		JsonGeneratorFactory jgf = Json.createGeneratorFactory(map);
		JsonGenerator generator = jgf.createGenerator(writer);

		// JsonGenerator generator = Json.createGenerator(writer);

		generator.writeStartObject();

		if (identityItem.getId() != null)
			generator.write("id", identityItem.getId());

		if (identityItem.getKey() != null)
			generator.write("key", identityItem.getKey());
		generator.writeEnd();
		generator.close();

		return writer.toString();
	}

	// convert InputStream to String
	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}

}
