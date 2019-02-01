/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.statemachine;

import java.util.ArrayList;
import java.util.List;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.statemachine.DecodingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DecodingStateMachine implements DecodingState {
	private final Logger log = LoggerFactory.getLogger(DecodingStateMachine.class);
	private final List<Object> childProducts = new ArrayList();
	private final ProtocolDecoderOutput childOutput = new ProtocolDecoderOutput() {
		public void flush(NextFilter nextFilter, IoSession session) {
		}

		public void write(Object message) {
			DecodingStateMachine.this.childProducts.add(message);
		}
	};
	private DecodingState currentState;
	private boolean initialized;

	protected abstract DecodingState init() throws Exception;

	protected abstract DecodingState finishDecode(List<Object> arg0, ProtocolDecoderOutput arg1) throws Exception;

	protected abstract void destroy() throws Exception;

	public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		DecodingState state = this.getCurrentState();
		int limit = in.limit();
		int pos = in.position();

		DecodingStateMachine e1;
		try {
			while (pos != limit) {
				DecodingState e = state;
				state = state.decode(in, this.childOutput);
				if (state == null) {
					DecodingState newPos1 = this.finishDecode(this.childProducts, out);
					return newPos1;
				}

				int newPos = in.position();
				if (newPos == pos && e == state) {
					break;
				}

				pos = newPos;
			}

			e1 = this;
		} catch (Exception arg10) {
			state = null;
			throw arg10;
		} finally {
			this.currentState = state;
			if (state == null) {
				this.cleanup();
			}

		}

		return e1;
	}

	public DecodingState finishDecode(ProtocolDecoderOutput out) throws Exception {
		DecodingState state = this.getCurrentState();

		DecodingState nextState;
		try {
			DecodingState e;
			try {
				do {
					e = state;
					state = state.finishDecode(this.childOutput);
				} while (state != null && e != state);
			} catch (Exception arg7) {
				state = null;
				this.log.debug("Ignoring the exception caused by a closed session.", arg7);
			}
		} finally {
			this.currentState = state;
			nextState = this.finishDecode(this.childProducts, out);
			if (state == null) {
				this.cleanup();
			}

		}

		return nextState;
	}

	private void cleanup() {
		if (!this.initialized) {
			throw new IllegalStateException();
		} else {
			this.initialized = false;
			this.childProducts.clear();

			try {
				this.destroy();
			} catch (Exception arg1) {
				this.log.warn("Failed to destroy a decoding state machine.", arg1);
			}

		}
	}

	private DecodingState getCurrentState() throws Exception {
		DecodingState state = this.currentState;
		if (state == null) {
			state = this.init();
			this.initialized = true;
		}

		return state;
	}
}