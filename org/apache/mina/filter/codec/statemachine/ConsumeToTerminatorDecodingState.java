/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.statemachine.DecodingState;

public abstract class ConsumeToTerminatorDecodingState implements DecodingState {
	private final byte terminator;
	private IoBuffer buffer;

	public ConsumeToTerminatorDecodingState(byte terminator) {
		this.terminator = terminator;
	}

	public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		int terminatorPos = in.indexOf(this.terminator);
		if (terminatorPos >= 0) {
			int limit = in.limit();
			IoBuffer product;
			if (in.position() < terminatorPos) {
				in.limit(terminatorPos);
				if (this.buffer == null) {
					product = in.slice();
				} else {
					this.buffer.put(in);
					product = this.buffer.flip();
					this.buffer = null;
				}

				in.limit(limit);
			} else if (this.buffer == null) {
				product = IoBuffer.allocate(0);
			} else {
				product = this.buffer.flip();
				this.buffer = null;
			}

			in.position(terminatorPos + 1);
			return this.finishDecode(product, out);
		} else {
			if (this.buffer == null) {
				this.buffer = IoBuffer.allocate(in.remaining());
				this.buffer.setAutoExpand(true);
			}

			this.buffer.put(in);
			return this;
		}
	}

	public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
		IoBuffer product;
		if (this.buffer == null) {
			product = IoBuffer.allocate(0);
		} else {
			product = this.buffer.flip();
			this.buffer = null;
		}

		return this.finishDecode(product, out);
	}

	protected abstract DecodingState finishDecode(IoBuffer arg0, ProtocolDecoderOutput arg1) throws Exception;
}