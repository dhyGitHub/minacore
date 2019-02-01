/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core;

public class RuntimeIoException extends RuntimeException {
	private static final long serialVersionUID = 9029092241311939548L;

	public RuntimeIoException() {
	}

	public RuntimeIoException(String message) {
		super(message);
	}

	public RuntimeIoException(String message, Throwable cause) {
		super(message, cause);
	}

	public RuntimeIoException(Throwable cause) {
		super(cause);
	}
}