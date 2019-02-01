/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import org.apache.mina.filter.codec.ProtocolDecoderException;

public class RecoverableProtocolDecoderException extends ProtocolDecoderException {
	private static final long serialVersionUID = -8172624045024880678L;

	public RecoverableProtocolDecoderException() {
	}

	public RecoverableProtocolDecoderException(String message) {
		super(message);
	}

	public RecoverableProtocolDecoderException(Throwable cause) {
		super(cause);
	}

	public RecoverableProtocolDecoderException(String message, Throwable cause) {
		super(message, cause);
	}
}