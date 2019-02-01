/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import org.apache.mina.filter.codec.ProtocolCodecException;

public class ProtocolEncoderException extends ProtocolCodecException {
	private static final long serialVersionUID = 8752989973624459604L;

	public ProtocolEncoderException() {
	}

	public ProtocolEncoderException(String message) {
		super(message);
	}

	public ProtocolEncoderException(Throwable cause) {
		super(cause);
	}

	public ProtocolEncoderException(String message, Throwable cause) {
		super(message, cause);
	}
}