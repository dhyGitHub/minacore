/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import org.apache.mina.filter.codec.ProtocolCodecException;

public class ProtocolDecoderException extends ProtocolCodecException {
	private static final long serialVersionUID = 3545799879533408565L;
	private String hexdump;

	public ProtocolDecoderException() {
	}

	public ProtocolDecoderException(String message) {
		super(message);
	}

	public ProtocolDecoderException(Throwable cause) {
		super(cause);
	}

	public ProtocolDecoderException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getMessage() {
		String message = super.getMessage();
		if (message == null) {
			message = "";
		}

		return this.hexdump != null ? message + (message.length() > 0 ? " " : "") + "(Hexdump: " + this.hexdump + ')'
				: message;
	}

	public String getHexdump() {
		return this.hexdump;
	}

	public void setHexdump(String hexdump) {
		if (this.hexdump != null) {
			throw new IllegalStateException("Hexdump cannot be set more than once.");
		} else {
			this.hexdump = hexdump;
		}
	}
}