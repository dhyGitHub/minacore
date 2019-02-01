/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.statemachine.DecodingState;

public abstract class FixedLengthDecodingState implements DecodingState {
	private final int length;
	private IoBuffer buffer;

	public FixedLengthDecodingState(int length) {
		this.length = length;
	}

	public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		int limit;
		IoBuffer product;
		if (this.buffer == null) {
			if (in.remaining() >= this.length) {
				limit = in.limit();
				in.limit(in.position() + this.length);
				product = in.slice();
				in.position(in.position() + this.length);
				in.limit(limit);
				return this.finishDecode(product, out);
			} else {
				this.buffer = IoBuffer.allocate(this.length);
				this.buffer.put(in);
				return this;
			}
		} else if (in.remaining() >= this.length - this.buffer.position()) {
			limit = in.limit();
			in.limit(in.position() + this.length - this.buffer.position());
			this.buffer.put(in);
			in.limit(limit);
			product = this.buffer;
			this.buffer = null;
			return this.finishDecode(product.flip(), out);
		} else {
			this.buffer.put(in);
			return this;
		}
	}

	public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
		IoBuffer readData;
		if (this.buffer == null) {
			readData = IoBuffer.allocate(0);
		} else {
			readData = this.buffer.flip();
			this.buffer = null;
		}

		return this.finishDecode(readData, out);
	}

	protected abstract DecodingState finishDecode(IoBuffer arg0, ProtocolDecoderOutput arg1) throws Exception;
}