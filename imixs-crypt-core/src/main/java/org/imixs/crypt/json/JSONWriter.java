package org.imixs.crypt.json;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

	public static void writeIdentityItem(IdentityItem identityItem,String filePath)
			throws IOException {
		
		
		File jsonFile = new File(filePath);
		
		// Create files to store public and private key
		if (jsonFile.getParentFile() != null) {
			jsonFile.getParentFile().mkdirs();
		}
		
		FileOutputStream fop = null;
		fop = new FileOutputStream(jsonFile);

		JsonGenerator generator = Json.createGenerator(fop);

		generator.writeStartObject().write("id", identityItem.getId())
				.write("key", identityItem.getKey()).writeEnd();
		generator.close();

		fop.close();
	}
	
	
	
	
	
	
	
	
	public static void writeMessageItem(MessageItem messageItem,String filePath)
			throws IOException {
		
		
		File jsonFile = new File(filePath);
		
		// Create files to store public and private key
		if (jsonFile.getParentFile() != null) {
			jsonFile.getParentFile().mkdirs();
		}
		
		FileOutputStream fop = null;
		fop = new FileOutputStream(jsonFile);

		JsonGenerator generator = Json.createGenerator(fop);

		generator.writeStartObject()
				
				.write("recipient", messageItem.getRecipient())
				.write("sender", messageItem.getSender())
				.write("comment", messageItem.getComment())
				.write("message", messageItem.getMessage())
				.write("created", messageItem.getCreated())
				.write("signature", messageItem.getSignature())
				.write("digest", messageItem.getDigest())
				.writeEnd();
		generator.close();

		fop.close();
	}
}
