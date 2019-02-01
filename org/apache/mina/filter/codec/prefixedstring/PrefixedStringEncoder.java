/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.prefixedstring;

import java.nio.charset.Charset;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class PrefixedStringEncoder extends ProtocolEncoderAdapter {
	public static final int DEFAULT_PREFIX_LENGTH = 4;
	public static final int DEFAULT_MAX_DATA_LENGTH = 2048;
	private final Charset charset;
	private int prefixLength;
	private int maxDataLength;

	public PrefixedStringEncoder(Charset charset, int prefixLength, int maxDataLength) {
		this.prefixLength = 4;
		this.maxDataLength = 2048;
		this.charset = charset;
		this.prefixLength = prefixLength;
		this.maxDataLength = maxDataLength;
	}

	public PrefixedStringEncoder(Charset charset, int prefixLength) {
		this(charset, prefixLength, 2048);
	}

	public PrefixedStringEncoder(Charset charset) {
		this(charset, 4);
	}

	public PrefixedStringEncoder() {
		this(Charset.defaultCharset());
	}

	public void setPrefixLength(int prefixLength) {
		if (prefixLength != 1 && prefixLength != 2 && prefixLength != 4) {
			throw new IllegalArgumentException("prefixLength: " + prefixLength);
		} else {
			this.prefixLength = prefixLength;
		}
	}

	public int getPrefixLength() {
		return this.prefixLength;
	}

	public void setMaxDataLength(int maxDataLength) {
		this.maxDataLength = maxDataLength;
	}

	public int getMaxDataLength() {
		return this.maxDataLength;
	}

	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		String value = (String) message;
		IoBuffer buffer = IoBuffer.allocate(value.length()).setAutoExpand(true);
		buffer.putPrefixedString(value, this.prefixLength, this.charset.newEncoder());
		if (buffer.position() > this.maxDataLength) {
			throw new IllegalArgumentException("Data length: " + buffer.position());
		} else {
			buffer.flip();
			out.write(buffer);
		}
	}
}