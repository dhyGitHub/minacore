/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.codec.prefixedstring;

import java.nio.charset.Charset;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class PrefixedStringDecoder extends CumulativeProtocolDecoder {
	public static final int DEFAULT_PREFIX_LENGTH = 4;
	public static final int DEFAULT_MAX_DATA_LENGTH = 2048;
	private final Charset charset;
	private int prefixLength;
	private int maxDataLength;

	public PrefixedStringDecoder(Charset charset, int prefixLength, int maxDataLength) {
		this.prefixLength = 4;
		this.maxDataLength = 2048;
		this.charset = charset;
		this.prefixLength = prefixLength;
		this.maxDataLength = maxDataLength;
	}

	public PrefixedStringDecoder(Charset charset, int prefixLength) {
		this(charset, prefixLength, 2048);
	}

	public PrefixedStringDecoder(Charset charset) {
		this(charset, 4);
	}

	public void setPrefixLength(int prefixLength) {
		this.prefixLength = prefixLength;
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

	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		if (in.prefixedDataAvailable(this.prefixLength, this.maxDataLength)) {
			String msg = in.getPrefixedString(this.prefixLength, this.charset.newDecoder());
			out.write(msg);
			return true;
		} else {
			return false;
		}
	}
}