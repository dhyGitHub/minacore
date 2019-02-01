/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.statemachine.DecodingState;

public abstract class ConsumeToEndOfSessionDecodingState implements DecodingState {
	private IoBuffer buffer;
	private final int maxLength;

	public ConsumeToEndOfSessionDecodingState(int maxLength) {
		this.maxLength = maxLength;
	}

	public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (this.buffer == null) {
			this.buffer = IoBuffer.allocate(256).setAutoExpand(true);
		}

		if (this.buffer.position() + in.remaining() > this.maxLength) {
			throw new ProtocolDecoderException("Received data exceeds " + this.maxLength + " byte(s).");
		} else {
			this.buffer.put(in);
			return this;
		}
	}

	public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
		DecodingState arg1;
		try {
			if (this.buffer == null) {
				this.buffer = IoBuffer.allocate(0);
			}

			this.buffer.flip();
			arg1 = this.finishDecode(this.buffer, out);
		} finally {
			this.buffer = null;
		}

		return arg1;
	}

	protected abstract DecodingState finishDecode(IoBuffer arg0, ProtocolDecoderOutput arg1) throws Exception;
}