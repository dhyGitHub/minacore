/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.statemachine.DecodingState;

public abstract class ShortIntegerDecodingState implements DecodingState {
	private int counter;

	public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		short highByte = 0;

		while (in.hasRemaining()) {
			switch (this.counter) {
			case 0:
				highByte = in.getUnsigned();
				++this.counter;
				break;
			case 1:
				this.counter = 0;
				return this.finishDecode((short) (highByte << 8 | in.getUnsigned()), out);
			default:
				throw new InternalError();
			}
		}

		return this;
	}

	public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
		throw new ProtocolDecoderException("Unexpected end of session while waiting for a short integer.");
	}

	protected abstract DecodingState finishDecode(short arg0, ProtocolDecoderOutput arg1) throws Exception;
}