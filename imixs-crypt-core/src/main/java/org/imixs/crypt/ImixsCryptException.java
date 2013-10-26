
package org.imixs.crypt;

/**
 * ImixsCryptException is the wrapper exception for all keyUtil exceptions
 * 
 * @author rsoika
 * 
 */
public class ImixsCryptException extends Exception {

	private static final long serialVersionUID = 1L;

	public static String FILE_NOT_FOUND="FILE_NOT_FOUND";
	public static String NO_SUCH_ALGORITHM="NO_SUCH_ALGORITHM";
	public static String INVALID_KEY="INVALID_KEY";
	
	
	
	protected String errorCode = "UNDEFINED";

	public ImixsCryptException(String aErrorCode, String message) {
		super(message);
		errorCode = aErrorCode;

	}
	
	public ImixsCryptException(String aErrorCode, 	 Exception e) {
		super(aErrorCode, e);
		errorCode = aErrorCode;

	}



	public ImixsCryptException(String aErrorCode, String message, Exception e) {
		super(message, e);

		errorCode = aErrorCode;

	}


	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

}
