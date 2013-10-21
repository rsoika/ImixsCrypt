package org.imixs.crypt.server;

public class CryptSession {
	
	protected String password;

	private static CryptSession instance = null;

	protected CryptSession() {
		// Exists only to defeat instantiation.
	}

	public static CryptSession getInstance() {
		if (instance == null) {
			instance = new CryptSession();
		}
		return instance;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
	
}
