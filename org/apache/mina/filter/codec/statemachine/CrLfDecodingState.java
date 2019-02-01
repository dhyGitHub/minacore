/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.statemachine;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.statemachine.DecodingState;

public abstract class CrLfDecodingState implements DecodingState {
	private static final byte CR = 13;
	private static final byte LF = 10;
	private boolean hasCR;

	public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		boolean found = false;
		boolean finished = false;

		while (in.hasRemaining()) {
			byte b = in.get();
			if (!this.hasCR) {
				if (b == 13) {
					this.hasCR = true;
					continue;
				}

				if (b == 10) {
					found = true;
				} else {
					in.position(in.position() - 1);
					found = false;
				}

				finished = true;
				break;
			}

			if (b != 10) {
				throw new ProtocolDecoderException("Expected LF after CR but was: " + (b & 255));
			}

			found = true;
			finished = true;
			break;
		}

		if (finished) {
			this.hasCR = false;
			return this.finishDecode(found, out);
		} else {
			return this;
		}
	}

	public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
		return this.finishDecode(false, out);
	}

	protected abstract DecodingState finishDecode(boolean arg0, ProtocolDecoderOutput arg1) throws Exception;
}