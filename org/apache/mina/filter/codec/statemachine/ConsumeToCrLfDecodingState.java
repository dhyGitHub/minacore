/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.statemachine.DecodingState;

public abstract class ConsumeToCrLfDecodingState implements DecodingState {
	private static final byte CR = 13;
	private static final byte LF = 10;
	private boolean lastIsCR;
	private IoBuffer buffer;

	public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		int beginPos = in.position();
		int limit = in.limit();
		int terminatorPos = -1;

		for (int product = beginPos; product < limit; ++product) {
			byte endPos = in.get(product);
			if (endPos == 13) {
				this.lastIsCR = true;
			} else {
				if (endPos == 10 && this.lastIsCR) {
					terminatorPos = product;
					break;
				}

				this.lastIsCR = false;
			}
		}

		if (terminatorPos >= 0) {
			int arg8 = terminatorPos - 1;
			IoBuffer arg7;
			if (beginPos < arg8) {
				in.limit(arg8);
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
			in.position(beginPos);
			if (this.buffer == null) {
				this.buffer = IoBuffer.allocate(in.remaining());
				this.buffer.setAutoExpand(true);
			}

			this.buffer.put(in);
			if (this.lastIsCR) {
				this.buffer.position(this.buffer.position() - 1);
			}

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