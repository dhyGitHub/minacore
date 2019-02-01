/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.write;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

public class DefaultWriteRequest implements WriteRequest {
	public static final byte[] EMPTY_MESSAGE = new byte[0];
	private static final WriteFuture UNUSED_FUTURE = new WriteFuture() {
		public boolean isWritten() {
			return false;
		}

		public void setWritten() {
		}

		public IoSession getSession() {
			return null;
		}

		public void join() {
		}

		public boolean join(long timeoutInMillis) {
			return true;
		}

		public boolean isDone() {
			return true;
		}

		public WriteFuture addListener(IoFutureListener<?> listener) {
			throw new IllegalStateException("You can\'t add a listener to a dummy future.");
		}

		public WriteFuture removeListener(IoFutureListener<?> listener) {
			throw new IllegalStateException("You can\'t add a listener to a dummy future.");
		}

		public WriteFuture await() throws InterruptedException {
			return this;
		}

		public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
			return true;
		}

		public boolean await(long timeoutMillis) throws InterruptedException {
			return true;
		}

		public WriteFuture awaitUninterruptibly() {
			return this;
		}

		public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
			return true;
		}

		public boolean awaitUninterruptibly(long timeoutMillis) {
			return true;
		}

		public Throwable getException() {
			return null;
		}

		public void setException(Throwable cause) {
		}
	};
	private final Object message;
	private final WriteFuture future;
	private final SocketAddress destination;

	public DefaultWriteRequest(Object message) {
		this(message, (WriteFuture) null, (SocketAddress) null);
	}

	public DefaultWriteRequest(Object message, WriteFuture future) {
		this(message, future, (SocketAddress) null);
	}

	public DefaultWriteRequest(Object message, WriteFuture future, SocketAddress destination) {
		if (message == null) {
			throw new IllegalArgumentException("message");
		} else {
			if (future == null) {
				future = UNUSED_FUTURE;
			}

			this.message = message;
			this.future = future;
			this.destination = destination;
		}
	}

	public WriteFuture getFuture() {
		return this.future;
	}

	public Object getMessage() {
		return this.message;
	}

	public WriteRequest getOriginalRequest() {
		return this;
	}

	public SocketAddress getDestination() {
		return this.destination;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("WriteRequest: ");
		if (this.message.getClass().getName().equals(Object.class.getName())) {
			sb.append("CLOSE_REQUEST");
		} else if (this.getDestination() == null) {
			sb.append(this.message);
		} else {
			sb.append(this.message);
			sb.append(" => ");
			sb.append(this.getDestination());
		}

		return sb.toString();
	}

	public boolean isEncoded() {
		return false;
	}
}