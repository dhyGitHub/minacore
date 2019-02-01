/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.proxy.utils;

import org.apache.mina.core.buffer.IoBuffer;

public class IoBufferDecoder {
	private IoBufferDecoder.DecodingContext ctx = new IoBufferDecoder.DecodingContext();

	public IoBufferDecoder(byte[] delimiter) {
		this.setDelimiter(delimiter, true);
	}

	public IoBufferDecoder(int contentLength) {
		this.setContentLength(contentLength, false);
	}

	public void setContentLength(int contentLength, boolean resetMatchCount) {
		if (contentLength <= 0) {
			throw new IllegalArgumentException("contentLength: " + contentLength);
		} else {
			this.ctx.setContentLength(contentLength);
			if (resetMatchCount) {
				this.ctx.setMatchCount(0);
			}

		}
	}

	public void setDelimiter(byte[] delim, boolean resetMatchCount) {
		if (delim == null) {
			throw new IllegalArgumentException("Null delimiter not allowed");
		} else {
			IoBuffer delimiter = IoBuffer.allocate(delim.length);
			delimiter.put(delim);
			delimiter.flip();
			this.ctx.setDelimiter(delimiter);
			this.ctx.setContentLength(-1);
			if (resetMatchCount) {
				this.ctx.setMatchCount(0);
			}

		}
	}

	public IoBuffer decodeFully(IoBuffer in) {
		int contentLength = this.ctx.getContentLength();
		IoBuffer decodedBuffer = this.ctx.getDecodedBuffer();
		int oldLimit = in.limit();
		int oldPos;
		if (contentLength > -1) {
			if (decodedBuffer == null) {
				decodedBuffer = IoBuffer.allocate(contentLength).setAutoExpand(true);
			}

			if (in.remaining() < contentLength) {
				oldPos = in.remaining();
				decodedBuffer.put(in);
				this.ctx.setDecodedBuffer(decodedBuffer);
				this.ctx.setContentLength(contentLength - oldPos);
				return null;
			} else {
				oldPos = in.position() + contentLength;
				in.limit(oldPos);
				decodedBuffer.put(in);
				decodedBuffer.flip();
				in.limit(oldLimit);
				this.ctx.reset();
				return decodedBuffer;
			}
		} else {
			oldPos = in.position();
			int matchCount = this.ctx.getMatchCount();
			IoBuffer delimiter = this.ctx.getDelimiter();

			while (in.hasRemaining()) {
				byte b = in.get();
				if (delimiter.get(matchCount) == b) {
					++matchCount;
					if (matchCount == delimiter.limit()) {
						int pos = in.position();
						in.position(oldPos);
						in.limit(pos);
						if (decodedBuffer == null) {
							decodedBuffer = IoBuffer.allocate(in.remaining()).setAutoExpand(true);
						}

						decodedBuffer.put(in);
						decodedBuffer.flip();
						in.limit(oldLimit);
						this.ctx.reset();
						return decodedBuffer;
					}
				} else {
					in.position(Math.max(0, in.position() - matchCount));
					matchCount = 0;
				}
			}

			if (in.remaining() > 0) {
				in.position(oldPos);
				decodedBuffer.put(in);
				in.position(in.limit());
			}

			this.ctx.setMatchCount(matchCount);
			this.ctx.setDecodedBuffer(decodedBuffer);
			return decodedBuffer;
		}
	}

	public class DecodingContext {
		private IoBuffer decodedBuffer;
		private IoBuffer delimiter;
		private int matchCount = 0;
		private int contentLength = -1;

		public void reset() {
			this.contentLength = -1;
			this.matchCount = 0;
			this.decodedBuffer = null;
		}

		public int getContentLength() {
			return this.contentLength;
		}

		public void setContentLength(int contentLength) {
			this.contentLength = contentLength;
		}

		public int getMatchCount() {
			return this.matchCount;
		}

		public void setMatchCount(int matchCount) {
			this.matchCount = matchCount;
		}

		public IoBuffer getDecodedBuffer() {
			return this.decodedBuffer;
		}

		public void setDecodedBuffer(IoBuffer decodedBuffer) {
			this.decodedBuffer = decodedBuffer;
		}

		public IoBuffer getDelimiter() {
			return this.delimiter;
		}

		public void setDelimiter(IoBuffer delimiter) {
			this.delimiter = delimiter;
		}
	}
}