/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.filter.stream;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;

public abstract class AbstractStreamWriteFilter<T> extends IoFilterAdapter {
	public static final int DEFAULT_STREAM_BUFFER_SIZE = 4096;
	protected final AttributeKey CURRENT_STREAM = new AttributeKey(this.getClass(), "stream");
	protected final AttributeKey WRITE_REQUEST_QUEUE = new AttributeKey(this.getClass(), "queue");
	protected final AttributeKey CURRENT_WRITE_REQUEST = new AttributeKey(this.getClass(), "writeRequest");
	private int writeBufferSize = 4096;

	public void onPreAdd(IoFilterChain parent, String name, NextFilter nextFilter) throws Exception {
		Class clazz = this.getClass();
		if (parent.contains(clazz)) {
			throw new IllegalStateException("Only one " + clazz.getName() + " is permitted.");
		}
	}

	public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		Object message;
		if (session.getAttribute(this.CURRENT_STREAM) != null) {
			message = this.getWriteRequestQueue(session);
			if (message == null) {
				message = new ConcurrentLinkedQueue();
				session.setAttribute(this.WRITE_REQUEST_QUEUE, message);
			}

			((Queue) message).add(writeRequest);
		} else {
			message = writeRequest.getMessage();
			if (this.getMessageClass().isInstance(message)) {
				Object stream = this.getMessageClass().cast(message);
				IoBuffer buffer = this.getNextBuffer(stream);
				if (buffer == null) {
					writeRequest.getFuture().setWritten();
					nextFilter.messageSent(session, writeRequest);
				} else {
					session.setAttribute(this.CURRENT_STREAM, message);
					session.setAttribute(this.CURRENT_WRITE_REQUEST, writeRequest);
					nextFilter.filterWrite(session, new DefaultWriteRequest(buffer));
				}
			} else {
				nextFilter.filterWrite(session, writeRequest);
			}

		}
	}

	protected abstract Class<T> getMessageClass();

	private Queue<WriteRequest> getWriteRequestQueue(IoSession session) {
		return (Queue) session.getAttribute(this.WRITE_REQUEST_QUEUE);
	}

	private Queue<WriteRequest> removeWriteRequestQueue(IoSession session) {
		return (Queue) session.removeAttribute(this.WRITE_REQUEST_QUEUE);
	}

	public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
		Object stream = this.getMessageClass().cast(session.getAttribute(this.CURRENT_STREAM));
		if (stream == null) {
			nextFilter.messageSent(session, writeRequest);
		} else {
			IoBuffer buffer = this.getNextBuffer(stream);
			if (buffer == null) {
				session.removeAttribute(this.CURRENT_STREAM);
				WriteRequest currentWriteRequest = (WriteRequest) session.removeAttribute(this.CURRENT_WRITE_REQUEST);
				Queue queue = this.removeWriteRequestQueue(session);
				if (queue != null) {
					for (WriteRequest wr = (WriteRequest) queue.poll(); wr != null; wr = (WriteRequest) queue.poll()) {
						this.filterWrite(nextFilter, session, wr);
					}
				}

				currentWriteRequest.getFuture().setWritten();
				nextFilter.messageSent(session, currentWriteRequest);
			} else {
				nextFilter.filterWrite(session, new DefaultWriteRequest(buffer));
			}
		}

	}

	public int getWriteBufferSize() {
		return this.writeBufferSize;
	}

	public void setWriteBufferSize(int writeBufferSize) {
		if (writeBufferSize < 1) {
			throw new IllegalArgumentException("writeBufferSize must be at least 1");
		} else {
			this.writeBufferSize = writeBufferSize;
		}
	}

	protected abstract IoBuffer getNextBuffer(T arg0) throws IOException;
}