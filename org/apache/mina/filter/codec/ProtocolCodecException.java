/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

public class ProtocolCodecException extends Exception {
	private static final long serialVersionUID = 5939878548186330695L;

	public ProtocolCodecException() {
	}

	public ProtocolCodecException(String message) {
		super(message);
	}

	public ProtocolCodecException(Throwable cause) {
		super(cause);
	}

	public ProtocolCodecException(String message, Throwable cause) {
		super(message, cause);
	}
}