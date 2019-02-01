/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.handler.stream;

import java.io.IOException;
import java.io.InputStream;
import org.apache.mina.core.buffer.IoBuffer;

class IoSessionInputStream extends InputStream {
	private final Object mutex = new Object();
	private final IoBuffer buf = IoBuffer.allocate(16);
	private volatile boolean closed;
	private volatile boolean released;
	private IOException exception;

	public IoSessionInputStream() {
		this.buf.setAutoExpand(true);
		this.buf.limit(0);
	}

	public int available() {
		if (this.released) {
			return 0;
		} else {
			Object arg0 = this.mutex;
			synchronized (this.mutex) {
				return this.buf.remaining();
			}
		}
	}

	public void close() {
		if (!this.closed) {
			Object arg0 = this.mutex;
			synchronized (this.mutex) {
				this.closed = true;
				this.releaseBuffer();
				this.mutex.notifyAll();
			}
		}
	}

	public int read() throws IOException {
		Object arg0 = this.mutex;
		synchronized (this.mutex) {
			return !this.waitForData() ? -1 : this.buf.get() & 255;
		}
	}

	public int read(byte[] b, int off, int len) throws IOException {
		Object arg3 = this.mutex;
		synchronized (this.mutex) {
			if (!this.waitForData()) {
				return -1;
			} else {
				int readBytes;
				if (len > this.buf.remaining()) {
					readBytes = this.buf.remaining();
				} else {
					readBytes = len;
				}

				this.buf.get(b, off, readBytes);
				return readBytes;
			}
		}
	}

	private boolean waitForData() throws IOException {
		if (this.released) {
			return false;
		} else {
			Object arg0 = this.mutex;
			synchronized (this.mutex) {
				while (!this.released && this.buf.remaining() == 0 && this.exception == null) {
					try {
						this.mutex.wait();
					} catch (InterruptedException arg4) {
						IOException ioe = new IOException("Interrupted while waiting for more data");
						ioe.initCause(arg4);
						throw ioe;
					}
				}
			}

			if (this.exception != null) {
				this.releaseBuffer();
				throw this.exception;
			} else if (this.closed && this.buf.remaining() == 0) {
				this.releaseBuffer();
				return false;
			} else {
				return true;
			}
		}
	}

	private void releaseBuffer() {
		if (!this.released) {
			this.released = true;
		}
	}

	public void write(IoBuffer src) {
		Object arg1 = this.mutex;
		synchronized (this.mutex) {
			if (!this.closed) {
				if (this.buf.hasRemaining()) {
					this.buf.compact();
					this.buf.put(src);
					this.buf.flip();
				} else {
					this.buf.clear();
					this.buf.put(src);
					this.buf.flip();
					this.mutex.notifyAll();
				}

			}
		}
	}

	public void throwException(IOException e) {
		Object arg1 = this.mutex;
		synchronized (this.mutex) {
			if (this.exception == null) {
				this.exception = e;
				this.mutex.notifyAll();
			}

		}
	}
}