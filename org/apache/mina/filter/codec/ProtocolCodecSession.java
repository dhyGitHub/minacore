/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import java.util.Queue;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.DummySession;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.AbstractProtocolDecoderOutput;
import org.apache.mina.filter.codec.AbstractProtocolEncoderOutput;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class ProtocolCodecSession extends DummySession {
	private final WriteFuture notWrittenFuture = DefaultWriteFuture.newNotWrittenFuture(this,
			new UnsupportedOperationException());
	private final AbstractProtocolEncoderOutput encoderOutput = new AbstractProtocolEncoderOutput() {
		public WriteFuture flush() {
			return ProtocolCodecSession.this.notWrittenFuture;
		}
	};
	private final AbstractProtocolDecoderOutput decoderOutput = new AbstractProtocolDecoderOutput() {
		public void flush(NextFilter nextFilter, IoSession session) {
		}
	};

	public ProtocolEncoderOutput getEncoderOutput() {
		return this.encoderOutput;
	}

	public Queue<Object> getEncoderOutputQueue() {
		return this.encoderOutput.getMessageQueue();
	}

	public ProtocolDecoderOutput getDecoderOutput() {
		return this.decoderOutput;
	}

	public Queue<Object> getDecoderOutputQueue() {
		return this.decoderOutput.getMessageQueue();
	}
}