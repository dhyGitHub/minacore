/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.keepalive;

public class KeepAliveRequestTimeoutException extends RuntimeException {
	private static final long serialVersionUID = -1985092764656546558L;

	public KeepAliveRequestTimeoutException() {
	}

	public KeepAliveRequestTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public KeepAliveRequestTimeoutException(String message) {
		super(message);
	}

	public KeepAliveRequestTimeoutException(Throwable cause) {
		super(cause);
	}
}