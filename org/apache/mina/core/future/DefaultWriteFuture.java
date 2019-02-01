/*** Eclipse Class Decompiler plugin, copyright (c) 2016 Chen Chao (cnfree2000@hotmail.com) ***/
package org.apache.mina.core.future;

import org.apache.mina.core.future.DefaultIoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;

public class DefaultWriteFuture extends DefaultIoFuture implements WriteFuture {
	public static WriteFuture newWrittenFuture(IoSession session) {
		DefaultWriteFuture writtenFuture = new DefaultWriteFuture(session);
		writtenFuture.setWritten();
		return writtenFuture;
	}

	public static WriteFuture newNotWrittenFuture(IoSession session, Throwable cause) {
		DefaultWriteFuture unwrittenFuture = new DefaultWriteFuture(session);
		unwrittenFuture.setException(cause);
		return unwrittenFuture;
	}

	public DefaultWriteFuture(IoSession session) {
		super(session);
	}

	public boolean isWritten() {
		if (this.isDone()) {
			Object v = this.getValue();
			if (v instanceof Boolean) {
				return ((Boolean) v).booleanValue();
			}
		}

		return false;
	}

	public Throwable getException() {
		if (this.isDone()) {
			Object v = this.getValue();
			if (v instanceof Throwable) {
				return (Throwable) v;
			}
		}

		return null;
	}

	public void setWritten() {
		this.setValue(Boolean.TRUE);
	}

	public void setException(Throwable exception) {
		if (exception == null) {
			throw new IllegalArgumentException("exception");
		} else {
			this.setValue(exception);
		}
	}

	public WriteFuture await() throws InterruptedException {
		return (WriteFuture) super.await();
	}

	public WriteFuture awaitUninterruptibly() {
		return (WriteFuture) super.awaitUninterruptibly();
	}

	public WriteFuture addListener(IoFutureListener<?> listener) {
		return (WriteFuture) super.addListener(listener);
	}

	public WriteFuture removeListener(IoFutureListener<?> listener) {
		return (WriteFuture) super.removeListener(listener);
	}
}