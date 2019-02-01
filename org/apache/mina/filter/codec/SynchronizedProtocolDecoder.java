/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class SynchronizedProtocolDecoder implements ProtocolDecoder {
	private final ProtocolDecoder decoder;

	public SynchronizedProtocolDecoder(ProtocolDecoder decoder) {
		if (decoder == null) {
			throw new IllegalArgumentException("decoder");
		} else {
			this.decoder = decoder;
		}
	}

	public ProtocolDecoder getDecoder() {
		return this.decoder;
	}

	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		ProtocolDecoder arg3 = this.decoder;
		synchronized (this.decoder) {
			this.decoder.decode(session, in, out);
		}
	}

	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
		ProtocolDecoder arg2 = this.decoder;
		synchronized (this.decoder) {
			this.decoder.finishDecode(session, out);
		}
	}

	public void dispose(IoSession session) throws Exception {
		ProtocolDecoder arg1 = this.decoder;
		synchronized (this.decoder) {
			this.decoder.dispose(session);
		}
	}
}