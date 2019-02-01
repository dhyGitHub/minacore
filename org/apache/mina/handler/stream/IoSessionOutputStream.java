/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.handler.stream;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

class IoSessionOutputStream extends OutputStream {
	private final IoSession session;
	private WriteFuture lastWriteFuture;

	public IoSessionOutputStream(IoSession session) {
		this.session = session;
	}

	public void close() throws IOException {
		try {
			this.flush();
		} finally {
			this.session.closeNow().awaitUninterruptibly();
		}

	}

	private void checkClosed() throws IOException {
		if (!this.session.isConnected()) {
			throw new IOException("The session has been closed.");
		}
	}

	private synchronized void write(IoBuffer buf) throws IOException {
		this.checkClosed();
		WriteFuture future = this.session.write(buf);
		this.lastWriteFuture = future;
	}

	public void write(byte[] b, int off, int len) throws IOException {
		this.write(IoBuffer.wrap((byte[]) b.clone(), off, len));
	}

	public void write(int b) throws IOException {
		IoBuffer buf = IoBuffer.allocate(1);
		buf.put((byte) b);
		buf.flip();
		this.write(buf);
	}

	public synchronized void flush() throws IOException {
		if (this.lastWriteFuture != null) {
			this.lastWriteFuture.awaitUninterruptibly();
			if (!this.lastWriteFuture.isWritten()) {
				throw new IOException("The bytes could not be written to the session");
			}
		}
	}
}