/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.statemachine.DecodingState;

public abstract class ConsumeToDynamicTerminatorDecodingState implements DecodingState {
	private IoBuffer buffer;

	public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		int beginPos = in.position();
		int terminatorPos = -1;
		int limit = in.limit();

		for (int product = beginPos; product < limit; ++product) {
			byte b = in.get(product);
			if (this.isTerminator(b)) {
				terminatorPos = product;
				break;
			}
		}

		if (terminatorPos >= 0) {
			IoBuffer arg7;
			if (beginPos < terminatorPos) {
				in.limit(terminatorPos);
				if (this.buffer == null) {
					arg7 = in.slice();
				} else {
					this.buffer.put(in);
					arg7 = this.buffer.flip();
					this.buffer = null;
				}

				in.limit(limit);
			} else if (this.buffer == null) {
				arg7 = IoBuffer.allocate(0);
			} else {
				arg7 = this.buffer.flip();
				this.buffer = null;
			}

			in.position(terminatorPos + 1);
			return this.finishDecode(arg7, out);
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

	protected abstract boolean isTerminator(byte arg0);

	protected abstract DecodingState finishDecode(IoBuffer arg0, ProtocolDecoderOutput arg1) throws Exception;
}