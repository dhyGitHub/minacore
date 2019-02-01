/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.serialization;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class ObjectSerializationDecoder extends CumulativeProtocolDecoder {
	private final ClassLoader classLoader;
	private int maxObjectSize;

	public ObjectSerializationDecoder() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public ObjectSerializationDecoder(ClassLoader classLoader) {
		this.maxObjectSize = 1048576;
		if (classLoader == null) {
			throw new IllegalArgumentException("classLoader");
		} else {
			this.classLoader = classLoader;
		}
	}

	public int getMaxObjectSize() {
		return this.maxObjectSize;
	}

	public void setMaxObjectSize(int maxObjectSize) {
		if (maxObjectSize <= 0) {
			throw new IllegalArgumentException("maxObjectSize: " + maxObjectSize);
		} else {
			this.maxObjectSize = maxObjectSize;
		}
	}

	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (!in.prefixedDataAvailable(4, this.maxObjectSize)) {
			return false;
		} else {
			out.write(in.getObject(this.classLoader));
			return true;
		}
	}
}