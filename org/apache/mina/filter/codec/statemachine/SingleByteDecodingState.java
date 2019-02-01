/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.statemachine.DecodingState;

public abstract class SingleByteDecodingState implements DecodingState {
	public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		return (DecodingState) (in.hasRemaining() ? this.finishDecode(in.get(), out) : this);
	}

	public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
		throw new ProtocolDecoderException("Unexpected end of session while waiting for a single byte.");
	}

	protected abstract DecodingState finishDecode(byte arg0, ProtocolDecoderOutput arg1) throws Exception;
}