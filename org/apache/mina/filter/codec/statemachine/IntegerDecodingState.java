/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.statemachine.DecodingState;

public abstract class IntegerDecodingState implements DecodingState {
	private int counter;

	public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		short firstByte = 0;
		short secondByte = 0;

		for (short thirdByte = 0; in.hasRemaining(); ++this.counter) {
			switch (this.counter) {
			case 0:
				firstByte = in.getUnsigned();
				break;
			case 1:
				secondByte = in.getUnsigned();
				break;
			case 2:
				thirdByte = in.getUnsigned();
				break;
			case 3:
				this.counter = 0;
				return this.finishDecode(firstByte << 24 | secondByte << 16 | thirdByte << 8 | in.getUnsigned(), out);
			default:
				throw new InternalError();
			}
		}

		return this;
	}

	public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
		throw new ProtocolDecoderException("Unexpected end of session while waiting for an integer.");
	}

	protected abstract DecodingState finishDecode(int arg0, ProtocolDecoderOutput arg1) throws Exception;
}