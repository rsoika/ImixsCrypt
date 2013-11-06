package org.imixs.crypt.json;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;

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

		String json = toString(identityItem);

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

		String json = toString(messageItem);

		FileOutputStream fop = null;
		fop = new FileOutputStream(jsonFile);

		// get the content in bytes
		byte[] contentInBytes = json.getBytes();

		fop.write(contentInBytes);
		fop.flush();
		fop.close();

		fop.close();
	}

	public static String toString(MessageItem messageItem) {

		StringWriter writer = new StringWriter();
		JsonGenerator generator = Json.createGenerator(writer);

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

	public static String toString(IdentityItem identityItem) {
		StringWriter writer = new StringWriter();
		JsonGenerator generator = Json.createGenerator(writer);

		generator.writeStartObject();

		if (identityItem.getId() != null)
			generator.write("id", identityItem.getId());

		if (identityItem.getKey() != null)
			generator.write("key", identityItem.getKey());
		generator.writeEnd();
		generator.close();

		return writer.toString();
	}

}
