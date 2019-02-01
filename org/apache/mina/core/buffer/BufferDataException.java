/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.buffer;

public class BufferDataException extends RuntimeException {
	private static final long serialVersionUID = -4138189188602563502L;

	public BufferDataException() {
	}

	public BufferDataException(String message) {
		super(message);
	}

	public BufferDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public BufferDataException(Throwable cause) {
		super(cause);
	}
}