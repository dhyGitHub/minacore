/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.stream;

import java.io.IOException;
import java.io.InputStream;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.stream.AbstractStreamWriteFilter;

public class StreamWriteFilter extends AbstractStreamWriteFilter<InputStream> {
	protected IoBuffer getNextBuffer(InputStream is) throws IOException {
		byte[] bytes = new byte[this.getWriteBufferSize()];
		int off = 0;

		int n;
		for (n = 0; off < bytes.length && (n = is.read(bytes, off, bytes.length - off)) != -1; off += n) {
			;
		}

		if (n == -1 && off == 0) {
			return null;
		} else {
			IoBuffer buffer = IoBuffer.wrap(bytes, 0, off);
			return buffer;
		}
	}

	protected Class<InputStream> getMessageClass() {
		return InputStream.class;
	}
}