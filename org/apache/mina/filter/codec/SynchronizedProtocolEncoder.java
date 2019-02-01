/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class SynchronizedProtocolEncoder implements ProtocolEncoder {
	private final ProtocolEncoder encoder;

	public SynchronizedProtocolEncoder(ProtocolEncoder encoder) {
		if (encoder == null) {
			throw new IllegalArgumentException("encoder");
		} else {
			this.encoder = encoder;
		}
	}

	public ProtocolEncoder getEncoder() {
		return this.encoder;
	}

	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		ProtocolEncoder arg3 = this.encoder;
		synchronized (this.encoder) {
			this.encoder.encode(session, message, out);
		}
	}

	public void dispose(IoSession session) throws Exception {
		ProtocolEncoder arg1 = this.encoder;
		synchronized (this.encoder) {
			this.encoder.dispose(session);
		}
	}
}