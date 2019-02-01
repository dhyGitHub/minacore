/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.session;

public class IoSessionInitializationException extends RuntimeException {
	private static final long serialVersionUID = -1205810145763696189L;

	public IoSessionInitializationException() {
	}

	public IoSessionInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public IoSessionInitializationException(String message) {
		super(message);
	}

	public IoSessionInitializationException(Throwable cause) {
		super(cause);
	}
}