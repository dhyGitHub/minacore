/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.buffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.buffer.IoBufferLazyInitializer;
import org.apache.mina.util.LazyInitializedCacheMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BufferedWriteFilter extends IoFilterAdapter {
	private final Logger logger;
	public static final int DEFAULT_BUFFER_SIZE = 8192;
	private int bufferSize;
	private final LazyInitializedCacheMap<IoSession, IoBuffer> buffersMap;

	public BufferedWriteFilter() {
		this(8192, (LazyInitializedCacheMap) null);
	}

	public BufferedWriteFilter(int bufferSize) {
		this(bufferSize, (LazyInitializedCacheMap) null);
	}

	public BufferedWriteFilter(int bufferSize, LazyInitializedCacheMap<IoSession, IoBuffer> buffersMap) {
		this.logger = LoggerFactory.getLogger(BufferedWriteFilter.class);
		this.bufferSize = 8192;
		this.bufferSize = bufferSize;
		if (buffersMap == null) {
			this.buffersMap = new LazyInitializedCacheMap();
		} else {
			this.buffersMap = buffersMap;
		}

	}

	public int getBufferSize() {
		return this.bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		Object data = writeRequest.getMessage();
		if (data instanceof IoBuffer) {
			this.write(session, (IoBuffer) data);
		} else {
			throw new IllegalArgumentException("This filter should only buffer IoBuffer objects");
		}
	}

	private void write(IoSession session, IoBuffer data) {
		IoBuffer dest = (IoBuffer) this.buffersMap.putIfAbsent(session, new IoBufferLazyInitializer(this.bufferSize));
		this.write(session, data, dest);
	}

	private void write(IoSession session, IoBuffer data, IoBuffer buf) {
		try {
			int e = data.remaining();
			if (e >= buf.capacity()) {
				NextFilter nextFilter = session.getFilterChain().getNextFilter(this);
				this.internalFlush(nextFilter, session, buf);
				nextFilter.filterWrite(session, new DefaultWriteRequest(data));
				return;
			}

			if (e > buf.limit() - buf.position()) {
				this.internalFlush(session.getFilterChain().getNextFilter(this), session, buf);
			}

			synchronized (buf) {
				buf.put(data);
			}
		} catch (Exception arg7) {
			session.getFilterChain().fireExceptionCaught(arg7);
		}

	}

	private void internalFlush(NextFilter nextFilter, IoSession session, IoBuffer buf) throws Exception {
		IoBuffer tmp = null;
		synchronized (buf) {
			buf.flip();
			tmp = buf.duplicate();
			buf.clear();
		}

		this.logger.debug("Flushing buffer: {}", tmp);
		nextFilter.filterWrite(session, new DefaultWriteRequest(tmp));
	}

	public void flush(IoSession session) {
		try {
			this.internalFlush(session.getFilterChain().getNextFilter(this), session,
					(IoBuffer) this.buffersMap.get(session));
		} catch (Exception arg2) {
			session.getFilterChain().fireExceptionCaught(arg2);
		}

	}

	private void free(IoSession session) {
		IoBuffer buf = (IoBuffer) this.buffersMap.remove(session);
		if (buf != null) {
			buf.free();
		}

	}

	public void exceptionCaught(NextFilter nextFilter, IoSession session, Throwable cause) throws Exception {
		this.free(session);
		nextFilter.exceptionCaught(session, cause);
	}

	public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
		this.free(session);
		nextFilter.sessionClosed(session);
	}
}